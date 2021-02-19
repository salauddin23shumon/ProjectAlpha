package com.sss.bitm.projectalpha.tourEvent.eventFragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.sss.bitm.projectalpha.R;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Event;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Users;
import com.sss.bitm.projectalpha.util.PullData;


public class EventAddFragment extends Fragment {

    private EventAddFragmentListener mListener;
    private Calendar myCalendar = Calendar.getInstance();
    private Context context;

    private TextInputEditText eventNameET, eventSourceET, eventDestinationET, eventBudgetET, eventDateET;
    private String eID, name, source, destination, date, budget;
    private Button btnAddEvent;
    private Toolbar toolbar;
    private String mUserClickDate = "";

    private DatabaseReference eventRef;
    private Event event;
    private Users user;
    private PullData pullData;
    String TAG = "EventAddFragment";


    private DatePickerDialog.OnDateSetListener startingDate =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                    SimpleDateFormat sDF1 = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                    myCalendar.set(Calendar.YEAR, y);
                    myCalendar.set(Calendar.MONTH, m);
                    myCalendar.set(Calendar.DAY_OF_MONTH, d);
                    mUserClickDate = sDF1.format(myCalendar.getTime());
                    eventDateET.setText(mUserClickDate);
                }
            };

    public EventAddFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        pullData = new PullData(context);
        mListener = (EventAddFragmentListener) context;
        user = pullData.getUser();
        eventRef = pullData.getRootRef().child(user.getUserId()).child("events");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("event")) {
            event = (Event) bundle.getSerializable("event");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_event_add, container, false);

        eventNameET = rootView.findViewById(R.id.eventNameET);
        eventSourceET = rootView.findViewById(R.id.sourceET);
        eventDestinationET = rootView.findViewById(R.id.destinationET);
        eventDateET = rootView.findViewById(R.id.dateET);
        eventBudgetET = rootView.findViewById(R.id.budgetET);
        btnAddEvent = rootView.findViewById(R.id.btn_add_event);
        toolbar = rootView.findViewById(R.id.toolbar);


        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setTitle("Tour Event");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getFragmentManager().popBackStack();
            }
        });



        eventDateET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    DatePickerDialog datePiker = new DatePickerDialog
                            (context, startingDate, myCalendar
                                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                    myCalendar.get(Calendar.DAY_OF_MONTH));
                    datePiker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                    datePiker.show();
                }
            }
        });


        if (event != null) {
            eventNameET.setText(event.getName());
            eventSourceET.setText(event.getSource());
            eventDestinationET.setText(event.getDestination());
            eventDateET.setText(event.getDepartureDate());
            eventBudgetET.setText(String.valueOf(event.getBudget()));
            btnAddEvent.setText(context.getString(R.string.update_event));
        }

        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate())
                    saveEvent();
                else
                    Log.d(TAG, "onClick: validation failed");
            }
        });

        return rootView;
    }

    private void saveEvent() {
        final String keyId;
        if (event == null) {
            keyId = eventRef.push().getKey();
            Event event = new Event(keyId, name, source, destination, mUserClickDate, Double.parseDouble(budget), null, null);
            eventRef.child(keyId).setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mListener.onEventAddComplete();
                    Toast.makeText(context, "event add successfully.....", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "event not add.....", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            keyId = event.getEventId();
            eventRef.child(keyId).child("name").setValue(name);
            eventRef.child(keyId).child("source").setValue(source);
            eventRef.child(keyId).child("destination").setValue(destination);
            eventRef.child(keyId).child("departureDate").setValue(date);
            eventRef.child(keyId).child("budget").setValue(Double.parseDouble(budget)).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mListener.onEventAddComplete();
                    Toast.makeText(context, "event updated successfully.....", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "event not update.....", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    private boolean validate() {

        initialize();

        boolean valid = true;
        if (name.isEmpty() || name.length() > 32) {
            pullData.showToast("Please Enter valid name");
            valid = false;
        } else if (!name.matches("[a-z A-Z0-9.@'_]*")) {
            pullData.showToast("Please enter valid name without special symbol");
            valid = false;
        } else if (source.isEmpty() || source.length() > 32) {
            pullData.showToast("Please Enter valid Starting Location");
            valid = false;
        } else if (!source.matches("[a-z A-Z0-9.@'_]*")) {
            pullData.showToast("Please enter valid name without special symbol");
            valid = false;
        } else if (destination.isEmpty() || destination.length() > 32) {
            pullData.showToast("Please Enter valid destination");
            valid = false;
        } else if (!destination.matches("[a-z A-Z0-9.@'_]*")) {
            pullData.showToast("Please enter valid name without special symbol");
            valid = false;
        } else if (date.isEmpty()) {
            pullData.showToast("Please select date");
            valid = false;
        } else if (budget.isEmpty()) {
            pullData.showToast("Please Enter Event Budget");
            valid = false;
        }
        return valid;
    }

    private void initialize() {
        name = eventNameET.getText().toString();
        source = eventSourceET.getText().toString();
        destination = eventDestinationET.getText().toString();
        date = eventDateET.getText().toString();
        budget = eventBudgetET.getText().toString();
    }

    public interface EventAddFragmentListener {
        void onEventAddComplete();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        Log.d(TAG, "onStop: ");
    }


}
