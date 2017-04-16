package com.sxnwlfkk.dailyroutines.views.editActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

    private static final int EDIT_ROUTINE_LOADER = 31;
    private static final int EDIT_ITEMS_LOADER = 32;

    private ArrayList<RoutineItem> mItemsList;
    private ArrayList<Long> mDeletedItems;
    private Uri mCurrentUri;
    private int mCurrentItemIndex = -1;
    private int mRoutineItemSumLength = 0;

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

    // Click listeners
    private View.OnClickListener itemSaveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (checkInputFields()) {
                addNewItemToList();
                updateListView();
                mNewItemName.setText("");
                mNewItemLength.setText("");
                mCurrentItemIndex = -1;
                mRoutineHasChanged = true;
            }
        }
        private void addNewItemToList() {
            String itemName = mNewItemName.getText().toString().trim();
            int itemLength = Integer.parseInt(mNewItemLength.getText().toString().trim());
            if (mCurrentItemIndex == -1) {
                mItemsList.add(new RoutineItem(itemName, itemLength));
            } else {
                mItemsList.get(mCurrentItemIndex).setmItemName(itemName);
                mItemsList.get(mCurrentItemIndex).setmTime(itemLength);
            }
        }
        private boolean checkInputFields() {
            String itemName = mNewItemName.getText().toString().trim();
            if (itemName == "" || TextUtils.isEmpty(mNewItemName.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "Please enter a name for this item.", Toast.LENGTH_LONG).show();
                return false;
            }
            String lengthVal = mNewItemLength.getText().toString().trim();
            if (lengthVal == "" || TextUtils.isEmpty(mNewItemLength.getText())) {
                Toast.makeText(getApplicationContext(), "Please enter a length for this item.", Toast.LENGTH_LONG).show();
                return false;
            }
            try {
                int itemLen = Integer.parseInt(lengthVal);
            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "You can only use numbers in the length field", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }
    };
    private View.OnClickListener itemDeleteButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mCurrentItemIndex != -1) {
                if (mCurrentUri != null) {
                    mDeletedItems.add((Long) mItemsList.get(mCurrentItemIndex).getmId());
                }
                mItemsList.remove(mCurrentItemIndex);
                updateListView();
                mCurrentItemIndex = -1;
                mRoutineHasChanged = true;
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
                updateListView();
                mCurrentItemIndex--;
                mRoutineHasChanged = true;
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
                updateListView();
                mCurrentItemIndex++;
                mRoutineHasChanged = true;
            }
        }
    };
    private View.OnClickListener mRoutineSaveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurrentUri == null) {
                saveRoutine();
            } else {
                updateRoutine();
            }
            // This means, that it's an update, or the new routine save was successful
            if (mCurrentUri != null) {
                Intent intent = new Intent(EditActivity.this, ProfileActivity.class);
                intent.setData(mCurrentUri);
                startActivity(intent);
            }
        }
    };

    // Change sentinel
    private boolean mRoutineHasChanged = false;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mRoutineHasChanged = true;
            return false;
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

        // Setting up Routine main fields
        mRoutineName = (EditText) findViewById(R.id.edit_textbox_routine_name);
        mRoutineName.setOnTouchListener(mOnTouchListener);
        mRoutineEndTime = (EditText) findViewById(R.id.edit_textbox_routine_end_time);
        mRoutineEndTime.setOnTouchListener(mOnTouchListener);

        // Check intent
        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        if (mCurrentUri == null) {
            // New item
            // TODO
            getActionBar().setTitle(R.string.new_item);
        } else {
            getLoaderManager().initLoader(EDIT_ROUTINE_LOADER, null, this);
            getLoaderManager().initLoader(EDIT_ITEMS_LOADER, null, this);
            mDeletedItems = new ArrayList<>();
            getActionBar().setTitle(R.string.edit_item);
            mSaveRoutine.setText(R.string.update_button);
        }
    }

    // Dialogs
    @Override
    public void onBackPressed() {
        // If the routine hasn't changed, continue with handling back button press
        if (!mRoutineHasChanged) {
            NavUtils.navigateUpFromSameTask(EditActivity.this);
//            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }

            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Options


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mRoutineHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
//            super.onBackPressed();
                     return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        NavUtils.navigateUpFromSameTask(EditActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets the adapter on the list view, and updates the list item click listener.
     * In effect it makes the list to refresh.
     */
    private void updateListView() {
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

    /**
     * Saves a new routine, after checking the routine input fields.
     */
    private void saveRoutine() {
        // Input checking
        // TODO: check end time and relevant fields
        if (TextUtils.isEmpty(mRoutineName.getText().toString().trim())) {
            Toast.makeText(this, R.string.save_routine_have_no_name_message, Toast.LENGTH_LONG).show();
            return;
        }

        int routineItemNumber = mItemsList.size();
        if (routineItemNumber == 0) {
            Toast.makeText(this, R.string.save_without_item_message, Toast.LENGTH_LONG).show();
            return;
        }

        // Get info for routine
        String routineName = String.valueOf(mRoutineName.getText());
        for (int j = 0; j < mItemsList.size(); j++) mRoutineItemSumLength += mItemsList.get(j).getmTime();

        // Make CV
        ContentValues values = new ContentValues();
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME, routineName);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER, routineItemNumber);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH, mRoutineItemSumLength);
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
            itemValues.put(RoutineContract.ItemEntry.COLUMN_REMAINING_TIME, mItemsList.get(i).getmTime());
            itemValues.put(RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE, newRoutineId);

            getContentResolver().insert(RoutineContract.ItemEntry.CONTENT_URI, itemValues);
        }

        Toast.makeText(this, "Your routine is saved", Toast.LENGTH_LONG).show();
    }

    /**
     * Updates the pre-existing routine. First, it deletes all items, that the user removed
     * in the editor. Then it tries to update the remaining. If it fails, it means that the item
     * is new. If that is the case, the method inserts it to the table.
     */
    private void updateRoutine() {

        if (TextUtils.isEmpty(mRoutineName.getText().toString().trim())) {
            Toast.makeText(this, R.string.save_routine_have_no_name_message, Toast.LENGTH_LONG).show();
            return;
        }

        int routineItemNumber = mItemsList.size();
        if (routineItemNumber == 0) {
            Toast.makeText(this, R.string.save_without_item_message, Toast.LENGTH_LONG).show();
            return;
        }

        String routineName = String.valueOf(mRoutineName.getText());
        mRoutineItemSumLength = 0;
        for (int j = 0; j < mItemsList.size(); j++) mRoutineItemSumLength += mItemsList.get(j).getmTime();
        // Update routine
        ContentValues values = new ContentValues();
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME, routineName);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER, routineItemNumber);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH, mRoutineItemSumLength);

        getContentResolver().update(mCurrentUri, values, null, null);
        long updatedRoutineId = ContentUris.parseId(mCurrentUri);

        // Delete removed items from DB
        if (mDeletedItems.size() > 0) {
            for (int i = 0; i < mDeletedItems.size(); i++) {
                Uri deleteUri = ContentUris.withAppendedId(
                        RoutineContract.ItemEntry.CONTENT_URI,
                        mDeletedItems.get(i));
                getContentResolver().delete(deleteUri, null, null);
            }
        }
        // Update items
        for (int i = 0; i < mItemsList.size(); i++) {
            Uri updateUri = ContentUris.withAppendedId(RoutineContract.ItemEntry.CONTENT_URI, mItemsList.get(i).getmId());
            ContentValues itemValues = new ContentValues();
            itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NAME, mItemsList.get(i).getmItemName());
            itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NO, i);
            itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH, mItemsList.get(i).getmTime());
            itemValues.put(RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE, updatedRoutineId);

            int rowsAffected = getContentResolver().update(updateUri, itemValues, null, null);
            if (rowsAffected == 0) {
                getContentResolver().insert(RoutineContract.ItemEntry.CONTENT_URI, itemValues);
            }
        }
        Toast.makeText(this, "Your routine is updated", Toast.LENGTH_LONG).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        long id = 0;
        try {
            id = ContentUris.parseId(mCurrentUri);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Invalid argument in URI (URI is not ending with .../#): " + mCurrentUri);
            return null;
        }

        if (loaderId == EDIT_ROUTINE_LOADER) {
            String[] projection = {
                    RoutineContract.RoutineEntry._ID,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME,
            };

            return new CursorLoader(this,
                    mCurrentUri,
                    projection,
                    null,
                    null,
                    null);

        } else if (loaderId == EDIT_ITEMS_LOADER) {
            String[] projection = new String[] {
                    RoutineContract.ItemEntry._ID,
                    RoutineContract.ItemEntry.COLUMN_ITEM_NAME,
                    RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH,
                    RoutineContract.ItemEntry.COLUMN_ITEM_NO,
                    RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME,
            };

            String selection = RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE + "=?";
            String[] selectionArgs = new String[] { String.valueOf(id) };

            return new CursorLoader(this,
                    RoutineContract.ItemEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    RoutineContract.ItemEntry.COLUMN_ITEM_NO + " ASC");

        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int loaderId = loader.getId();
        cursor.moveToFirst();

        switch (loaderId) {
            case EDIT_ROUTINE_LOADER:
                String rName = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME));
                int rEndTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME));

                mRoutineName.setText(rName);
                mRoutineEndTime.setText(String.valueOf(rEndTime));

                break;
            case EDIT_ITEMS_LOADER:
                for (int i = 0; i < cursor.getCount(); i++) {
                    long id = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry._ID));
                    String itemName = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_NAME));
                    int itemTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH));
                    int itemAvg = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME));

                    RoutineItem newRoutine = new RoutineItem(itemName, itemTime, itemAvg);
                    newRoutine.setmId(id);
                    mItemsList.add(newRoutine);
                    if (!cursor.moveToNext()) break;
                }
                updateListView();
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int loaderId = loader.getId();

        switch (loaderId) {
            case EDIT_ROUTINE_LOADER:
                mRoutineName.setText("");
                mRoutineEndTime.setText("");
                break;
            case EDIT_ITEMS_LOADER:
                mListView.setAdapter(null);
                break;
            default:
                break;
        }
    }
}
