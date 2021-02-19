package com.sss.bitm.projectalpha.tourEvent.eventFragment;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import com.sss.bitm.projectalpha.R;
import com.sss.bitm.projectalpha.tourEvent.eventInterface.OpenGalleryFragment;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Event;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Images;
import com.sss.bitm.projectalpha.util.PullData;

import static com.sss.bitm.projectalpha.util.AppPermission.chkStoragePermission;


public class PhotoFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_ACCESS = 1;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button uploadBtn;
    private LinearLayout captureLL;
    private EditText fileNameET;
    private ImageView imageView;
    private Uri sdImageUri, camImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference imageRef;
    private StorageTask uploadTask;
    private Context context;
    private PullData pullData;
    private OpenGalleryFragment openGallery;
    private String filePath;
    private File imageFile;
    private Event event;
    private Vector<Dialog> alertDialogs = new Vector<Dialog>();     //to close unlimited number of dialogs
    String TAG = "UploadFragment";


    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        openGallery = (OpenGalleryFragment) context;
        pullData = new PullData(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("event"))
            event = (Event) getArguments().getSerializable("event");
        String eventId = event.getEventId();
        //        Log.d("PhotoFragment", "id : " + event.getEventId());
        String uid = pullData.getUser().getUserId();
        Log.d("user", "onCreateView: " + uid);
        mStorageRef = FirebaseStorage.getInstance().getReference("photos");
        imageRef = pullData.getRootRef().child(uid).child("events").child(eventId).child("images");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_capture, container, false);
        setHasOptionsMenu(true);

        uploadBtn = view.findViewById(R.id.button_upload);
        captureLL = view.findViewById(R.id.captureLL);
        fileNameET = view.findViewById(R.id.edit_text_file_name);
        imageView = view.findViewById(R.id.image_view);


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(context, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();
                }
            }
        });


        captureLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.photo_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sel_file:
                openFileChooser();
                break;
            case R.id.view_gallery:
                openGallery.onGalleryOpenListener(event);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        chkStoragePermission(((Activity) context), REQUEST_STORAGE_ACCESS);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            try {
                imageFile = createImageFile();    //initializing File

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (imageFile != null) {
                camImageUri = FileProvider.getUriForFile(context, context.getResources().getString(R.string.package_name), imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, camImageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//                camImageUri=Uri.fromFile(imageFile);
            }
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                timeStamp,      /* prefix */
                ".png",   /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        filePath = image.getAbsolutePath();
        Log.d(TAG, "createImageFile: " + filePath);
        return image;
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            sdImageUri = data.getData();
            Picasso.get().load(sdImageUri).into(imageView);
            captureLL.setVisibility(View.GONE);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            chkBitmap(filePath);
        }
    }

    private void uploadFile() {
        Uri uploadUri = null;
        if (camImageUri != null) {
            uploadUri = camImageUri;
//            saveRotatedImg(rotatedBmp);
        } else if (sdImageUri != null)
            uploadUri = sdImageUri;

        if (uploadUri == null) {
            Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show();
        } else if (fileNameET.getText().toString().trim().length() == 0) {
//            Toast.makeText(context, "Please insert image name", Toast.LENGTH_SHORT).show();
            fileNameET.setError("insert name");
        } else {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + ".jpg");
            uploadTask = fileReference.putFile(uploadUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String uploadId = imageRef.push().getKey();
                                    Toast.makeText(context, "Upload successful", Toast.LENGTH_LONG).show();
                                    Images images = new Images(uploadId, fileNameET.getText().toString().trim(),
                                            uri.toString());

                                    imageRef.child(uploadId).setValue(images);
                                    Log.d("key ", "onSuccess: " + uploadId);
                                    clearView();
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            showProgressDialog(progress);
                        }
                    });
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        Log.d("height and width", "setPic: " + targetH + " " + targetW);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
//        chkBitmap(filePath, bitmap);

    }

    public void chkBitmap(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        Glide
                .with(context)
                .asBitmap()
                .load(rotatedBitmap)
                .into(imageView);

        captureLL.setVisibility(View.GONE);
        saveRotatedImg(rotatedBitmap);
    }

    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void saveRotatedImg(Bitmap bmp) {
        OutputStream os = null;
        try {
            os = getContext().getContentResolver().openOutputStream(camImageUri);
            bmp.compress(Bitmap.CompressFormat.JPEG, 95, os);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearView() {
        fileNameET.getText().clear();
        imageView.setImageResource(0);
        camImageUri = null;
        sdImageUri = null;
        captureLL.setVisibility(View.VISIBLE);
        for (Dialog dialog : alertDialogs)
            if (dialog.isShowing())
                dialog.dismiss();
    }


    private void showProgressDialog(double progress) {
        Dialog alertDialog = new Dialog(context);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.progress_dialog);
        alertDialog.setCancelable(true);
        final ProgressBar progressBar = alertDialog.findViewById(R.id.circular_progressBar);
        final TextView progressUpdate = alertDialog.findViewById(R.id.progressTV);
        progressBar.setProgress((int) progress);
        progressUpdate.setText(progressBar.getProgress() + "%");
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        alertDialogs.add(alertDialog);
    }


//    private Boolean chkStoragePermission() {
//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
//                    , REQUEST_STORAGE_ACCESS);
//            return false;
//        }
//        return true;
//    }


}
