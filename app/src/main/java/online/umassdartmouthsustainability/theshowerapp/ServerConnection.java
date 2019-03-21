package online.umassdartmouthsustainability.theshowerapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class ServerConnection extends IntentService {

    private static final String ADD_USER_URL = "https://www.umassdartmouthsustainability.online/userHandler.php";
    private static final String SHOWER_URL = "https://www.umassdartmouthsustainability.online/timerHandler.php";
    private static final String GET_SHOWER_DATA_URL = "https://www.umassdartmouthsustainability.online/viewUserInfo.php";
    private static final String DELETE_SHOWER_DATA_URL = "https://www.umassdartmouthsustainability.online/deleteUserInfo.php";


    private static final String ACTION_SUBMIT_USER_DATA = "online.umassdartmouthsustainability.theshowerapp.action.SUBMIT_USER_DATA";
    private static final String ACTION_SUBMIT_SHOWER_DATA = "online.umassdartmouthsustainability.theshowerapp.action.SUBMIT_SHOWER_DATA";
    private static final String ACTION_RETRIEVE_SHOWER_DATA = "online.umassdartmouthsustainability.theshowerapp.action.RETRIEVE_SHOWER_DATA";
    private static final String INTENT_RETRIEVE_SHOWER_DATA = "online.umassdartmouthsustainability.theshowerapp.intent.RETRIEVE_SHOWER_DATA";
    private static final String ACTION_DELETE_SHOWER_DATA = "online.umassdartmouthsustainability.theshowerapp.action.DELETE_SHOWER_DATA";

    private static final String EXTRA_DATA = "data";
    private static final String uid = "userId";

    private static final String tag = "theShowerApp.ServerConn";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEdit;

    private HashMap<String, String> data;

    private RequestQueue queue;

    public ServerConnection() {
        super("ServerConnection");
    }


    @Override
    public void onCreate() {
        super.onCreate();

        queue = Volley.newRequestQueue(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEdit = sharedPreferences.edit();
        spEdit.apply();
    }

    //these casts are safe because in every scenario the serializable data that has been sent was a hashmap
    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            final String action = intent.getAction();
            if (ACTION_SUBMIT_USER_DATA.equals(action)) {
                this.data = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_DATA);
                handleActionSubmitUserData();

            } else if (ACTION_SUBMIT_SHOWER_DATA.equals(action)) {
                this.data = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_DATA);
                handleActionSubmitShowerData();

            } else if (ACTION_RETRIEVE_SHOWER_DATA.equals(action)) {
                this.data = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_DATA);
                handleActionRetrieveShowerData();

            } else if (ACTION_DELETE_SHOWER_DATA.equals(action)) {
                handleActionDeleteShowerData();
            }
        }
    }


    private void handleActionDeleteShowerData() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, DELETE_SHOWER_DATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d(tag, "Server response: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        errorDisplay("Shower Data Deletion", error);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                //get the parameters from the extra data sent through in the intent
                Map<String, String> params = new HashMap<>();
                params.put(uid, sharedPreferences.getString(uid, ""));
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                return headers;
            }
        };
        queue.add(postRequest);
    }

    private void handleActionRetrieveShowerData() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, GET_SHOWER_DATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d(tag, "Server response: " + response);
                        broadcastIntent(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error

                        errorDisplay("Shower Data Retrieval", error);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                //get the parameters from the extra data sent through in the intent
                Map<String, String> params = data;

                Log.d(tag,"retreiving shower data....");

                for (Map.Entry<String, String> item : params.entrySet()) {
                    String key = item.getKey();
                    String value = item.getValue();
                    Log.d(tag, "Key: " + key + " Value: " + value);
                }

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                return headers;
            }
        };
        queue.add(postRequest);

    }

    /**
     * Handle action Submit User Data in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSubmitUserData() {
        // Request a string response from the provided URL.
        StringRequest postRequest = new StringRequest(Request.Method.POST, ADD_USER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d(tag, "Server response: " + response);
                        if(response.charAt(0) == 'E'){
                            Log.d(tag, response);
                        } else {
                            spEdit.putString(uid, response);
                            spEdit.apply();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        errorDisplay("User Data Submission", error);

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                //get the parameters from the extra data sent through in the intent

                Map<String, String> params =  data;

                Log.d(tag,"submitting user data....");
                for (Map.Entry<String, String> item : params.entrySet()) {
                    String key = item.getKey();
                    String value = item.getValue();
                    Log.d(tag, "Key: " + key + " Value: " + value);
                }

                return params;// */
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                return headers;
            }

        };
        queue.add(postRequest);

//        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Submit Shower Data in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSubmitShowerData() {
        StringRequest postRequest = new StringRequest(Request.Method.POST, SHOWER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        handleActionRetrieveShowerData();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        errorDisplay("Shower Data Submission", error);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                //get the parameters from the extra data sent through in the intent
                Map<String, String> params = data;
                params.put(uid, sharedPreferences.getString(uid, "0"));
                for (Map.Entry<String, String> item : params.entrySet()) {
                    String key = item.getKey();
                    String value = item.getValue();
                    Log.d(tag, "Key: " + key + " Value: " + value);
                }

                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                return headers;
            }
        };
        queue.add(postRequest);
    }

    public void broadcastIntent(String json) {
        StringBuilder sb = new StringBuilder(json);
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(0);
        String j = sb.toString();
        Intent intent = new Intent();
        intent.putExtra("data", j);
        intent.setAction(INTENT_RETRIEVE_SHOWER_DATA);
        sendBroadcast(intent);
        Log.d(tag, "broadcasting");
    }

    private void errorDisplay(String func, VolleyError e) {
        Toast t = Toast.makeText(getApplicationContext(),
                String.format(Locale.US, "%s error code %d ", func, (e.networkResponse == null) ? -1 : e.networkResponse.statusCode),
                Toast.LENGTH_LONG);
        t.show();
    }
}
