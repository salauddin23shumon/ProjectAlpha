package me.ictlinkbd.com.projectalpha.nearByPlace;


import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.droidnet.DroidNet;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.SphericalUtil;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.ictlinkbd.com.projectalpha.R;

import me.ictlinkbd.com.projectalpha.nearByPlace.geofencing.GeoFencingOperation;
import me.ictlinkbd.com.projectalpha.nearByPlace.nearByPojo.Photo;
import me.ictlinkbd.com.projectalpha.nearByPlace.nearByPojo.Result;
import me.ictlinkbd.com.projectalpha.util.CustomMapView;
import me.ictlinkbd.com.projectalpha.util.DataCarrier;
import me.ictlinkbd.com.projectalpha.util.LocationTask;

import static me.ictlinkbd.com.projectalpha.MainActivity.startLocationUpdate;
import static me.ictlinkbd.com.projectalpha.MainActivity.stopLocationUpdate;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.chkGPS;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.chkLocationPermission;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.showAlert;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.ENABLE_GPS_ACCESS;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.Nearby_api_key;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.PLACE_PHOTO_URL;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.REQUEST_LOCATION_ACCESS;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.direction_api_key;
import static me.ictlinkbd.com.projectalpha.util.Utility.bitmapDescriptorFromVector;


/**
 * A simple {@link Fragment} subclass.
 */
public class NearByPlaceDetailFragment extends Fragment implements OnMapReadyCallback, RoutingListener {

    private static final String TAG = "PlaceDetailFragment";
    private GoogleMap googleMap;
    private Geocoder geocoder;
    private List<Address> targetMapAddresses, originMapAddress;
    private ArrayList<Route> routes;
    private Result nearbyPlacesData;
    private TextView placeNameTV, placeLocationTV, isOpenedTV, timeTV, distanceTV;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private FloatingActionButton upFab;
    private FloatingActionButton downFab;
    private NestedScrollView nestedView;
    private Switch geofenceBtn;
    private LinearLayout placeInfoLL;
    private RatingBar ratingBar;
    private ImageView placeImage;
    private GeoDataClient geoDataClient;
    private Location currentLocation;
    private final static int ACCESS_LOCATION_REQUEST_CODE = 1;
    private GeoFencingOperation geoFencingOperation;
    private ProgressBar photoProgress;
    private ImageView routeBtn;
    private static final int REQUEST_LOCATION = 1;
    private LocationManager locationManager;
    private LocationTask task;
    private LatLng startLatLng, endLatLng;
    private List<Polyline> polylines = null;

    private float zoomLevel = 15.5f;
    private Marker sourceMarker, destinationMarker = null;
    private Toolbar mapToolbar;
    private CustomMapView mapView;
    private Context context;
    private DroidNet mDroidNet;
    private EventBus bus = EventBus.getDefault();

    public NearByPlaceDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        geocoder = new Geocoder(context);
        task = new LocationTask(context);
        bus.register(this);
        Bundle args = getArguments();
        if (args != null) {
            nearbyPlacesData = (Result) args.getSerializable("Object");
            endLatLng = new LatLng(nearbyPlacesData.getGeometry().getLocation().getLat(), nearbyPlacesData.getGeometry().getLocation().getLng());
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nearby_place_map, container, false);

        geoFencingOperation = new GeoFencingOperation(getActivity());
        targetMapAddresses = new ArrayList<>();
        originMapAddress = new ArrayList<>();
        polylines = new ArrayList<>();
        geoDataClient = Places.getGeoDataClient(getActivity());
        placeNameTV = view.findViewById(R.id.placeNameTV);
        placeLocationTV = view.findViewById(R.id.placeLocatinTV);
        isOpenedTV = view.findViewById(R.id.placeOpenStatus);
        placeImage = view.findViewById(R.id.placesIV);
        photoProgress = view.findViewById(R.id.photoProgrss);
        routeBtn = view.findViewById(R.id.routeIV);
        mapToolbar = view.findViewById(R.id.mapToolbar);
        collapsingToolbar = view.findViewById(R.id.collapsingTB);
        appBarLayout = view.findViewById(R.id.appBarLayout);
        upFab = view.findViewById(R.id.fab);
        downFab = view.findViewById(R.id.fab2);
        mapView = view.findViewById(R.id.mapview);
        nestedView = view.findViewById(R.id.nestedView);
        geofenceBtn = view.findViewById(R.id.geofenceBtn);
        placeInfoLL = view.findViewById(R.id.placeInfoLL);
        distanceTV = view.findViewById(R.id.distanceTV);
        timeTV = view.findViewById(R.id.timeTV);
        ratingBar = view.findViewById(R.id.ratingBar);

