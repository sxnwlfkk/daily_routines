package com.sxnwlfkk.dailyroutines.views.editActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.classes.RoutineItem;

import java.util.ArrayList;

/**
 * Created by cs on 2017.04.05..
 */

public class EditActivity extends Activity {

    // VARS

    public static final String LOG_TAG = EditActivity.class.getSimpleName();

    private ArrayList<RoutineItem> mItemsList;
    private Uri mCurrentUri;

    // Views
    private ListView mListView;
    private EditListAdapter mAdapter;
    private EditText mNewItemName;
    private EditText mNewItemLength;
    private Button mSaveNewItem;

    // Click listeners
    private View.OnClickListener itemButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addNewItemToList();
            updateListView(mItemsList);
        }

        private void addNewItemToList() {
            String itemName = mNewItemName.getText().toString();
            int itemLength = Integer.parseInt(mNewItemLength.getText().toString());

            mItemsList.add(new RoutineItem(itemName, itemLength));
            Log.e("addNewItemToList", "Size of list: " + mItemsList.size());
            Log.e("addNewItemToList", "Items of mItemsList = " + mItemsList.get(0).getmItemName());
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Setting up list adapter
        mItemsList = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.edit_list);
        mAdapter = new EditListAdapter(this, mItemsList);
        mListView.setAdapter(mAdapter);

        // Setting up Item editor
        mNewItemName = (EditText) findViewById(R.id.edit_textbox_item_name);
        mNewItemLength = (EditText) findViewById(R.id.edit_textbox_item_length);
        mSaveNewItem = (Button) findViewById(R.id.edit_button_item_save);
        mSaveNewItem.setOnClickListener(itemButtonClickListener);

        // Check intent
        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        if (mCurrentUri == null) {
            setTitle(R.string.new_item);
        }

    }

    private void updateListView (final ArrayList<RoutineItem> items) {
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO
            }
        });

    }


}
