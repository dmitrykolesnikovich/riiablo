// automatically generated by the FlatBuffers compiler, do not modify

package com.riiablo.net.packet.d2gs;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class SwapBeltItem extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_1_12_0(); }
  public static SwapBeltItem getRootAsSwapBeltItem(ByteBuffer _bb) { return getRootAsSwapBeltItem(_bb, new SwapBeltItem()); }
  public static SwapBeltItem getRootAsSwapBeltItem(ByteBuffer _bb, SwapBeltItem obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public SwapBeltItem __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int itemId() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }

  public static int createSwapBeltItem(FlatBufferBuilder builder,
      int itemId) {
    builder.startTable(1);
    SwapBeltItem.addItemId(builder, itemId);
    return SwapBeltItem.endSwapBeltItem(builder);
  }

  public static void startSwapBeltItem(FlatBufferBuilder builder) { builder.startTable(1); }
  public static void addItemId(FlatBufferBuilder builder, int itemId) { builder.addInt(0, itemId, 0); }
  public static int endSwapBeltItem(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public SwapBeltItem get(int j) { return get(new SwapBeltItem(), j); }
    public SwapBeltItem get(SwapBeltItem obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

