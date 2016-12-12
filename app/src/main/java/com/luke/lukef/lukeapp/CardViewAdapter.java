package com.luke.lukef.lukeapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luke.lukef.lukeapp.model.SubmissionFromServer;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;
import com.luke.lukef.lukeapp.tools.LukeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.MyViewHolder> {

    private List<SubmissionFromServer> submissionList;
    private Activity activity;
    private List<Bitmap> mapsBitmaps;
    private List<Bitmap> picsBitmaps;
    LukeNetUtils lukeNetUtils;
    private static final String TAG = "CardViewAdapter";

    public CardViewAdapter(List<SubmissionFromServer> submissionList, Activity activity) {
        this.activity = activity;
        this.submissionList = submissionList;
        lukeNetUtils = new LukeNetUtils(activity);
        mapsBitmaps = new ArrayList<>();
        picsBitmaps = new ArrayList<>();
        /*getAllMapThumbs();
        getAllPicsThumbs();*/
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final SubmissionFromServer submission = submissionList.get(position);
        holder.content.setText(submission.getDescription());
        setupDateTime(LukeUtils.parseDateFromString(submission.getDate()), holder.mDate, holder.mTime);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (picsBitmaps == null || picsBitmaps.size() == 0) {
                        if (!TextUtils.isEmpty(submission.getImageUrl())) {
                            holder.rightImg.setImageBitmap(lukeNetUtils.getBitmapFromURL(submission.getImageUrl()));
                        } else {
                            holder.rightImg.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.no_img));
                        }
                    } else {
                        holder.rightImg.setImageBitmap(picsBitmaps.get(position));
                    }
                    if (mapsBitmaps == null || mapsBitmaps.size() == 0) {
                        holder.leftImg.setImageBitmap(lukeNetUtils.getMapThumbnail(submission.getLocation(), 400, 400));
                    } else {
                        holder.leftImg.setImageBitmap(mapsBitmaps.get(position));
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "run: ERROR ", e );
                }

            }
        });
//        holder.mDate.setText((CharSequence) submission.getDate());
//        holder.content.setText(submission.getContent());
//        holder.leftImg.setImageBitmap(submission.getImage());
        //  I dont know how to get the mapView to here
//        holder.rightImg.setImageBitmap(submission.getLocation());


    }

    private void setupDateTime(String fullDate, TextView left, TextView right) {
        try {
            String[] splited = fullDate.split("\\s+");
            left.setText(splited[0]);
            right.setText(splited[1]);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "setupDateTime: ", e);
        }
    }

    private void getAllMapThumbs() {
        for (SubmissionFromServer s : submissionList) {
            try {
                mapsBitmaps.add(lukeNetUtils.getMapThumbnail(s.getLocation(), 400, 400));
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "getAllMapThumbs: ERROR ", e );
            }
        }
    }

    /**
     * Fetches images for the cards, if no image URL is present, uses default
     */
    private void getAllPicsThumbs() {
        for (SubmissionFromServer s : submissionList) {
            try {
                String url = s.getImageUrl();
                if (!TextUtils.isEmpty(s.getImageUrl()) && !url.equals("null")) {
                    picsBitmaps.add(lukeNetUtils.getBitmapFromURL(s.getImageUrl()));
                } else {
                    Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), R.drawable.no_img);
                    picsBitmaps.add(bm);
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "getAllPicsThumbs: ERROR ", e );
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
        TextView mTime;
        TextView content;
        ImageView leftImg;
        ImageView rightImg;

        public MyViewHolder(View itemView) {
            super(itemView);
            mDate = (TextView) itemView.findViewById(R.id.postDate);
            mTime = (TextView) itemView.findViewById(R.id.postTime);
            content = (TextView) itemView.findViewById(R.id.postContent);
            leftImg = (ImageView) itemView.findViewById(R.id.picture);
            rightImg = (ImageView) itemView.findViewById(R.id.map);
        }
    }

}