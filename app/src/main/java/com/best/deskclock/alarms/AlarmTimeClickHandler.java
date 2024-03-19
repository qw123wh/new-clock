/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.best.deskclock.alarms;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.appcompat.app.AppCompatActivity;

import com.best.deskclock.AlarmClockFragment;
import com.best.deskclock.LabelDialogFragment;
import com.best.deskclock.LogUtils;
import com.best.deskclock.R;
import com.best.deskclock.Utils;
import com.best.deskclock.alarms.dataadapter.AlarmItemHolder;
import com.best.deskclock.data.DataModel;
import com.best.deskclock.data.Weekdays;
import com.best.deskclock.events.Events;
import com.best.deskclock.provider.Alarm;
import com.best.deskclock.ringtone.RingtonePickerActivity;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;

/**
 * Click handler for an alarm time item.
 */
public final class AlarmTimeClickHandler {

    private static final String TAG = "AlarmTimeClickHandler";
    private static final LogUtils.Logger LOGGER = new LogUtils.Logger(TAG);
    private static final String KEY_PREVIOUS_DAY_MAP = "previousDayMap";
    private final Fragment mFragment;
    private final Context mContext;
    private final AlarmUpdateHandler mAlarmUpdateHandler;
    private final ScrollHandler mScrollHandler;
    private Alarm mSelectedAlarm;
    private Bundle mPreviousDaysOfWeekMap;

    public AlarmTimeClickHandler(Fragment fragment, Bundle savedState,
                                 AlarmUpdateHandler alarmUpdateHandler, ScrollHandler smoothScrollController) {

        mFragment = fragment;
        mContext = mFragment.getActivity().getApplicationContext();
        mAlarmUpdateHandler = alarmUpdateHandler;
        mScrollHandler = smoothScrollController;
        if (savedState != null) {
            mPreviousDaysOfWeekMap = savedState.getBundle(KEY_PREVIOUS_DAY_MAP);
        }
        if (mPreviousDaysOfWeekMap == null) {
            mPreviousDaysOfWeekMap = new Bundle();
        }
    }

    public void setSelectedAlarm(Alarm selectedAlarm) {
        mSelectedAlarm = selectedAlarm;
    }

    public void setAlarmEnabled(Alarm alarm, boolean newState) {
        if (newState != alarm.enabled) {
            alarm.enabled = newState;
            Events.sendAlarmEvent(newState ? R.string.action_enable : R.string.action_disable, R.string.label_deskclock);
            mAlarmUpdateHandler.asyncUpdateAlarm(alarm, alarm.enabled, false);
            Utils.vibrationTime(mContext, 50);
            LOGGER.d("Updating alarm enabled state to " + newState);
        }
    }

    public void setAlarmVibrationEnabled(Alarm alarm, boolean newState) {
        if (newState != alarm.vibrate) {
            alarm.vibrate = newState;
            Events.sendAlarmEvent(R.string.action_toggle_vibrate, R.string.label_deskclock);
            mAlarmUpdateHandler.asyncUpdateAlarm(alarm, false, true);
            LOGGER.d("Updating vibrate state to " + newState);

            if (newState) {
                // Buzz the vibrator to preview the alarm firing behavior.
                Utils.vibrationTime(mContext, 300);
            }
        }
    }

    public void setAlarmRepeatEnabled(Alarm alarm, boolean isEnabled) {
        final Calendar now = Calendar.getInstance();
        final Calendar oldNextAlarmTime = alarm.getNextAlarmTime(now);
        final String alarmId = String.valueOf(alarm.id);
        if (isEnabled) {
            // Set all previously set days
            // or
            // Set all days if no previous.
            final int bitSet = mPreviousDaysOfWeekMap.getInt(alarmId);
            alarm.daysOfWeek = Weekdays.fromBits(bitSet);
            if (!alarm.daysOfWeek.isRepeating()) {
                alarm.daysOfWeek = Weekdays.ALL;
            }
        } else {
            // Remember the set days in case the user wants it back.
            final int bitSet = alarm.daysOfWeek.getBits();
            mPreviousDaysOfWeekMap.putInt(alarmId, bitSet);

            // Remove all repeat days
            alarm.daysOfWeek = Weekdays.NONE;
        }

        // if the change altered the next scheduled alarm time, tell the user
        final Calendar newNextAlarmTime = alarm.getNextAlarmTime(now);
        final boolean popupToast = !oldNextAlarmTime.equals(newNextAlarmTime);

        Events.sendAlarmEvent(R.string.action_toggle_repeat_days, R.string.label_deskclock);
        mAlarmUpdateHandler.asyncUpdateAlarm(alarm, popupToast, false);
    }

