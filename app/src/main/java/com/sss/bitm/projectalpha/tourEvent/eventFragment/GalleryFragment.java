package com.sss.bitm.projectalpha.tourEvent.eventFragment;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sss.bitm.projectalpha.R;
import com.sss.bitm.projectalpha.tourEvent.eventAdapter.GalleryAdapter;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Event;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Images;
import com.sss.bitm.projectalpha.util.PullData;

import static com.sss.bitm.projectalpha.util.AppPermission.chkStoragePermission;


public class GalleryFragment extends Fragment implements GalleryAdapter.GalleryAdapterItemListener, View.OnLongClickListener {

    private static final int REQUEST_STORAGE_ACCESS = 11;
    private List<Images> imagesList;
    private List<Images> selectionList;
    private GalleryAdapter adapter;
    private DatabaseReference imageRef;
    private Context context;
    private RecyclerView recyclerView;
    private String eventId;
    private PullData pullData;
    private Event event;
    String TAG = "Gallery ";
    private Toolbar toolbar;
    public static boolean isContextualModeEnabled = false;

//    TextView itemCounter;
    int counter = 0;

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        pullData = new PullData(context);
        imagesList = new ArrayList<>();
        selectionList = new ArrayList<>();
        adapter = new GalleryAdapter(imagesList, context, this);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("event")) {
            event = (Event) getArguments().getSerializable("event");
            eventId = event.getEventId();
//            Log.d("PhotoFragment", "eventid : " + eventId);
            imageRef = pullData.getRootRef().child(pullData.getUser().getUserId()).child("events").child(eventId).child("images");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        setHasOptionsMenu(true);
//        ((NavIconShowHideListener) getActivity()).showBackIcon();
        toolbar = view.findViewById(R.id.gallery_toolbar);

        recyclerView = view.findViewById(R.id.galleryRV);
        recyclerView.setHasFixedSize(true);
        setLayoutGrid();
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(adapter.item).attachToRecyclerView(recyclerView);   // swipe right to delete item from recyclerView

        toolbar.setTitle("Tour Gallery");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isContextualModeEnabled) {
                    removeContextualActionMode();
                }else {
//                    getActivity().onBackPressed();
                    getFragmentManager().popBackStack();
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        chkStoragePermission(((Activity)context),REQUEST_STORAGE_ACCESS);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLayoutGrid();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        downloadImages();
    }

    private void downloadImages() {
        pullData.getImageData(imageRef, new PullData.ImageCallback() {
            @Override
            public void onImageCallback(List<Images> images) {
                adapter.updateList(images);
                imagesList.addAll(images);
            }
        });
    }

    public void setLayoutGrid() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.v("TAG", "Landscape !!!");
            recyclerView.setLayoutManager(new GridLayoutManager(context, 5));
        } else {
            Log.v("TAG", "Portrait !!!");
            recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        }
    }


    @Override
    public void onItemDelete(final Map<String, Uri> map) {
        Log.e(TAG, "onItemDelete: "+map.size() );
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Deleting Alert!");
        builder.setMessage("Are you sure want to delete this image?");
        builder.setCancelable(true);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "Image is not deleted...", Toast.LENGTH_SHORT).show();
            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "Delete Successful", Toast.LENGTH_SHORT).show();
                for (Map.Entry<String, Uri> entry : map.entrySet()) {
                    deleteFromCache(entry.getValue());
                    Log.e(TAG, "onClick: "+entry.getKey() );
                    imageRef.child(entry.getKey()).removeValue();
                }
                downloadImages();
                adapter.clearMap();
            }
        });
        builder.show();
    }

    private void deleteFromCache(Uri uri) {
        File fdelete = new File(uri.getPath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + uri.getPath());
            } else {
                System.out.println("file not Deleted :" + uri.getPath());
            }
        }
    }



    @Override
    public void onItemEdit(final Images image) {

        final View view = LayoutInflater.from(context).inflate(R.layout.single_input_dialog, null);
        final EditText editText = view.findViewById(R.id.editText_dlg);
        editText.setText(image.getImageName());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        final AlertDialog addBudgetDlg = new AlertDialog.Builder(context)
                .setTitle("Edit Image Name")
                .setView(view)
                .setNegativeButton("cancel", null)
                .setPositiveButton("save", null)
                .setCancelable(false)
                .create();

        addBudgetDlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button saveBtn = addBudgetDlg.getButton(AlertDialog.BUTTON_POSITIVE);
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String imgName = editText.getText().toString();
                        if (editText.getText().toString().isEmpty()) {
                            addBudgetDlg.setTitle("Please insert input");
                        } else {
                            imageRef.child(image.getImageId()).child("imageName").setValue(imgName).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Name edited Successfully", Toast.LENGTH_SHORT).show();
                                    pullData.getImageData(imageRef, new PullData.ImageCallback() {
                                        @Override
                                        public void onImageCallback(List<Images> images) {
                                            adapter.updateList(images);
                                            addBudgetDlg.dismiss();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show();
                                    addBudgetDlg.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        });
        addBudgetDlg.show();
    }

    @Override
    public void showFullView(String url) {
        FullScreenDialogFragment fragment = new FullScreenDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("link", url);
        fragment.setArguments(bundle);
        fragment.show(getFragmentManager(), "Full Dialog");
    }

    @Override
    public void makeSelection(View view, int position) {
        if (((CheckBox) view).isChecked()) {
            selectionList.add(imagesList.get(position));
            counter++;
            UpdateCounter();

        } else {
            selectionList.remove(imagesList.get(position));
            counter--;
            UpdateCounter();
        }
        Log.e(TAG, "makeSelection: "+selectionList.size() );
    }

    @Override
    public void longClickPressed(View view) {
        onLongClick(view);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.options_menu, menu);

    }

    @Override
    public boolean onLongClick(View v) {
        isContextualModeEnabled = true;
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.contexual_menu);
        toolbar.setTitle("0 Item Selected");
        adapter.notifyDataSetChanged();
        return true;

    }


    public void UpdateCounter() {
        toolbar.setTitle(counter + " Item Selected");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            adapter.RemoveItem(selectionList);
            Log.d(TAG, "onOptionsItemSelected: del clicked");
            removeContextualActionMode();
        } else if (item.getItemId() == android.R.id.home) {
            removeContextualActionMode();
            adapter.notifyDataSetChanged();
        }else if (item.getItemId()==R.id.share){
            adapter.shareImage(selectionList);
            removeContextualActionMode();
        }

        return true;
    }

    private void removeContextualActionMode() {
        isContextualModeEnabled = false;
        toolbar.getMenu().clear();
//        toolbar.inflateMenu(R.menu.options_menu);
        counter = 0;
        selectionList.clear();
        adapter.notifyDataSetChanged();
        toolbar.setTitle("Tour Gallery");
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (isContextualModeEnabled)
                        removeContextualActionMode();
                    else
                        getFragmentManager().popBackStack();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDetach() {
//        ((NavIconShowHideListener) getActivity()).showHamburgerIcon();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        removeContextualActionMode();
        super.onDetach();
    }
}
