package de.oth_regensburg.mueller.simon.swimcount.view_models;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;
import de.oth_regensburg.mueller.simon.swimcount.repositories.UserRepository;
import de.oth_regensburg.mueller.simon.swimcount.utils.SearchQuery;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository mRepository;

    public UserViewModel(@NonNull Application application) {
        super(application);
        mRepository = UserRepository.getInstance(application);
    }

    public LiveData<List<User>> getAllUsers(){ return mRepository.getAllUsers();}

    public LiveData<User> getUser(String query, SearchQuery.QUERY_TYPE query_type) { return mRepository.getUser(query, query_type);}

    public LiveData<List<User>> getAllChangedUsers() { return  mRepository.getAllRefreshedUsers();}

    public void insert(User user) { mRepository.insert(user);}

    public void update(User user) { mRepository.update(user);}

    public void delete(User user) { mRepository.delete(user);}
}
