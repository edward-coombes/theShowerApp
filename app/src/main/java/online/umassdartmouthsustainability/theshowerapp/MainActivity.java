package online.umassdartmouthsustainability.theshowerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;


import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;


import online.umassdartmouthsustainability.theshowerapp.fragments.StopwatchFragment;
import online.umassdartmouthsustainability.theshowerapp.fragments.UserMenuFragment;
import online.umassdartmouthsustainability.theshowerapp.managers.StopwatchManager;

public class MainActivity extends FragmentActivity
        implements StopwatchFragment.OnFragmentInteractionListener,
        UserMenuFragment.OnFragmentInteractionListener,
        SpotifyMainFragment.OnFragmentInteractionListener{

    private int DEM_REQ_CODE = 2345;


    private static final String EXTRA_DATA = "data";
    private static final String ACTION_SUBMIT_USER_DATA = "online.umassdartmouthsustainability.theshowerapp.action.SUBMIT_USER_DATA";
    private static final String uid = "userId";
    private String tag = "theShowerApp.MainActivity";

    private static final int SPOT_REQ_CODE = 1337;
    private static final String CLIENT_ID = "";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    private FragmentPagerAdapter adapterViewPager;



    public void onFragmentInteraction(Uri uri) {
        Log.d(tag, "fragment interaction");
    }

    private void displayMainApp(){
        setContentView(R.layout.activity_fragment_launcher);

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSpotifyConnection();
            }
        });

        ViewPager vpPager = findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setCurrentItem(1);
        MediaPlayer alarmMP = MediaPlayer.create(this, R.raw.alarm);
        alarmMP.setLooping(true);
        MediaPlayer successMP = MediaPlayer.create(this, R.raw.success);
        StopwatchManager.getInstance(alarmMP, successMP);
    }

    void setControlStripArt(Drawable art){
        ImageView stripArt = findViewById(R.id.albumArt);
        stripArt.setImageDrawable(art);
    }

    void setControlStripText(String text){
        TextView stripText = findViewById(R.id.songTitle);
        stripText.setText(text);
    }

    void setControlStripPlaying(boolean b){
        ImageView playing = findViewById(R.id.pausePlayButton);
        playing.setImageDrawable(
                    getResources().getDrawable((b)?R.drawable.pause_icon:R.drawable.play_icon));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get referenceto shared preferences
        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spEdit = sharedPreferences.edit();

        /*
        //Developer debug code
        boolean forceStopwatchDisplay = false;
        if(forceStopwatchDisplay) {

            spEdit.putString(uid, "29");
            spEdit.apply();
        }

        //developer debug code
        boolean forceDemographics = false;
        if(forceDemographics){
            spEdit.clear();
            spEdit.apply();
        }*/

        //if the user doesn't already have a user id , then show the the demographics form

        if (sharedPreferences.getString(uid, "").equals("")) {
            Log.d(tag,"Starting demographics form.");
            Intent dem = new Intent(this, Demographics.class);
            startActivityForResult(dem, DEM_REQ_CODE);
        }

        displayMainApp();



    }

    protected void initSpotifyConnection(){
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-private", "playlist-read",
                "playlist-read-private","user-read-private", "playlist-read",
                "playlist-read-private","streaming","user-modify-playback-state"});
        AuthenticationRequest request = builder.build();


        AuthenticationClient.openLoginActivity(this,SPOT_REQ_CODE,request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == this.DEM_REQ_CODE) {

            if (data.hasExtra("data")) {
                Intent ServerConn = new Intent(this, ServerConnection.class);
                ServerConn.setAction(ACTION_SUBMIT_USER_DATA);
                ServerConn.putExtra(EXTRA_DATA, data.getSerializableExtra("data"));
                startService(ServerConn);
            }

            Toast t = Toast.makeText(getApplicationContext(),"Swipe right to play music with Spotify \nSwipe left to view your shower history.",Toast.LENGTH_LONG);
            t.show();

        } else if (requestCode == 1337) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    MusicManager.getManager().setAuthCode(response.getAccessToken());
                    ConnectionParams connectionParams =
                            new ConnectionParams.Builder(CLIENT_ID)
                                    .setRedirectUri(REDIRECT_URI)
                                    .showAuthView(true)
                                    .build();

                    SpotifyAppRemote.connect(this, connectionParams,
                            new Connector.ConnectionListener() {

                                public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                                    //create references to remote
                                    SpotifyAppRemote mSpotifyAppRemote = spotifyAppRemote;
                                    MusicManager m = MusicManager.getManager();
                                    m.setRemote(mSpotifyAppRemote);

                                    //perform connection routine in spotify fragment
                                    ((SpotifyMainFragment)adapterViewPager
                                            .getItem(MyPagerAdapter.SPOTIFY))
                                            .connected();
                                    //set click listener for pause/play button
                                    findViewById(R.id.pausePlayButton).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(!m.isPlaying()) {
                                                mSpotifyAppRemote.getPlayerApi().resume();
                                                ((ImageView)v).setImageDrawable(
                                                        getResources().getDrawable(R.drawable.pause_icon));
                                            } else {
                                                mSpotifyAppRemote.getPlayerApi().pause();
                                                ((ImageView)v).setImageDrawable(
                                                        getResources().getDrawable(R.drawable.play_icon));
                                            }
                                            m.setPlaying(!m.isPlaying());
                                        }
                                    });

                                }

                                public void onFailure(Throwable throwable) {
                                    Log.e(tag, throwable.getMessage(), throwable);

                                }
                            });

                    //display the control strip
                    findViewById(R.id.connect).setVisibility(View.GONE);
                    findViewById(R.id.albumArt).setVisibility(View.VISIBLE);
                    findViewById(R.id.songTitleContainer).setVisibility(View.VISIBLE);
                    findViewById(R.id.pausePlayButton).setVisibility(View.VISIBLE);
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Toast.makeText(getApplicationContext(),
                            "Cannot connect to spotify: " + response.getError(),
                            Toast.LENGTH_LONG).show();
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    Toast.makeText(getApplicationContext(),
                            "Cannot connect to spotify due to unknown error (user cancel?)",
                            Toast.LENGTH_SHORT).show();
            }

        }

    }
                
}
