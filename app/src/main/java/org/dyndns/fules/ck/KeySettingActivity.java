package org.dyndns.fules.ck;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class KeySettingActivity extends Activity {

    int row = 3;
    int col = 5;

    private Button[][] kbdbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_setting);
        kbdbutton = new Button[row][col];
        int[][]btnid = {{R.id.button1,R.id.button2,R.id.button3,R.id.button4,R.id.button5},
                {R.id.button6,R.id.button7,R.id.button8,R.id.button9,R.id.button10},
                {R.id.button11,R.id.button12,R.id.button13,R.id.button14,R.id.button15}};
        for(int i=0; i<row; i++)
            for(int j=0; j<col; j++)
                this.kbdbutton[i][j] = (Button) findViewById(btnid[i][j]);

        for(int i=0; i<row; i++)
            for(int j=0; j<col; j++)
                this.kbdbutton[i][j].setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        //해당 키의 위치정보(i,j 값 등을 이용)를 이용하여 세부 키 내용을 전달 할 수 있을 것 같다...
                        startActivity(new Intent(KeySettingActivity.this, KeySettingPopupActivity.class));
                    }
                });

    }

}
