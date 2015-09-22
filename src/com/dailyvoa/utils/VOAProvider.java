package com.dailyvoa.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dailyvoa.domain.VOAEventCode;
import com.dailyvoa.domain.VOAObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class VOAProvider {

	private static String strSITE = "http://www.51voa.com/";
	private static List<VOAObject> list;
	private static VOAObject voaitem;

	public static List<VOAObject> getTodayVOAList(Handler handler){
		list = new ArrayList<VOAObject>();
		String content = "";
		content = DownloadDriver.getWebPage(strSITE);
		if (content.isEmpty()){
			Message msg = Message.obtain();
			msg.what= VOAEventCode.NETERROR;
			handler.sendMessage(msg);
			return null;
		}
		Pattern pattern = Pattern.compile("<div id=\"list\">(.*)</div>",
				Pattern.MULTILINE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);
		matcher.find();
		String licontent = matcher.group(1);
		pattern = Pattern.compile("<li>(.*?)</li>", Pattern.MULTILINE
				| Pattern.DOTALL);
		matcher = pattern.matcher(licontent);

		// 暂时先过滤掉下面几种,只保留 有原文,有mp3 的形式的文章
		// 视频:http://www.51voa.com/VOA_Videos/farmers-use-creative-methods-to-grow-crops-61768.html
		int i=0;
		while (matcher.find()) {
			String li = matcher.group(1);
			String strdate = "(" + DateTools.getToday() + ")";
			voaitem = new VOAObject();
			if (li.contains(strdate)) {
				String album = li.substring(li.indexOf("[") + 1).split("]")[0]
						.trim();
				String contentURL = "";
				String title = "";
				pattern = Pattern
						.compile("<a href=\"(.*?)\" target=\"_blank\">(.*?)</a>");
				Matcher itemmatcher = pattern.matcher(li);
				while (itemmatcher.find()) {
					contentURL = strSITE + itemmatcher.group(1).trim();
					title = itemmatcher.group(2).replace(strdate, "").trim();
					// match the mp3,lrc,translate url here
				}
				content = DownloadDriver.getWebPage(contentURL);
				String mp3 = "";
				// get mp3
				Pattern pmp3 = Pattern.compile("<a id=\"mp3\" href=\"(.*?)\"");
				Matcher mmp3 = pmp3.matcher(content);
				if (mmp3.find()) {
					mp3 = mmp3.group(1);
				} else {
					continue;
				}
				String lrc = "";
				// get lyric if exists
				Pattern plrc = Pattern.compile("<a id=\"lrc\" href=\"(.*?)\"");
				Matcher mlrc = plrc.matcher(content);
				if (mlrc.find()) {
					lrc = strSITE + mlrc.group(1);
				}
				// get content
				// remove all the pics
				if (content.contains("<div class=contentImage>")) {
					String img = "<div class=(.*?)</div>";
					content = content.replaceAll(img, "");
				}
				Pattern conp = Pattern.compile(
						"<div id=\"content\">(.*?)</div>", Pattern.DOTALL);
				Matcher conm = conp.matcher(content);
				if (conm.find()) {
					content = conm.group(1);
				}
				voaitem.setId(i++);
				voaitem.setAlbum(album);
				voaitem.setTitle(title);
				voaitem.setContentURL(contentURL);
				voaitem.setContent(content);
				voaitem.setMp3url(mp3);
				list.add(voaitem);
			}
		}
		return list;
	}
}
