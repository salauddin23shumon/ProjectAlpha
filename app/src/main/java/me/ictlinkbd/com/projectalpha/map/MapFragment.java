package me.ictlinkbd.com.projectalpha.map;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;


import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ictlinkbd.com.projectalpha.R;
import me.ictlinkbd.com.projectalpha.util.DataCarrier;
import me.ictlinkbd.com.projectalpha.util.LocationTask;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static me.ictlinkbd.com.projectalpha.MainActivity.drawer;
import static me.ictlinkbd.com.projectalpha.MainActivity.startLocationUpdate;
import static me.ictlinkbd.com.projectalpha.MainActivity.stopLocationUpdate;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.chkGPS;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.chkLocationPermission;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.showAlert;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.ENABLE_GPS_ACCESS;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.GPS_ENABLED;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.REQUEST_LOCATION_ACCESS;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.place_api_key;


public class MapFragment extends Fragment implements OnMapReadyCallback, DroidListener {

    private MaterialSearchBar searchBar;
    private Dialog alertDialog;
    private MapView mMapView;
    private GoogleMap googleMap;
    private Marker marker = null;
    private LatLng latLng = null;
    private float zoomLevel = 15.5f;
    private Geocoder geocoder;
    private List<Address> predictedAddressList, mapAddresses;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    private final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
    private Location lastLocation;
    private LocationTask location;
    private Context context;
    private DroidNet mDroidNet;
    private EventBus bus = EventBus.getDefault();
    private String TAG = "MapFragment ";


    public MapFragment() {
        //// do not nothing
    }

