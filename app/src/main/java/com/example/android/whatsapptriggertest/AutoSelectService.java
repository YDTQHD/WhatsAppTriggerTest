package com.example.android.whatsapptriggertest;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.github.privacystreams.accessibility.AccEvent;
import com.github.privacystreams.core.Callback;
import com.github.privacystreams.core.Item;
import com.github.privacystreams.core.UQI;
import com.github.privacystreams.core.purposes.Purpose;
import com.github.privacystreams.utils.AppUtils;

import java.util.List;


public class AutoSelectService extends Service {

    UQI uqi;
    String[] contactNames;
    Boolean clicked;
    private static final String TERM_ARRIVED_HOME = "Hey, I just arrived my house!";
    private static final String CONTACT_RESOURCE_ID = "contactpicker_row_name";

    private NodeInfoListener nodeInfoListener;

    public void setNodeInfoListener(NodeInfoListener nodeInfoListener) {
        this.nodeInfoListener = nodeInfoListener;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.

        uqi = new UQI(this);
        clicked = false;
        contactNames = intent.getStringArrayExtra("contactNames");

        setNodeInfoListener(new NodeInfoListener() {

            public void asOutput(AccessibilityNodeInfo selectingView) {

                if (!clicked) {

                    Log.e("List Size", Integer.toString(contactNames.length));

                    for (String name : contactNames) {

                        List<AccessibilityNodeInfo> matchedList = selectingView.findAccessibilityNodeInfosByText(name);

                        if (!matchedList.isEmpty()) {

                            AccessibilityNodeInfo ro = matchedList.get(0);
                            do {
                                ro = ro.getParent();
                                if (ro.isClickable()) {
                                    ro.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    Log.e(name, "Clicked");
                                    clicked = true;
                                }
                            } while (!ro.isClickable());
                        } else {
                            Log.e("Warning", "No matched");
                        }
                    }
                } else {
                    Log.e("Warning", "Already Clicked");
                }
                stopSelf();
            }
        });

        uqi.getData(AccEvent.asUpdates(), Purpose.FEATURE("base event"))
                .forEach(new Callback<Item>() {

                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                    @Override
                    protected void onInput(Item item) {
                        AccessibilityNodeInfo root = item.getValueByField(AccEvent.ROOT_NODE);
                        if (root.getPackageName().equals(AppUtils.APP_PACKAGE_WHATSAPP)
                                && ((int) item.getValueByField(AccEvent.EVENT_TYPE) == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {
                            nodeInfoListener.asOutput(root);
                            Log.e("UQI Thread", "Transferred");
                        }
                    }
                });

        Intent whatsAppIntent = new Intent();
        whatsAppIntent.setAction(Intent.ACTION_SEND);
        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, TERM_ARRIVED_HOME);
        whatsAppIntent.setType("text/plain");
        whatsAppIntent.setPackage(AppUtils.APP_PACKAGE_WHATSAPP);
        startActivity(whatsAppIntent);

        /*
        if (selectingView != null) {

            Log.e("Finallllllly!", "s");

            for (String name : contactNames) {

                Log.e(name, "Searching " + name);

                List<AccessibilityNodeInfo> matchedList = selectingView.findAccessibilityNodeInfosByText(name);
                Log.e("GAN", matchedList.toString());

                if (!matchedList.isEmpty()) {

                    AccessibilityNodeInfo ro = matchedList.get(0);
                    do {
                        ro = ro.getParent();
                        if (ro.isClickable()) {
                            ro.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Log.e(name, "Clicked");
                            stopSelf(startId);
                        }
                    } while (!ro.isClickable());
                } else {
                    Log.e("Warning", "No matched");
                }
                Log.e(name, "Searching Done");
            }
        }
        */
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e("stopped", "stopped");
        uqi.stopAll();
        clicked = false;
        super.onDestroy();
    }
}