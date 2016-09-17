package com.pradeep.myreddit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pradeep.myreddit.events.*;
import com.pradeep.myreddit.fragments.CommentsFragment;
import com.pradeep.myreddit.fragments.ContentViewerFragment;
import com.pradeep.myreddit.fragments.ManageSubredditsFragment;
import com.pradeep.myreddit.fragments.PostsFragment;
import com.pradeep.myreddit.models.Subreddit;
import com.pradeep.myreddit.models.User;
import com.pradeep.myreddit.network.AuthManager;
import com.pradeep.myreddit.network.Callback;
import com.pradeep.myreddit.network.SubredditsManager;

import de.greenrobot.event.EventBus;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar appBar;
    private TabLayout subredditTabs;
    private ViewPager viewPager;
    private SubredditPagerAdapter subredditPagerAdapter;
    private String subreddit;
    private String sort;
    private static final String SUBREDDIT_BUNDLE_KEY = "subreddit_key";
    private static final String SORT_BUNDLE_KEY = "sort_key";
    private ProgressDialog progressDialog;
    private boolean isRefreshingAccessToken = false; // Makes sure that multiple 'refresh access token' are not being sent at the same time
    private FirebaseAnalytics mFirebaseAnalytics;
    public static String AUTHORITY = "com.pradeep.myreddit.myprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Analytics instance
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        /**
         * Find views
         */

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        subredditTabs = (TabLayout) findViewById(R.id.subreddit_tabs);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        /**
         * Restore saved values from instance state. If not available, set default vaules.
         */

        if (savedInstanceState == null) {
            subreddit = getResources().getString(R.string.front_page);
            sort = getResources().getString(R.string.action_sort_hot);
        } else {
            subreddit = savedInstanceState.getString(SUBREDDIT_BUNDLE_KEY);
            sort = savedInstanceState.getString(SORT_BUNDLE_KEY);
        }

        /**
         * Setup AppBar
         */

        setSupportActionBar(toolbar);
        appBar = getSupportActionBar();
        updateAppBarTitlesWithPostInfo();

        /**
         * Setup progress dialog which will be displayed while network operations are being performed
         */

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.label_fetching_subreddits));
        progressDialog.show();

        /**
         * Setup tabs and viewpager
         */

        SubredditsManager.getSubreddits(new Callback<List<Subreddit>>() {

            @Override
            public void onSuccess(List<Subreddit> data) {
                progressDialog.hide();

                getContentResolver().delete(CONTENT_URI, null, null);
                ContentValues cv = new ContentValues();
                for (Subreddit subr : data) {
                    if (subr.isSelected()) {
                        cv.put("subreddit_name", subr.getName());
                        getContentResolver().insert(CONTENT_URI, cv);
                    }
                }


                Cursor resultCursor = getContentResolver().query(CONTENT_URI, null,
                        "@", null, null);
                resultCursor.moveToFirst();

                setupViewPagerAndTabs(data);
            }

            @Override
            public void onFailure(String message) {
                progressDialog.hide();

                if (message != null)
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupViewPagerAndTabs(final List<Subreddit> subreddits) {
        // keep selected subreddits only
        final List<Subreddit> selectedSubreddits = new ArrayList<Subreddit>();
        for (Subreddit subreddit : subreddits) {
            if (subreddit.isSelected()) {
                selectedSubreddits.add(subreddit);

                // Logs the subreddit using Analytics
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, subreddit.getId());
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, subreddit.getName());
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        }

        // remove any existing onPageChange listeners in order to prevent multiple listeners from being attached
        viewPager.clearOnPageChangeListeners();

        // setup view pager
        subredditPagerAdapter = new SubredditPagerAdapter(selectedSubreddits);
        viewPager.setAdapter(subredditPagerAdapter);
        ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                subreddit = selectedSubreddits.get(position).getName();
                updateAppBarTitlesWithPostInfo();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPager.addOnPageChangeListener(onPageChangeListener);
        // bind view pager to tabs
        subredditTabs.setupWithViewPager(viewPager);

        // select active tab
        for (int i = 0; i < subredditTabs.getTabCount(); i++) {
            TabLayout.Tab tab = subredditTabs.getTabAt(i);
            if (tab.getText().toString().equals(subreddit)) {
                tab.select();
                return;
            }
        }

        // if no active tab found, set first tab as active
        subredditTabs.getTabAt(0).select();
        viewPager.setCurrentItem(0);
        onPageChangeListener.onPageSelected(0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        progressDialog.dismiss();

        super.onDestroy();
    }

    public void onEventMainThread(ViewContentEvent event) {
        if (event.getUrl().contains("youtube.com") || event.getUrl().contains("youtu.be")) {
            Intent viewIntent = new Intent();
            viewIntent.setAction(Intent.ACTION_VIEW);
            viewIntent.setData(Uri.parse(event.getUrl()));

            if (viewIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(viewIntent);
                return;
            }
        }

        ContentViewerFragment contentViewerFragment =
                (ContentViewerFragment) getSupportFragmentManager().findFragmentByTag(ContentViewerFragment.TAG);

        if (contentViewerFragment == null) {
            contentViewerFragment = ContentViewerFragment.newInstance(event.getContentTitle(), event.getUrl());
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, 0)
                    .add(android.R.id.content, contentViewerFragment, ContentViewerFragment.TAG)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, 0)
                    .show(contentViewerFragment)
                    .commit();
            contentViewerFragment.loadContent(event.getContentTitle(), event.getUrl());
            contentViewerFragment.getView().bringToFront();
        }
    }

    public void onEventMainThread(HideContentViewerEvent event) {
        ContentViewerFragment contentViewerFragment =
                (ContentViewerFragment) getSupportFragmentManager().findFragmentByTag(ContentViewerFragment.TAG);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(0, R.anim.fade_out)
                .hide(contentViewerFragment)
                .commit();
    }

    public void onEventMainThread(SubredditPreferencesUpdatedEvent event) {
        setupViewPagerAndTabs(event.getSubreddits());
        updateAppBarTitlesWithPostInfo();
    }

    public void onEventMainThread(ViewCommentsEvent event) {
        CommentsFragment commentsFragment =
                CommentsFragment.newInstance(event.getSelectedPost());
        FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();
        fTransaction.setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out);
        fTransaction.add(android.R.id.content, commentsFragment);
        fTransaction.addToBackStack(null);
        fTransaction.commit();
    }

    public void onEventMainThread(OauthCallbackEvent event) {
        if (event.getError() != null) {
            Toast.makeText(this, event.getError(), Toast.LENGTH_LONG).show();
        } else {
            progressDialog.setMessage(getResources().getString(R.string.label_logging_in));
            progressDialog.show();

            AuthManager.auth(event.getCode(), event.getState(), new Callback<User>() {

                @Override
                public void onSuccess(User data) {
                    progressDialog.hide();

                    EventBus.getDefault().post(new AuthenticatedEvent(data));
                }

                @Override
                public void onFailure(String message) {
                    progressDialog.hide();

                    if (message != null)
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

//    public void onEventMainThread(final AuthenticatedEvent event) {
//        progressDialog.setMessage(getResources().getString(R.string.label_fetching_subreddits));
//        progressDialog.show();
//
//        SubredditsManager.getSubreddits(new Callback<List<Subreddit>>() {
//
//            @Override
//            public void onSuccess(List<Subreddit> data) {
//                progressDialog.hide();
//
//                setupViewPagerAndTabs(data);
//                updateAppBarTitlesWithPostInfo();
//
//                Toast.makeText(MainActivity.this, getResources().getString(R.string.success_logged_in_base) +
//                        event.getUser().getUsername(), Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onFailure(String message) {
//                progressDialog.hide();
//
//                if (message != null)
//                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
//            }
//        });
//    }

//    public void onEventMainThread(DeauthenticatedEvent event) {
//        progressDialog.setMessage(getResources().getString(R.string.label_fetching_subreddits));
//        progressDialog.show();
//
//        SubredditsManager.getSubreddits(new Callback<List<Subreddit>>() {
//
//            @Override
//            public void onSuccess(List<Subreddit> data) {
//                progressDialog.hide();
//
//                setupViewPagerAndTabs(data);
//                updateAppBarTitlesWithPostInfo();
//            }
//
//            @Override
//            public void onFailure(String message) {
//                progressDialog.hide();
//
//                if (message != null)
//                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
//            }
//        });
//    }

    public void onEventMainThread(ViewSubredditPostsEvent event) {
        FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();
        fTransaction.setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out);
        fTransaction.add(android.R.id.content,
                PostsFragment.newInstance(event.getSubreddit(), event.getSort(), true));
        fTransaction.addToBackStack(null);
        fTransaction.commit();
    }

    public void onEventMainThread(ShareContentEvent event) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, event.getContent());
        sendIntent.setType(event.getMimeType());
        startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.label_share_via)));
    }

