package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;

import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY;

/**
 * Created by Hp on 07-04-2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int supplierColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_QUANTITY);
        int iDColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);

        // Read the pet attributes from the Cursor for the current pet
        String name = cursor.getString(nameColumnIndex);
        int supplierName = cursor.getInt(supplierColumnIndex);
        final int quantityTotal = cursor.getInt(quantityColumnIndex);
        final String rowId = cursor.getString(iDColumnIndex);


        String str = "$" + String.valueOf(supplierName);
        nameTextView.setText(name);
        summaryTextView.setText(str);
        quantityTextView.setText(String.valueOf(quantityTotal));

        Button buyMoreProducts = (Button) view.findViewById(R.id.buyOne);
        buyMoreProducts.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantityTotal > 0) {
                    Integer itemId = Integer.parseInt(rowId);
                    Integer remainingStock = quantityTotal - 1;
                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, remainingStock);
                    Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, itemId);
                    resolver.update(currentItemUri, values, null, null);
                }
            }
        });

        Button addMoreProducts = (Button) view.findViewById(R.id.addMore);
        addMoreProducts.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (quantityTotal > 0) {
                    Integer itemId = Integer.parseInt(rowId);
                    Integer remainingStock = quantityTotal + 1;
                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, remainingStock);
                    Uri currentItemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, itemId);
                    resolver.update(currentItemUri, values, null, null);
                }
            }
        });
    }
}