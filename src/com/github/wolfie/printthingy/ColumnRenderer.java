package com.github.wolfie.printthingy;

interface ColumnRenderer extends Renderer {
  String getStringForCell(Object value);
}
