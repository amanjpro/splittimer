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
import android.view.LayoutInflater;
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

    private SplitTimerListFragment splitTimerListFragment;
    private ShowTimeInformationFragment showTimeInformationFragment;
    private View rootView;
    private FragmentManager mFragmentManager;
    private FrameLayout mItemsFrameLayout, mDetailFrameLayout;
    private static final int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }


    public static ShowHistoryFragment newInstance() {
        ShowHistoryFragment fragment = new ShowHistoryFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_show_history, container, false);

        // Get a reference to the FragmentManager
        mFragmentManager = getActivity().getSupportFragmentManager();
        splitTimerListFragment = SplitTimerListFragment.newInstance();
        showTimeInformationFragment = ShowTimeInformationFragment.newInstance();

        if(!inTwoPanesMode()) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.add(R.id.fragment_container,
                    splitTimerListFragment);
            transaction.commit();
        } else {
            mItemsFrameLayout = (FrameLayout) rootView.findViewById(R.id.list_frag);
            mDetailFrameLayout = (FrameLayout) rootView.findViewById(R.id.detail_frag);

            // Start a new FragmentTransaction
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

            // Add the TitleFragment to the layout
            fragmentTransaction.add(R.id.list_frag, splitTimerListFragment);

            // Commit the FragmentTransaction
            fragmentTransaction.commit();

            // Add a OnBackStackChangedListener to reset the layout when the back stack changes
            mFragmentManager.addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        setLayout();
                    }
                });
        }


        return rootView;
    }


    @Subscribe
    public void onEvent(Message event) {
        if(event.tag() == MessageTag.OPEN) {
            TimeInformation lastOpened = ((Send<TimeInformation>) event).receive();

            if(showTimeInformationFragment == null)
                showTimeInformationFragment = ShowTimeInformationFragment.newInstance();

            if(!inTwoPanesMode()) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container,
                        showTimeInformationFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                fm.executePendingTransactions();
            } else if (!showTimeInformationFragment.isAdded()) {

                // Start a new FragmentTransaction
                FragmentTransaction fragmentTransaction = mFragmentManager
                        .beginTransaction();

                fragmentTransaction.add(R.id.detail_frag,
                        showTimeInformationFragment);

                // Add this FragmentTransaction to the backstack
                fragmentTransaction.addToBackStack(null);

                // Commit the FragmentTransaction
                fragmentTransaction.commit();

                // Force Android to execute the committed FragmentTransaction
                mFragmentManager.executePendingTransactions();
            }
            if(lastOpened != null) {
                openTimeInformation(lastOpened);
            }
        } else if(event.tag() == MessageTag.MAYBE_OPEN) {
            TimeInformation lastOpened = ((Send<TimeInformation>) event).receive();
            if(lastOpened != null)
                openTimeInformation(lastOpened);
        }
    }


    private void setLayout() {

        if (!showTimeInformationFragment.isAdded()) {

            // Make the TitleFragment occupy the entire layout
            mItemsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    MATCH_PARENT, MATCH_PARENT));
            mDetailFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
                    MATCH_PARENT));
        } else {

            // Make the TitleLayout take 1/3 of the layout's width
            mItemsFrameLayout.setLayoutParams(new LinearLayout.LayoutParams(0,
                    MATCH_PARENT, 1f));

            // Make the QuoteLayout take 2/3's of the layout's width
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
