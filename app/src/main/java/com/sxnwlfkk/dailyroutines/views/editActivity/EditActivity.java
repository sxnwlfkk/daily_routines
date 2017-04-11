package com.sxnwlfkk.dailyroutines.views.editActivity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.classes.RoutineItem;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;
import com.sxnwlfkk.dailyroutines.views.profileActivity.ProfileActivity;

import java.util.ArrayList;

/**
 * Created by cs on 2017.04.05..
 */

public class EditActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    // VARS

    public static final String LOG_TAG = EditActivity.class.getSimpleName();

    private ArrayList<RoutineItem> mItemsList;
    private Uri mCurrentUri;
    private int mCurrentItemIndex = -1;

    // Views
    private ListView mListView;
    private EditListAdapter mAdapter;
    private EditText mNewItemName;
    private EditText mNewItemLength;
    private EditText mRoutineName;
    private EditText mRoutineEndTime;
    private Button mSaveNewItem;
    private Button mDelItem;
    private Button mUpItem;
    private Button mDownItem;
    private Button mSaveRoutine;
    private Button mCancel;

    // Click listeners
    private View.OnClickListener itemSaveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addNewItemToList();
            updateListView(mItemsList);
            mNewItemName.setText("");
            mNewItemLength.setText("");
        }
        private void addNewItemToList() {
            String itemName = mNewItemName.getText().toString();
            int itemLength = Integer.parseInt(mNewItemLength.getText().toString());
            mItemsList.add(new RoutineItem(itemName, itemLength));
        }
    };
    private View.OnClickListener itemDeleteButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mCurrentItemIndex != -1) {
                mItemsList.remove(mCurrentItemIndex);
                updateListView(mItemsList);
            }
            mNewItemName.setText("");
            mNewItemLength.setText("");
        }
    };
    private View.OnClickListener itemUpButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurrentItemIndex > 0) {
                RoutineItem tempRoutine = mItemsList.get(mCurrentItemIndex - 1);
                mItemsList.set(mCurrentItemIndex - 1, mItemsList.get(mCurrentItemIndex));
                mItemsList.set(mCurrentItemIndex, tempRoutine);
                updateListView(mItemsList);
                mCurrentItemIndex--;
            }
        }
    };
    private View.OnClickListener itemDownButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurrentItemIndex < mItemsList.size() - 1) {
                RoutineItem tempRoutine = mItemsList.get(mCurrentItemIndex + 1);
                mItemsList.set(mCurrentItemIndex + 1, mItemsList.get(mCurrentItemIndex));
                mItemsList.set(mCurrentItemIndex, tempRoutine);
                updateListView(mItemsList);
                mCurrentItemIndex++;
            }
        }
    };
    private View.OnClickListener mRoutineSaveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveRoutine();
            Intent intent = new Intent(EditActivity.this, ProfileActivity.class);
            intent.setData(mCurrentUri);
            startActivity(intent);
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
        mSaveNewItem.setOnClickListener(itemSaveButtonClickListener);
        mDelItem = (Button) findViewById(R.id.edit_button_delete_item);
        mDelItem.setOnClickListener(itemDeleteButtonClickListener);
        mUpItem = (Button) findViewById(R.id.edit_button_up);
        mUpItem.setOnClickListener(itemUpButtonClickListener);
        mDownItem = (Button) findViewById(R.id.edit_button_down);
        mDownItem.setOnClickListener(itemDownButtonClickListener);

        // Setting up Cancel and Save buttons
        mSaveRoutine = (Button) findViewById(R.id.edit_button_routine_save);
        mSaveRoutine.setOnClickListener(mRoutineSaveButtonClickListener);
        mCancel = (Button) findViewById(R.id.edit_button_cancel);

        // Setting up Routine main fields
        mRoutineName = (EditText) findViewById(R.id.edit_textbox_routine_name);
        mRoutineEndTime = (EditText) findViewById(R.id.edit_textbox_routine_end_time);

        // Check intent
        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        if (mCurrentUri == null) {
            // New item
            // TODO
            setTitle(R.string.new_item);
        } else {
            // TODO: set up loaders
            // Edit exisiting item
        }

    }

    private void updateListView (final ArrayList<RoutineItem> items) {
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentItemIndex = (int) id;
                String name = mItemsList.get(mCurrentItemIndex).getmItemName();
                String length = String.valueOf(mItemsList.get(mCurrentItemIndex).getmTime());
                mNewItemName.setText(name);
                mNewItemLength.setText(length);
            }
        });
    }

    private void saveRoutine() {
        // Get info for routine
        String routineName = String.valueOf(mRoutineName.getText());
        int routineItemNumber = mItemsList.size();
        int rLength = 0;
        for (int j = 0; j < mItemsList.size(); j++) rLength += mItemsList.get(j).getmTime();

        // Make CV
        ContentValues values = new ContentValues();
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME, routineName);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER, routineItemNumber);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH, rLength);
        // Insert new routine
        mCurrentUri = getContentResolver().insert(RoutineContract.RoutineEntry.CONTENT_URI, values);
        // Get info for items
        long newRoutineId = ContentUris.parseId(mCurrentUri);
        // Insert items
        for (int i = 0; i < mItemsList.size(); i++) {
            ContentValues itemValues = new ContentValues();
            itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NAME, mItemsList.get(i).getmItemName());
            itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NO, i);
            itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH, mItemsList.get(i).getmTime());
            itemValues.put(RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE, newRoutineId);

            getContentResolver().insert(RoutineContract.ItemEntry.CONTENT_URI, itemValues);
        }

        Toast.makeText(this, "Your routine is saved", Toast.LENGTH_LONG).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
