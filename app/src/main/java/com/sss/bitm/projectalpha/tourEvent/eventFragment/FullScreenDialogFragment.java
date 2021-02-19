package com.sss.bitm.projectalpha.tourEvent.eventFragment;


import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ortiz.touchview.TouchImageView;

import com.sss.bitm.projectalpha.R;


public class FullScreenDialogFragment extends DialogFragment {

    private String url;
    private Bitmap bitmap;
    private Dialog fullScreenDialog;
    private TouchImageView fullScreenView;
    private int imgHeight, imgWidth;


    public FullScreenDialogFragment() {
        // Required empty public constructor
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bundle bundle = getArguments();
        url = bundle.getString("link");
        Log.d("url", "onCreateDialog: " + url);
        bitmap = BitmapFactory.decodeFile(url,options);
        imgHeight=options.outHeight;
        imgWidth=options.outWidth;

        View view = getActivity().getLayoutInflater().inflate(R.layout.full_screen_image, new LinearLayout(getActivity()), false);
        fullScreenDialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        fullScreenDialog.setContentView(view);
        fullScreenDialog.setCanceledOnTouchOutside(true);

        Button closeBtn = view.findViewById(R.id.btnClose);
        fullScreenView = view.findViewById(R.id.fullView);
        setView();

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreenDialog.dismiss();
            }
        });
        return fullScreenDialog;
    }

    public void setView(){
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE && imgHeight<imgWidth) {
            fullScreenView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE && imgHeight>imgWidth){
            fullScreenView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }else if (currentOrientation == Configuration.ORIENTATION_PORTRAIT && imgHeight>imgWidth){
            fullScreenView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }else if (currentOrientation == Configuration.ORIENTATION_PORTRAIT && imgHeight<imgWidth){
            fullScreenView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        fullScreenView.setImageBitmap(bitmap);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setView();
    }
}
