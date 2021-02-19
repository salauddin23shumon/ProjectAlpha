package com.sss.bitm.projectalpha.authentication.authFragment;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sss.bitm.projectalpha.R;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Users;
import com.sss.bitm.projectalpha.util.PullData;
import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginSplashFragment extends Fragment {

    private GifImageView gifImageView;
    private PullData pullData;
    private Users user;
    private SplashComplete splashComplete;
    private String url;
    private int delay = 1500;
    private boolean status;
    String TAG="LoginSplashFragment";


    public LoginSplashFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        splashComplete = (SplashComplete) context;
        pullData = new PullData(context);
        url = pullData.getUser().getUserImageUrl();
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("time"))
            delay = getArguments().getInt("time");
        Log.d("", "onAttach: "+url);

        if (url!=null && pullData.getProfileImage()==null){
            pullData.fetchImage(url.trim(),null, new PullData.ProfileImgCallBack() {
                @Override
                public void onDownloadComplete(boolean isCompleted) {
                    status=isCompleted;
                    callHandler();
                    Log.d("", "onAttach: "+status);
//                    splashComplete.onSplashComplete();
                }
            });
        }
        else
            callHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login_splash, container, false);
        gifImageView = view.findViewById(R.id.loginGif);
        return view;
    }

    public void callHandler(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashComplete.onSplashComplete();
            }
        }, 3200);
    }



    public interface SplashComplete {
        void onSplashComplete();
    }
}
