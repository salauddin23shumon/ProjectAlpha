package me.ictlinkbd.com.projectalpha.weather.weatherAdapter;


import android.location.Location;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import me.ictlinkbd.com.projectalpha.weather.weatherFragment.ForecastWeatherFragment;
import me.ictlinkbd.com.projectalpha.weather.weatherFragment.WeatherFragment;

public class WeatherViewPagerAdapter extends FragmentPagerAdapter {

    private int tabNum;
    private Bundle bundle;

    public WeatherViewPagerAdapter(FragmentManager fragmentManager, int tabNum, Location location) {
        super(fragmentManager);
        bundle = new Bundle();
        bundle.putParcelable("loc", location);
        this.tabNum = tabNum;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
//                return WeatherFragment.newInstance();
                fragment = new WeatherFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 1:
//                return ForecastWeatherFragment.newInstance();
                fragment = new ForecastWeatherFragment();
                fragment.setArguments(bundle);
                return fragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return tabNum;
    }

    public void updateLocation(Location location){
        bundle.putParcelable("loc", location);
    }
}
