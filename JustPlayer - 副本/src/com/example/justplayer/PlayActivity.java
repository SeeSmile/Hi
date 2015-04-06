package com.example.justplayer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.DB.MusicDB;
import com.example.lrc.LrcInfo;
import com.example.lrc.LrcParser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayActivity extends Activity {
	MediaPlayer player;
	private PlayService.MyBinder binder;
	private ServiceConnection conn;
	private ImageView iv_start;
	private MusicDB db;
	private String id;
	private TextView tv_name;
	private TextView tv_singer;
	private String path;
	private TextView tv_all;
	private TextView tv_now;
	private TextView tv_lrc;
	private SeekBar seekbar;
	private LrcInfo info;
	SharedPreferences sp;
	private LrcParser ser;
	private final String LRCPATH = "sdcard/FukuaPlayer/lrc";
	private boolean playlrc;
	private int order;
	private ImageView iv_order;
	private ImageView iv_next;
	private ImageView iv_last;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playmusic);
		
		init();
		setlistener();
		setService();
		changeProgressBar();
		new Thread(new MyThread()).start();  
		
	}

	/**
	 * �󶨷���
	 */
	private void setService(){
		Intent intent = new Intent();
		intent.setAction("com.example.justplayer.PlayService");
		bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}
	
	/**
	 * ��ʼ������
	 */
	private void init() {
		db = new MusicDB(this);
		sp = getSharedPreferences("SPDATA_Files", MODE_PRIVATE);
		//�õ�shared�������Ϣ
		id = sp.getString("id", null); 
		path = sp.getString("path", null);
		order = sp.getInt("order", 1);
		tv_name = (TextView)findViewById(R.id.tv_name);
		tv_singer = (TextView)findViewById(R.id.tv_singer);
		tv_lrc = (TextView)findViewById(R.id.tv_playlrc);
		tv_now = (TextView)findViewById(R.id.tv_nowtime);
		tv_all = (TextView)findViewById(R.id.tv_alltime);
		iv_start = (ImageView)findViewById(R.id.im_start);
		seekbar = (SeekBar)findViewById(R.id.sk_play);
		iv_order = (ImageView)findViewById(R.id.iv_order);
		iv_last = (ImageView)findViewById(R.id.im_last);
		iv_next = (ImageView)findViewById(R.id.im_next);
		player = new MediaPlayer();
		//����֮ǰ����Ĳ���˳��ͼ��
		setorder(order);
		//��ʼ��ʾ������Ϣ
		playlrc = true;
		//���ӷ���
		conn = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName arg0) {
				
			}
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				binder = (PlayService.MyBinder)arg1;
			}
		};
		
	}
	
	/**
	 *���ü���
	 */
	private void setlistener(){
		iv_start.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				//�����ǰ���ڲ������֣�����ͣ��������ͼ��
				if(binder.isplay()){
					binder.pause();
					iv_start.setImageResource(R.drawable.play);
				//�ָ��������֣�������ͼ��
				}else{
					binder.replay();
					iv_start.setImageResource(R.drawable.pause);
				}
				
			}
		});
		
		/**
		 * ���ò���˳��
		 */
		iv_order.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				if(order==3){
					iv_order.setImageResource(R.drawable.xunhuan);
					order=1;
				}else if(order == 1){
					order++;
					iv_order.setImageResource(R.drawable.danxun);
				}else{
					order++;
					iv_order.setImageResource(R.drawable.suiji);
				}
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("order", order);
				editor.commit();
				binder.getPlayer().setOnCompletionListener(new OnCompletionListener() {

					public void onCompletion(MediaPlayer arg0) {
						if(order == 1){
							playxun();
						}else if(order == 2){
							playdan();
						}else{
							playrandom();
						}
						
					}
				});
				
			}
		});
		
		/**
		 * ������һ��
		 */
		iv_next.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if(order==3){
					playrandom();
				}
				else if(order==1){
					playxun();
				}else{				
					playdan();
				}
			}
		});
		
		/**
		 * ������һ��
		 */
		iv_last.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				
				playlast();
			}
		});
		
		/**
		 * �϶��������󣬸ı��������
		 */
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			/**
			 * ��ֹͣ�϶�������ʱ����ȡ��ǰ�������Ľ��ȣ����ø����Ĳ��Ž���
			 */
			public void onStopTrackingTouch(SeekBar arg0) {
				binder.setprogress(seekbar.getProgress()*1000);
			}

			public void onStartTrackingTouch(SeekBar arg0) {
				
				
			}
			
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				
				
			}
		});
	}
	
	/**
	 * ���ò���˳��ͼ��
	 */
	public void setorder(int order){
		if(order==1){
			//�����б�ѭ������ͼ��
			iv_order.setImageResource(R.drawable.xunhuan);
		}else if(order==2){
			//���õ���ѭ������ͼ��
			iv_order.setImageResource(R.drawable.danxun);
		}else{
			//�����������ͼ��
			iv_order.setImageResource(R.drawable.suiji);
		}
			
	}
	
	/**
	 * ʹ�������ƶ�
	 */
	public void changeProgressBar(){
		    new Thread(new Runnable(){  
		     public void run(){  
		        while(playlrc){  
		          try {  
		           Thread.sleep(1000);
		           //���ý������������
		           seekbar.setMax(getsectime(tv_all.getText().toString().trim())); 
		           //���ݵ�ǰ����ʱ�䣬���ý������Ľ���
		           seekbar.setProgress(getsectime(tv_now.getText().toString().trim()));
		          } catch (InterruptedException e) {  
		            
		          }   
		        }  
		      }  
		  }).start();  
	}
	
	
	public void play(String id){
		
		String filename;
		try {
			filename = db.getfilename(id);
			File mfile = new File(path, filename);
			String playpath = mfile.getAbsolutePath();
			player.reset();
			try {
				player.setDataSource(playpath);
				player.prepare();
				player.setOnPreparedListener(new OnPreparedListener() {
					public void onPrepared(MediaPlayer arg0) {
						player.start();
						
					}
				});
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
	
	/**
	 * ������ת�ɷ�����ʾ
	 * @param time
	 * @return
	 */
	public boolean istime(long time){
		String yes;
		try {
			yes = info.getInfos().get(time);
			if(yes.length()!=0){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		
	}
	
	/**
	 * ������ʱ��ת����ʱ��
	 * @param �����ʱ��
	 * @return
	 */
	private String settime(int all){
		String time = null;
		int now = all/1000;
		int min = now/60;
		int sc = (now-min*60);
		time = min+":"+sc;
		return time;
	}
	
	/**
	 * �жϸø����Ƿ����ļ����������
	 */
	public void setlrc(){
		int postion = 0;
		String songname = db.getfilename(id);
		for(int i=0;i<songname.length();i++){
			if(songname.charAt(i)=='.'){
				postion = i;
			}
		}
		//�õ���Ӧ������lrc�ļ�
		String songlrc = songname.substring(0, postion)+".lrc";
		
		File file = new File(LRCPATH,songlrc);
		info = new LrcInfo();
		ser = new LrcParser();
		//����ļ����ڣ�����ؽ��ڴ�
		if(file.exists()){
			try {
				info = ser.parser(file.getPath());
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}else{
			tv_lrc.setText("�޶�Ӧ���");
		}
		
	}
	
	/**
	 * ����menu�˵�
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0,Menu.FIRST,0,"���ظ��");
		menu.add(0,Menu.FIRST+1,0,"ɾ�����");
		return true;
	}

	
	/**
	 * menu�˵�����
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		super.onOptionsItemSelected(item);
		switch(item.getItemId()){
		//���ظ��
		case 1 :
			String musicname = db.getname(id);
			String musicsinger = db.getsinger(id);
			MyDialog my = new MyDialog(this,musicname,musicsinger,db.getfilename(id));
			my.show();
			break;

		case 2 :
			deletelrc();
			break;
		}
		return true;
	}
	public void playlast(){
		player = binder.getPlayer();
		player.reset();
		if(id.equals("1")){
			Toast.makeText(getApplicationContext(), "û����һ��", Toast.LENGTH_SHORT).show();
		}else{
			String lastid = String.valueOf(Integer.valueOf(id)-1);
			id = lastid;
			play(id);
			iv_start.setImageResource(R.drawable.pause);
		}
	}
	
	public void playrandom(){
		player = binder.getPlayer();
		player.reset();
		int count = db.getcount();
		Random r = new Random();
		int idd = r.nextInt(count);
		toString();
		id = String.valueOf(idd);
		
		try {
			play(id);
			iv_start.setImageResource(R.drawable.pause);
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	public void playdan(){
		player = binder.getPlayer();
		player.reset();
		play(id);
		iv_start.setImageResource(R.drawable.pause);
		tv_name.setText(db.getname(id));
	}
	
	public void playxun(){
		player = binder.getPlayer();
		player.reset();
		toString();
		String nextid = String.valueOf(Integer.valueOf(id)+1);
		toString();
		String count = String.valueOf(db.getcount());
		if(nextid.equals(count)){		
			id="1";
			tv_name.setText(db.getname(id));
			play("1");
		}else{
			playnext();		
		}
	}
	
	public void playnext(){	
		player = binder.getPlayer();
		toString();
		String nextid = String.valueOf(Integer.valueOf(id)+1);
		toString();
		String count = String.valueOf(db.getcount());
		if(nextid.equals(count)){
			Toast.makeText(getApplicationContext(), "û����һ��", Toast.LENGTH_SHORT).show();
		}else{
			id = nextid;
			player.reset();
			tv_name.setText(db.getname(id));
			play(id);
			iv_start.setImageResource(R.drawable.pause);
		}
	}
	/**
	 * ���ø���ʱ���Լ����
	 */
	final Handler handler = new Handler(){          
        @SuppressLint("HandlerLeak")
		public void handleMessage(Message msg){  
             switch (msg.what) {  
             case 1:  
            	 //�����ܲ���ʱ��
            	 tv_all.setText(settime(binder.getDuration()));
                 int sc = binder.getCurrentPosition();
                 //���õ�ǰ����ʱ��
                 tv_now.setText(settime(sc));  
                 long time = binder.getCurrentPosition()/1000;
         
                 //��ѯ�Ƿ���ڸ��
                 setlrc();
                 
                 //������ڸ�ʣ���ʼ��ʾ���
                 if(istime(time)){
                	 //�õ���ǰʱ��ĸ�ʣ�����ʾ
                	 tv_lrc.setText(info.getInfos().get(time));        	 
                 }
                 //��ʾ���Ÿ�����
                 tv_name.setText(db.getname(id));
                 //��ʾ����
         		 tv_singer.setText(db.getsinger(id));
         		 //���ø���������󲥷ŵ�˳��
            }
            super.handleMessage(msg);  
         }  
	}; 

	/**
	 * ���ø���ʱ��͸����ʾ
	 * @author FuPei
	 *
	 */
	public class MyThread implements Runnable{        
		        
        public void run(){  
            while(playlrc){  
                try{  
                   Thread.sleep(100);     
                   Message message = new Message();  
                   message.what = 1;  
                   //������Ϣ
                   handler.sendMessage(message);  
                }catch (Exception e) {  
                }  
           }  
        }  
	}  

	/**
	 * ������ʱ��ת������
	 * @param all
	 * @return
	 */
	private int getsectime(String all){
		int postion = 0;
		String min = "";
		String sc = "";
		//��ȡð��λ��
		for(int i =0;i<all.length();i++){
			if(all.charAt(i)==':'){
				postion = i;
			}
		}
		//��ȡ������
		min = all.substring(0, postion);
		//��ȡ��
		sc = all.substring(postion+1, all.length());
		//������Ϊ��λ��ʱ��
		return Integer.valueOf(min)*60+Integer.valueOf(sc);
	}
	
	protected void onStop() {
		//������
		unbindService(conn);
		//��������ʾ��Ϣ�Ľ��̹ر�
		playlrc = false;
		
		super.onStop();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			SharedPreferences.Editor editor = sp.edit();
			editor.putString("id", id);
			editor.commit();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void deletelrc(){
		new AlertDialog.Builder(this)
		.setTitle("��ʾ")
		.setMessage("ȷ��ɾ������ļ���")
		.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				String songname = db.getfilename(id);
				int postion = 0;
				
				for(int i=0;i<songname.length();i++){
					if(songname.charAt(i)=='.'){
						postion = i;
					}
				}
				String songlrc = songname.substring(0, postion)+".lrc";
				File file = new File(LRCPATH,songlrc);
				if(file.exists()){
					file.delete();
					Toast.makeText(getApplicationContext(), "ɾ���ɹ�", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getApplicationContext(), "��ǰ����û�и���ļ�", Toast.LENGTH_SHORT).show();
				}
			}
		})
		.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				
				
			}
		})
		.create().show();
	}
}
