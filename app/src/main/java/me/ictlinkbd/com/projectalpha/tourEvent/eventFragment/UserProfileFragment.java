package me.ictlinkbd.com.projectalpha.tourEvent.eventFragment;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Vector;

import de.hdodenhof.circleimageview.CircleImageView;
import me.ictlinkbd.com.projectalpha.MainActivity;
import me.ictlinkbd.com.projectalpha.R;
import me.ictlinkbd.com.projectalpha.tourEvent.eventInterface.NavDrawerUpdateListener;
import me.ictlinkbd.com.projectalpha.tourEvent.eventDataModel.Users;
import me.ictlinkbd.com.projectalpha.util.NetworkStatus;
import me.ictlinkbd.com.projectalpha.util.PullData;

import static android.app.Activity.RESULT_OK;
import static me.ictlinkbd.com.projectalpha.util.NetworkStatus.chkNetworkCallback;
import static me.ictlinkbd.com.projectalpha.util.Utility.getImageBAOS;
import static me.ictlinkbd.com.projectalpha.util.Utility.isNetworkAvailable;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserProfileFragment extends Fragment {

    private EditText emailET, nameET, contactET;
    private String name, email, contact;
    private Button update_btn, add_img_btn;
    private CircleImageView circleImageView;
    private Uri filePath;
    private Users activeUser;
    private DatabaseReference rootRef, userRef;
    private StorageReference imgStorageRef;
    private StorageTask storageTask;
    private static final int IMG_RQST_CODE = 22;
    private Context context;
    private PullData pullData;
    private NavDrawerUpdateListener updateListener;
    private static final String TAG = "UserProfile";
    private Vector<Dialog> alertDialogs = new Vector<Dialog>();
    private Dialog alertDialog;
    private double progress;
    private long millisInFuture = 5 * 1000, countDownInterval = 1000, total =0;
    private byte[] imgByte;
    private Drawable oldDrawable;
    private Toolbar toolbar;

    private boolean isAvailable;
    private ConnectivityManager manager;


    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        pullData = new PullData(context);
        manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        imgStorageRef = FirebaseStorage.getInstance().getReference("profile_images");
        rootRef = pullData.getRootRef();
        activeUser = pullData.getUser();
        updateListener = (NavDrawerUpdateListener) context;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chkNetworkCallback(manager, new NetworkStatus.NetStatusCallBack() {
            @Override
            public void networkStatus(boolean isConnected, FirebaseAuth auth) {
                isAvailable = isConnected;
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        emailET = view.findViewById(R.id.emailET);
        nameET = view.findViewById(R.id.nameET);
        contactET = view.findViewById(R.id.contactET);
        add_img_btn = view.findViewById(R.id.add_img_btn);
        update_btn = view.findViewById(R.id.btn_update);
        circleImageView = view.findViewById(R.id.profile_image);
        oldDrawable = circleImageView.getDrawable();


        toolbar = view.findViewById(R.id.profile_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setTitle("User Profile");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getFragmentManager().popBackStack();
            }
        });

        if (nameET.getText().toString().isEmpty())
            nameET.setFocusable(true);
        loadView(activeUser);
        userRef = rootRef.child(activeUser.getUserId());


        add_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Photo"), IMG_RQST_CODE);
            }
        });

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isAvailable) {
                    if (validate())
                        uploadProfile();
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    if (isNetworkAvailable(manager)) {
                        uploadProfile();
                    } else {
                        pullData.showToast("u have no internet");
                        Log.d(TAG, "in  else if ");
                    }
                } else
                    pullData.showToast("u have no internet");
            }
        });

        return view;
    }


    private void loadView(Users user) {
        emailET.setText(user.getUserEmail());
        emailET.setFocusable(false);
        if (user.getUserImageUrl() != null || user.getUserName() != null || user.getPhoneNumber() != null) {
            nameET.setText(user.getUserName());
            contactET.setText(user.getPhoneNumber());
            circleImageView.setImageBitmap(pullData.getProfileImage());
            imgByte=getImageBAOS(pullData.getProfileImage());
        }
    }

    private void uploadProfile() {

            final StorageReference fileReference = imgStorageRef.child("profile_img_"+activeUser.getUserName());

            storageTask = fileReference.putBytes(imgByte)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

//                                    Users user = new Users(activeUser.getUserId(), name, activeUser.getUserEmail(), contact, uri.toString(), activeUser.getEvents());
                                    userRef.child("userName").setValue(name);
                                    userRef.child("phoneNumber").setValue(contact);
                                    userRef.child("userImageUrl").setValue(uri.toString());


                                    new CountDownTimer(millisInFuture, countDownInterval) {
//                                        final PullData newUserData = new PullData(context);

                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            pullData.fetchUserData(activeUser.getUserId());
                                            long finishedSeconds = millisInFuture - millisUntilFinished;
                                            progress = (int)progress+ (((float)finishedSeconds / (float)millisInFuture) * 50.0);    //50% from here
                                            showProgressDialog(progress);
                                        }
                                        @Override
                                        public void onFinish() {
                                            showProgressDialog(100);
                                            Users newUser = pullData.getUser();
                                            loadView(newUser);
                                            updateListener.onUserUpdate(newUser);
                                            Log.d(TAG, "task Complete: " + newUser.getUserName());
                                            clearView();
                                            Toast.makeText(context, "Updated successful", Toast.LENGTH_LONG).show();
                                        }
                                    }.start();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            progress = (50.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()); //50% from here
                            Log.d(TAG, "onProgress: "+progress);
                            showProgressDialog(progress);
                        }
                    });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_RQST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            RequestOptions myOptions = new RequestOptions()
                    .centerCrop() // or centerCrop
                    .override(195, 195);

            Glide
                    .with(context)
                    .asBitmap()
                    .apply(myOptions)
                    .load(filePath)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//                            String image = (getStringImage(resource));
                            circleImageView.setImageBitmap(resource);
                            imgByte=getImageBAOS(resource);
//                            Log.d(TAG, "onResourceReady: "+imgByte.length);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    getFragmentManager().popBackStack();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        super.onDestroy();
    }

    private void showProgressDialog(double progress) {
        alertDialog = new Dialog(context);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.progress_dialog);
        alertDialog.setCancelable(true);
        final ProgressBar progressBar = alertDialog.findViewById(R.id.circular_progressBar);
        final TextView progressUpdate = alertDialog.findViewById(R.id.progressTV);
        progressBar.setProgress((int) progress);
        progressUpdate.setText(progressBar.getProgress() + "%");
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialogs.add(alertDialog);
    }

    private boolean validate() {

        name = nameET.getText().toString();
        contact = contactET.getText().toString();

        boolean valid = true;
        if (name.isEmpty() || name.length() > 32) {
            pullData.showToast("Please Enter valid name");
            valid = false;
        } else if (!name.matches("[a-z A-Z0-9.@'_]*")) {
            pullData.showToast("Please enter valid name without special symbol");
            valid = false;
        } else if (contact.isEmpty()) {
            pullData.showToast("Please Enter Contact");
            valid = false;
        } else if (circleImageView.getDrawable().equals(oldDrawable) ) {
            pullData.showToast("Please select a profile photo");
            valid = false;
        }
        return valid;
    }


    private void clearView() {
        for (Dialog dialog : alertDialogs)
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        nameET.clearFocus();
        contactET.clearFocus();
    }
}
