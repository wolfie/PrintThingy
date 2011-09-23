package com.github.wolfie.printthingy.app;

import com.github.wolfie.printthingy.PrintThing;
import com.vaadin.Application;
import com.vaadin.demo.sampler.ExampleUtil;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class PrintthingyApplication extends Application {
  private static final long serialVersionUID = 1946460763992860772L;

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
        new PrintThing(mainWindow).hereYouGo(table).print();
      }
    }));
    mainWindow.addComponent(table);
  }
}
