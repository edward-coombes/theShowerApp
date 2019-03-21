package online.umassdartmouthsustainability.theshowerapp;

import com.spotify.android.appremote.api.SpotifyAppRemote;
import kaaes.spotify.webapi.android.SpotifyService;

public class MusicManager {
    private static String tag = "theShowerApp.MMgr";
    private static MusicManager manager = null;

    private boolean playing = false;
    private SpotifyAppRemote remote;
    private SpotifyService service;
    private String authCode;

    //mutators

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void setRemote(SpotifyAppRemote remote) {
        this.remote = remote;
    }

    public void setAuthCode(String authCode) { this.authCode = authCode; }

    public void setService(SpotifyService service) {
        this.service = service;
    }


    //Accessors

    public boolean isPlaying() {
        return playing;
    }

    public SpotifyAppRemote getRemote() { return remote; }

    public static MusicManager getManager() {
        if(MusicManager.manager == null){
            MusicManager.manager = new MusicManager();
        }
        return manager;
    }

    public SpotifyService getService() {
        return service;
    }

    public String getAuthCode() {
        return authCode;
    }

}
