package de.oth_regensburg.mueller.simon.swimcount.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.List;

import de.oth_regensburg.mueller.simon.swimcount.database.converter.DateConverter;
import de.oth_regensburg.mueller.simon.swimcount.database.dao.UserDao;
import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;

@TypeConverters(DateConverter.class)
@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class UserRoomDatabase extends RoomDatabase {

    // --- DAO ---
    public abstract UserDao userDao();

    // --- SINGLETON ---
    private static UserRoomDatabase INSTANCE;


    public static UserRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (UserRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            UserRoomDatabase.class, "user_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();

                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };


    //AsyncTask, der die Datenbank mit Dummy-Daten füllt
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final UserDao mDao;
        PopulateDbAsync(UserRoomDatabase database) {
            mDao = database.userDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDao.deleteAll();
            List<User> userList = User.createUserListGSON(dummyResponse);
            mDao.insert(userList.get(0));
           return null;
        }
    }


//--- Zum Testen ---

    private static final String dummyResponse = "[\n" + "{\n" +

            "            \"vorname\": \"Frants\",\n" +
            "            \"nachname\": \"Werrilow\",\n" +
            "            \"startnummer\": 844,\n" +
            "            \"strecke\": 3626,\n" +
            "            \"fotoPfad\": \"https://robohash.org/dictaautconsectetur.png?size=250x250&set=set1\"\n" + "} " + "]";
}
