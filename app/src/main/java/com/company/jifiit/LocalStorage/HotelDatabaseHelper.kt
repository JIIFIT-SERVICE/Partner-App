package com.company.jifiit.LocalStorage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class HotelDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "HotelDB"
        private const val DATABASE_NAME = "jiifit_hotel.db"
        private const val DATABASE_VERSION = 2   // bumped for UNIQUE constraint
        private const val TABLE_HOTEL_MASSAGE = "HotelMassageSelected"

        // Columns
        private const val COLUMN_ID = "id"
        private const val COLUMN_PRODUCT_ID = "product_id"
        private const val COLUMN_PRODUCT_NAME = "product_name"
        private const val COLUMN_ORIGINAL_PRICE = "original_price"
        private const val COLUMN_DISCOUNT_PRICE = "discount_price"
        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_DURATION = "duration"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_DISCOUNT = "discount"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_HOTEL_MASSAGE (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PRODUCT_ID TEXT,
                $COLUMN_PRODUCT_NAME TEXT,
                $COLUMN_ORIGINAL_PRICE REAL,
                $COLUMN_DISCOUNT_PRICE REAL,
                $COLUMN_QUANTITY INTEGER,
                $COLUMN_DURATION TEXT,
                $COLUMN_TYPE TEXT,
                $COLUMN_DISCOUNT REAL,
                UNIQUE($COLUMN_PRODUCT_ID, $COLUMN_TYPE)
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HOTEL_MASSAGE")
        onCreate(db)
    }

    // =========================================================
    // INSERT / UPSERT
    // =========================================================

    fun insertService(
        productId: String,
        productName: String,
        originalPrice: Double,
        discountPrice: Double,
        quantity: Int,
        duration: String,
        type: String,
        discount: Double
    ): Boolean {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_ID, productId)
            put(COLUMN_PRODUCT_NAME, productName)
            put(COLUMN_ORIGINAL_PRICE, originalPrice)
            put(COLUMN_DISCOUNT_PRICE, discountPrice)
            put(COLUMN_QUANTITY, quantity)
            put(COLUMN_DURATION, duration)
            put(COLUMN_TYPE, type)
            put(COLUMN_DISCOUNT, discount)
        }

        // Upsert — if a row with same (product_id, type) exists, replace it.
        val result = db.insertWithOnConflict(
            TABLE_HOTEL_MASSAGE,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )

        Log.d(TAG, "insertService($productId, $type, qty=$quantity) -> rowId=$result")
        return result != -1L
    }

    // =========================================================
    // QUERY
    // =========================================================

    fun getServiceQuantity(serviceId: String, serviceType: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_QUANTITY FROM $TABLE_HOTEL_MASSAGE " +
                    "WHERE $COLUMN_PRODUCT_ID = ? AND $COLUMN_TYPE = ?",
            arrayOf(serviceId, serviceType)
        )

        var quantity = 0
        cursor.use {
            if (it.moveToFirst()) quantity = it.getInt(0)
        }
        Log.d(TAG, "getServiceQuantity($serviceId, $serviceType) -> $quantity")
        return quantity
    }

    fun isServiceStored(productId: String, serviceType: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLE_HOTEL_MASSAGE " +
                    "WHERE $COLUMN_PRODUCT_ID = ? AND $COLUMN_TYPE = ? LIMIT 1",
            arrayOf(productId, serviceType)
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun isDatabaseEmpty(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_HOTEL_MASSAGE", null)
        var isEmpty = true
        cursor.use {
            if (it.moveToFirst()) isEmpty = it.getInt(0) == 0
        }
        return isEmpty
    }

    // =========================================================
    // UPDATE
    // =========================================================

    fun updateServiceQuantity(
        productId: String,
        serviceType: String,
        quantity: Int
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put(COLUMN_QUANTITY, quantity) }

        val result = db.update(
            TABLE_HOTEL_MASSAGE,
            values,
            "$COLUMN_PRODUCT_ID = ? AND $COLUMN_TYPE = ?",
            arrayOf(productId, serviceType)
        )

        Log.d(TAG, "updateServiceQuantity($productId, $serviceType, qty=$quantity) -> rows=$result")
        return result > 0
    }

    // =========================================================
    // DELETE
    // =========================================================

    fun deleteService(productId: String, serviceType: String): Boolean {
        val db = writableDatabase
        val result = db.delete(
            TABLE_HOTEL_MASSAGE,
            "$COLUMN_PRODUCT_ID = ? AND $COLUMN_TYPE = ?",
            arrayOf(productId, serviceType)
        )

        Log.d(TAG, "deleteService($productId, $serviceType) -> rows=$result")
        return result > 0
    }

    /** Fallback delete by product_id only (in case type is inconsistent). */
    fun deleteServiceByProductId(productId: String): Boolean {
        val db = writableDatabase
        val result = db.delete(
            TABLE_HOTEL_MASSAGE,
            "$COLUMN_PRODUCT_ID = ?",
            arrayOf(productId)
        )
        Log.d(TAG, "deleteServiceByProductId($productId) -> rows=$result")
        return result > 0
    }

    fun clearCart() {
        val db = writableDatabase
        db.delete(TABLE_HOTEL_MASSAGE, null, null)
        Log.d(TAG, "clearCart()")
    }

    // =========================================================
    // TOTALS
    // =========================================================

    fun getTotalDiscountPrice(): Double {
        val db = readableDatabase
        val query = """
            SELECT SUM($COLUMN_DISCOUNT_PRICE * $COLUMN_QUANTITY)
            FROM $TABLE_HOTEL_MASSAGE
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        var total = 0.0
        cursor.use {
            if (it.moveToFirst() && !it.isNull(0)) total = it.getDouble(0)
        }
        return total
    }

    fun getCartDiscountedTotal(): Double = getTotalDiscountPrice()

    fun getCartOriginalTotal(): Double {
        val db = readableDatabase
        val query = """
            SELECT SUM($COLUMN_ORIGINAL_PRICE * $COLUMN_QUANTITY)
            FROM $TABLE_HOTEL_MASSAGE
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        var total = 0.0
        cursor.use {
            if (it.moveToFirst() && !it.isNull(0)) total = it.getDouble(0)
        }
        return total
    }

    fun getCartItemCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_HOTEL_MASSAGE", null)
        var count = 0
        cursor.use {
            if (it.moveToFirst()) count = it.getInt(0)
        }
        return count
    }

    // =========================================================
    // DATA CLASSES
    // =========================================================

    data class SavedService(
        val productId: String,
        val duration: String,
        val quantity: Int,
        val discountPrice: Double,
        val originalPrice: Double
    )

    fun getAllSavedServices(): List<SavedService> {
        val list = mutableListOf<SavedService>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_PRODUCT_ID, $COLUMN_DURATION, $COLUMN_QUANTITY, " +
                    "$COLUMN_DISCOUNT_PRICE, $COLUMN_ORIGINAL_PRICE FROM $TABLE_HOTEL_MASSAGE",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    SavedService(
                        productId = it.getString(0),
                        duration = it.getString(1),
                        quantity = it.getInt(2),
                        discountPrice = it.getDouble(3),
                        originalPrice = it.getDouble(4)
                    )
                )
            }
        }
        return list
    }

    data class CartItem(
        val id: Int,
        val productId: String,
        val productName: String,
        val originalPrice: Double,
        val discountPrice: Double,
        val quantity: Int,
        val duration: String,
        val type: String,
        val discount: Double
    )

    fun getAllCartItems(): MutableList<CartItem> {
        val list = mutableListOf<CartItem>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                $COLUMN_ID,
                $COLUMN_PRODUCT_ID,
                $COLUMN_PRODUCT_NAME,
                $COLUMN_ORIGINAL_PRICE,
                $COLUMN_DISCOUNT_PRICE,
                $COLUMN_QUANTITY,
                $COLUMN_DURATION,
                $COLUMN_TYPE,
                $COLUMN_DISCOUNT
            FROM $TABLE_HOTEL_MASSAGE
            ORDER BY $COLUMN_ID ASC
            """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    CartItem(
                        id = it.getInt(0),
                        productId = it.getString(1),
                        productName = it.getString(2),
                        originalPrice = it.getDouble(3),
                        discountPrice = it.getDouble(4),
                        quantity = it.getInt(5),
                        duration = it.getString(6),
                        type = it.getString(7),
                        discount = it.getDouble(8)
                    )
                )
            }
        }
        return list
    }



    // =========================================================
// TOTAL MASSAGE DURATION (in minutes)
// =========================================================

    /**
     * Returns total massage duration in minutes.
     * Parses the numeric value from COLUMN_DURATION (e.g. "30 min" -> 30)
     * and multiplies by quantity, then sums across all rows.
     */
    fun getTotalMassageDuration(): Int {
        val db = readableDatabase
        var totalDuration = 0

        val cursor = db.rawQuery(
            "SELECT $COLUMN_DURATION, $COLUMN_QUANTITY FROM $TABLE_HOTEL_MASSAGE",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val durationStr = it.getString(0) ?: ""
                val quantity = it.getInt(1)

                val minutes = extractMinutes(durationStr)
                totalDuration += minutes * quantity
            }
        }

        Log.d(TAG, "getTotalMassageDuration() -> $totalDuration min")
        return totalDuration
    }

    /**
     * Extracts the numeric minutes from a duration string.
     * Handles: "30 min", "30min", "30", "1 hour", "1h", "1 hr 30 min"
     */
    private fun extractMinutes(duration: String): Int {
        if (duration.isBlank()) return 0

        val lower = duration.lowercase().trim()

        // If it contains "hour" or "hr" or "h", handle hours + optional minutes
        val hourRegex = Regex("""(\d+)\s*(hour|hr|h)""")
        val minRegex  = Regex("""(\d+)\s*(min|m)?""")

        var total = 0

        // Extract hours
        hourRegex.find(lower)?.let {
            total += (it.groupValues[1].toIntOrNull() ?: 0) * 60
        }

        // Extract minutes (only if "min" present, or if no hour matched)
        if (lower.contains("min")) {
            minRegex.find(lower)?.let {
                total += it.groupValues[1].toIntOrNull() ?: 0
            }
        } else if (total == 0) {
            // Pure number like "30"
            lower.filter { it.isDigit() }.toIntOrNull()?.let { total = it }
        }

        return total
    }
}