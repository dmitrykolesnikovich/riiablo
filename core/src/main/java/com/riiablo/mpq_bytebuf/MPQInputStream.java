package com.riiablo.mpq_bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FilenameUtils;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;
import com.riiablo.mpq_bytebuf.util.Decompressor;
import com.riiablo.mpq_bytebuf.util.Decryptor;
import com.riiablo.mpq_bytebuf.util.Exploder;

import static com.riiablo.mpq_bytebuf.MPQ.Block.FLAG_COMPRESSED;
import static com.riiablo.mpq_bytebuf.MPQ.Block.FLAG_ENCRYPTED;
import static com.riiablo.mpq_bytebuf.MPQ.Block.FLAG_EXISTS;
import static com.riiablo.mpq_bytebuf.MPQ.Block.FLAG_FIX_KEY;
import static com.riiablo.mpq_bytebuf.MPQ.Block.FLAG_IMPLODE;

public class MPQInputStream extends InputStream {
  private static final Logger log = LogManager.getLogger(MPQInputStream.class);

  private static final boolean DEBUG_MODE = !true;

  private static final int COMPRESSED_OR_IMPLODE = FLAG_COMPRESSED | FLAG_IMPLODE;

  final MPQFileHandle handle;
  final MPQ.Block block;
  final ByteBuf buffer;
  final int blockOffset;
  final int sectorSize;
  final int sectorCount;
  final ByteBuf sectorOffsets;
  final int encryptionKey;
  final int FSize;
  int curSector;
  int nextSectorOffset;
  int bytesRead;
  int decompressedBytes;

  final boolean releaseOnClose;
  boolean closed;

  public static InputStream open(MPQFileHandle handle) throws IOException {
    return open(handle, false, true);
  }

  public static InputStream open(MPQFileHandle handle, boolean buffered, boolean releaseOnClose) throws IOException {
    final MPQ.Block block = handle.block;
    if (block == null) {
      throw new FileNotFoundException("File not found: " + handle.name());
    }

    if (!buffered && (block.flags & ~FLAG_EXISTS) == 0) {
      assert block.CSize == block.FSize : "file(" + handle + ") block(" + block + ") CSize(" + block.CSize + ") != FSize(" + block.FSize + ")";
      return new ByteBufInputStream(handle.mpq.buffer(block.offset).readRetainedSlice(block.FSize), releaseOnClose);
    } else {
      return new MPQInputStream(handle, releaseOnClose);
    }
  }

