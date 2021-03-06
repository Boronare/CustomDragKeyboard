package kr.ac.cau.team135.customdragkeyboard;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import kr.ac.cau.team135.customdragkeyboard.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

//설정 진입 시, 기본 객체를 복사하여 조정용 객체를 만듦 (기본 객체 없으면 default 객체)
//그 후, 조정용 객체 값 불러와서 activity에 뿌림(표시)
//버튼 클릭 후 키 변경, 키보드 이름 변경 등 설정값 조작 시 조정용 객체에 반영됨.
//모든 조작 완료 후 사용자가 '적용' 시, 해당 조정용 객체를 기본 객체로 하여 저장...

public class KeySettingActivity extends Activity {
    //기본 키보드값
    static final String[][][] defaultShow = {
            {{"`","ㄲ","","ㅎ","ㅇ","ㅋ","","ㄱ",""},{"{","ㄸ","}","ㄹ","ㄴ","ㅌ","=","ㄷ","0"},{"1","2","3","4","5","6","7","8","9"},{"ㅖ","ㅛ","ㅒ","ㅕ","ㅣ","ㅑ","ㅖ","ㅠ","ㅒ"},{"(",",",")","<","⌫",">","[",".","]"}},
            {{"+","ㅆ","-","ㅊ","ㅅ","ㅉ","*","ㅈ","/"},{"'","ㅃ","\"","ㅍ","ㅁ","ㅍ",";","ㅂ",":"},{"!","@","#","$","%","^","&","*","?"},{"ㅔ","ㅗ","ㅐ","ㅓ","ㅡ","ㅏ","ㅔ","ㅜ","ㅐ"},{"s","t","u","v","⏎","w","x","y","z"}},
            {{"A","B","C","D","E","F","G","H","I"},{"J","K","L","M","N","O","P","Q","R"},{"S","T","U","V"," ","W","X","Y","Z"},{"a","b","c","d","e","f","g","h","i"},{"j","k","l","m","n","o","p","q","r"}}
    };

    //KbdModel kbdModelSerial;
    KbdModelSelector kbdList;
    int kbdPosition;

    int row;    // 2 ~ 6
    int col;    // 5 ~ 9

    KbdModel userKbdModel;
    KbdModel tempKbdModel;

    EditText kbdTitle;
    TableLayout kbdKeyLayout[][];
    TextView kbdText[][];
    Spinner spinnerLang;

    String[] listLang;
    private final int[] listLangValue = {0, 1, 2, 3};

    private class btnOnClickListener implements Button.OnClickListener{

