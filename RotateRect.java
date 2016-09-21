package com.bibler.biblerizer;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Ryan on 2/3/2015.
 */
public class RotateRect extends BaseObject {
    
    // Obviously, one would need to assign values to these Vector2Ds.
    Vector2D cornerA = new Vector2D();
    Vector2D cornerB = new Vector2D();
    Vector2D cornerC = new Vector2D();
    Vector2D cornerD = new Vector2D();
    Vector2D rotateTab;
    Vector2D[] corners;
    Vector2D center;
    Coordinate centerInDegrees;
    float bbX;
    float bbY;
    float bbWidth;
    float bbHeight;
    float rotateWidth;
    float rotateHeight;
    float baseImageWidth;
    float baseImageHeight;
    float scaleX;
    float scaleY;
    double rotation = 0;
    private boolean figuringExtent;

    float north;
    float south;
    float east;
    float west;
    
    public RotateRect(int x, int y, int width, int height, float bWidth, float bHeight) {
        cornerA.x = x;
        cornerA.y = y;
        cornerB.x = x + width;
        cornerB.y = y;
        cornerC.x = x + width;
        cornerC.y = y + height;
        cornerD.x = x;
        cornerD.y = y + height;
        corners = new Vector2D[] {cornerA, cornerB, cornerC, cornerD};
        center = new Vector2D(x + (width / 2), y + (height / 2));
        bbX = x;
        bbY = y;
        bbWidth = width;
        bbHeight = height;
        baseImageWidth = bWidth;
        baseImageHeight = bHeight;
        scaleX = width / baseImageWidth;
        scaleY = height / baseImageHeight;
        updateContainingBox();
    }
    
    public RotateRect clone() {
    	return new RotateRect((int) cornerA.x, (int) cornerA.y, (int) bbWidth, (int) bbHeight, baseImageWidth, baseImageHeight);
    }

    public void rotate(double angle, boolean fromInfo) {

        if(!fromInfo) {
            rotation += angle;
            sRegistry.infoPanel.setRotation(Math.toDegrees(rotation) % 360);

        } else {
            double temp = angle;
            angle = angle - rotation;
            rotation = temp;
        }
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        for(int i = 0; i < corners.length; i++) {
            float newX = (float) (center.x + (((corners[i].x - center.x) * cos) - ((corners[i].y - center.y) * sin)));
            float newY = (float) (center.y + (((corners[i].x - center.x) * sin) + ((corners[i].y - center.y) * cos)));
            corners[i].x = newX;
            corners[i].y = newY;
        }
        updateContainingBox();
        sRegistry.grPanel.repaint();
    }

    public void moveAllCorners(float x, float y) {
        Vector2D p;
        for(int i = 0; i < corners.length; i++) {
            p = corners[i];
            p.x += x;
            p.y += y;
        }
        updateContainingBox();
    }

    public void moveToCoords(float n, float s, float e, float w) {
        north = n;
        south = s;
        east = e;
        west = w;
        Coordinate newPixelNW = MathUtils.LatLongToPixelXY(new Coordinate(w, n), sRegistry.map.curZoom);
        Coordinate newPixelSE = MathUtils.LatLongToPixelXY(new Coordinate(e, s), sRegistry.map.curZoom);
        double oldRotation = rotation;
        rotate(-rotation, false);
        Coordinate mapNW = sRegistry.map.nwPixels;
        cornerA.x = newPixelNW.lon - mapNW.lon;
        cornerA.y = newPixelNW.lat - mapNW.lat;
        cornerB.x = newPixelSE.lon - mapNW.lon;
        cornerB.y = newPixelNW.lat - mapNW.lat;
        cornerC.x = newPixelSE.lon - mapNW.lon;
        cornerC.y = newPixelSE.lat - mapNW.lat;
        cornerD.x = newPixelNW.lon - mapNW.lon;
        cornerD.y = newPixelSE.lat - mapNW.lat;
        center = midPoint(cornerA, cornerC);
        Vector2D delta = sRegistry.map.goToPoint(center);
        moveAllCorners(-delta.x, -delta.y);
        center.x -= delta.x;
        center.y -= delta.y;
        rotate(oldRotation, false);
        centerInDegrees.lon = MathUtils.pixelToLon(center.x + sRegistry.map.nwPixels.lon, sRegistry.map.curZoom);
        centerInDegrees.lat = MathUtils.pixelToLat(center.y + sRegistry.map.nwPixels.lat, sRegistry.map.curZoom);
        sRegistry.grPanel.repaint();
    }

    public void moveAVector2D(int id, Vector2D newVector2D) {
        if(id < 0)
            return;
        Vector2D oldVector2D = corners[id];
        Vector2D Vector2DPrevious = corners[(id + 3) % 4];
        Vector2D Vector2DNext = corners[(id + 1) % 4];
        Vector2D Vector2DOpposite = corners[(id + 2) % 4];
        Vector2D delta = newVector2D.subtract(oldVector2D);
        Vector2D sidePrevious = Vector2DPrevious.subtract(oldVector2D);
        Vector2D sideNext = Vector2DNext.subtract(oldVector2D);
        Vector2D previousProjection;
        Vector2D nextProjection;

        if (sideNext.x == 0 && sideNext.y == 0) {
            if (sidePrevious.x == 0 && sidePrevious.y == 0) {
                return;
            }
            sideNext = new Vector2D(-sidePrevious.y, sidePrevious.x);
        }
        else {
            sidePrevious = new Vector2D(-sideNext.y, sideNext.x);
        }

        previousProjection = Projection(delta, sidePrevious);
        nextProjection = Projection(delta, sideNext);

        Vector2DNext.addSave(previousProjection);
        Vector2DPrevious.addSave(nextProjection);
        oldVector2D.x = newVector2D.x;
        oldVector2D.y = newVector2D.y;
        center.x = (cornerA.x + cornerC.x) / 2;
        center.y = (cornerA.y + cornerC.y) / 2;
        updateContainingBox();
    }

