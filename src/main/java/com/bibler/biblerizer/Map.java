package com.bibler.biblerizer;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

/**
 * Created by Ryan on 2/7/2015.
 */
public class Map implements Runnable {

    Vector<Tile> oldTiles;
    Vector<Tile> newTiles;
    Coordinate nwDegrees;
    Coordinate seDegrees;
    Coordinate nwMeters;
    Coordinate seMeters;
    Coordinate nwPixels;
    Coordinate sePixels;
    Coordinate centerDegrees;
    Coordinate centerMeters;
    Coordinate centerPixels;
    Coordinate oldCenterPixels;
    int curZoom;
    int oldZoom;
    int widthInTiles;
    int heightInTiles;
    int oldWidthInTiles;
    int oldHeightInTiles;
    int panelWidth;
    int panelHeight;
    float oldXOffset;
    float oldYOffset;
    int startX;
    int startY;
    float xOffset;
    float yOffset;
    float wMeters;
    float hMeters;
    boolean dirty;
    Thread t;
    boolean pause;
    boolean running;
    Object pauseLock = new Object();
    float lastSX;
    float lastSY;
    boolean updating;
    String mapType = "sat";

    public Map(float centerX, float centerY, int z, int pWidth, int pHeight) {
        t = new Thread(this);
        pause();
        running = true;
        t.start();
        panelWidth = pWidth;
        panelHeight = pHeight;
        oldTiles = new Vector<Tile>();
        newTiles = new Vector<Tile>();
        nwDegrees = new Coordinate();
        nwPixels = new Coordinate();
        nwMeters = new Coordinate();
        seDegrees = new Coordinate();
        seMeters = new Coordinate();
        sePixels = new Coordinate();
        centerDegrees = new Coordinate(centerX, centerY);
        centerMeters = MathUtils.convertTo3857(centerDegrees);
        centerPixels = MathUtils.LatLongToPixelXY(centerDegrees, z);
        oldCenterPixels = new Coordinate(centerPixels.lon, centerPixels.lat);
        curZoom = z;
        oldZoom = z;
        dirty = true;
        figureTiles();
    }

    public Vector2D figureTiles() {
        updating = true;
        float pixelsN = (centerPixels.lat - (panelHeight / 2));
        float pixelsW = (centerPixels.lon - (panelWidth / 2));
        float pixelsE  = pixelsW + panelWidth;
        float pixelsS = pixelsN + panelHeight;
        if(pixelsW < 0) {
            float diff = pixelsW;
            pixelsW = 0;
            centerPixels.lon -= diff;
        }
        if(pixelsN < 0) {
            float diff = pixelsN;
            pixelsN = 0;
            centerPixels.lat -= diff;
        }
        float mapSize = MathUtils.MapSize(curZoom);
        if(pixelsE > mapSize) {
            float diff = pixelsE - mapSize;
            pixelsE = mapSize;
            centerPixels.lon -= diff;
        }
        if(pixelsS > mapSize) {
            float diff = pixelsS - mapSize;
            pixelsS = mapSize;
            centerPixels.lat -= diff;
        }
        float deltaX = centerPixels.lon - oldCenterPixels.lon;
        float deltaY = centerPixels.lat - oldCenterPixels.lat;
        oldCenterPixels.lon = centerPixels.lon;
        oldCenterPixels.lat = centerPixels.lat;
        float coordNorth = MathUtils.pixelToLat(pixelsN, curZoom);
        float coordSouth = MathUtils.pixelToLat(pixelsS, curZoom);
        float coordWest = MathUtils.pixelToLon(pixelsW, curZoom);
        float coordEast = MathUtils.pixelToLon(pixelsE, curZoom);
        nwPixels.lon = pixelsW;
        nwPixels.lat = pixelsN;
        sePixels.lon = pixelsE;
        sePixels.lat = pixelsS;
        nwDegrees = new Coordinate(coordWest, coordNorth);
        seDegrees = new Coordinate(coordEast, coordSouth);
        nwMeters = MathUtils.convertTo3857(nwDegrees);
        seMeters = MathUtils.convertTo3857(seDegrees);
        wMeters = seMeters.lon - nwMeters.lon;
        hMeters = nwMeters.lat - seMeters.lat;
        xOffset =  -(nwPixels.lon % 256);
        yOffset =  -(nwPixels.lat % 256);
        widthInTiles = (int) ((sePixels.lon - (nwPixels.lon + xOffset)) / 256) + 1;
        heightInTiles = (int) ((sePixels.lat - (nwPixels.lat + yOffset)) / 256) + 1;
        if(heightInTiles < oldHeightInTiles && oldZoom == curZoom) {
            heightInTiles = oldHeightInTiles;
        }
        startX = (int)  (nwPixels.lon / 256);
        startY =  (int) (nwPixels.lat / 256);
        if(lastSX == startX && lastSY == startY && !dirty) {
            updating = false;
            BaseObject.sRegistry.grPanel.repaint();
        } else {
           resume();
        }
        lastSX = startX;
        lastSY = startY;
        oldWidthInTiles = widthInTiles;
        oldHeightInTiles = heightInTiles;
        oldZoom = curZoom;
        oldXOffset = xOffset;
        oldYOffset = yOffset;
        return new Vector2D(deltaX, deltaY);
    }

