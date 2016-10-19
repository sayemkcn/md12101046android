package net.toracode.moviedb;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import net.toracode.moviedb.commons.Pref;
import net.toracode.moviedb.entity.CategoryEntity;
import net.toracode.moviedb.events.UserCategoryLoadEvent;
import net.toracode.moviedb.fragmenthelpers.FeaturedFragmentHelper;
import net.toracode.moviedb.fragmenthelpers.MainFragmentHelper;
import net.toracode.moviedb.service.Commons;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindArray(R.array.categories)
    String[] categories;
    @BindArray(R.array.colors)
    String[] colors;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.collapsingBarlayout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.appBarImageView)
    ImageView appBarImageViw;

    private List<CategoryEntity> categoryList;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private boolean isUserRegistered = false;

    InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Interstitial ad load

        // Create the interstitial.
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getResources().getString(R.string.intertitial_ad_unit_id));

        // Create ad request.
        AdRequest intAdRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(getResources().getString(R.string.device_id))
                .build();

        // Begin loading your interstitial.
        interstitial.loadAd(intAdRequest);

        // End Loading Interstitial

        // init ButterKnife
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        // load user custom CategoryList
        if (this.isUserRegistered)
            Commons.loadUserCategoryList(getResources().getString(R.string.categoryUrl));
        else
            Commons.loadUserCategoryListFromResource(getCategoryListFromResource());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation Drawer
        DrawerLayout drawer = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        this.navigationView.setNavigationItemSelectedListener(this);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        // setup tablayout with viewpager
        this.mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                // show interstitial
                if (interstitial.isLoaded())
                    interstitial.show();
                // end

                Pref.savePreference(MainActivity.this, "page_number", position);

                collapsingToolbarLayout.setTitle(mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()).getText());
//                if (isUserRegistered && categoryList != null) {
                if (categoryList != null) {
                    Glide.with(MainActivity.this).load(categoryList.get(mTabLayout.getSelectedTabPosition()).getIconUrl()).placeholder(R.drawable.bdbn_banner).into(appBarImageViw);
                    collapsingToolbarLayout.setBackgroundColor(Color.parseColor(categoryList.get(mTabLayout.getSelectedTabPosition()).getAccentColorCode()));
                }
//                } else
//                    collapsingToolbarLayout.setBackgroundColor(Color.parseColor(colors[mTabLayout.getSelectedTabPosition()]));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private String getCategoryListFromResource() {
        InputStream is = getResources().openRawResource(R.raw.category_list);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {

        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        Log.d("HELLO", writer.toString());
        return writer.toString();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserCategoryListLoaded(UserCategoryLoadEvent e) {
        this.mTabLayout.removeAllTabs();
        this.categoryList = e.getCategoryList();
        for (int i = 0; i < e.getCategoryList().size(); i++) {
            TabLayout.Tab tab = this.mTabLayout.newTab();
            tab.setText(e.getCategoryList().get(i).getName());
            this.mTabLayout.addTab(tab);
        }
        this.changeTabsFont(this.mTabLayout);
    }

    private void changeTabsFont(TabLayout tabLayout) {
        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/SolaimanLipi.ttf");

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(typeface, Typeface.BOLD);
                }
            }
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            Commons.share(this, "Share this app", "Download BDBN.NEWS aggregator for android " + getResources().getString(R.string.app_url));
        } else if (id == R.id.action_about) {
            Commons.showDevDialog(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            mViewPager.setCurrentItem(0);
        } else if (id == R.id.nav_bookmark) {
            startActivity(new Intent(this, OfflineNewsActivity.class).putExtra("key", Pref.PREF_KEY_WISH_LIST));
        } else if (id == R.id.nav_offline_news) {
            startActivity(new Intent(this, OfflineNewsActivity.class).putExtra("key", Pref.PREF_KEY_OFFLINE_LIST));
        } else if (id == R.id.nav_share) {
            Commons.share(this, "Share this app", "Download BDBN.NEWS aggregator for android " + getResources().getString(R.string.app_url));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber - 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 0:
                    rootView = inflater.inflate(R.layout.fragment_featured, container, false);
                    FeaturedFragmentHelper featuredFragmentHelper = new FeaturedFragmentHelper(getActivity(), rootView);
                    featuredFragmentHelper.exec(getArguments().getInt(ARG_SECTION_NUMBER));
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    MainFragmentHelper mainFragmentHelper = new MainFragmentHelper(getActivity(), rootView);
                    mainFragmentHelper.exec(getArguments().getInt(ARG_SECTION_NUMBER));
                    break;

            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show ${categories.length} total pages.
            return MainActivity.this.categories.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return MainActivity.this.categories[position];
        }
    }
}