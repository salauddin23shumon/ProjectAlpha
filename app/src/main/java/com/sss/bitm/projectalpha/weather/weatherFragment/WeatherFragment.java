package com.sss.bitm.projectalpha.weather.weatherFragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sss.bitm.projectalpha.R;

import com.sss.bitm.projectalpha.util.networking.RetrofitClient;
import com.sss.bitm.projectalpha.weather.weatherDataModel.CurrentWeatherResponse;
import com.sss.bitm.projectalpha.util.ConstantValues;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sss.bitm.projectalpha.util.ConstantValues.WEATHER_URL;

public class WeatherFragment extends Fragment {

    private TextView tvCity, tvTemp, tvStatus, tvDetails, tvHumidity, tvPressure, tvMintemp, tvMaxtemp;
    private Double latitude = 0.0, longitude = 0.0;
    private ProgressDialog progress;
    private String city, country, main;
    double temp;

    private AppCompatImageView tempImg, backImage;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static final String TAG = WeatherFragment.class.getSimpleName();
    private String unit = "metric";
    private String urlString;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getArguments().getParcelable("loc") != null) {
            Location location = getArguments().getParcelable("loc");
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        Log.e(TAG, "onAttach: " + latitude);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_weather, container, false);
        progress = new ProgressDialog(getActivity(), R.style.AppCompatAlertDialogStyle);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayouts);
        tempImg = view.findViewById(R.id.temp_img);
        backImage = view.findViewById(R.id.back_img);

        tvCity = view.findViewById(R.id.tv_city);
        tvTemp = view.findViewById(R.id.tv_temps);
        tvStatus = view.findViewById(R.id.tv_status);
        tvHumidity = view.findViewById(R.id.humidtyWether);
        tvPressure = view.findViewById(R.id.pressureWeather);
        tvMaxtemp = view.findViewById(R.id.temMax);
        tvMintemp = view.findViewById(R.id.temMin);

        getCurrentWeather();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        Log.e(TAG, "onCreateView: " + latitude);
        return view;
    }


    private void refresh() {
        swipeRefreshLayout.setRefreshing(true);
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
                        getCurrentWeather();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }
        }).start();
    }

    private void getCurrentWeather() {
        Log.e(TAG, "getCurrentWeather: " + latitude);
        progress = new ProgressDialog(getContext());
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Loading");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(getActivity()).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                    PorterDuff.Mode.SRC_IN);
            progress.setIndeterminateDrawable(drawable);
        }
        progress.show();

        urlString = String.format("weather?lat=%f&lon=%f&units=%s&appid=" +ConstantValues.api_key, latitude, longitude, unit );
        Call<CurrentWeatherResponse> call = RetrofitClient.getInstance(WEATHER_URL).getApiService().getCurrentWeather(urlString);

        call.enqueue(new Callback<CurrentWeatherResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onResponse(Call<CurrentWeatherResponse> call, Response<CurrentWeatherResponse> response) {
                response.message();
                try {
                    if (response.code() == 200) {

                        CurrentWeatherResponse currentWeatherResponse = response.body();
                        city = currentWeatherResponse.getName();
                        temp = currentWeatherResponse.getMain().getTemp();
                        country = currentWeatherResponse.getSys().getCountry();
                        main = currentWeatherResponse.getWeather().get(0).getMain();
                        int humidity = currentWeatherResponse.getMain().getHumidity();
                        double pressure = currentWeatherResponse.getMain().getPressure();
                        double maxTemp = currentWeatherResponse.getMain().getTempMax();
                        double minTemp = currentWeatherResponse.getMain().getTempMin();
                        String mains = currentWeatherResponse.getWeather().get(0).getDescription();


                        /*Log.e("got", String.valueOf(humidity));
                        Log.e("got", String.valueOf(pressure));
                        Log.e("got", String.valueOf(minTemp));
                        Log.e("got", String.valueOf(maxTemp));*/


                        if (main.equals("Drizzle")) {
                            backImage.setImageResource(R.drawable.ic_if_mag);
                        } else if (main.equals("Clouds")) {
                            backImage.setImageResource(R.drawable.ic_clouds);
                        } else if (main.equals("Clear")) {
                            backImage.setImageResource(R.drawable.ic_weather);
                        } else if (main.equals("Haze")) {
                            backImage.setImageResource(R.drawable.ic_haze);
                        } else {
                            backImage.setImageResource(R.drawable.ic_weather);
                        }
                        tvStatus.setText(mains);


                        /*tvDetails.setText(

                                "Humidity: "+currentWeatherResponse.getMain().getHumidity()+"\n" +
                                        "Pressure: "+ currentWeatherResponse.getMain().getPressure()+"\n" +
                                        "Maximum Temperature: "+currentWeatherResponse.getMain().getTempMax()+"\n" +
                                        "Minimum Temperature: "+currentWeatherResponse.getMain().getTempMin()
                        );*/


                        if (city.equals("Bangshal")) {
                            tvCity.setText("Dhaka" + " , " + country);
                        } else {
                            tvCity.setText(city + " , " + country);
                        }

                        tvTemp.setText(Double.toString(temp));
                        if (unit.equals("metric")) {
                            tempImg.setImageResource(R.drawable.cel);
                        } else {
                            tempImg.setImageResource(R.drawable.fah);
                        }
                        // Toast.makeText(getActivity(), ""+humidity+pressure+maxTemp+minTemp, Toast.LENGTH_SHORT).show();

                        tvHumidity.setText(String.valueOf(humidity) + "%");
                        tvPressure.setText(String.valueOf(pressure) + "mb");
                        tvMaxtemp.setText(String.valueOf(maxTemp) + "°");
                        tvMintemp.setText(String.valueOf(minTemp) + "°");
                        progress.dismiss();
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {
                Log.d("onFailure", t.toString());
                progress.dismiss();
            }
        });
    }
}
