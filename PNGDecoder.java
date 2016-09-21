package com.bibler.biblerizer;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.swing.JOptionPane;

public class PNGDecoder {
	
	int bitDepth;
	int colorType;
	int compressionModel;
	int filterModel;
	int interlaceModel;
	int alphaR = -1;
	int alphaG = -1;
	int alphaB = -1;
	int scanlineLengthInBytes;
	int pixelLengthInBytes;
	long bytesInflatedChunk;
	
	Inflater inflater = new Inflater();

	boolean processedTrans;

	byte[] chunkTypeBytes;
	byte[] writeBytes;
	int writeByteCount;

	int[] currentLineBytes;
	int[] previousLineBytes;
	int[] pixelBytes;
	byte[] dataBytes;
	byte[] inflatedData;
	final byte[] SIG = new byte[]  {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
	final byte[] IHDR = new byte[] {0x49, 0x48, 0x44, 0x52};
	
	final byte[] IDAT = new byte[] {0x49, 0x44, 0x41, 0x54};
	final byte[] IEND = new byte[] {0x49, 0x45, 0x4E, 0x44, };
	final byte[] tRNS = new byte[] {0x74, 0x52, 0x4E, 0x53};
	final byte[] pLTE = new byte[] {0x70, 0x4C, 0x54, 0x45};
	
	final int IDAT_ID = 0x49444154;
	final int IEND_ID = 0x49454E44;
	final int tRNS_ID = 0x74524E53;
	final int pLTE_ID = 0x704c5445;
	private BufferedInputStream input;
	int lineCount;
	int linesProcessed;
	boolean finished;
	int imageWidth;
	int imageHeight;
	File imageFile;
	File outputFile;
	long bufferSize;
	long totalSize;
	boolean big;
	long bytesWrit;
	public FileChannel fc;
	public RandomAccessFile tmpFile;
	ByteBuffer bb;

	private int bufferPos;
	private boolean writing;

	public PNGDecoder() {

	}


	public File decode() {
		long start = System.currentTimeMillis();
		try {
			input = new BufferedInputStream(new FileInputStream(imageFile));
			if(!checkSig()) {
				return null;
			}
			processHeader(true);
			while(!finished) {
				processChunk(true);
			}
		} catch(IOException e) {e.printStackTrace();}
		if(bb.hasRemaining()) {
			try {
				while(bb.hasRemaining()) {
					fc.write(bb);
				}
			} catch(IOException e) {}
			finally {
				try {
					fc.close();
					tmpFile.close();
				} catch(IOException e) {}
			}
		}
		long elapsed = (System.currentTimeMillis() - start) / 1000;
		return outputFile;
	}
	
	public boolean checkSig() {
		chunkTypeBytes = readBytes(8);
		return compareBytes(chunkTypeBytes, SIG);
	}
	
	public void processHeader(boolean process) {
		skipBytes(4);
		chunkTypeBytes = readBytes(4);
		if(!compareBytes(chunkTypeBytes, IHDR))
			return;
		imageWidth = bytesToInt(readBytes(4));
		imageHeight = bytesToInt(readBytes(4));
		totalSize =  imageWidth * imageHeight * 4;
		writeBytes = new byte[imageWidth * 4];
		try {
			tmpFile = new RandomAccessFile(outputFile, "rw");
			tmpFile.setLength(imageWidth * (imageHeight + 1) * 4);
			fc = tmpFile.getChannel();
			bufferSize = tmpFile.length();
			long mem = (long) (Runtime.getRuntime().freeMemory() * .5f);
			while(bufferSize > mem) {
				bufferSize /= 2;
				if(bufferSize % (imageWidth * 4) != 0) {
					bufferSize -= (bufferSize % (imageWidth * 4));
				}
				big = true;
			}
			bb = ByteBuffer.allocate((int) bufferSize);
			bb.position(0);
			bb.order(ByteOrder.LITTLE_ENDIAN);
		} catch(IOException e) {
			e.printStackTrace();
		}
		bitDepth = bytesToInt(readBytes(1));
		colorType = bytesToInt(readBytes(1));
		compressionModel = bytesToInt(readBytes(1));
		filterModel = bytesToInt(readBytes(1));
		interlaceModel = bytesToInt(readBytes(1));
		determineScanLineLength();
		skipBytes(4);
		bytesInflatedChunk = scanlineLengthInBytes;
		currentLineBytes = new int[scanlineLengthInBytes];
		previousLineBytes = new int[scanlineLengthInBytes];
	}
	
	public void determineScanLineLength() {
		switch(colorType) {
			case 0:
			case 2:
			case 3:
			case 4:
				scanlineLengthInBytes = (imageWidth * 3) + 1;
				pixelLengthInBytes = 3;
				pixelBytes = new int[3];
				break;

			case 6:
				scanlineLengthInBytes = (imageWidth * 4) + 1;
				pixelLengthInBytes = 4;
				pixelBytes = new int[4] ;
				break;
		}
		System.out.println("Pixel bytes: " + pixelLengthInBytes);
		inflatedData = new byte[scanlineLengthInBytes];
	}

	public void setFile(File f) {
		imageFile = f;
		String fileRoot = f.getParent() + "/tempJAVA.rw";
		outputFile = new File(fileRoot);
	}
	
	public void processChunk(boolean process) {
		int length = bytesToInt(readBytes(4));
		chunkTypeBytes = readBytes(4);
		switch(determineChunkType(chunkTypeBytes)) {
			case IDAT_ID:
				if(process) {
					while(length > 0) {
						processData(16384 < length ? 16384 : length);
						length -= 16384;
					}
				} else {
					skipBytes(length);
				}
				skipBytes(4);
				break;
			case tRNS_ID:
				processTrans();
				break;
			case pLTE_ID:
				processPalette(length);
				break;
			case IEND_ID:
				int lines = (int) (bytesInflatedChunk / scanlineLengthInBytes);
				for(int i = 0; i < lines; i++) {
					defilterLine();
				}
				System.out.println("END!");
				finished = true;
				break;
				
			default:
				skipBytes(length);
				skipBytes(4);
		}
	}
	
	public void processTrans() {
		alphaR = bytesToInt(readBytes(2));
		alphaG = bytesToInt(readBytes(2));
		alphaB = bytesToInt(readBytes(2));
		System.out.println("Processed alpha: " + alphaR + " " + alphaG + " " + alphaB);
		processedTrans = true;
		skipBytes(4);
	}
	
	public void processPalette(int length) {
		skipBytes(length);
		skipBytes(4);
	}
	
	public void processData(int length) {
		dataBytes = readBytes(length);
		int bytesInflated = 1;
		inflater.setInput(dataBytes);
		try {
			while(bytesInflated > 0) {
				bytesInflated = inflater.inflate(inflatedData, 0, inflatedData.length);
				for(int i = 0; i < bytesInflated; i++) {
					currentLineBytes[lineCount++] = inflatedData[i];
					if(lineCount == scanlineLengthInBytes) {
						lineCount = 0;
						processLine();
					}
				}
			}
		} catch(DataFormatException e){
			e.printStackTrace();
			finished = true;
		}
	}
	
	public void processLine() {
		defilterLine();
		linesProcessed++;
		BaseObject.sRegistry.loader.loadProgress = linesProcessed / (float) imageHeight;
		if(BaseObject.sRegistry.geoReferencePanel.waitPanel != null) {
			BaseObject.sRegistry.geoReferencePanel.waitPanel.repaint();
		}
	}
	
	public void defilterLine() {
		int filterType = currentLineBytes[0];
		int xId, aId, bId, cId = 0;
		int x, a, b, c = 0;
		int orig;
		int pixelCount = 0;
		int[] pixelBytes = new int[4];
		int red;
		int green;
		int blue;
		int alpha;
		int pixelByteCount = 0;
		
		for(int i = 1; i < scanlineLengthInBytes; i++) {
			xId = i;
			aId = i >= pixelLengthInBytes + 1 ? i - pixelLengthInBytes : -1;
			bId = xId;
			cId = i >= pixelLengthInBytes + 1 ? i - pixelLengthInBytes : -1;
			x = currentLineBytes[xId];
			a = aId >= 0 ? currentLineBytes[aId] : 0;
			b = bId >= 0 ? previousLineBytes[bId] : 0;
			c = cId >= 0 ? previousLineBytes[cId] : 0;
			orig = (defilterPixel(filterType, (int) x & 0xFF, (int) a & 0xFF, (int) b & 0xFF, (int) c & 0xFF));
			currentLineBytes[i] = orig;
			pixelBytes[pixelByteCount++] = orig;
			if(writeByteCount + 4 < writeBytes.length) {
				if(pixelByteCount == pixelLengthInBytes) {
					pixelByteCount = 0;
					red = pixelBytes[0];
					green = pixelBytes[1];
					blue = pixelBytes[2];
					if(pixelLengthInBytes == 4)
						alpha = pixelBytes[3];
					else if(red == alphaR && blue == alphaB && green == alphaG) {
						alpha = 0;
					} else {
						alpha = 0xFF;
					} 
					writeBytes[writeByteCount++] = (byte) (alpha & 0xFF);
					writeBytes[writeByteCount++] = (byte) (red & 0xFF);
					writeBytes[writeByteCount++] = (byte) (green & 0xFF);
					writeBytes[writeByteCount++] = (byte) (blue & 0xFF);
					pixelCount++;
				}
			}
		}
		while(writing) {
			try {
				Thread.sleep(30);
			} catch(InterruptedException e) {}
		}
		bb.put(writeBytes);
		bytesWrit += writeBytes.length;
		if(bytesWrit >= bufferSize) {
			write();
		}
		writeByteCount = 0;
		for(int i = 0; i < currentLineBytes.length; i++) {
			previousLineBytes[i] = currentLineBytes[i];
		}
	}
	
	public int defilterPixel(int type, int x, int a, int b, int c) {
		switch(type) {
		case 0:
			return x % 256;
			
		case 1:
			return (x + a) % 256;
			
		case 2:
			return (x + b) % 256;
			
		case 3:
			return (int) (x + Math.floor((a + b) / 2)) % 256;
			
		case 4:
			 int p =  (a + b - c);
			 int pa = Math.abs(p - a);
			 int pb = Math.abs(p - b);
			 int pc = Math.abs(p - c);
			 int Pr = x;
			 if (pa <= pb && pa <= pc) {
				 Pr = a;
			 }
			 else if (pb <= pc) {
				 Pr = b;
			 }
			 else  {
				 Pr = c;
			 } 
			 return (x + Pr) % 256;
		default:
			System.out.println("Default");
			return 0;
		}
	}
	
	
	
	public boolean compareBytes(byte[] first, byte[] second) {
		if(first.length != second.length)
			return false;
		for(int i = 0; i < first.length; i++) {
			if(first[i] != second[i])
				return false;
		}
		return true;
	}
	
	public byte[] readBytes(int bytesToRead) {
		byte[] returnBytes = new byte[bytesToRead];
		try {
			input.read(returnBytes);
		} catch(IOException e) {}
		return returnBytes;
	}
	
	public void skipBytes(int bytesToSkip) {
		int skipped;
		try {
			skipped = (int) input.skip(bytesToSkip);
			while(bytesToSkip - skipped > 0) {
				bytesToSkip = bytesToSkip - skipped;
				skipped = (int) input.skip(bytesToSkip);
			}
		} catch(IOException e) {}
	}
	
	public int determineChunkType(byte[] array) {
		if(compareBytes(array, IDAT)) {
			return IDAT_ID;
		}
		if(compareBytes(array, IEND)) {
			return IEND_ID;
		}
		if(compareBytes(array, tRNS)) {
			return tRNS_ID;
		}
		if(compareBytes(array, pLTE)) {
			return pLTE_ID;
		}
		return 0;
	}
	
	public int bytesToInt(byte[] bytes) {
		if(bytes.length < 4) {
			byte[] temp = new byte[4];
			int diff = 4 - bytes.length;
			for(int i = 0; i < bytes.length; i++) {
				temp[i + diff] = bytes[i];
			}
			for(int i = 0; i < diff; i++) {
				temp[i] = 0;
			}
			bytes = temp;
		}
		int ret = 0;
		for(int i = 0; i < 4; i++) {
			ret <<= 8;
			ret |= (bytes[i] & 0xFF);
		}
		return ret;
	}

	public void write() {
		System.out.println("Write");
		bytesWrit = 0;
		bufferPos++;
		bytesWrit = bufferPos * bufferSize;
		if(totalSize - bytesWrit < bufferSize) {
			bufferSize = totalSize - bytesWrit;
		}
		bytesWrit = 0;
		bb.position(0);
		try {
			writing = true;
			while(bb.hasRemaining()) {
				try {
					fc.write(bb);
				} catch(ClosedChannelException e) {}
			}
			writing = false;
		} catch(IOException e) {
			e.printStackTrace();
		}
		bb.position(0);
	}
}
