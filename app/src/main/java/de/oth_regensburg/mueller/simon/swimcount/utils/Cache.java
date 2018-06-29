package de.oth_regensburg.mueller.simon.swimcount.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;

public class Cache {

    private static final String TAG = Cache.class.getSimpleName();

    private static final List<User> cachedUsers = new ArrayList<>();


    public static List<User> getCachedUsers() {
        return cachedUsers;
    }

    public static void setCachedUsers(List<User> users) {
        Log.d(TAG, "setCachedUsers");
        cachedUsers.addAll(users);
        if(cachedUsers.size() > 1)
            removeDuplicates();
    }

    public static int getCacheSize(){
        return cachedUsers.size();
    }

    public static void clearCache(){
        cachedUsers.clear();
        Log.d(TAG, "Cache cleared.");
    }

    public static void remove(User user){
        cachedUsers.remove(user);
    }


    private static void removeDuplicates(){
        for(int i = 0; i < cachedUsers.size()-1; i++){
            for(int j = i+1; j < cachedUsers.size(); j++){
                User u1 = cachedUsers.get(i), u2 = cachedUsers.get(j);

                if(u1.getStartnummer() == u2.getStartnummer()){
                    int compare = u1.getLastRefresh().compareTo(u2.getLastRefresh());
                    if(compare > 0){ //größer, also vor kurzem
                        cachedUsers.remove(j);
                    }
                    else{ //kleiner gleich, also älter
                        cachedUsers.remove(i);
                    }
                }
            }
        }
    }
}
