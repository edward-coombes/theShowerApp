package online.umassdartmouthsustainability.theshowerapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SpotifyMainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SpotifyMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpotifyMainFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private static final int SPOT_REQ_CODE = 1337;
    private static final String CLIENT_ID = "69bd0fab3b9343f1ae11805d2b7a61bf";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";

    private List<String> imageURL = new ArrayList<String>();
    private List<ImageButton> imageButtons = new ArrayList<ImageButton>();

    private String accessToken;

    private SpotifyAppRemote mSpotifyAppRemote;
    private SpotifyApi mSpotifyApi;
    private SpotifyService mSpotifyService;

    PopulateArgs popa;

    private static SpotifyMainFragment m;

    private String tag = "theShowerApp.spotFrag";

    private LinearLayout cont;

    public SpotifyMainFragment() {
        // Required empty public constructor
    }

    public static SpotifyMainFragment newInstance() {
        if(m == null) {
            m = new SpotifyMainFragment();
        }

        return m;
    }


    private void populate(){
        //add all of the clickable playlist icons to play music
        MusicManager m = MusicManager.getManager();
        mSpotifyService = m.getService();

        mSpotifyService.getMyPlaylists( new retrofit.Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> pager, Response response) {

                popa = new PopulateArgs(mSpotifyService,cont,m.getRemote(),getContext(),pager);
                new PopulateTask().execute(popa);
            }

            @Override
            public void failure(RetrofitError error) {
                //could not access spotify api no playlists available
                Log.e("Playlist failure", error.toString());
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_spotify_main, container, false);
        //get reference to container
        this.cont = v.findViewById(R.id.playlists);

        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("theShowerApp.spotF","starting");
        if(popa!= null){
            popa.setView(getActivity().findViewById(R.id.playlists));
            new PopulateTask().execute(popa);
        }else {
            Log.d("theShowerApp.spotF", "null popa");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }


    public void connected() {

        //Finish setting up all the stuff.
        mSpotifyApi = new SpotifyApi();
        mSpotifyApi.setAccessToken(MusicManager.getManager().getAuthCode());
        MusicManager.getManager().setService(mSpotifyApi.getService());

        populate();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

class PlaylistClickListener implements View.OnClickListener {
    PrefabPlaylist p;
    SpotifyAppRemote r;
    Context c;
    public PlaylistClickListener(PrefabPlaylist p,SpotifyAppRemote r,Context c){
        this.p = p;
        this.r = r;
        this.c = c;
    }
    @Override
    public void onClick(View v){
        MusicManager m = MusicManager.getManager();
        m.setPlaying(true);
        r.getPlayerApi().play(p.uri);

        ((MainActivity)c).setControlStripArt(p.image);
        ((MainActivity)c).setControlStripText(p.title);
        ((MainActivity)c).setControlStripPlaying(true);

    }
}

class PopulateArgs {
    public SpotifyService s;
    public LinearLayout v;
    public SpotifyAppRemote r;
    public Context c;
    public Pager p;
    PopulateArgs(SpotifyService s, LinearLayout v, SpotifyAppRemote r, Context c, Pager p){
        this.s = s;
        this.v = v;
        this.r = r;
        this.c = c;
        this.p = p;
    }

    public void setView(LinearLayout v){
        this.v = v;
    }
}

class PrefabPlaylist{
    public Drawable image;
    public String title;
    public String uri;

    PrefabPlaylist(Drawable d, String s, String u){
        this.image = d;
        this.title = s;
        this.uri = u;
    }
}


class PopulateTask extends AsyncTask<PopulateArgs,Integer,List<PrefabPlaylist>> {

    private SpotifyService s;
    private LinearLayout v;
    private SpotifyAppRemote r;
    private Context c;
    private Pager<PlaylistSimple> pager;
    private List<PrefabPlaylist> l;
    private String tag = "theShowerApp.pop";

    @Override
    protected List<PrefabPlaylist> doInBackground(PopulateArgs... populateArgs) {
        this.s = populateArgs[0].s;
        this.v = populateArgs[0].v;
        this.r = populateArgs[0].r;
        this.c = populateArgs[0].c;
        this.pager = populateArgs[0].p;

        String currURL;

        this.l = new ArrayList<PrefabPlaylist>();

        for(PlaylistSimple p : pager.items){
            //get the image url from the playlist
            currURL = p.images.get(0).url;

            try {
                InputStream is = (InputStream) new URL(currURL).getContent();     //get input stream
                Drawable bg = Drawable.createFromStream(is,null);       //make this drawable
                l.add(new PrefabPlaylist(bg, p.name,p.uri));
            } catch(IOException e){
                Log.e("theShowerApp.pop","IOException in populate");
            }
        }
        return l;
    }

    @Override
    protected void onPostExecute(List<PrefabPlaylist> list){
        Log.d("theShowerApp.pop","adding playlistviews");
        //Create layout parameters for margins
        LinearLayout.LayoutParams containerStyle = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                        LinearLayout.LayoutParams.WRAP_CONTENT);
        containerStyle.setMargins(32,32,0,32);



        LinearLayout.LayoutParams titleStyle = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        //create references to the id's of the playlist containers for styling.
        Integer prevID = null;
        Integer currID;

        //calculate screen sizes to make dynamically sized playlist icons

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)c).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int screenWidth = displayMetrics.widthPixels;
        int imageSize = (screenWidth/2)-(screenWidth/8);

        // loop thru all of the prefab playlists
        for(int i = 0; i < list.size();i++) {
            PrefabPlaylist p = list.get(i);
            LinearLayout playlist = new LinearLayout(c);
            currID = new Integer(View.generateViewId());
            ConstraintSet constraintSet = new ConstraintSet();

            if(i == 0){//special constraint for the first playlist
                constraintSet.connect(currID,ConstraintSet.TOP,R.id.playlists,ConstraintSet.TOP);
            } else {
                constraintSet.connect(currID,ConstraintSet.TOP,prevID,ConstraintSet.BOTTOM);
            }
            if(i == list.size()-1){
                //special constrain for last playlist to be constrained to the bottom of the screen
                constraintSet.connect(currID,ConstraintSet.BOTTOM, R.id.playlists,ConstraintSet.BOTTOM);
            }
            constraintSet.connect(currID,ConstraintSet.LEFT,R.id.playlists,ConstraintSet.LEFT);
            constraintSet.connect(currID,ConstraintSet.RIGHT,R.id.playlists,ConstraintSet.RIGHT);

            //create the playlist container layout
            playlist.setId(View.generateViewId());
            playlist.setLayoutParams(containerStyle);
            playlist.setOnClickListener(new PlaylistClickListener(p,r,c));

            //create the playlist image icon
            ImageView b = new ImageView(c);
            b.setBackground(p.image);
            b.setLayoutParams(new Gallery.LayoutParams(imageSize,imageSize));
            playlist.addView(b);

            //create the title text view
            TextView t = new TextView(c);
            t.setText(p.title);
            t.setPadding(4,0,2,0);
            t.setGravity(Gravity.CENTER);
            t.setLayoutParams(titleStyle);
            t.setTextAppearance(c,R.style.playlistTitle);
            playlist.addView(t);

            //add the playlist to the screen
            v.addView(playlist);

            prevID = currID;
        }

    }

}