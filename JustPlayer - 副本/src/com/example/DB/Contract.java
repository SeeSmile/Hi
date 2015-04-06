package com.example.DB;

import java.util.ArrayList;
import java.util.HashMap;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

public class Contract {
	// 取本机通讯录
	public ArrayList<HashMap<String, String>> getPhoneContracts(
			Context mContext) {
		ArrayList<HashMap<String, String>> maps = new ArrayList<HashMap<String,String>>();
		ContentResolver resolver = mContext.getContentResolver();
		// 获取手机联系人
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, null, null,
				null, null); // 传入正确的uri
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				int nameIndex = phoneCursor.getColumnIndex(Phone.DISPLAY_NAME); // 获取联系人name
				String name = phoneCursor.getString(nameIndex);
				String Number = phoneCursor.getString(phoneCursor
						.getColumnIndex(Phone.NUMBER)); // 获取联系人number
				if (TextUtils.isEmpty(Number)) {
					continue;
				}
				// 以下是我自己的数据封装。
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("Number",Number);
				map.put("name", name);
				maps.add(map);
			}
			phoneCursor.close();
		}
		return maps;
	}
//
//	// 接下来看获取sim卡的方法，sim卡的uri有两种可能content://icc/adn与content://sim/adn
//	// （一般情况下是第一种）
//	public static HashMap<String, ContractInfo> getSimContracts(Context mContext) {
//		// 读取SIM卡手机号,有两种可能:content://icc/adn与content://sim/adn
//		HashMap<String, ContractInfo> map = new HashMap<String, ContractInfo>();
//
//		ContentResolver resolver = mContext.getContentResolver();
//		Uri uri = Uri.parse("content://icc/adn");
//		Cursor phoneCursor = resolver.query(uri, null, null, null, null);
//		if (phoneCursor != null) {
//			while (phoneCursor.moveToNext()) {
//				String name = phoneCursor.getString(phoneCursor
//						.getColumnIndex("name"));
//				String phoneNumber = phoneCursor.getString(phoneCursor
//						.getColumnIndex("number"));
//				if (TextUtils.isEmpty(phoneNumber)) {
//					continue;
//				}
//				// 以下是我自己的数据封装。
//				ContractInfo contractInfo = new ContractInfo();
//				contractInfo.setName(name);
//				contractInfo.setPhoneNumber(getNumber(phoneNumber));
//				contractInfo.setFrom(SIM);
//				map.put(getNumber(phoneNumber), contractInfo);
//			}
//			phoneCursor.close();
//		}
//		return map;
//	}
//
//	private static Object getNumber(String phoneNumber) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