    @Override
    public void onAttach(Context context) {
        Log.e(TAG, "onAttach: called");
        super.onAttach(context);
        this.context = context;
        location = new LocationTask(context);
        mapAddresses = new ArrayList<>();
        predictedAddressList = new ArrayList<>();
        geocoder = new Geocoder(context);
        Places.initialize(context, place_api_key);
        placesClient = Places.createClient(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: called");
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        searchBar = rootView.findViewById(R.id.searchBar);
        mMapView = rootView.findViewById(R.id.mapView);

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);


        if (chkLocationPermission(context, REQUEST_LOCATION_ACCESS)) {
            mMapView.getMapAsync(MapFragment.this);
            if (chkGPS(context))
                startLocationUpdate(location);
            else
                showAlert(context);
        }


        searchBar.inflateMenu(R.menu.map_menu);
        searchBar.setCardViewElevation(10);
        searchBar.setMaxSuggestionCount(8);
        searchBar.setHint("Search a Place");


        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                stopLocationUpdate(location);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                getActivity().startSearch(text.toString(), true, null, true);
//                showProgressDialog();
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                switch (buttonCode) {
                    case MaterialSearchBar.BUTTON_NAVIGATION:
                        drawer.openDrawer(GravityCompat.START);
                        break;
                    case MaterialSearchBar.BUTTON_SPEECH:
                        break;
                    case MaterialSearchBar.BUTTON_BACK:
                        searchBar.closeSearch();
                        searchBar.clearSuggestions();
                        startLocationUpdate(location);
                        marker.remove();
                        marker = null;
                        break;
                }
            }
        });

        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(charSequence.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                searchBar.updateLastSuggestions(suggestionsList);

//                                if (!searchBar.isSuggestionsVisible()) {
//                                    searchBar.showSuggestionsList();
//                                    Log.d(TAG, "onComplete: this if working");
//                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                Log.d(TAG, "OnItemClickListener: suggestion clicked");
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = searchBar.getLastSuggestions().get(position).toString();
                searchBar.setText(suggestion);

                searchBar.clearSuggestions();
                searchBar.hideSuggestionsList();

                InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {

                        Log.d(TAG, "onSuccess: " + fetchPlaceResponse.toString());
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i("mytag", "Place found: " + place.getName());
                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {
                            setPosition(latLngOfPlace, 1);
                        } else {
                            Log.e(TAG, "onSuccess: latlng not found");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        searchBar.clearSuggestions();
                        searchBar.hideSuggestionsList();
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("mytag", "place not found: " + e.getMessage());
                            Log.i("mytag", "status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {
                Log.d(TAG, "OnItemDeleteListener: clicked");
                searchBar.clearSuggestions();
                searchBar.hideSuggestionsList();
            }
        });

        searchBar.getMenu().setOnMenuItemClickListener(new androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.map_traffic:
                        if (googleMap != null && !googleMap.isTrafficEnabled()) {
                            item.setTitle("Disable Road Traffic");
                            googleMap.setTrafficEnabled(true);
                        } else {
                            item.setTitle("Enable Road Traffic");
                            googleMap.setTrafficEnabled(false);
                        }
                        return true;

                    case R.id.map_road:
                        if (googleMap != null)
                            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        return true;

                    case R.id.map_satellite:
                        if (googleMap != null)
                            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        Log.d(TAG, "onOptionsItemSelected: called");
                        return true;

                    case R.id.map_hybrid:
                        if (googleMap != null)
                            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        return true;

                    case R.id.map_terrain:
                        if (googleMap != null)
                            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        return true;
                    default:
                        return false;
                }
            }
        });

    }


    private void setPosition(LatLng latLng, int fromWhere) {
        MarkerOptions markerOptions = new MarkerOptions();

        Log.d(TAG, "setPosition: " + latLng + "  " + googleMap);

        try {
            mapAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 2);
            markerOptions.position(latLng);
            markerOptions.title(mapAddresses.get(0).getAddressLine(0));


            if (fromWhere == 0) {
                if (marker == null) {
                    Log.d(TAG, "setPosition: if0");
                    marker = googleMap.addMarker(markerOptions);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                } else {
                    Log.d(TAG, "setPosition: else0");
                    marker.remove();
                    marker = googleMap.addMarker(markerOptions);
                    zoomLevel = googleMap.getCameraPosition().zoom;
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, zoomLevel));
                }
            } else {
                if (marker != null) {
                    Log.d(TAG, "setPosition: if1");
                    marker.remove();
                    marker = googleMap.addMarker(markerOptions);
                    zoomLevel = googleMap.getCameraPosition().zoom;
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                } else {
                    Log.d(TAG, "setPosition: else1");
                    marker = googleMap.addMarker(markerOptions);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, zoomLevel));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void showProgressDialog() {
        alertDialog = new Dialog(context);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.map_progress_dialog);
        alertDialog.setCancelable(false);
        final CircularProgressBar circularProgressBar = alertDialog.findViewById(R.id.circular_progressBar);
        circularProgressBar.setProgressDirection(CircularProgressBar.ProgressDirection.TO_RIGHT);
        final TextView progressUpdate = alertDialog.findViewById(R.id.progressTV);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK && alertDialog.isShowing()) {
//                    getFragmentManager().popBackStack();
                    alertDialog.dismiss();
                }
                return true;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        Log.d(TAG, "onResume: called");

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        if (!bus.isRegistered(this)){
            bus.register(this);
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.d(TAG, "onKey: back btn pressed");
                    if (drawer.isDrawerOpen(GravityCompat.START))
                        drawer.closeDrawer(GravityCompat.START);
                    else
                        getActivity().onBackPressed();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        mMapView.onStop();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        stopLocationUpdate(location);
        if (bus.isRegistered(this))
            bus.unregister(this);
        mDroidNet.removeInternetConnectivityChangeListener(this);
        super.onStop();
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        mMapView.onPause();
        stopLocationUpdate(location);
//        if (alertDialog.isShowing())
//            alertDialog.dismiss();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: ");
        stopLocationUpdate(location);
        if (bus.isRegistered(this))
            bus.unregister(this);
        mDroidNet.removeInternetConnectivityChangeListener(this);
        super.onDetach();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(DataCarrier carrier) {
        Log.d(TAG, "onEvent: " + carrier.getLocation());
        if (carrier.getLocation() != null) {
            lastLocation = carrier.getLocation();
            latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            setPosition(latLng, 0);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: called");
        showProgressDialog();
        this.googleMap = googleMap;
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                alertDialog.dismiss();
            }
        });

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (searchBar.isSuggestionsVisible())
                    searchBar.clearSuggestions();
                if (searchBar.isSuggestionsEnabled())
                    searchBar.hideSuggestionsList();
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==ENABLE_GPS_ACCESS){
            startLocationUpdate(location);
        }
        Log.d(TAG, "onActivityResult: "+requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_LOCATION_ACCESS) {
            mMapView.getMapAsync(MapFragment.this);
            if (chkGPS(context))
                startLocationUpdate(location);
            else
                showAlert(context);
        }
        Log.d(TAG, "onRequestPermissionsResult: "+requestCode);
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (!isConnected) {
            Toast.makeText(context, "no internet", Toast.LENGTH_SHORT).show();
            searchBar.setEnabled(false);

        } else {
            searchBar.setEnabled(true);
        }
    }

}
