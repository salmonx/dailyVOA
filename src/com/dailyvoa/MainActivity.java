package com.dailyvoa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.dailyvoa.domain.VOAObject;
import com.dailyvoa.utils.DataDriver;
import com.dailyvoa.utils.DateTools;
import com.dailyvoa.utils.MediaButtonReceiver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

@SuppressLint("SdCardPath")
public class MainActivity extends Activity implements OnClickListener,
		SeekBar.OnSeekBarChangeListener {

	private ImageButton ib_pre, ib_play, ib_next, ib_list;
	private TextView tv_start, tv_end, tv_title;
	private TextView tv_content;
	private SeekBar seekBar;
	private MediaPlayer mp;
	private int curDuration;
	private int lastListPos;
	private static VOAObject curVOA;
	private int PlayStatus = 0;
	private static int curListPos = 0;
	private final int PLAYING = 1;
	private final int PAUSE = 2;
	private final int TIME = 10;
	private final int READY = 20;
	static List<VOAObject> list = null;
	//初始化Handler对象  
	MyHandler earphoneHandler = new MyHandler();
	//初始化媒体(耳机)广播对象.  
	MediaButtonReceiver mediaButtonReceiver = new MediaButtonReceiver(earphoneHandler);  
		
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case TIME:
				tv_start.setText(timeToStr(curDuration));
				seekBar.setProgress(curDuration);
				break;
			case READY:
				play(false);
				break;
			}
		};
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		ib_pre = (ImageButton) findViewById(R.id.ib_pre);
		ib_play = (ImageButton) findViewById(R.id.ib_play);
		ib_next = (ImageButton) findViewById(R.id.ib_next);
		ib_list = (ImageButton) findViewById(R.id.ib_list);

		ib_pre.setOnClickListener(this);
		ib_play.setOnClickListener(this);
		ib_next.setOnClickListener(this);
		ib_list.setOnClickListener(this);

		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_content = (TextView) findViewById(R.id.tv_content);
		tv_start = (TextView) findViewById(R.id.tv_start);
		tv_end = (TextView) findViewById(R.id.tv_end);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(this);

		mp = new MediaPlayer();
		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				next();
			}
		});
		curDuration = 0;
		new Thread() {
			public void run() {
				while (true)
					try {
						Thread.sleep(1000);
						if (curListPos == lastListPos) {
							if (mp != null && mp.isPlaying()) {
								curDuration += 1;
								Message msg = new Message();
								msg.what = TIME;
								handler.sendMessage(msg);
							}
						} else {
							curDuration = 0;
							lastListPos = curListPos;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			};
		}.start();
		init();
	}

	private void init() {
		// get sharedpreferences: 1.lastdate 2.lastlistpotion
		// get 51voa.com today's list compare date with lastdate,null or not
		// equal -> update
		// update: getlist->
		SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
		String lastdate = sp.getString("lastdate", "");
		Log.i("main", lastdate);
		if (!lastdate.equals(DateTools.getToday())) {
			Intent intent = new Intent(getApplicationContext(),
					DownManagerView.class);
			intent.putExtra("refresh", true);
			startActivityForResult(intent, 1);
		} else {
			curListPos = sp.getInt("curpos", 0);
			list = DataDriver.getToday();
			Log.i("Today's count", list.size() + "");
			if (list.size() > 0) {
				Message msg = Message.obtain();
				msg.what = READY;
				handler.sendMessage(msg);
			}
		}

		//注册媒体(耳机)广播对象  
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);  
		intentFilter.setPriority(1000);
		registerReceiver(mediaButtonReceiver, intentFilter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == 1) {
			list = DataDriver.getToday();
		} else if (requestCode == 11 && resultCode == 0) {
			SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
			Editor editor = sp.edit();

			if (sp.getBoolean("selected", false) == true) {
				editor.putBoolean("selected", false);
				editor.commit();
				curListPos = sp.getInt("curpos", 0);
				curVOA = list.get(curListPos);
				Log.i("main_result", curListPos + "");
				seekBar.setProgress(0);
				play(false);
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ib_pre:
			pre();
			break;
		case R.id.ib_next:
			next();
			break;
		case R.id.ib_play:
			play(true);
			break;
		case R.id.ib_list:

			Intent intent = new Intent(getApplicationContext(),
					DownManagerView.class);
			startActivityForResult(intent, 11);
			mp.pause();
			ib_play.setImageResource(R.drawable.play);
			break;
		case R.id.ll_myLyric:
			play(true);
			break;
		}
	}

	private void play(boolean selfClick) {
		try {
			if (!selfClick) {
				mp.reset();
				Log.i("pos", curListPos + "");
				curVOA = list.get(curListPos);
				String title = curVOA.getTitle();
				String album = curVOA.getAlbum();
				String content = curVOA.getContent();
				String strPath = curVOA.getMp3fileLocation();
				File file = new File(strPath);
				FileInputStream fis = new FileInputStream(file);
				mp.setDataSource(fis.getFD());
				mp.prepare();
				tv_title.setText(title);
				tv_content.setText(Html.fromHtml(curVOA.getContent()));
				int duration = mp.getDuration() / 1000;
				seekBar.setMax(duration);
				tv_end.setText(timeToStr(duration));
				mp.start();
				ib_play.setImageResource(R.drawable.pause);
				/*
				 * String lrcpath = "in-the-news-february-25.lrc";
				 * lyricView.readLrc(lrcpath); lyricView.SetTextSize();
				 * lyricView.setOffsetY(350);
				 */
			} else if (mp.isPlaying()) {
				ib_play.setImageResource(R.drawable.play);
				mp.pause();
			} else {
				ib_play.setImageResource(R.drawable.pause);
				mp.start();
			}

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void next() {
		if (mp != null) {
			mp.stop();
		}
		resetView();
		curListPos += 1;
		if (curListPos == list.size())
			curListPos = 0;
		play(false);
	}

	private void pre() {
		if (mp != null) {
			mp.stop();
		}
		resetView();
		curListPos -= 1;
		if (curListPos < 0)
			curListPos = list.size() - 1;
		play(false);
	}

	private void resetView() {
		curDuration = 0;
		tv_start.setText(timeToStr(curDuration));
		seekBar.setProgress(curDuration);
	}

	private void love() {
		// add to loves list stored in loves.txt
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.action_feedback:

			break;
		case R.id.action_about:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		curDuration = progress;
		Message msg = new Message();
		msg.what = TIME;
		handler.sendMessage(msg);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// play change
		mp.seekTo(curDuration * 1000);
	}

	private String timeToStr(int curDuration) {
		int sec = curDuration % 60;
		String stime = curDuration / 60 + ":" + (sec > 9 ? sec : "0" + sec);
		return stime;
	}

	@Override
	protected void onStop() {
		SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt("curpos", curListPos);
		editor.commit();
		super.onStop();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		Log.i("main_restart", curListPos + "");
		super.onRestart();
	}

	@Override
	protected void onResume() {

		Log.i("main_resume", curListPos + "");
		super.onResume();
	}

	@Override
	protected void onDestroy() {

		SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt("curpos", curListPos);
		editor.commit();
		mp.release();
		mp = null;
		unregisterReceiver(mediaButtonReceiver);  
		super.onDestroy();
	}

	class runable implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(100);
					if (mp.isPlaying()) {
						seekBar.setProgress(mp.getCurrentPosition());
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case 100:// 单击按键广播
				Bundle data = msg.getData();
				// 按键值
				int keyCode = data.getInt("key_code");
				Log.i("earphone",keyCode+"");
				switch (keyCode) {
				case KeyEvent.KEYCODE_HEADSETHOOK:// 播放或暂停
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:// 播放或暂停
					play(false);
					break;

				default:// 其他消息-则扔回上层处理
					super.handleMessage(msg);
				}
			}
		}
	}

}
