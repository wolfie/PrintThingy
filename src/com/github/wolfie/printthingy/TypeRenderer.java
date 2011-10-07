package com.github.wolfie.printthingy;

interface TypeRenderer<T> extends Renderer {
  String getStringForCell(T value);
}
