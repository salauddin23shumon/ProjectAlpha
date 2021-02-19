package com.sss.bitm.projectalpha.tourEvent.eventFragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.sss.bitm.projectalpha.R;
import com.sss.bitm.projectalpha.tourEvent.eventAdapter.EventAdapter;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Event;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Users;
import com.sss.bitm.projectalpha.util.PullData;

public class EventListFragment extends Fragment implements EventAdapter.EventListActionListener {

    private static final String TAG = "EventListFragment";
    private Context context;
    private TextInputEditText eventName, eventSource, eventDestination, eventBudget, eventDate;
    private EventListFragmentListener eventListFragmentListener;
    private List<Event> eventsList = new ArrayList<>();
    private PullData pullData;
    private EventAdapter adapter;
    private DatabaseReference eventRef;
    private Users currentUser;
    private int mYear, mMonth, mDay;

    private DatePickerDialog.OnDateSetListener user_given_Date =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                    SimpleDateFormat sDF1 = new SimpleDateFormat("dd-MM-yyyy");
                    final Calendar calendar = Calendar.getInstance();
                    calendar.set(y, m, d);
                    eventDate.setText(sDF1.format(calendar.getTime()));
                }
            };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        pullData = new PullData(context);
        adapter = new EventAdapter(context, eventsList, this);
        currentUser = pullData.getUser();
        eventListFragmentListener = (EventListFragmentListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventRef = pullData.getRootRef().child(currentUser.getUserId()).child("events");
        getEventData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);
        FloatingActionButton floatingActionButton = rootView.findViewById(R.id.eventAddBtn);
        RecyclerView recyclerView = rootView.findViewById(R.id.eventListRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventListFragmentListener.onEventAdd();
            }
        });
        adapter.updateList(eventsList);
        return rootView;
    }


    private void getEventData() {
        pullData.getEventData(eventRef, new PullData.EventCallback() {
            @Override
            public void onEventCallback(List<Event> eventList) {
                adapter.updateList(eventList);
                Log.d("read data", "onEventCallback: " + eventList.size());
            }
        });
    }


    @Override
    public void onEventEdit(final Event event) {

        Bundle bundle = new Bundle();
        bundle.putSerializable("event", event);
        EventAddFragment eventFragment = new EventAddFragment();
        eventFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, eventFragment).addToBackStack(null).commit();

    }

    @Override
    public void onEventDelete(final String eventID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Deleting Alert!");
        builder.setMessage("Are you sure want to delete this event?");
        builder.setCancelable(true);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "Event is not deleted...", Toast.LENGTH_SHORT).show();
            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "Delete Successful", Toast.LENGTH_SHORT).show();
                eventRef.child(eventID).removeValue();
                getEventData();

            }
        });
        builder.show();
    }


    public interface EventListFragmentListener {
        void onLogOutComplete();
        void onEventAdd();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
        getEventData();
    }
}
