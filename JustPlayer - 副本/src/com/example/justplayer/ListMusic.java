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
	//�������ݿ����
	private MusicDB db;
	private ListView list;
	private ImageView iv_file;
	//�����б����ڵ�·��
	private String path;
	SharedPreferences sp;
	private ImageView iv_music;
	private TextView musicname;
	private TextView musicsinger;
	//��������ͨѶ
	private PlayService.MyBinder binder;
	private ServiceConnection conn;
	//֮ǰ�رղ�����ʱ���ڲ��Ÿ�����id
	private String saveid;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);
		
		//��ʼ������
		init();
		//Ϊ������ü���
		setlistener();
		//��ʾ�б�
		setsonglist(path);
		//��ʾ�����·�����Ϣ
		setplaymode(saveid);
		//�󶨷���
		Intent intent = new Intent();
		intent.setAction("com.example.justplayer.PlayService");
		bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}
	
	/**
	 * ��ʼ������
	 */
	private void init(){
		db = new MusicDB(this);
		//��ȡ��������
		sp = getSharedPreferences("SPDATA_Files", MODE_PRIVATE);
		list = (ListView)findViewById(R.id.lv_songlist);
		iv_file = (ImageView)findViewById(R.id.iv_file);
		path = sp.getString("path", null);
		saveid = sp.getString("id", null);
		iv_music = (ImageView)findViewById(R.id.iv_musiclogo);
		musicname = (TextView)findViewById(R.id.tv_playname);
		musicsinger = (TextView)findViewById(R.id.tv_playsinger);
		//�õ�ͨѶ����
		conn = new ServiceConnection() {

			public void onServiceDisconnected(ComponentName arg0) {
				
			}
			
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				binder = (PlayService.MyBinder)arg1;
			}
		};
	}
	
	/**
	 * ���ü���
	 */
	private void setlistener(){

		iv_file.setOnClickListener(new OnClickListener() {
			
			/**
			 * ����ļ���ͼ����ת����
			 */
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(ListMusic.this, ChooseActivity.class);
				startActivity(intent);
				
			}
		});
		
		list.setOnItemClickListener(new OnItemClickListener() {
			
			/**
			 * �������
			 */
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				String id = arg2+1+"";
				setplaymode(id);
				try {
					binder.play(path, db.getfilename(id));
					Intent in = new Intent();
					//�ύ����ĸ���id
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
		 * ���ó����¼�
		 */
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int songid, long arg3) {
				//�б�Ի�����б�ѡ��
				String[] doit = new String[]{"���ŷ��������","��Ϊ����","ȡ��"};
				//��ʾһ���Ի���
				new AlertDialog.Builder(ListMusic.this)
				.setTitle("ѡ�����")
				.setItems(doit, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						switch(arg1){
							//���������
							case 0:
								Intent intent = new Intent();
								//���ݸ�����id������ת
								intent.putExtra("id", songid+1+"");
								intent.setClass(ListMusic.this, SendMassage.class);
								startActivity(intent);
								break;
							//����Ϊ����
							case 1:
								String name =path + db.getfilename(songid+1+"");
								//�������������ķ���
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
		 * ��������·�����ͼƬ���н������ת
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
	 * ��������б�
	 * @param fpath �������ڵ��ļ���
	 */
	private void setsonglist(String fpath) {
		//ˢ�����ݿ�����Ϣ
		db.delete();
		db = new MusicDB(this);
		int len = 0;
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		File file;
		try {
			file = new File(fpath);
			//��õ�ǰ·���µ������ļ�
			File[] filelist = file.listFiles();	
			for(int i=0; i<filelist.length; i++){
				//ѡ���ļ���׺Ϊ'.mp3'���ļ�
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
			//Ϊ��������id
			for(int i=0;i<len;i++){
				listItem.get(i).put("id", i+1);
			}
			//��������Ϣ��ӽ����ݿ�
			for(int i=0;i<len;i++){
				String id = listItem.get(i).get("id").toString();
				String name = listItem.get(i).get("name").toString();
				String singer = listItem.get(i).get("singer").toString();
				String filename = listItem.get(i).get("filename").toString();
				db.add(id, filename,name,singer);
			}
			//���б�����ҳ��
			SimpleAdapter adapter = new SimpleAdapter(this,listItem,
		           R.layout.songmode,new String[] {"id","name","singer"},
		           new int[] {R.id.tv_songid,R.id.tv_songname,R.id.tv_songsinger});  
			list.setAdapter(adapter);
		} catch (Exception e) {		
			Toast.makeText(getApplicationContext(), "��ǰĿ¼�޸���", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * ����С�Ĳ���ģ��
	 * @param id ����id
	 */
	public void setplaymode(String id ){
		//ȡ�õ�ǰid�ĸ�����Ϣ
		String name;
		try {
			name = db.getfilename(id);
			//�Ѹ�����װ�ɴ��࣬ͨ�����෽�����ɵõ�������Ϣ
			Mp3ReadId3v2 mp3 = new Mp3ReadId3v2(path, name);
			byte[] data;
			try {
				//�õ�����ͼƬ�ֽ���
				data = mp3.getImg();
				Bitmap bmp;
				try {
					//����ͼƬ
					bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
					iv_music.setImageBitmap(bmp);
					//�õ��������ֲ���ʾ
					musicname.setText(mp3.getName());
					//�õ��������ֲ���ʾ
					musicsinger.setText(mp3.getAuthor()); 
				} catch (Exception e) {
					// TODO ����õ��ĸ���ͼƬ�����⣬��ֻ���ظ����͸�����
					musicname.setText(mp3.getName());
					musicsinger.setText(mp3.getAuthor()); 
				}
				
			} catch (Exception e) {
				//����Ĭ�ϵ�ͼƬ
				iv_music.setImageDrawable(getResources().getDrawable(R.drawable.music));
				musicname.setText(mp3.getName());
				musicsinger.setText(mp3.getAuthor()); 
			}
			
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}
		
	}
	
	/**
	 * ���ش˽���ʱˢ�¸����б�
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
	 * ��������
	 * (�˷���ժ�����磬�����)
	 * @param path ������·��
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
      Toast.makeText( getApplicationContext (),"�������������ɹ���", Toast.LENGTH_SHORT ).show();
    }
} 
