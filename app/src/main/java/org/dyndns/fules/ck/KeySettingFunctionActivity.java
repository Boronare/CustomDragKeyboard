package org.dyndns.fules.ck;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class KeySettingFunctionActivity extends Activity {
    TextView textShow;
    EditText editShow;
    EditText editKey;
    Spinner spinnerAct;
    Spinner spinnerKeycode;
    Button btr;

    private final String[] listAct = {"1. 문자 입력", "2. Keycode 입력", "3. 입력기 입력"};
    private final String[] listKeycode = {"KEYCODE_DPAD_LEFT", "KEYCODE_DPAD_RIGHT", "KEYCODE_SHIFT_LEFT", "KEYCODE_SPACE", "KEYCODE_ENTER", "KEYCODE_DEL"}; //조사해서 중요한 몇 개 넣기
    private final int[] ArrKeycode = {21, 22, 59, 62, 66, 67};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_key_setting_function);

        Intent intent = getIntent();
        final int row = intent.getIntExtra("row", 0);
        String defaultShow = intent.getStringExtra("show");
        int defaultActType = intent.getIntExtra("actType",1);
        String defaultSValue = intent.getStringExtra("sValue");
        int defaultIValue = intent.getIntExtra("iValue", 0);


        textShow = findViewById(R.id.textShow);
        editShow = findViewById(R.id.editShow);
        editKey = findViewById(R.id.editKey);

        ArrayAdapter<String> spinnerAdapterAct = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_activated_1, listAct);
        ArrayAdapter<String> spinnerAdapterKeycode = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_activated_1, listKeycode);

        spinnerAct = findViewById(R.id.spinnerAct);
        spinnerKeycode = findViewById(R.id.spinnerKeycode);

        spinnerAct.setAdapter(spinnerAdapterAct);
        spinnerKeycode.setAdapter(spinnerAdapterKeycode);

        //초기화하는 부분
        spinnerAct.setSelection(defaultActType-1);
        spinnerKeycode.setSelection(Arrays.binarySearch(ArrKeycode, defaultIValue));
        editKey.setText(defaultSValue);
        textShow.setText(defaultShow);
        editShow.setText(defaultShow);


        editKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                /*String temp = editKey.getText().toString();
                textShow.setText(temp);*/
                String temp = editKey.getText().toString();
                editShow.setText(temp);
            }
        });

        spinnerAct.setOnItemSelectedListener(new OnItemSelectedListener() {
            //1번 textShow   editShow        textShow    <- 다 editShow로
            //3번 editKey    spinnerKeycode  editKey
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){    //actType 1
                    textShow.setVisibility(View.GONE);
                    editShow.setVisibility(View.VISIBLE);
                    spinnerKeycode.setVisibility(View.GONE);
                    editKey.setVisibility(View.VISIBLE);
                }
                else if(position==1){
                    textShow.setVisibility(View.GONE);
                    editShow.setVisibility(View.VISIBLE);
                    editKey.setVisibility(View.GONE);
                    spinnerKeycode.setVisibility(View.VISIBLE);
                }
                else if(position==2){
                    textShow.setVisibility(View.GONE);
                    editShow.setVisibility(View.VISIBLE);
                    spinnerKeycode.setVisibility(View.GONE);
                    editKey.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btr = findViewById(R.id.btr);
        btr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KeySettingFunctionActivity.this, KeySettingPopupActivity.class);
                intent.putExtra("row",row);
                intent.putExtra("actType", spinnerAct.getSelectedItemPosition()+1); //0부터 시작임.
                // 세 개 중 하나 선택되면 맞는 값 - string or spinner의 int값
                //intent.putExtra("value", Integer.parseInt(spinnerCol.getSelectedItem().toString()));
                if(spinnerAct.getSelectedItemPosition()==0 || spinnerAct.getSelectedItemPosition()==2){
                    //intent.putExtra("show", textShow.getText());
                    intent.putExtra("show", editShow.getText().toString());
                    intent.putExtra("sValue", editKey.getText().toString());
                    intent.putExtra("iValue", 0);   //초기화
                }
                else if(spinnerAct.getSelectedItemPosition()==1){
                    intent.putExtra("show", editShow.getText().toString());
                    intent.putExtra("iValue", ArrKeycode[spinnerKeycode.getSelectedItemPosition()]);
                    intent.putExtra("sValue", "");  //초기화
                }
                setResult(0, intent);
                finish();
            }
        });
    }
}
