package org.intelehealth.app.appointmentNew;

import android.content.Context;
import android.os.Parcelable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class MyAppointmentsPagerAdapter extends FragmentPagerAdapter {

    int tabCount;
    Context context;

    public MyAppointmentsPagerAdapter(FragmentManager fm, int numberOfTabs, Context context) {
        super(fm);
        this.tabCount = numberOfTabs;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new AllAppointmentsFragment();

            case 0:

            default:
                return new TodaysMyAppointmentsFragment();
        }

    }

    @Override
    public int getCount() {
        return tabCount;
    }

  /*  @Override
    public int getItemPosition(Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter
        return POSITION_NONE;
    }*/

    @Override
    public Parcelable saveState() {
        return null;
    }
}