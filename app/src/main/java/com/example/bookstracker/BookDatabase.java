package com.example.bookstracker;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Book.class, Quote.class}, version = 7)
public abstract class BookDatabase extends RoomDatabase {

    private static BookDatabase instance;
    public abstract BookDao bookDao();
    public abstract QuoteDao quoteDao(); // Adăugăm noul DAO

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `quotes` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`bookId` INTEGER NOT NULL, " +
                    "`text` TEXT, " +
                    "`timestamp` INTEGER NOT NULL)");
        }
    };
    public static synchronized BookDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            BookDatabase.class, "book_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
