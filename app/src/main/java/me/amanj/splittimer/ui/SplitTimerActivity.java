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

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;


import me.amanj.splittimer.BuildConfig;
import me.amanj.splittimer.R;
import me.amanj.splittimer.messages.Send;
import me.amanj.splittimer.util.IO;
import me.amanj.splittimer.messages.MessageTag;
import me.amanj.splittimer.util.Configurations;
import me.amanj.splittimer.model.SplitTimerFragmentAdapter;

public class SplitTimerActivity extends AppCompatActivity {

    private final int DEFAULT_FRAGMENT_NUMBER = 2;
    private static final EventBus bus = EventBus.getDefault();
    private TabLayout tabLayout;
    private SplitTimerFragmentAdapter adapterViewPager;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private ViewPager vpPager;
    private SwitchCompat lapOnStopSwitch, screenOrientationSwitch, volumeKeySwitch;
    private final static String TAG = SplitTimerActivity.class.getCanonicalName();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putBoolean("stored", adapterViewPager.getCount() > DEFAULT_FRAGMENT_NUMBER);
//        outState.putBoolean("shouldGo", vpPager.getCurrentItem() == DEFAULT_FRAGMENT_NUMBER);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        if(savedInstanceState != null && savedInstanceState.getBoolean("stored")) {
//            adapterViewPager.addFragment();
//            if (savedInstanceState.getBoolean("shouldGo")) {
//                vpPager.setCurrentItem(DEFAULT_FRAGMENT_NUMBER);
//            }
//        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split_timer);
        vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new SplitTimerFragmentAdapter(getSupportFragmentManager(),
                getApplicationContext(), DEFAULT_FRAGMENT_NUMBER);
        vpPager.setAdapter(adapterViewPager);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        vpPager = (ViewPager) findViewById(R.id.vpPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(vpPager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();


        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        mDrawer.addDrawerListener(drawerToggle);

        ((TextView) nvDrawer.getHeaderView(0).findViewById(R.id.nav_version_label)).setText(
                getString(R.string.version_number, BuildConfig.VERSION_NAME));



        final MenuItem item = nvDrawer.getMenu().findItem(R.id.nav_lap_on_stop_switch);
        lapOnStopSwitch = (SwitchCompat) MenuItemCompat.getActionView(item);
        lapOnStopSwitch.setChecked(Configurations.shouldLapOnStop());
        lapOnStopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Configurations.setLapOnStop(isChecked);
                Log.i(TAG, "LapOnStop configuration set to: " + isChecked);
            }
        });
        MenuItemCompat.setActionView(item, lapOnStopSwitch);

        final MenuItem item2 = nvDrawer.getMenu().findItem(R.id.nav_screen_orientation_switch);
        screenOrientationSwitch = (SwitchCompat) MenuItemCompat.getActionView(item2);
        screenOrientationSwitch.setChecked(Configurations.isScreenRotationActivated());
        screenOrientationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Configurations.activateScreenRotation(isChecked);
                setScreenOrientationSensor();
                Log.i(TAG, "ScreenOrientation configuration set to: " + isChecked);
            }
        });
        MenuItemCompat.setActionView(item2, screenOrientationSwitch);
        setScreenOrientationSensor();


        final MenuItem item3 = nvDrawer.getMenu().findItem(R.id.nav_volume_key_switch);
        volumeKeySwitch = (SwitchCompat) MenuItemCompat.getActionView(item3);
        volumeKeySwitch.setChecked(Configurations.isVolumeKeyControlerActivated());
        volumeKeySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Configurations.activateVolumeKeyControler(isChecked);
            }
        });
        MenuItemCompat.setActionView(item3, volumeKeySwitch);
    }

    private void setScreenOrientationSensor() {
        if(Configurations.isScreenRotationActivated()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }
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
                        PrecisionSettingDialog.TAG);
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

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar,
                R.string.drawer_open,  R.string.drawer_close);
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
        IO.loadConfigurations(this);
        screenOrientationSwitch.setChecked(Configurations.isScreenRotationActivated());
        volumeKeySwitch.setChecked(Configurations.isVolumeKeyControlerActivated());
        lapOnStopSwitch.setChecked(Configurations.shouldLapOnStop());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(Configurations.isVolumeKeyControlerActivated()) {
            int keyCode = event.getKeyCode();
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        Log.d(TAG, "Volume key down");
                        bus.post(new Send<Void>() {
                            public MessageTag tag() {
                                return MessageTag.LAP;
                            }

                            public Void receive() {
                                return null;
                            }
                        });
                    }
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onPause() {
        super.onPause();
        IO.saveToFile(this, Configurations.CONFIGURATIONS_FILE_NAME, Configurations.dumpConfigurations());
    }



}


