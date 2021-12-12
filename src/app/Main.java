package app;

import javax.swing.SwingUtilities;

import org.opencv.core.Core;

import app.gui.UserInterface;

public class Main {
	// Constructor generating Java Swing window
	public Main() {
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		new UserInterface();
	}

	public static void main(String[] args) {
		// Ensure thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Main();
			}
		});
	}
}
