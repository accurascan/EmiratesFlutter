package com.docrecog.scan;


public interface OCRCallback {
	/**
	 * This is callback function to get face detection result.
	 * @param s
	 */
	public void onUpdateProcess(String s);
}
