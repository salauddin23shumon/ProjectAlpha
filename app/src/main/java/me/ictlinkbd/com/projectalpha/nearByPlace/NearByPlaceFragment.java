package me.ictlinkbd.com.projectalpha.nearByPlace;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.isapanah.awesomespinner.AwesomeSpinner;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import me.ictlinkbd.com.projectalpha.MainActivity;
import me.ictlinkbd.com.projectalpha.R;
import me.ictlinkbd.com.projectalpha.interfaces.NavIconShowHideListener;
import me.ictlinkbd.com.projectalpha.nearByPlace.nearByPojo.NearByPlacesResponse;
import me.ictlinkbd.com.projectalpha.nearByPlace.nearByPojo.Result;
import me.ictlinkbd.com.projectalpha.util.ConstantValues;
import me.ictlinkbd.com.projectalpha.util.DataCarrier;
import me.ictlinkbd.com.projectalpha.util.LocationTask;
import me.ictlinkbd.com.projectalpha.util.networking.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static me.ictlinkbd.com.projectalpha.MainActivity.startLocationUpdate;
import static me.ictlinkbd.com.projectalpha.MainActivity.stopLocationUpdate;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.chkGPS;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.chkLocationPermission;
import static me.ictlinkbd.com.projectalpha.util.AppPermission.showAlert;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.ENABLE_GPS_ACCESS;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.REQUEST_LOCATION_ACCESS;
import static me.ictlinkbd.com.projectalpha.util.ConstantValues.NEARBY_PLACE_URL;


public class NearByPlaceFragment extends Fragment implements DroidListener {

    private static final String TAG = "NearByPlaceFragment";
    private AwesomeSpinner distanceSpinner, placeSpinner;
    private String[] placeSpinners;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView searchedResult;
    private String[] distanceSpinners;
    private Toolbar toolbar;
    private ArrayAdapter<String> placeAdapter;
    private ArrayAdapter<String> distanceAdapter;
    private Button findBtn;
    private String placeName = null;
    private String nearbyAreaDistance;
    private double latitude;
    private double longitude;
    //    private double radius=0.5;
    private double nearbyPlaceRadius = 0.5;
    private String nearbySupportedName = null;
    private LocationTask task;
    private ArrayList<Result> nearbyPlacesData = new ArrayList<>();
    private NearByPlaceAdapter nearByPlaceAdapter;

    private View.OnClickListener onClickListener;
    private Location lastLocation;
    private Context context;
    private DroidNet mDroidNet;
    private EventBus bus = EventBus.getDefault();
    private DataCarrier carrier;
    private Dialog alertDialog;


    public NearByPlaceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: called");
        this.context = context;
        task = new LocationTask(context);
        if (!bus.isRegistered(this))
            bus.register(this);
        nearbyPlacesData = new ArrayList<>();
        nearByPlaceAdapter = new NearByPlaceAdapter(context, nearbyPlacesData);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
//        setRetainInstance(true);
        if (chkLocationPermission(context, REQUEST_LOCATION_ACCESS)) {
            if (chkGPS(context))
                startLocationUpdate(task);
            else
                showAlert(context);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_nearby_by_place, container, false);

        findBtn = view.findViewById(R.id.btnFind);
        placeSpinner = view.findViewById(R.id.spinnerPlaceName);
        distanceSpinner = view.findViewById(R.id.spinnerPlaceDistance);
        searchedResult = view.findViewById(R.id.searchResult);
        recyclerView = view.findViewById(R.id.recyclerview);
        swipeRefreshLayout = view.findViewById(R.id.swipe_container);
        toolbar = ((MainActivity) getActivity()).findViewById(R.id.main_toolbar);

        setHasOptionsMenu(true);
        ((NavIconShowHideListener) context).showBackIcon();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(nearByPlaceAdapter);

        placeSpinners = getResources().getStringArray(R.array.NearbyPlace);
        distanceSpinners = getResources().getStringArray(R.array.NearbyPlaceDistance);

        placeAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, placeSpinners);
        placeSpinner.setAdapter(placeAdapter);

        distanceAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, distanceSpinners);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        distanceSpinner.setAdapter(distanceAdapter);


        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar.setTitle("NearBy Places");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getFragmentManager().beginTransaction().remove(NearByPlaceFragment.this).commit();
//                onDetach();
//                getActivity().onBackPressed();
                getFragmentManager().popBackStack();
            }
        });

//        placeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//                placeName = adapterView.getItemAtPosition(position).toString();
//                Log.d(TAG, "onItemSelected: "+placeName);
//                getNearbyPlacesSupportedName(position);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//                Log.d(TAG, "onNothingSelected: ");
////                placeName = "Nearest places";
////                int pos = 0;
//////                getNearbyPlacesSupportedName(pos);
//            }
//        });


        placeSpinner.setSpinnerHint("Select Place");
        placeSpinner.setHintTextColor(Color.BLACK);
        placeSpinner.setDownArrowTintColor(Color.RED);
        placeSpinner.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
            @Override
            public void onItemSelected(int position, String itemAtPosition) {
                getNearbyPlacesSupportedName(position);
                Log.d(TAG, "onItemSelected: "+position);
            }
        });


        distanceSpinner.setSpinnerHint("Select Distance");
        distanceSpinner.setHintTextColor(Color.BLACK);
        distanceSpinner.setDownArrowTintColor(Color.RED);
        distanceSpinner.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
            @Override
            public void onItemSelected(int position, String itemAtPosition) {
                Log.e(TAG, "onItemSelected: "+ position);
                getNearbyPlaceRadius(position);
            }
        });

