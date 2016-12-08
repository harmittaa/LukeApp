package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    View fragmentView;
    Button profileMapButton;
    Button profileLeaderboard;
    Button profilePoi;
    TextView Username, Title, Score;
    ImageView ProfileImage, Submission;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        profileMapButton = (Button) fragmentView.findViewById(R.id.profile_map_button);
        profileLeaderboard = (Button) fragmentView.findViewById(R.id.profile_leaderboard);
        profilePoi = (Button) fragmentView.findViewById(R.id.profile_poi);
        setupButtons();
        Username = (TextView) fragmentView.findViewById(R.id.usernamedisplay);
        Title = (TextView) fragmentView.findViewById(R.id.title);
        Score = (TextView) fragmentView.findViewById(R.id.Score);
        ProfileImage = (ImageView) fragmentView.findViewById(R.id.profieImage);
        Submission = (ImageView) fragmentView.findViewById(R.id.submission);

        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_map_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP,null);
                break;
            case R.id.profile_leaderboard:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_LEADERBOARD,null);
                break;
            case R.id.profile_poi:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_POINT_OF_INTEREST,null);
                break;
        }
    }

    
    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
        profileMapButton.setOnClickListener(this);
        profileLeaderboard.setOnClickListener(this);
        profilePoi.setOnClickListener(this);
    }
}
