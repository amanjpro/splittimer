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


import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by Amanj Sherwany on 8/4/16.
 */

public class Timestamp implements Serializable {
    private long startMillis, stopMillis, lastLap;
    private boolean isRunning, hasLapped;
    // don't serialize this
    transient private static SimpleDateFormat formatter =
            new SimpleDateFormat(Configurations.getCurrentFormat());

    public Timestamp() {
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }


    public void start() {
        startMillis = System.currentTimeMillis();
        hasLapped = false;
        lastLap = 0;
        isRunning = true;
    }

    public long stop() {
        stopMillis = System.currentTimeMillis();
        isRunning = false;
        long toReturn = !hasLapped?
            stopMillis - startMillis:
            stopMillis - lastLap;
        return toReturn;
    }

    public long getLapTime() {
        long end = isRunning? System.currentTimeMillis() : stopMillis;
        if(!hasLapped) {
            return end - startMillis;
        }
        return end - lastLap;
    }
    public long getElapsedTime() {
        if(isRunning) {
            return System.currentTimeMillis() - startMillis;
        }
        return stopMillis - startMillis;
    }

    public long lap() {
        if(isRunning) {
            return lap(System.currentTimeMillis());
        } else {
            return lap(stopMillis);
        }
    }

    private long lap(long stop) {
        if(!hasLapped) {
            long toReturn = stop - startMillis;
            lastLap = stop;
            hasLapped = true;
            return toReturn;
        } else {
            long toReturn = stop - lastLap;
            lastLap = stop;
            return toReturn;
        }
    }



    public static void updateFormatter(String newFormat) {
        formatter = new SimpleDateFormat(newFormat);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static String timeStampToString(long timestamp) {
        return formatter.format(timestamp);
    }


}
