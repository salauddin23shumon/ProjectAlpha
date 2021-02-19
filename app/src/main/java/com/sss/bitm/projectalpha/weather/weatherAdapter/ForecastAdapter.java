package com.sss.bitm.projectalpha.weather.weatherAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.sss.bitm.projectalpha.R;
import com.sss.bitm.projectalpha.weather.weatherDataModel.ForecastWeatherResponse;


public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ItemViewHolder> {

    private List<ForecastWeatherResponse.ListData> mForecastList;
    private Context contexts;
    private String units;

    public ForecastAdapter(List<ForecastWeatherResponse.ListData> forecastlist, Context context, String units) {
        this.mForecastList = forecastlist;

        contexts = context;
        this.units= units;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, final int position) {
        final ForecastWeatherResponse.ListData model = mForecastList.get(position);
        holder.bind(model,contexts);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.forcast_row_item, viewGroup, false);
        return new ItemViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mForecastList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        public TextView castDay,castStatus,castTemp;
        ImageView tempImage;
        private Context contexts;



        public ItemViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            castDay = (TextView) itemView.findViewById(R.id.cast_toDay);
            castStatus = (TextView) itemView.findViewById(R.id.cast_Status);
            castTemp = (TextView) itemView.findViewById(R.id.max_temp);
            tempImage = itemView.findViewById(R.id.adapterTempImage);
        }

        public void bind(ForecastWeatherResponse.ListData listData,Context context) {
            castDay.setText(listData.getDtTxt());
            castStatus.setText(listData.getWeather().get(0).getMain());
            castTemp.setText(Double.toString(listData.getMain().getTempMax()));

            if (units.equals("metric")) {
                tempImage.setImageResource(R.drawable.ic_celsd);
            } else {
                tempImage.setImageResource(R.drawable.ic_fahsd);
            }

            contexts =context;
        }
    }

    public void updateList(List<ForecastWeatherResponse.ListData> mForecastList){
        this.mForecastList=mForecastList;
    }

}
