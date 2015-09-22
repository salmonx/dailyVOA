package com.dailyvoa.domain;

import java.io.Serializable;


public class VOAObject implements Serializable{
	private int id;
	private String title;
	private String album;
	private String date;
	private String content;
	private String mp3fileLocation;
	private String lrcfileLocation;
	private String mp3url;
	private String lrcurl;
	private String contentURL;
	//private TreeMap<Integer,LyricObject> lrc_read;
	public VOAObject() {
		id=0;
		title = "";
		album = "";
		date = "";
		content ="";
		
		mp3fileLocation = "";
		lrcfileLocation = "";
		contentURL = "";
	}
	public String getAlbum() {
		return album;
	}
	public String getContent() {
		return content;
	}
	public String getContentURL() {
		return contentURL;
	}
	public String getDate() {
		return date;
	}
	public int getId() {
		return id;
	}
	public String getLrcfileLocation() {
		return lrcfileLocation;
	}
	public String getMp3fileLocation() {
		return mp3fileLocation;
	}
	public String getTitle() {
		return title;
	}
	public String getLrcurl() {
		return lrcurl;
	}
	public String getMp3url() {
		return mp3url;
	}
	public void setAlbum(String album) {
		if (album.length()>0)
			this.album = album;
	}
	public void setContent(String content) {
		if (!content.isEmpty())
			this.content = content;
	}
	public void setContentURL(String contentURL) {
		if (!contentURL.isEmpty())
			this.contentURL = contentURL;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setLrcfileLocation(String lrcfileLocation) {
		if (!lrcfileLocation.isEmpty())
			this.lrcfileLocation = lrcfileLocation;
	}
	public void setMp3fileLocation(String mp3fileLocation) {
		this.mp3fileLocation = mp3fileLocation;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setLrcurl(String lrcurl) {
		this.lrcurl = lrcurl;
	}
	public void setMp3url(String mp3url) {
		this.mp3url = mp3url;
	}
}

