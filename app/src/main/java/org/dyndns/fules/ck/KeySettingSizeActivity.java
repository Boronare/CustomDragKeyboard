package org.dyndns.fules.ck;

import android.app.Activity;
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

    private final String[] listRow = {"2", "3", "4", "5", "6", "7"};
    private final String[] listCol = {"5", "6", "7", "8", "9"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_setting_size);

        ArrayAdapter<String> spinnerAdapterRow = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_activated_1, listRow);
        ArrayAdapter<String> spinnerAdapterCol = new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_activated_1, listCol);

        spinnerRow = (Spinner)findViewById(R.id.spinnerRow);
        spinnerCol = (Spinner)findViewById(R.id.spinnerCol);

        spinnerRow.setAdapter(spinnerAdapterRow);
        spinnerCol.setAdapter(spinnerAdapterCol);

        btr = (Button)findViewById(R.id.btr);

        btr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KeySettingSizeActivity.this, KeySettingActivity.class);
                intent.putExtra("row", Integer.parseInt(spinnerRow.getSelectedItem().toString()));
                intent.putExtra("col", Integer.parseInt(spinnerCol.getSelectedItem().toString()));
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }
}
