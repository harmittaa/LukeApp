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

package com.luke.lukef.lukeapp.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.luke.lukef.lukeapp.tools.LukeNetUtils;

import java.util.ArrayList;

/**
 * Fragment to be passed into the tablayout that displays all of a users submissions in a cardview
 */
public class UserSubmissionFragment extends Fragment {
    private View fragmentView;
    private RecyclerView recyclerView;
    private String userId;
    private ArrayList<Submission> submissions;
    private CardViewAdapter cardViewAdapter;
    private static final String TAG = "UserSubmissionFragment";

    public UserSubmissionFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.fragmentView = inflater.inflate(R.layout.fragment_user_submission, container, false);
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.recyclerView = (RecyclerView) this.fragmentView.findViewById(R.id.recyclerViewUserSubmissions);
        this.recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.userId = getArguments().getString("userId");
        UserSubmissionsAsync userSubmissionsAsync = new UserSubmissionsAsync(this.userId);
        userSubmissionsAsync.execute();
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    /**
     * Asynctask that fetches a users submissions. Only once this is done, can a new cardviewadapter be created with a list of submissions
     */
    private class UserSubmissionsAsync extends AsyncTask<Void, Void, Void> {
        private String userId;

        UserSubmissionsAsync(String userId) {
            this.userId = userId;
        }

        @Override
        protected Void doInBackground(Void... params) {
            LukeNetUtils lukeNetUtils = new LukeNetUtils(getMainActivity());
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
