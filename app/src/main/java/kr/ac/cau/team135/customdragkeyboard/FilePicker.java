package kr.ac.cau.team135.customdragkeyboard;
import kr.ac.cau.team135.customdragkeyboard.R;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.*;


public class FilePicker extends Activity implements FilePickerView.ResultListener {
	private static final String TAG = "FilePicker";

	public static final String ACTION_PICK = "kr.ac.cau.team135.customdragkeyboard.filepicker.action.PICK";
    public static final String EXTRA_PATH = "kr.ac.cau.team135.customdragkeyboard.filepicker.extra.path";
	public static final String EXTRA_REGEX = "kr.ac.cau.team135.customdragkeyboard.filepicker.extra.regex";
	public static final String EXTRA_SHOW_HIDDEN = "kr.ac.cau.team135.customdragkeyboard.filepicker.extra.show.hidden";
	public static final String EXTRA_SHOW_FILES = "kr.ac.cau.team135.customdragkeyboard.filepicker.extra.show.files";
	public static final String EXTRA_SHOW_OTHERS = "kr.ac.cau.team135.customdragkeyboard.filepicker.extra.show.others";
	public static final String EXTRA_SHOW_UNREADABLE = "kr.ac.cau.team135.customdragkeyboard.filepicker.extra.show.unreadable";
	public static final String EXTRA_PREFERENCE = "kr.ac.cau.team135.customdragkeyboard.filepicker.extra.preference";
	public static final String EXTRA_PREFERENCE_KEY = "kr.ac.cau.team135.customdragkeyboard.filepicker.extra.preference.key";

    String prefName = null;
    String prefKey = null;
	@TargetApi(23) void getPermission(){
		if(checkSelfPermission(WRITE_EXTERNAL_STORAGE)!=PERMISSION_GRANTED||checkSelfPermission(READ_EXTERNAL_STORAGE)!=PERMISSION_GRANTED){
			requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE},1);
		}
	}
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT>=23) getPermission();
		setContentView(R.layout.filepicker);
		Intent i = getIntent();
        {
            Bundle bundle = i.getExtras();
            if (bundle == null) {
                Log.d(TAG, "No extras");
            }
            else {
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    Log.d(TAG, String.format("Extra: %s %s (%s)", key,  value.toString(), value.getClass().getName()));
                }
            }
        }
		String action = i.getAction();
		if (action.contentEquals(Intent.ACTION_MAIN) || action.contentEquals(ACTION_PICK)) {
			String s;
			int n;

			FilePickerView fp = (FilePickerView)findViewById(R.id.filepicker);
			fp.setResultListener(this);

			s = i.getStringExtra(EXTRA_PATH);
			if (s != null)
				fp.setWorkingDir(s);

			s = i.getStringExtra(EXTRA_REGEX);
			if (s != null)
				fp.setRegex(s);

			n = i.getIntExtra(EXTRA_SHOW_HIDDEN, -1);
			if (n != -1)
				fp.setShowHidden(n != 0);

			n = i.getIntExtra(EXTRA_SHOW_FILES, -1);
			if (n != -1)
				fp.setShowFiles(n != 0);

			n = i.getIntExtra(EXTRA_SHOW_OTHERS, -1);
			if (n != -1)
				fp.setShowOthers(n != 0);

			n = i.getIntExtra(EXTRA_SHOW_UNREADABLE, -1);
			if (n != -1)
				fp.setShowUnreadable(n != 0);

			prefName = i.getStringExtra(EXTRA_PREFERENCE);
			prefKey = i.getStringExtra(EXTRA_PREFERENCE_KEY);
		}
		else {
			Log.e(TAG, "Unsupported action; value='" + action + "'");
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	}

	public void onFileSelected(String path, boolean selected) {
		Log.d(TAG, "Selected file; path='" + path + "', state='" + String.valueOf(selected) + "'");
        if ((prefName != null) && (prefKey != null) && (prefName.length() > 0) && (prefKey.length() > 0)) {
            Log.d(TAG, "Shared pref; name='" + prefName + "', key='" + prefKey + "'");
            SharedPreferences prefs = getSharedPreferences(prefName, 0);
            Editor ed = prefs.edit();
            ed.putString(prefKey, path);
            ed.commit();
        }
		setResult(Activity.RESULT_OK, new Intent().setAction(ACTION_PICK).putExtra(EXTRA_PATH, path));
		finish();
	}
}
