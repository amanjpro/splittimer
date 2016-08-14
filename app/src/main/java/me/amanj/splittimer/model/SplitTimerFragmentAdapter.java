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

package me.amanj.splittimer.model;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import me.amanj.splittimer.R;
import me.amanj.splittimer.ui.ShowHistoryFragment;
import me.amanj.splittimer.ui.ShowTimeInformationFragment;
import me.amanj.splittimer.ui.SplitTimerListFragment;
import me.amanj.splittimer.ui.TimerFragment;

/**
 * Created by Amanj Sherwany on 8/6/16.
 */
public class SplitTimerFragmentAdapter extends FragmentStatePagerAdapter {

    private final Context mContext;
    private int FRAGMENT_COUNT;

    public SplitTimerFragmentAdapter(FragmentManager fm, Context mContext, int count) {
        super(fm);
        this.mContext = mContext;
        this.FRAGMENT_COUNT = count;
    }

    @Override
    public int getCount() {
        return FRAGMENT_COUNT;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return TimerFragment.newInstance();
            case 1:
                return ShowHistoryFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.app_name);
            case 1:
                return mContext.getString(R.string.saved_list);
            default:
                return null;
        }
    }




}
