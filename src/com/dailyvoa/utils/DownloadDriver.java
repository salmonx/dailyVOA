package com.dailyvoa.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.dailyvoa.DownManagerView;
import com.dailyvoa.domain.VOAEventCode;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DownloadDriver {
	public static String download(String url, int id, Handler handler)	{
		try {
			URL mp3url = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) mp3url
					.openConnection();
			InputStream is = conn.getInputStream();
			int mp3size = conn.getContentLength();
			String path = Environment.getExternalStorageDirectory().getPath()+"/voafiles"; 
			File file = new File(path);
			if(!file.exists()) 
				file.mkdir(); 
			String mp3file = path+"/"+url.substring(url.lastIndexOf("/") + 1).replace("/", "");
			File filetest = new File(mp3file);
			if (filetest.exists()) {
				Log.i("down",mp3file);
				return mp3file;
			}
			FileOutputStream fos = new FileOutputStream(mp3file, true);
			byte[] buffer = new byte[1024];
			int byteread = 0;
			int total = 0;
			int progress = 0;
			while ((byteread = is.read(buffer, 0, 1024)) != -1) {
				fos.write(buffer, 0, byteread);
				total += byteread;
				if ( total * 100 / mp3size > progress ){
					progress = total * 100 / mp3size;
					Message msg = Message.obtain();
					Bundle data = new Bundle();
					data.putInt("progress", progress);
					data.putInt("id", id);
					msg.setData(data);
					msg.what= VOAEventCode.DOWNLOADING;
					handler.sendMessage(msg);
				}
				
			}
			is.close();
			fos.close();
			return mp3file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getWebPage(String strSITE) {
		try {
			URL url = new URL(strSITE);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setUseCaches(false);
			conn.setConnectTimeout(3000);
			if (conn.getResponseCode() != 200)
				Log.i("DownloadDriver", conn.getResponseCode() + "");
			InputStream is = conn.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line, result = "";
			while ((line = in.readLine()) != null) {
				result += line;
			}
			conn.disconnect();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
