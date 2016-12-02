package com.luke.lukef.lukeapp;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luke.lukef.lukeapp.model.Submission;

import java.util.List;

/**
 * Created by Bang Nguyen on 11/18/2016.
 */

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.MyViewHolder> {

    private List<Submission> submissionList;

    public CardViewAdapter(List<Submission> submissionList) {
        this.submissionList = submissionList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_template, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Submission submission = submissionList.get(position);
        //Dummy info for testing
        holder.mDate.setText("" + DateFormat.format("dd/MM/yyyy", System.currentTimeMillis()));
        holder.content.setText(R.string.text_test);

//        holder.mDate.setText((CharSequence) submission.getDate());
//        holder.content.setText(submission.getContent());
//        holder.leftImg.setImageBitmap(submission.getImage());
        //  I dont know how to get the mapView to here
//        holder.rightImg.setImageBitmap(submission.getLocation());


    }

    @Override
    public int getItemCount() {
        return submissionList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mDate;
        TextView content;
        ImageView leftImg;
        ImageView rightImg;

        public MyViewHolder(View itemView) {
            super(itemView);
            mDate = (TextView) itemView.findViewById(R.id.postDate);
            content = (TextView) itemView.findViewById(R.id.postContent);
            leftImg = (ImageView) itemView.findViewById(R.id.picture);
            rightImg = (ImageView) itemView.findViewById(R.id.map);
        }
    }

}