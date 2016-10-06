package com.bibler.biblerizer;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;


public class Tiler extends BaseObject {
	
	static Tile[][][] tiles;
	int width;
	int height;
	double sin;
	double cos;
	float cX;
	float cY;
	long tilesInMemory;
	long maxTilesAllowed;
	Vector<Tile> dirtyTiles;
	Coordinate[] strides;
	
	public Tiler() {
		sRegistry.tiler = this;
		dirtyTiles = new Vector<Tile>();
	}
	
	public void setup() {
		BaseObject.sRegistry.writingTiles = false;
		int zoom = sRegistry.baseZoom;
		tiles = new Tile[zoom][][];
		width = sRegistry.image.width;
		height = sRegistry.image.height;
		tile();
		writeAllTiles(true);
	}
	
	public void addDirtyTile(Tile t) {
		if(!dirtyTiles.isEmpty() && dirtyTiles.contains(t))
			return;
		dirtyTiles.add(t);
	}
	
	public void writeAllTiles(boolean done) {
		BaseObject.sRegistry.writingTiles = true;
		boolean visible = sRegistry.progressPanel.timeRemainingLabel.isVisible();
		String oldLabel = sRegistry.progressPanel.timeRemainingLabel.getText();
		sRegistry.progressPanel.timeRemainingLabel.setText("Writing tiles...");
		BaseObject.sRegistry.progressPanel.timeRemainingLabel.setVisible(true);
		Tile tile;
		int tileCount = 0;
		long start = System.currentTimeMillis();
		while(!dirtyTiles.isEmpty()) {
			tile = dirtyTiles.remove(0);
			if(tile == null || tile.pixels == null)
				continue;
			tileCount++;
			tile.writeTile();
			tilesInMemory -= (256 * 256);
		}
		long elapsed = System.currentTimeMillis() - start;
		System.gc();
		System.out.println("Done wrote " + tileCount + " in " + elapsed);
		sRegistry.writingTiles = false;
		BaseObject.sRegistry.progressPanel.timeRemainingLabel.setText(oldLabel);
		BaseObject.sRegistry.progressPanel.timeRemainingLabel.setVisible(visible);
	}
	
	public void addTilesForZoomLevel(int zoom) {
		int divisor = (int) Math.pow(2, (sRegistry.baseZoom - 1) - zoom);
		int startX = (int) (sRegistry.startX / divisor);
		int startY = (int) (sRegistry.startY / divisor);
		int w = (int) (sRegistry.outputWidth / divisor);
		int h = (int) (sRegistry.outputHeight / divisor);
		int endX = startX + w;
		int endY = startY + h;
		startX /= 256;
		startY /= 256;
		endX /= 256;
		endY /= 256;
		int tilesAcross = (int) Math.ceil(endX - startX) + 1;
		int tilesDown = (int) Math.ceil(endY - startY) + 1;

		if(tiles[zoom] != null)
			return;
		tiles[zoom] = new Tile[tilesAcross][tilesDown];
		Tile[][] zoomTiles = tiles[zoom];
		for(int i = 0; i < tilesAcross; i++) {
			for(int j = 0; j < tilesDown; j++) {
				zoomTiles[i][j] = new Tile(startX + i, startY + j, zoom + 1);
			}
		}
	}
	
	public void tile() {
		for(int i = tiles.length - 1; i >= 0; i--) {
			addTilesForZoomLevel(i);
		}
		maxTilesAllowed = (long) (Runtime.getRuntime().freeMemory() * .25);
		strides = figureStrides();
		if(sRegistry.rotated)
			tileRotate();
		else
			tileSquare();
	}
	
	public void tileRotate() {
		int pixel;
		int divFactor;
		float sX;
		float sY;
		int strX;
		int strY;
		sin = Math.sin(Math.toRadians(-sRegistry.angle));
		cos = Math.cos(Math.toRadians(-sRegistry.angle));
		float newX;
		float newY;
		cX = (sRegistry.coordWidth / 2);
		cY =  (sRegistry.coordHeight / 2);
		float rawX;
		float rawY;
		int passCounter = 0;
		sRegistry.image.reset();
		Point cutOff;
		do {
			sRegistry.image.loadNextBuffer();
			cutOff = sRegistry.image.findRotateCutoff();
			sRegistry.progressPanel.timeRemainingLabel.setText("Pass " + ++passCounter + " of " + sRegistry.image.divisions);
			sRegistry.progressPanel.timeRemainingLabel.setVisible(true);
			for(int y = cutOff.y; y < cutOff.x; y++) {
				sRegistry.progressPanel.updateProgress((y / (float) sRegistry.outputHeight));
				for(int x = 0; x < sRegistry.outputWidth; x++) {
					divFactor = 1;
					sX = sRegistry.scaleX / divFactor;
					sY = sRegistry.scaleY / divFactor;
					newX = x;
					newY = y;
					for(int z = sRegistry.baseZoom - 1; z >= 0; z--) {
						if(x % divFactor == 0 && y % divFactor == 0) {
							strX = (int) strides[z].lon;
							strY = (int) strides[z].lat;
							rawX = x + ((sRegistry.coordWidth - sRegistry.outputWidth) / 2);
							rawY = y + ((sRegistry.coordHeight - sRegistry.outputHeight) / 2);
							newX = (float) (cos * (rawX - cX) - sin * (rawY - cY) + cX);
							newY = (float) (sin * (rawX - cX) + cos * (rawY - cY) + cY);
							newX /= sX;
							newY /= sY;
							if(newX < 0 || newY < 0 || newX >= sRegistry.image.width || newY >= sRegistry.image.height) {
								//pixel = 0 << 24;
								continue;
							} else {
								if (strX < 0 || strY < 0 || z == sRegistry.baseZoom - 1) {
									pixel = sRegistry.image.weightedAvg(newX, newY);
								} else
									pixel = sRegistry.image.average(newX, newY, strX, strY);
							}
							addPixel(pixel, x / divFactor, y / divFactor, z);
						}
						divFactor *= 2;
					}
				}
			} 
			writeAllTiles(false);
		} while(sRegistry.image.remaining > 0);
		return;
	}
	
