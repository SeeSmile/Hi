package com.example.justplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseActivity extends Activity {
	private Button b;
	private Button b_back;
	private Button b_yes;
	private ListView list;
	SharedPreferences sp;
	private TextView tv_path;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose);
		//初始化变量
		init();
		//加载文件列表信息，默认为SD卡根目录
		setfile("sdcard");
		
		/**
		 * 点击取消后关闭此界面
		 */
		b.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		/**
		 * 点击确定按钮
		 */
		b_yes.setOnClickListener(new OnClickListener() {
			
			//向共享数据提交选中的显示歌曲的路径
			public void onClick(View arg0) {
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("path", tv_path.getText().toString().trim());
				editor.commit();
				finish();
			}
		});
		
		/**
		 * 点击列表
		 */
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> item=(HashMap<String, Object>) arg0.getItemAtPosition(arg2);
				String name = item.get("name").toString().trim();
				String mypath = tv_path.getText().toString().trim()+"/"+name;
				File file = new File(mypath);
				//判断此目录是否还存在目录，如果存在则加载此目录的文件目录列表
				if(isfile(file)){
					tv_path.setText(mypath);
					setfile(mypath);
				}else{
					//不存在目录，则提交文件的路径
					SharedPreferences.Editor editor = sp.edit();
					editor.putString("path", mypath);
					editor.commit();
					finish();	
				}
			}
		});
		
		/**
		 * 返回上一层
		 */
		b_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String path = tv_path.getText().toString().trim();
				if(path.equals("sdcard")){
					Toast.makeText(getApplicationContext(), "当前为根目录", Toast.LENGTH_SHORT).show();
				}else{
					int postion = 0;
					for(int i = 0; i<path.length();i++){
						if(path.charAt(i)=='/'){
							postion = i;
						}
					}
					//把路径的最后一个"/"后包括此"/"的字符去掉，得到此目录的上一层目录
					String npath = path.substring(0,postion);
					tv_path.setText(npath);
					//加载上一层目录
					setfile(npath);
				}
			}
		});
	}
	
	/**
	 * 初始化变量
	 */
	private void init(){
		tv_path = (TextView)findViewById(R.id.tv_path);
		list = (ListView)findViewById(R.id.lv_choose);
		sp = getSharedPreferences("SPDATA_Files", MODE_PRIVATE);
		b = (Button)findViewById(R.id.bt_quxiao);
		b_yes = (Button)findViewById(R.id.bt_chok);
		b_back = (Button)findViewById(R.id.bt_return);
		tv_path.setText("sdcard");
	}
	
	/**
	 * 加载列表数据
	 * @param path 加载列表的路径
	 */
	private void setfile(String path) {
		list = (ListView)findViewById(R.id.lv_choose);
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		File file = new File(path);
		File[] filelist = file.listFiles();	
		for(int i=0; i<filelist.length; i++){
			//判断是不是一个文件夹，是则加载数据
			if(filelist[i].isDirectory()){
				 HashMap<String, Object> map = new HashMap<String, Object>();
				 map.put("name", filelist[i].getName());
				 listItem.add(map);
			}
		}
		SimpleAdapter adapter = new SimpleAdapter(this,listItem,
	            R.layout.cmodel,new String[] {"name"},
	            new int[] {R.id.tv_file});  
		list.setAdapter(adapter);
	}
	
	/**
	 * 判断此文件是否为一个文件夹
	 * @param file 文件
	 * @return 真假
	 */
	private boolean isfile(File file){
		File[] files;
		boolean is = false;
		try {
			files = file.listFiles();	
			for(int i=0;i<files.length;i++){
				if(files[i].isDirectory()){
					is = true;
					i = files.length;
				}
			}
		} catch (Exception e) {
			
			is = false;
		}
		
		return is;
	}
}
