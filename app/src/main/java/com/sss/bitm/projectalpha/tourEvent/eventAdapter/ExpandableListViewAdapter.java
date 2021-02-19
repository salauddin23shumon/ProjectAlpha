package com.sss.bitm.projectalpha.tourEvent.eventAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sss.bitm.projectalpha.R;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<String> listHeaders;
    private HashMap<String, List<String>> listAllChild;

    public ExpandableListViewAdapter(Context context, Fragment fragment) {
        this.context = context;
        createExpandableList();
    }

    @Override
    public int getGroupCount() {
        return listHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listAllChild.get(listHeaders.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listHeaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listAllChild.get(listHeaders.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.expandable_group_layout,parent,false);
        TextView listHeaderTV = convertView.findViewById(R.id.group_header_tv);
        listHeaderTV.setText(listHeaders.get(groupPosition));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.expandable_child_layout,parent,false);
        TextView listChildTV = convertView.findViewById(R.id.single_list_item_tv);
        listChildTV.setText(listAllChild.get(listHeaders.get(groupPosition)).get(childPosition));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    private void createExpandableList() {
        listHeaders = new ArrayList<>();
        listAllChild = new HashMap<>();

        listHeaders.add("Expenditure");
        listHeaders.add("Moments");
        listHeaders.add("More on Event");

        ArrayList<String> expenditure = new ArrayList<>();
        expenditure.add("Add New Expense");
        expenditure.add("View All Expense");
        expenditure.add("Add More Budget");

        ArrayList<String> moments = new ArrayList<>();
        moments.add("Take a Photo");
        moments.add("View Event Gallery");

        ArrayList<String> moreOnEvent = new ArrayList<>();
        moreOnEvent.add("Edit Event");
        moreOnEvent.add("Delete Event");

        listAllChild.put(listHeaders.get(0),expenditure);
        listAllChild.put(listHeaders.get(1),moments);
        listAllChild.put(listHeaders.get(2),moreOnEvent);
    }
}
