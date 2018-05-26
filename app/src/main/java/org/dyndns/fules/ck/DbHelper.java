package org.dyndns.fules.ck;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

class DbHelper extends SQLiteOpenHelper {

    private Context mContext;
    private SQLiteDatabase mDB;

    private static int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "Dictionary.db";
    private static final String PACKAGE_NAME  = "org.dyndns.fules.ck";
    private static final String DATABASE_PATH = "/data/data/" + PACKAGE_NAME + "/databases/";
    private String searchQuery = "SELECT RESULT FROM EN_DICTIONARY_TABLE WHERE NAME LIKE '";
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;
        mDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean checkDB() {


        return true;
    }



    public static void copyDataBase(Context context) {
        File fp = new File(DATABASE_PATH);
        File outFile = new File(DATABASE_PATH + DATABASE_NAME);
        if (outFile.exists()) {
        }

        if (!fp.exists()) { fp.mkdir(); }

        try {
            AssetManager am = context.getResources().getAssets();
            InputStream is = am.open("Dictionary.db3", AssetManager.ACCESS_BUFFER);
            BufferedInputStream bis = new BufferedInputStream(is);
            FileOutputStream fos = new FileOutputStream(outFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int read = -1;
            int filesize = is.available();

            byte[] buffer = new byte[filesize];

            while ((read = bis.read(buffer, 0, filesize)) != -1) {
                bos.write(buffer, 0, read);
            }

            bos.flush();

            fos.close();
            bos.close();
            is.close();
            bis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ArrayList<String> search(String cond) {
        ArrayList<String> ans = new ArrayList<String>();

        Cursor cursor = mDB.rawQuery("SELECT NAME FROM EN_DICTIONARY_TABLE WHERE NAME LIKE '" + cond + "%' ORDER BY FREQUENCY",null);

        cursor.moveToFirst();
        while(cursor.moveToNext()) {
            ans.add(cursor.getString(cursor.getColumnIndex("NAME")));
        }

        return ans;
    }

    public void updateRecordParameter(String dic, String name) {

        ContentValues value = new ContentValues();







    }
}