        @Override
        public void onClick(View view) {    //save (적용)
            tempKbdModel.kbdName = kbdTitle.getText().toString();
            tempKbdModel.kbdLang = listLangValue[spinnerLang.getSelectedItemPosition()];
            try {
               /*
                FileOutputStream fos = openFileOutput("" + kbdModelSerial, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(tempKbdModel);
                oos.close();*/
                kbdList.kbdSerialList.set(kbdPosition, tempKbdModel);
                FileOutputStream fos = openFileOutput("kbdList", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(kbdList);
                Log.i("TEST::","new kbdList and 수정된 kbdModel saved");
                oos.close();
                Toast.makeText(KeySettingActivity.this, R.string.key_setting_modify, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(KeySettingActivity.this, KeySettingSelectActivity.class);
                setResult(1, intent);
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

        listLang = getResources().getStringArray(R.array.key_setting_lang);

        Intent intent = getIntent();
        /*try {
            userKbdModel = (KbdModel) this.undoSerializable();

        } catch(FileNotFoundException e) {
            userKbdModel = init(row, col);
        } catch (IOException e) {
            e.printStackTrace();
            userKbdModel=init(row, col);
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            userKbdModel=init(row, col);
        }*/
        userKbdModel = (KbdModel) intent.getSerializableExtra("KbdModelSerial");
        kbdList = (KbdModelSelector) intent.getSerializableExtra("KbdList");
        kbdPosition = intent.getIntExtra("position", 0);

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
        mainLayout.setFocusable(true);
        mainLayout.isFocusableInTouchMode();

        TableRow titleTr = new TableRow(this);

        TableRow.LayoutParams titleParams = new TableRow.LayoutParams();
        titleParams.span = col;
        titleParams.width = (getResources().getDisplayMetrics().widthPixels);
        titleParams.height = (getResources().getDisplayMetrics().heightPixels/10);   //어림잡아 정한 크기
        titleParams.setMargins(20,0,20,0);

        kbdTitle = new EditText(this);
        kbdTitle.setLayoutParams(titleParams);
        kbdTitle.setText(tempKbdModel.kbdName);    //"키보드 이름 :" + 키보드이름변수
        kbdTitle.setTextSize(24);
        kbdTitle.setSingleLine();
        titleTr.addView(kbdTitle);
        mainLayout.addView(titleTr);

        TableRow langTr = new TableRow(this);
        TableRow.LayoutParams langParams = new TableRow.LayoutParams();
        langParams.span = col/2;
        langParams.width = (getResources().getDisplayMetrics().widthPixels);
        langParams.height = (getResources().getDisplayMetrics().heightPixels/10);   //어림잡아 정한 크기
        langParams.setMargins(20,0,0,0);

        TextView kbdLang = new TextView(this);
        kbdLang.setLayoutParams(langParams);
        kbdLang.setText(R.string.key_setting_default_lang);    //"기본 언어 :" + 키보드언어변수
        kbdLang.setTextSize(24);
        langTr.addView(kbdLang);


        spinnerLang = new Spinner(this);
        ArrayAdapter<String> spinnerAdapterLang = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_activated_1, listLang);
        spinnerLang.setAdapter(spinnerAdapterLang);
        TableRow.LayoutParams spinnerParams = new TableRow.LayoutParams();
        if(col%2==0)
            spinnerParams.span = col/2;
        else
            spinnerParams.span = col/2 +1;
        spinnerParams.width = (getResources().getDisplayMetrics().widthPixels);
        spinnerParams.height = (getResources().getDisplayMetrics().heightPixels/10);   //어림잡아 정한 크기
        spinnerParams.setMargins(0,0,20,0);
        spinnerLang.setLayoutParams(spinnerParams);
        spinnerLang.setSelection(tempKbdModel.kbdLang);
        langTr.addView(spinnerLang);
        mainLayout.addView(langTr);

        kbdKeyLayout = new TableLayout[row][col];
        kbdText = new TextView[row*3][col*3];

        for(int i=0; i<row; i++){
            TableRow tr = new TableRow(this);
            for(int j=0; j<col; j++){
                kbdKeyLayout[i][j] = new TableLayout(this);
                for(int k=0; k<3; k++){
                    TableRow innerTr = new TableRow(this);
                    for(int l=0; l<3; l++){
                        kbdText[i*3+k][j*3+l] = new TextView(this);
                        kbdText[i*3+k][j*3+l].setWidth(getResources().getDisplayMetrics().widthPixels/(col*3));
                        kbdText[i*3+k][j*3+l].setHeight(getResources().getDisplayMetrics().widthPixels/(col*3));
                        //kbdText[i*3+k][j*3+l].setBackgroundResource(R.drawable.button);
                        kbdText[i*3+k][j*3+l].setGravity(Gravity.CENTER);
                        kbdText[i*3+k][j*3+l].setTextColor(Color.BLACK);
                        kbdText[i*3+k][j*3+l].setText(tempKbdModel.row[i].col[j].dir[k*3+l].show);
                        innerTr.addView(kbdText[i*3+k][j*3+l]);
                    }
                    kbdKeyLayout[i][j].addView(innerTr);
                }
                final int finalI = i;
                final int finalJ = j;
                kbdKeyLayout[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(KeySettingActivity.this, KeySettingPopupActivity.class);
                        intent.putExtra("kbdModel", tempKbdModel);
                        intent.putExtra("row",finalI);
                        intent.putExtra("col",finalJ);
                        startActivityForResult(intent, 0);
                    }
                });
                kbdKeyLayout[i][j].setBackgroundResource(R.drawable.button);
                tr.addView(kbdKeyLayout[i][j]);
            }
            mainLayout.addView(tr);
        }

        TableRow sizeTr = new TableRow(this);
        Button sizeBtr = new Button(this);
        TableRow.LayoutParams sizeBtrParams = new TableRow.LayoutParams();
        sizeBtrParams.span = col;
        sizeBtrParams.setMargins(20,10,20,0);
        sizeBtr.setLayoutParams(sizeBtrParams);
        sizeBtr.setText(R.string.key_setting_set_size);
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

        TableRow exportTr = new TableRow(this);
        Button exportBtr = new Button(this);
        exportBtr.setLayoutParams(sizeBtrParams);
        exportBtr.setText(R.string.key_setting_export);
        exportBtr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*try {
                    FileOutputStream fos = new FileOutputStream(new File(Environment.
                            getExternalStorageDirectory() + "/" + tempKbdModel.kbdName));
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(tempKbdModel);
                    Log.i("TEST::","저장된 경로 :" + Environment.
                            getExternalStorageDirectory());
                    Toast.makeText(KeySettingActivity.this, "" + Environment.
                            getExternalStorageDirectory() + " 에 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    oos.close();
                } catch (FileNotFoundException e) {
                    Log.i("TEST::","failed");
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                //마시멜로우 이상인지 확인...
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
                else if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    try {
                        FileOutputStream fos = new FileOutputStream(new File(Environment.
                                getExternalStorageDirectory() + "/" + tempKbdModel.kbdName));
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(tempKbdModel);
                        Log.i("TEST::","저장된 경로 :" + Environment.
                                getExternalStorageDirectory());
                        Toast.makeText(KeySettingActivity.this, KeySettingActivity.this.getString(R.string.key_setting_export_locate) + Environment.
                                getExternalStorageDirectory(), Toast.LENGTH_SHORT).show();
                        oos.close();
                    } catch (FileNotFoundException e) {
                        Log.i("TEST::","failed");
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{

                }

            }
        });
        exportTr.addView(exportBtr);
        mainLayout.addView(exportTr);

        TableRow applyTr = new TableRow(this);
        Button applyBtr = new Button(this);
        applyBtr.setLayoutParams(sizeBtrParams);
        applyBtr.setText(R.string.save);
        btnOnClickListener onClickListener = new btnOnClickListener();
        applyBtr.setOnClickListener(onClickListener);
        applyTr.addView(applyBtr);

        mainLayout.addView(applyTr);

        TableRow closeTr = new TableRow(this);
        Button closeBtr = new Button(this);
        closeBtr.setLayoutParams(sizeBtrParams);
        closeBtr.setText(R.string.cancel);
        closeBtr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        closeTr.addView(closeBtr);

        mainLayout.addView(closeTr);

        mainScroll.addView(mainLayout);
        setContentView(mainScroll);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(Environment.
                        getExternalStorageDirectory() + "/" + tempKbdModel.kbdName));
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(tempKbdModel);
                Log.i("TEST::","저장된 경로 :" + Environment.
                        getExternalStorageDirectory());
                Toast.makeText(KeySettingActivity.this, KeySettingActivity.this.getString(R.string.key_setting_export_locate) + Environment.
                        getExternalStorageDirectory(), Toast.LENGTH_SHORT).show();
                oos.close();
            } catch (FileNotFoundException e) {
                Log.i("TEST::","failed");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            CALLDialog();
            //Toast.makeText(getApplicationContext(), "사용자가 해당 권한을 거부하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void CALLDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(R.string.permission_dialog_title);
        alertDialog.setMessage(R.string.permission_dialog);

        // 권한 설정
        alertDialog.setPositiveButton(R.string.permission_dialog_yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                        startActivity(intent);
                        dialog.cancel();
                    }
                });
        //취소
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    public static KbdModel init(int rows, int cols) {
        //키보드 크기 변경 시에만 사용...
        KbdModel userKbdModel;
        userKbdModel = new KbdModel(rows, cols);
        int initRows;
        int initCols;

        if(rows <2) //예외처리
            initRows = 3;
        else
            initRows = rows;

        if(cols <4) //예외처리
            initCols = 5;
        else
            initCols = cols;

        userKbdModel = new KbdModel(initRows, initCols);
        userKbdModel.kbdName = "Default Keyboard";
        userKbdModel.kbdLang = 0;

        for (int i = 0; i < initRows; i++) {
            Log.i("TEST::","i="+i);
            userKbdModel.row[i] = new KbdModel.Row(initCols);
            for (int j = 0; j < initCols; j++) {
                userKbdModel.row[i].col[j] = new KbdModel.Col();
                for (int k = 0; k < 9; k++) {
                    KbdModel.Dir curdir = userKbdModel.row[i].col[j].dir[k];
                    if(i>2 || j>4) {  //기본 값 row=3, col=5 이거보다 클 경우 기본 문자로 초기화 ㄴㄴ 공백으로 초기화
                        curdir.show = "";
                        curdir.sValue = "";
                        curdir.iValue = 0;
                        curdir.actType = 1;
                    }
                    else{
                        switch(defaultShow[i][j][k]){
                            case " ":curdir.show="␣";
                                curdir.sValue=" ";
                                curdir.iValue=0;
                                curdir.actType=1;
                                break;
                            case "⏎":curdir.show="⏎";
                                curdir.sValue="";
                                curdir.iValue= KeyEvent.KEYCODE_ENTER;
                                curdir.actType=2;
                                break;
                            case "⌫":curdir.show="⌫";
                                curdir.sValue="";
                                curdir.iValue=KeyEvent.KEYCODE_DEL;
                                curdir.actType=2;
                                break;
                            default:
                                curdir.show = defaultShow[i][j][k];
                                curdir.sValue = defaultShow[i][j][k];
                                curdir.iValue = 0;
                                curdir.actType = 1;
                        }
                    }
                }
            }
        }
        return userKbdModel;
    }
    /*public void doSerializable() throws IOException {

        FileOutputStream fos = openFileOutput("userKbdModel", Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(userKbdModel);
        oos.close();
        Toast.makeText(KeySettingActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();

        finish();
    }

*/
    /*public Object undoSerializable() throws IOException, ClassNotFoundException {

        FileInputStream fis = openFileInput("" + kbdModelSerial);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object kbdModel = (KbdModel) ois.readObject();
        ois.close();
        Log.i("TEST::","receivedKbdSerial read");


        return kbdModel;
    }
*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode ==0 && data!=null) {  //키 변경
            int newRow = data.getIntExtra("row", 3);
            int newCol = data.getIntExtra("col", 5);
            tempKbdModel = (KbdModel) data.getSerializableExtra("kbdModel");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    kbdText[newRow*3+i][newCol*3+j].setText(tempKbdModel.row[newRow].col[newCol].dir[i*3+j].show);
                }
            }

        }
        else if(requestCode == 1 && resultCode ==1 && data!=null){   //사이즈 변경
            int newRow = data.getIntExtra("row", 3);
            int newCol = data.getIntExtra("col", 5);
            this.init(newRow, newCol);
            userKbdModel=init(newRow, newCol);

            try {
               /* FileOutputStream fos = openFileOutput("" + kbdModelSerial, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(userKbdModel);
                oos.close();*/
                kbdList.kbdSerialList.set(kbdPosition, userKbdModel);
                FileOutputStream fos = openFileOutput("kbdList", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(kbdList);
                Log.i("TEST::","new kbdList and 수정된 kbdModel saved");
                oos.close();
                Toast.makeText(KeySettingActivity.this, R.string.key_setting_modify, Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            restartActivity(this, userKbdModel, kbdList, kbdPosition);
        }
    }

    public static void restartActivity(Activity act, KbdModel userKbdModel, KbdModelSelector kbdList, int kbdPosition){

        Intent intent=new Intent();
        intent.putExtra("KbdModelSerial", userKbdModel);
        intent.putExtra("KbdList", kbdList);
        intent.putExtra("position", kbdPosition);
        intent.setClass(act, act.getClass());

        act.finish();
        act.startActivity(intent);

    }

}
