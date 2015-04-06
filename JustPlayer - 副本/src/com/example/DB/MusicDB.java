package com.example.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MusicDB extends SQLiteOpenHelper {
	private final String CREATE = "create table music(id text,filename text,name text,singer text)";
	public MusicDB(Context context) {
		super(context, "JustPlayer2.db", null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}

	public void add(String id,String filename,String name,String singer){
		String sql = "insert into music(id,filename,name,singer) values(?,?,?,?)";
		this.getWritableDatabase().execSQL(sql, new String[]{id,filename,name,singer});
	}
	
	public String getfilename (String id){
		String sql = "select * from music where id=?";
		Cursor cursor;
		try {
			cursor = this.getReadableDatabase().rawQuery(sql, new String[]{id});
			cursor.moveToNext();
			return cursor.getString(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String getname(String id){
		String sql = "select * from music where id=?";
		Cursor cursor = this.getReadableDatabase().rawQuery(sql, new String[]{id});
		cursor.moveToNext();
		return cursor.getString(2);
	}
	
	public String getsinger(String id){
		String sql = "select * from music where id=?";
		Cursor cursor = this.getReadableDatabase().rawQuery(sql, new String[]{id});
		cursor.moveToNext();
		return cursor.getString(3);
	}
	
	public void delete(){
		String sql = "DROP TABLE music";
		this.getWritableDatabase().execSQL(sql);
		this.getWritableDatabase().execSQL(CREATE);
	}
	public int getcount(){
		String sql = "select * from music";
		Cursor cursor = this.getReadableDatabase().rawQuery(sql, null);
		int count = 1;
		while(cursor.moveToNext()){
			count++;
		}
		return count;
	}	

}