  public MPQInputStream(MPQFileHandle handle, boolean releaseOnClose) throws IOException {
    block = handle.block;
    if (block == null) {
      throw new FileNotFoundException("File not found: " + handle.name());
    }

    this.handle = handle;
    this.releaseOnClose = releaseOnClose;
    final MPQ mpq = handle.mpq;
    assert (block.flags & FLAG_EXISTS) == FLAG_EXISTS : "file(" + handle + ") does not exist!";
    final int flags = block.flags;
    final int offset = blockOffset = block.offset;
    final int CSize = block.CSize;
    FSize = block.FSize;

    log.tracef("Accessing %s+%x:%s", mpq, offset, handle.name());
    final ByteBuf archive = mpq.buffer(offset);
    if ((flags & ~FLAG_EXISTS) == 0) { // FIXME: Note it is assumed that ByteBufInputStream be used in this case
      assert CSize == FSize : "file(" + handle + ") block(" + block + ") CSize(" + CSize + ") != FSize(" + FSize + ")";
      sectorSize = FSize;
      sectorCount = 1;
      sectorOffsets = Unpooled.EMPTY_BUFFER; // TODO: 1 element = max length?
      encryptionKey = 0;
      buffer = archive.readRetainedSlice(FSize);
      return;
    }

    sectorSize = mpq.sectorSize;
    sectorCount = (FSize + sectorSize - 1) / sectorSize;
    sectorOffsets = mpq.alloc().heapBuffer(sectorCount << 2);
    encryptionKey = getEncryptionKey(handle.filename, flags, offset, FSize);
    buffer = mpq.obtainHeapBuffer(sectorSize);

    log.trace("Populating sector offsets...");
    archive.readBytes(sectorOffsets);
    if ((flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED) {
      log.trace("Decrypting sector offsets...");
      Decryptor.decrypt(encryptionKey - 1, sectorOffsets);
    }

    sectorOffsets.writeIntLE(CSize);

    if (log.traceEnabled()) {
      final StringBuilder builder = new StringBuilder(256);
      for (int i = 0, s = sectorCount; i <= s; i++) {
        builder.append(Integer.toHexString(MPQ.readSafeUnsignedIntLE(sectorOffsets))).append(',');
      }
      if (builder.length() > 0) builder.setLength(builder.length() - 1);
      log.trace("sector offsets: {}+[{}]", Integer.toHexString(offset), builder);
      sectorOffsets.resetReaderIndex();
    }

    nextSectorOffset = MPQ.readSafeUnsignedIntLE(sectorOffsets);
  }

  @Override
  public int available() {
    return FSize - bytesRead;
  }

  public int cachedBytes() {
    return buffer.readableBytes();
  }

  @Override
  public int read() {
    if (available() <= 0) {
      return -1;
    }

    if (!buffer.isReadable()) {
      readSector();
    }

    bytesRead++;
    if (DEBUG_MODE) log.trace("Read {} bytes ({} bytes remaining)", 1, available());
    return buffer.readUnsignedByte();
  }

  @Override
  public int read(byte[] b) {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) {
    if (available() <= 0) {
      return -1;
    }

    int bytesRead = 0;
    final ByteBuf dst = Unpooled.wrappedBuffer(b, off, len).setIndex(0, 0);
    while (dst.isWritable() && available() > 0) {
      if (!buffer.isReadable()) {
        readSector();
      }

      final int writableBytes = Math.min(dst.writableBytes(), buffer.readableBytes());
      if (DEBUG_MODE) log.trace("Copying {} bytes", writableBytes);
      bytesRead += writableBytes;
      buffer.readBytes(dst, writableBytes);
    }

    this.bytesRead += bytesRead;
    if (DEBUG_MODE) log.trace("Read {} bytes ({} bytes remaining)", bytesRead, available());
    return bytesRead;
  }

  void readSector() {
    log.tracefEntry("readSector(%s+%x:%s)", handle.mpq, nextSectorOffset, handle.name());
    if (curSector >= sectorCount) {
      throw new IllegalStateException(
          "curSector(" + curSector + ") >= sectorCount(" + sectorCount + ")");
    }

    final int flags = block.flags;
    assert (flags & COMPRESSED_OR_IMPLODE) != 0 : "block(" + block + ") is neither compressed or imploded";

    try {
      MDC.put("sector", curSector);
      if (log.traceEnabled()) log.trace("flags={}", block.getFlagsString());
      final int sectorOffset = nextSectorOffset;
      nextSectorOffset = MPQ.readSafeUnsignedIntLE(sectorOffsets);
      final int sectorCSize = nextSectorOffset - sectorOffset;
      final int sectorFSize = Math.min(FSize - decompressedBytes, sectorSize);
      log.debug("Reading sector {} / {} ({} bytes)", curSector, sectorCount - 1, sectorCSize);

      handle.mpq.buffer(blockOffset + sectorOffset).readBytes(buffer.setIndex(0, 0), sectorCSize);
      if (DEBUG_MODE) log.trace("sector: {}", buffer);

      if ((flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED) {
        if (DEBUG_MODE) log.trace("Decrypting sector...");
        Decryptor.decrypt(encryptionKey + curSector, buffer);
        if (DEBUG_MODE) log.trace("Decrypted {} bytes", buffer.writerIndex());
      }

      if ((flags & FLAG_COMPRESSED) == FLAG_COMPRESSED && sectorCSize != sectorFSize) {
        if (DEBUG_MODE) log.trace("Decompressing sector...");
        Decompressor.decompress(buffer, sectorCSize, sectorFSize);
        if (DEBUG_MODE) log.trace("Decompressed {} bytes", buffer.writerIndex());
      }

      if ((flags & FLAG_IMPLODE) == FLAG_IMPLODE && sectorCSize != sectorFSize) {
        if (DEBUG_MODE) log.trace("Exploding sector...");
        Exploder.pkexplode(buffer);
        if (DEBUG_MODE) log.trace("Exploded {} bytes", buffer.writerIndex());
      }

      decompressedBytes += buffer.readableBytes();
      curSector++;
    } finally {
      MDC.remove("sector");
    }
  }

  @Override
  public void close() throws IOException {
    try {
      super.close();
    } finally {
      if (!closed) sectorOffsets.release();
      if (releaseOnClose && !closed) {
        closed = true;
        buffer.release();
      }
    }
  }

  public static ByteBuf readByteBuf(MPQFileHandle handle) {
    final MPQ mpq = handle.mpq;
    final MPQ.Block block = handle.block;
    assert (block.flags & FLAG_EXISTS) == FLAG_EXISTS : "file(" + handle + ") does not exist!";
    final int flags = block.flags;
    final int offset = block.offset;
    final int CSize = block.CSize;
    final int FSize = block.FSize;

    log.tracef("Accessing %s+%x:%s", mpq, offset, handle.name());
    final ByteBuf archive = mpq.buffer(offset);
    if ((flags & ~FLAG_EXISTS) == 0) {
      assert CSize == FSize : "file(" + handle + ") block(" + block + ") CSize(" + CSize + ") != FSize(" + FSize + ")";
      return archive.readRetainedSlice(FSize);
    }

    final int sectorSize = mpq.sectorSize;
    final int sectorCount = (FSize + sectorSize - 1) / sectorSize;
    final ByteBuf buffer = mpq.alloc().heapBuffer(FSize, FSize);

    if (log.traceEnabled()) log.trace("flags={}", block.getFlagsString());
    assert (flags & COMPRESSED_OR_IMPLODE) != 0 : "block(" + block + ") is neither compressed or imploded";

    log.trace("Populating sector offsets...");
    final ByteBuf sectorOffsets = mpq.alloc().heapBuffer(sectorCount << 2);
    try {
      final int encryptionKey = getEncryptionKey(handle.filename, flags, offset, FSize);
      archive.readBytes(sectorOffsets);
      if ((flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED) {
        log.trace("Decrypting sector offsets...");
        Decryptor.decrypt(encryptionKey - 1, sectorOffsets);
      }

      sectorOffsets.writeIntLE(CSize);

      if (log.traceEnabled()) {
        final StringBuilder builder = new StringBuilder(256);
        for (int i = 0, s = sectorCount; i <= s; i++) {
          builder.append(Integer.toHexString(MPQ.readSafeUnsignedIntLE(sectorOffsets))).append(',');
        }
        if (builder.length() > 0) builder.setLength(builder.length() - 1);
        log.trace("sector offsets: {}+[{}]", Integer.toHexString(offset), builder);
        sectorOffsets.resetReaderIndex();
      }

      int decompressedBytes = 0;
      int nextSectorOffset = MPQ.readSafeUnsignedIntLE(sectorOffsets);
      for (int curSector = 0, s = sectorCount; curSector < s; curSector++) {
        try {
          MDC.put("sector", curSector);
          final int sectorOffset = nextSectorOffset;
          nextSectorOffset = MPQ.readSafeUnsignedIntLE(sectorOffsets);
          final int sectorCSize = nextSectorOffset - sectorOffset;
          final int sectorFSize = Math.min(FSize - decompressedBytes, sectorSize);
          log.debug("Reading sector {} / {} ({} bytes)", curSector, s - 1, sectorCSize);
          archive.readerIndex(offset + sectorOffset);

          final ByteBuf sector = buffer.slice(buffer.writerIndex(), sectorFSize);
          buffer.writerIndex(buffer.writerIndex() + sectorFSize);
          archive.readBytes(sector.setIndex(0, 0), sectorCSize);
          if (DEBUG_MODE) log.trace("sector: {}", sector);

          if ((flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED) {
            if (DEBUG_MODE) log.trace("Decrypting sector...");
            Decryptor.decrypt(encryptionKey + curSector, sector);
            if (DEBUG_MODE) log.trace("Decrypted {} bytes", sector.writerIndex());
          }

          if ((flags & FLAG_COMPRESSED) == FLAG_COMPRESSED && sectorCSize != sectorFSize) {
            if (DEBUG_MODE) log.trace("Decompressing sector...");
            Decompressor.decompress(sector, sectorCSize, sectorFSize);
            if (DEBUG_MODE) log.trace("Decompressed {} bytes", sector.writerIndex());
          }

          if ((flags & FLAG_IMPLODE) == FLAG_IMPLODE && sectorCSize != sectorFSize) {
            if (DEBUG_MODE) log.trace("Exploding sector...");
            Exploder.pkexplode(sector);
            if (DEBUG_MODE) log.trace("Exploded {} bytes", sector.writerIndex());
          }

          decompressedBytes += sector.readableBytes();
        } finally {
          MDC.remove("sector");
        }
      }
    } finally {
      sectorOffsets.release();
    }

    return buffer;
  }

  private static int getEncryptionKey(final String filename, final int flags, final int offset, final int FSize) {
    if ((flags & FLAG_ENCRYPTED) == FLAG_ENCRYPTED) {
      final String basename = FilenameUtils.getName(filename);
      final int encryptionKey = Decryptor.HASH_ENCRYPTION_KEY.hash(basename);
      return (flags & FLAG_FIX_KEY) == FLAG_FIX_KEY
          ? (encryptionKey + offset) ^ FSize
          : encryptionKey;
    }

    return 0;
  }
}
