package com.luke.lukef.lukeapp.fragments;

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
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Submission;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Bang Nguyen on 11/15/2016.
 */

public class UserSubmissionFragment extends Fragment {
    View fragmentView;
    RecyclerView recyclerView;

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

        recyclerView = (RecyclerView) getActivity().findViewById(R.id.submit_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        CardViewAdapter cardViewAdapter = new CardViewAdapter(createList(50));
        recyclerView.setAdapter(cardViewAdapter);
    }

    //  Create a list of user's submissions
    private List<Submission> createList(int size) {
        List<Submission> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Submission submission = new Submission();
//            submission.datE = Submission.DATE + i;
//            submission.contenT = Submission.CONTENT + i;
            result.add(submission);
        }
        return result;
    }
}
