package com.github.wolfie.printthingy;

import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

class PrintWindow extends Window {
  private static final long serialVersionUID = 2361219300649081504L;

  private static final String IFRAME_NAME = "__iframe";
  private static final String INJECT_FUNCTION = "inject";

  public PrintWindow(final Window mainWindow) {
    setModal(true);
    addComponent(new Label(getIFrameXHTML(), Label.CONTENT_XHTML));
    mainWindow.executeJavaScript(getInjectCode(getBodyHTML()));
  }

  private String getIFrameXHTML() {
    final String iframeRoot = String.format(
        "<iframe name='%s' id='%s' onload='%s()'></iframe>", IFRAME_NAME,
        IFRAME_NAME, INJECT_FUNCTION);
    return iframeRoot;
  }

  private String getInjectCode(final String bodyHTML) {
    final String bodyAsJavascriptString = convertHtmlToJavascriptString(bodyHTML);
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
      INJECT_FUNCTION, IFRAME_NAME, bodyAsJavascriptString);
    // @formatter:on
  }

  private String convertHtmlToJavascriptString(final String bodyHTML) {
    // TODO
    return bodyHTML;
  }

  private String getBodyHTML() {
    // TODO
    return "<h1>Hello World</h1>";
  }
}
