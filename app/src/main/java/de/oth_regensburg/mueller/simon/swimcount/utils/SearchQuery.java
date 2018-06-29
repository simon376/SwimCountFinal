package de.oth_regensburg.mueller.simon.swimcount.utils;

import android.util.Log;

import java.util.regex.Pattern;

public abstract class SearchQuery {
    private static final String TAG = SearchQuery.class.getSimpleName();


    public enum QUERY_TYPE { QUERY_NUMMER, QUERY_NAME, QUERY_ERROR }

    public static QUERY_TYPE examineQuery(String query){
        if(Pattern.matches("(?<!\\S)\\d+(?!\\S)", query)){
            Log.d(TAG, "regex Number");
            return QUERY_TYPE.QUERY_NUMMER;
        }
        else if(Pattern.matches("(?<!\\S)\\p{Alpha}+(?!\\S)", query)){
            Log.d(TAG, "regex name");
            return QUERY_TYPE.QUERY_NAME;
        }
        else{
            Log.d(TAG, "examineQuery: something else");
            return QUERY_TYPE.QUERY_ERROR;
        }

    }

}
