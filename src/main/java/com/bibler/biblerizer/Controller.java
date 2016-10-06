package com.bibler.biblerizer;
import java.awt.image.BufferedImage;
import java.io.File;


public class Controller extends BaseObject {
	
	ImageLoader loader;
	int width;
	int height;
	int baseZoom;
	Tiler tiler;
	 
	boolean running;
	boolean pause;
	boolean started;
	float angle;
	Object pauseLock = new Object();
	TileRunner tileRunner;
	Thread thread;
	 
	public Controller() {
		tileRunner = new TileRunner();
		tileRunner.pause();
		running = true;
		thread = new Thread(tileRunner);
		thread.start();
	}
	
	public void setFile(File f) {
		BaseObject.sRegistry.fileRoot = f.getAbsolutePath().substring(0, f.getAbsolutePath().length() -4);
		if(loader == null) {
			loader = new ImageLoader();
			BaseObject.sRegistry.loader = loader;
		}
		loader.setFile(f);
		loader.getImage();
		width = loader.imageWidth;
		height = loader.imageHeight;
		BaseObject.sRegistry.imageWidth = width;
		BaseObject.sRegistry.imageHeight = height;
		sRegistry.infoPanel.updateImageInfo(width, height, f);
		tiler = new Tiler();
	}
	 
	public void setCoords(float north, float south, float east, float west, float a) {
		sRegistry.angle = a < 0 ? a : a - 360;
		angle = sRegistry.angle;
		if(north > 1000 || south > 1000 || east > 1000 || west > 1000) {
			Coordinate westNorth = MathUtils.convertFrom3857(new Coordinate(west, north));
			Coordinate eastSouth = MathUtils.convertFrom3857(new Coordinate(east, south));
			west = westNorth.lon;
			north = westNorth.lat;
			east = eastSouth.lon;
			south = eastSouth.lat;
		}
		sRegistry.infoPanel.updateCoords(north, south, east, west);
		baseZoom = figureInitialValues(north, south, east, west, width, height, true);
		if(baseZoom == 0)
			baseZoom = 1;
		sRegistry.northInDegrees = north;
		sRegistry.southInDegrees = south;
		sRegistry.eastInDegrees = east;
		sRegistry.westInDegrees = west;
		figureDims(new Coordinate(west, north), new Coordinate(east, south));
		BaseObject.sRegistry.baseZoom = baseZoom;
	}

	public void figureDims(Coordinate nw, Coordinate se) {
		nw = MathUtils.LatLongToPixelXY(nw, baseZoom);
		se = MathUtils.LatLongToPixelXY(se, baseZoom);
		sRegistry.startX = nw.lon;
		sRegistry.startY = nw.lat;
		float coordWidth = se.lon - nw.lon;
		float coordHeight = se.lat - nw.lat;
		sRegistry.coordWidth = coordWidth;
		sRegistry.coordHeight = coordHeight;
		if(angle != 0 && angle != 360 && angle != -360)
			sRegistry.rotated = true;
		double theta;
		if(angle > -90 && angle < 0) {
			theta = -angle;
		} else if(angle < -90 && angle > -180) {
			theta = angle;
		} else if(angle < -180 && angle > -270) {
			theta = -angle;
		} else {
			theta = angle;
		}
		double sin =  Math.sin(Math.toRadians(theta));
		double cos =  Math.cos(Math.toRadians(theta));
		sRegistry.outputWidth = (float) Math.abs(((coordWidth * cos) + (coordHeight * sin)));
		sRegistry.outputHeight = (float) Math.abs(((coordWidth * sin) + (coordHeight * cos)));
		int xDiff = (int) (sRegistry.outputWidth - coordWidth) / 2;
		int yDiff = (int) (sRegistry.outputHeight - coordHeight) / 2;
		sRegistry.startX -= xDiff;
		sRegistry.startY -= yDiff;
		sRegistry.progressPanel.tilePanel.setImage();
		sRegistry.scaleX = coordWidth / (float) width;
		sRegistry.scaleY = coordHeight / (float) height;
	}
	 
	public void start() {
		 tileRunner.resume();
	 }
	
	public void tile() {
		started = true;
		System.out.println("Tiler Started");
		while(!BaseObject.sRegistry.progressPanel.tilePanel.loaded) {
			try {
				synchronized(BaseObject.sRegistry.progressPanel.tilePanel.pauseLock) {
					BaseObject.sRegistry.progressPanel.tilePanel.pauseLock.wait();
				}
			} catch(InterruptedException e) {}
		}
		tiler.setup();
		System.out.println("Done");
		System.out.println("Wrote: " + BaseObject.sRegistry.pixelsWrit);
		System.out.println("totalPixels: " + BaseObject.sRegistry.totalPixels);
	}

	public int figureInitialValues(float north, float south, float east, float west, int width, int height, boolean base) {Coordinate nw = MathUtils.convertTo3857(new Coordinate(west, north));
		Coordinate se = MathUtils.convertTo3857(new Coordinate(east, south));
		BaseObject.sRegistry.northInMeters = nw.lat;
		BaseObject.sRegistry.southInMeters = se.lat;
		BaseObject.sRegistry.eastInMeters = se.lon;
		BaseObject.sRegistry.westInMeters = nw.lon;
		BaseObject.sRegistry.imageWidth = width;
		BaseObject.sRegistry.imageHeight = height;
		BaseObject.sRegistry.widthInMeters = BaseObject.sRegistry.eastInMeters - BaseObject.sRegistry.westInMeters;
		BaseObject.sRegistry.heightInMeters = BaseObject.sRegistry.northInMeters - BaseObject.sRegistry.southInMeters;
		BaseObject.sRegistry.metersPerPixel = BaseObject.sRegistry.widthInMeters / (float) width;
		return MathUtils.closestZoomLevel(BaseObject.sRegistry.metersPerPixel);
	}
	
	public void exit() {
		sRegistry.image.disposeImage();
		System.exit(0);
	}
	 
	public class TileRunner implements Runnable {
		public void pause() {
			synchronized(pauseLock) {
				pause = true;
			}
		}
		 
		public void resume() {
			synchronized(pauseLock) {
				pause = false;
				pauseLock.notifyAll();
			}
		}
		 
		@Override
		public void run() {
			while(running) {
				synchronized(pauseLock) {
					if (pause) {
						while (pause) {
							try {
								pauseLock.wait();
							} catch (InterruptedException e) {}
						}
					}
				}
				tile();
				sRegistry.progressPanel.done();
				pause();
			}
		}
	}
}
