package org.intelehealth.app.activities.achievements.fragments;

import static org.intelehealth.app.activities.chooseLanguageActivity.SplashScreenActivity.PERMISSION_USAGE_ACCESS_STATS;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.achievements.adapters.MyAchievementsPagerAdapter;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Locale;

public class MyAchievementsFragment extends Fragment implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "MyAchievementsFragmentN";
    View view;
    ImageView ivInternet;
    NetworkUtils networkUtils;
    private ObjectAnimator syncAnimator;

    protected SessionManager sessionManager;
    public UsageStatsManager usageStatsManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(getContext());
        sessionManager = new SessionManager(requireActivity());
        usageStatsManager = (UsageStatsManager) (requireActivity().getSystemService(Context.USAGE_STATS_SERVICE));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setLocale(getContext());
        view = inflater.inflate(R.layout.fragment_my_achievements_ui2, container, false);
        networkUtils = new NetworkUtils(getActivity(), this);
        setLocale(getContext());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initUI();
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    private void initUI() {
        //View layoutToolbar = requireActivity().findViewById(R.id.toolbar_home);
        //layoutToolbar.setVisibility(View.GONE);
        TextView tvTitle = view.findViewById(R.id.tv_achievements_title);
        tvTitle.setText(getResources().getString(R.string.my_achievements));
        ivInternet = view.findViewById(R.id.iv_achievements_internet);

        ivInternet.setOnClickListener(v -> SyncUtils.syncNow(requireActivity(), ivInternet, syncAnimator));
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_home);
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.getMenu().findItem(R.id.bottom_nav_achievements).setChecked(true);
        configureTabLayout();

    }

    public void configureTabLayout() {
        TabLayout tabLayout = view.findViewById(R.id.tablayout_achievements);
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.overall)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.daily)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.date_range)));

        ViewPager viewPager = view.findViewById(R.id.pager_achievements);
        PagerAdapter adapter = new MyAchievementsPagerAdapter(getChildFragmentManager(), tabLayout.getTabCount(), getActivity());
        viewPager.setAdapter(adapter);
        int limit = (adapter.getCount() > 1 ? adapter.getCount() - 1 : 1);

        viewPager.setOffscreenPageLimit(limit);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
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

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        Log.d(TAG, "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            ivInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_internet_available));

        } else {
            ivInternet.setImageDrawable(getResources().getDrawable(R.drawable.ui2_ic_no_internet));

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}