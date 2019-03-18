package online.umassdartmouthsustainability.theshowerapp;



import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.android.appremote.api.UserApi;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;


import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import online.umassdartmouthsustainability.theshowerapp.fragments.StopwatchFragment;
import online.umassdartmouthsustainability.theshowerapp.fragments.UserMenuFragment;
import online.umassdartmouthsustainability.theshowerapp.managers.StopwatchManager;

public class MainActivity extends FragmentActivity
        implements StopwatchFragment.OnFragmentInteractionListener,
        UserMenuFragment.OnFragmentInteractionListener,
        SpotifyMainFragment.OnFragmentInteractionListener{

    private int DEM_REQ_CODE = 2345;

    private static volatile boolean readyToDoMainApp = false;

    private static final String EXTRA_DATA = "data";
    private static final String ACTION_SUBMIT_USER_DATA = "online.umassdartmouthsustainability.theshowerapp.action.SUBMIT_USER_DATA";
    private static final String uid = "userId";
    private String tag = "theShowerApp.MainActivity";

    private static final int SPOT_REQ_CODE = 1337;
    private static final String CLIENT_ID = "69bd0fab3b9343f1ae11805d2b7a61bf";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";



    public void onFragmentInteraction(Uri uri) {
        Log.d(tag, "fragment interaction");
    }

    private void displayMainApp(String token){
        setContentView(R.layout.activity_fragment_launcher);

        ViewPager vpPager = findViewById(R.id.vpPager);
        FragmentPagerAdapter adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(),token);
        vpPager.setAdapter(adapterViewPager);
        vpPager.setCurrentItem(1);
        MediaPlayer alarmMP = MediaPlayer.create(this, R.raw.alarm);
        alarmMP.setLooping(true);
        MediaPlayer successMP = MediaPlayer.create(this, R.raw.success);
        StopwatchManager.getInstance(alarmMP, successMP);
    }

    public static void setDisplayMainApp(){
        MainActivity.readyToDoMainApp = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MusicManager.getManager().setAlbumArt((ImageView) findViewById(R.id.albumArt));

        //get referenceto shared preferences
        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spEdit = sharedPreferences.edit();
        boolean forceStopwatchDisplay = false;
        if(forceStopwatchDisplay) {

            spEdit.putString(uid, "29");
            spEdit.apply();
        }

        //if the user doesn't already have a user id , then show the the demographis form
        boolean forceDemographics = false;
        if(forceDemographics){
            spEdit.clear();
            spEdit.apply();
        }
        if (sharedPreferences.getString(uid, "").equals("")) {
            Log.d(tag,"Starting demographics form.");
            Intent dem = new Intent(this, Demographics.class);
            startActivityForResult(dem, DEM_REQ_CODE);
        }

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
                    displayMainApp(response.getAccessToken());
                    ConnectionParams connectionParams =
                            new ConnectionParams.Builder(CLIENT_ID)
                                    .setRedirectUri(REDIRECT_URI)
                                    .showAuthView(true)
                                    .build();

                    SpotifyAppRemote.connect(this, connectionParams,
                            new Connector.ConnectionListener() {

                                public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                                    SpotifyAppRemote mSpotifyAppRemote = spotifyAppRemote;
                                    MusicManager m = MusicManager.getManager();
                                    m.setRemote(mSpotifyAppRemote);
                                    findViewById(R.id.pausePlayButton).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(m.isPlaying())
                                                mSpotifyAppRemote.getPlayerApi().resume();
                                            else
                                                mSpotifyAppRemote.getPlayerApi().pause();
                                        }
                                    });

                                }

                                public void onFailure(Throwable throwable) {
                                    Log.e(tag, throwable.getMessage(), throwable);

                                }
                            });



                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Log.d(tag, response.getError());
                    displayMainApp("");
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    Log.d(tag, "Unknown issue prevented successful aquisition ");
                    displayMainApp("");

            }

        }

    }





}
