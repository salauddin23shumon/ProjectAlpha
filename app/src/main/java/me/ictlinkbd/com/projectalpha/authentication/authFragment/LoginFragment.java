package me.ictlinkbd.com.projectalpha.authentication.authFragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import me.ictlinkbd.com.projectalpha.R;
import me.ictlinkbd.com.projectalpha.tourEvent.eventDataModel.Users;
import me.ictlinkbd.com.projectalpha.util.NetworkStatus;
import me.ictlinkbd.com.projectalpha.util.PullData;

import static me.ictlinkbd.com.projectalpha.util.NetworkStatus.chkNetworkCallback;
import static me.ictlinkbd.com.projectalpha.util.Utility.isNetworkAvailable;

public class LoginFragment extends Fragment {
    private String TAG = "LoginFragment ";
    private Button btnLogin, btnSignup;
    private ProgressBar loginProgress, signupProgress;
    private TextInputEditText emailET, passET;
    private String email, password;
    private Context context;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private PullData pullData;
    private boolean isAvailable;
    private ConnectivityManager manager;

    private AuthCompleteListener completeListener;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        pullData = new PullData(context);
        manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        completeListener= (AuthCompleteListener) context;
        Log.d("", "onAttach: " + firebaseAuth);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chkNetworkCallback(manager, new NetworkStatus.NetStatusCallBack() {
            @Override
            public void networkStatus(boolean isConnected, FirebaseAuth auth) {
                isAvailable = isConnected;
                firebaseAuth = auth;
                Log.d(TAG, "  " + firebaseAuth);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        btnLogin = view.findViewById(R.id.btn_login);
        btnSignup = view.findViewById(R.id.btn_signup);
        emailET = view.findViewById(R.id.emailET);
        passET = view.findViewById(R.id.passET);
        loginProgress = view.findViewById(R.id.progress_login);
        signupProgress = view.findViewById(R.id.progress_signup);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email=emailET.getText().toString();
                password=passET.getText().toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isAvailable) {
                    if (validate(email,password))
                        doLogin(email,password);
                }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    if (isNetworkAvailable(manager)) {
                        firebaseAuth = FirebaseAuth.getInstance();
                        doLogin(email,password);
                    } else {
                        pullData.showToast("u have no internet");
                        Log.d(TAG, "in  else if ");
                    }
                } else
                        pullData.showToast("u have no internet");
                        Log.d(TAG, "onClick: ");
            }
        });


        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email=emailET.getText().toString();
                password=passET.getText().toString();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isAvailable) {
                    if (validate(email,password))
                        doRegister(email,password,firebaseAuth);
                }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    if (isNetworkAvailable(manager)) {
                        firebaseAuth = FirebaseAuth.getInstance();
                        doRegister(email,password,firebaseAuth);
                    } else {
                        pullData.showToast("u have no internet");
                        Log.d(TAG, "in  else if ");
                    }
                } else
                    pullData.showToast("u have no internet");
                Log.d(TAG, "onClick: ");
            }
        });

        return view;
    }

    private void doLogin(String mail, String pass) {

        btnLogin.setVisibility(View.GONE);
            loginProgress.setVisibility(View.VISIBLE);
            firebaseAuth.signInWithEmailAndPassword(mail, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentUser = firebaseAuth.getCurrentUser();
                                final String uId = currentUser.getUid();
                                try {
                                    pullData.getUserData(uId, new PullData.CurrentUserCallback() {
                                        @Override
                                        public void onUserDataSaveCallback(Users user) {
                                            pullData.showToast("login successful...");
                                            completeListener.onAuthComplete();
                                            clearViews();
                                        }
                                    });
                                } catch (Exception ex) {
                                    Log.d(TAG, "onComplete: " + ex.toString());
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pullData.showToast(e.getMessage());
                            loginProgress.setVisibility(View.GONE);
                            btnLogin.setVisibility(View.VISIBLE);
                        }
                    });
    }

    private void doRegister(final String mail, String pass, final FirebaseAuth auth) {

        signupProgress.setVisibility(View.VISIBLE);
        btnSignup.setVisibility(View.GONE);
        auth.createUserWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        signupProgress.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            final String uID = auth.getCurrentUser().getUid();
                            Users user = new Users(uID, null, mail, null, null, null);
                            FirebaseDatabase
                                    .getInstance()
                                    .getReference("users").child(uID)
                                    .setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                pullData.getUserData(uID, new PullData.CurrentUserCallback() {
                                                    @Override
                                                    public void onUserDataSaveCallback(Users user) {
                                                        pullData.showToast("account created successfully");
                                                        completeListener.onAuthComplete();
                                                    }
                                                });
                                            } else {
                                                pullData.showToast("account not created");
                                            }

                                        }
                                    });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pullData.showToast(e.getMessage());
                signupProgress.setVisibility(View.GONE);
                btnSignup.setVisibility(View.VISIBLE);
            }
        });

    }

    private boolean validate(String userEmail,String userPass) {

        boolean valid = true;
        if (userEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
           pullData.showToast("Please Enter valid Email");
            valid = false;
        } else if (userPass.isEmpty()) {
            pullData.showToast("Please Enter Password");
            valid = false;
        }
        return valid;
    }

//    private void showToast(String string){
//        Toast toast=Toast.makeText(context,""+string,Toast.LENGTH_LONG);
//        View toastView=toast.getView();
//        TextView textView=toastView.findViewById(android.R.id.message);
//        textView.setTextColor(Color.RED);
//        textView.setBackgroundResource(R.drawable.round_border);
//        toast.show();
//    }

    private void clearViews(){
        emailET.setText("");
        passET.setText("");
        signupProgress.setVisibility(View.GONE);
        loginProgress.setVisibility(View.GONE);
    }

    public interface AuthCompleteListener {
        void onAuthComplete();
    }
}
