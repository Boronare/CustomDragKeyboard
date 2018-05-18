package org.dyndns.fules.ck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

public class KeySettingSelectSwapActivity extends Activity {

    Context mContext;

    Button btr;
    KbdModelSelector kbdList;
    RecyclerView.Adapter mAdaptor;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView kbdModelTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            kbdModelTextView = itemView.findViewById(R.id.kbdModelTextView);

        }


    }

    public class MyAdapter extends RecyclerView.Adapter<ViewHolder> implements ItemTouchHelperAdapter{

        private Context context;
        private List<String> mItems;

        public MyAdapter(List<String> items, Context mContext){
            mItems = items;
            context = mContext;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String item = mItems.get(position);
            holder.kbdModelTextView.setText(item);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }


        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Log.i("TEST::","previous: " + kbdList.kbdSerialList.get(i) + " and " + kbdList.kbdSerialList.get(i+1));
                    Collections.swap(mItems, i, i + 1);
                    Log.i("TEST::","now: " + kbdList.kbdSerialList.get(i) + " and " + kbdList.kbdSerialList.get(i+1));
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mItems, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }
    }

    public class MyItemTouchHelperCallback extends ItemTouchHelper.Callback{
        private final ItemTouchHelperAdapter mAdapter;

        public MyItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }
        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

    }

    public interface ItemTouchHelperAdapter {
        boolean onItemMove(int fromPosition, int toPosition);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_setting_select_swap);

        /*//undoserializable 최종 수정하기!! 밑의 온클릭 시 저장하는 부분에서도 name 최종수정 맟춰주기
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
*/

        Intent intent = getIntent();
        kbdList = (KbdModelSelector) intent.getSerializableExtra("kbdList");

        mContext = getApplicationContext();

        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdaptor = new MyAdapter(kbdList.kbdSerialList, mContext);
        mRecyclerView.setAdapter(mAdaptor);

        ItemTouchHelper.Callback callback =  new MyItemTouchHelperCallback((ItemTouchHelperAdapter) mAdaptor);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        btr = findViewById(R.id.btr1);
        btr.setText("완료");
        btr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Arraylist 순서 변경 후 저장
                /*try {
                    FileOutputStream fos = openFileOutput("kbdList", Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(kbdList);
                    Log.i("TEST::","new kbdList saved");
                    oos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                Intent intent = new Intent(KeySettingSelectSwapActivity.this, KeySettingSelectActivity.class);
                intent.putExtra("kbdList", kbdList);
                setResult(0, intent);
                finish();
            }
        });

    }

    /*public Object undoSerializable() throws IOException, ClassNotFoundException {

        Log.i("TEST::","undo Invoked");

        FileInputStream fis = openFileInput("kbdList");
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object kbdList = (KbdModelSelector) ois.readObject();
        ois.close();

        return kbdList;
    }*/

}
