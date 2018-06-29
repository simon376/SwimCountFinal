package de.oth_regensburg.mueller.simon.swimcount.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Date;

import de.oth_regensburg.mueller.simon.swimcount.R;
import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;
import de.oth_regensburg.mueller.simon.swimcount.utils.CameraSourcePreview;

/** Auf Basis des Mobile Vision Samples überarbeiteter QR-Code-Reader*/
public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = QRScannerActivity.class.getSimpleName();


    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private Button mButton;
    private TextView mTextView;
    public static final String BarcodeObject = "Barcode";
    private Barcode mBarcode;

    private User foundUser;

    private boolean noBarcodeYet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);


        mPreview = findViewById(R.id.preview);
        mButton = findViewById(R.id.qr_confirm_button);
        mTextView = findViewById(R.id.tv_qr_data);

        updateButtonAndTextAppearance(false);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(noBarcodeYet){
                    Log.e(TAG, "weird error occurred: OnClick without recognized Barcode.");
                }
                else {
                    String barcodeStartnummer = String.valueOf(foundUser.getStartnummer());

                    Intent data = new Intent();
                    data.putExtra(BarcodeObject, barcodeStartnummer);
                    setResult(CommonStatusCodes.SUCCESS, data);
                    Toast.makeText(QRScannerActivity.this, R.string.barcode_success, Toast.LENGTH_SHORT).show();

                    finish();
                }
            }
        });

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

        noBarcodeYet = true;

    }


    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w("DEBUG", "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        final Activity thisActivity = this;
        View topLayout = findViewById(R.id.topLayout);

        // Permission is not granted
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    RC_HANDLE_CAMERA_PERM);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        topLayout.setOnClickListener(listener);
        Snackbar.make(topLayout, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();

    }


    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource() {
        Context context = getApplicationContext();


        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> barcodes = detections.getDetectedItems();


                    if(barcodes.size() != 0){

                        // DO it Only Once
                        if(noBarcodeYet){

                            mBarcode = barcodes.valueAt(0);
                            foundUser = getUserFromBarcode(mBarcode.rawValue);
                             Log.d(TAG, "QR-Code: " + mBarcode.displayValue);

                            QRScannerActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                updateButtonAndTextAppearance(true); }});

                        }
                        noBarcodeYet = false;
                    }
                }
        });

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w("DEBUG", "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w("DEBUG", getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(640, 460)
                .setAutoFocusEnabled(true)
                .setRequestedFps(30.0f);

        mCameraSource = builder.build();


    }


    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }


    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d("DEBUG", "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG", "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e("DEBUG", "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(QRScannerActivity.this, R.string.barcode_failure, Toast.LENGTH_SHORT).show();
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name))
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Log.e("DEBUG", "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }


    @SuppressLint("DefaultLocale")
    private void updateButtonAndTextAppearance(boolean found){
        if(found){
            mButton.setEnabled(true);
            mButton.setBackgroundColor(getResources().getColor(R.color.secondaryColor));
            mButton.setTextColor(getResources().getColor(R. color.secondaryTextColor));

            mTextView.setText(String.format("%d %s", foundUser.getStartnummer(), foundUser.getName()));
            mTextView.setVisibility(View.VISIBLE);

        }
        else{
            mButton.setEnabled(false);
            mButton.setBackgroundResource(0);
            mButton.setTextColor(getResources().getColor(R. color.primaryTextColor));

            mTextView.setText("");
            mTextView.setVisibility(View.INVISIBLE);
        }
    }

    //Aufbau QR-Code: startnummer,vorname,nachname   123,Max,Mustermann
    private User getUserFromBarcode(String barcodeData) {
        String[] extractedData = barcodeData.split(",");

        int startnummer = Integer.parseInt(extractedData[0]);

        return new User(startnummer, extractedData[1], extractedData[2], 0, null, new Date());
    }

}
