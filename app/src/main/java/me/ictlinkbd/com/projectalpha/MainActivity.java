package me.ictlinkbd.com.projectalpha;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.navigation.NavigationView;

import org.greenrobot.eventbus.EventBus;

import de.hdodenhof.circleimageview.CircleImageView;
import me.ictlinkbd.com.projectalpha.interfaces.NavIconShowHideListener;
import me.ictlinkbd.com.projectalpha.nearByPlace.NearByPlaceDetailFragment;
import me.ictlinkbd.com.projectalpha.nearByPlace.NearByPlaceAdapter;
import me.ictlinkbd.com.projectalpha.nearByPlace.NearByPlaceFragment;
import me.ictlinkbd.com.projectalpha.authentication.AuthenticationActivity;
import me.ictlinkbd.com.projectalpha.nearByPlace.nearByPojo.Result;
import me.ictlinkbd.com.projectalpha.tourEvent.eventInterface.NavDrawerUpdateListener;
import me.ictlinkbd.com.projectalpha.tourEvent.eventInterface.OpenGalleryFragment;
import me.ictlinkbd.com.projectalpha.util.DataCarrier;
import me.ictlinkbd.com.projectalpha.util.LocationTask;
import me.ictlinkbd.com.projectalpha.weather.WeatherActivity;
import me.ictlinkbd.com.projectalpha.map.MapFragment;
import me.ictlinkbd.com.projectalpha.tourEvent.eventAdapter.EventAdapter;
import me.ictlinkbd.com.projectalpha.tourEvent.eventFragment.EventListFragment;
import me.ictlinkbd.com.projectalpha.tourEvent.eventFragment.EventAddFragment;
import me.ictlinkbd.com.projectalpha.tourEvent.eventFragment.EventInfoFragment;
import me.ictlinkbd.com.projectalpha.tourEvent.eventFragment.GalleryFragment;
import me.ictlinkbd.com.projectalpha.tourEvent.eventFragment.PhotoFragment;
import me.ictlinkbd.com.projectalpha.tourEvent.eventFragment.UserProfileFragment;
import me.ictlinkbd.com.projectalpha.tourEvent.eventDataModel.Event;
import me.ictlinkbd.com.projectalpha.tourEvent.eventDataModel.Users;
import me.ictlinkbd.com.projectalpha.util.PullData;

