package de.oth_regensburg.mueller.simon.swimcount.repositories;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.oth_regensburg.mueller.simon.swimcount.database.UserRoomDatabase;
import de.oth_regensburg.mueller.simon.swimcount.database.dao.UserDao;
import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;
import de.oth_regensburg.mueller.simon.swimcount.utils.SearchQuery;


public class UserRepository {

    private static final String TAG = UserRepository.class.getSimpleName();

    @SuppressWarnings("FieldCanBeLocal")
    private static final int FRESH_TIMEOUT_IN_MINUTES = 1;


    private static UserRepository sInstance;
    private static UserDao mUserDao;
    private static final Object sLock = new Object();


    /**
     * @return an instance of the {@link UserRepository}.
     * @param context activity-context
     */
    public static UserRepository getInstance(Context context) {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new UserRepository(context);
            }
        }
        return sInstance;
    }

    private UserRepository(Context context) {
        UserRoomDatabase database = UserRoomDatabase.getDatabase(context);
        mUserDao = database.userDao();
        }


    public LiveData<List<User>> getAllUsers() {
        Log.d(TAG, "getAllUsers");

        // return a LiveData directly from the database.
        return mUserDao.getAll();
    }

    //For Update-Worker
    public LiveData<List<User>> getAllRefreshedUsers() {
        return mUserDao.getAllRefreshed(getMaxRefreshTime(new Date()));
    }

    public LiveData<User> getUser(final String query, final SearchQuery.QUERY_TYPE query_type){
        Log.d(TAG, "getUser");

        //Return LiveData directly from the database.
        switch (query_type) {
            case QUERY_NAME:
                return mUserDao.findOneByLastName(query);
            case QUERY_NUMMER:
                return mUserDao.findOneByNumber(Integer.parseInt(query));
            default:
                Log.e(TAG, "kein passender Query-Typ.");
                return null;
        }
    }

    public void update(User user) { new updateAsyncTask(mUserDao).execute(user);}
    private static class updateAsyncTask extends AsyncTask<User, Void, Void> {
        private final UserDao mAsyncTaskDao;

        updateAsyncTask(UserDao dao){ mAsyncTaskDao = dao;}

        @Override
        protected Void doInBackground(User... users) {
            users[0].setLastRefresh(new Date());
            mAsyncTaskDao.updateUser(users[0]);
            Log.d(TAG, "RepositoryAsync Update User");
            return null;
        }
    }

    public void insert (User user) {
        new insertAsyncTask(mUserDao).execute(user);
    }
    private static class insertAsyncTask extends AsyncTask<User, Void, Void> {

        private final UserDao mAsyncTaskDao;

        insertAsyncTask(UserDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final User... users) {
            users[0].setLastRefresh(new Date());
            try{
                mAsyncTaskDao.insert(users[0]);
            }catch(Exception e){
                Log.e(TAG, "insert user failed.");
            }
            Log.d(TAG, "RepositoryAsync Insert User");
            return null;
        }
    }

    public void delete (User user) {
        new deleteAsyncTask(mUserDao).execute(user);
    }
    private static class deleteAsyncTask extends AsyncTask<User, Void, Void> {

        private final UserDao mAsyncTaskDao;

        deleteAsyncTask(UserDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final User... users) {
            mAsyncTaskDao.delete(users[0]);
            Log.d(TAG, "RepositoryAsync Delete User");
            return null;
        }
    }


    /** @return Aktuelle Zeit -1 Minute */
    private Date getMaxRefreshTime(Date currentDate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.MINUTE, -FRESH_TIMEOUT_IN_MINUTES);
        return cal.getTime();
    }

}
