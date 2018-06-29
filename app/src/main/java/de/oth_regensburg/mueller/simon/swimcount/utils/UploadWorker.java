package de.oth_regensburg.mueller.simon.swimcount.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.work.Worker;
import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;
import de.oth_regensburg.mueller.simon.swimcount.network.SwimmerApi;
import retrofit2.Response;

/**
 * Uploads an image to SwimmerDB using the {@link SwimmerApi}.
 */
public class UploadWorker extends Worker {

    private static final String TAG = UploadWorker.class.getSimpleName();

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: Started to work");
        List<User> userList = new ArrayList<>(Cache.getCachedUsers());
        SwimmerApi swimmerApi = SwimmerApi.getInstance();

        if(userList.isEmpty()){
            Log.d(TAG, "nothing to upload");
            return Result.SUCCESS;
        }

        for (User user : userList) {
            try {
                Response<APIError> response = swimmerApi.uploadData(user).execute();
                //Check if upload succeeded.
                if (response.isSuccessful()) {
                    Log.d(TAG, "success");
                    Cache.remove(user);
                } else {
                    //parse the response body
                    APIError error = APIError.parseError(response);
                    String errorMessage = error.getCode() + " " + error.getMessage();
                    Log.e(TAG, errorMessage);
                    backgroundToast(getApplicationContext(), errorMessage);
                    return Result.FAILURE;
                }

            } catch (Exception e) {
                e.printStackTrace();
                String message = String.format("Failed to upload user with name %s", user.getName());
                backgroundToast(getApplicationContext(), message);
                Log.e(TAG, message);
                return Result.FAILURE;
            }
        }

        return Result.SUCCESS;
    }


    private static void backgroundToast(final Context context, final String msg) {
        if (context != null && msg != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
