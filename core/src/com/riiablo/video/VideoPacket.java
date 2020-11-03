package com.riiablo.video;

import com.riiablo.io.ByteInput;

public class VideoPacket implements Runnable {
  final ByteInput in;

  VideoPacket(ByteInput in) {
    this.in = in;
  }

  @Override
  public void run() {

  }

  private static final int[][] bink_tree_bits = {
      {
          0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
          0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
      },
      {
          0x00, 0x01, 0x03, 0x05, 0x07, 0x09, 0x0B, 0x0D,
          0x0F, 0x13, 0x15, 0x17, 0x19, 0x1B, 0x1D, 0x1F,
      },
      {
          0x00, 0x02, 0x01, 0x09, 0x05, 0x15, 0x0D, 0x1D,
          0x03, 0x13, 0x0B, 0x1B, 0x07, 0x17, 0x0F, 0x1F,
      },
      {
          0x00, 0x02, 0x06, 0x01, 0x09, 0x05, 0x0D, 0x1D,
          0x03, 0x13, 0x0B, 0x1B, 0x07, 0x17, 0x0F, 0x1F,
      },
      {
          0x00, 0x04, 0x02, 0x06, 0x01, 0x09, 0x05, 0x0D,
          0x03, 0x13, 0x0B, 0x1B, 0x07, 0x17, 0x0F, 0x1F,
      },
      {
          0x00, 0x04, 0x02, 0x0A, 0x06, 0x0E, 0x01, 0x09,
          0x05, 0x0D, 0x03, 0x0B, 0x07, 0x17, 0x0F, 0x1F,
      },
      {
          0x00, 0x02, 0x0A, 0x06, 0x0E, 0x01, 0x09, 0x05,
          0x0D, 0x03, 0x0B, 0x1B, 0x07, 0x17, 0x0F, 0x1F,
      },
      {
          0x00, 0x01, 0x05, 0x03, 0x13, 0x0B, 0x1B, 0x3B,
          0x07, 0x27, 0x17, 0x37, 0x0F, 0x2F, 0x1F, 0x3F,
      },
      {
          0x00, 0x01, 0x03, 0x13, 0x0B, 0x2B, 0x1B, 0x3B,
          0x07, 0x27, 0x17, 0x37, 0x0F, 0x2F, 0x1F, 0x3F,
      },
      {
          0x00, 0x01, 0x05, 0x0D, 0x03, 0x13, 0x0B, 0x1B,
          0x07, 0x27, 0x17, 0x37, 0x0F, 0x2F, 0x1F, 0x3F,
      },
      {
          0x00, 0x02, 0x01, 0x05, 0x0D, 0x03, 0x13, 0x0B,
          0x1B, 0x07, 0x17, 0x37, 0x0F, 0x2F, 0x1F, 0x3F,
      },
      {
          0x00, 0x01, 0x09, 0x05, 0x0D, 0x03, 0x13, 0x0B,
          0x1B, 0x07, 0x17, 0x37, 0x0F, 0x2F, 0x1F, 0x3F,
      },
      {
          0x00, 0x02, 0x01, 0x03, 0x13, 0x0B, 0x1B, 0x3B,
          0x07, 0x27, 0x17, 0x37, 0x0F, 0x2F, 0x1F, 0x3F,
      },
      {
          0x00, 0x01, 0x05, 0x03, 0x07, 0x27, 0x17, 0x37,
          0x0F, 0x4F, 0x2F, 0x6F, 0x1F, 0x5F, 0x3F, 0x7F,
      },
      {
          0x00, 0x01, 0x05, 0x03, 0x07, 0x17, 0x37, 0x77,
          0x0F, 0x4F, 0x2F, 0x6F, 0x1F, 0x5F, 0x3F, 0x7F,
      },
      {
          0x00, 0x02, 0x01, 0x05, 0x03, 0x07, 0x27, 0x17,
          0x37, 0x0F, 0x2F, 0x6F, 0x1F, 0x5F, 0x3F, 0x7F,
      },
  };

