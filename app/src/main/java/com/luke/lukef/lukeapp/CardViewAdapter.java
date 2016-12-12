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


public class CardViewAdapter extends RecyclerView.Adapter<CardViewAdapter.MyViewHolder> {

    private List<Submission> submissionList;
    private Activity activity;
    private List<Bitmap> mapsBitmaps;
    private List<Bitmap> picsBitmaps;
    LukeNetUtils lukeNetUtils;
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
                holder.rightImg.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.no_img));
            }
        } else {
            holder.rightImg.setImageBitmap(picsBitmaps.get(position));
        }
        if (mapsBitmaps == null || mapsBitmaps.size() == 0) {
            LoadMapTask loadMapTask = new LoadMapTask(holder, submission.getLocation(), this.activity);
            loadMapTask.execute();
        } else {
            holder.leftImg.setImageBitmap(mapsBitmaps.get(position));
        }


    }

    /**
     * Called when a view created by this adapter has been recycled.
     * <p>
     * <p>A view is recycled when a {@link } decides that it no longer
     * needs to be attached to its parent {@link RecyclerView}. This can be because it has
     * fallen out of visibility or a set of cached views represented by views still
     * attached to the parent RecyclerView. If an item view has large or expensive data
     * bound to it such as large bitmaps, this may be a good place to release those
     * resources.</p>
     * <p>
     * RecyclerView calls this method right before clearing ViewHolder's internal data and
     * sending it to RecycledViewPool. This way, if ViewHolder was holding valid information
     * before being recycled, you can call {@link #()} to get
     * its adapter position.
     *
     * @param holder The ViewHolder for the view being recycled
     */

    @Override
    public void onViewRecycled(MyViewHolder holder) {
        holder.progressBarMap.setVisibility(View.VISIBLE);
        holder.progressBarPicture.setVisibility(View.VISIBLE);
        holder.leftImg.setImageBitmap(null);
        holder.leftImg.setVisibility(View.INVISIBLE);
        holder.rightImg.setImageBitmap(null);
        holder.rightImg.setVisibility(View.INVISIBLE);
        super.onViewRecycled(holder);
    }

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
        int i = 0;
        return submissionList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mDate;
        TextView mTime;
        TextView content;
        ImageView leftImg;
        ImageView rightImg;
        ProgressBar progressBarPicture;
        ProgressBar progressBarMap;

        public MyViewHolder(View itemView) {
            super(itemView);
            mDate = (TextView) itemView.findViewById(R.id.postDate);
            mTime = (TextView) itemView.findViewById(R.id.postTime);
            content = (TextView) itemView.findViewById(R.id.postContent);
            leftImg = (ImageView) itemView.findViewById(R.id.picture);
            rightImg = (ImageView) itemView.findViewById(R.id.map);
            progressBarPicture = (ProgressBar) itemView.findViewById(R.id.card_progress_picutre);
            progressBarMap = (ProgressBar) itemView.findViewById(R.id.card_progress_map);
        }
    }

    private class LoadImageTask extends AsyncTask<Void, Void, Void> {

        private MyViewHolder holder;
        private String url;
        private Activity activity;
        private Bitmap bitmap = null;

        public LoadImageTask(MyViewHolder holder, String url, Activity activity) {
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
                } catch (ExecutionException e) {
                    Log.e(TAG, "doInBackground: ", e);
                } catch (InterruptedException e) {
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
                        LoadImageTask.this.holder.rightImg.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.no_img, null));
                    } else {
                        LoadImageTask.this.holder.rightImg.setImageBitmap(LoadImageTask.this.bitmap);
                    }
                    LoadImageTask.this.holder.progressBarPicture.setVisibility(View.GONE);
                    LoadImageTask.this.holder.rightImg.setVisibility(View.VISIBLE);
                }
            });
            super.onPostExecute(aVoid);
        }
    }

    private class LoadMapTask extends AsyncTask<Void, Void, Void> {

        private MyViewHolder holder;
        private Location location;
        private Activity activity;
        private Bitmap bitmap = null;

        public LoadMapTask(MyViewHolder holder, Location location, Activity activity) {
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
                } catch (ExecutionException e) {
                    Log.e(TAG, "doInBackground: ", e);
                } catch (InterruptedException e) {
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
                        LoadMapTask.this.holder.leftImg.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.no_img, null));
                    } else {
                        LoadMapTask.this.holder.leftImg.setImageBitmap(LoadMapTask.this.bitmap);
                    }
                    LoadMapTask.this.holder.progressBarMap.setVisibility(View.GONE);
                    LoadMapTask.this.holder.leftImg.setVisibility(View.VISIBLE);
                }
            });
            super.onPostExecute(aVoid);
        }
    }

}