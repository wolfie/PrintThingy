package com.github.wolfie.printthingy;

public interface ColumnRendererUnescaped extends ColumnRenderer, Unescaped {
  public static class Default implements ColumnRendererUnescaped {
    public String getStringForCell(final Object value) {
      if (value != null) {
        return value.toString();
      }
      return "";
    }
  }
}
