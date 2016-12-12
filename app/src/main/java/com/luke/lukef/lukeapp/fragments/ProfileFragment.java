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

// TODO: 12/12/2016 DANIEL

/**
 * Handles the profile fragment, setting up the tab layout
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    private View fragmentView;
    private TextView username;
    private TextView title;
    private TextView score;
    private ImageView profileImage;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String userID;
    private Bundle extras;
    private LukeNetUtils lukeNetUtils;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        this.lukeNetUtils = new LukeNetUtils(getMainActivity());
        setupViews();
        this.extras = getArguments();
        this.userID = extras.getString("userId");
        setupTabLayout();
        setUserProfile();
        return this.fragmentView;
    }

    /**
     * Finds the view elements and sets the necessary onClick listeners
     */
    private void setupViews() {
        this.tabLayout = (TabLayout) this.fragmentView.findViewById(R.id.tabLayout);
        this.viewPager = (ViewPager) this.fragmentView.findViewById(R.id.viewPager);
        this.username = (TextView) this.fragmentView.findViewById(R.id.usernamedisplay);
        this.title = (TextView) this.fragmentView.findViewById(R.id.title);
        this.score = (TextView) this.fragmentView.findViewById(R.id.progressTextView);
        this.profileImage = (ImageView) this.fragmentView.findViewById(R.id.profieImage);
        ImageButton backButton = (ImageButton) this.fragmentView.findViewById(R.id.button_back);
        backButton.setOnClickListener(this);
    }

    // TODO: 12/12/2016 DANIEL
    private void setupTabLayout() {
        PageAdapter pageAdapter = new PageAdapter(getMainActivity().getSupportFragmentManager(), this.extras);
        this.viewPager.setAdapter(pageAdapter);

        this.tabLayout.setupWithViewPager(this.viewPager);
        this.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
     * Sets the user profile image from URL or if there's no URL then sets the default profile image
     */
    private void setUserProfile() {
        try {
            UserFromServer userFromServer = this.lukeNetUtils.getUserFromUserId(this.userID);

            if (!TextUtils.isEmpty(userFromServer.getImageUrl())) {
                Bitmap b = this.lukeNetUtils.getBitmapFromURL(userFromServer.getImageUrl());
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

    // TODO: 12/12/2016 DANIEL
    private class PageAdapter extends FragmentStatePagerAdapter {

        private String[] tabTitles = new String[]{"Submissions", "Achievements"};
        private Bundle bundle;

        @Override
        public CharSequence getPageTitle(int position) {
            return this.tabTitles[position];
        }

        List<android.support.v4.app.Fragment> fragments;

        PageAdapter(FragmentManager fm, Bundle extras) {
            super(fm);
            UserSubmissionFragment userSubmissionFragment = new UserSubmissionFragment();
            userSubmissionFragment.setUserId(ProfileFragment.this.userID);
            TabFragmentAchievements achievementFragment = new TabFragmentAchievements();
            this.fragments = new ArrayList<>();
            this.fragments.add(userSubmissionFragment);
            this.fragments.add(achievementFragment);
            this.bundle = extras;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            android.support.v4.app.Fragment f = this.fragments.get(position);
            f.setArguments(this.bundle);
            return f;
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }
}
