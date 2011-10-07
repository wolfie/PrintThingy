package com.github.wolfie.printthingy;

import java.util.HashMap;
import java.util.Map;

import com.sun.tools.javac.util.List;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class PrintThing {

  private final Window mainWindow;

  private Table table;

  private final Map<Object, ColumnRendererEscaped> columnRenderers = new HashMap<Object, ColumnRendererEscaped>();
  private final Map<Class<?>, TypeRenderer<?>> typeRenderers = new HashMap<Class<?>, TypeRenderer<?>>();

  private ColumnRendererEscaped defaultRenderer = new ColumnRendererEscaped.Default();

  public PrintThing(final Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  /**
   * Set the {@link ColumnRendererEscaped} that will be used for rendering
   * everything that hasn't got a different <code>ColumnRenderer</code>
   * specified
   * 
   * @see #setRenderer(Object, ColumnRendererEscaped)
   */
  public PrintThing setDefaultRenderer(
      final ColumnRendererEscaped defaultColumnRenderer) {
    if (defaultColumnRenderer == null) {
      throw new IllegalArgumentException(
          "defaultColumnRenderer may not be null");
    }
    defaultRenderer = defaultColumnRenderer;
    return this;
  }

  /**
   * Define which {@link Container} should be printed out.
   */
  public PrintThing hereYouGo(final Table table) {
    this.table = table;
    return this;
  }

  /**
   * Initiates the print sequence.
   * 
   * @throws IllegalStateException
   *           if container hasn't been set (see {@link #hereYouGo(Container)}).
   */
  public void print() throws IllegalStateException {
    checkForNullTable();
    checkForColumnRenderersWithoutMatchingProperties();

    final String cssFileURI = getCssFileURI();
    System.out.println(cssFileURI);
    mainWindow.addWindow(new PrintWindow(mainWindow, table, columnRenderers,
        typeRenderers, defaultRenderer, cssFileURI));
  }

  @SuppressWarnings("deprecation")
  private String getCssFileURI() {
    final ClassResource stylesCss2 = new ClassResource("/style.css",
        mainWindow.getApplication());
    stylesCss2.setCacheTime(0);
    final String relativeLocation = mainWindow.getApplication()
        .getRelativeLocation(stylesCss2);

    final ApplicationContext context = mainWindow.getApplication().getContext();
    final String contextPath = ((WebApplicationContext) context)
        .getHttpSession().getServletContext().getContextPath();

    final String shortenedRelativeLocation = fixVaadinBug(relativeLocation
        .substring("app://".length()));

    return contextPath + "/" + shortenedRelativeLocation;
  }

  private String fixVaadinBug(final String substring) {
    final int major = AbstractApplicationServlet.VERSION_MAJOR;
    final int minor = AbstractApplicationServlet.VERSION_MINOR;
    // final int revision = AbstractApplicationServlet.VERSION_REVISION;

    if (major == 6 && minor == 7) {
      /*
       * vaadin 6.7.0 (and onwards?) has problems with excessive escaping,
       * leading to troubles.
       */
      return substring.replace("%2F", "/");
    } else {
      return substring;
    }
  }

  /**
   * @throws IllegalStateException
   *           if {@link #table} is not set.
   */
  private void checkForNullTable() throws IllegalStateException {
    if (table == null) {
      throw new IllegalStateException("Table was not set.");
    }
  }

  /**
   * Prints warnings if there are {@link ColumnRendererEscaped ColumnRenderers}
   * for columns that aren't visible or present in {@link #table}.
   * <p>
   * If the container is a {@link Table}, check for visible columns, and use
   * them instead. This will include
   */
  private void checkForColumnRenderersWithoutMatchingProperties() {
    final List<Object> visibleColumns = List.from(table.getVisibleColumns());
    for (final Object propertyId : columnRenderers.keySet()) {
      if (!visibleColumns.contains(propertyId)) {
        System.out.println("Warning: a ColumnRenderer is defined for "
            + propertyId
            + ", but that property isn't shown with the given table.");
      }
    }
  }

  /**
   * <p>
   * Set a {@link ColumnRendererEscaped} for a certain {@link Property}.
   * </p>
   * 
   * <p>
   * Each <code>Property</code> can have only one <code>ColumnRenderer</code>.
   * Subsequent uses override previous calls.
   * </p>
   * 
   * @param propertyId
   *          the propertyId for the {@link Property} in the {@link Container}
   *          that is to be printed out.
   * @param renderer
   *          the <code>ColumnRenderer</code> to render <code>propertyId</code>.
   *          If <code>null</code>, the renderer for the given
   *          <code>propertyId</code> is removed.
   */
  public PrintThing setRenderer(final Object propertyId,
      final ColumnRendererEscaped renderer) {
    if (renderer != null) {
      columnRenderers.put(propertyId, renderer);
    } else {
      removeRenderer(propertyId);
    }
    return this;
  }

  /** Check whether a {@link ColumnRendererEscaped} is set for a property id. */
  public boolean hasRenderer(final Object propertyId) {
    return columnRenderers.containsKey(propertyId);
  }

  /**
   * Remove the {@link ColumnRendererEscaped} for a certain {@link Property}.
   */
  public PrintThing removeRenderer(final Object propertyId) {
    columnRenderers.remove(propertyId);
    return this;
  }

  /**
   * Set a {@link TypeRenderer} for a certain type.
   * <p>
   * {@link ColumnRendererEscaped ColumnRenderers} override any
   * <code>TypeRenderers</code> that may be set.
   * 
   * @param type
   *          The class of the type you want to define rendering for
   * @param renderer
   *          The {@link TypeRenderer} for <code>type</code>. If
   *          <code>null</code>, the renderer is removed for the
   *          <code>type</code>.
   */
  public <T extends Object> PrintThing setRenderer(final Class<T> type,
      final TypeRenderer<T> renderer) {
    if (renderer != null) {
      typeRenderers.put(type, renderer);
    } else {
      removeRenderer(type);
    }
    return this;
  }

  public PrintThing removeRenderer(final Class<?> type) {
    typeRenderers.remove(type);
    return this;
  }

  public boolean hasRenderer(final Class<?> type) {
    return typeRenderers.containsKey(type);
  }
}
