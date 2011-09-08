package com.github.wolfie.printthingy;

import com.vaadin.data.Container;
import com.vaadin.ui.Window;

public class PrintThing {
	
	private final Window mainWindow;
	private Container container;
	
	public PrintThing(final Window mainWindow) {
		this.mainWindow = mainWindow;
	}
	
	public PrintThing hereYouGo(final Container container) {
		this.container = container;
		return this;
	}
	
	public void print() {
		mainWindow.addWindow(new PrintWindow(mainWindow, container));
	}
}
