package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;

public class NewSubmissionFragment extends Fragment implements View.OnClickListener {
    View fragmentView;
    Button cameraButton;
    Button categoryButton;
    EditText submissionTitle;
    EditText submissionDescription;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_new_submission, container, false);
        cameraButton = (Button) fragmentView.findViewById(R.id.activateCameraButton);
        categoryButton = (Button) fragmentView.findViewById(R.id.buttonCategory);
        setupButtons();
        getMainActivity().setBottomBarButtons(Constants.bottomActionBarStates.BACK_TICK);
        this.setBottomButtonListeners();
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirmation_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_CONFIRMATION);
                break;
            case R.id.button_back1:
                getMainActivity().onBackPressed();
                break;
            case R.id.button_back2:
                getMainActivity().onBackPressed();
                break;
            case R.id.button_tick:
                // TODO: 18/11/2016 check the submission valididty, then submit it
                break;
            case R.id.activateCameraButton:
                // TODO: 18/11/2016 activate camera 
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
        cameraButton.setOnClickListener(this);
        categoryButton.setOnClickListener(this);
    }

    private void setBottomButtonListeners() {
        LinearLayout v = getMainActivity().getBottomBar();
        final int childcount = v.getChildCount();
        for (int i = 0; i < childcount; i++) {
            View view = v.getChildAt(i);
            if (view instanceof Button || view instanceof ImageButton) {
                view.setOnClickListener(this);
            }
        }
    }
}
