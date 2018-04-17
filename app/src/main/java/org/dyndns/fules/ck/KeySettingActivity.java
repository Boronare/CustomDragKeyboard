package org.dyndns.fules.ck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

//설정 진입 시, 기본 객체를 복사하여 조정용 객체를 만듦 (기본 객체 없으면 default 객체)
//그 후, 조정용 객체 값 불러와서 activity에 뿌림(표시)
//버튼 클릭 후 키 변경, 키보드 이름 변경 등 설정값 조작 시 조정용 객체에 반영됨.
//모든 조작 완료 후 사용자가 '적용' 시, 해당 조정용 객체를 기본 객체로 하여 저장...

public class KeySettingActivity extends Activity {
    //기본 키보드값
    static final String[][][] defaultShow = {
            {{"+","ㄲ","-","あ","ㄱ","ㅋ","*","#","/"},{"!","ㄸ","@","い","ㄴ","ㅌ","#","ㄷ","$"},{"%","^","&","(","ㄹ",")","*","_","="},{"{","ㅛ","}","ㅕ","う","ㅑ","[","ㅠ","]"},{"|",";",":","'","◁","'",",",".","."}},
            {{"1","2","3","ㅍ","ㅁ","ㅍ","7","8","9"},{"お","ㅃ","か","き","ㅂ","く","7","ㅃ","9"},{"~","ㅆ","~","ㅆ","ㅅ","ㅆ","~","ㅆ","~"},{"ㅖ","ㅗ","ㅒ","ㅓ","ㅣ","ㅏ","!","ㅜ","?"},{"1","2","3","4","5","6","7","8","9"}},
            {{"0","ㅗ","え","ㅓ","ㅇ","ㅏ","!","ㅜ","?"},{"ㅉ","ㅉ","ㅉ","ㅊ","ㅈ","ㅊ","ㅊ","ㅊ","ㅊ"},{"a","b","c","d","ㅎ","e","f","g","h"},{"ㅖ","ㅚ","ㅒ","ㅝ","ㅢ","ㅘ","!","ㅟ","?"},{"<","0",">","0","←","0","<","0",">"}}
    };

    int row;    // 2 ~ 6
    int col;    // 5 ~ 9

    KbdModel userKbdModel;
    KbdModel tempKbdModel;

    private class btnOnClickListener implements Button.OnClickListener{

