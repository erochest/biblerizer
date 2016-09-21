package com.bibler.biblerizer;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class Main {
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(
		            UIManager.getSystemLookAndFeelClassName());
			} 
			catch (UnsupportedLookAndFeelException e) {}
			catch (ClassNotFoundException e) {}
			catch (InstantiationException e) {}
			catch (IllegalAccessException e) {}
			Controller controller = new Controller();
			BaseObject.sRegistry.controller = controller;
			MainFrame frame = new MainFrame();
			frame.setVisible(true);
	}
}