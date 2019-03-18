package online.umassdartmouthsustainability.theshowerapp;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;

public class Demographics extends AppCompatActivity {

    private static boolean debug = true;
    private String tag = "theShowerApp.Demographics";

    private EditText age;
    private Spinner schoolYear;
    private Spinner gender;
    private Spinner raceEthnicity;
    private Spinner building;
    private Spinner college;

    private HashMap<String, String> data = new HashMap<>();

    private int ToS_REQUEST = 1234;
    private boolean ToSAgree = false;

    private Intent result = new Intent(this, Demographics.class);

    private Toast display;
    private int duration = Toast.LENGTH_SHORT;

    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demographics);

        resources = getResources();
        if (debug)
            Log.d(tag, "Demographics form content view set");

        //get all the resources
        this.age = findViewById(R.id.ETAge);
        this.schoolYear = findViewById(R.id.SYSpinner);
        this.gender = findViewById(R.id.Gspinner);
        this.raceEthnicity = findViewById(R.id.REspinner);
        this.building = findViewById(R.id.Bspinner);
        this.college = findViewById(R.id.Cspinner);


        Button submit = findViewById(R.id.Submit);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //code to execute when submit button clicked
                if (validateForm()) {
                    if (debug) {
                        Log.d(tag, "Demographics form valid, ToS accepted");
                    }

                } else {
                    if (debug) {
                        Log.d(tag, "Demographics form invalid or ToS not accepted");
                    }
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent d) {
        if (resultCode == RESULT_OK && requestCode == this.ToS_REQUEST) {
            if (d.hasExtra("agree")) {
                this.ToSAgree = (1 == d.getIntExtra("agree", -1));

                if (!this.ToSAgree) {
                    this.display = Toast.makeText(this, R.string.ToSError, this.duration);
                    this.display.show();
                    Log.d(tag, "The user did not agree to the ToS");
                } else {
                    if (data.containsKey("age")) {
                        Log.d(tag, "age: " + data.get("age"));
                    }
                    result.putExtra("data", data);
                    setResult(RESULT_OK, result);
                    finish();
                }
            }
        }
    }

    protected boolean validateForm() {
        boolean valid;
        //validate all form elements
        if (validateAge() && validateSchoolYear()
                && validateGender() && validateRaceEthnicity()
                && validateBuilding() && validateCollege()) {
            //display the terms of service
            Log.d(tag, "Showing ToS");
            Intent ToS = new Intent(this, ToS.class);
            startActivityForResult(ToS, this.ToS_REQUEST);
            valid = this.ToSAgree;
        } else {
            valid = false;
        }

        return valid;
    }


    protected boolean validateAge() {
        //this validation method  must go first, as it is the only one that doesn't append the "&"
        //just gets the value, and checks that it's between ageMin, and ageMax

        boolean valid;
        int value = -1;

        int ageMin = 17;
        int ageMax = 35;

        String aprefix = "age";


        try {
            value = Integer.parseInt(this.age.getText().toString());

            valid = (value >= ageMin && value <= ageMax);
        } catch (NumberFormatException e) {
            valid = false;
        }


        if (!valid) {
            this.display = Toast.makeText(this, R.string.aError, this.duration);
            this.display.show();
        } else {
            data.put(
                    aprefix,
                    Integer.toString(value));
        }
        return valid;
    }

    /*
     * All validation methods (except for age) have a similar setup,
     *
     * they get the value and position.
     * if the position != then the value is valid, as it is not blank.
     *
     * */

    protected boolean validateCollege() {
        boolean valid;
        String cprefix = "collegeId";
        int vposition = this.college.getSelectedItemPosition();

        String values[] = this.resources.getStringArray(R.array.College_Value_Array);

        valid = vposition != 0;

        if (!valid) {
            this.display = Toast.makeText(this, R.string.cError, this.duration);
            this.display.show();
        } else {
            this.data.put(cprefix, values[vposition]);
        }

        return valid;
    }

    protected boolean validateBuilding() {
        boolean valid;
        String bprefix = "building";
        int vposition = this.building.getSelectedItemPosition();

        String values[] = this.resources.getStringArray(R.array.Building_Value_Array);


        valid = vposition != 0;


        if (!valid) {
            this.display = Toast.makeText(this, R.string.bError, this.duration);
            this.display.show();
        } else {
            data.put(bprefix, values[vposition]);
        }

        return valid;
    }


    protected boolean validateSchoolYear() {
        boolean valid;
        String syprefix = "schYearCode";

        int vposition = this.schoolYear.getSelectedItemPosition();

        String values[] = this.resources.getStringArray(R.array.SchoolYear_Value_Array);

        valid = vposition != 0;

        if (!valid) {
            this.display = Toast.makeText(this, R.string.syError, this.duration);
            this.display.show();
        } else {
            this.data.put(syprefix, values[vposition]);
        }

        return valid;
    }

    protected boolean validateGender() {
        boolean valid;
        String gprefix = "gender";

        String value = this.gender.getSelectedItem().toString();
        int vposition = this.gender.getSelectedItemPosition();

        String options[] = this.resources.getStringArray(R.array.Gender_Array);
        String values[] = this.resources.getStringArray(R.array.Gender_Value_Array);

        valid = !value.equals(options[0]);


        if (!valid) {
            this.display = Toast.makeText(this, R.string.gError, this.duration);
            this.display.show();
        } else {
            this.data.put(gprefix, values[vposition]);
        }

        return valid;
    }

    protected boolean validateRaceEthnicity() {
        boolean valid;
        String reprefix = "ethnicityCode";
        int vposition = this.raceEthnicity.getSelectedItemPosition();

        String values[] = this.resources.getStringArray(R.array.RaceEthnicity_Value_Array);

        valid = vposition != 0;

        if (!valid) {
            this.display = Toast.makeText(this, R.string.reError, this.duration);
            this.display.show();
        } else {
            this.data.put(reprefix, values[vposition]);
        }

        return valid;
    }


}
