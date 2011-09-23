package com.github.wolfie.printthingy;

import com.vaadin.data.Container;

public class DefaultColumnRenderer implements ColumnRenderer {
  public String getXHTMLForCell(final Object value, final Container container) {
    if (value != null) {
      return value.toString();
    }
    return "";
  }
}
