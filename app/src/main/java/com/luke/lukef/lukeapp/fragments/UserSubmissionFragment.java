package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.luke.lukef.lukeapp.R;

/**
 * Created by Bang Nguyen on 11/15/2016.
 */

public class UserSubmissionFragment extends Fragment {
    View fragmentView;
    public UserSubmissionFragment() {

        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_user_submission, container, false);

        return fragmentView;
    }
}
