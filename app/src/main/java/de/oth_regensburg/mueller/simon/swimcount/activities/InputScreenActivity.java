package de.oth_regensburg.mueller.simon.swimcount.activities;

import android.app.ActivityOptions;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.Date;
import java.util.List;

import de.oth_regensburg.mueller.simon.swimcount.R;
import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;
import de.oth_regensburg.mueller.simon.swimcount.utils.GlideApp;
import de.oth_regensburg.mueller.simon.swimcount.utils.SearchQuery;
import de.oth_regensburg.mueller.simon.swimcount.view_models.UserViewModel;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import tk.wasdennnoch.progresstoolbar.ProgressToolbar;

//Wird bei Klick auf User aufgerufen, um Wert manuell zu ändern.
public class InputScreenActivity extends AppCompatActivity {
    private static final String TAG = InputScreenActivity.class.getSimpleName();

    private User currentUser;
    private ImageView mUserThumb;
    private String mCurrentPhotoPath;
    private TextView mTitle;
    private EditText mStartnummer;
    private EditText mVorname;
    private EditText mNachname;
    private EditText mStrecke;
    private TextInputLayout textInputLayout;
    private Button cancelButton;
    private Button saveButton;
    private FloatingActionButton mFAB;


    private File takenImage;

    private UserViewModel userViewModel;

    private Intent imageFullscreenIntent;


    private boolean newImagePicked = false;


    private static final int MIN_DISTANCE = 0;
    private static final int MAX_DISTANCE = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_screen);

        initViews();

        newImagePicked = false;

        // --- PROCESS INTENT ---

        Intent intent = getIntent();
        String startnummer = intent.getStringExtra("NUMBER");

        //Transition
        postponeEnterTransition();
        String tnImage = intent.getStringExtra("tnImage");

        mCurrentPhotoPath = intent.getStringExtra("imageLink");

        mUserThumb.setTransitionName(tnImage);

        currentUser = new User();



        EasyImage.configuration(this)
                .setImagesFolderName("userpictures")
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
                .setAllowMultiplePickInGallery(false);


        /// --- VIEWMODEL ---

        userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        SearchQuery.QUERY_TYPE query_type = SearchQuery.QUERY_TYPE.QUERY_NUMMER;

        userViewModel.getUser(startnummer, query_type).observe(this, new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if(user != null){
                    currentUser = user;
                    updateViews();
                }
            }
        });


        // --- ONCLICKLISTENERS ---
        initClickListeners();
    }

    private void initViews(){
        ProgressToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.hideProgress();
        mTitle = findViewById(R.id.toolbar_title);
        mStartnummer = findViewById(R.id.et_is_startnummer);
        mVorname = findViewById(R.id.et_is_vorname);
        mNachname = findViewById(R.id.et_is_nachname);
        mStrecke = findViewById(R.id.et_is_neue_strecke);
        cancelButton = findViewById(R.id.button_is_cancel);
        saveButton = findViewById(R.id.button_is_save);
        mUserThumb = findViewById(R.id.iv_is_user_image);
        mFAB = findViewById(R.id.fab_camera);
        textInputLayout = findViewById(R.id.til_et_neue_strecke);
        textInputLayout.setError(null);
    }

    private void initClickListeners(){

        imageFullscreenIntent = new Intent(getApplicationContext(), ImageViewActivity.class);

        mUserThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserThumb.setTransitionName("inputThumb");
                String transitionName = mUserThumb.getTransitionName();
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(InputScreenActivity.this, mUserThumb, transitionName);

                imageFullscreenIntent.putExtra("transitionName", transitionName);
                imageFullscreenIntent.putExtra("imageLink", mCurrentPhotoPath);

                startActivity(imageFullscreenIntent, options.toBundle());
            }
        });


        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openChooserWithGallery(InputScreenActivity.this, "Benutzerfoto auswählen", 0);
            }
        });


        //Strecke speichern.
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String streckeString =  mStrecke.getText().toString();
                if(checkStringInput(streckeString)){
                    Toast.makeText(view.getContext(), "Änderung gespeichert", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        //Activity beenden. nichts wird gespeichert.
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(newImagePicked){
                    currentUser.setFotoPfad("");
                    if(takenImage != null){
                        boolean deleted = takenImage.delete();
                        if(!deleted)
                            Log.e(TAG, "file deletion error");
                    }
                }
                finishAfterTransition();
            }
        });

    }

    private void updateViews() {
        //Views befüllen
        mTitle.setText(currentUser.getName());
        mStartnummer.setHint(String.valueOf(currentUser.getStartnummer()));
        mVorname.setHint(currentUser.getVorname());
        mNachname.setHint(currentUser.getNachname());
        mStrecke.setText(getString(R.string.neue_strecke_dummy, currentUser.getStrecke()));


        mCurrentPhotoPath = currentUser.getFotoPfad();

        GlideApp.with(this)
                .load(mCurrentPhotoPath)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        startPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        startPostponedEnterTransition();
                        return false;
                    }
                })
                .placeholder(R.mipmap.dummy_face)
                .into(mUserThumb);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                Log.d(TAG, "EasyImagePickerError");
                Toast.makeText(InputScreenActivity.this, "Picture wasn't taken or selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagesPicked(@NonNull List<File> imagesFiles, EasyImage.ImageSource source, int type) {
                //Handle the images
                onPhotosReturned(imagesFiles);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                // Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(InputScreenActivity.this);
                    if (photoFile != null) {
                        boolean deleted = photoFile.delete();
                        if(!deleted)
                            Log.e(TAG, "file not deleted onCanceled");
                    }
                }
            }
        });

    }


    private void onPhotosReturned(List<File> imagesFiles) {

        newImagePicked = true;

        //nur 1 Element in Liste, keine Mehrfachauswahl erlaubt
        takenImage = imagesFiles.get(0);

        mCurrentPhotoPath = takenImage.getAbsolutePath();
        currentUser.setFotoPfad(mCurrentPhotoPath);
        currentUser.setLastRefresh(new Date());

        GlideApp.with(this)
                .load(mCurrentPhotoPath)
                .into(mUserThumb);

        mUserThumb.invalidate();
    }

    private boolean checkStringInput(String input){
        //Ignoriert alles, was keine Zahl ist.
        String numberOnly= input.replaceAll("[^0-9]", "");
        if(!numberOnly.isEmpty()){
            int inputValue = Integer.parseInt(numberOnly.trim());

            if(inputValue < MIN_DISTANCE || inputValue > MAX_DISTANCE){
                textInputLayout.setError(getString(R.string.string_check_error, MIN_DISTANCE, MAX_DISTANCE));
                return false;
            }else {
                textInputLayout.setError(null);
                currentUser.setStrecke(inputValue);
                currentUser.setLastRefresh(new Date());
                userViewModel.update(currentUser);
                return true;
            }
        }else {
            textInputLayout.setError(getString(R.string.string_check_error, MIN_DISTANCE, MAX_DISTANCE));
            return false;
        }

    }

}
