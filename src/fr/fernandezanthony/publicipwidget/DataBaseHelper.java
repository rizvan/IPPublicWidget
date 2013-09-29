package fr.fernandezanthony.publicipwidget;

import java.util.ArrayList;
import java.util.List;

import fr.fernandezanthony.publicipwidget.IP;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {


	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "accountManager";

	// Contacts table name
	private static final String TABLE_ACCOUNT = "ipaddress";

	// Contacts Table Columns names
	private static final String KEY_IP = "ip";

	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_ACCOUNT + "("
				+ KEY_IP + " TEXT"
				+")";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);

		// Create tables again
		onCreate(db);
	}
	
	
	public void deleteAll() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from "+ TABLE_ACCOUNT);
		db.close(); // Closing database connection
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new contact
	public void addAccount(IP account) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_IP, account.getIp()); // Contact Name

		// Inserting Row
		db.insert(TABLE_ACCOUNT, null, values);
		db.close(); // Closing database connection
	}

	public void updateAccount(IP ip) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_IP, ip.getIp().toString());
		Log.w("tag", "NOM "+ip.getIp().toString());
		// Inserting Row
		db.update(TABLE_ACCOUNT, values, KEY_IP+" = '"+ip.getIp().toString()+"'", null);
		db.close(); // Closing database connection
	}

	public boolean deleteTitle(String lastname, String name) 
	{
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(TABLE_ACCOUNT, KEY_IP + "= '"+name+"'", null) > 0;
	}

	
	public List<IP> getAllAccount() {
		List<IP> accountList = new ArrayList<IP>();
		// Select All Query
		String selectQuery = "SELECT * FROM "+TABLE_ACCOUNT;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				IP account = new IP();
				account.setIp(cursor.getString(0));
				//cursor.getString(0)
				// Adding contact to list
				accountList.add(account);
			} while (cursor.moveToNext());
		}
		Log.w("tag", "CURSOR = "+cursor);
		// return contact lists
		return accountList;
	}

}
