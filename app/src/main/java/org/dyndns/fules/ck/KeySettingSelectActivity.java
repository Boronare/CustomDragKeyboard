package org.dyndns.fules.ck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KeySettingSelectActivity extends Activity {

    //intent로 SwapActivity와 정보 주고받기,
    //여기 액티비티에는 드래그 금지...

    Context mContext;

    Button btr;
    Button btr2;
    Button btr3;
    KbdModelSelector kbdList;
    RecyclerView.Adapter mAdaptor;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView kbdModelTextView;
        private OnListItemClickListener mListener;
        private OnListItemLongClickListener longListener;

        public ViewHolder(View itemView) {
            super(itemView);
            kbdModelTextView = itemView.findViewById(R.id.kbdModelTextView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onListItemClick(getAdapterPosition());
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longListener.onListItemLongClick(getAdapterPosition());
                    return true;
                }
            });
        }

        public interface OnListItemClickListener{
            public void onListItemClick(int position);
        }

        public interface OnListItemLongClickListener{
            public void onListItemLongClick(int position);
        }

        public void setOnListItemClickListener(OnListItemClickListener onListItemClickListener){
            mListener = onListItemClickListener;
        }
        public void setOnListItemLongClickListener(OnListItemLongClickListener onListItemLongClickListener){
            longListener = onListItemLongClickListener;
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<ViewHolder> implements ViewHolder.OnListItemClickListener, ViewHolder.OnListItemLongClickListener{

        private Context context;
        private List<KbdModel> mItems;

        public MyAdapter(List<KbdModel> items, Context mContext){
            mItems = items;
            context = mContext;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
            ViewHolder holder = new ViewHolder(v);
            holder.setOnListItemClickListener(this);
            holder.setOnListItemLongClickListener(this);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            KbdModel item = mItems.get(position);
            holder.kbdModelTextView.setText(item.kbdName);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public void onListItemClick(int position) {
            Intent intent = new Intent(KeySettingSelectActivity.this, KeySettingActivity.class);
            intent.putExtra("KbdModelSerial", mItems.get(position));
            intent.putExtra("KbdList", kbdList);
            intent.putExtra("position", position);
            Log.i("TEST::","get() is " + mItems.get(position).kbdName);
            startActivityForResult(intent, 1);
        }

        @Override
        public void onListItemLongClick(int position) {
            final int pos = position;
            Log.i("TEST::","Long event 됬음. get() is " + mItems.get(position));
            AlertDialog.Builder alert = new AlertDialog.Builder(KeySettingSelectActivity.this);
            alert.setMessage("삭제하시겠습니까?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mItems.remove(pos);
                    dialog.dismiss();     //닫기
                    try {
                        FileOutputStream fos = openFileOutput("kbdList", Context.MODE_PRIVATE);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(kbdList);
                        Log.i("TEST::","new kbdList saved");
                        oos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    recreate();
                }
            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();     //닫기
                }
            });
            // 창 띄우기
            alert.show();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_setting_select);

        //undoserializable 최종 수정하기!! 밑의 온클릭 시 저장하는 부분에서도 name 최종수정 맟춰주기
        try {
            kbdList = (KbdModelSelector) this.undoSerializable();
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

        mContext = getApplicationContext();

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdaptor = new MyAdapter(kbdList.kbdSerialList, mContext);
        mRecyclerView.setAdapter(mAdaptor);

        btr = findViewById(R.id.btr1);
        btr.setText("추가");
        btr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kbdList.add(new KbdModel());

                try {
                    FileOutputStream fos = openFileOutput("kbdList", Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(kbdList);
                    Log.i("TEST::","new kbdList saved");
                    oos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                recreate();

            }
        });

        btr2 = findViewById(R.id.btr2);
        btr2.setText("순서 변경");
        btr2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KeySettingSelectActivity.this, KeySettingSelectSwapActivity.class);
                intent.putExtra("kbdList", kbdList);
                startActivityForResult(intent, 0);
            }
        });
/*
        btr3 = findViewById(R.id.btr3);
        btr3.setText("Export");
        btr3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FileOutputStream fos = new FileOutputStream(new File(Environment.
                            getExternalStorageDirectory() + "/kbdList"));
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(kbdList);
                    Log.i("TEST::","저장된 경로 :" + Environment.
                            getExternalStorageDirectory());
                    Toast.makeText(mContext, "" + Environment.
                            getExternalStorageDirectory() + " 에 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    oos.close();
                } catch (FileNotFoundException e) {
                    Log.i("TEST::","failed");
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });*/

    }

    public Object undoSerializable() throws IOException, ClassNotFoundException {

        Log.i("TEST::","undo Invoked");

        FileInputStream fis = openFileInput("kbdList");
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object kbdList = (KbdModelSelector) ois.readObject();
        ois.close();

        return kbdList;
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode ==0 && data!=null) {
            kbdList = (KbdModelSelector) data.getSerializableExtra("kbdList");
            try {
                FileOutputStream fos = openFileOutput("kbdList", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(kbdList);
                Log.i("TEST::","new kbdList swapped and saved");
                oos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recreate();
        }
        else if(requestCode == 1 && resultCode ==1 && data!=null) {
            recreate();
        }
    }

}
