package de.oth_regensburg.mueller.simon.swimcount.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.oth_regensburg.mueller.simon.swimcount.R;
import de.oth_regensburg.mueller.simon.swimcount.database.entity.User;
import de.oth_regensburg.mueller.simon.swimcount.interfaces.UserListAdapterListener;
import de.oth_regensburg.mueller.simon.swimcount.utils.GlideApp;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private final UserListAdapterListener mOnClickListener;
    private final Context mContext;


    class UserViewHolder extends RecyclerView.ViewHolder {
        final TextView nameItemView;
        final TextView startnummerTextView;
        final TextView streckeTextView;
        final ImageView imageView;
        final Button plusButton;
        final Button minusButton;



        UserViewHolder(View itemView) {
            super(itemView);

            nameItemView = itemView.findViewById(R.id.tv_user_name);
            startnummerTextView = itemView.findViewById(R.id.tv_startnummer);
            streckeTextView = itemView.findViewById(R.id.tv_strecke);
            imageView = itemView.findViewById(R.id.iv_user_image);
            plusButton = itemView.findViewById(R.id.button_plus);
            minusButton = itemView.findViewById(R.id.button_minus);

            plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnClickListener.plusButtonOnClick(getAdapterPosition());
                }
            });
            minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnClickListener.minusButtonOnClick(getAdapterPosition());
                }
            });
            nameItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickListener.nameViewOnClick(imageView, v, getAdapterPosition());
                }
            });


        }


    }


    private final LayoutInflater mInflater;
    private List<User> mUserList;

    public UserListAdapter(Context context, List<User> userList, UserListAdapterListener onClickListener) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        this.mUserList = userList;
        mOnClickListener = onClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(R.layout.recyclerview_element, parent, false);
        return new UserViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder holder, int position) {

        if(mUserList != null){
            // Retrieve the data for that position
            User user = mUserList.get(position);

            // Add the data to the view
            holder.nameItemView.setText(user.getName());
            holder.streckeTextView.setText(mContext.getString(R.string.tv_strecke, user.getStrecke()));
            holder.startnummerTextView.setText(mContext.getString(R.string.tv_startnummer, user.getStartnummer()));

            holder.imageView.setTransitionName("transitionName" + position);
            GlideApp.with(mContext)
                    .load(user.getFotoPfad())
                    .placeholder(R.mipmap.dummy_face)
                    .into(holder.imageView);

        }
        else{
            // Covers the case of data not being ready yet.
            holder.nameItemView.setText(mContext.getString(R.string.tv_name));
            holder.streckeTextView.setText(mContext.getString(R.string.tv_strecke, 0));
            holder.startnummerTextView.setText(mContext.getString(R.string.tv_startnummer, 0));
        }

    }


    public void setUserList(List<User> users){
        mUserList = users;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mUserList has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if(mUserList != null)
            return mUserList.size();
        else return 0;
    }


    public void removeUser(int position){
        mUserList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreUser(User user, int position){
        mUserList.add(position, user);
        notifyItemInserted(position);
    }

}
