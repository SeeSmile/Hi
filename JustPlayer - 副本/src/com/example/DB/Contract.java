package com.example.DB;

import java.util.ArrayList;
import java.util.HashMap;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

public class Contract {
	// ȡ����ͨѶ¼
	public ArrayList<HashMap<String, String>> getPhoneContracts(
			Context mContext) {
		ArrayList<HashMap<String, String>> maps = new ArrayList<HashMap<String,String>>();
		ContentResolver resolver = mContext.getContentResolver();
		// ��ȡ�ֻ���ϵ��
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, null, null,
				null, null); // ������ȷ��uri
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				int nameIndex = phoneCursor.getColumnIndex(Phone.DISPLAY_NAME); // ��ȡ��ϵ��name
				String name = phoneCursor.getString(nameIndex);
				String Number = phoneCursor.getString(phoneCursor
						.getColumnIndex(Phone.NUMBER)); // ��ȡ��ϵ��number
				if (TextUtils.isEmpty(Number)) {
					continue;
				}
				// ���������Լ������ݷ�װ��
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
//	// ����������ȡsim���ķ�����sim����uri�����ֿ���content://icc/adn��content://sim/adn
//	// ��һ��������ǵ�һ�֣�
//	public static HashMap<String, ContractInfo> getSimContracts(Context mContext) {
//		// ��ȡSIM���ֻ���,�����ֿ���:content://icc/adn��content://sim/adn
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
//				// ���������Լ������ݷ�װ��
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
