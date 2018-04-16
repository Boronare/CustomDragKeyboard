package org.dyndns.fules.ck;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class KeySettingPopupActivity extends Activity {

    EditText ed[] = new EditText[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_key_setting_popup);

        Intent intent = getIntent();
        final KbdModel tempKbdModel = (KbdModel) intent.getSerializableExtra("kbdModel");
        final int row = intent.getIntExtra("row", 0);
        final int col = intent.getIntExtra("col", 0);

        ed[0] = (EditText) findViewById(R.id.EditText1);    // 0 1 2
        ed[1] = (EditText) findViewById(R.id.EditText2);    // 3 4 5
        ed[2] = (EditText) findViewById(R.id.EditText3);    // 6 7 8
        ed[3] = (EditText) findViewById(R.id.EditText4);
        ed[4] = (EditText) findViewById(R.id.EditText5);
        ed[5] = (EditText) findViewById(R.id.EditText6);
        ed[6] = (EditText) findViewById(R.id.EditText7);
        ed[7] = (EditText) findViewById(R.id.EditText8);
        ed[8] = (EditText) findViewById(R.id.EditText9);
        for (int i = 0; i < 9; i++) {
            ed[i].setText(tempKbdModel.row[row].col[col].dir[i].show);
        }

        Button btr = (Button) findViewById(R.id.Button1);
        btr.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str[] = new String[9];

                for (int i = 0; i < 9; i++) {
                    str[i] = ed[i].getText().toString();
                    tempKbdModel.row[row].col[col].dir[i].show = str[i];
                }
                Intent intent1 = new Intent(KeySettingPopupActivity.this, KeySettingActivity.class);
                intent1.putExtra("kbdModel", tempKbdModel);
                setResult(0, intent1);
                finish();
            }
        });


    }
}
