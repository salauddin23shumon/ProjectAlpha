package me.ictlinkbd.com.projectalpha.authentication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import me.ictlinkbd.com.projectalpha.R;

import me.ictlinkbd.com.projectalpha.authentication.authFragment.LoginFragment;
import me.ictlinkbd.com.projectalpha.authentication.authFragment.LoginSplashFragment;
import me.ictlinkbd.com.projectalpha.MainActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class AuthenticationActivity extends AppCompatActivity implements  LoginFragment.AuthCompleteListener,
        LoginSplashFragment.SplashComplete {

    private Fragment fragment;
    private Bundle bundle=new Bundle();
    String TAG="AuthenticationActivity";


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            onAuthComplete();
        } else {
            fragment = new LoginFragment();
            commitTransaction(fragment);
        }

    }

    private void commitTransaction(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragContainer, fragment).commit();
    }

//    @Override
//    public void onSignupComplete(int t) {
//        startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
////        finish();
//        Bundle bundle =new Bundle();
//        bundle.putInt("time",t);
//        fragment=new LoginSplashFragment();
//        fragment.setArguments(bundle);
//        commitTransaction(fragment);
//    }

    @Override
    public void onAuthComplete() {
//        startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
//        finish();
        fragment=new LoginSplashFragment();
        commitTransaction(fragment);
    }


    @Override
    public void onSplashComplete() {
        startActivity(new Intent(AuthenticationActivity.this, MainActivity.class));
        finish();
    }
}
