package digital.edgelabs.bdbnnewsedgelabs;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import digital.edgelabs.bdbnnewsedgelabs.entity.CategoryEntity;
import digital.edgelabs.bdbnnewsedgelabs.events.UserCategoryLoadEvent;
import digital.edgelabs.bdbnnewsedgelabs.fragmenthelpers.MainFragmentHelper;
import digital.edgelabs.bdbnnewsedgelabs.service.Commons;

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

    private boolean isUserRegistered = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init ButterKnife
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        // load user custom CategoryList
        if (this.isUserRegistered)
            Commons.loadUserCategoryList("http://ekushay.com/picosoft/bdbn/category/list.json");

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
                collapsingToolbarLayout.setTitle(mTabLayout.getTabAt(mTabLayout.getSelectedTabPosition()).getText());
                if (isUserRegistered && categoryList != null) {
                    Glide.with(MainActivity.this).load(categoryList.get(mTabLayout.getSelectedTabPosition()).getIconUrl()).placeholder(R.mipmap.ic_launcher).into(appBarImageViw);
                    collapsingToolbarLayout.setBackgroundColor(Color.parseColor(categoryList.get(mTabLayout.getSelectedTabPosition()).getAccentColorCode()));
                } else
                    collapsingToolbarLayout.setBackgroundColor(Color.parseColor(colors[mTabLayout.getSelectedTabPosition()]));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

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
        Typeface typeface = Typeface.createFromAsset(this.getAssets(),"fonts/SolaimanLipi.ttf");

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(typeface,Typeface.BOLD);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Toast.makeText(getApplicationContext(), id + " Selected", Toast.LENGTH_SHORT).show();
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
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;
//            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
//                case 1:
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            MainFragmentHelper mainFragmentHelper = new MainFragmentHelper(getActivity(), rootView);
            mainFragmentHelper.exec(getArguments().getInt(ARG_SECTION_NUMBER));
//                    break;

//            }
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
            // Show 3 total pages.
            return MainActivity.this.categories.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            switch (position) {
//                case 0:
//                    return "SECTION 1";
//                case 1:
//                    return "SECTION 2";
//                case 2:
//                    return "SECTION 3";
//            }
            if (isUserRegistered && categoryList != null)
                return categoryList.get(position).getName();
            else
                return MainActivity.this.categories[position];


        }
    }
}
