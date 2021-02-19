package com.sss.bitm.projectalpha.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.sss.bitm.projectalpha.R;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Event;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Expense;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Images;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Users;

import static com.sss.bitm.projectalpha.util.Utility.getBitmapImage;
import static com.sss.bitm.projectalpha.util.Utility.getStringImage;

public class PullData {

    private Context context;
    private SharedPreferences preferences;
    private DatabaseReference rootRef;
    private SharedPreferences.Editor editor;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private static final String FILE_NAME = "userData";
    private static final String USER_KEY_NAME = "user";
    private static final String IMAGE_KEY_NAME = "stringImage";

    public PullData(Context context) {
        this.context = context;
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        rootRef = FirebaseDatabase.getInstance().getReference("users");
    }


    public void getUserData(String uid, final CurrentUserCallback callback) {
        DatabaseReference userRef = rootRef.child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                saveIntoFile(user,callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Fetching user data:", " something went wrong");
            }
        });
    }

    public void fetchUserData(String uid) {
        DatabaseReference userRef = rootRef.child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                if (user!=null && user.getUserImageUrl() != null) {
                    fetchImage(user.getUserImageUrl(),user, null);
//                    fetchImage(user.getUserImageUrl(), null, user);
                } else {
                    saveIntoFile(user,null);
                    Log.d("else", "onDataChange: "+user.getUserName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Fetching user data:", " something went wrong");
            }
        });
    }

    public void fetchImage( final String url, Users user, final ProfileImgCallBack callback){
        Log.d("", "fetchImage called: ");
        Bitmap photo = null;
        try {
            photo = new BitmapAsyncTask().execute(url).get();
            editor = preferences.edit();
            String imgstring = getStringImage(photo);
            editor.putString(IMAGE_KEY_NAME, imgstring);
            editor.apply();
            if (user!=null)
                saveIntoFile(user,null);
            if (callback!=null)
                callback.onDownloadComplete(true);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public void saveIntoFile(Users user,CurrentUserCallback callback) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor = preferences.edit();
        editor.putString(USER_KEY_NAME, json);
        editor.apply();
        if (callback!=null) {
            callback.onUserDataSaveCallback(user);
            Log.d("pullData", "data saved");
        }
    }

    public void clearUser() {
        preferences.edit().clear().apply();
    }



    public void getEventData(DatabaseReference reference, final EventCallback callback ) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Event> events = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);
                    events.add(event);
                }
                callback.onEventCallback(events);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Fetching event data:", " something went wrong");
            }
        });
    }

    public void getSingleEventData(DatabaseReference reference, final SingleEventCallBack callback) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                callback.onSingleEventCallBack(event);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Fetching user data:", " something went wrong");
            }
        });
    }

    public void getExpenseData(DatabaseReference reference, final ExpenseCallback callback) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Expense> expenses = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Expense expense = snapshot.getValue(Expense.class);
                    expenses.add(expense);
                }
                callback.onExpenseCallback(expenses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Fetching expense data:", " something went wrong");
            }
        });
    }


    public void getImageData(DatabaseReference reference, final ImageCallback callback) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Images> images = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Images image = snapshot.getValue(Images.class);
                    images.add(image);
                }
                callback.onImageCallback(images);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Fetching expense data:", " something went wrong");
            }
        });
    }

    public Users getUser() {
        Gson gson = new Gson();
        String json = preferences.getString(USER_KEY_NAME, "");
        return gson.fromJson(json, Users.class);
    }

    public Bitmap getProfileImage() {
        String stringImg = preferences.getString(IMAGE_KEY_NAME, "");
        return getBitmapImage(stringImg);
    }


    public FirebaseAuth getAuth() {
        return auth;
    }

    public DatabaseReference getRootRef() {
        return rootRef;
    }


    public void showToast(String string){
        Toast toast=Toast.makeText(context,""+string,Toast.LENGTH_LONG);
        View toastView=toast.getView();
        TextView textView=toastView.findViewById(android.R.id.message);
        textView.setTextColor(Color.RED);
        textView.setBackgroundResource(R.drawable.round_border);
        toast.show();
    }


    public interface EventCallback {
        void onEventCallback(List<Event> eventList);
    }

    public interface ExpenseCallback {
        void onExpenseCallback(List<Expense> expense);
    }

    public interface CurrentUserCallback {
        void onUserDataSaveCallback(Users user);
    }

    public interface ImageCallback {
        void onImageCallback(List<Images> images);
    }

    public interface SingleEventCallBack{
        void onSingleEventCallBack(Event event);
    }

    public interface ProfileImgCallBack{
        void onDownloadComplete(boolean isCompleted);
    }

}
