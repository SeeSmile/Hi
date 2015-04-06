package com.example.justplayer;

import java.io.File;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class MainActivity extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//启动线程定时跳转到第二个界面
		new Thread(new MyThread()).start();
		//创建本程序保存歌词的默认文件夹
		File file = new File("sdcard/FukuaPlayer");
		file.mkdir();
		File file2 = new File("sdcard/FukuaPlayer/lrc");
		file2.mkdir();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	
	}

	final Handler handler = new Handler();

	public class MyThread implements Runnable {
 
        public void run() {  
             	handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						Intent intent = new Intent();
						intent.setClass(MainActivity.this, ListMusic.class);
						startActivity(intent);
						//设置动画效果
						MainActivity.this.overridePendingTransition(R.anim.inalpha, R.anim.alpha);
						
					}
				}, 1000);  
         }  
    }  	
}