	public void tileSquare() {
		int pixel;
		int divFactor;
		float sX = sRegistry.scaleX;
		float sY = sRegistry.scaleY;
		int strX;
		int strY;
		float newX;
		float newY;
		int line;
		int strideY = (int) strides[0].lat;
		int bufferLine;
		sRegistry.image.reset();
		sRegistry.image.loadNextBuffer();
		for(int y = 0; y < sRegistry.outputHeight; y++) {
			line = (int) Math.ceil(y / (sRegistry.scaleY));
			if(Math.abs(sRegistry.image.bufferLineEndPos - line) <= strideY) {
				sRegistry.image.loadBufferAtLine(line - strideY);
			}
			sRegistry.progressPanel.updateProgress((y / (float) sRegistry.outputHeight));
			for(int x = 0; x < sRegistry.outputWidth; x++) {
				divFactor = 1;
				newX = x;
				newY = y;
				for(int z = sRegistry.baseZoom - 1; z >= 0; z--) {
					if(x % divFactor == 0 && y % divFactor == 0) {
						strX = (int) strides[z].lon;
						strY = (int) strides[z].lat;
						newX = x / sX;
						newY = y / sY;
						if(newX < 0 || newY < 0 || newX >= sRegistry.image.width || newY >= sRegistry.image.height) {
							//pixel = 0 << 24;
							continue;
						} else {
							if (strX < 0 || strY < 0 || z == sRegistry.baseZoom - 1) {
								pixel = sRegistry.image.weightedAvg(newX, newY);
							} else
								pixel = sRegistry.image.average(newX, newY, strX, strY);
						}
						addPixel(pixel, x / divFactor, y / divFactor, z);
					}
					divFactor *= 2;
				}
			}
		} 
		return;
	}

	public Coordinate[] figureStrides() {
		Coordinate[] ret = new Coordinate[sRegistry.baseZoom];
		int divFactor = 1;
		int strX;
		int strY;
		int maxStride = 100;
		for(int i = ret.length - 1; i >= 0; i--) {
			strX = (int) (sRegistry.imageWidth / (sRegistry.outputWidth / divFactor));
			strY = (int) (sRegistry.imageHeight / (sRegistry.outputWidth / divFactor));
			if(strX > maxStride)
				strX = maxStride;
			if(strY > maxStride)
				strY = maxStride;
			ret[i] = new Coordinate(strX, strY);
			divFactor *= 2;

		}
		sRegistry.image.setStideY((int) (ret[0].lat));
		return ret;
	}
	
	public void addPixel(int pixel, int x, int y, int zoom) {
		Tile[][] tempTiles = tiles[zoom];
		int divisor = (int) Math.pow(2, (sRegistry.baseZoom - 1) - zoom);
		int sX = (int) (sRegistry.startX / divisor);
		int sY = (int) sRegistry.startY / divisor;
		x += sX;
		y += sY;

		if(zoom + 1 == sRegistry.progressPanel.tilePanel.imageZoom) {
			sRegistry.progressPanel.tilePanel.setPixel(x, y, pixel);
		}
		int tileXCorner = (int) Math.floor(sX / 256.0f);
		int tileYCorner = (int) Math.floor(sY / 256.0f);
		int tileX = (int) Math.floor(x / 256.0f);
		int tileY = (int) Math.floor(y / 256.0f);
		int tileXRel = tileX - tileXCorner;
		int tileYRel = tileY - tileYCorner;
		tempTiles[tileXRel][tileYRel].addPixel(new Coordinate(x,y), pixel);
	}
	
	public void writeTiles(int zoom, boolean done) {
		Tile[][] tempTiles = tiles[zoom];
		Tile tile;
		for(int i = 0; i < tempTiles.length; i++) {
			for(int j = 0; j < tempTiles[i].length; j++) {
				tile = tempTiles[i][j];
				if(tile == null || tile.pixels == null)
					continue;
				tempTiles[i][j].writeTile();
				tempTiles[i][j].pixels = null;
				tilesInMemory -= (256 * 256);
			}
		}
	}
}
