package net.toracode.moviedb;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;

import net.toracode.moviedb.commons.Pref;
import net.toracode.moviedb.fragmenthelpers.FeaturedFragmentHelper;
import net.toracode.moviedb.fragmenthelpers.MainFragmentHelper;
import net.toracode.moviedb.service.Commons;
import net.toracode.moviedb.service.ResourceProvider;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindArray(R.array.categories)
    String[] categories;
    @BindArray(R.array.colors)
    String[] colors;

    public static int APP_REQUEST_CODE = 99;

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

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init ButterKnife
        ButterKnife.bind(this);

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

    }

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

            if (loginResult.getError() != null) {
                Commons.showSimpleToast(this, loginResult.getError().getErrorType().getMessage());
//                showErrorActivity(loginResult.getError());
            } else if (loginResult.wasCancelled()) {
                Commons.showSimpleToast(getApplicationContext(), "Not cool man! not cool!");
            } else {
                if (loginResult.getAccessToken() != null) {
                    //**********LOGGED IN********//
                    // SEND REQUEST WITH THIS ACCOUNT ID //
                    registerUser(loginResult);
//                    Log.d("AUTH_TOKEN", loginResult.getAccessToken().getAccountId());
                } else {
                    Commons.showSimpleToast(getApplicationContext(), "Success:%s..." +
                            loginResult.getAuthorizationCode().substring(0, 10));
                    Log.d("AUTH_CODE", loginResult.getAuthorizationCode());

                }

                // If you have an authorization code, retrieve it from
                // loginResult.getAuthorizationCode()
                // and pass it to your server and exchange it for an access token.

                // Success! Start your next activity...
//                goToMyLoggedInActivity();
            }

        }
    }

    // registers user with account kit account id
    private void registerUser(final AccountKitLoginResult loginResult) {
        final String url = getResources().getString(R.string.baseUrl) + "user/"
                + loginResult.getAccessToken().getAccountId() + "?name=&email=";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Response response = new ResourceProvider(MainActivity.this).fetchPostResponse(url);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.code() == ResourceProvider.RESPONSE_CODE_CREATED) {
                                Commons.showSimpleToast(getApplicationContext(), "Registration successful!!");
                            } else if (response.code() == ResourceProvider.RESPONSE_CODE_FOUND) {
                                Commons.showSimpleToast(getApplicationContext(), "Registration successful!!");
                            }
                        }
                    });
                    Log.d("RESPONSE", response.toString());
                } catch (IOException e) {
                    Log.e("EXCEPTION", e.toString());
                }
            }
        }).start();

    }

    // **********END FACEBOOK ACCOUNT KIT //

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
            Commons.share(this, "Share this app", getResources().getString(R.string.shareAppText) + " " + getResources().getString(R.string.app_base_url) + getApplication().getPackageName());
        } else if (id == R.id.action_about) {
            Commons.showDevDialog(this);
            return true;
        } else if (id == R.id.action_settings) {
            this.startActivity(new Intent(this, PreferenceActivity.class));
        } else if (id == R.id.action_logout) {
            AccountKit.logOut();
            Commons.showSimpleToast(this, "You're logged out!");
        } else if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchResultsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            mViewPager.setCurrentItem(0);
        } else if (id == R.id.nav_my_list) {
            if (AccountKit.getCurrentAccessToken() != null)
                startActivity(new Intent(this, MyListActivity.class));
            else
                startActivity(new Intent(this, PreferenceActivity.class));
        } else if (id == R.id.nav_my_reviews) {
            if (AccountKit.getCurrentAccessToken() != null)
                startActivity(new Intent(this, MyReviewsActivity.class));
            else
                startActivity(new Intent(this, PreferenceActivity.class));
        } else if (id == R.id.nav_offline_news) {
            startActivity(new Intent(this, OfflineActivity.class).putExtra("key", Pref.PREF_KEY_OFFLINE_LIST));
        } else if (id == R.id.nav_share) {
            Commons.share(this, "Share this app", getResources().getString(R.string.shareAppText) + " " + getResources().getString(R.string.app_base_url) + getApplication().getPackageName());
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
