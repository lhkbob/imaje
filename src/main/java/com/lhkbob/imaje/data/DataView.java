package com.lhkbob.imaje.data;

/**
 *
 */
public interface DataView<T extends DataSource<?>> {
  T getSource();
}
