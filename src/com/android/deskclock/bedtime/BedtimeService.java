///*
// * Copyright (C) 2015 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.best.deskclock.bedtime;
//
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.IBinder;
//
//import com.best.deskclock.DeskClock;
//import com.best.deskclock.R;
//import com.best.deskclock.data.DataModel;
//import com.best.deskclock.data.Timer;
//import com.best.deskclock.events.Events;
//import com.best.deskclock.uidata.UiDataModel;
//
//import static com.best.deskclock.uidata.UiDataModel.Tab.TIMERS;
//
//import androidx.annotation.Nullable;
//
//
//public final class BedtimeService extends Service {
//
//    private static final String ACTION_PREFIX = "com.android.deskclock.action.";
//
//    // shows the tab with the bedtime
//    public static final String ACTION_SHOW_BEDTIME = ACTION_PREFIX + "SHOW_BEDTIME";
//
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        final String action = intent.getAction();
//        final int label = intent.getIntExtra(Events.EXTRA_EVENT_LABEL, R.string.label_intent);
//        switch (action) {
//            case ACTION_SHOW_BEDTIME: {
//                Events.sendStopwatchEvent(R.string.action_show, label);
//
//                // Open DeskClock positioned on the stopwatch tab.
//                UiDataModel.getUiDataModel().setSelectedTab(BEDTIME);
//                final Intent showBedtime = new Intent(this, DeskClock.class)
//                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(showBedtime);
//                break;
//            }
//        }
//        return START_NOT_STICKY;
//
//    }
//
//} TODO