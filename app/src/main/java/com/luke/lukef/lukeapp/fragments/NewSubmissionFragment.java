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

public class NewSubmissionFragment extends Fragment implements View.OnClickListener {
    View fragmentView;
    Button newsubmissionMapButton;
    Button confirmationButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_new_submission, container, false);
        confirmationButton = (Button) fragmentView.findViewById(R.id.confirmation_button);
        newsubmissionMapButton = (Button) fragmentView.findViewById(R.id.newsubmission_map_button);
        setupButtons();
        getMainActivity().setBottomBarButtons(Constants.bottomActionBarStates.BACK_TICK);
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirmation_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_CONFIRMATION);
                break;
            case R.id.newsubmission_map_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP);
                break;
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
        confirmationButton.setOnClickListener(this);
        newsubmissionMapButton.setOnClickListener(this);
    }
}
