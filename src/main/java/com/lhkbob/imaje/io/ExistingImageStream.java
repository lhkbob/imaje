package com.lhkbob.imaje.io;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Pixel;
import com.lhkbob.imaje.color.Color;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 */
public class ExistingImageStream<T extends Color> implements ImageStream<T> {
  private final Image<T> image;

  public ExistingImageStream(Image<T> image) {
    this.image = image;
  }

  @Override
  public int getWidth() {
    return image.getWidth();
  }

  @Override
  public int getHeight() {
    return image.getHeight();
  }

  @Override
  public int getMipmapCount() {
    return image.getMipmapCount();
  }

  @Override
  public int getLayerCount() {
    return image.getLayerCount();
  }

  @Override
  public boolean isMipmapped() {
    return image.isMipmapped();
  }

  @Override
  public boolean isLayered() {
    return image.isLayered();
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    return image.iterator();
  }

  @Override
  public void forEach(Consumer<? super Pixel<T>> action) {
    image.forEach(action);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    return image.spliterator();
  }
}
