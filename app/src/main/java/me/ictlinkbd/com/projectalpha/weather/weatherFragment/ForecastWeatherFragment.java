package me.ictlinkbd.com.projectalpha.weather.weatherFragment;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import me.ictlinkbd.com.projectalpha.R;
import me.ictlinkbd.com.projectalpha.util.networking.RetrofitClient;
import me.ictlinkbd.com.projectalpha.weather.weatherAdapter.ForecastAdapter;
import me.ictlinkbd.com.projectalpha.weather.weatherDataModel.ForecastWeatherResponse;
import me.ictlinkbd.com.projectalpha.util.ConstantValues;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static me.ictlinkbd.com.projectalpha.util.ConstantValues.WEATHER_URL;


public class ForecastWeatherFragment extends Fragment {

    private ForecastAdapter forecastAdapter;
    private RecyclerView recyclerview;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<ForecastWeatherResponse.ListData> list=new ArrayList<>();
    private double longitude = 0.0;
    private double latitude = 0.0;
    private String units = "metric";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Location location=getArguments().getParcelable("loc");
        if (location!=null){
            latitude=location.getLatitude();
            longitude=location.getLongitude();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forecast_weather, container, false);
        recyclerview = view.findViewById(R.id.lsv_forecast);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerview.setLayoutManager(layoutManager);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        forecastAdapter = new ForecastAdapter(list,getContext(),units);
        retrofitService();
        return view;
    }
    private void refresh() {
        swipeRefreshLayout.setRefreshing(true);

        //refresh long-time task in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //dummy delay for 2 second
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //update ui on UI thread
                (getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        retrofitService();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }
        }).start();
    }

    private void retrofitService() {

        String url = String.format("forecast?lat=%f&lon=%f&units=%s&appid=" + ConstantValues.api_key, latitude, longitude, units);

        Call<ForecastWeatherResponse> call = RetrofitClient.getInstance(WEATHER_URL).getApiService().getForeCastWeather(url);

        call.enqueue(new Callback<ForecastWeatherResponse>() {
            @Override
            public void onResponse(Call<ForecastWeatherResponse> call, Response<ForecastWeatherResponse> response) {
                if (response.code() == 200) {
                    ForecastWeatherResponse forecastWeatherResponse = response.body();
                    list = forecastWeatherResponse.getList();
                    forecastAdapter.updateList(list);
                    forecastAdapter.notifyDataSetChanged();
//                    forecastAdapter = new ForecastAdapter(list,getContext(),units);

                }
            }

            @Override
            public void onFailure(Call<ForecastWeatherResponse> call, Throwable t) {

                Log.e("Error", t.getMessage());
            }
        });

        recyclerview.setAdapter(forecastAdapter);
    }

}
