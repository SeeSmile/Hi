package com.example.justplayer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
/**
 * 自定义的对话框
 * @author FuPei
 *
 */
public class MyDialog extends Dialog {
	private Window mwindow;
	/**
	 * 名字
	 */
	private String name;
	private String singer;
	private EditText ename;
	private EditText esinger;
	private final String LRCPATH = "sdcard/FukuaPlayer/lrc";
	private Button b_ok;
	private Button b_back;
	private Context context;
	Handler handler = new Handler();
	private String lrcurl;
	private String jsonurl;
	private String filename;
	public MyDialog(Context context,String name,String singer,String filename) {
		super(context);
		mwindow = this.getWindow();
		
		WindowManager.LayoutParams lp = mwindow.getAttributes();
		lp.gravity = Gravity.CENTER;
		setCanceledOnTouchOutside(false);
		mwindow.setContentView(R.layout.mydialog);
		setTitle("下载歌词");
		this.context = context;
		this.name = name;
		this.singer = singer;
		this.filename = filename;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ename = (EditText)findViewById(R.id.et_dia_name);
		esinger = (EditText)findViewById(R.id.et_dia_singer);
		b_ok = (Button)mwindow.findViewById(R.id.bt_ok);
		b_back = (Button)findViewById(R.id.bt_back);
		ename.setText(name);
		esinger.setText(singer);
		
		b_ok.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				jsonurl = "http://geci.me/api/lyric/"+name+"/"+singer;
				new Thread(new urlThread()).start();
				MyDialog.this.dismiss();
			}
		});
		
		b_back.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				MyDialog.this.dismiss();
			}
		});
	}
	

	public View findViewById(int id) {
		
		return super.findViewById(id);
	}

	private class urlThread implements Runnable {

		public void run() {

			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(jsonurl);
			HttpResponse httpResponse = null;

			try {
				httpResponse = httpClient.execute(httpGet);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					final byte[] data = EntityUtils.toByteArray(httpResponse
							.getEntity());
			
					handler.post(new Runnable() {

						public void run() {
							String json = new String(data);
							JSONObject object;
							JSONArray array;
							try {
								object = new JSONObject(json);
								array=object.getJSONArray("result");
								JSONObject jpath = array.getJSONObject(0);
								lrcurl = jpath.getString("lrc");
								Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show();
								new Thread(new lrcThread()).start();
							} catch (JSONException e) {
								
								e.printStackTrace();
							}
							
						}
					});

				}
			} catch (Exception e) {
				
				Toast.makeText(context, "连接超时", Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	private class lrcThread implements Runnable {

		public void run() {
			final String songname = filename;
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(lrcurl);
			HttpResponse httpResponse = null;

			try {
				httpResponse = httpClient.execute(httpGet);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					final byte[] data = EntityUtils.toByteArray(httpResponse
							.getEntity());
					handler.post(new Runnable() {

						public void run() {
							int postion = 0;
							
							for(int i=0;i<songname.length();i++){
								if(songname.charAt(i)=='.'){
									postion = i;
								}
							}
							String songlrc = songname.substring(0, postion)+".lrc";
							getFile(data, LRCPATH, songlrc);
							Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show();
							
						}
					});

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Toast.makeText(context, "下载超时", Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	public static void getFile(byte[] bfile, String filePath, String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			File dir = new File(filePath);
			//判断目录是否存在
			if (!dir.exists() && dir.isDirectory()) {
				dir.mkdirs();
			}
			file = new File(filePath + "/"+ fileName);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bfile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
}
