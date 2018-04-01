package com.arielg.loudtime;

import android.content.Intent;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    //====================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.tabIndicator, null));
        else
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.tabIndicator));
    }

    //====================================================================================================
    @Override
    protected void onResume() {
        super.onResume();
        handleIntents();
    }

    //====================================================================================================
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Use onNewIntent() with single-instance Activity (android:launchMode="singleTask").
        // Calling getIntent() in onResume() for a single-instance Activity will return the Intent that
        // originally started the Activity, not the Intent that the Activity just received. To ensure
        // that getIntent() always returns the last-received Intent in a single-instance Activity, use
        // setIntent() to store the last-received Intent.
        setIntent(intent);
    }

    //====================================================================================================
    private void handleIntents() {

        Intent intent = getIntent();
        String action = intent.getAction();

        if(action != null && action.equals(Intent.ACTION_MAIN)) {

            String openFragment;

            if( (openFragment = intent.getStringExtra(BaseFragment.PARAM_OPEN_FRAGMENT)) != null) {

                if(openFragment.equals(BaseFragment.PARAM_VALUE_OPEN_TIMER_FRAGMENT))
                    mViewPager.setCurrentItem(0, true);
                else if(openFragment.equals(BaseFragment.PARAM_VALUE_OPEN_STOPWATCH_FRAGMENT))
                    mViewPager.setCurrentItem(1, true);
            }
        }
    }

    //====================================================================================================
    // A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        //====================================================================================================
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return TimerFragment.newInstance(position + 1);
                case 1:
                    return StopwatchFragment.newInstance(position + 1);
            }
            return null;
        }

        //====================================================================================================
        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        //====================================================================================================
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "T i m e r";
                case 1:
                    return "S t o p w a t c h";
            }
            return null;
        }
    }
}
