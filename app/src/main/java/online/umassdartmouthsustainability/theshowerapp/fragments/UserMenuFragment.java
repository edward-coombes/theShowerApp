package online.umassdartmouthsustainability.theshowerapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import online.umassdartmouthsustainability.theshowerapp.R;
import online.umassdartmouthsustainability.theshowerapp.ServerConnection;
import online.umassdartmouthsustainability.theshowerapp.managers.UserMenuManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserMenuFragment extends Fragment {


    private static final String ACTION_RETRIEVE_SHOWER_DATA = "online.umassdartmouthsustainability.theshowerapp.action.RETRIEVE_SHOWER_DATA";
    private static final String ACTION_DELETE_SHOWER_DATA = "online.umassdartmouthsustainability.theshowerapp.action.DELETE_SHOWER_DATA";

    private static final String tag = "theShowerApp.UserMenu";

    //Not quite sure what it does, but it's gotta be there
    @SuppressWarnings("unused")
    private OnFragmentInteractionListener mListener;
    TableLayout table;
    Button deleteDataButton;

    //suppresses potential null pointer exception, as it has been handled
    @SuppressWarnings("ConstantConditions")
    View.OnClickListener deleteDataAction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TableRow th;
            Intent ServerConn = new Intent(getActivity(), ServerConnection.class);
            ServerConn.setAction(ACTION_DELETE_SHOWER_DATA);
            try {
                getActivity().startService(ServerConn);
            } catch (NullPointerException e) {
                Toast t = Toast.makeText(getContext(), "Unexpected Error starting Server connection service in UserMenu", Toast.LENGTH_LONG);
                t.show();
            }
            table.removeAllViews();
            th = createHeaders();
            table.addView(th);
        }
    };

    public UserMenuFragment() {
        // Required empty public constructor
    }

    public static UserMenuFragment newInstance() {

        return new UserMenuFragment();
    }

    //suppress null warning, it is contained in try catch
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get the user data
        //send the data to the server
        Intent ServerConn = new Intent(getActivity(), ServerConnection.class);
        UserMenuReceiver receiver = new UserMenuReceiver(this);
        try {
            getActivity().registerReceiver(receiver, new IntentFilter("online.umassdartmouthsustainability.theshowerapp.intent.RETRIEVE_SHOWER_DATA"));
        } catch (NullPointerException e) {
            Toast t = Toast.makeText(getContext(), "Unexpected error registering user data broadcast receiver in fragment", Toast.LENGTH_LONG);
            t.show();
        }



        ServerConn.setAction(ACTION_RETRIEVE_SHOWER_DATA);
        HashMap<String, String> data = new HashMap<>();
        data.put("userId", PreferenceManager
                .getDefaultSharedPreferences(getContext())
                .getString("userId", "")
        );


        ServerConn.putExtra("data", data);
        getActivity().startService(ServerConn);
    }


    public void parseData(String json) {
        Log.d(tag, "parsing data");
        try {
            char[] jsonCharArr = json.toCharArray();
            LinkedList<JSONObject> jsonList = new LinkedList<>();
            int i = 0;
            int j = 0;
            for (; j < json.length(); j++) {
                if (jsonCharArr[j] == '}') {
                    jsonList.add(new JSONObject(json.substring(i, j + 1)));
                    i = j + 2;
                }
            }
            createTable(jsonList);


        } catch (JSONException e) {
            Log.d(tag, "invalid json format");
        }
    }

    private void createTable(LinkedList<JSONObject> j) {
        TableRow t;

        //create the header
        table.removeAllViews();
        t = createHeaders();
        table.addView(t);
        for (int i = 0; i < j.size(); i++) {
            t = createRow(j.get(i));
            table.addView(t);
        }
    }

    //I like for loops, so that's what I used
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private TableRow createHeaders() {
        TableRow t = new TableRow(getContext());
        TextView tv;
        String headers[] = {"  Elapsed Time", "Goal Time", "Date-Time"};
        for (int i = 0; i < headers.length; i++) {
            tv = new TextView(getContext());
            tv.setText(headers[i]);
            t.addView(tv);
        }
        return t;
    }

    private TableRow createRow(JSONObject j) {
        TableRow tr = new TableRow(getContext());
        TextView tv;
        Iterator<String> keys = j.keys();
        String k, v;
        int secs;

        for (int i = 0; i < 3; i++) {
            k = keys.next();

            try {
                v = j.getString(k);
                tv = new TextView(getContext());
                switch (i) {
                    case 2:
                        tv.setText(v);
                        break;
                    default:
                        secs = Integer.parseInt(v);
                        tv.setText(String.format(Locale.US, "  %d:%02d", secs / 60, secs % 60));
                        tv.setTextSize(18);
                        break;
                }
                tr.addView(tv);
            } catch (JSONException e) {
                Log.d(tag, "The data is not properly formatted");
            }
        }
        return tr;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_usermenu, container, false);
        table = view.findViewById(R.id.table);
        deleteDataButton = view.findViewById(R.id.delete_data);
        deleteDataButton.setOnClickListener(deleteDataAction);
        String d = UserMenuManager.getInstance().getData();
        if (d != null)
            parseData(d);

        return view;
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
    @SuppressWarnings("unused")
    //Same deal where I dunno what this does, but it's gotta be there
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}

