/*
 * Copyright (c) 2016, Amanj Sherwany <http://www.amanj.me>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list
 *     of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 *     of conditions and the following disclaimer in the documentation and/or other
 *     materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.amanj.splittimer.ui;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;



import me.amanj.splittimer.R;
import me.amanj.splittimer.control.IO;
import me.amanj.splittimer.messages.Message;
import me.amanj.splittimer.messages.Send;
import me.amanj.splittimer.messages.MessageTag;
import me.amanj.splittimer.model.Configurations;
import me.amanj.splittimer.model.SplitTimerFragmentAdapter;
import me.amanj.splittimer.model.TimeInformation;

public class SplitTimerActivity extends AppCompatActivity {

    private final int DEFAULT_FRAGMENT_NUMBER = 2;
    EventBus bus = EventBus.getDefault();
    private SplitTimerFragmentAdapter adapterViewPager;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private ViewPager vpPager;
    private SwitchCompat switchCompat;
    private final static String TAG = SplitTimerActivity.class.getCanonicalName();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("stored", adapterViewPager.getCount() > DEFAULT_FRAGMENT_NUMBER);
        outState.putBoolean("shouldGo", vpPager.getCurrentItem() == DEFAULT_FRAGMENT_NUMBER);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.getBoolean("stored")) {
            adapterViewPager.addFragment();
            if(savedInstanceState.getBoolean("shouldGo")) {
                vpPager.setCurrentItem(DEFAULT_FRAGMENT_NUMBER);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_timer);
        vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new SplitTimerFragmentAdapter(getSupportFragmentManager(),
                getApplicationContext(), DEFAULT_FRAGMENT_NUMBER);
        bus.register(this);
        vpPager.setAdapter(adapterViewPager);
//        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                // dummy
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                if(adapterViewPager.getCount() == 3
//                        && position != adapterViewPager.getCount() - 1) {
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                // dummy
//            }
//        });

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();


        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawer.addDrawerListener(drawerToggle);



        final MenuItem item = nvDrawer.getMenu().findItem(R.id.nav_lap_on_stop_switch);
        switchCompat = (SwitchCompat) item.getActionView();
        switchCompat.setChecked(Configurations.shouldLapOnStop());
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Configurations.setLapOnStop(isChecked);
                Log.i(TAG, "LapOnStop configuration set to: " + isChecked);
            }
        });
        item.setActionView(switchCompat);

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    selectDrawerItem(menuItem);
                    return true;
                }
            });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.nav_precision:
                PrecisionSettingDialog precisionSettingDialog = new PrecisionSettingDialog();
                mDrawer.closeDrawers();
                precisionSettingDialog.show(getSupportFragmentManager(),
                        Configurations.PRECISION_DIALOG_TAG);
                break;
            case R.id.nav_clear_history:
                bus.post(new Send<Void>() {
                    public MessageTag tag() { return MessageTag.CLEAR_HISTORY; }
                    public Void receive() { return null; }
                });
                bus.post(new Send<Void>() {
                    public MessageTag tag() { return MessageTag.REMOVE_LAST_FRAGMETN; }
                    public Void receive() { return null; }
                });
                break;
            case R.id.nav_fork_me:
                Intent github =
                    new Intent(Intent.ACTION_VIEW, Uri.parse(Configurations.APP_FORK_URL));
                startActivity(github);
                break;
            case R.id.nav_rate_me:
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
        }
    }

    @Subscribe
    public void onEvent(final Message event) {
        if(event.tag() == MessageTag.OPEN) {
            TimeInformation lastOpened = ((Send<TimeInformation>) event).receive();
            if(lastOpened != null)
                openTimeInformation(lastOpened);
        } else if(event.tag() == MessageTag.REMOVE_LAST_FRAGMETN) {
            if(adapterViewPager.getCount() > DEFAULT_FRAGMENT_NUMBER)
                adapterViewPager.removeLastFragment();
        } else if(event.tag() == MessageTag.MAYBE_OPEN) {
            if(adapterViewPager.getCount() > DEFAULT_FRAGMENT_NUMBER) {
                TimeInformation lastOpened = ((Send<TimeInformation>) event).receive();
                if(lastOpened != null)
                    openTimeInformation(lastOpened);
                vpPager.setCurrentItem(DEFAULT_FRAGMENT_NUMBER - 1);
            }
        }
    }


    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar,
                R.string.drawer_open,  R.string.drawer_close);
    }


    public void openTimeInformation(final TimeInformation tinfo) {
        if(adapterViewPager.getCount() == DEFAULT_FRAGMENT_NUMBER) {
            adapterViewPager.addFragment();
        }
        bus.post(new Send<TimeInformation>() {
            public MessageTag tag() { return MessageTag.SHOW; }
            public TimeInformation receive() {
                return tinfo;
            }
        });
        vpPager.setCurrentItem(adapterViewPager.getCount() - 1);
    }


    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // Do not modify below here

    @Override
    public void onResume() {
        super.onResume();
        IO.loadConfigurations(switchCompat, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        IO.saveToFile(this, Configurations.CONFIGURATIONS_FILE_NAME, Configurations.dumpConfigurations());
    }



}


