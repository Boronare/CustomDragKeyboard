package org.dyndns.fules.ck;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

class DbHelper extends SQLiteOpenHelper {
    private static SQLiteDatabase db;

    private static int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "DICTIONARY.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE JP_DICTIONARY_TABLE ( _ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NAME TEXT, FREQUENCY INTEGER, RESULT TEXT); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(SQL_DELETE_ENTRIES);
        //onCreate(db);
    }

    public void insert(String name, int frq, String res) {
        //SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("NAME", name);
        values.put("FREQUENCY", frq);
        values.put("RESULT", res);

        db.insert("JP_DICTIONARY_TABLE", null, values);
    }

    public ArrayList<String> search(String cond) {
        // SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> ans = new ArrayList<String>();

        String COL_NAME = null;
        String TABLE_NAME = null;
        String COL_INDEX_NAME = null;

        //db.query("JP_DICTIONARY_TABLE","RESULT")

        Cursor cursor = db.rawQuery("SELECT RESULT FROM JP_DICTIONARY_TABLE WHERE NAME LIKE '" + cond + "%'",null);

        cursor.moveToFirst();
        while(cursor.moveToNext()) {
            ans.add(cursor.getString(cursor.getColumnIndex("RESULT")));
        }

        return ans;
    }

    public void fileToDB(FileHelper file) {
        int i;

        FileHelper fp = file;
        ArrayList<ArrayList> answer = fp.getAns();

        ArrayList<String> name = answer.get(0);
        ArrayList<String> frq  = answer.get(1);
        ArrayList<String> res  = answer.get(2);

        for (i = 0; i < name.size(); i++) {
            this.insert(name.get(i), Integer.parseInt(frq.get(i)), res.get(i));
        }
    }
}


