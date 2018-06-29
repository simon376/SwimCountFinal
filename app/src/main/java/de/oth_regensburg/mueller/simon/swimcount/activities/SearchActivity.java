package de.oth_regensburg.mueller.simon.swimcount.activities;

import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.oth_regensburg.mueller.simon.swimcount.R;
import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;
import de.oth_regensburg.mueller.simon.swimcount.network.SwimmerApi;
import de.oth_regensburg.mueller.simon.swimcount.utils.SearchQuery;
import de.oth_regensburg.mueller.simon.swimcount.view_models.UserViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//Geändert: Sucht nicht mehr online, sondern lokal über das ViewModel, da DB ^= online
public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = SearchActivity.class.getSimpleName();

    private ProgressBar progressBar;

    public static final String SearchObject = "Search";

    //SEARCH RESPONSE
    private List<User> mUserList;

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    private SearchAdapter mAdapter;
    private SwimmerApi swimmerApi;
    private UserViewModel mUserViewModel;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.search_header);


        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        swimmerApi = SwimmerApi.getInstance();

        mUserList = new ArrayList<>();
        mAdapter = new SearchAdapter(this, mUserList);

        ListView listView = findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);


        progressBar = findViewById(R.id.search_progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        SearchView searchView =  findViewById(R.id.searchView);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchQuery.QUERY_TYPE query_type = SearchQuery.examineQuery(query);
                if(query_type != SearchQuery.QUERY_TYPE.QUERY_ERROR)
                    doMySearch(query, query_type);
                else
                    Toast.makeText(SearchActivity.this, R.string.entered_text_not_ok, Toast.LENGTH_SHORT).show();
                    //Show hint
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null) {
                SearchQuery.QUERY_TYPE query_type = SearchQuery.examineQuery(query);
                if(query_type != SearchQuery.QUERY_TYPE.QUERY_ERROR){
                    searchView.setQueryHint(query);
                    doMySearch(query, query_type);

                }
            }
            //Wenn null: kein Extra mitgesendet -> kein QR-Code sondern von MainActivity.
        }


    }

    private void doMySearch(String searchQuery, SearchQuery.QUERY_TYPE query_type) {
        Log.d(TAG, "doMySearch");
        progressBar.setVisibility(View.VISIBLE);

        swimmerApi.getUsers(searchQuery, query_type).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                progressBar.setVisibility(View.INVISIBLE);
                int statusCode = response.code();
                if (response.isSuccessful()){
                    List<User> list = response.body();
                    if(list != null){
                        mUserList.addAll(list);

                        mAdapter.notifyDataSetChanged();
                        Log.d(TAG, "API getUserlist request response: " + String.valueOf(statusCode));
                    }
                    else{
                        Toast.makeText(SearchActivity.this, R.string.http_response_error, Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    String message = getString(R.string.http_response_error_parameter, statusCode);
                    Toast.makeText(SearchActivity.this, message, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, message);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.INVISIBLE);
                //Transport level errors such as no internet etc.
                Log.e(TAG, "API getUser request failed.");
                Toast.makeText(SearchActivity.this, R.string.http_connection_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Wählt User aus (Startnummer)
     * gibt Startnummer zurück
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        User wantedUser = mAdapter.getItem(position);
        //Benutzer in DB einpflegen.
        mUserViewModel.insert(wantedUser);

        Intent intent = new Intent();
        //Dann kann in der MainActivity hingescrollt werden.
        intent.putExtra(SearchObject, Objects.requireNonNull(wantedUser).getStartnummer());
        setResult(CommonStatusCodes.SUCCESS);
        finish();

    }



    class SearchAdapter extends ArrayAdapter<User> {
        SearchAdapter(Context context, List<User> users) {
            super(context, 0, users);
        }
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            User user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            // Lookup view for data population
            TextView tvName = convertView.findViewById(android.R.id.text1);
            TextView tvNummer = convertView.findViewById(android.R.id.text2);
            // Populate the data into the template view using the data object
            tvName.setText(Objects.requireNonNull(user).getName());
            tvNummer.setText(String.valueOf(user.getStartnummer()));
            // Return the completed view to render on screen
            return convertView;
        }

    }

}
