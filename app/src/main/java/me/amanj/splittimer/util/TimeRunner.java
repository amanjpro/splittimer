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

package me.amanj.splittimer.util;

import android.os.AsyncTask;
import android.widget.TextView;

/**
 * Created by Amanj Sherwany on 8/4/16.
 */


public class TimeRunner extends AsyncTask<Timestamp, String, String> {
    private TextView lapDisplay, totalDisplay;

    public TimeRunner(TextView lapDisplay, TextView totalDisplay) {
        this.totalDisplay = totalDisplay;
        this.lapDisplay = lapDisplay;
    }

//    public void updateDisplays(TextView lapDisplay, TextView totalDisplay) {
//        this.lapDisplay = lapDisplay;
//        this.totalDisplay = totalDisplay;
//    }

    @Override
    protected String doInBackground(Timestamp... params) {
        while(true) {
            publishProgress(Timestamp.timeStampToString(params[0].getLapTime()),
                    Timestamp.timeStampToString(params[0].getElapsedTime()));
            if(isCancelled()) break;
            try {
                Thread.sleep(100);
            } catch(InterruptedException iex) {}
        }
        return Timestamp.timeStampToString(params[1].getElapsedTime());
    }

    @Override
    public void onProgressUpdate(String... values) {
        lapDisplay.setText(values[0]);
        totalDisplay.setText(values[1]);
    }
}

