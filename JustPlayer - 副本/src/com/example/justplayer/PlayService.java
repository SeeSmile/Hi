package com.example.justplayer;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import com.example.DB.MusicDB;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class PlayService extends Service {
	private MyBinder binder = new MyBinder();
	public class MyBinder extends Binder{
		private MediaPlayer player = new MediaPlayer();
		/**
		 * 播放歌曲
		 * @param path歌曲所在路径
		 * @param filename歌曲文件名
		 * @throws RuntimeException
		 * @throws SecurityException
		 * @throws IllegalStateException
		 * @throws IOException
		 */
		public void play(String path2,String filename) throws RuntimeException, SecurityException, IllegalStateException, IOException{
			File mfile = new File(path2, filename);
			String playpath = mfile.getAbsolutePath();
			player.reset();
			player.setDataSource(playpath);
			player.prepare();
			player.setOnPreparedListener(new OnPreparedListener() {
				public void onPrepared(MediaPlayer arg0) {
					player.start();
					
				}
			});
		}
		
		/**
		 * 暂停播放
		 */
		public void pause(){
			player.pause();
		}
		
		/**
		 * 恢复播放
		 */
		public void replay(){
			player.start();
		}
		
		/**
		 * 得到当前进度时间
		 * @return
		 */
		public int getCurrentPosition(){
			return player.getCurrentPosition();
		}
		
		/**
		 * 得到歌曲的总时间
		 * @return
		 */
		public int getDuration(){
			return player.getDuration();
		}
		
		/**
		 * 停止歌曲
		 */
		public void stop(){
			player.stop();
			player.release();
		}
		
		/**
		 * 查看歌曲是否在播放
		 * @return
		 */
		public boolean isplay(){
			return player.isPlaying();
		}
		
		/**
		 *设置进度
		 * @param progress
		 */
		public void setprogress(int progress){
			player.seekTo(progress);
		}

		public MediaPlayer getPlayer() {
			return player;
		}

	}
	
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
		super.onDestroy();
	}

	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO Auto-generated method stub
		
		return START_STICKY;
	}
	
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
	
		return super.onUnbind(intent);
	}
	
}
