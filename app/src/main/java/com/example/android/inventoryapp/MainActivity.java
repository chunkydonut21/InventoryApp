package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // The name of this class, for the purposes of logs
    private static final String LOG_TAG = MainActivity.class.getName();

    private static final int INVENTORY_LOADER = 0;

    // The adapter which holds the Cursor data, and has instructions for creating list item Views
    // for each row in its Cursor.
    private InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView lv = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        lv.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);

        lv.setAdapter(mCursorAdapter);


        // Set up an OnItemClickListener which will open up EditorActivity if a list item is clicked
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri currentUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);

                Log.i(LOG_TAG, "The content URI being passed is " + currentUri.toString());
                intent.setData(currentUri);
                startActivity(intent);
            }
        });

        // Initialise the Loader.
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                showDeleteAllDataConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertInfo() {
        Drawable drawable= getResources().getDrawable(R.drawable.apple);
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] buffer= out.toByteArray();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, "Apple");
        values.put(InventoryContract.InventoryEntry.COLUMN_NAME, "Fruit Fresh");
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, 5);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, 2);
        values.put(InventoryContract.InventoryEntry.COLUMN_EMAIL, "abc342@gmail.com");
        values.put(InventoryContract.InventoryEntry.COLUMN_IMAGE, buffer);

        Uri newRowUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        // If this is the Loader that queries the inventory table,
        if (loaderId == INVENTORY_LOADER) {
            // Define the projection (i.e. the column names you want returned in the Cursor object).
            // Each list item displays only the name and breed, so we don't need the gender and
            // weight columns. However, the _id column is needed because the CursorAdapter assumes
            // that the Cursor contains a column called _id in order to work correctly.
            String[] projection = {
                    InventoryContract.InventoryEntry._ID,
                    InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                    InventoryContract.InventoryEntry.COLUMN_NAME,
                    InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                    InventoryContract.InventoryEntry.COLUMN_EMAIL,
                    InventoryContract.InventoryEntry.COLUMN_IMAGE,
                    InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE
            };

            // Create the CursorLoader with the Content URI of the inventory table and a projection.
            return new CursorLoader(
                    this,                       // Parent activity context
                    InventoryContract.InventoryEntry.CONTENT_URI,       // Content URI of table to query
                    projection,                 // Columns to include in the resulting Cursor
                    null,                       // No selection clause
                    null,                       // No selection arguments
                    null                        // Default sort order
            );
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Invoked whenever the CursorLoader is reset. For example if the data in the
        // ContentProvider changes, it means data in the current Cursor has become outdated/stale.
        //
        // Clear out the adapter's reference to the outdated Cursor.
        mCursorAdapter.swapCursor(null);
    }


    private void showDeleteAllDataConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_pets_dialog_msg);

        builder.setPositiveButton(R.string.delete_all, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllData();
            }
        });

        builder.setNegativeButton(R.string.delete_it, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deleteAllData() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);

        // If more than one row was deleted,
        if (rowsDeleted > 0) {
            Toast.makeText(this, R.string.delete_successful, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
        }
    }
}