package me.ictlinkbd.com.projectalpha.util.networking;


import me.ictlinkbd.com.projectalpha.nearByPlace.mapDirectionPojo.MapDirectionResponse;
import me.ictlinkbd.com.projectalpha.nearByPlace.nearByPojo.NearByPlacesResponse;
import me.ictlinkbd.com.projectalpha.weather.weatherDataModel.CurrentWeatherResponse;
import me.ictlinkbd.com.projectalpha.weather.weatherDataModel.ForecastWeatherResponse;
import me.ictlinkbd.com.projectalpha.weather.weatherDataModel.SearchedWeatherResponse;
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
