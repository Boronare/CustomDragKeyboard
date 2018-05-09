package org.dyndns.fules.ck;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class KeySettingPopupActivity extends Activity {

    TextView ed[] = new TextView[9];
    int actTypeArr[] = new int[9];
    String sValueArr[] = new String[9];
    int iValueArr[] = new int[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_key_setting_popup);

        Intent intent = getIntent();
        final KbdModel tempKbdModel = (KbdModel) intent.getSerializableExtra("kbdModel");
        final int row = intent.getIntExtra("row", 0);
        final int col = intent.getIntExtra("col", 0);

        ed[0] =  findViewById(R.id.TextView1);    // 0 1 2
        ed[1] =  findViewById(R.id.TextView2);    // 3 4 5
        ed[2] =  findViewById(R.id.TextView3);    // 6 7 8
        ed[3] =  findViewById(R.id.TextView4);
        ed[4] =  findViewById(R.id.TextView5);
        ed[5] =  findViewById(R.id.TextView6);
        ed[6] =  findViewById(R.id.TextView7);
        ed[7] =  findViewById(R.id.TextView8);
        ed[8] =  findViewById(R.id.TextView9);
        for (int i = 0; i < 9; i++) {
            ed[i].setText(tempKbdModel.row[row].col[col].dir[i].show);
            actTypeArr[i] = tempKbdModel.row[row].col[col].dir[i].actType;
            sValueArr[i] = tempKbdModel.row[row].col[col].dir[i].sValue;
            iValueArr[i] = tempKbdModel.row[row].col[col].dir[i].iValue;
            final int finalI = i;
            ed[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(KeySettingPopupActivity.this, KeySettingFunctionActivity.class);
                    //intent.putExtra("kbdModel", tempKbdModel);
                    intent.putExtra("row",finalI);
                    intent.putExtra("show", ed[finalI].getText());
                    intent.putExtra("actType",actTypeArr[finalI]);
                    intent.putExtra("sValue",sValueArr[finalI]);
                    intent.putExtra("iValue",iValueArr[finalI]);
                    startActivityForResult(intent, 0);
                }
            });
        }

        Button btr = (Button) findViewById(R.id.Button1);
        btr.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str[] = new String[9];

                for (int i = 0; i < 9; i++) {
                    str[i] = ed[i].getText().toString();
                    tempKbdModel.row[row].col[col].dir[i].show = str[i];
                    tempKbdModel.row[row].col[col].dir[i].actType = actTypeArr[i];
                    tempKbdModel.row[row].col[col].dir[i].sValue = sValueArr[i];
                    tempKbdModel.row[row].col[col].dir[i].iValue = iValueArr[i];
                }
                /*Log.d("쇼값",  tempKbdModel.row[row].col[col].dir[4].show);
                Log.d("액트타입값",  "" + tempKbdModel.row[row].col[col].dir[4].actType);
                Log.d("에스밸류값",  tempKbdModel.row[row].col[col].dir[4].sValue);
                Log.d("아이벨류값",  "" + tempKbdModel.row[row].col[col].dir[4].iValue);*/
                Intent intent1 = new Intent(KeySettingPopupActivity.this, KeySettingActivity.class);
                intent1.putExtra("kbdModel", tempKbdModel);
                intent1.putExtra("row",row);
                intent1.putExtra("col",col);
                setResult(0, intent1);
                finish();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode ==0 && data!=null) {  //키 변경
            int newRow = data.getIntExtra("row", 0);
            String show = data.getStringExtra("show");
            int actType = data.getIntExtra("actType",1);
            String sValue = data.getStringExtra("sValue");
            int iValue = data.getIntExtra("iValue",0);

            ed[newRow].setText(show);
            actTypeArr[newRow] = actType;
            sValueArr[newRow] = sValue;
            iValueArr[newRow] = iValue;

           /* Log.d("쇼값",  show);
            Log.d("액트타입값",  "" + actType);
            Log.d("에스밸류값",  sValue);
            Log.d("아이벨류값",  "" + iValue);*/
        }
    }

}
