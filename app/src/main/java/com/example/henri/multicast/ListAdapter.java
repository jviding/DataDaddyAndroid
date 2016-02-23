package com.example.henri.multicast;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Jasu on 20.2.2016.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>{
    private final ArrayList<String> mDataset;
    private final ArrayList<String> selected;

    public ListAdapter(ArrayList<String> myDataset, ArrayList<String> anotherDataset) {
        mDataset = myDataset;
        if (anotherDataset == null) {
            selected = new ArrayList<String>();
        } else {
            selected = anotherDataset; // user's highlighted files
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset.get(position));
        if (selected.contains(mDataset.get(position))) {
            holder.mTextView.setBackgroundColor(Color.GRAY);
            holder.mTextView.setActivated(true);
        } else {
            holder.mTextView.setActivated(false);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ViewHolder(TextView v) {
            super(v);
            mTextView = v;
        }
    }

    private String getFileNameFromPath(String path) {
        if (path.equals(File.separator)) {
            return "";
        }else if (path.length() > 1 && (path.charAt(path.length()-1)+"").equals(File.separator)) {
            path = path.substring(0, path.length()-1);
        }
        for (int i=path.length()-1; i>=0; i--) {
            if ((path.charAt(i)+"").equals(File.separator)) {
                path = path.substring(i+1);
            }
        }
        return path;
    }

}
