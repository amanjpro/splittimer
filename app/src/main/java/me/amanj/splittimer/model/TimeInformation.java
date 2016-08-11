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

/**
 * Created by Amanj Sherwany on 8/4/16.
 */

import java.io.Serializable;
import java.sql.Array;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeInformation implements Cloneable, Serializable {
    private List<Long> laps;
    private String name;
    private long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
    private long storingTime;

    public TimeInformation() {
        this.laps = new ArrayList<>(50);
    }


    public TimeInformation(String name, long storingTime) {
        this.name = name;
        this.storingTime = storingTime;
        this.laps = new ArrayList<>(50);
    }


    public long getAverageTime() {
        return getElapsedTime() / laps.size();
    }

    public long getMin() { return min; }
    public long getMax() { return max; }

    public void addLap(long time) {
        if(min > time) min = time;
        if(max < time) max = time;
        laps.add(time);
    }

    public void setName(String name) { this.name = name; }
    public void setStoringTime(long storingTime) {
        this.storingTime = storingTime;
    }
    public void setLaps(List<Long> laps) { this.laps = laps; }

    public String getName() { return name; }
    public List<Long> getLaps() { return laps; }
    public long getStoringTime() { return storingTime; }

    public void mergeLaps(int fIndex, int sIndex) {
        int size = laps.size();
        if(fIndex >= 0 && fIndex == sIndex - 1 && sIndex < size) {
            long lap2 = laps.remove(sIndex);
            long lap1 = laps.get(fIndex);
            laps.set(fIndex, lap1 + lap2);
        }
    }


    public long lapAt(int i) { return laps.get(i); }

    public long elapsedTimeAt(int i) {
        long elapsed = 0l;
        for(int j = 0; j <= i; j++) elapsed += laps.get(j);
        return elapsed;
    }

    public void clearLaps() { laps.clear(); }

    public int countLaps() { return laps.size(); }

    public long getElapsedTime() {
        long elapsed = 0;
        for(Long lap: laps) elapsed += lap;
        return elapsed;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Name: ");
        builder.append(name);
        builder.append(", Saving time: ");
        builder.append(storingTime);
        builder.append(", ");
        builder.append(laps.toString());
        return builder.toString();
    }


    protected Object clone() {
        List<Long> laps = (List<Long>) ((ArrayList<Long>) this.laps).clone();
        TimeInformation res = new TimeInformation(this.name, this.storingTime);
        res.setLaps(laps);
        return res;
    }
}
