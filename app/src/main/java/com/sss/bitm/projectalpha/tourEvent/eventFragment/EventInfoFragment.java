package com.sss.bitm.projectalpha.tourEvent.eventFragment;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sss.bitm.projectalpha.MainActivity;
import com.sss.bitm.projectalpha.R;
import com.sss.bitm.projectalpha.interfaces.NavIconShowHideListener;
import com.sss.bitm.projectalpha.tourEvent.eventInterface.OpenGalleryFragment;
import com.sss.bitm.projectalpha.tourEvent.eventAdapter.ExpandableListViewAdapter;
import com.sss.bitm.projectalpha.tourEvent.eventAdapter.ExpenseAdapter;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Event;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Expense;
import com.sss.bitm.projectalpha.util.PullData;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventInfoFragment extends Fragment implements ExpenseAdapter.ExpenseItemAction {

    private ProgressBar mProgressBar;
    private LinearLayout rootLayout;
    private ExpandableListView mExpandableListView;
    private TextView mEventName, mEventBudget, budget_remain, cash_remainTV;
    private Event mEvent;
    private Context context;
    private Toolbar toolbar;
    private String title = "Tour Info";
    public String TAG="EventInfoFragment";

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference eventNodeReference,eventRef, expenseRef;
    private EventInfoFragmentListener eventInfoFragmentListener;
    private OpenGalleryFragment openGallery;

    private PullData pullData;
    private AlertDialog addExpenseDlg, expDialog;

    private ExpenseAdapter adapter;
    private List<Expense> expenseList = new ArrayList<>();


    private DecimalFormat df = new DecimalFormat("#.##");
    private double eventBudget;

    public EventInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        pullData = new PullData(context);
        eventInfoFragmentListener = (EventInfoFragmentListener) context;
        openGallery= (OpenGalleryFragment) context;
        adapter = new ExpenseAdapter(expenseList, context, this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        eventNodeReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Events");
        eventRef=pullData.getRootRef().child(pullData.getUser().getUserId()).child("events").child(mEvent.getEventId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_event_info, container, false);

        ((NavIconShowHideListener) getActivity()).showBackIcon();
        toolbar = ((MainActivity) getActivity()).findViewById(R.id.main_toolbar);

        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        rootLayout = rootView.findViewById(R.id.root_layout);
        mExpandableListView = rootView.findViewById(R.id.ui_expandable_list_view);
        mProgressBar = rootView.findViewById(R.id.ui_progressBar);
        mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.gradient_progress));
        mEventName = rootView.findViewById(R.id.ui_eInfo_name);
        mEventBudget = rootView.findViewById(R.id.ui_eInfo_budget);
        budget_remain = rootView.findViewById(R.id.remainingTV);
        cash_remainTV = rootView.findViewById(R.id.cash_remainTV);


        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("event")) {
            mEvent = (Event) bundle.getSerializable("event");
            eventBudget = mEvent.getBudget();
            expenseRef = pullData.getRootRef().child(pullData.getUser().getUserId()).child("events").child(mEvent.getEventId()).child("expense");

            if (mEvent.getExpense() != null) {
                Map<String, Expense> expenseMap = mEvent.getExpense();
                expenseList = new ArrayList<>(expenseMap.values());
                Log.d(TAG, "expenseList: " + expenseList.size());
                adapter.updateList(expenseList);
                updateBudgetProgress(eventBudget,getTotalExpense(expenseList));
            }
            else
                mEventBudget.setText("Budget Status (" + eventBudget+ ")");
        }else
            Log.d(TAG, "onCreateView: bundle=null");
        mEventName.setText(mEvent.getName());


        ExpandableListViewAdapter adapter = new ExpandableListViewAdapter(getActivity(), this);
        mExpandableListView.setAdapter(adapter);

        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (groupPosition == 0 && childPosition == 0) {
                    addExpense();
                } else if (groupPosition == 0 && childPosition == 1) {
                    showExpense();
                } else if (groupPosition == 0 && childPosition == 2) {
                    addBudget(eventBudget);
                } else if (groupPosition == 1 && childPosition == 0) {
                    eventInfoFragmentListener.openPhotoFragment(mEvent);
                } else if (groupPosition == 1 && childPosition == 1) {
                    openGallery.onGalleryOpenListener(mEvent);
                }else if (groupPosition == 2 && childPosition == 0) {

                    // go to add_fragment_page with values
                    eventInfoFragmentListener.open_event_add_fragment_page(mEvent);

                } else if (groupPosition == 2 && childPosition == 1) {
                    // For Delete Options
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
                            eventNodeReference.child(mEvent.getEventId()).removeValue();
                            eventInfoFragmentListener.back_to_event_list_page();
                        }
                    });
                    builder.show();
                }
                return false;
            }
        });

        return rootView;
    }

    private void addBudget(final double budget) {
        final View view = LayoutInflater.from(context).inflate(R.layout.single_input_dialog, null);
        final EditText editText = view.findViewById(R.id.editText_dlg);
        editText.setHint("Add Budget");
        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final AlertDialog addBudgetDlg = new AlertDialog.Builder(context)
                .setTitle("Add Budget")
                .setMessage("your current eventBudget is: "+budget)
                .setView(view)
                .setNegativeButton("cancel", null)
                .setPositiveButton("add", null)
                .setCancelable(false)
                .create();

        addBudgetDlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button saveBtn = addBudgetDlg.getButton(AlertDialog.BUTTON_POSITIVE);
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Double newBudget = Double.parseDouble(editText.getText().toString())+budget;
                        if (editText.getText().toString().isEmpty()) {
                            addBudgetDlg.setTitle("Please insert input");
                        } else {
                            eventRef.child("budget").setValue(newBudget).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "New Budget Added Successfully", Toast.LENGTH_SHORT).show();
                                    pullData.getSingleEventData(eventRef, new PullData.SingleEventCallBack() {
                                        @Override
                                        public void onSingleEventCallBack(Event event) {
                                            eventBudget=event.getBudget();
                                            updateBudgetProgress(eventBudget,getTotalExpense(expenseList));
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Budget Update Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                            addBudgetDlg.dismiss();
                        }
                    }
                });
            }
        });
        addBudgetDlg.show();
    }

    private void updateBudgetProgress(double budget, double totalExpense) {
        Log.d(TAG, "budget: " + budget);
        Log.d(TAG, "budget: " + totalExpense);
        cash_remainTV.setVisibility(View.VISIBLE);
        double remainingBudget;
        mEventBudget.setText("Budget Status (" + budget + ")");
        mProgressBar.setMax((int) budget);
        remainingBudget = (budget - totalExpense);
        double ratio = (totalExpense * 100) / budget;
        int amountProgress = (int) (budget - remainingBudget);
        cash_remainTV.setText("Budget remain= "+df.format(remainingBudget));
        mProgressBar.setProgress(amountProgress);
        budget_remain.setText(df.format(ratio) + "%");
        Log.d(TAG, "remainingBudget: " + remainingBudget);


    }

    public double getTotalExpense(List<Expense> expenseList){
        double totalExpense=0;
        for (Expense expense : expenseList) {
            totalExpense = totalExpense + expense.getExpenseCost();
        }
        Log.d(TAG, "totalExpense: " + totalExpense);
        return totalExpense;
    }


    private void showExpense() {
        expDialog = new AlertDialog.Builder(context)
                .setTitle("Expense")
                .setNegativeButton("back", null)
                .create();
        final View view = LayoutInflater.from(context).inflate(R.layout.all_expense_dlg, null);
        TextView totalTV=view.findViewById(R.id.totalTV);
        totalTV.setText("Total Cost= "+getTotalExpense(expenseList));
        RecyclerView recyclerView = view.findViewById(R.id.expenseRV);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        expDialog.setView(view);
        expDialog.show();
    }

    private void addExpense() {

        final View view = LayoutInflater.from(context).inflate(R.layout.add_expense_dialog, null);
        final EditText expenseNameET = view.findViewById(R.id.expense_nameET);
        final EditText expenseAmountET = view.findViewById(R.id.expense_amountET);
        addExpenseDlg = new AlertDialog.Builder(context)
                .setTitle("add expense")
                .setMessage("Input New Expense")
                .setView(view)
                .setNegativeButton("cancel", null)
                .setPositiveButton("save", null)
                .setCancelable(false)
                .create();

        addExpenseDlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button saveBtn = addExpenseDlg.getButton(AlertDialog.BUTTON_POSITIVE);
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String expenseName = expenseNameET.getText().toString();
                        final String expense = expenseAmountET.getText().toString();
                        float amount=Float.parseFloat(expense);
                        if (expenseAmountET.getText().toString().isEmpty()) {
                            addExpenseDlg.setTitle("Please insert input");
                            Log.d(TAG, "onClick: negative");
                        } else if (amount>eventBudget) {
                            Toast.makeText(context, "amount is exceeding your budget", Toast.LENGTH_SHORT).show();
                        }else {
                            sendToDb(expenseName, expense);
                            Log.d(TAG, "onClick: positive");
                        }
                    }
                });
            }
        });
        addExpenseDlg.show();
    }

    private void sendToDb(String expenseName, String expenseAmount) {
        String expenseKeyId = expenseRef.push().getKey();
        Expense expense = new Expense(expenseKeyId, expenseName, Double.parseDouble(expenseAmount));
        expenseRef.child(expenseKeyId).setValue(expense).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pullData.getExpenseData(expenseRef, new PullData.ExpenseCallback() {
                    @Override
                    public void onExpenseCallback(List<Expense> expense) {
                        adapter.updateList(expense);
                        expenseList=expense;
                        updateBudgetProgress(eventBudget,getTotalExpense(expenseList));
                    }
                });
                Toast.makeText(context, "save successfully", Toast.LENGTH_SHORT).show();
                addExpenseDlg.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Data could not be saved.", Toast.LENGTH_SHORT).show();
                addExpenseDlg.dismiss();
            }
        });
    }

    @Override
    public void onExpenseDelete(final Expense expense) {
        final AlertDialog alertBuilder = new AlertDialog.Builder(context)
                .setTitle("Deleting Alert!")
                .setMessage("Are you sure want to delete this expense?")
                .setNegativeButton("no", null)
                .setPositiveButton("yse", null)
                .setCancelable(false)
                .create();
        alertBuilder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button deleteBtn = alertBuilder.getButton(AlertDialog.BUTTON_POSITIVE);
                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        expenseRef.child(expense.getExpenseId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                pullData.getExpenseData(expenseRef, new PullData.ExpenseCallback() {
                                    @Override
                                    public void onExpenseCallback(List<Expense> expense) {
                                        adapter.updateList(expense);
                                        expenseList=expense;
                                        updateBudgetProgress(eventBudget,getTotalExpense(expenseList));
                                        Toast.makeText(context, "Delete Successful", Toast.LENGTH_SHORT).show();
                                        showSnackbar();
                                        alertBuilder.dismiss();
                                        if (expDialog.isShowing())
                                            expDialog.dismiss();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        alertBuilder.show();
    }

    public void showSnackbar(){
        final Snackbar snackbar=Snackbar.make(rootLayout, Html.fromHtml("<font color=\"#FA0707\"><b>No internet</b></font>"),Snackbar.LENGTH_INDEFINITE);
        snackbar.setDuration(10000);
        snackbar.show();

        snackbar.setAction("", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call your action method here
                snackbar.dismiss();
            }
        });
    }


    @Override
    public void onDetach() {
        ((NavIconShowHideListener) getActivity()).showHamburgerIcon();
        super.onDetach();
    }

    public interface EventInfoFragmentListener {
        void back_to_event_list_page();

        void open_event_add_fragment_page(Event eventObject);

        void openPhotoFragment(Event event);
    }

}
