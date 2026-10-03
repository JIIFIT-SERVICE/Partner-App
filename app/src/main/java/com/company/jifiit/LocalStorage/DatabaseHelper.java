package com.company.jifiit.LocalStorage;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "jiifit";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    private static final String TABLE_NAME = "Services";

    // Table Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_PRODUCT_NAME = "product_name";
    private static final String COLUMN_ORIGINAL_PRICE = "original_price";
    private static final String COLUMN_DISCOUNT_PRICE = "discount_price";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_DISCOUNT = "discount";
    private static final String COLUMN_ORDER_ID = "order_id";




    private static final String HOTEL_MASSAGE_TABLE = "HotelMassageSelected";

    private static final String HM_ID = "id";
    private static final String HM_PRODUCT_ID = "product_id";
    private static final String HM_PRODUCT_NAME = "product_name";
    private static final String HM_ORIGINAL_PRICE = "original_price";
    private static final String HM_DISCOUNT_PRICE = "discount_price";
    private static final String HM_QUANTITY = "quantity";
    private static final String HM_DURATION = "duration";
    private static final String HM_TYPE = "type";
    private static final String HM_DISCOUNT = "discount";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table with required columns
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PRODUCT_ID + " TEXT, " +
                COLUMN_PRODUCT_NAME + " TEXT, " +
                COLUMN_ORIGINAL_PRICE + " REAL, " +
                COLUMN_DISCOUNT_PRICE + " REAL, " +
                COLUMN_QUANTITY + " INTEGER, " +
                COLUMN_DURATION + " TEXT, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_DISCOUNT + " REAL, " +
                COLUMN_ORDER_ID + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop table if it exists and recreate it
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert data method
    public boolean insertService(String productId, String productName, String originalPrice, String discountPrice, int quantity,
                                 String duration, String type, String discount, String orderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PRODUCT_ID, productId);
        contentValues.put(COLUMN_PRODUCT_NAME, productName);
        contentValues.put(COLUMN_ORIGINAL_PRICE, originalPrice);
        contentValues.put(COLUMN_DISCOUNT_PRICE, discountPrice);
        contentValues.put(COLUMN_QUANTITY, quantity);
        contentValues.put(COLUMN_DURATION, duration);
        contentValues.put(COLUMN_TYPE, type);
        contentValues.put(COLUMN_DISCOUNT, discount);
        contentValues.put(COLUMN_ORDER_ID, orderId);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;  // returns true if data was inserted successfully
    }

    // Update service by product name
    public boolean updateServiceQuantitys(String productName, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_QUANTITY, newQuantity);

        int result = db.update(TABLE_NAME, contentValues, COLUMN_PRODUCT_NAME + " = ?", new String[]{productName});
        return result > 0; // returns true if the service was updated successfully
    }
    public void updateServiceQuantity(String productId, String orderId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_QUANTITY, newQuantity);

        db.update(TABLE_NAME, contentValues, COLUMN_PRODUCT_ID + " = ? AND " + COLUMN_ORDER_ID + " = ?",
                new String[]{productId, orderId});
    }

    // Check if a service exists by product name
    public boolean serviceExists(String productId, String orderId, String type) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to check if a record with the given productName and orderId exists
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_PRODUCT_ID + " = ? AND " + COLUMN_ORDER_ID + " = ? AND " + COLUMN_TYPE + " = ?";

        // Execute the query with both productName and orderId as parameters
        Cursor cursor = db.rawQuery(query, new String[]{productId, orderId,type});

        // Check if any rows were returned
        boolean exists = cursor.getCount() > 0;

        // Close the cursor after the query
        cursor.close();

        return exists;
    }

    // Method to calculate the sum of discount prices by order ID
    @SuppressLint("Range")
    public double getTotalPriceByOrderId(String orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalPrice = 0;

        // SQL query to select discount price and quantity for the given order ID
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_DISCOUNT_PRICE + ", " + COLUMN_QUANTITY + " FROM " + TABLE_NAME + " WHERE " + COLUMN_ORDER_ID + " = ?", new String[]{orderId});

        // Loop through all matching rows and calculate the total price
        if (cursor.moveToFirst()) {
            do {
                double discountPrice = cursor.getDouble(cursor.getColumnIndex(COLUMN_DISCOUNT_PRICE));
                int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
                totalPrice += discountPrice * quantity;  // Calculate total for this row
            } while (cursor.moveToNext());
        }

        cursor.close();
        return totalPrice;  // Return the summed total
    }
    @SuppressLint("Range")
    public double getTotalOriginalPriceByOrderId(String orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalPrice = 0;

        // SQL query to select discount price and quantity for the given order ID
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ORIGINAL_PRICE + ", " + COLUMN_QUANTITY + " FROM " + TABLE_NAME + " WHERE " + COLUMN_ORDER_ID + " = ?", new String[]{orderId});

        // Loop through all matching rows and calculate the total price
        if (cursor.moveToFirst()) {
            do {
                double originalPrice = cursor.getDouble(cursor.getColumnIndex(COLUMN_ORIGINAL_PRICE));
                int quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
                totalPrice += originalPrice * quantity;  // Calculate total for this row
            } while (cursor.moveToNext());
        }

        cursor.close();
        return totalPrice;  // Return the summed total
    }


    public boolean deleteService(String productId, String orderId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Use both productId and orderId in the whereClause
        int result = db.delete(TABLE_NAME, COLUMN_PRODUCT_ID + " = ? AND " + COLUMN_ORDER_ID + " = ?", new String[]{productId, orderId});

        return result > 0;  // returns true if a row was deleted successfully
    }

    public boolean deleteProducts(String orderId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Use both productId and orderId in the whereClause
        int result = db.delete(TABLE_NAME, COLUMN_ORDER_ID + " = ?", new String[]{orderId});

        return result > 0;  // returns true if a row was deleted successfully
    }

    public boolean fetchServices(String orderId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to check if a record with the given productName and orderId exists
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ORDER_ID + " = ?";

        // Execute the query with both productName and orderId as parameters
        Cursor cursor = db.rawQuery(query, new String[]{orderId});

        // Check if any rows were returned
        boolean exists = cursor.getCount() > 0;

        // Close the cursor after the query
        cursor.close();

        return exists;
    }



    @SuppressLint("Range")
    public int getQuantityByProductIdAndOrderId(String productId, String orderId,String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_QUANTITY + " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_PRODUCT_ID + " = ? AND " + COLUMN_ORDER_ID + " = ? AND " + COLUMN_TYPE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{productId, orderId,type});

        int quantity = 0;
        if (cursor.moveToFirst()) {
            quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY));
        }

        cursor.close();
        return quantity;
    }

    public Cursor fetchAddedProduct(String orderId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch data with the given orderId
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ORDER_ID + " = ?";

        // Execute the query and return the cursor
        return db.rawQuery(query, new String[]{orderId});
    }

    @SuppressLint("Range")
    public int getCartItemCount(String orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_QUANTITY + ") as total FROM " + TABLE_NAME +
                " WHERE " + COLUMN_ORDER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{orderId});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndex("total"));
        }
        cursor.close();
        return count;
    }






}