//    public void onEventMainThread(AccessTokenExpiredEvent event) {
//        if (!isRefreshingAccessToken) {
//            isRefreshingAccessToken = true;
//
//            progressDialog.setMessage(getResources().getString(R.string.label_refreshing_user_session));
//            progressDialog.show();
//
//            AuthManager.refreshAccessToken(new Callback<Void>() {
//
//                @Override
//                public void onSuccess(Void data) {
//                    progressDialog.hide();
//
//                    isRefreshingAccessToken = false;
//
//                    Toast.makeText(MainActivity.this, R.string.success_user_session_refreshed, Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onFailure(String message) {
//                    progressDialog.hide();
//
//                    if (message != null)
//                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
//                }
//            });
//        }
//    }

    private void updateAppBarTitlesWithPostInfo() {
        appBar.setTitle(subreddit);
        appBar.setSubtitle(sort);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SUBREDDIT_BUNDLE_KEY, subreddit);
        outState.putString(SORT_BUNDLE_KEY, sort);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // called every time the menu is about to be shown
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort_hot || id == R.id.action_sort_new || id == R.id.action_sort_rising ||
                id == R.id.action_sort_controversial || id == R.id.action_sort_top) {
            sort = item.getTitle().toString();
            updateAppBarTitlesWithPostInfo();
            subredditPagerAdapter.getCurrentFragment().updateSort(sort);
            return true;
        } else if (id == R.id.action_go_to_subreddit) {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_go_tp_subreddit, null);
            final EditText subredditEditText = (EditText) dialogView.findViewById(R.id.subreddit_edittext);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.action_go_to_subreddit)
                    .setView(dialogView)
                    .setPositiveButton(R.string.action_go, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String subredditName = subredditEditText.getText().toString();
                            if (TextUtils.isEmpty(subredditName)) {
                                Toast.makeText(MainActivity.this, R.string.error_subreddit_name_required, Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                EventBus.getDefault().post(new ViewSubredditPostsEvent(subredditEditText.getText().toString(),
                                        getResources().getString(R.string.action_sort_hot)));
                            }
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .create();
            dialog.show();
        } else if (id == R.id.action_manage_subreddits) {
            FragmentTransaction fTransaction = getSupportFragmentManager().beginTransaction();
            fTransaction.setCustomAnimations(R.anim.fade_in, 0, 0, R.anim.fade_out);
            fTransaction.add(android.R.id.content, new ManageSubredditsFragment());
            fTransaction.addToBackStack(null);
            fTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // if back button is pressed while content viewer fragment is visible, delegate call to its onBackPressed method
        // else, pop back stack (if possible)
        ContentViewerFragment contentViewerFragment =
                (ContentViewerFragment) getSupportFragmentManager().findFragmentByTag(ContentViewerFragment.TAG);
        if (contentViewerFragment != null && contentViewerFragment.isVisible()) {
            contentViewerFragment.onBackPressed();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private class SubredditPagerAdapter extends FragmentStatePagerAdapter {

        private List<Subreddit> subreddits;

        public SubredditPagerAdapter(List<Subreddit> subreddits) {
            super(getSupportFragmentManager());

            this.subreddits = subreddits;
        }

        @Override
        public Fragment getItem(int position) {
            return PostsFragment.newInstance(subreddits.get(position).getName(), sort, false);
        }

        @Override
        public int getCount() {
            return subreddits.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return subreddits.get(position).getName();
        }

        public PostsFragment getCurrentFragment() {
            return (PostsFragment) instantiateItem(viewPager, viewPager.getCurrentItem());
        }
    }
}
