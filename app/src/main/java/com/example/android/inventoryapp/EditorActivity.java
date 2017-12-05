package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;

import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static java.lang.Integer.parseInt;

/**
 * Created by Hp on 07-04-2017.
 */
public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_INVENT_LOADER = 1;
    private static final int SELECT_FILE = 1;

    private static final int RESULT_LOAD_IMAGE = 1;


    private Uri mCurrentInventoryUri;
    private EditText mProductNameEditText;
    private EditText mYourName;
    private EditText mYourEmail;
    private EditText mProductQuantity;
    private EditText mPriceEditText;
    private boolean mInventtHasChanged = false;
    /**
     * ImageView for holding product image
     */
    private ImageView mImageView;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventtHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mProductNameEditText = (EditText) findViewById(R.id.edit_name);
        mYourName = (EditText) findViewById(R.id.supplier_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mYourEmail = (EditText) findViewById(R.id.supplier_mail);
        mProductQuantity = (EditText) findViewById(R.id.quantity_edit);

        mImageView = (ImageView) findViewById(R.id.image_view);
        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();


        // If no content URI was passed as an Intent extra, this means the Floating Action Button
        // started this Activity
        if (mCurrentInventoryUri == null) {
            setTitle(getString(R.string.editorActivityTitle));

            invalidateOptionsMenu();
        } else {

            setTitle(getString(R.string.editor_activity_title_edit));

            getLoaderManager().initLoader(EXISTING_INVENT_LOADER, null, this);
        }

        mProductNameEditText.setOnTouchListener(mTouchListener);
        mYourName.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mProductQuantity.setOnTouchListener(mTouchListener);
        mYourEmail.setOnTouchListener(mTouchListener);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save info to database
                saveInfo();
                // Exit activity
                finish();
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if (!mInventtHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteButton(View view) {
        // Display a dialog for the user to confirm deletion
        showDeleteConfirmationDialog();
    }


    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the list hasn't changed, continue with handling back button press
        if (!mInventtHasChanged) {
            super.onBackPressed();
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

    private void saveInfo() {
        String nameString = mProductNameEditText.getText().toString().trim();
        String supplierString = mYourName.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mProductQuantity.getText().toString().trim();
        String emailString = mYourEmail.getText().toString().trim();

        // This converts image into a byteArray so that it can be saved in the database.
        BitmapDrawable bitmapDrawable = (BitmapDrawable) mImageView.getDrawable();
        // Check if an image was selected, if not, then return
        if (bitmapDrawable == null) {
            Toast.makeText(this, "No image selected",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if this is supposed to be a new product
        // and check if the name or price fields in the editor are blank
        if (mCurrentInventoryUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(supplierString)) {
            // If the name or price is blank, then show toast and do not save product info.
            Toast.makeText(this, "Cannot Save !! There are Empty Fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (mCurrentInventoryUri == null && TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(emailString)) {

            Toast.makeText(this, "Cannot Save !! There are Empty Fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap imageBitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] image = stream.toByteArray();

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_NAME, supplierString);
        values.put(InventoryContract.InventoryEntry.COLUMN_EMAIL, emailString);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(InventoryContract.InventoryEntry.COLUMN_IMAGE, image);


        int weight = 0;
        if (!TextUtils.isEmpty(priceString)) {
            weight = parseInt(priceString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, weight);

        if (mCurrentInventoryUri == null) {
            Uri newRowUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
            if (newRowUri == null) {
                // If a new row was not successfully added, display a Toast to that effect
                Toast.makeText(this, R.string.insert_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, display a toast saying "Data saved"
                Toast.makeText(this, R.string.insert_success, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(mCurrentInventoryUri, values, null, null);

            if (rowsUpdated == 0) {
                Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.update_successful, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.InventoryEntry.COLUMN_EMAIL,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_IMAGE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,                       // Parent activity context
                mCurrentInventoryUri,             // The content URI of the table being queried
                projection,                 // The columns to be returned
                null,                       // The selection clause
                null,                       // The selection arguments
                null                        // The sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME));
            String breed = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME));
            int weight = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE));
            String email = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_EMAIL));
            int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY));

            int imageColumnIndex = cursor.getColumnIndex
                    (InventoryContract.InventoryEntry.COLUMN_IMAGE);
            // Update the views on the screen with the values from the database
            mProductNameEditText.setText(name);
            mYourName.setText(breed);
            mPriceEditText.setText(Integer.toString(weight));
            mYourEmail.setText(email);
            mProductQuantity.setText(Integer.toString(quantity));
            byte[] image = cursor.getBlob(imageColumnIndex);

            mImageView.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the
            // current selection.

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameEditText.setText("");
        mYourName.setText("");
        mProductQuantity.setText("");
        mYourEmail.setText("");

        mImageView.setImageBitmap(null);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the data present.
                deleteInfo();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteInfo() {
        // Only perform the delete if this is an existing data.
        if (mCurrentInventoryUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete. Display a toast
                // explaining that deletion failed.
                displayMessage(getString(R.string.editor_delete_failed));
            } else {
                displayMessage(getString(R.string.editor_delete_successful));
            }
        }

        // Close the activity
        finish();
    }

    public void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // This will get an image from one of the image apps like "Gallery".
    public void loadImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, SELECT_FILE);
    }


    // Once image is selected, then need to resize the image to fit the ImageView.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        Bitmap bitmapImage;
        Bitmap resizedImage;

        try {
            // When image is picked
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {

                InputStream stream = getContentResolver().openInputStream(data.getData());
                bitmapImage = BitmapFactory.decodeStream(stream);
                resizedImage = Bitmap.createScaledBitmap(bitmapImage, 75, 75, true);
                stream.close();

                // Set the image in ImageView after decoding the string
                imageView.setImageBitmap(resizedImage);
            } else {
                Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error while uploading", Toast.LENGTH_SHORT).show();
        }
    }

    public void orderButton(View view) {

        String nameString = mProductNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Order:  " + nameString);
        intent.putExtra(Intent.EXTRA_TEXT, "Total cost: " + priceString);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void increase(View view) {
        int numOne = Integer.parseInt(mProductQuantity.getText().toString());
        numOne++;
        mProductQuantity.setText(String.valueOf(numOne));
    }

    public void decrease(View view) {
        int numOne = Integer.parseInt(mProductQuantity.getText().toString());
        numOne--;
        mProductQuantity.setText(String.valueOf(numOne));
    }
}