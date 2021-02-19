package com.sss.bitm.projectalpha.util.networking;


import com.sss.bitm.projectalpha.nearByPlace.mapDirectionPojo.MapDirectionResponse;
import com.sss.bitm.projectalpha.nearByPlace.nearByPojo.NearByPlacesResponse;
import com.sss.bitm.projectalpha.weather.weatherDataModel.CurrentWeatherResponse;
import com.sss.bitm.projectalpha.weather.weatherDataModel.ForecastWeatherResponse;
import com.sss.bitm.projectalpha.weather.weatherDataModel.SearchedWeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiServiceInterface {


    @GET()
    Call<NearByPlacesResponse> getNearbyPlaces(@Url String url);

    @GET()
    Call<MapDirectionResponse> getMapDirections(@Url String url);

    @GET()
    Call<CurrentWeatherResponse> getCurrentWeather(@Url String urlString);

    @GET()
    Call<ForecastWeatherResponse> getForeCastWeather(@Url String forecastURL);


    @GET()
    Call<SearchedWeatherResponse> getSearchedWeather(@Url String searchedURL);


}
