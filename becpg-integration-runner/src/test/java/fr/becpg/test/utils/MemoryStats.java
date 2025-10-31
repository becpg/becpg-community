package fr.becpg.test.utils;

public class MemoryStats {
	long startUsed;
	long peakUsed;
	long endUsed;
	long startTotal;
	long endTotal;
	
	public void snapshot() {
		Runtime runtime = Runtime.getRuntime();
		runtime.gc(); // Suggest GC before measurement
		try {
			Thread.sleep(100); // Give GC time to run
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		long used = runtime.totalMemory() - runtime.freeMemory();
		if (used > peakUsed) {
			peakUsed = used;
		}
	}
	
	public void start() {
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		startTotal = runtime.totalMemory();
		startUsed = runtime.totalMemory() - runtime.freeMemory();
		peakUsed = startUsed;
	}
	
	public void end() {
		Runtime runtime = Runtime.getRuntime();
		endTotal = runtime.totalMemory();
		endUsed = runtime.totalMemory() - runtime.freeMemory();
	}
	
	public String format(long bytes) {
		return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
	}
	
	public String report() {
		return String.format(
			"Memory: Start=%s, Peak=%s, End=%s, Delta=%s, Total=%s->%s",
			format(startUsed), format(peakUsed), format(endUsed),
			format(peakUsed - startUsed), format(startTotal), format(endTotal)
		);
	}
}
