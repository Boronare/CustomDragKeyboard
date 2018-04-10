package org.dyndns.fules.ck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class KeySettingActivity extends Activity {

    Intent intent;

    int row;    // 2 ~ 6
    int col;    // 5 ~ 9

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();   //preference로 변경해야 함

        row = intent.getIntExtra("row", 3);
        col = intent.getIntExtra("col", 5);

        ScrollView mainScroll = new ScrollView(this);
        ScrollView.LayoutParams scrollParams = new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)

                ;
        TableLayout mainLayout = new TableLayout(this);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                0.0F
        );


        TableRow blankTr = new TableRow(this);
        TextView blank = new TextView(this);
        TableRow.LayoutParams blankParams = new TableRow.LayoutParams();
        blankParams.span = col;
        blankParams.width = (getResources().getDisplayMetrics().widthPixels);
        blankParams.height = (getResources().getDisplayMetrics().heightPixels/8);   //어림잡아 정한 크기
        blank.setLayoutParams(blankParams);
        blank.setText("원하는 키를 터치해주세요.");
        blank.setGravity(Gravity.CENTER);
        blank.setClickable(false);
        blank.setTextSize(24);
        blankTr.addView(blank);
        mainLayout.addView(blankTr);

        for(int i=0; i<row; i++){
            TableRow tr = new TableRow(this);
            for(int j=0; j<col; j++){
                Button btr = new Button(this);
                TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btr.setLayoutParams(buttonParams);
                //btr.setText(i + "" + j);
                btr.setWidth(getResources().getDisplayMetrics().widthPixels/col);
                btr.setHeight(getResources().getDisplayMetrics().widthPixels/col);
                btr.setBackgroundResource(R.drawable.button);
                btr.setId(10*i + j);
                btr.setText(btr.getId() + "");
                btr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(KeySettingActivity.this, KeySettingPopupActivity.class));
                    }
                });
                tr.addView(btr);
            }
            mainLayout.addView(tr);
        }

        TableRow sizeTr = new TableRow(this);
        Button sizeBtr = new Button(this);
        TableRow.LayoutParams sizeBtrParams = new TableRow.LayoutParams();
        sizeBtrParams.span = col;
        sizeBtrParams.setMargins(20,20,20,0);
        sizeBtr.setLayoutParams(sizeBtrParams);
        sizeBtr.setText("크기 변경");
        sizeBtr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(KeySettingActivity.this, KeySettingSizeActivity.class));
            }
        });
        sizeTr.addView(sizeBtr);
        Button applyBtr = new Button(this);
        applyBtr.setLayoutParams(sizeBtrParams);
        applyBtr.setText("수정");
        mainLayout.addView(sizeTr);
/*
        for(int i=0; i<row; i++)
            for(int j=0; j<col; j++)
                this.kbdbutton[i][j].setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        //해당 키의 위치정보(i,j 값 등을 이용)를 이용하여 세부 키 내용을 전달 할 수 있을 것 같다...
                        startActivity(new Intent(KeySettingActivity.this, KeySettingPopupActivity.class));
                    }
                });
*/
       // setContentView(mainLayout);
        mainScroll.addView(mainLayout);
        setContentView(mainScroll);

    }

}
