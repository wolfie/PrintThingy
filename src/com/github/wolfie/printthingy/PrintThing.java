package com.github.wolfie.printthingy;

import com.vaadin.ui.Window;

public class PrintThing {

  private final Window mainWindow;

  public PrintThing(final Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public void print() {
    mainWindow.addWindow(new PrintWindow(mainWindow));
  }
}
