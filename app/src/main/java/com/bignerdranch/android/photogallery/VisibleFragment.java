package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by rbanks on 11/7/16.
 */

public class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";
    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        //adding the permission to the receiver indicates that this app and only this app can trigger it
        getActivity().registerReceiver(m_onShowNotification, filter,
                PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(m_onShowNotification);
    }

    private BroadcastReceiver m_onShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If we receive this, we're visible, so cancel the notifications
            Log.i(TAG, "canceling notifications");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}
