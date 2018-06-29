package de.oth_regensburg.mueller.simon.swimcount.network;

import java.util.List;

import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;
import de.oth_regensburg.mueller.simon.swimcount.utils.APIError;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * A {@link retrofit2.Retrofit} interface to the swimmers API.
 */
interface SwimmerService {
    // Request method and URL specified in the annotation

    //List<User>, da API Array statt Objekt zurück liefert
    @GET("users")
    Call<List<User>> getUserByNumber(@Query("startnummer") String startnummer);

    //List<User>, da API Array statt Objekt zurück liefert
    @GET("users")
    Call<List<User>> getUserByLastName(@Query("nachname") String nachname);


    @Headers("Content-Type: application/json")
    @PUT("users/{startnummer}")
    Call<APIError> updateData(@Path("startnummer") int startnummer, @Body User user );


}
