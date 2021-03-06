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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amanj Sherwany on 8/6/16.
 */

public class Configurations {
    public static final String[] precisions =
            {"HH:mm:ss", "HH:mm:ss.S", "mm:ss.SS", "mm:ss.SSS"};


    public static final int PRECISION_LEVEL_ONE = 0;
    public static final int PRECISION_LEVEL_TWO = 1;
    public static final int PRECISION_LEVEL_THREE = 2;
    public static final int PRECISION_LEVEL_FOUR = 3;
    private static int CURRENT_FORMAT = PRECISION_LEVEL_FOUR;

    public static String getCurrentFormat() { return precisions[CURRENT_FORMAT]; }

    public static int getCurrentPrecisionLevel() { return CURRENT_FORMAT; }

    public static void updateCurrentFormat(int format) {
        CURRENT_FORMAT = format;
    }


    private static boolean LAP_ON_STOP = true;
    private static boolean SCREEN_ORIENTATION_ACTIVATED = false;
    private static boolean VOLUME_KEY_CONTROLLER_ACTIVATED = false;

    public static boolean shouldLapOnStop() { return LAP_ON_STOP; }
    public static void setLapOnStop(boolean option) { LAP_ON_STOP = option;}

    public static boolean isScreenRotationActivated() { return SCREEN_ORIENTATION_ACTIVATED; }
    public static void activateScreenRotation(boolean option) { SCREEN_ORIENTATION_ACTIVATED = option;}

    public static boolean isVolumeKeyControlerActivated() { return VOLUME_KEY_CONTROLLER_ACTIVATED; }
    public static void activateVolumeKeyControler(boolean option) { VOLUME_KEY_CONTROLLER_ACTIVATED = option; }

    public static final String APP_FORK_URL = "http://www.github.com/amanjpro/splittimer";
    public static final String CONFIGURATIONS_FILE_NAME = "config";
    public static final String ENTRIES_FILE_NAME = "data";



    public static String CURRENT_FORMAT_CONFIG_NAME = "current_format";
    public static String LAP_ON_STOP_CONFIG_NAME = "lap_on_stop";
    public static String SCREEN_ORIENTATION_ACTIVATED_CONFIG_NAME = "screen_orientation_activated";
    public static String VOLUME_KEY_CONTROLLER_ACTIVATED_CONFIG_NAME = "volume_key_controller_activated";

    public static Map<String, String> dumpConfigurations() {
        Map<String, String> configurations = new HashMap<>();
        configurations.put(CURRENT_FORMAT_CONFIG_NAME, "" + CURRENT_FORMAT);
        configurations.put(LAP_ON_STOP_CONFIG_NAME, "" + LAP_ON_STOP);
        configurations.put(SCREEN_ORIENTATION_ACTIVATED_CONFIG_NAME, "" + SCREEN_ORIENTATION_ACTIVATED);
        configurations.put(VOLUME_KEY_CONTROLLER_ACTIVATED_CONFIG_NAME, "" + VOLUME_KEY_CONTROLLER_ACTIVATED);
        return  configurations;
    }
}
