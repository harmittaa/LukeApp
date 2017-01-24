package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.luke.lukef.lukeapp.R;

/**
 * Fragment that gets displayed in the tab view of a users profile. Implementation not done, this fragment is empty
 */

public class AchievementFragment extends Fragment {
    View fragmentView;

    public AchievementFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_achievement, container, false);

        return fragmentView;
    }
}