    public void setDayOfWeekEnabled(Alarm alarm, boolean checked, int index) {
        final Calendar now = Calendar.getInstance();
        final Calendar oldNextAlarmTime = alarm.getNextAlarmTime(now);

        final int weekday = DataModel.getDataModel().getWeekdayOrder().getCalendarDays().get(index);
        alarm.daysOfWeek = alarm.daysOfWeek.setBit(weekday, checked);

        // if the change altered the next scheduled alarm time, tell the user
        final Calendar newNextAlarmTime = alarm.getNextAlarmTime(now);
        final boolean popupToast = !oldNextAlarmTime.equals(newNextAlarmTime);
        mAlarmUpdateHandler.asyncUpdateAlarm(alarm, popupToast, false);

        Utils.vibrationTime(mContext, 10);
    }

    public void onDeleteClicked(AlarmItemHolder itemHolder) {
        if (mFragment instanceof AlarmClockFragment) {
            ((AlarmClockFragment) mFragment).removeItem(itemHolder);
        }
        final Alarm alarm = itemHolder.item;
        Events.sendAlarmEvent(R.string.action_delete, R.string.label_deskclock);
        mAlarmUpdateHandler.asyncDeleteAlarm(alarm);
        LOGGER.d("Deleting alarm.");
    }

    public void onClockClicked(Alarm alarm) {
        mSelectedAlarm = alarm;
        Events.sendAlarmEvent(R.string.action_set_time, R.string.label_deskclock);
        ShowMaterialTimePicker(alarm.hour, alarm.minutes);
    }

    private void ShowMaterialTimePicker(int hour, int minute) {

        @TimeFormat int clockFormat;
        boolean isSystem24Hour = DateFormat.is24HourFormat(mFragment.getContext());
        clockFormat = isSystem24Hour ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H;

        MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setHour(hour)
                .setMinute(minute)
                .build();
        Context context = mFragment.getContext();
        materialTimePicker.show(((AppCompatActivity) context).getSupportFragmentManager(), TAG);

        materialTimePicker.addOnPositiveButtonClickListener(dialog -> {
            int newHour = materialTimePicker.getHour();
            int newMinute = materialTimePicker.getMinute();
            onTimeSet(newHour, newMinute);
        });
    }

    public void onRingtoneClicked(Context context, Alarm alarm) {
        mSelectedAlarm = alarm;
        Events.sendAlarmEvent(R.string.action_set_ringtone, R.string.label_deskclock);

        final Intent intent = RingtonePickerActivity.createAlarmRingtonePickerIntent(context, alarm);
        context.startActivity(intent);
    }

    public void onEditLabelClicked(Alarm alarm) {
        Events.sendAlarmEvent(R.string.action_set_label, R.string.label_deskclock);
        final LabelDialogFragment fragment = LabelDialogFragment.newInstance(alarm, alarm.label, mFragment.getTag());
        LabelDialogFragment.show(mFragment.getFragmentManager(), fragment);
    }

    public void onTimeSet(int hourOfDay, int minute) {
        if (mSelectedAlarm == null) {
            // If mSelectedAlarm is null then we're creating a new alarm.
            final Alarm alarm = new Alarm();
            alarm.hour = hourOfDay;
            alarm.minutes = minute;
            alarm.enabled = true;
            alarm.vibrate = false;
            mAlarmUpdateHandler.asyncAddAlarm(alarm);
        } else {
            mSelectedAlarm.hour = hourOfDay;
            mSelectedAlarm.minutes = minute;
            mSelectedAlarm.enabled = true;
            mScrollHandler.setSmoothScrollStableId(mSelectedAlarm.id);
            mAlarmUpdateHandler.asyncUpdateAlarm(mSelectedAlarm, true, false);
            mSelectedAlarm = null;
        }
    }
}
