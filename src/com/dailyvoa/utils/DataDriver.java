package com.dailyvoa.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

import com.dailyvoa.domain.VOAObject;

public class DataDriver {
	private final static String path = Environment.getExternalStorageDirectory().getPath()
			+"/voafiles/TodayVOAList.txt"; 
	public static void saveToday(List<VOAObject> list) {
		try {
			FileOutputStream fos = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(list);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<VOAObject> getToday() {
		List<VOAObject> list = new ArrayList<VOAObject>();
		try {
			FileInputStream fis = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fis);
			list = (List<VOAObject>) ois.readObject();
			return list;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
