package online.umassdartmouthsustainability.theshowerapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import online.umassdartmouthsustainability.theshowerapp.managers.UserMenuManager;


public class UserMenuReceiver extends BroadcastReceiver {
    UserMenuFragment userMenu;
    String tag = "theShowerApp.broadcastReceiver";

    public UserMenuReceiver(){}

    UserMenuReceiver(UserMenuFragment u) {
        this.userMenu = u;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(tag, "broadcast received");
        String d = intent.getStringExtra("data");
        UserMenuManager.getInstance().setData(d);
        userMenu.parseData(d);
    }
}