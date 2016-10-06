package com.bibler.biblerizer;

public class MemoryTools extends BaseObject {
	
	static long freeMem;
	static long totalMem;
	static long memInUse;
	static float percentMemInUse;
	static Runtime runtime;
	
	final static long MB = 0x100000;
	
	public static void grabRuntime() {
		runtime = Runtime.getRuntime();
	}
	
	public static void figureMemValues() {
		freeMem = runtime.freeMemory();
		totalMem = runtime.totalMemory();
		memInUse = totalMem - freeMem;
		percentMemInUse = (memInUse / (float) totalMem) * 100;
		System.out.println("Mem in Use: " + percentMemInUse + " Free mem in MB: " + (freeMem / MB) + " Total Mem: " + (totalMem / MB));
	}
	
	public static void collect() {
		runtime.gc();
	}

}
