package com.luke.lukef.lukeapp.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Rank;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.model.UserFromServer;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class LeaderboardFragment extends Fragment implements View.OnClickListener {
    private View fragmentView;
    private ListView leaderboardListView;
    private ImageButton backButton;
    LukeNetUtils lukeNetUtils;
    final private String TAG = "LeaderBoarDFragment";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.fragmentView = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        this.backButton = (ImageButton) fragmentView.findViewById(R.id.button_back);
        this.backButton.setOnClickListener(this);
        this.leaderboardListView = (ListView) fragmentView.findViewById(R.id.leaderboardListView);
        lukeNetUtils = new LukeNetUtils(getMainActivity());
        setupListView();
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_back:
                getMainActivity().onBackPressed();
                break;
        }
    }

    private void setupListView() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<UserFromServer> userFromServersAll = lukeNetUtils.getAllUsers();
                    userFromServersAll = sortOutNoScoreUsers(userFromServersAll);
                    //sort by whose score is bigger
                    Collections.sort(userFromServersAll, new Comparator<UserFromServer>() {
                        @Override
                        public int compare(UserFromServer o1, UserFromServer o2) {
                            return Integer.valueOf(o2.getScore()).compareTo(o1.getScore());
                        }
                    });
                    final UserListViewAdapter userListViewAdapter = new UserListViewAdapter(getMainActivity(), R.layout.leaderboard_list_item, userFromServersAll);
                    getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            leaderboardListView.setAdapter(userListViewAdapter);
                        }
                    });
                } catch (ExecutionException e) {
                    Log.e("", "run: ", e);
                } catch (InterruptedException e) {
                    Log.e("", "run: ", e);
                }
            }
        });
        t.start();
        leaderboardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    UserFromServer userFromServer = (UserFromServer) parent.getItemAtPosition(position);
                    Bundle b = new Bundle();
                    b.putString("userId",userFromServer.getId());
                    getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_PROFILE,b);
                } catch (ClassCastException e) {
                    Log.e(TAG, "onItemClick: ", e);
                }

            }
        });
    }

    private ArrayList<UserFromServer> sortOutNoScoreUsers(ArrayList<UserFromServer> allUsers){
        ArrayList<UserFromServer> tempList = new ArrayList<>();
        for (UserFromServer u : allUsers){
            if(u.getId().equals(SessionSingleton.getInstance().getUserId())){
                tempList.add(u);
                continue;
            }
            else if(u.getScore() > 0){
                tempList.add(u);
            }
        }
        return tempList;
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private class UserListViewAdapter extends ArrayAdapter<UserFromServer> {

        LayoutInflater layoutInflater = (LayoutInflater) getMainActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        public UserListViewAdapter(Context context, int resource) {
            super(context, resource);
        }

        public UserListViewAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        public UserListViewAdapter(Context context, int resource, UserFromServer[] objects) {
            super(context, resource, objects);
        }

        public UserListViewAdapter(Context context, int resource, int textViewResourceId, UserFromServer[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public UserListViewAdapter(Context context, int resource, List<UserFromServer> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = layoutInflater.inflate(R.layout.leaderboard_list_item, parent, false);
            }
            final ImageView userImage = (ImageView) v.findViewById(R.id.leaderboard_list_item_user_image);
            TextView username = (TextView) v.findViewById(R.id.leaderboard_list_item_username);
            TextView rankTitle = (TextView) v.findViewById(R.id.leaderboard_list_item_rank);
            final ImageView rankImage = (ImageView) v.findViewById(R.id.leaderboard_list_item_rank_image);
            TextView score = (TextView) v.findViewById(R.id.leaderboard_list_item_score);
            TextView positionTextView = (TextView) v.findViewById(R.id.leaderboard_list_item_position);

            final UserFromServer userFromServer = getItem(position);

            //load user profile image
            loadImageTask loadImageTask = new loadImageTask(userImage, userFromServer.getImageUrl(), getMainActivity());
            loadImageTask.execute();

            //setup rank, title and image
            Rank r = SessionSingleton.getInstance().getRankById(userFromServer.getRankId());
            if (r != null) {
                rankTitle.setText(r.getTitle());
                loadImageTask loadImageTask2 = new loadImageTask(rankImage, r.getImageUrl(), getMainActivity());
                loadImageTask2.execute();
            } else {
                getMainActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rankImage.setImageDrawable(ContextCompat.getDrawable(getMainActivity(),R.drawable.luke_rank_default));
                    }
                });
            }

            positionTextView.setText("" + (position + 1));
            username.setText(userFromServer.getUsername());
            score.setText("Score: " + userFromServer.getScore());

            if (userFromServer.getId().equals(SessionSingleton.getInstance().getUserId())) {
                v.setBackgroundColor(ContextCompat.getColor(getMainActivity(), R.color.shamrock));
            } else {
                v.setBackgroundColor(ContextCompat.getColor(getMainActivity(), android.R.color.transparent));
            }

            return v;

        }


    }

    private class loadImageTask extends AsyncTask<Void, Void, Void> {

        private ImageView imageView;
        private String urlString;
        private Activity activity;
        private Bitmap bitmap = null;

        loadImageTask(ImageView imageView, String urlString, Activity activity) {
            this.activity = activity;
            this.urlString = urlString;
            this.imageView = imageView;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (this.urlString != null) {
                LukeNetUtils lukeNetUtils = new LukeNetUtils(this.activity);
                try {
                    this.bitmap = lukeNetUtils.getBitmapFromURL(urlString);
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("PERKELE", "doInBackground: ", e);
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
                    if (loadImageTask.this.bitmap == null) {
                        loadImageTask.this.imageView.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.luke_default_profile_pic, null));
                    } else {
                        loadImageTask.this.imageView.setImageBitmap(loadImageTask.this.bitmap);
                    }
                }
            });
            super.onPostExecute(aVoid);
        }
    }

}
