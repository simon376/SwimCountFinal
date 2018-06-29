package de.oth_regensburg.mueller.simon.swimcount.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.Date;
import java.util.List;

import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;

@SuppressWarnings("unused")
@Dao
public interface UserDao {

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAll();

    @Query("SELECT * FROM users WHERE last_name LIKE :last LIMIT 1")
    LiveData<User> findOneByLastName(String last);

    @Query("SELECT * FROM users WHERE startnummer LIKE :startnr LIMIT 1")
    LiveData<User> findOneByNumber(int startnr);

    @Query("SELECT * FROM users WHERE startnummer LIKE :startnr")
    LiveData<List<User>> findByNumber(int startnr);

    @Query("SELECT * FROM users WHERE last_name LIKE :last")
    LiveData<List<User>> findByLastName(String last);


    @Query("SELECT * FROM users WHERE  lastRefresh > :lastRefreshMax LIMIT 1")
    LiveData<List<User>> getAllRefreshed(Date lastRefreshMax);


    @Query("SELECT * FROM users WHERE startnummer = :startnr AND lastRefresh > :lastRefreshMax LIMIT 1")
    LiveData<User> hasUser(String startnr, Date lastRefreshMax);

    @Query("SELECT startnummer FROM users")
    List<Integer> getAllStartnummern();

    @Update
    void updateUser(User user);

    @Update
    void updateUsers(User... users);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(User... users);

    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insert(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM users")
    void deleteAll();
}