        photoProgress.setVisibility(View.VISIBLE);


        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.setViewParent(nestedView);


        if (chkLocationPermission(context, REQUEST_LOCATION_ACCESS)) {
            mapView.getMapAsync(this);
            if (chkGPS(context))
                startLocationUpdate(task);
            else
                showAlert(context);
        }

        getPlacePhoto(nearbyPlacesData);

        routeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: "+polylines);
                if (nearbyPlacesData != null) {
                    if (polylines.isEmpty()){
                        findRoutes(startLatLng, endLatLng);
                    }
                    else {
                        resetPolyLines(polylines);
                        placeInfoLL.setVisibility(View.GONE);
                    }
                }
            }
        });

        geofenceBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (chkGPS(context))
                    geoFencingOperation.addToGeoFencing(
                            nearbyPlacesData.getName(),
                            endLatLng.latitude,
                            endLatLng.longitude);
                else
                    showAlert(context);
            }
        });

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mapToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mapToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getActivity().onBackPressed();
                getFragmentManager().popBackStack();
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                boolean isShow = true;
                int scrollRange = -1;
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle("Place Details");
                    isShow = true;
                    downFab.setVisibility(View.VISIBLE);
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");//careful there should a space between double quote otherwise it wont work
                    isShow = false;
                    downFab.setVisibility(View.GONE);
                }
            }

        });

        upFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appBarLayout.getTop() < 0)
                    appBarLayout.setExpanded(true);
                else
                    appBarLayout.setExpanded(false);
            }
        });

        downFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appBarLayout.getTop() < 0)
                    appBarLayout.setExpanded(true);
                else
                    appBarLayout.setExpanded(false);
            }
        });
    }


    private void getPlacePhoto(Result placeData) {
        List<Photo> photos = placeData.getPhotos();
        String apiKeyNearBy = Nearby_api_key;
        placeNameTV.setText(placeData.getName());
        placeLocationTV.setText(placeData.getVicinity());

        Log.d(TAG, "getPlacePhoto: " + placeData.getRating());
        if (placeData.getRating() != null)
            ratingBar.setRating(placeData.getRating().floatValue());
        else
            ratingBar.setRating(0);
        if (placeData.getOpeningHours() != null && placeData.getOpeningHours().getOpenNow())
            isOpenedTV.setText("Open Now");
        else
            isOpenedTV.setText("Close Now");

        if (photos != null) {
            Log.d(TAG, "getPlacePhoto: " + photos.size());
            if (photos.size() > 0) {
                String photoRef = photos.get(0).getPhotoReference();
                String photoUrl = String.format("%s%s&key=%s", PLACE_PHOTO_URL, photoRef, apiKeyNearBy);
                Picasso.get().load(photoUrl).fit().centerCrop().into(placeImage, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        photoProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        photoProgress.setVisibility(View.GONE);
                    }
                });
            } else {
                Log.d(TAG, "getPlacePhoto photo not null: no photo found");
                photoProgress.setVisibility(View.GONE);
            }
        } else {
            Log.d(TAG, "getPlacePhoto: no photo found");
            photoProgress.setVisibility(View.GONE);
            placeImage.setBackgroundResource(R.drawable.empty_place);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.setMyLocationEnabled(true);
//        if (startLatLng!=null)
//        setPosition(startLatLng, endLatLng, 0);
    }


    private void findRoutes(LatLng start, LatLng end) {


        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .key(direction_api_key)  //also define api key here.
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Log.e(TAG, "onRoutingFailure: " + e.getMessage());
    }

    @Override
    public void onRoutingStart() {
//        Toast.makeText(getContext(), "Finding Route...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {

        placeInfoLL.setVisibility(View.VISIBLE);
        timeTV.setText(routes.get(0).getDurationText());
        distanceTV.setText(routes.get(0).getDistanceText());
        this.routes = routes;

//        if (polylines != null) {
//            polylines.clear();
//        }
        PolylineOptions polyOptions = new PolylineOptions();
        //add route(s) to the map using polyline
        for (int i = 0; i < routes.size(); i++) {

            if (i == shortestRouteIndex) {
                polyOptions.color(getResources().getColor(R.color.red));
                polyOptions.width(10);
                polyOptions.addAll(routes.get(shortestRouteIndex).getPoints());
                polyOptions.geodesic(true);
                polyOptions.jointType(JointType.ROUND);
                polyOptions.startCap(new CustomCap(bitmapDescriptorFromVector(context,R.drawable.ic_navigation_rev)));
                polyOptions.endCap(new CustomCap(bitmapDescriptorFromVector(context,R.drawable.ic_navigation)));
                Polyline polyline = googleMap.addPolyline(polyOptions);
//                polylineStartLatLng = polyline.getPoints().get(0);
                int k = polyline.getPoints().size();
//                polylineEndLatLng = polyline.getPoints().get(k - 1);
                polylines.add(polyline);

            } else {
                Log.d(TAG, "onRoutingSuccess: in empty");
            }

        }

        setPosition(startLatLng, endLatLng, 1);

    }

    @Override
    public void onRoutingCancelled() {

    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(DataCarrier carrier) {
        currentLocation = carrier.getLocation();
        Log.d(TAG, "onEvent: " + currentLocation);

        if (currentLocation != null) {
            startLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            setPosition(startLatLng, endLatLng, 0);
            if (!polylines.isEmpty()){
                resetPolyLines(polylines);
                findRoutes(startLatLng,endLatLng);
            }
        }
    }


    private void setPosition(LatLng origin, LatLng target, int fromWhere) {
        MarkerOptions startMarkerOptions = new MarkerOptions();
        MarkerOptions endMarkerOptions = new MarkerOptions();

        Log.e(TAG, "setPosition: " + origin + "\n" + target);

        try {
            targetMapAddresses = geocoder.getFromLocation(target.latitude, target.longitude, 2);
            endMarkerOptions
                    .position(target)
                    .title(targetMapAddresses.get(0).getAddressLine(0));

            originMapAddress = geocoder.getFromLocation(origin.latitude, origin.longitude, 2);
            startMarkerOptions
                    .position(origin)
                    .title(originMapAddress.get(0).getAddressLine(0));

            if (fromWhere == 0) {
                if (sourceMarker==null) {
                    sourceMarker = googleMap.addMarker(startMarkerOptions
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                            .snippet("it's me"));
                    destinationMarker = googleMap.addMarker(endMarkerOptions
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .snippet("destination"));

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, zoomLevel));
                }else {
                    sourceMarker.remove();
                    zoomLevel = googleMap.getCameraPosition().zoom;
                    sourceMarker = googleMap.addMarker(startMarkerOptions
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                            .snippet("it's me"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, zoomLevel));
                }
            } else {
                Log.d(TAG, "1 ");
                destinationMarker.remove();
                sourceMarker.remove();

                sourceMarker = googleMap.addMarker(startMarkerOptions
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .snippet("it's me"));
                destinationMarker = googleMap.addMarker(endMarkerOptions
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .snippet(routes.get(0).getDistanceText()));
                zoomLevel = googleMap.getCameraPosition().zoom;
                Log.d(TAG, "setPosition: " + zoomLevel);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, zoomLevel));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void resetPolyLines(List<Polyline> lines){
        for(Polyline line : lines)
            line.remove();
        lines.clear();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_GPS_ACCESS)
            startLocationUpdate(task);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_ACCESS)
            mapView.getMapAsync(this);
        if (chkGPS(context))
            startLocationUpdate(task);
        else
            showAlert(context);
        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        if (!bus.isRegistered(this)) {
            bus.register(this);
            startLocationUpdate(task);
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.d(TAG, "onKey: back btn pressed");
                    getFragmentManager().popBackStack();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        stopLocationUpdate(task);
        if (bus.isRegistered(this))
            bus.unregister(this);
        super.onStop();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}




