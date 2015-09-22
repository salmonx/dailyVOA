package com.dailyvoa;

import java.util.ArrayList;
import java.util.List;

import com.dailyvoa.domain.VOAEventCode;
import com.dailyvoa.domain.VOAObject;
import com.dailyvoa.utils.CleanCache;
import com.dailyvoa.utils.DataDriver;
import com.dailyvoa.utils.DateTools;
import com.dailyvoa.utils.DownloadDriver;
import com.dailyvoa.utils.VOAProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DownManagerView extends Activity {
	MyAdapter adapter = new MyAdapter();
	ListView lv_list;
	LinearLayout ll_pd;
	List<String> progresslist = new ArrayList<String>();
	private List<VOAObject> list = new ArrayList<VOAObject>();
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case VOAEventCode.SHOWLIST:
				Log.i("Today's count", list.size() + "");
				ll_pd.setVisibility(View.INVISIBLE);
				lv_list.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
				break;
			case VOAEventCode.GETLISTDONE:
				Log.i("Today's count", list.size() + "");
				ll_pd.setVisibility(View.INVISIBLE);
				lv_list.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
				progresslist.clear();
				for (int ii = 0; ii < list.size(); ii++) {
					progresslist.add("");
				}
				for (int i = 0; i < list.size(); i++) {
					DownMp3(i);
				}
				break;
			case VOAEventCode.DOWNLOADING:
				Bundle bundle = msg.getData();
				int id = bundle.getInt("id");
				int progress = bundle.getInt("progress");
				Log.i("downloding", id + "" + progress);
				progresslist.set(id, progress + "%");
				adapter.notifyDataSetChanged();
				break;
			case VOAEventCode.DOWNLOADMP3DONE:
				bundle = msg.getData();
				id = bundle.getInt("id");
				progress = bundle.getInt("progress");
				progresslist.set(id, "[√]");
				boolean done = false;
				for (String prog : progresslist) {
					if (!prog.equals("[√]")) {
						done = false;
						break;
					}
					done = true;
				}
				if (done) {
					Log.i("down", "Download complete!");
					adapter.notifyDataSetChanged();
					DataDriver.saveToday(list);
					SharedPreferences sp = getSharedPreferences("config",
							MODE_PRIVATE);
					Editor editor = sp.edit();
					editor.putString("lastdate", DateTools.getToday());
					editor.commit();

					DownManagerView.this.setResult(1);
					DownManagerView.this.finish();
				}
				break;
			case VOAEventCode.NETERROR:
				Toast.makeText(getApplicationContext(), "连接网络失败", 2000);
				break;
			}
		};
	};

	private void DownMp3(final int position) {
		new Thread() {
			public void run() {
				VOAObject obj = new VOAObject();
				obj = list.get(position);
				String mp3fileLocation = DownloadDriver.download(
						obj.getMp3url(), obj.getId(), handler);
				obj.setMp3fileLocation(mp3fileLocation);
				list.set(position, obj);
				Message msg = Message.obtain();
				Bundle data = new Bundle();
				data.putInt("process", 0);
				data.putInt("id", position);
				msg.setData(data);
				msg.what = VOAEventCode.DOWNLOADMP3DONE;
				handler.sendMessage(msg);
			};
		}.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		ll_pd = (LinearLayout) findViewById(R.id.ll_pd);
		list.clear();
		lv_list = (ListView) findViewById(R.id.lv_list);
		lv_list.setAdapter(adapter);
		Intent intent = getIntent();
		if (intent.getBooleanExtra("refresh", false)) {
			// clean cachce
			new Thread() {
				public void run() {
					CleanCache.ClearCache();
				};
			}.start();
			new Thread() {
				public void run() {
					try {
						list.clear();
						Log.i("net","network not open");
						if(!isConn(getApplicationContext())){
							Log.i("net","network not open");
							setNetworkMethod(getApplicationContext());
							
				        }
						list = VOAProvider.getTodayVOAList(handler);
						Message msg = Message.obtain();
						msg.what = VOAEventCode.GETLISTDONE;
						handler.sendMessage(msg);
					} catch (Exception e) {
						// e.printStackTrace();
						Log.i("downmanager", e.getMessage());
					}
				}
			}.start();
		} else { 
			Log.i("Done","download done,review");
			list = DataDriver.getToday();
			Log.i("Today's count", list.size() + "");

			if (list.size() > 0) {
				ll_pd.setVisibility(View.INVISIBLE);
				lv_list.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
			}
		}

		lv_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				
				SharedPreferences sp = getSharedPreferences("config",
						MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putInt("curpos", position);
				editor.putBoolean("selected", true);
				editor.commit();
				Log.i("down",position+"");
				DownManagerView.this.setResult(0);
				DownManagerView.this.finish();
			}
		});
		
	}

	 public static boolean isConn(Context context){
	        boolean bisConnFlag=false;
	        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo network = conManager.getActiveNetworkInfo();
	        if(network!=null){
	            bisConnFlag=conManager.getActiveNetworkInfo().isAvailable();
	        }
	        return bisConnFlag;
	    }
	 /*
	     * 打开设置网络界面
	     * */
	    public static void setNetworkMethod(final Context context){
	        //提示对话框
	        AlertDialog.Builder builder=new Builder(context);
	        builder.setTitle("网络设置提示").setMessage("网络连接不可用,是否进行设置?").setPositiveButton("设置", new DialogInterface.OnClickListener() {
	            
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                // TODO Auto-generated method stub
	                Intent intent=null;
	                //判断手机系统的版本  即API大于10 就是3.0或以上版本 
	                if(android.os.Build.VERSION.SDK_INT>10){
	                    intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
	                }else{
	                    intent = new Intent();
	                    ComponentName component = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
	                    intent.setComponent(component);
	                    intent.setAction("android.intent.action.VIEW");
	                }
	                context.startActivity(intent);
	            }
	        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
	            
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                // TODO Auto-generated method stub
	                dialog.dismiss();
	            }
	        }).show();
	    }
	 
	public class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.i("downview", "");
			View view;
			ViewHolder holder = new ViewHolder();
			if (convertView == null) {
				view = View.inflate(getApplicationContext(),
						R.layout.list_item, null);
				holder.tv_item_id = (TextView) view
						.findViewById(R.id.tv_item_id);
				holder.tv_item_title = (TextView) view
						.findViewById(R.id.tv_item_title);
				holder.tv_item_album = (TextView) view
						.findViewById(R.id.tv_item_album);
				holder.tv_item_more = (TextView) view
						.findViewById(R.id.tv_item_more);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}
			VOAObject obj = new VOAObject();
			obj = list.get(position);
			try {
				holder.tv_item_id.setText(obj.getId() + 1 + "");
				holder.tv_item_album.setText(obj.getAlbum());
				holder.tv_item_title.setText(obj.getTitle());
				holder.tv_item_more.setText(progresslist.get(obj.getId()));
			} catch (Exception e) {
				// e.printStackTrace();
			}
			return view;
		}
	}

	static class ViewHolder {
		TextView tv_item_id;
		TextView tv_item_title;
		TextView tv_item_album;
		TextView tv_item_more;
	}
}
