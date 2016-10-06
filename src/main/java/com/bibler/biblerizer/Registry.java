package com.bibler.biblerizer;
import java.awt.Color;
import java.awt.Font;
import java.io.File;

import javax.swing.JFileChooser;


public class Registry {
	
	int imageWidth;
	int imageHeight;
	float widthInMeters;
	float heightInMeters;
	float northInMeters;
	float southInMeters;
	float eastInMeters;
	float westInMeters;
	float metersPerPixel;
	float westInDegrees;
	float northInDegrees;
	float southInDegrees;
	float eastInDegrees;
	int baseZoom;
	long totalCalcs = 0;
	
	Coordinate[] scaleFactors;
	
	String fileRoot = "C:/users/ryan/desktop/images/map";
	public Coordinate startPixel;
	
	int totalPixels;
	int pixelsWrit;
	
	Controller controller;
	//UI Elements
	public MainFrame mainFrame;
	public InfoPanel infoPanel;
	public ImageLoadPanel loadPanel;
	public GeoReferencePanel geoReferencePanel;
	public OutputPanel outputPanel;
	public MapProgressPanel progressPanel;
	public MapTileProgressPanel tilePanel;
	public JFileChooser chooser;
		
		// UI params
	public Font messageFont = Font.decode("tahoma-bold-18");
	public Font messageFontSmall = Font.decode("tahoma-bold-16");
	public Color skyBlue = new Color(0, 0x99, 0xFF, 0xFF);
	public Color skyBlueLight = new Color(0, 0x99, 0xFF, 0xBC);

	public File imageFileLocation;
	public Tiler tiler;
	boolean writingTiles;
	public long assumedReadTime;
	public long assumedWriteTime;
	long totalReadTime;
	long totalWriteTime;
	int readCycles;
	int writeCycles;
	int readCyclesRemaining;
	int writeCyclesRemaining;
	public float scaleX;
	public float scaleY;
	public float startX;
	public float startY;
	public float outputWidth;
	public float outputHeight;
	float angle;
	float rotateWidth;
	float rotateHeight;
	float coordWidth;
	float coordHeight;
	public Map map;
	public GRPanel grPanel;
	public boolean drawLines = true;
	public ImageLoader loader;
	MapImage image;
	public boolean rotated;
}