        @Override
        public void onClick(View view) {    //save (적용)
            try {
                FileOutputStream fos = openFileOutput("userKbdModel", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(tempKbdModel);
                oos.close();
                Toast.makeText(KeySettingActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.init(row,col);
        try {
            userKbdModel = (KbdModel) this.undoSerializable();
        } catch(FileNotFoundException e) {
            userKbdModel = init(row, col);
        } catch (IOException e) {
            e.printStackTrace();
            userKbdModel=init(row, col);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            userKbdModel=init(row, col);
        }

        row = userKbdModel.row.length;
        col = userKbdModel.row[0].col.length;
        tempKbdModel = userKbdModel;    //temp에 복사해서 temp를 사용하자.

        ScrollView mainScroll = new ScrollView(this);
        ScrollView.LayoutParams scrollParams = new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        TableLayout mainLayout = new TableLayout(this);
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                0.0F
        );

        TableRow titleTr = new TableRow(this);
        TextView kbdTitle = new TextView(this);
        TableRow.LayoutParams titleParams = new TableRow.LayoutParams();
        titleParams.span = col;
        titleParams.width = (getResources().getDisplayMetrics().widthPixels);
        titleParams.height = (getResources().getDisplayMetrics().heightPixels/16);   //어림잡아 정한 크기
        titleParams.setMargins(20,0,0,0);
        kbdTitle.setLayoutParams(titleParams);
        kbdTitle.setText("키보드 이름 : " + tempKbdModel.kbdName);    //"키보드 이름 :" + 키보드이름변수
        kbdTitle.setTextSize(24);
        titleTr.addView(kbdTitle);
        mainLayout.addView(titleTr);

        TableRow langTr = new TableRow(this);
        TextView kbdLang = new TextView(this);
        kbdLang.setLayoutParams(titleParams);
        kbdLang.setText("기본 언어 : 한국어");    //"기본 언어 :" + 키보드언어변수
        kbdLang.setTextSize(24);
        langTr.addView(kbdLang);
        mainLayout.addView(langTr);

        for(int i=0; i<row; i++){
            TableRow tr = new TableRow(this);
            for(int j=0; j<col; j++){
                Button btr = new Button(this);
                TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btr.setLayoutParams(buttonParams);
                btr.setWidth(getResources().getDisplayMetrics().widthPixels/col);
                btr.setHeight(getResources().getDisplayMetrics().widthPixels/col);
                btr.setBackgroundResource(R.drawable.button);
                btr.setId(10*i + j);
                if(i<3 && j<5){
                    btr.setText(tempKbdModel.row[i].col[j].dir[4].show);
                }
                final int finalI = i;
                final int finalJ = j;
                btr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(KeySettingActivity.this, KeySettingPopupActivity.class);
                        intent.putExtra("kbdModel", tempKbdModel);  //여기서부터...
                        intent.putExtra("row",finalI);
                        intent.putExtra("col",finalJ);
                        startActivityForResult(intent, 0);
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
                Intent intent = new Intent(KeySettingActivity.this, KeySettingSizeActivity.class);
                intent.putExtra("row",row);
                intent.putExtra("col",col);
                startActivityForResult(intent, 1);
            }
        });
        sizeTr.addView(sizeBtr);
        mainLayout.addView(sizeTr);

        TableRow applyTr = new TableRow(this);
        Button applyBtr = new Button(this);
        applyBtr.setLayoutParams(sizeBtrParams);
        applyBtr.setText("수정");
        btnOnClickListener onClickListener = new btnOnClickListener();
        applyBtr.setOnClickListener(onClickListener);
        applyTr.addView(applyBtr);

        mainLayout.addView(applyTr);

        mainScroll.addView(mainLayout);
        setContentView(mainScroll);
    }

    public static KbdModel init(int rows, int cols) {
        Log.i("TEST::","init Invoked");
        KbdModel userKbdModel;
        userKbdModel = new KbdModel(rows, cols);
        userKbdModel.kbdName = "기본 키보드";

        for (int i = 0; i < rows; i++) {
            Log.i("TEST::","i="+i);
            userKbdModel.row[i] = new KbdModel.Row(cols);
            for (int j = 0; j < cols; j++) {
                userKbdModel.row[i].col[j] = new KbdModel.Col();
                for (int k = 0; k < 9; k++) {
                    if(i>2 || j>4) {  //기본 값 row=3, col=5 이거보다 클 경우 기본 문자로 초기화 ㄴㄴ 공백으로 초기화
                        userKbdModel.row[i].col[j].dir[k].show = "";
                        userKbdModel.row[i].col[j].dir[k].sValue = "";
                        userKbdModel.row[i].col[j].dir[k].actType = 1;
                    }
                    else{
                        userKbdModel.row[i].col[j].dir[k].show = defaultShow[i][j][k];
                        userKbdModel.row[i].col[j].dir[k].sValue = defaultShow[i][j][k];
                        userKbdModel.row[i].col[j].dir[k].actType = 1;
                    }
                }
            }
        }
        return userKbdModel;
    }
    public void doSerializable() throws IOException {

        FileOutputStream fos = openFileOutput("userKbdModel", Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(userKbdModel);
        oos.close();
        Toast.makeText(KeySettingActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();

        finish();
    }


    public Object undoSerializable() throws IOException, ClassNotFoundException {

        FileInputStream fis = openFileInput("userKbdModel");
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object kbdModel = (KbdModel) ois.readObject();
        ois.close();

        return kbdModel;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode ==0 && data!=null) {  //키 변경
            tempKbdModel = (KbdModel) data.getSerializableExtra("kbdModel");
            userKbdModel = tempKbdModel;
            try {
                FileOutputStream fos = openFileOutput("userKbdModel", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(userKbdModel);
                oos.close();
                Toast.makeText(KeySettingActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            restartActivity(this);
        }
        else if(requestCode == 1 && resultCode ==1 && data!=null){   //사이즈 변경
            int newRow = data.getIntExtra("row", 3);
            int newCol = data.getIntExtra("col", 5);
            userKbdModel=init(newRow, newCol);
            //tempKbdModel = userKbdModel;

            try {
                FileOutputStream fos = openFileOutput("userKbdModel", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(userKbdModel);
                oos.close();
                Toast.makeText(KeySettingActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            restartActivity(this);
        }
        //잘 나오는지 확인...
        //Toast.makeText(this, "" + tempKbdModel.row[0].col[0].dir[0].show, Toast.LENGTH_SHORT).show();
    }

    public static void restartActivity(Activity act){

        Intent intent=new Intent();
        intent.setClass(act, act.getClass());

        act.finish();
        act.startActivity(intent);

    }

}
