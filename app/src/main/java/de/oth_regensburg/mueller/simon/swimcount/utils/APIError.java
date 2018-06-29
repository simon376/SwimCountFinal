package de.oth_regensburg.mueller.simon.swimcount.utils;

import java.io.IOException;
import java.lang.annotation.Annotation;

import de.oth_regensburg.mueller.simon.swimcount.network.SwimmerApi;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

@SuppressWarnings("unused")
public class APIError {

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static APIError parseError(Response<?> response) {
        if(response.errorBody() != null) {
            Converter<ResponseBody, APIError> converter =
                    SwimmerApi.getInstance().getRetrofitClient()
                            .responseBodyConverter(APIError.class, new Annotation[0]);


            try {
                return converter.convert(response.errorBody());
            } catch (IOException e) {
                return new APIError();
            }
        }
        return new APIError();
    }
}
