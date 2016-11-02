package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;

public class MapFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MapFragment";
    private View fragmentView;
    private Button pointOfInterestButton;
    private Button newSubmissionButton;
    private Button leaderboardButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: MAP fragment" );
        fragmentView = inflater.inflate(R.layout.fragment_map, container, false);
        pointOfInterestButton = (Button) fragmentView.findViewById(R.id.poi_button);
        newSubmissionButton = (Button) fragmentView.findViewById(R.id.new_submission_button);
        leaderboardButton = (Button) fragmentView.findViewById(R.id.leaderboard_button);
        setupButtons();
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.poi_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_POINT_OF_INTEREST);
                break;
            case R.id.new_submission_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_NEW_SUBMISSION);
                break;
            case R.id.leaderboard_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_LEADERBOARD);
                break;
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
        pointOfInterestButton.setOnClickListener(this);
        newSubmissionButton.setOnClickListener(this);
        leaderboardButton.setOnClickListener(this);
    }
}
