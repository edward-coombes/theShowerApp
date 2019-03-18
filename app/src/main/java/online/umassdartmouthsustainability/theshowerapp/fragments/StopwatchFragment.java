package online.umassdartmouthsustainability.theshowerapp.fragments;

import online.umassdartmouthsustainability.theshowerapp.R;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.util.HashMap;
import java.util.Locale;

import online.umassdartmouthsustainability.theshowerapp.ServerConnection;
import online.umassdartmouthsustainability.theshowerapp.managers.StopwatchManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StopwatchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StopwatchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StopwatchFragment extends Fragment {
    @SuppressWarnings("unused")
    private boolean debug = true;
    @SuppressWarnings("unused")
    private String tag = "theShowerApp.Stopwatch";
    private static final String ACTION_SUBMIT_SHOWER_DATA = "online.umassdartmouthsustainability.theshowerapp.action.SUBMIT_SHOWER_DATA";
    private static final String ACTION_RETRIEVE_SHOWER_DATA = "online.umassdartmouthsustainability.theshowerapp.action.RETRIEVE_SHOWER_DATA";

    private boolean started = false;
    private boolean timerInitialized = false;

    private Button quick;
    private Button relax;
    private Button shave;
    private Button start_stop;
    private TextView display;

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            display.setText(stopwatchManager.getTimeString());
        }
    };

    private Resources res;

    private Toast message;
    private int messsageDuration = Toast.LENGTH_SHORT;
    private int REAL_SHOWER_LENGTH = 60;

    private static StopwatchManager stopwatchManager;

    private static final long startDelay = 3;


    //tbch i have no idea what this does, I just know if you take it out it breaks
    @SuppressWarnings("unused")
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
    //listeners

    View.OnClickListener initializeTimer = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!started) {
                //initialize the timer
                char code = (char) v.getTag();
                timerInitialized = true;


                stopwatchManager.initializeTimer(code);
            }
        }
    };

    private View.OnClickListener stopTimer = new View.OnClickListener() {
        //null is contained in try catch, have never run into an issue with it
        //"0:00" doesn't need to be translated
        @SuppressWarnings("all")
        @Override
        public void onClick(View v) {
            //stop the timer
            started = false;
            timerInitialized = false;
            stopwatchManager.stopTimer();

            //send the data to the server
            if (stopwatchManager.getElapsedTime() > REAL_SHOWER_LENGTH) {
                message = Toast.makeText(getActivity(),
                        res.getString(R.string.submitThankYou),
                        messsageDuration);
                message.show();
                //only send data to server for real showers
                Intent ServerConn = new Intent(getActivity(), ServerConnection.class);
                ServerConn.setAction(ACTION_SUBMIT_SHOWER_DATA);
                HashMap<String, String> data = new HashMap<>();
                data.put("goalTime", Integer.toString(stopwatchManager.getGoalTime()));
                data.put("elapsedTime", Integer.toString(stopwatchManager.getElapsedTime()));


                ServerConn.putExtra("data", data);
                try {
                    getActivity().startService(ServerConn);
                } catch (NullPointerException e) {
                    Toast t = Toast.makeText(getContext(), "Unexpected Error starting Server connection service in stopwatch", Toast.LENGTH_LONG);
                    t.show();
                }
            } else {
                Toast t = Toast.makeText(getContext(), "Wow! That was a short shower...", messsageDuration);
                t.show();
            }

            display.setText("0:00");
            start_stop.setText(res.getString(R.string.startTimer));
            start_stop.setOnClickListener(startTimer);
            quick.setVisibility(View.VISIBLE);
            relax.setVisibility(View.VISIBLE);
            shave.setVisibility(View.VISIBLE);
        }
    };
    private View.OnClickListener startTimer = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //start the timer
            if (!timerInitialized) {
                //tell the user to select a value
                message = Toast.makeText(getActivity(),
                        "you must select a time",
                        messsageDuration);
                message.show();
            } else {
                //start the timer
                started = true;
                stopwatchManager.startTimer();
                message = Toast.makeText(getActivity(),
                        String.format(Locale.US, "The timer will start in %d seconds", startDelay),
                        messsageDuration);
                message.show();
                setStartedView();
            }

        }
    };

    public static StopwatchFragment newInstance() {

        return new StopwatchFragment();
    }

    private void setStartedView() {
        start_stop.setText(res.getString(R.string.stopTimer));
        display.setText(stopwatchManager.getTimeString());
        quick.setVisibility(View.INVISIBLE);
        relax.setVisibility(View.INVISIBLE);
        shave.setVisibility(View.INVISIBLE);

        start_stop.setOnClickListener(stopTimer);
    }

    @Override
    public void onResume() {
        super.onResume();
        stopwatchManager = StopwatchManager.getInstance();
        stopwatchManager.setUiHandler(uiHandler);
        if (stopwatchManager.getStarted()) {
            setStartedView();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_stopwatch, container, false);


        this.res = getResources();
        this.display = view.findViewById(R.id.display);


        //set event listeners
        quick = view.findViewById(R.id.quick);
        quick.setOnClickListener(initializeTimer);
        quick.setTag(StopwatchManager.QUICK_CODE);

        shave = view.findViewById(R.id.shave);
        shave.setOnClickListener(initializeTimer);
        shave.setTag(StopwatchManager.SHAVE_CODE);

        relax = view.findViewById(R.id.relax);
        relax.setOnClickListener(initializeTimer);
        relax.setTag(StopwatchManager.RELAX_CODE);

        this.start_stop = view.findViewById(R.id.start_stop);
        this.start_stop.setOnClickListener((started) ? stopTimer : startTimer);

        return view;
    }

    public void setDisplay(String d) {
        this.display.setText(d);
    }

}