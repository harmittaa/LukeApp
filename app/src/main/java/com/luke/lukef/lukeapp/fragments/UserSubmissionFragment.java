package com.luke.lukef.lukeapp.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.luke.lukef.lukeapp.CardViewAdapter;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Submission;
import com.luke.lukef.lukeapp.model.SubmissionFromServer;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Bang Nguyen on 11/15/2016.
 */

public class UserSubmissionFragment extends Fragment {
    View fragmentView;
    RecyclerView recyclerView;
    private String userId;
    private List<SubmissionFromServer> submissions;
    LukeNetUtils lukeNetUtils;
    private CardViewAdapter cardViewAdapter;
    private static final String TAG = "UserSubmissionFragment";

    public UserSubmissionFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_user_submission, container, false);
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recyclerViewUserSubmissions);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        this.userId = getArguments().getString("userId");
        UserSubmissionsAsync userSubmissionsAsync = new UserSubmissionsAsync(this.userId);
        userSubmissionsAsync.execute();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private class UserSubmissionsAsync extends AsyncTask<Void,Void,Void>{
        private String userId;
        public UserSubmissionsAsync(String userId){
            this.userId = userId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            lukeNetUtils = new LukeNetUtils(getMainActivity());
            UserSubmissionFragment.this.submissions = lukeNetUtils.getSubmissionsByUser(this.userId);
            Log.e(TAG, "doInBackground: SUBMISSIONS IS: " + UserSubmissionFragment.this.submissions);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            UserSubmissionFragment.this.cardViewAdapter = new CardViewAdapter(submissions, getMainActivity());
            recyclerView.setAdapter(cardViewAdapter);
        }
    }
}
