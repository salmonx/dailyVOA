package com.dailyvoa.utils;

import java.io.File;

import android.os.Environment;

public class CleanCache {
	public static void ClearCache() {
		String path = Environment.getExternalStorageDirectory().getPath()+"/voafiles"; 
		File file = new File(path);
		delDirctory(file);
	}

	public static void delDirctory(File file) {
		if (file.isDirectory()) {
			File[] dels = file.listFiles();
			for (int i = 0; i < dels.length; i++) {
				File f = dels[i];
				if (f.isDirectory()) {
					if (f.getName().equals(DateTools.getToday())
							|| !f.getName().startsWith("20")) {
						continue;
					}
					delDirctory(f);
				}
				f.delete();
			}
		}
		file.delete();
	}
}
