package online.umassdartmouthsustainability.theshowerapp;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.Track;

//this class is not completed, but it should simplify record keeping/transfering of data between activities/fragments
public class MusicManager {
    private static MusicManager manager = null;

    private boolean playing = false;
    private SpotifyAppRemote remote;
    private SpotifyService service;
    private String authCode;
    private ImageView albumArt;
    private TextView title;

    private PlaylistSimple currentPlaylist;

    //mutators

    public void setCurrentPlaylist(PlaylistSimple currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void setRemote(SpotifyAppRemote remote) {
        this.remote = remote;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public void setService(SpotifyService service) {
        this.service = service;
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setTitle(TextView title){
        this.title = title;
    }

    public void setAlbumArt(Drawable art) {
        this.albumArt.setImageDrawable(art);
    }

    public void setAlbumArt(ImageView albumArt){
        this.albumArt = albumArt;
    }



    //Accessors

    public boolean isPlaying() {
        return playing;
    }

    public PlaylistSimple getCurrentPlaylist(){
        return currentPlaylist;
    }

    public SpotifyAppRemote getRemote() {
        return remote;
    }

    public static MusicManager getManager() {
        if(MusicManager.manager == null){
            MusicManager.manager = new MusicManager();
        }
        return manager;
    }

    public SpotifyService getService() {
        return service;
    }

    public ImageView getAlbumArt() {
        return albumArt;
    }

    public String getAuthCode() {
        return authCode;
    }

    public TextView getTitle() {
        return title;
    }


}
