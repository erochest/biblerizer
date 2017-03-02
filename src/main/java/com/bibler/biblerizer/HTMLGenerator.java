package com.bibler.biblerizer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class HTMLGenerator extends BaseObject {
	
	public static URI createTestSite() {
		URI ret= null;
		File f = new File(sRegistry.fileRoot + "/testSite.html");
		ret = f.toURI();
		writeHTMLToFile(f);
		return ret;
	}
	
	public static void writeHTMLToFile(File location) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(location);
			writer.write(generateString());
		}catch(IOException e) {}
		finally {
			if(writer!= null) {
				try {
					writer.close();
				} catch(IOException e) {}
			}
		}
	}
	
	public static String generateString() {
		String s = "<html lang=\"en\"> " +
						"<head>" +
							"<link rel=\"stylesheet\" href=\"http://openlayers.org/en/v3.0.0/css/ol.css\" type=\"text/css\"> " +
							"<style> " +
							".map { " +
								"height: 470px; " +
								"width: 670px; " +
							"} " +
							"</style> " +
         					"<script src=\"http://openlayers.org/en/v3.0.0/build/ol.js\" type=\"text/javascript\"></script> " +
         					"<title>OpenLayers 3 example</title> " +
         				"</head> " +
         				"<body> " +
         					"<div id=\"map\" class=\"map\"></div> " +
         					"<script> " +
         						"var map; " +
          						"var view; " +
         						"displayMap(); " +
         						"function displayMap() { " +
         							"map = new ol.Map({ " +
         								"target: \'map\', " +
         								"layers: [ " +
         								         "new ol.layer.Tile({ " +
         								        	 "source: new ol.source.BindMaps({key: \"AozBteKhpdAaLf20ql4_MDxo4BZ1Y3WWnT_ckiSiL2oVhPcCwkUoS2rBAtR77iPZ\", imagerySet: \'Aerial\'}), " +
         								             "extent: ol.proj.transformExtent([-180, -90, 180, 90], \'EPSG:4326\', \'EPSG:3857\') " +
         								        	 "}), " +
                        
         								         "new ol.layer.Tile({ " +
         								        	 "source: new ol.source.XYZ({ " +
         								        		 "url: \'{z}/{x}/{y}.png\' " +
         								        	 "}), " +
         								        	 "visible: true " +
         								         "}) " +
         								 "], " +
         							"view: new ol.View({ " +
         								"center: [" + (sRegistry.westInMeters + (sRegistry.widthInMeters / 2)) + "," + (sRegistry.northInMeters - sRegistry.heightInMeters / 2) + "], " +
         										"zoom: " + (sRegistry.baseZoom - 3) +
         							"}) " +
         								         "}); " +
         								"view = map.getView(); " +
         								"map.render(); " +
         							"} " +
         							"</script> " +
         							"</body> " +
         							"</html> ";
		return s;
	}
	
	public static String convertFileRoot() {
		String root = sRegistry.fileRoot;
		return root.replace("\\", "/");
	}

}
