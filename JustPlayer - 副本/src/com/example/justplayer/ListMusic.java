package com.example.justplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.DB.MusicDB;
import com.example.music.Mp3ReadId3v2;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ListMusic extends Activity {
	//操作数据库对象
	private MusicDB db;
	private ListView list;
	private ImageView iv_file;
	//歌曲列表所在的路径
	private String path;
	SharedPreferences sp;
	private ImageView iv_music;
	private TextView musicname;
	private TextView musicsinger;
	//与服务进行通讯
	private PlayService.MyBinder binder;
	private ServiceConnection conn;
	//之前关闭播放器时正在播放歌曲的id
	private String saveid;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);
		
		//初始化变量
		init();
		//为组件设置监听
		setlistener();
		//显示列表
		setsonglist(path);
		//显示界面下方的信息
		setplaymode(saveid);
		//绑定服务
		Intent intent = new Intent();
		intent.setAction("com.example.justplayer.PlayService");
		bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}
	
	/**
	 * 初始化变量
	 */
	private void init(){
		db = new MusicDB(this);
		//获取共享数据
		sp = getSharedPreferences("SPDATA_Files", MODE_PRIVATE);
		list = (ListView)findViewById(R.id.lv_songlist);
		iv_file = (ImageView)findViewById(R.id.iv_file);
		path = sp.getString("path", null);
		saveid = sp.getString("id", null);
		iv_music = (ImageView)findViewById(R.id.iv_musiclogo);
		musicname = (TextView)findViewById(R.id.tv_playname);
		musicsinger = (TextView)findViewById(R.id.tv_playsinger);
		//得到通讯对象
		conn = new ServiceConnection() {

			public void onServiceDisconnected(ComponentName arg0) {
				
			}
			
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				binder = (PlayService.MyBinder)arg1;
			}
		};
	}
	
	/**
	 * 设置监听
	 */
	private void setlistener(){

		iv_file.setOnClickListener(new OnClickListener() {
			
			/**
			 * 点击文件夹图标跳转界面
			 */
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(ListMusic.this, ChooseActivity.class);
				startActivity(intent);
				
			}
		});
		
		list.setOnItemClickListener(new OnItemClickListener() {
			
			/**
			 * 点击歌曲
			 */
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				String id = arg2+1+"";
				setplaymode(id);
				try {
					binder.play(path, db.getfilename(id));
					Intent in = new Intent();
					//提交点击的歌曲id
					SharedPreferences.Editor editor = sp.edit();
					editor.putString("id", id);
					editor.commit();
					in.setClass(ListMusic.this, PlayActivity.class);
					startActivity(in);
				} catch (RuntimeException e) {
					
					Toast.makeText(getApplicationContext(), path+"\n"+db.getfilename(id), Toast.LENGTH_LONG).show();
				} catch (IOException e) {
					
					Toast.makeText(getApplicationContext(), "cao2", Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					
					Toast.makeText(getApplicationContext(), "caoc3", Toast.LENGTH_SHORT).show();
				} catch (Throwable e) {
					
					Toast.makeText(getApplicationContext(), "cao4", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		/**
		 * 设置长按事件
		 */
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int songid, long arg3) {
				//列表对话框的列表选项
				String[] doit = new String[]{"短信分享给好友","设为铃声","取消"};
				//显示一个对话框
				new AlertDialog.Builder(ListMusic.this)
				.setTitle("选择操作")
				.setItems(doit, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						switch(arg1){
							//分享给好友
							case 0:
								Intent intent = new Intent();
								//传递歌曲的id进行跳转
								intent.putExtra("id", songid+1+"");
								intent.setClass(ListMusic.this, SendMassage.class);
								startActivity(intent);
								break;
							//设置为铃声
							case 1:
								String name =path + db.getfilename(songid+1+"");
								//调用设置铃声的方法
								setMyRingtone(name);
								Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
								break;
							default:
								break;
						}
						
					}
				}).create().show();
				return false;
			}
		});
		
		/**
		 * 点击界面下方歌曲图片进行界面的跳转
		 */
		iv_music.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(ListMusic.this, PlayActivity.class);
				startActivity(intent);
				
			}
		});
	}
	
	/**
	 * 载入歌曲列表
	 * @param fpath 歌曲所在的文件夹
	 */
	private void setsonglist(String fpath) {
		//刷新数据库表的信息
		db.delete();
		db = new MusicDB(this);
		int len = 0;
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		File file;
		try {
			file = new File(fpath);
			//获得当前路径下的所有文件
			File[] filelist = file.listFiles();	
			for(int i=0; i<filelist.length; i++){
				//选择文件后缀为'.mp3'的文件
				if(filelist[i].getName().endsWith(".mp3")){
					 HashMap<String, Object> map = new HashMap<String, Object>();
					 String singer = new Mp3ReadId3v2(fpath, filelist[i].getName()).getAuthor();
					 String name = new Mp3ReadId3v2(fpath, filelist[i].getName()).getName();
					 map.put("name", name);
					 map.put("filename", filelist[i].getName());
					 map.put("singer", singer);
					 listItem.add(map);
					 len++;
				}
			}
			//为歌曲配置id
			for(int i=0;i<len;i++){
				listItem.get(i).put("id", i+1);
			}
			//将歌曲信息添加进数据库
			for(int i=0;i<len;i++){
				String id = listItem.get(i).get("id").toString();
				String name = listItem.get(i).get("name").toString();
				String singer = listItem.get(i).get("singer").toString();
				String filename = listItem.get(i).get("filename").toString();
				db.add(id, filename,name,singer);
			}
			//将列表填充进页面
			SimpleAdapter adapter = new SimpleAdapter(this,listItem,
		           R.layout.songmode,new String[] {"id","name","singer"},
		           new int[] {R.id.tv_songid,R.id.tv_songname,R.id.tv_songsinger});  
			list.setAdapter(adapter);
		} catch (Exception e) {		
			Toast.makeText(getApplicationContext(), "当前目录无歌曲", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 设置小的播放模板
	 * @param id 歌曲id
	 */
	public void setplaymode(String id ){
		//取得当前id的歌曲信息
		String name;
		try {
			name = db.getfilename(id);
			//把歌曲封装成此类，通过此类方法即可得到歌曲信息
			Mp3ReadId3v2 mp3 = new Mp3ReadId3v2(path, name);
			byte[] data;
			try {
				//得到歌曲图片字节流
				data = mp3.getImg();
				Bitmap bmp;
				try {
					//设置图片
					bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
					iv_music.setImageBitmap(bmp);
					//得到歌曲名字并显示
					musicname.setText(mp3.getName());
					//得到歌手名字并显示
					musicsinger.setText(mp3.getAuthor()); 
				} catch (Exception e) {
					// TODO 如果得到的歌曲图片有问题，则只加载歌曲和歌手名
					musicname.setText(mp3.getName());
					musicsinger.setText(mp3.getAuthor()); 
				}
				
			} catch (Exception e) {
				//设置默认的图片
				iv_music.setImageDrawable(getResources().getDrawable(R.drawable.music));
				musicname.setText(mp3.getName());
				musicsinger.setText(mp3.getAuthor()); 
			}
			
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}
		
	}
	
	/**
	 * 返回此界面时刷新歌曲列表
	 */
	protected void onResume() {
		super.onResume();
		sp = getSharedPreferences("SPDATA_Files", MODE_PRIVATE);
		String fpath = sp.getString("path", null);
		String d = sp.getString("id", null);
		setplaymode(d);
		setsonglist(fpath);
	}

	protected void onStart() {
		sp = getSharedPreferences("SPDATA_Files", MODE_PRIVATE);
		String d = sp.getString("id", null);
		setplaymode(d);
		super.onStart();
	}
	
	protected void onRestart() {
		sp = getSharedPreferences("SPDATA_Files", MODE_PRIVATE);
		String d = sp.getString("id", null);
		setplaymode(d);
		super.onRestart();
	}
	/**
	 * 设置铃声
	 * (此方法摘至网络，不可深究)
	 * @param path 歌曲的路径
	 */
	public void setMyRingtone(String path)
    { 
      File sdfile = new File(path);
      ContentValues values = new ContentValues();
      values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
      values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
      values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");  
      values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
      values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
      values.put(MediaStore.Audio.Media.IS_ALARM, false);
      values.put(MediaStore.Audio.Media.IS_MUSIC, false);
      Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
      Uri newUri = this.getContentResolver().insert(uri, values);
      RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, newUri);
      Toast.makeText( getApplicationContext (),"设置来电铃声成功！", Toast.LENGTH_SHORT ).show();
    }
} 
