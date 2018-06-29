package de.oth_regensburg.mueller.simon.swimcount.interfaces;

import android.view.View;


public interface UserListAdapterListener {

    void nameViewOnClick(View image, View v, int position);

    void plusButtonOnClick(int position);

    void minusButtonOnClick(int position);

}
