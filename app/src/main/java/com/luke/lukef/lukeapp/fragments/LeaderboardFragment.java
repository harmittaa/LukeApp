package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;


public class LeaderboardFragment extends Fragment implements View.OnClickListener {
    private View fragmentView;
    Button leaderboardMapButton;
    Button userProfileButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        leaderboardMapButton = (Button) fragmentView.findViewById(R.id.leaderboard_map_button);
        userProfileButton = (Button) fragmentView.findViewById(R.id.user_profile_button);
        setupButtons();
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.leaderboard_map_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP);
                break;
            case R.id.user_profile_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_PROFILE);
                break;
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
        leaderboardMapButton.setOnClickListener(this);
        userProfileButton.setOnClickListener(this);
    }

}
