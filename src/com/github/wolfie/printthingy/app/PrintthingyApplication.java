package com.github.wolfie.printthingy.app;

import java.util.Random;

import com.github.wolfie.printthingy.PrintThing;
import com.github.wolfie.printthingy.TypeRendererEscaped;
import com.github.wolfie.printthingy.TypeRendererUnescaped;
import com.vaadin.Application;
import com.vaadin.demo.sampler.ExampleUtil;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class PrintthingyApplication extends Application {
  private static final long serialVersionUID = 1946460763992860772L;

  private static class Modulator implements TypeRendererEscaped<String> {
    public String getStringForCell(final String value) {
      final StringBuilder sb = new StringBuilder();
      int i = 0;
      for (final char c : value.toLowerCase().toCharArray()) {
        if (i % 2 == 0) {
          sb.append(c);
        } else {
          sb.append(Character.toUpperCase(c));
        }
        i++;
      }
      return sb.toString();
    }
  }

  private static class ResourceRenderer implements
      TypeRendererUnescaped<Resource> {
    private final Random random = new Random();

    public String getStringForCell(final Resource value) {
      if (random.nextBoolean()) {
        return "<img src='http://www.famfamfam.com/lab/icons/silk/icons/brick.png'/>";
      } else {
        return value.toString();
      }
    }
  }

  @Override
  public void init() {
    final Window mainWindow = new Window("Printthingy Application");
    setMainWindow(mainWindow);

    final Table table = new Table(null, ExampleUtil.getISO3166Container());
    table.getItem(table.addItem())
        .getItemProperty(ExampleUtil.iso3166_PROPERTY_NAME)
        .setValue("<b>FOO</b>");
    mainWindow.addComponent(new Button("Print", new Button.ClickListener() {
      private static final long serialVersionUID = 7041343257783673309L;

      public void buttonClick(final ClickEvent event) {
        new PrintThing(mainWindow).hereYouGo(table)
            .setRenderer(String.class, new Modulator())
            .setRenderer(Resource.class, new ResourceRenderer()).print();
      }
    }));
    mainWindow.addComponent(table);
  }
}
