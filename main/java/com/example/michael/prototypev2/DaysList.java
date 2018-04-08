package com.example.michael.prototypev2;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Michael on 3/18/2018.
 */

public class DaysList extends ArrayAdapter<DaysModel> {
    private Activity context;
    private List<DaysModel> daysModelList;

    public DaysList(Activity context, List<DaysModel> daysModelList){
        super(context, R.layout.layout_days_list, daysModelList);
        this.context = context;
        this.daysModelList = daysModelList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_days_list, null, true);

        TextView textViewDay = (TextView) listViewItem.findViewById(R.id.textViewDays);


        DaysModel selectedDay = daysModelList.get(position);
        textViewDay.setText(selectedDay.getDisplayString());

        return listViewItem;

    }

}

