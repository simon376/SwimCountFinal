package de.oth_regensburg.mueller.simon.swimcount.activities;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ag.floatingactionmenu.OptionsFabLayout;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.util.List;
import java.util.Objects;

import de.oth_regensburg.mueller.simon.swimcount.BuildConfig;
import de.oth_regensburg.mueller.simon.swimcount.R;
import de.oth_regensburg.mueller.simon.swimcount.adapters.UserListAdapter;
import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;
import de.oth_regensburg.mueller.simon.swimcount.interfaces.UserListAdapterListener;
import de.oth_regensburg.mueller.simon.swimcount.utils.Cache;
import de.oth_regensburg.mueller.simon.swimcount.view_models.UploadViewModel;
import de.oth_regensburg.mueller.simon.swimcount.view_models.UserViewModel;
import tk.wasdennnoch.progresstoolbar.ProgressToolbar;

public class MainActivity extends AppCompatActivity implements UserListAdapterListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DEFAULT_DISTANCE = 50;

    private List<User> mUserList;
    private UserListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private ProgressToolbar mProgressToolbar;


    private UserViewModel mUserViewModel;


    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final int RC_SEARCH = 1234;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initViewModels();
        prepareUploader();

    }

    // --- INITIALISIERUNGEN ---

    private void prepareUploader(){
        mUserViewModel.getAllChangedUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(@Nullable List<User> users) {
                if(users != null && users.size() != 0){
                    Cache.setCachedUsers(users); //Cache-Update
                }
            }
        });

        UploadViewModel mUploadViewModel = ViewModelProviders.of(this).get(UploadViewModel.class);
        //PeriodicWorkRequest starten
        mUploadViewModel.uploadDataRequest();

        /* Funktioniert leider nicht ganz.
        mUploadViewModel.getOutputStatuses().observe(this, new Observer<List<WorkStatus>>() {
            @Override
            public void onChanged(@Nullable List<WorkStatus> listOfWorkStatuses) {
                if (listOfWorkStatuses == null || listOfWorkStatuses.isEmpty()){
                    return;
                }

                WorkStatus workStatus = listOfWorkStatuses.get(0);
                boolean finished = workStatus.getState().isFinished();
                if(!finished) {
                    showWorkInProgress();
                } else {
                    showWorkFinished();
                }
            }
        });
*/
    }

    /**
     * Zeigt ProgressBar wenn Worker daten hochlädt.
     */
    private void showWorkInProgress() {

        mProgressToolbar.setIndeterminate(true);
        mProgressToolbar.showProgress(true);
    }

    /**
     * Versteckt ProgressBar wenn Worker fertig ist.
     */
    private void showWorkFinished() {
        mProgressToolbar.hideProgress(true);
    }

    private void initViewModels(){
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mUserViewModel.getAllUsers().observe(this, new Observer<List<User>>() {
            @Override
            public void onChanged(@Nullable List<User> users) {
                //Update the cached copy of the users in the adapter.
                mAdapter.setUserList(users);
                mUserList = users;
            }
        });
    }

    private void initViews(){
        mProgressToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.app_name);
        setSupportActionBar(mProgressToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        mProgressToolbar.hideProgress();

        initFab();

        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Create an adapter and supply the data to be displayed.
        mAdapter = new UserListAdapter(this, mUserList, this);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);  //Deaktiviert Animation beim neu zeichnen (Plus/Minus)


        ItemTouchHelper mIth = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
                //Drag n Drop nicht erlaubt
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // backup of removed item for undo purpose
                final int deletedIndex = viewHolder.getAdapterPosition();
                final User deletedUser = mUserList.get(deletedIndex);

                String name = deletedUser.getName();

                //aus db löschen
                mUserViewModel.delete(deletedUser);
                // remove the item from recycler view
                mAdapter.removeUser(deletedIndex);

                // showing snack bar with Undo option
                Snackbar snackbar = Snackbar
                        .make(mRecyclerView,name + " removed!", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // undo is selected, restore the deleted item
                        //in db
                        mUserViewModel.insert(deletedUser);
                        //in RecyclerView
                        mAdapter.restoreUser(deletedUser, deletedIndex);

                    }
                });

                snackbar.setActionTextColor(ContextCompat.getColor(MainActivity.this, R.color.primaryColor));
                snackbar.show();
            }
        });

        mIth.attachToRecyclerView(mRecyclerView);

    }

    private void initFab() {
        final OptionsFabLayout fabWithOptions = findViewById(R.id.fab);
        //Set main fab OnClickListener.
        fabWithOptions.setMainFabOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fabWithOptions.isOptionsMenuOpened()){
                    fabWithOptions.closeOptionsMenu();
                }
                Log.d(TAG, "Main-FAB clicked");
            }
        });

        //Set mini FABs OnClickListeners.
        fabWithOptions.setMiniFabSelectedListener(new OptionsFabLayout.OnMiniFabSelectedListener() {
            @Override
            public void onMiniFabSelected(MenuItem fabItem) {
                switch (fabItem.getItemId()) {
                    case R.id.fab_qr:
                        startRevealActivity(fabItem.getItemId());
                        Log.d(TAG, "QR-FAB clicked");
                        break;
                    case R.id.fab_search:
                        startRevealActivity(fabItem.getItemId());
                        Log.d(TAG, "Search-FAB clicked");
                    default:
                        break;
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
        //TODO fürs Debugging Bildschirm anlassen

        if (BuildConfig.DEBUG) { // don't even consider it otherwise
            if (Debug.isDebuggerConnected()) {
                Log.d("SCREEN", "Keeping screen on for debugging, detach debugger and force an onResume to turn it off.");
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.d("SCREEN", "Keeping screen on for debugging is now deactivated.");
            }
        }

    }

    // --- ONCLICKLISTENER ---

    @Override
    public void nameViewOnClick(View imageView, View v, int position) {
        Log.d(TAG, "OnClickCardView");

        Intent intent = new Intent(this, InputScreenActivity.class);
        String tnImage = ("image" + position);


        //noinspection unchecked
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageView, tnImage);


        String userImagePath = mUserList.get(position).getFotoPfad();
        intent.putExtra("imageLink", userImagePath);
        intent.putExtra("tnImage", tnImage);


        intent.putExtra("NUMBER", String.valueOf(mUserList.get(position).getStartnummer()));

        startActivity(intent, options.toBundle());

        // Notify the adapter, that the data has changed so it can
        // update the RecyclerView to display the data.
        //View wird erst beim wieder-hochscrollen aktualisiert..
        this.mAdapter.notifyItemChanged(position);

    }

    @Override
    public void plusButtonOnClick(int position) {
        Log.d(TAG, "OnPlusButtonClick");

        //access the affected item in mUserList.
        User currentUser = mUserList.get(position);

        currentUser.addStrecke(DEFAULT_DISTANCE);       //Strecke + 50m

        mUserViewModel.update(currentUser);

        // Notify the adapter, that the data has changed so it can
        // update the RecyclerView to display the data.
        //View wird erst beim wieder-hochscrollen aktualisiert..
        this.mAdapter.notifyItemChanged(position);
    }

    @Override
    public void minusButtonOnClick(int position) {
        Log.d(TAG, "OnMinusButtonClick");

        //access the affected item in mUserList.
        User currentUser = mUserList.get(position);
        int aktuelleStrecke = currentUser.getStrecke();
        if(aktuelleStrecke >= 50){
            currentUser.addStrecke(-DEFAULT_DISTANCE);       //Strecke - 50m
        }
        else if(aktuelleStrecke == 0){

            return;
        }
        else{
            currentUser.setStrecke(0);
        }

        mUserViewModel.update(currentUser);

        // Notify the adapter, that the data has changed so it can
        // update the RecyclerView to display the data.
        //View wird erst beim wieder-hochscrollen aktualisiert..
        this.mAdapter.notifyItemChanged(position);

    }


    private void startRevealActivity(int id) {

        Intent intent;
        switch (id) {
            case R.id.fab_qr:
                intent = new Intent(this, QRScannerActivity.class);
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                break;
            case R.id.fab_search:
                intent = new Intent(this, SearchActivity.class);
                intent.setAction(Intent.ACTION_SEARCH);
                startActivityForResult(intent, RC_SEARCH);
                break;
            default:
                Log.d(TAG, "Nichts geklickt? komisch.");
                break;
        }

    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_BARCODE_CAPTURE:
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        String barcode = data.getStringExtra(QRScannerActivity.BarcodeObject);
                        Log.d(TAG, "Barcode read: " + barcode);

                        Intent intent = new Intent(this, SearchActivity.class);
                        intent.putExtra(SearchManager.QUERY, barcode);

                        intent.setAction(Intent.ACTION_SEARCH);
                        startActivityForResult(intent, RC_SEARCH);


                    } else {
                        Log.d(TAG, "No barcode captured, intent data is null");
                    }
                } else {
                    Log.e(TAG, CommonStatusCodes.getStatusCodeString(resultCode));
                }
                break;
            case RC_SEARCH:
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        int nummer = data.getIntExtra(SearchActivity.SearchObject, 0);
                        Log.d(TAG, "Startnummer searched: " + nummer);
                    }
                } else {
                    Log.e(TAG, CommonStatusCodes.getStatusCodeString(resultCode));
                    Toast.makeText(this, "Fehler oder BackButtonPressed", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

}
