package faceapi.userdb;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

public class UserDbCreator {

    public static UserDatabase create(Context context, String dbName) {
        return Room.databaseBuilder(context.getApplicationContext(), UserDatabase.class, dbName).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).allowMainThreadQueries().build();
    }

//    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(SupportSQLiteDatabase database) {
//        }
//    };
//
//    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
//        @Override
//        public void migrate(SupportSQLiteDatabase database) {
//        }
//    };

}
