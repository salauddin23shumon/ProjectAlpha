package com.sss.bitm.projectalpha.nearByPlace;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import com.sss.bitm.projectalpha.R;
import com.sss.bitm.projectalpha.nearByPlace.nearByPojo.Result;


public class NearByPlaceAdapter extends RecyclerView.Adapter<NearByPlaceAdapter.PlaceViewHolder> {

    private Context context;
    private List<Result> placeList;
    private PlaceAdapterClickListener clickedListener;

    public static final String TAG ="NearByPlaceAdapter";

    public NearByPlaceAdapter(Context context, List<Result> placeList) {
        this.context = context;
        this.placeList = placeList;
        clickedListener = (PlaceAdapterClickListener) context;
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view =  LayoutInflater.from(context).inflate(R.layout.nearby_row_item, parent,false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {

        Result place = placeList.get(position);
        holder.placeNameTV.setText(place.getName());
        holder.placeLocationTv.setText(place.getVicinity());

        if (place.getRating()!=null){
            holder.ratingBar.setRating(place.getRating().floatValue());
        }else {
            Log.d(TAG, "onBindViewHolder: Rating Not Found");
        }
    }


    @Override
    public int getItemCount() {
        return placeList.size();
    }


    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView placeNameTV, placeLocationTv;
        RatingBar ratingBar;

        public PlaceViewHolder(View itemView) {
            super(itemView);

            placeNameTV = itemView.findViewById(R.id.placeNameTV);
            //placeTypeTV = itemView.findViewById(R.id.placeTypesTV);
            placeLocationTv = itemView.findViewById(R.id.placeLocationTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickedListener.onPlaceClicked(placeList.get(getAdapterPosition()));
                }
            });

        }
    }

    public void updatePlaceList(List<Result> placeList){
        this.placeList=placeList;
        notifyDataSetChanged();
        Log.d(TAG, "updatePlaceList: "+placeList.size());
    }


    public interface PlaceAdapterClickListener{
        void onPlaceClicked(Result nearbyPlacesData);
    }
}
