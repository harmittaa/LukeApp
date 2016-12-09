package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.v4fragments.TabFragmentAchievements;
import com.luke.lukef.lukeapp.v4fragments.TabFragmentSubmissions;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    View fragmentView;
    Button profileMapButton;
    Button profileLeaderboard;
    Button profilePoi;
    TextView Username, Title, Score;
    ImageView ProfileImage;
    ImageView Submission;
    TabLayout tabLayout;
    ViewPager viewPager;
    private String userID;
    Bundle extras;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        tabLayout = (TabLayout) fragmentView.findViewById(R.id.tabLayout);
        viewPager = (ViewPager) fragmentView.findViewById(R.id.viewPager);
        Username = (TextView) fragmentView.findViewById(R.id.usernamedisplay);
        Title = (TextView) fragmentView.findViewById(R.id.title);
        Score = (TextView) fragmentView.findViewById(R.id.Score);
        ProfileImage = (ImageView) fragmentView.findViewById(R.id.profieImage);
        extras = getArguments();
        this.userID = extras.getString("userId");
        setupTabLayout();
        return fragmentView;
    }

    private void setupTabLayout() {
        PageAdapter pageAdapter = new PageAdapter(getMainActivity().getSupportFragmentManager(),this.extras);
        viewPager.setAdapter(pageAdapter);

        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


    private class PageAdapter extends FragmentStatePagerAdapter {

        private String[] tabTitles = new String[]{"Submissions", "Achievements"};
        private Bundle bundle;

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        List<android.support.v4.app.Fragment> fragments;

        public PageAdapter(FragmentManager fm,Bundle extras) {
            super(fm);
            UserSubmissionFragment userSubmissionFragment = new UserSubmissionFragment();
            userSubmissionFragment.setUserId(ProfileFragment.this.userID);
            TabFragmentAchievements achievementFragment = new TabFragmentAchievements();
            fragments = new ArrayList<>();
            fragments.add(userSubmissionFragment);
            fragments.add(achievementFragment);
            this.bundle = extras;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            android.support.v4.app.Fragment f = fragments.get(position);
            f.setArguments(this.bundle);
            return f;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
