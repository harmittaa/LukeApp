package com.luke.lukef.lukeapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luke.lukef.lukeapp.model.Submission;
import com.luke.lukef.lukeapp.model.SubmissionFromServer;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Bang Nguyen on 11/18/2016.
 */

public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.MyViewHolder> {

    private List<SubmissionFromServer> submissionList;
    private Activity activity;
    private List<Bitmap> mapsBitmaps;
    private List<Bitmap> picsBitmaps;
    LukeNetUtils lukeNetUtils;

    public CardViewAdapter(List<SubmissionFromServer> submissionList, Activity activity) {
        this.activity = activity;
        this.submissionList = submissionList;
        lukeNetUtils = new LukeNetUtils(activity);
        mapsBitmaps = new ArrayList<>();
        picsBitmaps = new ArrayList<>();
        getAllMapThumbs();
        getAllPicsThumbs();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_template, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final SubmissionFromServer submission = submissionList.get(position);
        holder.mDate.setText(submission.getDate());
        holder.content.setText(submission.getDescription());
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (picsBitmaps == null) {
                        holder.rightImg.setImageBitmap(lukeNetUtils.getBitmapFromURL(submission.getImageUrl()));
                    } else {
                        holder.rightImg.setImageBitmap(picsBitmaps.get(position));
                    }
                    if(mapsBitmaps == null) {
                        holder.leftImg.setImageBitmap(lukeNetUtils.getMapThumbnail(submission.getLocation(), 400, 400));
                    } else {
                        holder.leftImg.setImageBitmap(mapsBitmaps.get(position));
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
//        holder.mDate.setText((CharSequence) submission.getDate());
//        holder.content.setText(submission.getContent());
//        holder.leftImg.setImageBitmap(submission.getImage());
        //  I dont know how to get the mapView to here
//        holder.rightImg.setImageBitmap(submission.getLocation());


    }

    private void getAllMapThumbs(){
        for(SubmissionFromServer s : submissionList){
            try {
                mapsBitmaps.add(lukeNetUtils.getMapThumbnail(s.getLocation(), 400, 400));
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void getAllPicsThumbs(){
        for(SubmissionFromServer s : submissionList){
            try {
                picsBitmaps.add(lukeNetUtils.getBitmapFromURL(s.getImageUrl()));
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public int getItemCount() {
        int i = 0;
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