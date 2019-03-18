package online.umassdartmouthsustainability.theshowerapp;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ToS extends AppCompatActivity {
    /*
     * This class just adds all of the text lines to the scrolling view
     * so that they can all be contained in the strings.xml file for easier updating
     * */

    private boolean debug = true;
    private Intent result = new Intent(this, ToS.class);
    private static final String tag = "theShowerApp.ToS";

    //the deprecation is handled
    @SuppressWarnings({"deprecation", "ForLoopReplaceableByForEach"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tos);

        Log.d(tag, "TOSA content view set");

        Resources res = getResources();
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout l = this.findViewById(R.id.ToSLayout);

        Button accept = findViewById(R.id.Accept_ToS);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.putExtra("agree", 1);
                if (debug)
                    Log.d(tag, "ToS agreed to");
                setResult(RESULT_OK, result);
                finish();
            }
        });

        Button decline = findViewById(R.id.Decline_ToS);
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.putExtra("agree", 0);
                if (debug)
                    Log.d(tag, "ToS disagreed");
                setResult(RESULT_OK, result);
                finish();
            }
        });

        //string array
        String[] body = res.getStringArray(R.array.Terms_Of_Service_Array);

        for (int i = 0; i < body.length; i++) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(lparams);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setLinkTextColor(Color.BLUE);
            if (Build.VERSION.SDK_INT >= 24) {
                tv.setText(Html.fromHtml(body[i], Html.FROM_HTML_MODE_LEGACY));
            } else {
                tv.setText(Html.fromHtml(body[i]));
            }
            l.addView(tv);
        }

        if (debug)
            Log.d("HELP", "TOSA body done loading");
    }
}
