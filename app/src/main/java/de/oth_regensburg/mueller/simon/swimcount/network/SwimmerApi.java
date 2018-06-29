package de.oth_regensburg.mueller.simon.swimcount.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;
import de.oth_regensburg.mueller.simon.swimcount.utils.APIError;
import de.oth_regensburg.mueller.simon.swimcount.utils.SearchQuery;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



/**
 * The Swimmer API client, which uses the {@link SwimmerService} Retrofit APIs.
 */
public class SwimmerApi {

    private static final String TAG = SwimmerApi.class.getSimpleName();

 //   private static final String BASE_URL = "https://my-json-server.typicode.com/simon376/demo/";
      private static final String BASE_URL = "http://a9bb9520.ngrok.io/";   //Muss bei jedem ngrok-Start aktualisiert werden.


    private static SwimmerApi sInstance;
    private final SwimmerService mSwimmerService;
    private static final Object sLock = new Object();

    public Retrofit getRetrofitClient() {
        return retrofitClient;
    }

    private final Retrofit retrofitClient;

    /**
     * @return an instance of the {@link SwimmerApi} client.
     */
    public static SwimmerApi getInstance() {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new SwimmerApi();
            }
        }
        return sInstance;
    }

    private SwimmerApi() {

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        retrofitClient = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mSwimmerService = retrofitClient.create(SwimmerService.class);
    }

    public Call<List<User>> getUsers(String query, SearchQuery.QUERY_TYPE query_type){
        Call<List<User>> call = null;
        switch (query_type) {
            case QUERY_NAME:
                call = mSwimmerService.getUserByLastName(query);
                break;
            case QUERY_NUMMER:
                call = mSwimmerService.getUserByNumber(query);
                break;
            default:
                Log.e(TAG, "keine passendes query.");
        }

        return call;

    }

    public Call<APIError> uploadData(User user){
        return mSwimmerService.updateData(user.getStartnummer(), user);
    }

}