    public void scaleUp() {
        moveToCoords(north, south, east, west);
        /*
        Vector2D p;
        float diffX;
        float diffY;
        for(int i = 0; i < corners.length; i++) {
            p = corners[i];
            diffX = p.x - center.x;
            diffY = p.y - center.y;
            p.x = center.x + (diffX * 2);
            p.y = center.y + (diffY * 2);
        }
        Coordinate newCenterInPixels = MathUtils.LatLongToPixelXY(centerInDegrees, BaseObject.sRegistry.map.curZoom);
        newCenterInPixels.lon -= BaseObject.sRegistry.map.nwPixels.lon;
        newCenterInPixels.lat -= BaseObject.sRegistry.map.nwPixels.lat;
        moveAllCorners(newCenterInPixels.lon - center.x, newCenterInPixels.lat - center.y);
        center.x = newCenterInPixels.lon;
        center.y = newCenterInPixels.lat;
        updateContainingBox();
        */
    }

    public void scaleDown() {
        moveToCoords(north, south, east, west);
        /*
        Vector2D p;
        float diffX;
        float diffY;
        for(int i = 0; i < corners.length; i++) {
            p = corners[i];
            diffX = p.x - center.x;
            diffY = p.y - center.y;
            p.x = center.x + (diffX / 2);
            p.y = center.y + (diffY / 2);
        }
        Coordinate newCenterInPixels = MathUtils.LatLongToPixelXY(centerInDegrees, BaseObject.sRegistry.map.curZoom);
        newCenterInPixels.lon -= BaseObject.sRegistry.map.nwPixels.lon;
        newCenterInPixels.lat -= BaseObject.sRegistry.map.nwPixels.lat;
        moveAllCorners(newCenterInPixels.lon - center.x, newCenterInPixels.lat - center.y);
        center.x = newCenterInPixels.lon;
        center.y = newCenterInPixels.lat;
        updateContainingBox();
        */
    }


    public Vector2D midPoint(Vector2D p1, Vector2D p2) {
        return new Vector2D((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    public void rotateFromDrag(Vector2D newPoint) {
        float rtDX = center.x - rotateTab.x;
        float rtDY = center.y - rotateTab.y;
        float npDX = center.x - newPoint.x;
        float npDY = center.y - newPoint.y;
        double aRT = Math.atan2(rtDY, rtDX);
        double aNP = Math.atan2(npDY, npDX);
        rotate(aNP - aRT, false);
    }

    public float distance(Vector2D p1, Vector2D p2) {
        return (float) Math.sqrt(Math.pow(p2.x - p1.x, 2) + (Math.pow(p2.y - p1.y, 2)));
    }

    public void updateContainingBox() {
        float maxX = Integer.MIN_VALUE;
        float maxY = Integer.MIN_VALUE;
        float  minX = Integer.MAX_VALUE;
        float minY = Integer.MAX_VALUE;
        Vector2D p;
        for(int i = 0; i < corners.length; i++) {
            p = corners[i];
            if(p.x < minX) {
                minX = p.x;
            }
            if(p.x > maxX) {
                maxX = p.x;
            }
            if(p.y < minY) {
                minY = p.y;
            }
            if(p.y > maxY) {
                maxY = p.y;
            }
        }
        bbX = minX;
        bbY = minY;
        bbWidth = maxX - minX;
        bbHeight = maxY - minY;
        rotateWidth = distance(cornerA, cornerB);
        rotateHeight =distance(cornerA, cornerD);
        scaleX = rotateWidth / baseImageWidth;
        scaleY = rotateHeight / baseImageHeight;
        rotateTab = midPoint(center, midPoint(cornerB, cornerC));
        if(!figuringExtent)
            figureExtent();
    }

    public void figureExtent() {
        figuringExtent = true;
        double oldRotation = rotation;
        rotate(-rotation, false);
        west = MathUtils.pixelToLon(cornerA.x  + sRegistry.map.nwPixels.lon, sRegistry.map.curZoom);
        north = MathUtils.pixelToLat(cornerA.y + sRegistry.map.nwPixels.lat, sRegistry.map.curZoom);
        east = MathUtils.pixelToLon(cornerB.x  + sRegistry.map.nwPixels.lon, sRegistry.map.curZoom);
        south = MathUtils.pixelToLat(cornerD.y + sRegistry.map.nwPixels.lat, sRegistry.map.curZoom);
        sRegistry.infoPanel.updateCoords(north, south, east, west);
        rotate(oldRotation, false);
        sRegistry.grPanel.repaint();
        figuringExtent = false;
    }

    public Vector2D Projection(Vector2D vectorA, Vector2D vectorB) {
        Vector2D vectorBUnit = new Vector2D(vectorB.x, vectorB.y);
        vectorBUnit = vectorBUnit.normalize();
        float dotProduct = vectorA.x * vectorBUnit.x + vectorA.y * vectorBUnit.y;
        return new Vector2D(vectorBUnit.x * dotProduct, vectorBUnit.y * dotProduct);
    }
}

