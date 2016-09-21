package com.bibler.biblerizer;
import java.awt.Point;


public class MathUtils {
	
	public static float[] zoomLevels = new float[] {
		156412,					// 0
		78271.5170f, 			// 1
		39135.7585f, 			// 2
		19567.8792f, 			// 3
		9783.9396f,				// 4 
		4891.9698f, 			// 5	
		2445.9849f, 			// 6
		1222.9925f, 
		610.984f, 
		305.492f,
		152.746f, 
		76.373f, 
		38.187f, 
		19.093f, 
		9.547f, 
		4.773f,
		2.387f, 
		1.193f,
		0.5972f,
		0.2985f,
		0.1493f,
		0.0746f,
		0.0373f,
		0.0187f};
	
	
	final static float RADIUS = 6378137;
	//Lon, Lat
	
	public static Coordinate convertTo3857(Coordinate input) {
        Coordinate output = new Coordinate();
        output.lon = (float) (RADIUS * Math.PI * input.lon / 180);
        output.lat = (float) (RADIUS * Math.log(Math.tan(Math.PI * (input.lat + 90) / 360)));
        return output;
	}
	
	public static Coordinate convertFrom3857(Coordinate input) {
		Coordinate output = new Coordinate();
		output.lon = (float) (180 * input.lon / (RADIUS * Math.PI));
		output.lat = (float) (360 * Math.atan(Math.exp(input.lat / RADIUS)) / Math.PI - 90);
		return output;
	}
	
	public static int closestZoomLevel(float metersPerPixel) {
		float curMin = Float.MAX_VALUE;
		float min;
		for(int i = 0; i < zoomLevels.length - 1; i++) {
			/*min = zoomLevels[i] - metersPerPixel;
			if(min == 0)
				return i;
			if(min < 0) {
				if(Math.abs(min) < curMin) {
					return i;
				} else {
					return i - 1;
				}
			}
			curMin = min;*/
			if(metersPerPixel <= zoomLevels[i] && metersPerPixel >= zoomLevels[i + 1]) {
				return i;
			}
		}
		return 0;
	}

	public static Coordinate xyFigurer(Coordinate input, int zoom) {
		Coordinate coords = convertFrom3857(input);
		float lon = (float) Math.toRadians(coords.lon);
		float lat = (float) Math.toRadians(coords.lat);
		float x = lon;
		float y = (float) Math.log((Math.tan(lat) + (1 / Math.cos(lat))));
		x = (float) ((1 + (x / Math.PI)) / 2);
		y = (float) ((1 - (y / Math.PI)) / 2); 
		int n = (int) Math.pow(2, zoom);
		x = (float) Math.floor(x *= n);
		y = (float) Math.floor(y *= n);
		return new Coordinate(x,y);
	}
	
	public static float findTileWidthDegrees(int zoom) {
		return (float) (360 / (Math.pow(2, zoom)));
	}
	
	public static Coordinate pixelCoordsInMeters(int x, int y) {
		float metersX = x * BaseObject.sRegistry.metersPerPixel;
		float metersY = y * BaseObject.sRegistry.metersPerPixel;
		return new Coordinate (BaseObject.sRegistry.westInMeters + metersX, BaseObject.sRegistry.northInMeters - metersY);
	}
	
	public static Coordinate LatLongToPixelXY(Coordinate input, int zoom) {
        double x = (input.lon + 180) / 360; 
        double sinLatitude = Math.sin(input.lat * Math.PI / 180);
       // double y = 0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI);
        double y = (1 + sinLatitude);
        y = y / (1 - sinLatitude);
        y = Math.log(y);
        y = y / (4 * Math.PI);
        y = 0.5 - y;

        int mapSize = MapSize(zoom);
        int pixelX = (int) Clip(x * mapSize + 0.5, 0, mapSize - 1);
        int pixelY = (int) Clip(y * mapSize + 0.5, 0, mapSize - 1);
        return new Coordinate(pixelX, pixelY);
    }
	
	public static Coordinate PixelXYToLatLon(Coordinate input, int zoom) {
		double mapSize = MapSize(zoom);
		double pixelXRaw = input.lon / mapSize;
		pixelXRaw = (pixelXRaw * 360D) - 180D;
		pixelXRaw += .01;
		
		double pixelYRaw = input.lat / mapSize;
		pixelYRaw = 0.5 - pixelYRaw;
		pixelYRaw = pixelYRaw * (4 * Math.PI);
		pixelYRaw = Math.pow(Math.E, pixelYRaw);
		
		
		
		System.out.println("Input: " + input.lon + " Output: " + pixelXRaw);
		return new Coordinate((float) pixelXRaw, 0);
		
	}

	public static float distance(Coordinate a, Coordinate b) {
		float x = (b.lon - a.lon) * (b.lon - a.lon);
		float y = (b.lat - a.lat) * (b.lat - a.lat);
		return (float) Math.sqrt(x + y);
	}

	public static float pixelToLat(float y, int zoom) {
		float mapHeight = MapSize(zoom);
		double lat_rad = Math.atan(Math.sinh(Math.PI * (1 - 2 * y / mapHeight)));
		return (float) Math.toDegrees(lat_rad);
	}

	public static float pixelToLon(float x, int zoom) {
		float mapHeight = MapSize(zoom);
		return x / mapHeight * 360.0f - 180.0f;
	}
	
	public static int MapSize(int levelOfDetail) {
        return 256 << levelOfDetail;
    }
	
	private static double Clip(double n, double minValue, double maxValue)
    {
        return Math.min(Math.max(n, minValue), maxValue);
    }
	
	public static Coordinate tileCoords(Coordinate input, int zoom) {
		Coordinate pixelCoords = LatLongToPixelXY(input, zoom);
		int x = (int) Math.floor(pixelCoords.lon / 256);
		int y = (int) Math.floor(pixelCoords.lat / 256);
		return new Coordinate(x, y);
	}
    

}
