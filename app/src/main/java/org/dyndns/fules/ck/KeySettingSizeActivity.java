package org.dyndns.fules.ck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

public class KeySettingSizeActivity extends Activity {

    private Spinner spinnerRow;
    private Spinner spinnerCol;

    private Button btr;

    private final String[] listRow = {"2", "3", "4", "5", "6"};
    private final String[] listCol = {"5", "6", "7", "8", "9"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_setting_size);

        Intent intent = getIntent();
        final int row = intent.getIntExtra("row", 3);
        final int col = intent.getIntExtra("col", 5);

        ArrayAdapter<String> spinnerAdapterRow = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_activated_1, listRow);
        ArrayAdapter<String> spinnerAdapterCol = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_activated_1, listCol);

        spinnerRow = (Spinner)findViewById(R.id.spinnerRow);
        spinnerCol = (Spinner)findViewById(R.id.spinnerCol);

        spinnerRow.setAdapter(spinnerAdapterRow);
        spinnerCol.setAdapter(spinnerAdapterCol);
        spinnerRow.setSelection(row - 2);
        spinnerCol.setSelection(col - 5);

        btr = (Button)findViewById(R.id.btr);

        /*btr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KeySettingSizeActivity.this, KeySettingActivity.class);
                intent.putExtra("row", Integer.parseInt(spinnerRow.getSelectedItem().toString()));
                intent.putExtra("col", Integer.parseInt(spinnerCol.getSelectedItem().toString()));
                setResult(1, intent);
                finish();
                *//*intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*//*
            }
        });*/
        btr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(KeySettingSizeActivity.this);
                alert.setTitle("※주의");
                alert.setMessage("변경 시 키보드가 초기화됩니다. " +
                        "실행하시겠습니까?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(KeySettingSizeActivity.this, KeySettingActivity.class);
                        intent.putExtra("row", Integer.parseInt(spinnerRow.getSelectedItem().toString()));
                        intent.putExtra("col", Integer.parseInt(spinnerCol.getSelectedItem().toString()));
                        setResult(1, intent);
                        dialog.dismiss();     //닫기
                        finish();
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(KeySettingSizeActivity.this, KeySettingActivity.class);
                        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        dialog.dismiss();     //닫기
                    }
                });
                // 창 띄우기
                alert.show();
            }
        });

    }
}
