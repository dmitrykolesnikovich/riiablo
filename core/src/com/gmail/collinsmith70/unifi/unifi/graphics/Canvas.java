package com.gmail.collinsmith70.unifi.unifi.graphics;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.gmail.collinsmith70.unifi.unifi.math.Boundary;

public class Canvas implements Disposable {

  @NonNull
  private Pixmap pixmap;

  @NonNull
  private Boundary clip;

  public Canvas(@IntRange(from = 0, to = Integer.MAX_VALUE) final int width,
                @IntRange(from = 0, to = Integer.MAX_VALUE) final int height) {
    this(new Pixmap(width, height, Pixmap.Format.RGBA8888));
  }

  public Canvas(@NonNull final Pixmap pixmap) {
    if (pixmap == null) {
      throw new IllegalArgumentException("pixmap cannot be null");
    }

    this.pixmap = pixmap;
    this.clip = new Boundary(0, 0, getWidth(), getHeight());
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getWidth() {
    return pixmap.getWidth();
  }

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  public int getHeight() {
    return pixmap.getHeight();
  }

  @NonNull
  public Boundary getClipBounds() {
    return new Boundary(clip);
  }

  @NonNull
  public Boundary getClipBounds(@Nullable final Boundary dst) {
    if (dst == null) {
      return getClipBounds();
    }

    dst.set(clip);
    return dst;
  }

  public void clipRectangle(final int left, final int top, final int right, final int bottom) {
    clip.set(left, top, right, bottom);
  }

  public void clipRectangle(@NonNull final Boundary src) {
    clip.set(src);
  }

  private void prepare(@NonNull final Paint paint) {
    Gdx.gl.glLineWidth(paint.getStrokeWidth());
    pixmap.setColor(paint.getColor());
    Pixmap.setBlending(paint.getBlendingMode());
    Pixmap.setFilter(paint.getFilterMode());
  }

  public void drawLine(final int x,
                       final int y,
                       @IntRange(from = 0, to = Integer.MAX_VALUE) final int length,
                       final boolean vertical,
                       @NonNull final Paint paint) {
    if (vertical) {
      drawVerticalLine(x, y, length, paint);
    } else {
      drawHorizontalLine(x, y, length, paint);
    }
  }

  public void drawVerticalLine(final int x,
                               final int y,
                               @IntRange(from = 0, to = Integer.MAX_VALUE) final int length,
                               @NonNull final Paint paint) {
    prepare(paint);
    pixmap.fillRectangle(x, y, paint.getStrokeWidth(), length);
  }

  public void drawHorizontalLine(final int x, final int y, final int length,
                                 @NonNull final Paint paint) {
    prepare(paint);
    pixmap.fillRectangle(x, y, length, paint.getStrokeWidth());
  }

  public void drawRectangle(final int x,
                            final int y,
                            @IntRange(from = 0, to = Integer.MAX_VALUE) final int width,
                            @IntRange(from = 0, to = Integer.MAX_VALUE) final int height,
                            @NonNull final Paint paint) {
    prepare(paint);
    if (paint.getStyle() == Paint.Style.FILL) {
      pixmap.fillRectangle(x, y, width, height);
      return;
    }

    pixmap.fillRectangle(x, y, paint.getStrokeWidth(), height);
    pixmap.fillRectangle(x + width - paint.getStrokeWidth(), y, paint.getStrokeWidth(), height);
    pixmap.fillRectangle(x, y + height - paint.getStrokeWidth(), width, paint.getStrokeWidth());
    pixmap.fillRectangle(x, y, width, paint.getStrokeWidth());
  }

  public void drawCircle(final int x,
                         final int y,
                         @IntRange(from = 0, to = Integer.MAX_VALUE) final int radius,
                         @NonNull final Paint paint) {
    prepare(paint);
    if (paint.getStyle() == Paint.Style.FILL) {
      pixmap.fillCircle(x, y, radius);
      return;
    }

    for (int i = 0; i < paint.getStrokeWidth(); i++) {
      pixmap.drawCircle(x, y, radius - i);
    }
  }

  public void drawCanvas(final int x,
                         final int y,
                         @NonNull final Canvas canvas) {
    drawCanvas(x, y, canvas, Paint.DEFAULT_PAINT);
  }

  public void drawCanvas(final int x,
                         final int y,
                         @NonNull final Canvas canvas,
                         @NonNull final Paint paint) {
    prepare(paint);
    pixmap.drawPixmap(canvas.getPixmap(), x, y);
  }

  public void drawPixmap(final int x,
                         final int y,
                         @NonNull final Pixmap pixmap) {
    drawPixmap(x, y, pixmap, Paint.DEFAULT_PAINT);
  }

  public void drawPixmap(final int x,
                         final int y,
                         @NonNull final Pixmap pixmap,
                         @NonNull final Paint paint) {
    prepare(paint);
    pixmap.drawPixmap(pixmap, x, y);
  }

  public void fill(@NonNull final Paint paint) {
    prepare(paint);
    pixmap.fill();
  }

  public Pixmap getPixmap() {
    return pixmap;
  }

  public Texture toTexture() {
    if (pixmap == null) {
      throw new IllegalStateException("Canvas instance has been disposed!");
    }

    return new Texture(pixmap);
  }

  @Override
  public void dispose() {
    this.pixmap.dispose();
    this.pixmap = null;
  }

}
