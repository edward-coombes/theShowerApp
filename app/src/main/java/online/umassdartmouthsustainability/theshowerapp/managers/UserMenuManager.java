package online.umassdartmouthsustainability.theshowerapp.managers;

import android.util.Log;


public class UserMenuManager {
    private static final String tag = "theShowerApp.UsrMenuMgr";

    private static UserMenuManager manager;


    private String json;


    public static UserMenuManager getInstance() {
        if (manager == null) {
            manager = new UserMenuManager();
        }
        return manager;
    }

    public void setData(String json) {
        manager.json = json;
    }

    public String getData() {
        return manager.json;
    }


}
