package com.bibler.biblerizer;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.JOptionPane;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class ImageLoader extends BaseObject {
	
	ImageReader reader;
	PNGDecoder decoder;
	File imageFile;
	int imageWidth;
	int imageHeight;
	boolean decoderClosed;
	float loadProgress;
	Thread loadThread;

	public ImageLoader() {
	}
	
	public void setFile(File f) {
		imageFile = f;
		setup();
	}
	
	public void setup() {
		FileImageInputStream is = null;
		try {
			is = new FileImageInputStream(imageFile);
		} catch (IOException e) {}
		reader = ImageIO.getImageReaders(is).next();
		reader.setInput(is, false, true);
		try {
			imageWidth = reader.getWidth(0);
			imageHeight = reader.getHeight(0);
		} catch(IOException e) {}
	}

	public void getImage() {
		decoder = new PNGDecoder();
		decoder.setFile(imageFile);
		loadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				File output = null;
				try {
					output = decoder.decode();
				} catch(OutOfMemoryError e) {
					JOptionPane.showMessageDialog(BaseObject.sRegistry.mainFrame, 
							"Not enough memory to complete this action.\n" +
							"Please close other programs and try again.");
					BaseObject.sRegistry.geoReferencePanel.goBack();
				}
				if(decoder != null) {
		       		if(decoder.bb.hasRemaining()) {
		       			while(decoder.bb.hasRemaining()) {
		       				try {
		       					Thread.sleep(30);
		       				} catch(InterruptedException e){}
		       			}
		       		} else {
		       			try {
		       				decoder.fc.close();
		       				decoder.tmpFile.close();
		       			} catch(IOException e) {}
		       			finally {
		       				decoder.bb = null;
		       				decoder = null;
		       				System.gc();
		       				BaseObject.sRegistry.image = new MapImage(output, imageWidth, imageHeight);
		    				BaseObject.sRegistry.grPanel.setImage(BaseObject.sRegistry.image.smallify());
		    				BaseObject.sRegistry.geoReferencePanel.updateButtons(true);
		       			}
		       		}
				}
				
			}
		});
		loadThread.start();
	}
}
