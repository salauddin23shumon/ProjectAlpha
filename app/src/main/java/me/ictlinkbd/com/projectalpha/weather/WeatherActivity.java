package me.ictlinkbd.com.projectalpha.weather;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import me.ictlinkbd.com.projectalpha.R;
import me.ictlinkbd.com.projectalpha.util.LocationTask;
import me.ictlinkbd.com.projectalpha.util.networking.RetrofitClient;
import me.ictlinkbd.com.projectalpha.weather.weatherAdapter.WeatherViewPagerAdapter;
import me.ictlinkbd.com.projectalpha.weather.weatherDataModel.SearchedWeatherResponse;
import me.ictlinkbd.com.projectalpha.util.ConstantValues;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static me.ictlinkbd.com.projectalpha.util.AppPermission.chkGPS;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.chkLocationPermission;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.showAlert;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.REQUEST_LOCATION_ACCESS;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.WEATHER_URL;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";
    private TabLayout tabLayout;
    private WeatherViewPagerAdapter weatherViewPagerAdapter;
    private ViewPager viewPager;
    private String searchResult = "";
    private Intent intent;
    private Location currentLocation;
    private LocationTask locationTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        tabLayout = findViewById(R.id.tab);
        viewPager = findViewById(R.id.viewPager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchResult = intent.getStringExtra(SearchManager.QUERY);
            searchingService();
        }


        if (chkLocationPermission(this, REQUEST_LOCATION_ACCESS)) {
            locationTask = new LocationTask(this);
            if (chkGPS(this)) {
                getLocation();
            }else
                showAlert(this);
        }
    }


    public void getLocation(){
        locationTask.getDeviceLastLocation(new LocationTask.LastLocationCallBack() {
            @Override
            public void getDeviceLastLocation(Location location) {
                currentLocation = location;
                Log.e("weatherActivity", "onCreateView: " + location.getLatitude());
                viewPagers();
                tablistener();
            }
        });
    }



    private void tablistener() {

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void viewPagers() {
        tabLayout.addTab(tabLayout.newTab().setText("Current Weather").setIcon(R.drawable.weather));
        tabLayout.addTab(tabLayout.newTab().setText("Forecast Weather").setIcon(R.drawable.forecast));
        weatherViewPagerAdapter = new WeatherViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), currentLocation);
        viewPager.setAdapter(weatherViewPagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchView).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        return true;
    }

    public void searchingService() {
        String url = String.format("weather?q=%s&units=metric&appid=" + ConstantValues.api_key, searchResult);
        Call<SearchedWeatherResponse> call = RetrofitClient.getInstance(WEATHER_URL).getApiService().getSearchedWeather(url);

        call.enqueue(new Callback<SearchedWeatherResponse>() {
            @Override
            public void onResponse(Call<SearchedWeatherResponse> call, Response<SearchedWeatherResponse> response) {

                if (response.code() == 200) {
                    SearchedWeatherResponse weatherResponse = response.body();
                    if (weatherResponse != null) {
                        final AlertDialog.Builder dialog = new AlertDialog.Builder(WeatherActivity.this);
                        dialog.setTitle("Search Result for " + searchResult.toUpperCase());
                        dialog.setMessage("Current Weather: " + weatherResponse.getWeather().get(0).getMain() +
                                "\nTemperature: " + weatherResponse.getMain().getTemp() + "°C"
                                + "\nPressure: " + weatherResponse.getMain().getPressure()
                                + "\nHumidity: " + weatherResponse.getMain().getHumidity()
                                + "\nMaximum Temp: " + weatherResponse.getMain().getTempMax() + "°C"
                                + "\nMinimum Temp: " + weatherResponse.getMain().getTempMin() + "°C"
                        );

                        dialog.setCancelable(false);
                        dialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        dialog.show();
                    }
                } else {
                    Toast.makeText(WeatherActivity.this, "City not found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SearchedWeatherResponse> call, Throwable t) {
                Log.e("Error", t.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_ACCESS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (chkGPS(this)) {
                getLocation();
            }else
                showAlert(this);
            Log.d(TAG, "onRequestPermissionsResult: ");
            getSupportFragmentManager().getFragments().get(0).onRequestPermissionsResult(requestCode,permissions,grantResults);
        } else {
            Toast.makeText(WeatherActivity.this, "u will not allow to use GPS from this app", Toast.LENGTH_SHORT).show();
        }
    }
}
