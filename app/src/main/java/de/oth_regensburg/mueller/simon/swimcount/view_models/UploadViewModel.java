package de.oth_regensburg.mueller.simon.swimcount.view_models;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;
import de.oth_regensburg.mueller.simon.swimcount.utils.UploadWorker;


public class UploadViewModel extends AndroidViewModel {
    private static final String TAG = UploadViewModel.class.getSimpleName();


    private static final long UPLOAD_INTERVAL = 15;     //15 Minuten ist Minimum-Zeit
    private static final String UPLOAD_TAG = "UPLOADING_DATA";

    private final LiveData<List<WorkStatus>> mUploadedWorkStatus;
    private final WorkManager mWorkManager;

    public UploadViewModel(@NonNull Application application) {
        super(application);

        mWorkManager = WorkManager.getInstance();

        mUploadedWorkStatus = mWorkManager.getStatusesByTag(UPLOAD_TAG);
    }

    public void uploadDataRequest(){
        Log.d(TAG, "uploadDataRequest");

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(UploadWorker.class , UPLOAD_INTERVAL, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .addTag(UPLOAD_TAG)
                .build();

        mWorkManager.enqueueUniquePeriodicWork("Upload Data", ExistingPeriodicWorkPolicy.REPLACE, workRequest);

    }


    public LiveData<List<WorkStatus>> getOutputStatuses() { return mUploadedWorkStatus; }

    //Entfernt die Workstatuses für die korrekte Anzeige der Jobs(-Snackbar)
    public void cleanup(){ mWorkManager.pruneWork();}
}
