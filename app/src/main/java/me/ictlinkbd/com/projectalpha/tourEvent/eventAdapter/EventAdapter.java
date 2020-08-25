package me.ictlinkbd.com.projectalpha.tourEvent.eventAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import me.ictlinkbd.com.projectalpha.R;
import me.ictlinkbd.com.projectalpha.tourEvent.eventDataModel.Event;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {


    private Context context;
    private List<Event> eventList;
    private EventListDetailsListener mEventListDetailsListener;
    private EventListActionListener eventListActionListener;

    public EventAdapter(Context context, List<Event> eventList, Fragment fragment) {
        this.context = context;
        this.eventList = eventList;

        mEventListDetailsListener = (EventListDetailsListener) context;
        // for activity to fragment
        eventListActionListener = (EventListActionListener) fragment;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {

        View singleRow = LayoutInflater.from(context)
                .inflate(R.layout.eventlist_single_row, parent, false);

        return new EventViewHolder(singleRow);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, final int position) {

        final Event event = eventList.get(position);
        final String event_id = event.getEventId();
        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        long daysLeft = 0;
        Date presentDay = new Date();
        String eventDay = event.getDepartureDate();
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd-MM-yyyy");

        try {
            Date departure = dayFormat.parse(eventDay);
            long dayDifferent = departure.getTime() - presentDay.getTime();
            daysLeft = TimeUnit.DAYS.convert(dayDifferent, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.name.setText(eventList.get(position).getName());
        holder.createdDay.setText("Created on: " + today);
        holder.startDate.setText("Start on: " + event.getDepartureDate());
        if (daysLeft<0)
            holder.remainingDay.setText(Math.abs(daysLeft)+ " days gone");
        else
            holder.remainingDay.setText(daysLeft + " days left");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(context, "" + event.getName(), Toast.LENGTH_SHORT).show();

                mEventListDetailsListener.onEventClicked(event);
            }
        });

        holder.menu.setOnClickListener(new View.OnClickListener() {
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
                                eventListActionListener.onEventEdit(event);
                                break;
                            case R.id.delete_menu_item:
                                eventListActionListener.onEventDelete(event_id);
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
        return eventList.size();
    }


    class EventViewHolder extends RecyclerView.ViewHolder {

        TextView name, startDate, remainingDay, createdDay, menu;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.ui_event_name);
            startDate = itemView.findViewById(R.id.ui_event_start);
            remainingDay = itemView.findViewById(R.id.ui_event_remain);
            createdDay = itemView.findViewById(R.id.ui_event_create);
            menu = itemView.findViewById(R.id.ui_event_menu_options);
        }
    }

    public void updateList(List<Event> events) {
        eventList = events;
        notifyDataSetChanged();
    }

    public interface EventListDetailsListener {
        void onEventClicked(Event eventObject);
    }

    public interface EventListActionListener {
        void onEventEdit(Event event);
        void onEventDelete(String eventID);
    }
}