//        distanceSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(@NotNull MaterialSpinner materialSpinner, @org.jetbrains.annotations.Nullable View view, int i, long l) {
//
//                nearbyAreaDistance = materialSpinner.getAdapter().getItem(i).toString();
//                Log.e(TAG, "onItemSelected: "+ nearbyAreaDistance);
//                getNearbyPlaceRadius(nearbyAreaDistance);
//            }
//
//            @Override
//            public void onNothingSelected(@NotNull MaterialSpinner materialSpinner) {
//                Log.d(TAG, "onNothingSelected: ");
//            }
//        });

//        distanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
////                nearbyAreaDistance = "0.5km";
////                getNearbyPlaceRadius();
//            }
//        });

        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NearByPlaceFragment nearbyByPlace = new NearByPlaceFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction().replace(R.id.fragment_container, nearbyByPlace);
                transaction.commit();
            }
        };


        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNearbyPlaceData(lastLocation.getLatitude(), lastLocation.getLongitude(), nearbyPlaceRadius, nearbySupportedName);
            }
        });
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
                        if (carrier!=null)
                            onEvent(carrier);
//                        if (!bus.isRegistered(this)) {
//                            bus.register(this);
//                            startLocationUpdate(task);
//                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (!bus.isRegistered(this))
            bus.register(this);

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
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
        if (bus.isRegistered(this))
            bus.unregister(this);
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: ");
        mDroidNet.removeInternetConnectivityChangeListener(this);
        ((NavIconShowHideListener) context).showHamburgerIcon();
        if (bus.isRegistered(this))
            bus.unregister(this);
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(DataCarrier carrier) {
        this.carrier=carrier;
        Log.d(TAG, "onEvent: " + carrier.getLocation());
        if (carrier.getLocation() != null) {
            lastLocation = carrier.getLocation();
            getNearbyPlaceData(lastLocation.getLatitude(), lastLocation.getLongitude(), nearbyPlaceRadius, nearbySupportedName);
            bus.unregister(this);
        }
    }


    private void getNearbyPlacesSupportedName(int pos) {
        Log.d(TAG, "getNearbyPlacesSupportedName: "+pos);
        switch (pos) {
            case 0:
                nearbySupportedName = "restaurant";
                break;
            case 1:
                nearbySupportedName = "bank";
                break;
            case 2:
                nearbySupportedName = "atm";
                break;
            case 3:
                nearbySupportedName = "hospital";
                break;
            case 4:
                nearbySupportedName = "shopping_mall";
                break;
            case 5:
                nearbySupportedName = "bus_station";
                break;
            case 6:
                nearbySupportedName = "police";
                break;
            default:
                nearbySupportedName = null;
        }
    }


    private void getNearbyPlaceRadius(int pos) {

        switch (pos) {
            case 0:
                nearbyPlaceRadius = 0.5;
                break;
            case 1:
                nearbyPlaceRadius = 1.0;
                break;
            case 2:
                nearbyPlaceRadius = 1.5;
                break;
            case 3:
                nearbyPlaceRadius = 2.0;
                break;
            case 4:
                nearbyPlaceRadius = 2.5;
                break;
            case 5:
                nearbyPlaceRadius = 3.0;
                break;
            case 6:
                nearbyPlaceRadius = 4.0;
                break;
            case 7:
                nearbyPlaceRadius = 5.0;
                break;
            case 8:
                nearbyPlaceRadius = 10.0;
                break;
            case 9:
                nearbyPlaceRadius = 20.0;
                break;
        }
    }


    private void getNearbyPlaceData(double lat, double lng, double radius, final String placeType) {
        showProgressDialog();
        String key = ConstantValues.Nearby_api_key;
        Log.e(TAG, "getNearbyPlaceData: " + placeType + " r:" + radius);
        String url = String.format("json?location=%f,%f&radius=%f&type=%s&key=%s", lat, lng, (radius * 1000), placeType, key);

        Call<NearByPlacesResponse> nearbyPlaceResponse = RetrofitClient.getInstance(NEARBY_PLACE_URL).getApiService().getNearbyPlaces(url);
        nearbyPlaceResponse.enqueue(new Callback<NearByPlacesResponse>() {
            @Override
            public void onResponse(Call<NearByPlacesResponse> call, Response<NearByPlacesResponse> response) {

                if (response.isSuccessful()) {
                    NearByPlacesResponse placeResponse = response.body();
                    if (placeResponse != null) {
//                        nearbyPlacesData.clear();
                        nearbyPlacesData.addAll(placeResponse.getResults());
                        nearByPlaceAdapter.updatePlaceList(placeResponse.getResults());
                        Log.e(TAG, "onResponse: " + placeResponse.getResults().size());
                        alertDialog.dismiss();
                        stopLocationUpdate(task);
                        if (placeType==null)
                            searchedResult.setText("Places nearest to uou");
                        else
                            searchedResult.setText("Searched Result for: " + placeType);
                    } else {
                        alertDialog.dismiss();
                        Toast.makeText(context, "Server Error !! Please Try Again", Toast.LENGTH_SHORT).show();
                        stopLocationUpdate(task);
                    }
                } else {
                    Log.d(TAG, "onResponse: " + response.code());
                    stopLocationUpdate(task);
                }
            }

            @Override
            public void onFailure(Call<NearByPlacesResponse> call, Throwable t) {
                Log.e("Error", t.getMessage());
                alertDialog.dismiss();
                stopLocationUpdate(task);
            }
        });
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
                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    getFragmentManager().popBackStack();
                    alertDialog.dismiss();
                }
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_GPS_ACCESS)
            startLocationUpdate(task);
        Log.d(TAG, "onActivityResult: " + requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_ACCESS)
            if (chkGPS(context))
                startLocationUpdate(task);
            else
                showAlert(context);
        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (!isConnected)
            Log.d(TAG, "onInternetConnectivityChanged: no internet");
    }
}