  private static final int[][] bink_tree_lens = {
      { 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4 },
      { 1, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
      { 2, 2, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
      { 2, 3, 3, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
      { 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5 },
      { 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5 },
      { 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5 },
      { 1, 3, 3, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 },
      { 1, 2, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 },
      { 1, 3, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6 },
      { 2, 2, 3, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6 },
      { 1, 4, 4, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6 },
      { 2, 2, 2, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 },
      { 1, 3, 3, 3, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7 },
      { 1, 3, 3, 3, 5, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 },
      { 2, 2, 3, 3, 3, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7 },
  };

  private static final int[][] bink_patterns = {
      {
          0x00, 0x08, 0x10, 0x18, 0x20, 0x28, 0x30, 0x38,
          0x39, 0x31, 0x29, 0x21, 0x19, 0x11, 0x09, 0x01,
          0x02, 0x0A, 0x12, 0x1A, 0x22, 0x2A, 0x32, 0x3A,
          0x3B, 0x33, 0x2B, 0x23, 0x1B, 0x13, 0x0B, 0x03,
          0x04, 0x0C, 0x14, 0x1C, 0x24, 0x2C, 0x34, 0x3C,
          0x3D, 0x35, 0x2D, 0x25, 0x1D, 0x15, 0x0D, 0x05,
          0x06, 0x0E, 0x16, 0x1E, 0x26, 0x2E, 0x36, 0x3E,
          0x3F, 0x37, 0x2F, 0x27, 0x1F, 0x17, 0x0F, 0x07,
      },
      {
          0x3B, 0x3A, 0x39, 0x38, 0x30, 0x31, 0x32, 0x33,
          0x2B, 0x2A, 0x29, 0x28, 0x20, 0x21, 0x22, 0x23,
          0x1B, 0x1A, 0x19, 0x18, 0x10, 0x11, 0x12, 0x13,
          0x0B, 0x0A, 0x09, 0x08, 0x00, 0x01, 0x02, 0x03,
          0x04, 0x05, 0x06, 0x07, 0x0F, 0x0E, 0x0D, 0x0C,
          0x14, 0x15, 0x16, 0x17, 0x1F, 0x1E, 0x1D, 0x1C,
          0x24, 0x25, 0x26, 0x27, 0x2F, 0x2E, 0x2D, 0x2C,
          0x34, 0x35, 0x36, 0x37, 0x3F, 0x3E, 0x3D, 0x3C,
      },
      {
          0x19, 0x11, 0x12, 0x1A, 0x1B, 0x13, 0x0B, 0x03,
          0x02, 0x0A, 0x09, 0x01, 0x00, 0x08, 0x10, 0x18,
          0x20, 0x28, 0x30, 0x38, 0x39, 0x31, 0x29, 0x2A,
          0x32, 0x3A, 0x3B, 0x33, 0x2B, 0x23, 0x22, 0x21,
          0x1D, 0x15, 0x16, 0x1E, 0x1F, 0x17, 0x0F, 0x07,
          0x06, 0x0E, 0x0D, 0x05, 0x04, 0x0C, 0x14, 0x1C,
          0x24, 0x2C, 0x34, 0x3C, 0x3D, 0x35, 0x2D, 0x2E,
          0x36, 0x3E, 0x3F, 0x37, 0x2F, 0x27, 0x26, 0x25,
      },
      {
          0x03, 0x0B, 0x02, 0x0A, 0x01, 0x09, 0x00, 0x08,
          0x10, 0x18, 0x11, 0x19, 0x12, 0x1A, 0x13, 0x1B,
          0x23, 0x2B, 0x22, 0x2A, 0x21, 0x29, 0x20, 0x28,
          0x30, 0x38, 0x31, 0x39, 0x32, 0x3A, 0x33, 0x3B,
          0x3C, 0x34, 0x3D, 0x35, 0x3E, 0x36, 0x3F, 0x37,
          0x2F, 0x27, 0x2E, 0x26, 0x2D, 0x25, 0x2C, 0x24,
          0x1C, 0x14, 0x1D, 0x15, 0x1E, 0x16, 0x1F, 0x17,
          0x0F, 0x07, 0x0E, 0x06, 0x0D, 0x05, 0x0C, 0x04,
      },
      {
          0x18, 0x19, 0x10, 0x11, 0x08, 0x09, 0x00, 0x01,
          0x02, 0x03, 0x0A, 0x0B, 0x12, 0x13, 0x1A, 0x1B,
          0x1C, 0x1D, 0x14, 0x15, 0x0C, 0x0D, 0x04, 0x05,
          0x06, 0x07, 0x0E, 0x0F, 0x16, 0x17, 0x1E, 0x1F,
          0x27, 0x26, 0x2F, 0x2E, 0x37, 0x36, 0x3F, 0x3E,
          0x3D, 0x3C, 0x35, 0x34, 0x2D, 0x2C, 0x25, 0x24,
          0x23, 0x22, 0x2B, 0x2A, 0x33, 0x32, 0x3B, 0x3A,
          0x39, 0x38, 0x31, 0x30, 0x29, 0x28, 0x21, 0x20,
      },
      {
          0x00, 0x01, 0x02, 0x03, 0x08, 0x09, 0x0A, 0x0B,
          0x10, 0x11, 0x12, 0x13, 0x18, 0x19, 0x1A, 0x1B,
          0x20, 0x21, 0x22, 0x23, 0x28, 0x29, 0x2A, 0x2B,
          0x30, 0x31, 0x32, 0x33, 0x38, 0x39, 0x3A, 0x3B,
          0x04, 0x05, 0x06, 0x07, 0x0C, 0x0D, 0x0E, 0x0F,
          0x14, 0x15, 0x16, 0x17, 0x1C, 0x1D, 0x1E, 0x1F,
          0x24, 0x25, 0x26, 0x27, 0x2C, 0x2D, 0x2E, 0x2F,
          0x34, 0x35, 0x36, 0x37, 0x3C, 0x3D, 0x3E, 0x3F,
      },
      {
          0x06, 0x07, 0x0F, 0x0E, 0x0D, 0x05, 0x0C, 0x04,
          0x03, 0x0B, 0x02, 0x0A, 0x09, 0x01, 0x00, 0x08,
          0x10, 0x18, 0x11, 0x19, 0x12, 0x1A, 0x13, 0x1B,
          0x14, 0x1C, 0x15, 0x1D, 0x16, 0x1E, 0x17, 0x1F,
          0x27, 0x2F, 0x26, 0x2E, 0x25, 0x2D, 0x24, 0x2C,
          0x23, 0x2B, 0x22, 0x2A, 0x21, 0x29, 0x20, 0x28,
          0x31, 0x30, 0x38, 0x39, 0x3A, 0x32, 0x3B, 0x33,
          0x3C, 0x34, 0x3D, 0x35, 0x36, 0x37, 0x3F, 0x3E,
      },
      {
          0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
          0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08,
          0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
          0x1F, 0x1E, 0x1D, 0x1C, 0x1B, 0x1A, 0x19, 0x18,
          0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27,
          0x2F, 0x2E, 0x2D, 0x2C, 0x2B, 0x2A, 0x29, 0x28,
          0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
          0x3F, 0x3E, 0x3D, 0x3C, 0x3B, 0x3A, 0x39, 0x38,
      },
      {
          0x00, 0x08, 0x09, 0x01, 0x02, 0x03, 0x0B, 0x0A,
          0x12, 0x13, 0x1B, 0x1A, 0x19, 0x11, 0x10, 0x18,
          0x20, 0x28, 0x29, 0x21, 0x22, 0x23, 0x2B, 0x2A,
          0x32, 0x31, 0x30, 0x38, 0x39, 0x3A, 0x3B, 0x33,
          0x34, 0x3C, 0x3D, 0x3E, 0x3F, 0x37, 0x36, 0x35,
          0x2D, 0x2C, 0x24, 0x25, 0x26, 0x2E, 0x2F, 0x27,
          0x1F, 0x17, 0x16, 0x1E, 0x1D, 0x1C, 0x14, 0x15,
          0x0D, 0x0C, 0x04, 0x05, 0x06, 0x0E, 0x0F, 0x07,
      },
      {
          0x18, 0x19, 0x10, 0x11, 0x08, 0x09, 0x00, 0x01,
          0x02, 0x03, 0x0A, 0x0B, 0x12, 0x13, 0x1A, 0x1B,
          0x1C, 0x1D, 0x14, 0x15, 0x0C, 0x0D, 0x04, 0x05,
          0x06, 0x07, 0x0E, 0x0F, 0x16, 0x17, 0x1E, 0x1F,
          0x26, 0x27, 0x2E, 0x2F, 0x36, 0x37, 0x3E, 0x3F,
          0x3C, 0x3D, 0x34, 0x35, 0x2C, 0x2D, 0x24, 0x25,
          0x22, 0x23, 0x2A, 0x2B, 0x32, 0x33, 0x3A, 0x3B,
          0x38, 0x39, 0x30, 0x31, 0x28, 0x29, 0x20, 0x21,
      },
      {
          0x00, 0x08, 0x01, 0x09, 0x02, 0x0A, 0x03, 0x0B,
          0x13, 0x1B, 0x12, 0x1A, 0x11, 0x19, 0x10, 0x18,
          0x20, 0x28, 0x21, 0x29, 0x22, 0x2A, 0x23, 0x2B,
          0x33, 0x3B, 0x32, 0x3A, 0x31, 0x39, 0x30, 0x38,
          0x3C, 0x34, 0x3D, 0x35, 0x3E, 0x36, 0x3F, 0x37,
          0x2F, 0x27, 0x2E, 0x26, 0x2D, 0x25, 0x2C, 0x24,
          0x1F, 0x17, 0x1E, 0x16, 0x1D, 0x15, 0x1C, 0x14,
          0x0C, 0x04, 0x0D, 0x05, 0x0E, 0x06, 0x0F, 0x07,
      },
      {
          0x00, 0x08, 0x10, 0x18, 0x19, 0x1A, 0x1B, 0x13,
          0x0B, 0x03, 0x02, 0x01, 0x09, 0x11, 0x12, 0x0A,
          0x04, 0x0C, 0x14, 0x1C, 0x1D, 0x1E, 0x1F, 0x17,
          0x0F, 0x07, 0x06, 0x05, 0x0D, 0x15, 0x16, 0x0E,
          0x24, 0x2C, 0x34, 0x3C, 0x3D, 0x3E, 0x3F, 0x37,
          0x2F, 0x27, 0x26, 0x25, 0x2D, 0x35, 0x36, 0x2E,
          0x20, 0x28, 0x30, 0x38, 0x39, 0x3A, 0x3B, 0x33,
          0x2B, 0x23, 0x22, 0x21, 0x29, 0x31, 0x32, 0x2A,
      },
      {
          0x00, 0x08, 0x09, 0x01, 0x02, 0x03, 0x0B, 0x0A,
          0x13, 0x1B, 0x1A, 0x12, 0x11, 0x10, 0x18, 0x19,
          0x21, 0x20, 0x28, 0x29, 0x2A, 0x22, 0x23, 0x2B,
          0x33, 0x3B, 0x3A, 0x32, 0x31, 0x39, 0x38, 0x30,
          0x34, 0x3C, 0x3D, 0x35, 0x36, 0x3E, 0x3F, 0x37,
          0x2F, 0x27, 0x26, 0x2E, 0x2D, 0x2C, 0x24, 0x25,
          0x1D, 0x1C, 0x14, 0x15, 0x16, 0x1E, 0x1F, 0x17,
          0x0E, 0x0F, 0x07, 0x06, 0x05, 0x0D, 0x0C, 0x04,
      },
      {
          0x18, 0x10, 0x08, 0x00, 0x01, 0x02, 0x03, 0x0B,
          0x13, 0x1B, 0x1A, 0x19, 0x11, 0x0A, 0x09, 0x12,
          0x1C, 0x14, 0x0C, 0x04, 0x05, 0x06, 0x07, 0x0F,
          0x17, 0x1F, 0x1E, 0x1D, 0x15, 0x0E, 0x0D, 0x16,
          0x3C, 0x34, 0x2C, 0x24, 0x25, 0x26, 0x27, 0x2F,
          0x37, 0x3F, 0x3E, 0x3D, 0x35, 0x2E, 0x2D, 0x36,
          0x38, 0x30, 0x28, 0x20, 0x21, 0x22, 0x23, 0x2B,
          0x33, 0x3B, 0x3A, 0x39, 0x31, 0x2A, 0x29, 0x32,
      },
      {
          0x00, 0x08, 0x09, 0x01, 0x02, 0x0A, 0x12, 0x11,
          0x10, 0x18, 0x19, 0x1A, 0x1B, 0x13, 0x0B, 0x03,
          0x07, 0x06, 0x0E, 0x0F, 0x17, 0x16, 0x15, 0x0D,
          0x05, 0x04, 0x0C, 0x14, 0x1C, 0x1D, 0x1E, 0x1F,
          0x3F, 0x3E, 0x36, 0x37, 0x2F, 0x2E, 0x2D, 0x35,
          0x3D, 0x3C, 0x34, 0x2C, 0x24, 0x25, 0x26, 0x27,
          0x38, 0x30, 0x31, 0x39, 0x3A, 0x32, 0x2A, 0x29,
          0x28, 0x20, 0x21, 0x22, 0x23, 0x2B, 0x33, 0x3B,
      },
      {
          0x00, 0x01, 0x08, 0x09, 0x10, 0x11, 0x18, 0x19,
          0x20, 0x21, 0x28, 0x29, 0x30, 0x31, 0x38, 0x39,
          0x3A, 0x3B, 0x32, 0x33, 0x2A, 0x2B, 0x22, 0x23,
          0x1A, 0x1B, 0x12, 0x13, 0x0A, 0x0B, 0x02, 0x03,
          0x04, 0x05, 0x0C, 0x0D, 0x14, 0x15, 0x1C, 0x1D,
          0x24, 0x25, 0x2C, 0x2D, 0x34, 0x35, 0x3C, 0x3D,
          0x3E, 0x3F, 0x36, 0x37, 0x2E, 0x2F, 0x26, 0x27,
          0x1E, 0x1F, 0x16, 0x17, 0x0E, 0x0F, 0x06, 0x07,
      }
  };
}