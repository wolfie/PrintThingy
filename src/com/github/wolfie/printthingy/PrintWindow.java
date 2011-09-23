package com.github.wolfie.printthingy;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Window;

class PrintWindow extends Window {
  private static final long serialVersionUID = 2361219300649081504L;

  private static final String IFRAME_NAME = "__iframe";
  private static final String INJECT_FUNCTION = "inject";

  private static final int BREAK_AFTER_AMOUNT = 35;

  private final Container container;

  /** The column renderer that will be rolled back to if there's no custom one */
  private final ColumnRenderer defaultRenderer;

  /** Special renderers for a particular column. */
  private final Map<Object, ColumnRenderer> renderers;

  private final String cssFileUri;

  public PrintWindow(final Window mainWindow, final Container container,
      final Map<Object, ColumnRenderer> renderers,
      final ColumnRenderer defaultRenderer, final String cssFileUri) {

    this.container = container;
    this.renderers = renderers;
    this.defaultRenderer = defaultRenderer;
    this.cssFileUri = cssFileUri;

    setModal(true);
    addComponent(new Label("<h1>Printing...</h1>", Label.CONTENT_XHTML));
    addComponent(new Label(getIFrameXHTML(), Label.CONTENT_XHTML));
    mainWindow.executeJavaScript(PrintWindow.getInjectCode(getBodyHTML()));

    // closeAfterAWhile();
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

    if (container != null) {
      final Collection<?> itemIds = container.getItemIds();
      if (!itemIds.isEmpty()) {
        sb.append("<table cellspacing=0 cellpadding=0>");
        for (final Object itemId : itemIds) {
          i++;

          String breakAfter = "";
          if (i % BREAK_AFTER_AMOUNT == 0) {
            breakAfter = "style='page-break-after:always'";
          }
          sb.append("<tr " + breakAfter + ">");
          final Item item = container.getItem(itemId);
          for (final Object propertyId : item.getItemPropertyIds()) {

            final ColumnRenderer renderer = getRendererFor(propertyId);

            final Property itemProperty = item.getItemProperty(propertyId);
            if (itemProperty != null) {
              final String xhtmlForCell = renderer.getXHTMLForCell(
                  itemProperty.getValue(), container);
              sb.append(String.format("<td>%s</td>",
                  StringEscapeUtils.escapeXml(xhtmlForCell)));
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

  /**
   * If there's a custom renderer for the given propertyId, return that.
   * Otherwise this will return {@link #defaultRenderer}
   */
  private ColumnRenderer getRendererFor(final Object propertyId) {
    final ColumnRenderer columnRenderer = renderers.get(propertyId);
    if (columnRenderer != null) {
      return columnRenderer;
    } else {
      return defaultRenderer;
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
