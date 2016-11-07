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

public class ConfirmationFragment extends Fragment implements View.OnClickListener {
    View fragmentView;
    Button confirmationMapButton;
    Button confirmationSubmissionButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_confirmation, container, false);
        confirmationMapButton = (Button) fragmentView.findViewById(R.id.confirmation_map_button);
        confirmationSubmissionButton = (Button) fragmentView.findViewById(R.id.confirmation_submission_button);
        setupButtons();
        getMainActivity().setBottomBarButtons(Constants.bottomActionBarStates.BACK_TICK);
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirmation_map_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP);
                break;
            case R.id.confirmation_submission_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_NEW_SUBMISSION);
                break;
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
        confirmationMapButton.setOnClickListener(this);
        confirmationSubmissionButton.setOnClickListener(this);
    }

}
