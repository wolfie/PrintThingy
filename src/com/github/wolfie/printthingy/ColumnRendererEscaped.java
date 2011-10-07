package com.github.wolfie.printthingy;

public interface ColumnRendererEscaped extends ColumnRenderer, Escaped {
  public static class Default implements ColumnRendererEscaped {
    public String getStringForCell(final Object value) {
      if (value != null) {
        return value.toString();
      }
      return "";
    }
  }
}
