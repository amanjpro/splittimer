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

package me.amanj.splittimer.control;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import me.amanj.splittimer.R;
import me.amanj.splittimer.model.CSVSupport;
import me.amanj.splittimer.model.Configurations;
import me.amanj.splittimer.model.TimeInformation;


/**
 * Created by Amanj Sherwany on 8/7/16.
 */

public class IO {

    private final static String TAG = IO.class.getCanonicalName();
    public static void loadConfigurations(SwitchCompat switchCompat, Context context) {
        BufferedReader reader = null;
        Log.i(TAG, "Loading configurations");
        try {
            FileInputStream fis = context.openFileInput(Configurations.CONFIGURATIONS_FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(fis));

            String line;

            if (null != (line = reader.readLine())) {
                Configurations.updateCurrentFormat(Integer.parseInt(line));
                Log.i(TAG, "Precision level is loaded as: " + line);
            }

            if (null != (line = reader.readLine())) {
                Configurations.setLapOnStop(Boolean.parseBoolean(line));
                switchCompat.setChecked(Configurations.shouldLapOnStop());
                Log.i(TAG, "Should lap on stop is loaded as: " + line);
            }

        } catch (IOException e) {
            Toast.makeText(context, R.string.bad_configuration_file, Toast.LENGTH_SHORT).show();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.i(TAG, "Couldn't close reader");
                    Log.i(TAG, "Error in loading configurations");
                }
            }
        }
    }

    public static List<TimeInformation> loadEntries(Context context) {
        BufferedReader reader = null;
        List<TimeInformation> list = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput(Configurations.ENTRIES_FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(fis));

            String line;

            while (null != (line = reader.readLine())) {
                TimeInformation tinfo = CSVSupport.fromCSV(line);
                if(tinfo != null) {
                    list.add(tinfo);
                }
            }

        } catch (IOException e) {
            Toast.makeText(context, R.string.bad_entry_file, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Error in loading entries");
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.i(TAG, "Couldn't close reader");
                }
            }
        }
        Log.i(TAG, "Loading entries: " + list.size());
        return list;
    }

    public static void saveToFile(Context context, String fileName, String content) {
        Log.i(TAG, "Saving content to file: " + fileName);
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            PrintWriter pw = new PrintWriter(fos);
            pw.print(content);
            pw.close();
            fos.close();
        } catch (IOException e) {
            Log.i(TAG, "Bug in saving " + fileName);
        }
    }
}
