package com.bibler.biblerizer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Arrays;

// import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

/**
 * Created by Ryan on 2/11/2015.
 */
public class MapImage extends BaseObject {
    ByteBuffer bb;
    FileChannel fc;
    byte[] buffer;
    int width;
    int height;
    int totalSize;
    int remaining;
    int bufferSize;
    int bufferStart;
    int bufferEnd;
    int strideYInBytes;
    int bufferLineEndPos;
    int bufferLineStartPos;
    File imageFile;
    int divisions;

    public float smallifyProgress;

    public MapImage(File f, int w, int h) {
        width = w;
        height = h;
        totalSize =  width * height * 4;
        remaining = totalSize;
        imageFile = f;
        try {
            FileInputStream stream = new FileInputStream(f);
            fc = stream.getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void prepare() {
        allocate();
    }
    
    public void setStideY(int lines) {
    	strideYInBytes = (lines * width * 4);
    	System.out.println("sy: " + strideYInBytes);
    }

    public void allocate() {
    	System.gc();
        long mem = (long) (Runtime.getRuntime().freeMemory() * .5);
        bufferSize = totalSize;
        while(bufferSize > mem) {
        	bufferSize -= (width * 4);
        }
        buffer = new byte[bufferSize];
        bb = ByteBuffer.wrap(buffer);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.position(0);
        divisions = (int) Math.ceil(totalSize / (float) (bufferSize - strideYInBytes));
    }
    
    public void reset() {
    	bb.clear();
    	allocate();
    	remaining = totalSize;
    	bufferStart = 0;
    	bufferEnd = 0;
    	bufferLineEndPos = 0;
    	try {
			fc.position(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void loadNextBuffer() {
        bb.position(0);
        int read = 0;
        long curPos = bufferEnd - strideYInBytes;
        if(curPos < 0)
        	curPos = 0;
        System.out.println("Cur pos: " + curPos);
        try {
        	read = fc.read(bb, curPos);
        } catch(IOException e) {}
        finally {
        	bufferStart = (int) curPos;
        	bufferEnd = bufferStart + read;
        	bufferLineEndPos = (int) Math.floor(bufferEnd / (float) (width * 4));
        	bufferLineStartPos = (int) Math.floor(bufferStart / (float) (width * 4));
        	remaining = totalSize - bufferEnd;
        	System.out.println("BS: " + bufferStart);
            System.out.println("Remaining: " + remaining);
        	bb.position(0);
        }
    }
    
    public void loadBufferAtLine(int line) {
        bb.position(0);
        int read = 0;
        long curPos = (line * width * 4);
        if(curPos < 0)
        	curPos = 0;
        System.out.println("Cur pos: " + curPos);
        try {
        	read = fc.read(bb, curPos);
        } catch(IOException e) {}
        finally {
        	bufferStart = (int) curPos;
        	bufferEnd = bufferStart + read;
        	bufferLineEndPos = (int) Math.floor(bufferEnd / (float) (width * 4));
        	remaining = totalSize - bufferEnd;
        	System.out.println("BS: " + bufferStart);
            System.out.println("Remaining: " + remaining);
        	bb.position(0);
        }
    }

    public int getPixel(int x, int y) {
    	if(x >= width || y >= height || x < 0 || y < 0)
            return 0 << 24;
        int offset = ((y * width) + x) * 4;
        if (!(offset >= bufferStart && offset <= bufferStart + bufferSize)) {
        	return 0;
        }
        int a = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        try {
        	a = buffer[offset - bufferStart] & 0xFF;
        	r = buffer[(offset + 1) - bufferStart] & 0xFF;
        	g = buffer[(offset + 2) - bufferStart] & 0xFF;
        	b = buffer[(offset + 3) - bufferStart] & 0xFF;
        } catch(ArrayIndexOutOfBoundsException e) {}
        return a << 24 | r << 16 | g << 8 | b;
    }

    public BufferedImage smallify() {
        int newWidth = 1000;
        float s = (newWidth / (float) width);
        int newHeight = (int) (height * s);
        prepare();
        float scaleX = newWidth / (float) width;
        float scaleY = newHeight / (float) height;
        BufferedImage imgScale = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_4BYTE_ABGR);
        do {
        	loadNextBuffer();
            for(int y = 0; y < newHeight; y++) {
            	smallifyProgress = y / (float) newHeight;
                if(sRegistry.geoReferencePanel.waitPanel != null)
                		sRegistry.geoReferencePanel.waitPanel.repaint();
                for(int x = 0; x < newWidth; x ++) {
                	if(imgScale.getRGB(x,y) == 0)
                		imgScale.setRGB(x , y, weightedAvg(x / scaleX, y / scaleY));
                	}
                }
        } while(remaining > 0);      
        bb.position(0);
        return imgScale;
    }

    public int average(float newX, float newY, int strideX, int strideY) {
        int x1 = (int) newX;
        int y1 = (int) newY;
        int a = 0;
        int b = 0;
        int r = 0;
        int g = 0;
        if(strideX < 1) {
            strideX = 1;
        }
        if(strideY < 1) {
            strideY = 1;
        }
        int pixel;
        for(int y = 0; y < strideY; y++) {
            for(int x = 0; x < strideX; x++) {
                pixel = getPixel(x + x1, y + y1);
                a += (pixel >> 24 & 0xFF);
                r += (pixel >> 16 & 0xFF);
                g += (pixel >> 8 & 0xFF);
                b += (pixel & 0xFF);
            }
        }
        a /= (strideX * strideY);
        r /= (strideX * strideY);
        g /= (strideX * strideY);
        b /= (strideX * strideY);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public int weightedAvg(float newX, float newY) {
        int pixX;
        int pixY;
        int subX;
        int subY;
        float oneOverN = (1.0f / 16.0f);
        int pix1;
        int pix2;
        int pix3;
        int pix4;
        int aA, aR, aG, aB, bA, bR, bG, bB, cA, cR, cG, cB, dA, dR, dG, dB, avgA, avgR, avgG, avgB;
        int n = 4;
        int weight;
        int trans = 0 << 24 | 0 << 16 | 0 << 8 | 0;
        pixX = (int) newX;
        pixY = (int) newY;
        subX = (int) ((newX - pixX) * 4);
        subY = (int) ((newY - pixY) * 4);
        pix1 = getPixel(pixX, pixY);
        pix2 = (pixX + 1 < width) ? getPixel(pixX + 1, pixY) : trans;
        pix3 = (pixY + 1 < height) ? getPixel(pixX, pixY + 1): trans;
        pix4 = (pixX + 1 < width && pixY + 1 < height) ? getPixel(pixX + 1, pixY + 1) : trans;
        weight = (n - subX) * (n - subY);
        aA = (pix1 >> 24 & 0xFF) * weight;
        aR = (pix1 >> 16 & 0xFF) * weight;
        aG = (pix1 >> 8 & 0xFF) * weight;
        aB = (pix1 & 0xFF) * weight;
        weight = subX * (n - subY);
        bA = (pix2 >> 24 & 0xFF) * weight;
        bR = (pix2 >> 16 & 0xFF) * weight;
        bG = (pix2 >> 8 & 0xFF) * weight;
        bB = (pix2 & 0xFF) * weight;
        weight = (n - subX) * subY;
        cA = (pix3 >> 24 & 0xFF) * weight;
        cR = (pix3 >> 16 & 0xFF) * weight;
        cG = (pix3 >> 8 & 0xFF) * weight;
        cB = (pix3 & 0xFF) * weight;
        weight = subX * subY;
        dA = (pix4 >> 24 & 0xFF) * weight;
        dR = (pix4 >> 16 & 0xFF) * weight;
        dG = (pix4 >> 8 & 0xFF) * weight;
        dB = (pix4 & 0xFF) * weight;
        avgA = (int) ((aA + bA + cA + dA) * oneOverN);
        avgR = (int) ((aR + bR + cR + dR) * oneOverN);
        avgG = (int) ((aG + bG + cG + dG) * oneOverN);
        avgB = (int) ((aB + bB + cB + dB) * oneOverN);
        return avgA << 24 | avgR << 16 | avgG << 8 | avgB;
    }
    
    public Point findRotateCutoff() {
    	float scaleWidth = width * sRegistry.scaleX;
    	float scaleHeight = height * sRegistry.scaleY;
    	float cX = scaleWidth / 2;
    	float cY = scaleHeight /2;
    	double theta;
    	double angle = sRegistry.angle;
		if(angle > -90 && angle < 0) {
			theta = -angle;
		} else if(angle < -90 && angle > -180) {
			theta = angle;
		} else if(angle < -180 && angle > -270) {
			theta = -angle;
		} else {
			theta = angle;
		} 
		double cos = Math.cos(Math.toRadians(theta));
		double sin = Math.sin(Math.toRadians(theta));
		int newWidth = (int) Math.abs(((scaleWidth * cos) + (scaleHeight * sin)));
		int newHeight = (int) Math.abs(((scaleWidth * sin) + (scaleHeight * cos)));
		cos = Math.cos(Math.toRadians(sRegistry.angle));
		sin = Math.sin(Math.toRadians(sRegistry.angle));
		Point[] corners = new Point[] {
				new Point(0, (int) (bufferLineStartPos * sRegistry.scaleY)), 
				new Point((int) scaleWidth, (int) (bufferLineStartPos * sRegistry.scaleY)),
				new Point((int) scaleWidth, (int) (bufferLineEndPos * sRegistry.scaleY)), 
				new Point(0, (int) (bufferLineEndPos * sRegistry.scaleY))
		};
		float newX;
		float newY;
		float rawX;
		float rawY;
		Point p;
		int maxY = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		for(int i = 0; i < corners.length; i++) {
			p = corners[i];
			rawX = p.x;
			rawY = p.y;
			newX = (float) (cos * (rawX - cX) - sin * (rawY - cY) + cX);
			newY = (float) (sin * (rawX - cX) + cos * (rawY - cY) + cY);
			p.x = (int)  (newX - (scaleWidth - newWidth) / 2);
			p.y = (int)  (newY - (scaleHeight - newHeight) / 2);
			if(p.y > maxY)
				maxY = p.y;
			if(p.y < minY)
				minY = p.y;
		}
		if(maxY > sRegistry.outputHeight)
			maxY = (int) sRegistry.outputHeight;
    	return new Point(maxY, minY);
    }
    
    public void disposeImage() {
    	if(fc != null) {
    		try {
    			fc.close();
    		} catch(IOException e) {}
    	}
    	try {
			Files.delete(imageFile.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
