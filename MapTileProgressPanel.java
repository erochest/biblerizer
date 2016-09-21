package com.bibler.biblerizer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;


public class MapTileProgressPanel extends JPanel {
	
	BufferedImage baseImage;
	BufferedImage overlayImage;
	
	int imageWidth;
	int scaleXWidth;
	int scaleYHeight;
	int pixelX;
	int pixelY;
	int imgCornerX;
	int imgCornerY;
	
	int imageZoom;
	int maxWidth;
	int maxHeight;
	float scaleXFactor;
	float scaleYFactor;
	int pixelXOffset;
	int pixelYOffset;
	float imgWidth;
	float imgHeight;
	boolean loaded;
	Graphics2D g2D;
	ImageLoader loader;
	JLabel label;
	URI attribLink;
	Object pauseLock = new Object();
	int tryAgainCount = 0;
	int newA;
	int oldA;

	public MapTileProgressPanel() {
		super();
		try {
			attribLink = new URI("http://www.openstreetmap.org/copyright");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String attrib = "<html><center>Data, imagery and map information provided by MapQuest, <br>" +
				"<a href = \"http://www.openstreetmap.org/copyright\">OpenStreetMap</a> and contributors, ODbL</center><html>";
		label = new JLabel(attrib);
		label.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(arg0.getClickCount() < 2)
					return;
				try {
					Desktop.getDesktop().browse(attribLink);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				label.setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mousePressed(MouseEvent arg0) {}

			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
		});
		add(label);
		SpringLayout layout = new SpringLayout();
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, label, 0, SpringLayout.HORIZONTAL_CENTER, this);
		layout.putConstraint(SpringLayout.SOUTH, label, -5, SpringLayout.SOUTH, this);
		setLayout(layout);
		setPreferredSize(new Dimension(512, 512));
		setMaximumSize(new Dimension(512, 512));
		maxWidth = (int) (512 * .8f);
		maxHeight = (int) (512 * .8f);
		imgCornerX = (512 - maxWidth) / 2;
		imgCornerY = (512 - maxHeight) / 2;
		setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		setBackground(Color.DARK_GRAY);
		overlayImage = new BufferedImage(512, 512, BufferedImage.TYPE_4BYTE_ABGR);
		baseImage = new BufferedImage(512, 512, BufferedImage.TYPE_4BYTE_ABGR);
		addMouseMotionListener(new Listener());
	}
	
	public void setImage() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				figureImageDims();
			}
		});
		thread.start();	
	}
	
	public void figureImageDims() {
		Registry sRegistry = BaseObject.sRegistry;
		int baseZoom = sRegistry.baseZoom;
		int zoom;
		float startX = sRegistry.startX;
		float startY = sRegistry.startY;
		float endX = sRegistry.startX + sRegistry.outputWidth;
		float endY = sRegistry.startY + sRegistry.outputHeight;
		float metersPerPixel = MathUtils.zoomLevels[baseZoom];
		float width = sRegistry.outputWidth * metersPerPixel;
		float height = sRegistry.outputHeight * metersPerPixel;

		if(width > height) {
			imgWidth = maxWidth;
			imgHeight = maxHeight * (height / width);
			zoom = MathUtils.closestZoomLevel(width / imgWidth);
		} else {
			imgHeight = maxHeight;
			imgWidth = maxWidth * (width / height);
			zoom = MathUtils.closestZoomLevel(height / imgHeight);
		}
		int widthPixels = (int) (width / MathUtils.zoomLevels[zoom]);
		int heightPixels = (int) (height / MathUtils.zoomLevels[zoom]);
		scaleXFactor = widthPixels / imgWidth;
		scaleYFactor = heightPixels / imgHeight;
		scaleXWidth = (int) (512 / scaleXFactor);
		scaleYHeight = (int) (512 / scaleYFactor);

		int wTileX = 0;
		int nTileY = 0;
		int eTileX = 0;
		int sTileY = 0;
		startX = MathUtils.pixelToLon(startX, baseZoom);
		startY = MathUtils.pixelToLat(startY, baseZoom);
		endX = MathUtils.pixelToLon(endX, baseZoom);
		endY = MathUtils.pixelToLat(endY, baseZoom);
		pixelX = (int) MathUtils.LatLongToPixelXY(new Coordinate(startX, startY), zoom).lon;
		pixelY = (int) MathUtils.LatLongToPixelXY(new Coordinate(startX, startY), zoom).lat;
		nTileY = (int) Math.floor(pixelY / 256);
		wTileX = (int) Math.floor(pixelX / 256);
		imageZoom = zoom;
		pixelXOffset = (int) ((pixelX - (wTileX * 256)));
		pixelYOffset = (int) (pixelY - (nTileY * 256));

		eTileX = wTileX + ((512 - (imgCornerX - pixelXOffset)) / 256) + 1;

		sTileY = nTileY + ((512 - (imgCornerY - pixelYOffset)) / 256) + 1;

		URL temp = null;

		for(int i = 0; i <= eTileX - wTileX; i++) {
			for(int j = 0; j <= sTileY - nTileY; j++) {
				try {
					temp = new URL("http://otile1.mqcdn.com/tiles/1.0.0/sat/" + imageZoom + "/" + (wTileX + i) + "/" + (nTileY + j) + ".jpg");
					baseImage.getGraphics().drawImage(ImageIO.read(temp), imgCornerX - pixelXOffset + (i * 256), imgCornerY - pixelYOffset + (j * 256), null);
				} catch (IOException e) {}
			}
		}

		synchronized(pauseLock) {
			pauseLock.notifyAll();
		}
		loaded = true;
	}
	
	public void setPixel(int x, int y, int pixel) {
		if((pixel >> 24 & 0xFF) == 0)
			return;
		int xPos = imgCornerX + (x - pixelX);
		int yPos = imgCornerY + Math.abs(pixelY - y);
		if(xPos < 0 || xPos > 512 || yPos < 0 || yPos > 512)
			return;
		newA = pixel >> 24 & 0xFF;
		oldA = overlayImage.getRGB(xPos,yPos) >> 24 & 0xFF;
		if(newA < oldA)
			return;
		overlayImage.setRGB(imgCornerX + (x - pixelX), imgCornerY + Math.abs(pixelY - y), pixel);
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawBaseImage(g);
		
	}
	
	public void drawBaseImage(Graphics g) {
		g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		int offsetX = (int) (imgCornerX / scaleXFactor) - imgCornerX;
		int offsetY = (int) (imgCornerY / scaleYFactor) - imgCornerY;
		g2D.drawImage(baseImage, -offsetX, -offsetY, scaleXWidth, scaleYHeight, null);
		g2D.drawImage(overlayImage, -offsetX, -offsetY, scaleXWidth, scaleYHeight, null);
		g2D.setColor(Color.YELLOW);
		g2D.drawRect(imgCornerX, imgCornerY, (int) (imgWidth + 1), (int) (imgHeight + 1));
		g2D.setColor(Color.RED);
		g2D.drawRect((int) ((imgCornerX + (imgWidth / 2)) - (BaseObject.sRegistry.rotateWidth / 2)),
				(int) ((imgCornerY + (imgHeight / 2)) - BaseObject.sRegistry.rotateHeight / 2),
				(int) BaseObject.sRegistry.rotateWidth, (int) BaseObject.sRegistry.rotateHeight);
	}

	public class Listener implements MouseMotionListener {


		@Override
		public void mouseDragged(MouseEvent e) {

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			BaseObject.sRegistry.infoPanel.updateCoords(x, y, 0, 0);
		}
	}

}
