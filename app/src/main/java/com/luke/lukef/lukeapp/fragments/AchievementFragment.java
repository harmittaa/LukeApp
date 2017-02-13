

package com.luke.lukef.lukeapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.luke.lukef.lukeapp.R;

/**
 * Fragment displayed in one of the tabs in the {@link ProfileFragment}. Currently has no funcitionality
 */

public class AchievementFragment extends Fragment{
    View fragmentView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_v4_submission, container, false);
        return fragmentView;
    }
}
