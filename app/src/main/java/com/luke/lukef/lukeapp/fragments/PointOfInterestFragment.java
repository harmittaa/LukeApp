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

public class PointOfInterestFragment extends Fragment implements View.OnClickListener {
    View fragmentView;
    Button poiMapButton;
    Button profileButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_point_of_interest, container, false);
        profileButton = (Button) fragmentView.findViewById(R.id.profile_button);
        poiMapButton = (Button) fragmentView.findViewById(R.id.poi_map_button);
        setupButtons();
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.poi_map_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP,null);
                break;
            case R.id.profile_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_PROFILE,null);
                break;
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
        profileButton.setOnClickListener(this);
        poiMapButton.setOnClickListener(this);
    }
}
