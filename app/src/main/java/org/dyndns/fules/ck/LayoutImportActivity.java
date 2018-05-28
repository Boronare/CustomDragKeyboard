package org.dyndns.fules.ck;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LayoutImportActivity extends ListActivity {

    KbdModelSelector kbdList;
    KbdModel kbdModel;

    private List<String> item = null;
    private List<String> path = null;
    private String root = "/storage/emulated/0";
    private TextView mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_import);
        mPath = (TextView) findViewById(R.id.path);
        getDir(root);
        try {
            kbdList = (KbdModelSelector) this.undoSerializable("kbdList");
        } catch(FileNotFoundException e) {
            Log.i("TEST::","new selector created");
            kbdList = new KbdModelSelector();
        } catch (IOException e) {
            e.printStackTrace();
            kbdList = new KbdModelSelector();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            kbdList = new KbdModelSelector();
        }
        /*
        try {

            FileOutputStream fos = new FileOutputStream(new File(Environment.
                    getExternalStorageDirectory() + "/kbdList"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(kbdList);
            Log.i("TEST::","저장된 경로 :" + Environment.
                    getExternalStorageDirectory());
            Toast.makeText(this, "" + Environment.
                    getExternalStorageDirectory() + " 에 저장되었습니다.", Toast.LENGTH_LONG).show();
            oos.close();
        } catch (FileNotFoundException e) {
            Log.i("TEST::","failed");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    private void getDir(String dirPath) {
        mPath.setText("Location: " + dirPath);
        item = new ArrayList<String>();
        path = new ArrayList<String>();
        File f = new File(dirPath);
        File[] files = f.listFiles();
        if (!dirPath.equals(root)) {
            item.add(root);
            path.add(root);
            item.add("../");
            path.add(f.getParent());
        }

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            path.add(file.getPath());
            if (file.isDirectory())
                item.add(file.getName() + "/");
            else
                item.add(file.getName());
        }
        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item);
        setListAdapter(fileList);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        File file = new File(path.get(position));
        if (file.isDirectory()) {
            if (file.canRead())
                getDir(path.get(position));
            else {
                new AlertDialog.Builder(this)
                        .setTitle("[" + file.getName() + "] folder can't be read!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                            }
                        }).show();
            }
        } else {    //파일 읽는 부분...
            try {
                kbdModel = (KbdModel) this.saveKbdList(file);
                kbdList.add(kbdModel);
                FileOutputStream fos = openFileOutput("kbdList", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(kbdList);
                oos.close();
                Toast.makeText(this, "성공적으로 저장되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            } catch(FileNotFoundException e) {
                Log.i("TEST::","error1");
            } catch (IOException e) {
                Log.i("TEST::","error2");
                Toast.makeText(this, "맞는 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                Log.i("TEST::","error3");
                e.printStackTrace();
            } catch (ClassCastException e) {
                Log.i("TEST::","error4");
                Toast.makeText(this, "맞는 형식이 아닙니다.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public Object undoSerializable(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = openFileInput(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object kbdList = (KbdModelSelector) ois.readObject();
        ois.close();

        return kbdList;
    }

    public Object saveKbdList(File file) throws IOException, ClassNotFoundException {

        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object kbdModel = (KbdModel) ois.readObject();
        ois.close();
        Log.i("TEST::","불러온 file로 saved");
        return kbdModel;
    }

}
