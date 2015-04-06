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
		//��ʼ������
		init();
		//�����ļ��б���Ϣ��Ĭ��ΪSD����Ŀ¼
		setfile("sdcard");
		
		/**
		 * ���ȡ����رմ˽���
		 */
		b.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		/**
		 * ���ȷ����ť
		 */
		b_yes.setOnClickListener(new OnClickListener() {
			
			//���������ύѡ�е���ʾ������·��
			public void onClick(View arg0) {
				SharedPreferences.Editor editor = sp.edit();
				editor.putString("path", tv_path.getText().toString().trim());
				editor.commit();
				finish();
			}
		});
		
		/**
		 * ����б�
		 */
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> item=(HashMap<String, Object>) arg0.getItemAtPosition(arg2);
				String name = item.get("name").toString().trim();
				String mypath = tv_path.getText().toString().trim()+"/"+name;
				File file = new File(mypath);
				//�жϴ�Ŀ¼�Ƿ񻹴���Ŀ¼�������������ش�Ŀ¼���ļ�Ŀ¼�б�
				if(isfile(file)){
					tv_path.setText(mypath);
					setfile(mypath);
				}else{
					//������Ŀ¼�����ύ�ļ���·��
					SharedPreferences.Editor editor = sp.edit();
					editor.putString("path", mypath);
					editor.commit();
					finish();	
				}
			}
		});
		
		/**
		 * ������һ��
		 */
		b_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String path = tv_path.getText().toString().trim();
				if(path.equals("sdcard")){
					Toast.makeText(getApplicationContext(), "��ǰΪ��Ŀ¼", Toast.LENGTH_SHORT).show();
				}else{
					int postion = 0;
					for(int i = 0; i<path.length();i++){
						if(path.charAt(i)=='/'){
							postion = i;
						}
					}
					//��·�������һ��"/"�������"/"���ַ�ȥ�����õ���Ŀ¼����һ��Ŀ¼
					String npath = path.substring(0,postion);
					tv_path.setText(npath);
					//������һ��Ŀ¼
					setfile(npath);
				}
			}
		});
	}
	
	/**
	 * ��ʼ������
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
	 * �����б�����
	 * @param path �����б��·��
	 */
	private void setfile(String path) {
		list = (ListView)findViewById(R.id.lv_choose);
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		File file = new File(path);
		File[] filelist = file.listFiles();	
		for(int i=0; i<filelist.length; i++){
			//�ж��ǲ���һ���ļ��У������������
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
	 * �жϴ��ļ��Ƿ�Ϊһ���ļ���
	 * @param file �ļ�
	 * @return ���
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
