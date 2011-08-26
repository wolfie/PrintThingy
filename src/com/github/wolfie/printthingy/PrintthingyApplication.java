package com.github.wolfie.printthingy;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

public class PrintthingyApplication extends Application {
  private static final long serialVersionUID = 1946460763992860772L;

  @Override
  public void init() {
    final Window mainWindow = new Window("Printthingy Application");
    setMainWindow(mainWindow);
    mainWindow.addComponent(new Button("Print", new Button.ClickListener() {
      private static final long serialVersionUID = 7041343257783673309L;

      public void buttonClick(final ClickEvent event) {
        new PrintThing(mainWindow).print();
      }
    }));
  }
}
