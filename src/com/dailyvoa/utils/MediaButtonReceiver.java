package com.dailyvoa.utils;


import android.content.BroadcastReceiver;  
import android.content.Context;  
import android.content.Intent;  
import android.os.Bundle;  
import android.os.Handler;  
import android.os.Message;  
import android.util.Log;  
import android.view.KeyEvent;  
  
public class MediaButtonReceiver extends BroadcastReceiver {  
      
    /** 
     * Handler 
     */  
    private Handler handler;  
      
    /** 
     * ������. 
     * @param handler 
     */  
    public MediaButtonReceiver(Handler handler) {  
        this.handler = handler;  
    }  
  
    @Override  
    public void onReceive(Context context, Intent intent) {  
        boolean isActionMediaButton = Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction());  
        Log.i("receiver",intent.getAction());
        if(!isActionMediaButton) return; 
        KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);  
        if(event==null) return;  
          
        boolean isActionUp = (event.getAction()==KeyEvent.ACTION_UP);  
        if(!isActionUp) return;  
          
        int keyCode = event.getKeyCode();  
        //long eventTime = event.getEventTime()-event.getDownTime();//�������µ��ɿ���ʱ��  
        Message msg = Message.obtain();  
        msg.what = 100;  
        Bundle data = new Bundle();  
        data.putInt("key_code", keyCode);  
        //data.putLong("event_time", eventTime);  
        msg.setData(data);  
        handler.sendMessage(msg);  
          
        //��ֹ�㲥(���ñ�ĳ����յ��˹㲥�����ܸ���)  
        abortBroadcast();  
    }  
}  