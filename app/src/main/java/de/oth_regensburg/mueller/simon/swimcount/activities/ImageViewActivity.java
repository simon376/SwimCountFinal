package de.oth_regensburg.mueller.simon.swimcount.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import de.oth_regensburg.mueller.simon.swimcount.R;
import de.oth_regensburg.mueller.simon.swimcount.utils.GlideApp;

public class ImageViewActivity extends AppCompatActivity {

    private int mColorPalette;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        postponeEnterTransition();


        final ConstraintLayout constraintLayout = findViewById(R.id.image_layout);
        ImageView imageView = findViewById(R.id.iv_fullscreen);

        Intent intent = getIntent();
        String pfad = intent.getStringExtra("imageLink");
        String transitionName = intent.getStringExtra("transitionName");

        imageView.setTransitionName(transitionName);

        GlideApp.with(this)
                .asBitmap()
                .load(pfad)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.dummy_face)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        startPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        if (resource != null) {
                            //Generiert Hintergrundfarbe
                            Palette p = Palette.from(resource).generate();
                            mColorPalette = p.getMutedColor(getResources().getColor(R.color.imageViewerBackground));
                            if(mColorPalette != 0){
                                constraintLayout.setBackgroundColor(mColorPalette);
                            }
                        }
                        startPostponedEnterTransition();
                        return false;
                    }
                })
                .into(imageView);

    }


}