import static me.ictlinkbd.com.projectalpha.util.AppPermission.chkLocationPermission;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.ENABLE_GPS_ACCESS;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.GPS_ENABLED;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.REQUEST_IMAGE_CAPTURE;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.REQUEST_LOCATION_ACCESS;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        EventListFragment.EventListFragmentListener, NavDrawerUpdateListener,
        EventAdapter.EventListDetailsListener, OpenGalleryFragment,
        EventAddFragment.EventAddFragmentListener, NavIconShowHideListener,
        EventInfoFragment.EventInfoFragmentListener, NearByPlaceAdapter.PlaceAdapterClickListener {

    private static final String TAG = "MainActivity";
    private TextView mStartProgress, mRemainProgress, mUserName, mEmailAddress;
    private FragmentManager manager;
    public static Toolbar toolbar;
    public static ActionBarDrawerToggle toggle;
    public static DrawerLayout drawer;
    private NavigationView navigationView;

    private CircleImageView profile_img;

    private Fragment fragment;
    private Users user;
    private PullData userData;
    private LocationTask locationTask;
    public static Location CurrentLocation;
    public static LocationCallback callback;


    private static EventBus dataBus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userData = new PullData(this);
        user = userData.getUser();

        setViewForNavHeader();

        locationTask = new LocationTask(this);


        if (chkLocationPermission(this, REQUEST_LOCATION_ACCESS)) {
            getLocation();
        }


        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        fragment = new EventListFragment();
        if (savedInstanceState == null) {
            Log.e(TAG, "onCreate: if");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }

//        navigation_drawer_show(savedInstanceState);
        setDrawerView(user);

    }

    private void setViewForNavHeader() {
        View header = ((NavigationView) findViewById(R.id.main_nav_view)).getHeaderView(0);
        mUserName = header.findViewById(R.id.ui_nav_user_name);
        mEmailAddress = header.findViewById(R.id.ui_nav_email_address);
        profile_img = header.findViewById(R.id.profile_image);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: called");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ui_menu_logout:
                onLogOutComplete();
                break;
            case R.id.menu_profile:
                fragment = new UserProfileFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        switch (id) {
            case R.id.nav_event:
                this.fragment = new EventListFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, this.fragment, "listFragment").commit();
                getSupportActionBar().setTitle("Event");
                break;
            case R.id.nav_weather:
                startActivity(new Intent(MainActivity.this, WeatherActivity.class));
                break;
            case R.id.nav_map:
                MapFragment mapFragment = new MapFragment();
                if (!(fragment instanceof MapFragment)) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapFragment).addToBackStack("listFragment").commit();
                    getSupportActionBar().setTitle("Map");
                }
                break;
            case R.id.nav_near_by:
                this.fragment = new NearByPlaceFragment();
                if (!(fragment instanceof NearByPlaceFragment)) {
                    if (fragment instanceof MapFragment) {
                        getSupportFragmentManager().popBackStack();
//                        fragment.onDetach();
//                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, this.fragment).addToBackStack("listFragment").commit();
                        getSupportActionBar().setTitle("NearBy Place");
                    } else {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, this.fragment).addToBackStack("nearbyplace").commit();
                        getSupportActionBar().setTitle("NearBy Place");
                    }
                }
                break;
        }
        // auto hide after clicking.....
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
//            tracker.stopUsingGPS();
        }
    }


    @Override
    public void onLogOutComplete() {
        if (userData.getAuth() != null)
            userData.getAuth().signOut();
        userData.clearUser();
        startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
        Toast.makeText(this, "Logout Successfully...", Toast.LENGTH_SHORT).show();
        finish();
    }


    @Override
    public void onEventClicked(Event eventObj) {
        fragment = new EventInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", eventObj);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    @Override
    public void onEventAdd() {
        fragment = new EventAddFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    @Override
    public void back_to_event_list_page() {

    }

    @Override
    public void open_event_add_fragment_page(Event eventObject) {
        Log.d(TAG, "open_event_add_fragment_page: clicked");
        fragment = new EventAddFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", eventObject);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    @Override
    public void openPhotoFragment(Event event) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        fragment = new PhotoFragment();
        fragment.setArguments(bundle);
        getSupportActionBar().setTitle("Capture Image");
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
        Log.d(TAG, "openPhotoFragment: " + event.getEventId());
    }

    @Override
    public void onEventAddComplete() {
        fragment = new EventListFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }

    @Override
    public void onUserUpdate(Users newUser) {
        setDrawerView(newUser);
        Log.d(TAG, "onUserUpdate: called " + newUser.getUserName());
    }

    public void setDrawerView(Users user) {
        if (user.getUserName() != null) {
            mUserName.setText(user.getUserName());
        }
        if (userData.getProfileImage() != null)
            profile_img.setImageBitmap(userData.getProfileImage());

        mEmailAddress.setText(user.getUserEmail());
    }

    @Override
    public void onGalleryOpenListener(Event event) {
        GalleryFragment galleryFragment = new GalleryFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        galleryFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, galleryFragment).addToBackStack(null).commit();
    }

    @Override
    public void showHamburgerIcon() {
        Log.d(TAG, "showHamburgerIcon: called");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toggle.setDrawerIndicatorEnabled(true);
        toolbar.setTitle("Events");
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void showBackIcon() {
        Log.d(TAG, "showBackIcon: called");
        toggle.setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void getLocation() {
        Log.d(TAG, "getLocation: called");
        locationTask.getLocationUpdates(new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                CurrentLocation = locationResult.getLastLocation();
                Log.e(TAG, "onLocationResult: " + CurrentLocation.getLatitude());

                if (CurrentLocation != null) {
                    locationTask.stopLocationUpdate(this);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_ACCESS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSupportFragmentManager().getFragments().get(0).onRequestPermissionsResult(requestCode, permissions, grantResults);
                } else {
                    Toast.makeText(MainActivity.this, "u will not allow to use GPS from this app", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                Log.d(TAG, "onRequestPermissionsResult: "+requestCode);
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {           ///called for gpsChecking purpose
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: " + requestCode + "   " + resultCode);
        getSupportFragmentManager().getFragments().get(0).onActivityResult(requestCode, resultCode, data);
    }


    public static void startLocationUpdate(LocationTask location) {
        Log.d(TAG, "startLocationUpdate: called");
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.e(TAG, "onLocationResult: " + locationResult.getLocations());
                CurrentLocation = locationResult.getLastLocation();
                dataBus.post(new DataCarrier(CurrentLocation));
            }
        };
        location.getLocationUpdates(callback);
    }

    public static void stopLocationUpdate(LocationTask location) {
        if (callback != null) {
            location.stopLocationUpdate(callback);
        }
    }

    @Override
    public void onPlaceClicked(Result nearbyPlacesData) {
        fragment = new NearByPlaceDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("Object", nearbyPlacesData);
        fragment.setArguments(bundle);
//        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
//        fragment.onStop();
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, this.fragment).addToBackStack("nearbyplace").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).addToBackStack("nearbyplace").commit();
    }
}
