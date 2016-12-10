package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.UserFromServer;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;
import com.luke.lukef.lukeapp.v4fragments.TabFragmentAchievements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    View fragmentView;
    TextView username, title, score;
    ImageView profileImage;
    TabLayout tabLayout;
    ViewPager viewPager;
    private String userID;
    private ImageButton backButton;
    Bundle extras;
    LukeNetUtils lukeNetUtils;
    UserFromServer userFromServer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        lukeNetUtils = new LukeNetUtils(getMainActivity());
        tabLayout = (TabLayout) fragmentView.findViewById(R.id.tabLayout);
        viewPager = (ViewPager) fragmentView.findViewById(R.id.viewPager);
        username = (TextView) fragmentView.findViewById(R.id.usernamedisplay);
        title = (TextView) fragmentView.findViewById(R.id.title);
        score = (TextView) fragmentView.findViewById(R.id.progressTextView);
        profileImage = (ImageView) fragmentView.findViewById(R.id.profieImage);
        this.backButton = (ImageButton) fragmentView.findViewById(R.id.button_back);
        this.backButton.setOnClickListener(this);
        extras = getArguments();
        this.userID = extras.getString("userId");
        setupTabLayout();
        setUserProfile();
        return fragmentView;
    }

    private void setupTabLayout() {
        PageAdapter pageAdapter = new PageAdapter(getMainActivity().getSupportFragmentManager(), this.extras);
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

    /**
     * Sets the user profile image from URL or if there's no URL then defaults
     */
    private void setUserProfile() {
        try {
            this.userFromServer = lukeNetUtils.getUserFromUserId(this.userID);

            if (!TextUtils.isEmpty(this.userFromServer.getImageUrl())) {
                Bitmap b = lukeNetUtils.getBitmapFromURL(this.userFromServer.getImageUrl());
                if (b == null) {
                    this.profileImage.setImageBitmap(BitmapFactory.decodeResource(getMainActivity().getResources(), R.drawable.luke_default_profile_pic));
                } else {
                    this.profileImage.setImageBitmap(b);
                }
            } else {
                this.profileImage.setImageBitmap(BitmapFactory.decodeResource(getMainActivity().getResources(), R.drawable.luke_default_profile_pic));
            }
            this.username.setText(userFromServer.getUsername());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_back:
                getMainActivity().onBackPressed();
                break;
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

        public PageAdapter(FragmentManager fm, Bundle extras) {
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
