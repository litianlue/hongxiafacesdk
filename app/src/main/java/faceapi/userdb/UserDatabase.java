package faceapi.userdb;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {UserInfo.class}, version = 1, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {

    private static UserDatabase sDb;


    public static UserDatabase create(Context context, String dbName) {
        if (sDb == null) {
            sDb = UserDbCreator.create(context, dbName);//Room.databaseBuilder(context.getApplicationContext(), UserDatabase.class, "user.db").build();
        }
        return sDb;
    }

    public static void destroy() {
        sDb = null;
    }

    public abstract UserInfoDao getUserInfoDao();

}
