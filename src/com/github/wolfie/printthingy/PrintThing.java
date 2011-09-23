package com.github.wolfie.printthingy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Window;

public class PrintThing {

  private final Window mainWindow;
  private Container container;
  private final Map<Object, ColumnRenderer> renderers = new HashMap<Object, ColumnRenderer>();
  private ColumnRenderer defaultRenderer = new DefaultColumnRenderer();

  public PrintThing(final Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  /**
   * Set the {@link ColumnRenderer} that will be used for rendering everything
   * that hasn't got a different <code>ColumnRenderer</code> specified
   * 
   * @see #setColumnRenderer(Object, ColumnRenderer)
   */
  public PrintThing setDefaultColumnRenderer(
      final ColumnRenderer defaultColumnRenderer) {
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
  public PrintThing hereYouGo(final Container container) {
    this.container = container;
    return this;
  }

  /**
   * Initiates the print sequence.
   * 
   * @throws IllegalStateException
   *           if container hasn't been set (see {@link #hereYouGo(Container)}).
   */
  public void print() throws IllegalStateException {
    checkForNullContainer();
    checkForColumnRenderersWithoutMatchingProperties();

    final String cssFileURI = getCssFileURI();
    System.out.println(cssFileURI);
    mainWindow.addWindow(new PrintWindow(mainWindow, container, renderers,
        defaultRenderer, cssFileURI));
  }

  @SuppressWarnings("deprecation")
  private String getCssFileURI() {
    final ClassResource resource = new ClassResource("/style.css",
        mainWindow.getApplication());
    resource.setCacheTime(0);
    final String relativeLocation = mainWindow.getApplication()
        .getRelativeLocation(resource);

    final ApplicationContext context = mainWindow.getApplication().getContext();
    final String contextPath = ((WebApplicationContext) context)
        .getHttpSession().getServletContext().getContextPath();

    return contextPath + "/" + relativeLocation.substring("app://".length());
  }

  /**
   * @throws IllegalStateException
   *           if {@link #container} is not set.
   */
  private void checkForNullContainer() throws IllegalStateException {
    if (container == null) {
      throw new IllegalStateException("Container was not set.");
    }
  }

  /**
   * Prints warnings if there are {@link ColumnRenderer ColumnRenderers} for
   * properties that aren't present in {@link #container}.
   */
  private void checkForColumnRenderersWithoutMatchingProperties() {
    final Collection<?> containerPropertyIds = container
        .getContainerPropertyIds();
    for (final Object propertyId : renderers.keySet()) {
      if (!containerPropertyIds.contains(propertyId)) {
        System.out.println("Warning: a ColumnRenderer is defined for "
            + propertyId
            + ", but that property doesn't exist in the given container.");
      }
    }
  }

  /**
   * <p>
   * Set a {@link ColumnRenderer} for a certain {@link Property}.
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
  public PrintThing setColumnRenderer(final Object propertyId,
      final ColumnRenderer renderer) {
    if (renderer != null) {
      renderers.put(propertyId, renderer);
    } else {
      removeColumnRenderer(propertyId);
    }
    return this;
  }

  /**
   * Remove the {@link ColumnRenderer} for a certain {@link Property}.
   */
  public PrintThing removeColumnRenderer(final Object propertyId) {
    renderers.remove(propertyId);
    return this;
  }
}
