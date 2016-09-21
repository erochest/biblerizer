package com.bibler.biblerizer;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.imageio.ImageIO;


public class Tile extends BaseObject {
	
	int row;
	int col;
	int pixelX;
	int pixelY;
	int lastXOffset;
	int lastYOffset;
	int zoom;
	int curCount = 0;
	int divisor = 1;
	int[] pixels;

	int pixelCount;
	int rAvg;
	int gAvg;
	int bAvg;
	int aAvg;
	int lastPixel = 0;
	boolean dirty;
	
	public Tile(int c, int r, int z) {
		col = c;
		row = r;
		zoom = z;
		pixelX = col * 256;
		pixelY = row * 256;
	}
	
	public void addPixel(Coordinate pixelCoords, int pixel) {
		if((pixel >> 24 & 0xFF) == 0)
			return;
		int xOffset = (int) (pixelCoords.lon % 256);
		int yOffset = (int) (pixelCoords.lat % 256);
		if(pixels == null) {
			if(sRegistry.tiler.tilesInMemory + (256 * 256) >= sRegistry.tiler.maxTilesAllowed) {
				sRegistry.tiler.writeAllTiles(false);
			}
			pixels = new int[256 * 256];
			sRegistry.tiler.tilesInMemory += 256 * 256;
		}
		if(!dirty) {
			dirty = true;
			sRegistry.tiler.addDirtyTile(this);
		}
		if(((pixel >> 16) & 0xFF) == 0 && ((pixel >> 8) & 0xFF) == 0 && (pixel & 0xff)  == 0) {
			pixel = 0 << 24 | 0 << 16 | 0 << 8 | 255;
		}
		int curPixel = (yOffset * 256) + xOffset;
		try {
			pixels[curPixel] = pixel;
		}catch(ArrayIndexOutOfBoundsException e) {}
	}
	
	public void writeTile() {
		File imgFile = new File(BaseObject.sRegistry.fileRoot + "/" + zoom + "/" + col + "/" + row + ".png");
		if(!imgFile.exists()) {
			imgFile.mkdirs();
			BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
			if(pixels != null) {
				img.setRGB(0, 0, 256, 256, pixels, 0, 256);
			}
			try {
				ImageIO.write(img, "png", imgFile);
			} catch(IOException e) {}
		} else {
			writeToExistingFile(imgFile);
		}	
		pixels = null;
		dirty = false;
	}
	
	public void interpPixels() {
		if(divisor > 1) {
			System.out.println("Zoom: " + zoom + "Divisor: " + divisor);
		}
		for(int i = 0; i < pixels.length; i++) {
			pixels[i] /= divisor;
		}
	}
	
	public void writeToExistingFile(File f) {
		BufferedImage img = null;
		int width = 0;
		int height = 0;
		int curPixel;
		int newPixel = 0;
		int alphaO;
		int alphaN;
		try {
			img = ImageIO.read(f);
			width = img.getWidth();
			height = img.getHeight();
		} catch(IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				curPixel = img.getRGB(i, j);
				alphaO = curPixel >> 24 & 0xFF;
				newPixel = 0;
				if(pixels != null) {
					newPixel = pixels[(j * 256) + i];
					alphaN = newPixel >> 24 & 0xFF;
					if(alphaN > alphaO) {
						curPixel = newPixel;
					}
				}
				img.setRGB(i, j, curPixel);
			}
		}
		try {
			ImageIO.write(img, "png", f);
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
