package kr.ac.cau.team135.customdragkeyboard;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    private static final String PACKAGE_NAME  = "kr.ac.cau.team135.customdragkeyboard";
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
            return;
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

    public ArrayList<String> search(String cond,int lang) {
        Cursor cursor = null;
        ArrayList<String> ans = new ArrayList<String>();

        //String dic = whatLanguage(cond);
        String dic;
        switch(lang){
            case 0:dic="EN"; break;
            case 1:dic="KO"; break;
            case 2:dic="JP"; break;
            case 3:dic="ZH"; break;
            default:dic="ERR";
        }

        if (dic.compareTo("EN") == 0) {
            cursor = mDB.rawQuery("SELECT NAME FROM EN_DICTIONARY_TABLE WHERE NAME LIKE '" + cond + "%' Order by FREQUENCY DESC Limit 10",null);
            boolean status = cursor.moveToFirst();
            while(status) {
                ans.add(cursor.getString(cursor.getColumnIndex("NAME")));
                status=cursor.moveToNext();
            }
        }
        else if (dic.compareTo("KO") == 0) {
            cursor = mDB.rawQuery("SELECT NAME FROM KO_DICTIONARY_TABLE WHERE NAME LIKE '" + cond + "%' Order by FREQUENCY DESC Limit 10",null);
            boolean status = cursor.moveToFirst();
            while(status) {
                ans.add(cursor.getString(cursor.getColumnIndex("NAME")));
                status = cursor.moveToNext();
            }
        }
        else if (dic.compareTo("JP") == 0) {
            cursor = mDB.rawQuery("SELECT NAME FROM JP_DICTIONARY_TABLE WHERE HIRA LIKE '" + cond + "%' Order by FREQUENCY DESC Limit 10",null);
            boolean status=cursor.moveToFirst();
            while(status) {
                ans.add(cursor.getString(cursor.getColumnIndex("RESULT")));
                status=cursor.moveToNext();
            }
        }
        else if (dic.compareTo("ZH") == 0) {
            cursor = mDB.rawQuery("SELECT NAME FROM CH_DICTIONARY_TABLE WHERE PINYIN_NUMBER LIKE '" + cond +"%' Order by frequency desc limit 10",null);
            boolean status = cursor.moveToFirst();
            while(status) {
                ans.add(cursor.getString(cursor.getColumnIndex("NAME")));
                status=cursor.moveToNext();
            }
        }
        else {

        }

        return ans;
    }

    public void updateRecordParameter(String name,int lang) {
        String dic_table = null;

        String dic;
        switch(lang){
            case 0:dic="EN"; break;
            case 1:dic="KO"; break;
            case 2:dic="JP"; break;
            case 3:dic="ZH"; break;
            default : dic="ERR";
        }//String dic = whatLanguage(name);

        if (dic == "EN") { dic_table = "EN_DICTIONARY_TABLE"; }
        else if (dic == "KO") { dic_table = "KO_DICTIONARY_TABLE"; }
        else if (dic == "JP") { dic_table = "JP_DICTIONARY_TABLE"; }
        else if (dic == "ZH") { dic_table = "CH_DICTIONARY_TABLE"; }

        String sql = "UPDATE " + dic_table + " SET FREQUENCY = FREQUENCY + 1 WHERE NAME = '" + name+"'";

        mDB.execSQL(sql);
    }

    public String whatLanguage(String name) {
        String lang = null;

        char last_c = name.charAt(name.length() - 1);
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(last_c);

        if (Character.UnicodeBlock.HANGUL_SYLLABLES.equals( unicodeBlock ) ||
                Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO.equals( unicodeBlock ) ||
                Character.UnicodeBlock.HANGUL_JAMO.equals(unicodeBlock))
        {
            lang = "KO";
        }
        else if (Character.UnicodeBlock.KATAKANA.equals( unicodeBlock ) ||
                Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS.equals( unicodeBlock ) ||
                Character.UnicodeBlock.HIRAGANA.equals( unicodeBlock ))
        {
            lang = "JP";
        }
        else if(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals( unicodeBlock ) ||
                Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals( unicodeBlock ) ||
                Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals( unicodeBlock ) ||
                Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT.equals( unicodeBlock ))
        {
            lang = "ZH";
        }
        else if(Character.UnicodeBlock.BASIC_LATIN.equals( unicodeBlock ) ||
                Character.UnicodeBlock.LATIN_1_SUPPLEMENT.equals( unicodeBlock ) ||
                Character.UnicodeBlock.LATIN_EXTENDED_A.equals( unicodeBlock ))
        {
            lang = "EN";
        }
        else {
            lang = null;
        }

        return lang;
    }
}