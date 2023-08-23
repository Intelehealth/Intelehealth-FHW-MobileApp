package org.intelehealth.unicef.activities.informativeVideos.fragments;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.informativeVideos.adapters.InformativeVideosPagerAdapter;

import java.util.Objects;

public class InformativeVideosFragment_New extends Fragment {
    private static final String TAG = "InformativeVideosFragme";
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_informative_videos_ui2, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initUI();

    }

    private void initUI() {
        View layoutToolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar_home);
        TextView tvLocation = layoutToolbar.findViewById(R.id.tv_user_location_home);
        TextView tvLastSyncApp = layoutToolbar.findViewById(R.id.tv_app_sync_time);
        ImageView ivNotification = layoutToolbar.findViewById(R.id.imageview_notifications_home);
        ImageView ivBackArrow = layoutToolbar.findViewById(R.id.iv_hamburger);
        ivBackArrow.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ui2_ic_arrow_back_new));
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(getActivity(), HomeScreenActivity_New.class);
                startActivity(intent);*/
                FragmentManager fm = Objects.requireNonNull(getActivity()).getFragmentManager();
                fm.popBackStack();
            }
        });
        tvLocation.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        tvLastSyncApp.setVisibility(View.GONE);
        ivNotification.setVisibility(View.GONE);
        tvLocation.setText(getResources().getString(R.string.videos));
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.setVisibility(View.GONE);

        configureTabLayout();

    }

    public void configureTabLayout() {
        TabLayout tabLayout = view.findViewById(R.id.tablayout_videos);

        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.health)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.training)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.about_app)));

        ViewPager viewPager = view.findViewById(R.id.pager_videos);
        PagerAdapter adapter = new InformativeVideosPagerAdapter
                (getChildFragmentManager(), tabLayout.getTabCount(), getActivity());
        viewPager.setAdapter(adapter);
        int limit = (adapter.getCount() > 1 ? adapter.getCount() - 1 : 1);

        viewPager.setOffscreenPageLimit(limit);

        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            public void onTabReselected(TabLayout.Tab tab) {

            }

        });
    }

}