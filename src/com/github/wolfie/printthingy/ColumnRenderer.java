package com.github.wolfie.printthingy;

import com.vaadin.data.Container;

public interface ColumnRenderer {
  String getXHTMLForCell(Object itemId, Container container);
}
