package com.example.justplayer;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.DB.Contract;
import com.example.DB.MusicDB;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class SendMassage extends Activity {
	private MusicDB db;
	private EditText emass;
	private Button bsend;
	private Button bno;
	private EditText enumber;
	private ImageView ivfind;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sendmass);
		
		init();
		setlistener();
	}
	
	private void setlistener() {
		bno.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				
				finish();
			}
		});
		
		bsend.setOnClickListener(new OnClickListener() {
			
			
			public void onClick(View arg0) {
				SmsManager sms = SmsManager.getDefault();
				String num = enumber.getText().toString().trim();
				String conent = emass.getText().toString();
				sms.sendTextMessage(num, null, conent, null, null);
				Toast.makeText(getApplicationContext(), "已发送", Toast.LENGTH_LONG).show();
				finish();
			}
		});
		
		ivfind.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				
				Contract con = new Contract();
				final ArrayList<HashMap<String, String>> v = con.getPhoneContracts(SendMassage.this);
				final String[] person = new String[v.size()];
				for(int i=0;i<v.size();i++){
					person[i] = v.get(i).get("name")+":"+v.get(i).get("Number");
				}
				new AlertDialog.Builder(SendMassage.this)
				.setTitle("请选择联系人")
				.setItems(person, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						String ss = person[arg1];
						for(int i = 0;i<ss.length();i++){
							if(ss.charAt(i)==':'){
								ss = ss.substring(1+i, ss.length());
							}
						}
						enumber.setText(ss);
					}
				}).create().show();
				
			}
			
		});
	}

	protected void init(){
		db = new MusicDB(this);
		emass = (EditText)findViewById(R.id.et_mass);
		bsend = (Button)findViewById(R.id.bt_send);
		bno = (Button)findViewById(R.id.bt_no);
		enumber = (EditText)findViewById(R.id.et_number);
		ivfind = (ImageView)findViewById(R.id.iv_find);
		
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();
		String id = bundle.getString("id");
		String mass = db.getsinger(id)+"的《"+db.getname(id)+"》很好听，推荐你听听哦！！";
		emass.setText(mass);
	}
}