    public void updateTiles() {
        Tile t;
        newTiles.clear();
        Tile oT;
        boolean found;
        for (int x = 0; x < widthInTiles; x++) {
            for (int y = 0; y < heightInTiles; y++) {
                found = false;
                t = new Tile(startX + x, startY + y, curZoom);
                newTiles.add(t);
                if(!dirty) {
                    for (int i = 0; i < oldTiles.size(); i++) {
                        oT = oldTiles.get(i);
                        if (oT.x == t.x && oT.y == t.y && oT.z == t.z) {
                            t.img = oT.img;
                            found = true;
                            break;
                        }
                    }
                }
                if (found) {
                    continue;
                } else {
                    t.img = getImage(t);
                }
            }
        }
        dirty = false;
        updating = false;
        oldTiles.clear();
        for(int i = 0; i < newTiles.size(); i++) {
            t = newTiles.get(i);
            oT = new Tile(t.x, t.y, t.z);
            oT.img = t.img;
            oldTiles.add(oT);
        }
    }

    public Vector2D goToPoint(Vector2D point) {
        float deltaX = (point.x + nwPixels.lon) - centerPixels.lon;
        float deltaY = centerPixels.lat - (point.y + nwPixels.lat);
        return translate(new Vector2D(deltaX, deltaY));
    }

    public Vector2D translate(Vector2D delta) {
        centerPixels.lon += delta.x;
        centerPixels.lat -= delta.y;
        return figureTiles();
    }

    public void zoom(int zoomDir) {
        centerDegrees.lon = MathUtils.pixelToLon(centerPixels.lon, curZoom);
        centerDegrees.lat = MathUtils.pixelToLat(centerPixels.lat, curZoom);
        curZoom += zoomDir;
        if(curZoom < 0)
            curZoom = 0;
        if(curZoom >= MathUtils.zoomLevels.length)
            curZoom = MathUtils.zoomLevels.length - 1;
        centerPixels = MathUtils.LatLongToPixelXY(centerDegrees, curZoom);
        figureTiles();
        if(zoomDir < 1) {
            BaseObject.sRegistry.grPanel.rect.scaleDown();
        } else {
            BaseObject.sRegistry.grPanel.rect.scaleUp();
        }
    }

    public void drawMap(Graphics g) {
        Point p;
        int x;
        int y;
        Tile temp;
        for(int i = 0; i < newTiles.size(); i++) {
            temp = newTiles.get(i);
            if(temp == null)
                continue;
            x = temp.x - startX;
            y = temp.y - startY;
            g.drawImage(temp.img,(int) (x * 256 + xOffset), (int) (y * 256 + yOffset), null);
        }
    }

    public Image getImage(Tile t) {

        Image ret = null;
        String url = "http://dev.virtualearth.net/REST/v1/Imagery/Map/Aerial/"
            + t.x + "," + t.y + "/"
            + curZoom + "/"
            + "&format=jpeg"
            + "&key=AozBteKhpdAaLf20ql4_MDxo4BZ1Y3WWnT_ckiSiL2oVhPcCwkUoS2rBAtR77iPZ";
        java.lang.System.out.println("GETTING <" + url + "> for " + t.toString() + "\n");
        try {
            URL temp = new URL(url);
            ret = ImageIO.read(temp);
        } catch(IOException e) {}
        return ret;
    }

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
            if(pause) {
                synchronized(pauseLock) {
                    while(pause) {
                        try {
                            pauseLock.wait();
                        } catch(InterruptedException e){}
                    }
                }
            }
            updateTiles();
            BaseObject.sRegistry.grPanel.repaint();
            pause();
        }
    }

    public class Tile {
        int x;
        int y;
        int z;
        Image img;

        public Tile(int xVal, int yVal, int zVal) {
            x = xVal;
            y = yVal;
            z = zVal;
        }

        @Override
        public String toString() {
            return "<Tile (" + this.x + "," + this.y + "," + this.z + ")>";
        }
    }
}
