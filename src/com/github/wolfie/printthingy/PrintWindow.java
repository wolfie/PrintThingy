package com.github.wolfie.printthingy;

import java.util.Collection;

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
	
	private final Container container;
	
	public PrintWindow(final Window mainWindow, final Container container) {
		this.container = container;
		setModal(true);
		addComponent(new Label(getIFrameXHTML(), Label.CONTENT_XHTML));
		mainWindow.executeJavaScript(PrintWindow.getInjectCode(getBodyHTML()));
		
		closeAfterAWhile();
	}
	
	private void closeAfterAWhile() {
		// no need for Refresher ^_^
		addComponent(new ProgressIndicator());
		
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
				"<iframe name='%s' id='%s' onload='%s()'></iframe>",
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
		
		sb.append(PrintWindow.getHTMLHeading());
		
		sb.append("<h1>Hello World</h1>");
		
		if (container != null) {
			
			final Collection<?> itemIds = container.getItemIds();
			if (!itemIds.isEmpty()) {
				sb.append("<table>");
				for (final Object itemId : itemIds) {
					sb.append("<tr>");
					final Item item = container.getItem(itemId);
					for (final Object propertyId : item.getItemPropertyIds()) {
						final Property property = item.getItemProperty(propertyId);
						sb.append(String.format("<td>%s</td>", property.getValue()
								.toString()));
					}
					sb.append("</tr>");
				}
				sb.append("</table>");
			}
		}
		
		sb.append(PrintWindow.getHTMLFooting());
		
		return sb.toString();
	}
	
	private static String getHTMLFooting() {
		return "<script type='text/javascript'>window.print()</script></body></html>";
	}
	
	private static String getHTMLHeading() {
		return "<!DOCTYPE html><html><body>";
	}
}
