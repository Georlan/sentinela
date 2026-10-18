package dev.georlan.sentinela

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class SavedPosition(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val recordedAtMs: Long
)

class LocationStore(context: Context) :
    SQLiteOpenHelper(context, "sentinela_positions.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE positions(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                accuracy_m REAL NOT NULL,
                recorded_at_ms INTEGER NOT NULL
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        error("Database migration required from $oldVersion to $newVersion")
    }

    fun save(point: SavedPosition) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.insertOrThrow("positions", null, ContentValues().apply {
                put("latitude", point.latitude)
                put("longitude", point.longitude)
                put("accuracy_m", point.accuracyMeters)
                put("recorded_at_ms", point.recordedAtMs)
            })
            // Limite local: evita crescimento ilimitado sem internet.
            db.execSQL(
                "DELETE FROM positions WHERE id NOT IN " +
                    "(SELECT id FROM positions ORDER BY id DESC LIMIT 2000)"
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun latest(): SavedPosition? {
        readableDatabase.query(
            "positions", arrayOf("latitude", "longitude", "accuracy_m", "recorded_at_ms"),
            null, null, null, null, "id DESC", "1"
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return SavedPosition(
                cursor.getDouble(0), cursor.getDouble(1),
                cursor.getFloat(2), cursor.getLong(3)
            )
        }
    }

    fun count(): Long {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM positions", null).use {
            it.moveToFirst()
            return it.getLong(0)
        }
    }
}
