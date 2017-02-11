/*
        BalticApp, for studying and tracking the condition of the Baltic sea
        and Gulf of Finland throug user submissions.
        Copyright (C) 2016  Daniel Zakharin, LuKe

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/> or
        the beginning of MainActivity.java file.

*/

package com.luke.lukef.lukeapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.luke.lukef.lukeapp.model.Submission;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;
import com.luke.lukef.lukeapp.tools.LukeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Custom adapter to be used in user profile fragment. Works with Submission objects
 */
public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.MyViewHolder> {

    private List<Submission> submissionList;
    private Activity activity;
    private List<Bitmap> mapsBitmaps;
    private List<Bitmap> picsBitmaps;
    private LukeNetUtils lukeNetUtils;
    private static final String TAG = "CardViewAdapter";

    public CardViewAdapter(List<Submission> submissionList, Activity activity) {
        this.activity = activity;
        this.submissionList = submissionList;
        lukeNetUtils = new LukeNetUtils(activity);
        mapsBitmaps = new ArrayList<>();
        picsBitmaps = new ArrayList<>();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Submission submission = submissionList.get(position);
        holder.content.setText(submission.getDescription());
        setupDateTime(LukeUtils.parseDateFromString(submission.getDate()), holder.mDate, holder.mTime);



        if (picsBitmaps == null || picsBitmaps.size() == 0) {
            if (!TextUtils.isEmpty(submission.getImageUrl())) {
                LoadImageTask loadImageTask = new LoadImageTask(holder, submission.getImageUrl(), this.activity);
                loadImageTask.execute();
            } else {
                holder.mapImage.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.no_img));
            }

        } else {
            holder.mapImage.setImageBitmap(picsBitmaps.get(position));
        }
        if (mapsBitmaps == null || mapsBitmaps.size() == 0) {
            LoadMapTask loadMapTask = new LoadMapTask(holder, submission.getLocation(), this.activity);
            loadMapTask.execute();
        } else {
            holder.submissionImage.setImageBitmap(mapsBitmaps.get(position));
        }

    }

    @Override
    public void onViewRecycled(MyViewHolder holder) {
        holder.progressBarMap.setVisibility(View.VISIBLE);
        holder.progressBarPicture.setVisibility(View.VISIBLE);
        holder.submissionImage.setImageBitmap(null);
        holder.submissionImage.setVisibility(View.INVISIBLE);
        holder.mapImage.setImageBitmap(null);
        holder.mapImage.setVisibility(View.INVISIBLE);
        super.onViewRecycled(holder);
    }

    /**
     * Splits a date String into a date and a time and displays them in seperate TextViews
     * @param fullDate The full date as a String, containing both a date and a time
     * @param left TextView on the left side of the card
     * @param right TextView on the right side of the card
     */
    private void setupDateTime(final String fullDate, final TextView left, final TextView right) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] splited = fullDate.split("\\s+");
                    left.setText(splited[0]);
                    right.setText(splited[1]);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "setupDateTime: ", e);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return submissionList.size();
    }

    /**
     * Inner class representing an individual card
     */
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mDate;
        TextView mTime;
        TextView content;
        ImageView submissionImage;
        ImageView mapImage;
        ProgressBar progressBarPicture;
        ProgressBar progressBarMap;

        MyViewHolder(View itemView) {
            super(itemView);
            mDate = (TextView) itemView.findViewById(R.id.postDate);
            mTime = (TextView) itemView.findViewById(R.id.postTime);
            content = (TextView) itemView.findViewById(R.id.postContent);
            submissionImage = (ImageView) itemView.findViewById(R.id.picture);
            mapImage = (ImageView) itemView.findViewById(R.id.map);
            progressBarPicture = (ProgressBar) itemView.findViewById(R.id.card_progress_picutre);
            progressBarMap = (ProgressBar) itemView.findViewById(R.id.card_progress_map);
        }
    }

    /**
     * Asynctask for loading an image into a card. Displays images when they are ready, even with slow
     * internet speed.
     */
    private class LoadImageTask extends AsyncTask<Void, Void, Void> {

        private MyViewHolder holder;
        private String url;
        private Activity activity;
        private Bitmap bitmap = null;

        LoadImageTask(MyViewHolder holder, String url, Activity activity) {
            this.activity = activity;
            this.url = url;
            this.holder = holder;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!TextUtils.isEmpty(this.url) && !this.url.equals("null")) {
                LukeNetUtils lukeNetUtils = new LukeNetUtils(this.activity);
                try {
                    this.bitmap = lukeNetUtils.getBitmapFromURL(this.url);
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "doInBackground: ", e);
                }
            } else {

                this.bitmap = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (LoadImageTask.this.bitmap == null) {
                        LoadImageTask.this.holder.mapImage.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.no_img, null));
                    } else {
                        LoadImageTask.this.holder.mapImage.setImageBitmap(LoadImageTask.this.bitmap);
                    }
                    LoadImageTask.this.holder.progressBarPicture.setVisibility(View.GONE);
                    LoadImageTask.this.holder.mapImage.setVisibility(View.VISIBLE);
                }
            });
            super.onPostExecute(aVoid);
        }
    }

    /**
     * Asynctask for loading a map image into a card. Displays maps when they are ready, even with slow
     * internet speed.
     */
    private class LoadMapTask extends AsyncTask<Void, Void, Void> {

        private MyViewHolder holder;
        private Location location;
        private Activity activity;
        private Bitmap bitmap = null;

        LoadMapTask(MyViewHolder holder, Location location, Activity activity) {
            this.activity = activity;
            this.location = location;
            this.holder = holder;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (this.location != null) {
                LukeNetUtils lukeNetUtils = new LukeNetUtils(this.activity);
                try {
                    this.bitmap = lukeNetUtils.getMapThumbnail(this.location, 250, 250);
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "doInBackground: ", e);
                }
            } else {
                this.bitmap = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (LoadMapTask.this.bitmap == null) {
                        LoadMapTask.this.holder.submissionImage.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.no_img, null));
                    } else {
                        LoadMapTask.this.holder.submissionImage.setImageBitmap(LoadMapTask.this.bitmap);
                    }
                    LoadMapTask.this.holder.progressBarMap.setVisibility(View.GONE);
                    LoadMapTask.this.holder.submissionImage.setVisibility(View.VISIBLE);
                }
            });
            super.onPostExecute(aVoid);
        }
    }

}