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
	 * 绑定服务
	 */
	private void setService(){
		Intent intent = new Intent();
		intent.setAction("com.example.justplayer.PlayService");
		bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}
	
	/**
	 * 初始化变量
	 */
	private void init() {
		db = new MusicDB(this);
		sp = getSharedPreferences("SPDATA_Files", MODE_PRIVATE);
		//得到shared保存的信息
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
		//设置之前保存的播放顺序图标
		setorder(order);
		//开始显示播放信息
		playlrc = true;
		//连接服务
		conn = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName arg0) {
				
			}
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				binder = (PlayService.MyBinder)arg1;
			}
		};
		
	}
	
	/**
	 *设置监听
	 */
	private void setlistener(){
		iv_start.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				//如果当前正在播放音乐，则暂停。并设置图标
				if(binder.isplay()){
					binder.pause();
					iv_start.setImageResource(R.drawable.play);
				//恢复播放音乐，并设置图标
				}else{
					binder.replay();
					iv_start.setImageResource(R.drawable.pause);
				}
				
			}
		});
		
		/**
		 * 设置播放顺序
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
		 * 播放下一曲
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
		 * 播放上一曲
		 */
		iv_last.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				
				playlast();
			}
		});
		
		/**
		 * 拖动进度条后，改变歌曲进度
		 */
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			/**
			 * 当停止拖动进度条时，获取当前进度条的进度，设置歌曲的播放进度
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
	 * 设置播放顺序图标
	 */
	public void setorder(int order){
		if(order==1){
			//设置列表循环播放图标
			iv_order.setImageResource(R.drawable.xunhuan);
		}else if(order==2){
			//设置单曲循环播放图标
			iv_order.setImageResource(R.drawable.danxun);
		}else{
			//设置随机播放图标
			iv_order.setImageResource(R.drawable.suiji);
		}
			
	}
	
	/**
	 * 使进度条移动
	 */
	public void changeProgressBar(){
		    new Thread(new Runnable(){  
		     public void run(){  
		        while(playlrc){  
		          try {  
		           Thread.sleep(1000);
		           //设置进度条的最大数
		           seekbar.setMax(getsectime(tv_all.getText().toString().trim())); 
		           //根据当前播放时间，设置进度条的进度
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
	 * 将毫秒转成分钟显示
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
	 * 将毫秒时间转成秒时间
	 * @param 毫秒的时间
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
	 * 判断该歌曲是否有文件，有则加载
	 */
	public void setlrc(){
		int postion = 0;
		String songname = db.getfilename(id);
		for(int i=0;i<songname.length();i++){
			if(songname.charAt(i)=='.'){
				postion = i;
			}
		}
		//得到对应歌曲的lrc文件
		String songlrc = songname.substring(0, postion)+".lrc";
		
		File file = new File(LRCPATH,songlrc);
		info = new LrcInfo();
		ser = new LrcParser();
		//如果文件存在，则加载进内存
		if(file.exists()){
			try {
				info = ser.parser(file.getPath());
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}else{
			tv_lrc.setText("无对应歌词");
		}
		
	}
	
	/**
	 * 设置menu菜单
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0,Menu.FIRST,0,"下载歌词");
		menu.add(0,Menu.FIRST+1,0,"删除歌词");
		return true;
	}

	
	/**
	 * menu菜单方法
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		super.onOptionsItemSelected(item);
		switch(item.getItemId()){
		//下载歌词
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
			Toast.makeText(getApplicationContext(), "没有上一曲", Toast.LENGTH_SHORT).show();
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
			Toast.makeText(getApplicationContext(), "没有下一首", Toast.LENGTH_SHORT).show();
		}else{
			id = nextid;
			player.reset();
			tv_name.setText(db.getname(id));
			play(id);
			iv_start.setImageResource(R.drawable.pause);
		}
	}
	/**
	 * 设置歌曲时间以及歌词
	 */
	final Handler handler = new Handler(){          
        @SuppressLint("HandlerLeak")
		public void handleMessage(Message msg){  
             switch (msg.what) {  
             case 1:  
            	 //设置总播放时间
            	 tv_all.setText(settime(binder.getDuration()));
                 int sc = binder.getCurrentPosition();
                 //设置当前播放时间
                 tv_now.setText(settime(sc));  
                 long time = binder.getCurrentPosition()/1000;
         
                 //查询是否存在歌词
                 setlrc();
                 
                 //如果存在歌词，则开始显示歌词
                 if(istime(time)){
                	 //得到当前时间的歌词，并显示
                	 tv_lrc.setText(info.getInfos().get(time));        	 
                 }
                 //显示播放歌曲名
                 tv_name.setText(db.getname(id));
                 //显示歌手
         		 tv_singer.setText(db.getsinger(id));
         		 //设置歌曲播放完后播放的顺序
            }
            super.handleMessage(msg);  
         }  
	}; 

	/**
	 * 设置歌曲时间和歌词显示
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
                   //发送消息
                   handler.sendMessage(message);  
                }catch (Exception e) {  
                }  
           }  
        }  
	}  

	/**
	 * 将分钟时间转化成秒
	 * @param all
	 * @return
	 */
	private int getsectime(String all){
		int postion = 0;
		String min = "";
		String sc = "";
		//获取冒号位置
		for(int i =0;i<all.length();i++){
			if(all.charAt(i)==':'){
				postion = i;
			}
		}
		//获取分钟数
		min = all.substring(0, postion);
		//获取秒
		sc = all.substring(postion+1, all.length());
		//返回秒为单位的时间
		return Integer.valueOf(min)*60+Integer.valueOf(sc);
	}
	
	protected void onStop() {
		//解绑服务
		unbindService(conn);
		//将界面显示信息的进程关闭
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
		.setTitle("提示")
		.setMessage("确认删除歌词文件？")
		.setPositiveButton("确认", new DialogInterface.OnClickListener() {

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
					Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getApplicationContext(), "当前歌曲没有歌词文件", Toast.LENGTH_SHORT).show();
				}
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				
				
			}
		})
		.create().show();
	}
}
