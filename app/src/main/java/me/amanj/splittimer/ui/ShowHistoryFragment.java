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


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import me.amanj.splittimer.R;
import me.amanj.splittimer.messages.Message;
import me.amanj.splittimer.messages.MessageTag;
import me.amanj.splittimer.messages.Send;
import me.amanj.splittimer.util.TimeInformation;

/**
 * Created by Amanj Sherwany on 8/14/16.
 */
public class ShowHistoryFragment extends Fragment {
    EventBus bus = EventBus.getDefault();

    private static final String TAG = ShowTimeInformationFragment.class.getCanonicalName();
    private SplitTimerListFragment splitTimerListFragment;
    private ShowTimeInformationFragment showTimeInformationFragment;
    private View rootView;
    private Menu menu;
    private MenuItem showStatistics, clearHistory;
    private FragmentManager mFragmentManager;
    private FrameLayout mItemsFrameLayout, mDetailFrameLayout;
    private static final int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;


    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public static ShowHistoryFragment newInstance() {
        ShowHistoryFragment fragment = new ShowHistoryFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_show_history, container, false);

        mFragmentManager = getActivity().getSupportFragmentManager();

        splitTimerListFragment = (SplitTimerListFragment)
                mFragmentManager.findFragmentByTag(SplitTimerListFragment.TAG);
        showTimeInformationFragment = (ShowTimeInformationFragment)
                mFragmentManager.findFragmentByTag(ShowTimeInformationFragment.TAG);


        if(splitTimerListFragment == null) {
            Log.d(TAG, "New instance of SplitTimerListFragment");
            splitTimerListFragment = SplitTimerListFragment.newInstance();
        }

        if(showTimeInformationFragment == null) {
            showTimeInformationFragment = ShowTimeInformationFragment.newInstance();
        }


        boolean inTwoPanesMode = inTwoPanesMode();

        if(inTwoPanesMode) {
            mItemsFrameLayout = (FrameLayout) rootView.findViewById(R.id.list_frag);
            mDetailFrameLayout = (FrameLayout) rootView.findViewById(R.id.detail_frag);

            // Add a OnBackStackChangedListener to reset the layout when the back stack changes
            mFragmentManager.addOnBackStackChangedListener (
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        showStatistics.setVisible(showTimeInformationFragment.isAdded());
                        setLayout();
                    }
                });
        } else {
            mFragmentManager.addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        if(!splitTimerListFragment.isHidden()) {
                            splitTimerListFragment.unsetLastOpen();
                        }
                        clearHistory.setVisible(!clearHistory.isVisible());
                        showStatistics.setVisible(!showStatistics.isVisible());
                    }
                });
        }


        if(savedInstanceState != null) {
            if(inTwoPanesMode) {
                setLayout();
            }
            return rootView;
        }


        if(!inTwoPanesMode) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.add(R.id.fragment_container,
                    splitTimerListFragment, SplitTimerListFragment.TAG);
            transaction.commit();
        } else {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.list_frag, splitTimerListFragment,
                    SplitTimerListFragment.TAG);
            fragmentTransaction.commit();
        }

        setHasOptionsMenu(true);


        return rootView;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_history_menu_item:
                ClearHistoryConfirmationDialog clearHistoryDialog =
                        new ClearHistoryConfirmationDialog();
                clearHistoryDialog.show(getActivity().getSupportFragmentManager(),
                        ClearHistoryConfirmationDialog.TAG);
                return true;
            case R.id.show_statics_history_menu_item:
                bus.post(new Message() {
                    public MessageTag tag() { return MessageTag.SHOW_STATISTICS; }
                });
            default:
                return false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.show_history_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;
        showStatistics = menu.findItem(R.id.show_statics_history_menu_item);
        clearHistory   = menu.findItem(R.id.clear_history_menu_item);
        showStatistics.setVisible(showTimeInformationFragment.isAdded() ||
                                    splitTimerListFragment.isHidden());
        clearHistory.setVisible(!splitTimerListFragment.isHidden());
    }


    @Subscribe
    public void onEvent(Message event) {
        if (event.tag() == MessageTag.OPEN) {
            TimeInformation lastOpened = ((Send<TimeInformation>) event).receive();

            if (!inTwoPanesMode()) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.hide(splitTimerListFragment);
                transaction.add(R.id.fragment_container,
                        showTimeInformationFragment, ShowTimeInformationFragment.TAG);
                transaction.addToBackStack(null);
                transaction.commit();
                mFragmentManager.executePendingTransactions();
            } else if (inTwoPanesMode() && !showTimeInformationFragment.isAdded()) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.add(R.id.detail_frag,
                        showTimeInformationFragment, ShowTimeInformationFragment.TAG);
                transaction.addToBackStack(null);
                transaction.commit();
                mFragmentManager.executePendingTransactions();
            }
            openTimeInformation(lastOpened);
        } else if (event.tag() == MessageTag.REMOVE_DETAILED_PANE) {
            if(inTwoPanesMode()) {
                mFragmentManager.beginTransaction().remove(showTimeInformationFragment).commit();
                mFragmentManager.executePendingTransactions();
            }
        } else if(event.tag() == MessageTag.MAYBE_OPEN) {
            if(inTwoPanesMode() && showTimeInformationFragment.isAdded()) {
                TimeInformation lastOpened = ((Send<TimeInformation>) event).receive();
                if(lastOpened != null)
                    openTimeInformation(lastOpened);
            }
        }
    }


    private void setLayout() {
        if (!showTimeInformationFragment.isAdded()) {
            mDetailFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
                    MATCH_PARENT));
            mItemsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    MATCH_PARENT, MATCH_PARENT));
        } else {
            mItemsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
                    MATCH_PARENT, 1f));
            mDetailFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
                    MATCH_PARENT, 2f));
        }
    }

    public void openTimeInformation(final TimeInformation tinfo) {
        bus.post(new Send<TimeInformation>() {
            public MessageTag tag() { return MessageTag.SHOW; }
            public TimeInformation receive() {
                return tinfo;
            }
        });
    }

    private boolean inTwoPanesMode() {
        return rootView.findViewById(R.id.fragment_container) == null;
    }


}
