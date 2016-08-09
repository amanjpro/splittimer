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

public class CSVSupport {
    private static final String coma = "&coma;";
    public static String toCSV(TimeInformation tinfo) {
        StringBuilder builder = new StringBuilder();
        builder.append(csvify(tinfo.getName()));
        builder.append(",");
        builder.append(tinfo.getStoringTime());
        for(Long lap: tinfo.getLaps()) {
            builder.append(",");
            builder.append(lap);
        }
        return builder.toString();
    }

    public static TimeInformation fromCSV(String csv) {
        String[] parts = csv.split(",");
        if(parts.length < 2) return null;

        String name = decsvify(parts[0]);
        long storingTime = Long.parseLong(parts[2]);
        TimeInformation tinfo = new TimeInformation(name, storingTime);
        for(int i = 2; i < parts.length; i++)
            tinfo.addLap(Long.parseLong(parts[i]));
        return tinfo;
    }


    private static String decsvify(String name) {
        return name.replaceAll(coma, ",");
    }
    private static String csvify(String name) {
        return name.replaceAll(",", coma);
    }
}
