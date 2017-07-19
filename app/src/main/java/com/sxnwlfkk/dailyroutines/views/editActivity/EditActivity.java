package com.sxnwlfkk.dailyroutines.views.editActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.codetroopers.betterpickers.recurrencepicker.RecurrencePickerDialogFragment;
import com.sxnwlfkk.dailyroutines.R;
import com.sxnwlfkk.dailyroutines.backend.AlarmNotificationReceiver;
import com.sxnwlfkk.dailyroutines.classes.CompositionDialogRoutine;
import com.sxnwlfkk.dailyroutines.classes.RoutineItem;
import com.sxnwlfkk.dailyroutines.util.CompositionUtils;
import com.sxnwlfkk.dailyroutines.util.RoutineUtils;
import com.sxnwlfkk.dailyroutines.data.RoutineContract;
import com.sxnwlfkk.dailyroutines.util.RoutineRecurrencePickerFragment;
import com.sxnwlfkk.dailyroutines.views.profileActivity.ProfileActivity;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cs on 2017.04.05..
 */

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        RecurrencePickerDialogFragment.OnRecurrenceSetListener {

    // VARS

    public static final String LOG_TAG = EditActivity.class.getSimpleName();

    private static final int EDIT_ROUTINE_LOADER = 31;
    private static final int EDIT_ITEMS_LOADER = 32;
    public static final int DAY_IN_MILISECONDS = 24 * 60 * 60 * 1000;
    private static final String FRAG_TAG_RECUR_PICKER = "fragment_recurrence_picker";

    private ArrayList<RoutineItem> mItemsList;
    private ArrayList<Long> mDeletedItems;
    private Uri mCurrentUri;
    private int mCurrentItemIndex = -1;
    private int mRoutineItemSumLength = 0;
    private long mRoutineEndTime = 0;
    private int mTimesUsed;
    private boolean mShowMoreClicked;
    private boolean itemsListLoaded;
    private String mRrule;
    private ArrayList<Long> dependencies;
    ArrayList<CompositionDialogRoutine> composableRoutines;

    // Views
    private ListView mListView;
    private EditListAdapter mAdapter;
    private EditText mNewItemName;
    private EditText mNewItemLengthMinutes;
    private EditText mNewItemLengthSeconds;
    private EditText mRoutineName;
    private TextView mRoutineEndTimeText;
    private Switch mEndTimeSwitch;
    private Button mEndTimeButton;
    private Button mSaveNewItem;
    private Button mDelItem;
    private Button mUpItem;
    private Button mDownItem;
    private Button mShowMoreButton;
    private Button mRecurrenceButton;
    private LinearLayout mTimeLayout;

    private TextView mItemNameTextView;
    private TextView mItemNumber;

    // Click listeners
    private View.OnClickListener itemSaveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (checkInputFields()) {
                addNewItemToList();
                updateListView();
                mNewItemName.setText("");
                mNewItemLengthMinutes.setText("");
                mNewItemLengthSeconds.setText("");
                mCurrentItemIndex = -1;
                mRoutineHasChanged = true;
                mItemNumber.setVisibility(View.GONE);

                // Resetting editability
                mNewItemName.setEnabled(true);
                mNewItemLengthMinutes.setEnabled(true);
                mNewItemLengthSeconds.setEnabled(true);
                LinearLayout editorView = (LinearLayout) findViewById(R.id.edit_item_editor_layout);
                editorView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.bpTransparent));
            }
        }
        private void addNewItemToList() {
            String itemName = mNewItemName.getText().toString().trim();
            String lengthValMin = mNewItemLengthMinutes.getText().toString().trim();
            String lengthValSec = mNewItemLengthSeconds.getText().toString().trim();

            int itemLength = 0;
            if (TextUtils.isEmpty(lengthValMin)) {
                itemLength = Integer.parseInt(lengthValSec);
            } else if (TextUtils.isEmpty(lengthValSec)) {
                itemLength = 60 * Integer.parseInt(lengthValMin);
            } else {
                itemLength = 60 * Integer.parseInt(lengthValMin) + Integer.parseInt(lengthValSec);
            }

            addItemToList(itemName, RoutineUtils.secToMsec(itemLength));

        }
        private boolean checkInputFields() {
            String itemName = mNewItemName.getText().toString().trim();
            if (itemName == "" || TextUtils.isEmpty(mNewItemName.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "Please enter a name for this item.", Toast.LENGTH_LONG).show();
                return false;
            }
            String lengthValMin = mNewItemLengthMinutes.getText().toString().trim();
            String lengthValSec = mNewItemLengthSeconds.getText().toString().trim();
            if (TextUtils.isEmpty(lengthValMin) && TextUtils.isEmpty(lengthValSec)) {
                Toast.makeText(getApplicationContext(), "Please enter a length for this item.", Toast.LENGTH_LONG).show();
                return false;
            }
            try {

                if (TextUtils.isEmpty(lengthValMin)) {
                    Integer.parseInt(lengthValSec);
                } else if (TextUtils.isEmpty(lengthValSec)) {
                    Integer.parseInt(lengthValMin);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "You can only use numbers in the length field", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }
    };

    private void addItemToList(String name, long length) {
        if (mCurrentItemIndex == -1) {
            mItemsList.add(new RoutineItem(name, length));
        } else {
            mItemsList.get(mCurrentItemIndex).setmItemName(name);
            mItemsList.get(mCurrentItemIndex).setmTime(length);
        }

    }
    private View.OnClickListener itemDeleteButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mCurrentItemIndex != -1) {
                if (mCurrentUri != null) {
                    mDeletedItems.add( mItemsList.get(mCurrentItemIndex).getmId());
                }
                mItemsList.remove(mCurrentItemIndex);
                updateListView();
                mCurrentItemIndex = -1;
                mRoutineHasChanged = true;
                mItemNumber.setVisibility(View.GONE);
            }
            mNewItemName.setText("");
            mNewItemLengthMinutes.setText("");
            mNewItemLengthSeconds.setText("");

            // Resetting editability
            mNewItemName.setEnabled(true);
            mNewItemLengthMinutes.setEnabled(true);
            mNewItemLengthSeconds.setEnabled(true);
            LinearLayout editorView = (LinearLayout) findViewById(R.id.edit_item_editor_layout);
            editorView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.bpTransparent));
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
                setItemNumberText(mCurrentItemIndex);
            }
        }
    };
    private View.OnClickListener itemDownButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurrentItemIndex < mItemsList.size() - 1 && mCurrentItemIndex >= 0) {
                RoutineItem tempRoutine = mItemsList.get(mCurrentItemIndex + 1);
                mItemsList.set(mCurrentItemIndex + 1, mItemsList.get(mCurrentItemIndex));
                mItemsList.set(mCurrentItemIndex, tempRoutine);
                updateListView();
                mCurrentItemIndex++;
                mRoutineHasChanged = true;
                setItemNumberText(mCurrentItemIndex);
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
        composableRoutines = null;
        dependencies = new ArrayList<>();

        // Boolean setup
        itemsListLoaded = false;

        // Show more button
        mShowMoreButton = (Button) findViewById(R.id.edit_show_time_button);
        mShowMoreClicked = false;
        mTimeLayout = (LinearLayout) findViewById(R.id.edit_time_box);
        mShowMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShowMoreClicked) {
                    mTimeLayout.setVisibility(View.GONE);
                    mShowMoreButton.setText("more");
                    mShowMoreClicked = false;
                } else {
                    mTimeLayout.setVisibility(View.VISIBLE);
                    mShowMoreButton.setText("less");
                    mShowMoreClicked = true;
                }

            }
        });

        // Recurrence button
        mRecurrenceButton = (Button) findViewById(R.id.edit_change_recurrence_button);
        mRecurrenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                Bundle bundle = new Bundle();
                Time time = new Time();
                time.setToNow();
                if (mRrule == null || mRrule == "") {
                    mRrule = "FREQ=WEEKLY;WKST=MO;BYDAY=MO;";
                }
                bundle.putLong(RecurrencePickerDialogFragment.BUNDLE_START_TIME_MILLIS, time.toMillis(false));
                bundle.putString(RecurrencePickerDialogFragment.BUNDLE_TIME_ZONE, time.timezone);
                bundle.putString(RecurrencePickerDialogFragment.BUNDLE_RRULE, mRrule);
                bundle.putBoolean(RecurrencePickerDialogFragment.BUNDLE_HIDE_SWITCH_BUTTON, true);

                RecurrencePickerDialogFragment rpd = (RecurrencePickerDialogFragment) fm.findFragmentByTag(
                        FRAG_TAG_RECUR_PICKER);
                if (rpd != null) {
                    rpd.dismiss();
                }
                rpd = new RoutineRecurrencePickerFragment();
                rpd.setArguments(bundle);
                rpd.setOnRecurrenceSetListener(EditActivity.this);
                rpd.show(fm, FRAG_TAG_RECUR_PICKER);
            }
        });

        // Setting up Item editor
        mNewItemName = (EditText) findViewById(R.id.edit_textbox_item_name);
        mNewItemLengthMinutes = (EditText) findViewById(R.id.edit_item_length_minutes);
        mNewItemLengthSeconds = (EditText) findViewById(R.id.edit_item_length_seconds);
        mSaveNewItem = (Button) findViewById(R.id.edit_button_item_save);
        mSaveNewItem.setOnClickListener(itemSaveButtonClickListener);
        mDelItem = (Button) findViewById(R.id.edit_button_delete_item);
        mDelItem.setOnClickListener(itemDeleteButtonClickListener);
        mUpItem = (Button) findViewById(R.id.edit_button_up);
        mUpItem.setOnClickListener(itemUpButtonClickListener);
        mDownItem = (Button) findViewById(R.id.edit_button_down);
        mDownItem.setOnClickListener(itemDownButtonClickListener);
        mEndTimeButton = (Button) findViewById(R.id.edit_routine_end_time_change_button);
        mEndTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v);
            }
        });
        mItemNumber = (TextView) findViewById(R.id.edit_item_textbox_number);
        mItemNumber.setVisibility(View.GONE);

        // Setting up Routine main fields
        mRoutineName = (EditText) findViewById(R.id.edit_textbox_routine_name);
        mRoutineName.setOnTouchListener(mOnTouchListener);
        mRoutineEndTimeText = (TextView) findViewById(R.id.edit_routine_end_time_textview);
        mRoutineEndTimeText.setOnTouchListener(mOnTouchListener);
        mEndTimeButton = (Button) findViewById(R.id.edit_routine_end_time_change_button);
        mEndTimeButton.setOnTouchListener(mOnTouchListener);
        mEndTimeSwitch = (Switch) findViewById(R.id.edit_end_time_switch);
        mEndTimeSwitch.setOnTouchListener(mOnTouchListener);
        mEndTimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LinearLayout endTimeBox = (LinearLayout) findViewById(R.id.edit_routine_end_time_box);
                if (isChecked) {
                    endTimeBox.setVisibility(View.VISIBLE);
                } else {
                    endTimeBox.setVisibility(View.GONE);
                }
            }
        });

        // Check intent
        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        if (mCurrentUri == null) {
            // New item
            // TODO
            getSupportActionBar().setTitle(R.string.new_item);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.material_indigo_darken3)));
        } else {
            getLoaderManager().initLoader(EDIT_ROUTINE_LOADER, null, this);
            getLoaderManager().initLoader(EDIT_ITEMS_LOADER, null, this);
            getSupportActionBar().setTitle(R.string.edit_item);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.material_indigo_darken3)));
        }
        mDeletedItems = new ArrayList<>();
    }

    // Dialogs
    @Override
    public void onBackPressed() {
        // If the routine hasn't changed, continue with handling back button press
        if (!mRoutineHasChanged) {
            NavUtils.navigateUpFromSameTask(EditActivity.this);
//            super.onBackPressed();
            finish();
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

    private void showOptimizeDialog(DialogInterface.OnClickListener optimizeButtonClockListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.optmimize_dialog_msg);
        builder.setPositiveButton(R.string.optimize_button_text, optimizeButtonClockListener);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Nope" button, so dismiss the dialog
                // and continue editing the routine.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showComposeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_menu_compose_dialog_title);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item);
        if (mCurrentUri != null) {
            composableRoutines = CompositionUtils.loadRoutinesForDialog(this, ContentUris.parseId(mCurrentUri));
        } else {
            composableRoutines = CompositionUtils.loadRoutinesForDialog(this, (long) 0);
        }


        ArrayList<String> composableRoutinesStrings = new ArrayList<>();

        if (composableRoutines.size() == 0) {
            composableRoutinesStrings.add(getResources().getString(R.string.no_composable_routines_text));
        } else {
            for (int i = 0; i < composableRoutines.size(); i++) {
                CompositionDialogRoutine cdr = composableRoutines.get(i);
                String itemStr = cdr.getName()
                        + " ("
                        + RoutineUtils.formatLengthString(RoutineUtils.msecToSec(cdr.getLength()) )
                        + ")";

                composableRoutinesStrings.add(itemStr);
            }

        }

        arrayAdapter.addAll(composableRoutinesStrings);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nothing
                addCompositeRoutine(composableRoutines.get(which));
                mRoutineHasChanged = true;
            }
        });

        builder.show();
    }

    private void addCompositeRoutine(CompositionDialogRoutine cdr) {
        mItemsList.add(new RoutineItem(cdr.getName(), cdr.getLength(), -1 * cdr.getId()));
        updateListView();
        dependencies.add(cdr.getId());
    }

    // Recurrence set listener
    @Override
    public void onRecurrenceSet(String rrule) {
        Log.d(LOG_TAG, "Recurrence rule: " + rrule);

        mRrule = rrule;
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
                    finish();
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        NavUtils.navigateUpFromSameTask(EditActivity.this);
                        finish();
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case R.id.menu_edit_save_button:
                boolean error = false;
                if (mCurrentUri == null) {
                    error = saveRoutine();
                } else {
                    error = updateRoutine();
                }
                // This means, that it's an update, or the new routine save was successful
                if (mCurrentUri != null && !error) {
                    Intent intent = new Intent(EditActivity.this, ProfileActivity.class);
                    intent.setData(mCurrentUri);
                    startActivity(intent);
                    finish();
                }
                return true;
            case R.id.edit_menu_optimize_button:
                if (mCurrentUri == null) {
                    Toast.makeText(this, "Can't optimize a new routine, sorry.", Toast.LENGTH_LONG).show();
                } else if (mTimesUsed < 2) {
                    Toast.makeText(this, "Not enough information about the routine. Finish it a couple of times then come back.", Toast.LENGTH_LONG).show();
                } else {
                    DialogInterface.OnClickListener optimizeButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Optimize!" button
                                    optimizeRoutine();
                                }
                            };
                    showOptimizeDialog(optimizeButtonClickListener);
                }
                break;
            case R.id.edit_menu_compose_button:
                showComposeDialog();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void optimizeRoutine() {
        for (int i = 0; i < mItemsList.size(); i++) {
            mItemsList.get(i).setmTime((long) mItemsList.get(i).getmAverageTime());
        }
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
                String lengthMin = String.valueOf(mItemsList.get(mCurrentItemIndex).getmTime() / 60000);
                lengthMin = (lengthMin.equals("0")) ? "" : lengthMin;
                String lengthSec = String.valueOf((mItemsList.get(mCurrentItemIndex).getmTime() / 1000) % 60);
                lengthSec = (lengthSec.equals("0")) ? "" : lengthSec;
                mNewItemName.setText(name);
                mNewItemLengthMinutes.setText(lengthMin);
                mNewItemLengthSeconds.setText(lengthSec);

                LinearLayout editorView = (LinearLayout) findViewById(R.id.edit_item_editor_layout);
                if (mItemsList.get(mCurrentItemIndex).getmAverageTime() < 0) {
                    editorView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.material_indigo_lighten5));
                    mNewItemName.setEnabled(false);
                    mNewItemLengthMinutes.setEnabled(false);
                    mNewItemLengthSeconds.setEnabled(false);
                } else {
                    editorView.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.bpTransparent));
                    mNewItemName.setEnabled(true);
                    mNewItemLengthMinutes.setEnabled(true);
                    mNewItemLengthSeconds.setEnabled(true);
                }

                mItemNumber.setVisibility(View.VISIBLE);
                setItemNumberText(mCurrentItemIndex);
            }
        });
    }

    /**
     * Saves a new routine, after checking the routine input fields.
     */
    private boolean saveRoutine() {
        // Input checking
        // TODO: check end time and relevant fields
        if (TextUtils.isEmpty(mRoutineName.getText().toString().trim())) {
            Toast.makeText(this, R.string.save_routine_have_no_name_message, Toast.LENGTH_LONG).show();
            return true;
        }

        int routineItemNumber = mItemsList.size();
        if (routineItemNumber == 0) {
            Toast.makeText(this, R.string.save_without_item_message, Toast.LENGTH_LONG).show();
            return true;
        }

        // Get info for routine
        String routineName = String.valueOf(mRoutineName.getText());
        for (int j = 0; j < mItemsList.size(); j++) mRoutineItemSumLength += mItemsList.get(j).getmTime();
        if (mRoutineItemSumLength >= DAY_IN_MILISECONDS) {
            Toast.makeText(this, "Sorry, you can't have a routine longer than a day.", Toast.LENGTH_LONG).show();
            return true;
        }

        // Set recurrence rule if not set otherwise
        if (mRrule == null || mRrule == "") {
            mRrule = "FREQ=WEEKLY;WKST=MO;BYDAY=MO;";
        }

        // Make CV
        ContentValues values = new ContentValues();
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME, routineName);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER, routineItemNumber);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH, mRoutineItemSumLength);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME, mRoutineEndTime);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END, (mEndTimeSwitch.isChecked()) ? 1 : 0);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_WEEKDAYS_CONFIG, mRrule);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_EXTRA_TEXT, CompositionUtils.writeDependenciesString(dependencies));
        // Insert new routine
        mCurrentUri = getContentResolver().insert(RoutineContract.RoutineEntry.CONTENT_URI, values);
        // Get info for items
        long newRoutineId = ContentUris.parseId(mCurrentUri);
        // Insert items
        for (int i = 0; i < mItemsList.size(); i++) {
            ContentValues itemValues = new ContentValues();
            if (mItemsList.get(i).getmAverageTime() < 0) {
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME,
                        mItemsList.get(i).getmAverageTime());
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NO, i);
                itemValues.put(RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE, newRoutineId);
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NAME, mItemsList.get(i).getmItemName());
            } else {
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NAME, mItemsList.get(i).getmItemName());
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NO, i);
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH, mItemsList.get(i).getmTime());
                itemValues.put(RoutineContract.ItemEntry.COLUMN_REMAINING_TIME, mItemsList.get(i).getmTime());
                itemValues.put(RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE, newRoutineId);
            }

            getContentResolver().insert(RoutineContract.ItemEntry.CONTENT_URI, itemValues);
        }

        // Schedule alarm if it's set
        if (mEndTimeSwitch.isChecked()) {
            AlarmNotificationReceiver.registerNextAlarm(this, mCurrentUri,
                    RoutineUtils.calculateIdealStartTime(RoutineUtils.msecToSec(mRoutineEndTime),
                            RoutineUtils.msecToSec(mRoutineItemSumLength)),
                    routineName,
                    mRrule);
        }

        CompositionUtils.updateRoutine(this.getBaseContext(), newRoutineId);

        Toast.makeText(this, "Your routine is saved", Toast.LENGTH_LONG).show();

        return false;
    }

    /**
     * Updates the pre-existing routine. First, it deletes all items, that the user removed
     * in the editor. Then it tries to update the remaining. If it fails, it means that the item
     * is new. If that is the case, the method inserts it to the table.
     */
    private boolean updateRoutine() {


        if (TextUtils.isEmpty(mRoutineName.getText().toString().trim())) {
            Toast.makeText(this, R.string.save_routine_have_no_name_message, Toast.LENGTH_LONG).show();
            return true;
        }

        int routineItemNumber = mItemsList.size();
        if (routineItemNumber == 0) {
            Toast.makeText(this, R.string.save_without_item_message, Toast.LENGTH_LONG).show();
            return true;
        }

        String routineName = String.valueOf(mRoutineName.getText());
        mRoutineItemSumLength = 0;
        for (int j = 0; j < mItemsList.size(); j++) mRoutineItemSumLength += mItemsList.get(j).getmTime();
        if (mRoutineItemSumLength >= DAY_IN_MILISECONDS) {
            Toast.makeText(this, "Sorry, you can't have a routine longer than a day.", Toast.LENGTH_LONG).show();
            return true;
        }
        // Update the routine
        ContentValues values = new ContentValues();
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME, routineName);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_ITEMS_NUMBER, routineItemNumber);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH, mRoutineItemSumLength);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_END_TIME, mRoutineEndTime);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END, (mEndTimeSwitch.isChecked()) ? 1 : 0);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_WEEKDAYS_CONFIG, mRrule);
        values.put(RoutineContract.RoutineEntry.COLUMN_ROUTINE_EXTRA_TEXT, CompositionUtils.writeDependenciesString(dependencies));

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
        // Update changed items
        for (int i = 0; i < mItemsList.size(); i++) {
            Uri updateUri = ContentUris.withAppendedId(RoutineContract.ItemEntry.CONTENT_URI, mItemsList.get(i).getmId());
            ContentValues itemValues = new ContentValues();
            if (mItemsList.get(i).getmAverageTime() < 0) {
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME,
                        mItemsList.get(i).getmAverageTime());
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NO, i);
                itemValues.put(RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE, updatedRoutineId);
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NAME, mItemsList.get(i).getmItemName());
            } else {
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NAME, mItemsList.get(i).getmItemName());
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_NO, i);
                itemValues.put(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH, mItemsList.get(i).getmTime());
                itemValues.put(RoutineContract.ItemEntry.COLUMN_PARENT_ROUTINE, updatedRoutineId);
                itemValues.put(RoutineContract.ItemEntry.COLUMN_REMAINING_TIME, mItemsList.get(i).getmTime());
            }

            int rowsAffected = getContentResolver().update(updateUri, itemValues, null, null);
            if (rowsAffected == 0) {
                getContentResolver().insert(RoutineContract.ItemEntry.CONTENT_URI, itemValues);
            }
        }

        CompositionUtils.updateRoutine(this.getBaseContext(), updatedRoutineId);

        // Schedule alarm is it's set
        if (mEndTimeSwitch.isChecked()) {
            AlarmNotificationReceiver.registerNextAlarm(this, mCurrentUri,
                    RoutineUtils.calculateIdealStartTime(RoutineUtils.msecToSec(mRoutineEndTime),
                            RoutineUtils.msecToSec(mRoutineItemSumLength)), routineName, mRrule);
        } else {
            AlarmNotificationReceiver.cancelAlarm(this, mCurrentUri);
        }
        Toast.makeText(this, "Your routine is updated", Toast.LENGTH_LONG).show();

        return false;
    }

    private void setItemNumberText(int i) {
        mItemNumber.setText(i + 1 + ".");
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
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_TIMES_USED,
                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_WEEKDAYS_CONFIG,
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
                int rRequireEnd = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_REQUIRE_END));
                mTimesUsed = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_TIMES_USED));
                mRrule = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.RoutineEntry.COLUMN_ROUTINE_WEEKDAYS_CONFIG));

                boolean[] valami = RoutineUtils.parseAlarmDay(mRrule);


                mRoutineName.setText(rName);
                mRoutineEndTime = rEndTime;
                mRoutineEndTimeText.setText(RoutineUtils.formatClockTimeString(RoutineUtils.msecToSec(rEndTime)));
                if (rRequireEnd == 1) {
                    mEndTimeSwitch.setChecked(true);
                } else {
                    mEndTimeSwitch.setChecked(false);
                }
                break;
            case EDIT_ITEMS_LOADER:
                if (!itemsListLoaded) {
                    for (int i = 0; i < cursor.getCount(); i++) {
                        RoutineItem newRoutine = null;
                        long avg = cursor.getLong(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME));
                        if (avg < 0) {
                            newRoutine = new RoutineItem(null, 0, avg);
                            String[] projection = {
                                    RoutineContract.RoutineEntry._ID,
                                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_NAME,
                                    RoutineContract.RoutineEntry.COLUMN_ROUTINE_LENGTH,
                            };

                            Cursor itemCursor = getContentResolver().query(
                                    ContentUris.withAppendedId(RoutineContract.RoutineEntry.CONTENT_URI, -1 * avg),
                                    projection,
                                    null,
                                    null,
                                    null);

                            itemCursor.moveToFirst();
                            String name = itemCursor.getString(itemCursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_NAME));
                            long length = itemCursor.getLong(itemCursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH));
                            long id = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry._ID));

                            newRoutine = new RoutineItem(name, length, avg);
                            newRoutine.setmId(id);
                        } else {
                            long id = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry._ID));
                            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_NAME));
                            int itemTime = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_LENGTH));
                            int itemAvg = cursor.getInt(cursor.getColumnIndexOrThrow(RoutineContract.ItemEntry.COLUMN_ITEM_AVG_TIME));
                            newRoutine = new RoutineItem(itemName, itemTime, itemAvg);
                            newRoutine.setmId(id);
                        }

                        mItemsList.add(newRoutine);
                        if (!cursor.moveToNext()) break;
                    }
                    updateListView();
                    itemsListLoaded = true;
                }
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int loaderId = loader.getId();

        switch (loaderId) {
            case EDIT_ROUTINE_LOADER:
                mRoutineName.setText("");
                mRoutineEndTimeText.setText("");
                break;
            case EDIT_ITEMS_LOADER:
                mListView.setAdapter(null);
                break;
            default:
                break;
        }
    }

    // Time pickers
    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(this.getFragmentManager(), "timePicker");
    }

    public static class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final java.util.Calendar c = java.util.Calendar.getInstance();
            EditActivity parent = (EditActivity) getActivity();
            int timeInSeconds = RoutineUtils.msecToSec(parent.mRoutineEndTime);
            int hour;
            int minute;
            if (timeInSeconds == 0) {
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            } else {
                hour = timeInSeconds / 3600;
                minute = (timeInSeconds % 3600) / 60;
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            int seconds = hourOfDay * 3600 + minute * 60;
            EditActivity parent = (EditActivity) getActivity();
            parent.mRoutineEndTime = RoutineUtils.secToMsec(seconds);
            parent.mRoutineEndTimeText.setText(RoutineUtils.formatClockTimeString(seconds));
        }
}
}
