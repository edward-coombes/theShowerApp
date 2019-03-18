package online.umassdartmouthsustainability.theshowerapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import online.umassdartmouthsustainability.theshowerapp.fragments.UserMenuFragment;

class MyPagerAdapter extends FragmentPagerAdapter {
    private int NUM_ITEMS = 3;
    private volatile String accessToken;

    MyPagerAdapter(FragmentManager fragmentManager, String t) {
        super(fragmentManager);
        accessToken = t;

    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return UserMenuFragment.newInstance();
            case 1:
                return online.umassdartmouthsustainability.theshowerapp.fragments.StopwatchFragment.newInstance();
            case 2:
                return SpotifyMainFragment.newInstance(accessToken);
            default:
                return null;
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "History";
            case 1:
                return "Timer";
            case 2:
                return "Spotify";
            default:
                return "";
        }
    }

}