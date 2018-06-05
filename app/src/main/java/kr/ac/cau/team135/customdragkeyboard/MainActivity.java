package kr.ac.cau.team135.customdragkeyboard;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import kr.ac.cau.team135.customdragkeyboard.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void btn1_click(View v) {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        startActivityForResult(intent, 0);
    }

    public void btn2_click(View v) {
        InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        imeManager.showInputMethodPicker();
    }

    public void btn3_click(View v) {
        Intent intent = new Intent(this, CompassKeyboardSettings.class);
        startActivity(intent);
    }
}
