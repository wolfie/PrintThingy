package com.github.wolfie.printthingy;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

class PrintWindow extends Window {
  private static final long serialVersionUID = 2361219300649081504L;

  private static final String IFRAME_NAME = "__iframe";
  private static final String INJECT_FUNCTION = "inject";

  private final Table table;

  /** The column renderer that will be rolled back to if there's no custom one */
  private final ColumnRendererEscaped defaultRenderer;

  /** Special renderers for a particular column. */
  private final Map<Object, ColumnRendererEscaped> columnRenderers;
  private final Map<Class<?>, TypeRenderer<?>> typeRenderers;

  private final String cssFileUri;

  public PrintWindow(final Window mainWindow, final Table table,
      final Map<Object, ColumnRendererEscaped> columnRenderers,
      final Map<Class<?>, TypeRenderer<?>> typeRenderers,
      final ColumnRendererEscaped defaultRenderer, final String cssFileUri) {

    this.table = table;
    this.columnRenderers = columnRenderers;
    this.typeRenderers = typeRenderers;
    this.defaultRenderer = defaultRenderer;
    this.cssFileUri = cssFileUri;

    setModal(true);
    addComponent(new Label("<h1>Printing...</h1>", Label.CONTENT_XHTML));
    addComponent(new Label(getIFrameXHTML(), Label.CONTENT_XHTML));
    mainWindow.executeJavaScript(PrintWindow.getInjectCode(getBodyHTML()));

    closeAfterAWhile();
  }

  private void closeAfterAWhile() {
    // no need for Refresher ^_^
    final ProgressIndicator pi = new ProgressIndicator();
    pi.setWidth("0px");
    addComponent(pi);

    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(500);
        } catch (final InterruptedException e) {
        }
        PrintWindow.this.close();
      };
    }.start();
  }

  private String getIFrameXHTML() {
    final String iframeRoot = String.format(
        "<iframe name='%s' id='%s' onload='%s()' height=0 width=0></iframe>",
        PrintWindow.IFRAME_NAME, PrintWindow.IFRAME_NAME,
        PrintWindow.INJECT_FUNCTION);
    return iframeRoot;
  }

  private static String getInjectCode(final String bodyHTML) {
    final String bodyAsJavascriptString = StringEscapeUtils
        .escapeEcmaScript(bodyHTML);
    // @formatter:off
    return String.format(
      "function %s() { "+
        "var iframe = document.getElementById('%s'); "+
        "var doc = iframe.document; "+
        "if (iframe.contentDocument) "+
          "doc = iframe.contentDocument; "+
        "else if (iframe.contentWindow) "+
          "doc = iframe.contentWindow.document; "+
        "doc.open(); "+
        "doc.writeln('%s'); "+
        "doc.close(); "+
      "} ",
      PrintWindow.INJECT_FUNCTION, PrintWindow.IFRAME_NAME, bodyAsJavascriptString);
    // @formatter:on
  }

  private String getBodyHTML() {

    final StringBuilder sb = new StringBuilder();

    sb.append(PrintWindow.getHTMLHeading(cssFileUri));
    sb.append("<h1>Hello World</h1>");

    sb.append(getTableHtml());

    sb.append(PrintWindow.getHTMLFooting());

    return sb.toString();
  }

  private String getTableHtml() {
    final StringBuilder sb = new StringBuilder();

    int i = 0;

    if (table != null) {
      final Collection<?> itemIds = table.getItemIds();
      if (!itemIds.isEmpty()) {
        sb.append("<table cellspacing=0 cellpadding=0>");

        putHeadings(sb);

        for (final Object itemId : itemIds) {
          i++;

          sb.append("<tr>");
          final Item item = table.getItem(itemId);
          for (final Object propertyId : item.getItemPropertyIds()) {

            final Property itemProperty = item.getItemProperty(propertyId);
            if (itemProperty != null) {
              sb.append(String
                  .format("<td>%s</td>", render(itemId, propertyId)));
            } else {
              sb.append("<td></td>");
            }
          }
          sb.append("</tr>");
        }
        sb.append("</table>");
      }
    }

    return sb.toString();
  }

  private void putHeadings(final StringBuilder sb) {
    sb.append("<tr>");
    for (final String columnHeader : table.getColumnHeaders()) {
      sb.append(String.format("<th>%s</th>", columnHeader));
    }
    sb.append("</tr>");
  }

  private String render(final Object itemId, final Object propertyId) {
    final Object value = table.getItem(itemId).getItemProperty(propertyId)
        .getValue();
    final ColumnRendererEscaped columnRenderer = getColumnRendererFor(propertyId);
    if (columnRenderer != null) {
      return columnRenderer.getStringForCell(value);
    } else {
      final TypeRenderer<Object> typeRenderer = getTypeRendererFor(propertyId);
      if (typeRenderer != null && value != null) {
        return escapeIfNeeded(typeRenderer,
            typeRenderer.getStringForCell(value));
      }
    }

    return defaultRenderer.getStringForCell(value);
  }

  private String escapeIfNeeded(final Renderer renderer, final String string) {
    if (renderer instanceof Escaped) {
      return StringEscapeUtils.escapeXml(string);
    } else {
      return string;
    }
  }

  /**
   * If there's a custom renderer for the given propertyId, return that.
   * Otherwise, return <code>null</code>
   */
  private ColumnRendererEscaped getColumnRendererFor(final Object propertyId) {
    final ColumnRendererEscaped columnRenderer = columnRenderers
        .get(propertyId);
    if (columnRenderer != null) {
      return columnRenderer;
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private TypeRenderer<Object> getTypeRendererFor(final Object propertyId) {
    final Property property = table.getContainerProperty(table.firstItemId(),
        propertyId);
    final TypeRenderer<?> typeRenderer = typeRenderers.get(property.getType());
    if (typeRenderer != null) {
      return (TypeRenderer<Object>) typeRenderer;
    } else {
      return null;
    }
  }

  private static String getHTMLFooting() {
    return "<script type='text/javascript'>window.print();</script></body></html>";
  }

  private static String getHTMLHeading(final String cssFileUri) {
    return "<!DOCTYPE html><html><head><link href=\""
        + cssFileUri
        + "\" type=\"text/css\" rel=\"stylesheet\" media=\"print,screen\"/></head><body>";
  }
}
