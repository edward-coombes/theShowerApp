package online.umassdartmouthsustainability.theshowerapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.ImageView;

import online.umassdartmouthsustainability.theshowerapp.fragments.UserMenuFragment;

class MyPagerAdapter extends FragmentPagerAdapter {
    private int NUM_ITEMS = 3;
    public static final int USER_MENU = 0;
    public static final int STOPWATCH = 1;
    public static final int SPOTIFY = 2;

    MyPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);

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
            case USER_MENU:
                return UserMenuFragment.newInstance();
            case STOPWATCH:
                return online.umassdartmouthsustainability.theshowerapp.fragments.StopwatchFragment.newInstance();
            case SPOTIFY:
                return SpotifyMainFragment.newInstance();
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