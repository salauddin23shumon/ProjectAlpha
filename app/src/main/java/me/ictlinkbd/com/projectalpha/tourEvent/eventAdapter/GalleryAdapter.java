package me.ictlinkbd.com.projectalpha.tourEvent.eventAdapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ictlinkbd.com.projectalpha.R;
import me.ictlinkbd.com.projectalpha.tourEvent.eventDataModel.Images;

import static me.ictlinkbd.com.projectalpha.tourEvent.eventFragment.GalleryFragment.isContextualModeEnabled;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ImageViewHolder> {

    private List<Images> imageList;
    private Context context;
    private GalleryAdapterItemListener galleryAdapterItemListener;
    private File imgFilePath;
    private ImageLoader imageLoader;

    private String TAG = "GalleryAdapter ";
    private Map<String, Uri> imgMap = new HashMap<>();


    public GalleryAdapter(List<Images> imageList, Context context, Fragment fragment) {
        this.imageList = imageList;
        this.context = context;
        galleryAdapterItemListener = (GalleryAdapterItemListener) fragment;
        imgFilePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading0)
                .showImageForEmptyUri(R.drawable.loading0)
                .showImageOnFail(R.drawable.loading0)
                .delayBeforeLoading(0)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
//                .cacheOnDisk(true)        // don't use this ...eats memory
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();


        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(3)
                .threadPriority(Thread.NORM_PRIORITY - 1)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .imageDecoder(new BaseImageDecoder(false))
                .defaultDisplayImageOptions(options)
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);

    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_gallery_row, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {
        final Images image = imageList.get(position);
        holder.nameTV.setText(image.getImageName());

        if (!isContextualModeEnabled) {
            holder.checkBox.setVisibility(View.GONE);
            holder.textLL.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.textLL.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);
        }

        File dir = new File(imgFilePath + "/thumb");
        if (!dir.exists()) {
            dir.mkdir();
        }
        final File thumbImageFile = new File(dir, "/" + image.getImageId() + ".jpg");
        if (!thumbImageFile.exists()) {

            imageLoader.displayImage(image.getImageUrl(), holder.imageView, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    holder.loading.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    holder.loading.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.loading.setVisibility(View.GONE);
                    try {
                        FileOutputStream outputStreamthumb = new FileOutputStream(thumbImageFile);
                        loadedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStreamthumb);
//                        Toast.makeText(context, image.getImageId()+" thumb saved", Toast.LENGTH_SHORT).show();
                        Log.d("file", ": " + image.getImageId() + " saved");
                        outputStreamthumb.flush();
                        outputStreamthumb.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    holder.loading.setVisibility(View.GONE);
                }
            });
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFilePath + "/thumb/" + image.getImageId() + ".jpg");
            holder.loading.setVisibility(View.GONE);
            holder.imageView.setImageBitmap(bitmap);
        }


        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryAdapterItemListener.showFullView(imgFilePath + "/thumb/" + image.getImageId() + ".jpg");
            }
        });

        holder.menuTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.single_row_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {
                            case R.id.edit_menu_item:
                                galleryAdapterItemListener.onItemEdit(image);
                                break;
                            case R.id.delete_menu_item:
                                imgMap.put(image.getImageId(), Uri.parse(imgFilePath + "/thumb/" + image.getImageId() + ".jpg"));
                                galleryAdapterItemListener.onItemDelete(imgMap);
                                break;
                        }
                        return false;
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d("adapter", "getItemCount: " + imageList.size());
        return imageList.size();
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView nameTV, menuTV;
        private ProgressBar loading;
        private CheckBox checkBox;
        private LinearLayout textLL;
        private View view;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.galleryIV);
            nameTV = itemView.findViewById(R.id.img_nameTV);
            menuTV = itemView.findViewById(R.id.gallery_menu_dot);
            loading = itemView.findViewById(R.id.img_loading_progress);
            checkBox = itemView.findViewById(R.id.checkBox);
            textLL = itemView.findViewById(R.id.textLL);


            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    galleryAdapterItemListener.longClickPressed(v);
                    return false;
                }
            });

            view = itemView;
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    galleryAdapterItemListener.longClickPressed(v);
                    return true;
                }
            });
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    galleryAdapterItemListener.makeSelection(v, getAdapterPosition());
                }
            });
        }
    }

    public void updateList(List<Images> images) {
        imageList = images;
        notifyDataSetChanged();
    }


    public void clearMap() {
        imgMap.clear();
    }

    public void RemoveItem(List<Images> selectionList) {
        if (selectionList.size() > 0) {
            for (Images img : selectionList) {
                imgMap.put(img.getImageId(), Uri.parse(imgFilePath + "/thumb/" + img.getImageId() + ".jpg"));
            }
            galleryAdapterItemListener.onItemDelete(imgMap);
        } else {
            Toast.makeText(context, "no item selected", Toast.LENGTH_SHORT).show();
        }
    }

    public ItemTouchHelper.SimpleCallback item = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            imageList.remove(viewHolder.getAdapterPosition());
            notifyDataSetChanged();
        }
    };


    public void shareImage(List<Images> selectionList) {
        if (selectionList.size() > 0) {
            ArrayList<Uri> files = new ArrayList<>();
            Log.d(TAG, "shareImage: " + selectionList.size());
            for (Images img : selectionList) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFilePath + "/thumb/" + img.getImageId() + ".jpg");
                Log.e(TAG, "shareImage: " + bitmap);
                files.add(getImageUri(bitmap));
            }
            shareContent(files);
            Log.d("size of file", "shareImage: " + files.size());
        } else {
            Toast.makeText(context, "no item selected", Toast.LENGTH_SHORT).show();
        }

    }


    public Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }


    private void shareContent(ArrayList<Uri> shareFiles) {
        try {
            final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareFiles);
            intent.setType("image/png");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public interface GalleryAdapterItemListener {

        void onItemDelete(Map<String, Uri> map);

        void onItemEdit(Images image);

        void showFullView(String url);

        void makeSelection(View view, int position);

        void longClickPressed(View view);
    }
}
