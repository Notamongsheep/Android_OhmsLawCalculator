package com.christineberger.android.ohmslawcalculatoreventapp;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
public class MainActivity extends AppCompatActivity {

    //========== PRIVATE CLASS VARIABLES ==========//

    //variables to hold the numeric values of the inputs.
    private double volts;
    private double resistance;
    private double current;

    //variables that will point to the XML components.
    private EditText et_volts;
    private EditText et_resistance;
    private EditText et_current;

    //Variables used across the app that don't change.
    private final double DEFAULT_VAL = 0.0;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private final int blue = R.color.colorAccent;
    private final int white = R.color.colorLightFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //--------------------------------- Reference XML Elements. ------------------------------//
        //EditText Widgets
        et_volts = $et(R.id.input_volts);
        et_current = $et(R.id.input_current);
        et_resistance = $et(R.id.input_resistance);

        //TextView Widgets which display the units on focus of EditText fields.
        TextView text_volts = $tv(R.id.text_volts);
        TextView text_current = $tv(R.id.text_current);
        TextView text_resistance = $tv(R.id.text_resistance);

        //Button Widgets
        Button btn_calculate = $btn(R.id.btn_calculate);
        Button btn_clear = $btn(R.id.btn_clear);
        Button btn_exit = $btn(R.id.btn_exit);
        Button btn_help = $btn(R.id.btn_help);

        //---- Add InputWatchers to each EditText component so the color can change on input. ----//
        et_volts.addTextChangedListener(new InputWatcher(text_volts, et_volts));
        et_current.addTextChangedListener(new InputWatcher(text_current, et_current));
        et_resistance.addTextChangedListener(new InputWatcher(text_resistance, et_resistance));


        //---------------------------------- HELP Button Event -----------------------------------//
        //On click of 'help' icon, show the instructions.
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Use AlertDialog builder to customize a dialog.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                //Reference the layout that corresponds with the instructions modal.
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View layout = inflater.inflate(R.layout.modal_instr, (ViewGroup) findViewById(R.id.instructions_modal_layout));

                //Attach the layout to the builder.
                builder.setView(layout);

                //Create a dialog from the builder settings.
                final AlertDialog dialog = builder.create();

                //Reference the primary button from the layout and set the button to dismiss on
                //click.
                Button primaryBtn = (Button) layout.findViewById(R.id.btn_primary);
                primaryBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                //Show the dialog.
                dialog.show();
            }
        });


        //--------------------------------- CLEAR Button Event -----------------------------------//
        //On click of the 'clear' button, reset the UI text and data variables to the default value.
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Reset variables to 0.0.
                volts = DEFAULT_VAL;
                resistance = DEFAULT_VAL;
                current = DEFAULT_VAL;

                //Reset input components to display default text.
                et_volts.setText("");
                et_resistance.setText("");
                et_current.setText("");
            }
        });

        //-------------------------------- CALCULATE Button Event --------------------------------//
        //On click of 'Calculate!', calculate the missing value.
        btn_calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws NumberFormatException {

                //Get the values from the EditText strings.
                setValues();

                //If NO VALUES are given.
                if(volts == DEFAULT_VAL && current == DEFAULT_VAL && resistance == DEFAULT_VAL) {
                    useToaster(getString(R.string.str_error_not_enough_values));

                //If ALL VALUES were given.
                } else if(volts != DEFAULT_VAL && current != DEFAULT_VAL && resistance != DEFAULT_VAL) {

                    //If all values are given, but only volts and current are blue and resistance is
                    //white (Calculation has been done before - recalculate resistance).
                    if(currentColor(et_volts) == getResourceColor(blue) && currentColor(et_current) == getResourceColor(blue) && currentColor(et_resistance) == getResourceColor(white)) {
                        calcAndShowColor(et_resistance, white);
                    //If current and resistance are blue and volts are white (Calculation has been
                    //done before - recalculate volts).
                    } else if (currentColor(et_current) == getResourceColor(blue) && currentColor(et_resistance) == getResourceColor(blue) && currentColor(et_volts) == getResourceColor(white)) {
                        calcAndShowColor(et_volts, white);

                    //If volts and resistance are blue and current is white (Calculation has been
                    //done before - recalculate current).
                    } else if (currentColor(et_volts) == getResourceColor(blue) && currentColor(et_resistance) == getResourceColor(blue) && currentColor(et_current) == getResourceColor(white)){
                        calcAndShowColor(et_current, white);

                    //If all values are blue, the user either gave all values or changed the a white
                    // answer) value. Clear is needed.
                    } else {
                        useToaster(getString(R.string.str_error_all_values));
                    }

                //If SOME VALUES are given (This means that at least one value is in the default
                // state).
                } else {

                    //If ONLY ONE value is given, show an error.
                    if((volts != DEFAULT_VAL && current == 0 && resistance == 0) || (current != 0 && volts == 0 && resistance == 0) || (resistance != 0 && volts == 0 && current == 0)) {
                        useToaster(getString(R.string.str_error_not_enough_values));

                     //If volts and current is given, calculate resistance.
                    } else if (volts != 0 && current != 0) {
                        calcAndShowColor(et_resistance, white);

                    //Resistance IS given if the last statement didn't execute. So, if current is a
                    //number, calculate volts.
                    } else if (current != 0) {
                        calcAndShowColor(et_volts, white);

                    //Current IS NOT given if the last two statements didn't execute. Calculate
                    //current.
                    } else {
                        calcAndShowColor(et_current, white);
                    }
                }
            }
        });

        //--------------------------------- EXIT Button Event ------------------------------------//
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //------------------------------- ALERT DIALOG -----------------------------------//
                //Create new AlertDialog builder to customize dialog.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                //Reference and set the layout to be used in the dialog.
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                View layout = inflater.inflate(R.layout.modal, (ViewGroup) findViewById(R.id.modal_layout));

                //Attach the layout to the builder.
                builder.setView(layout);

                //Create a new dialog with the builder settings.
                final AlertDialog dialog = builder.create();

                //Reference the buttons from the layout and set listeners - primary button closes
                //the app and secondary dismisses the dialog.
                Button primaryBtn = (Button) layout.findViewById(R.id.btn_primary);
                Button secondaryBtn = (Button) layout.findViewById(R.id.btn_secondary);

                //Close app and kill all app processes.
                primaryBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Compatibility for exiting the app completely and ensuring it doesn't stay
                        //in the active apps.
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            MainActivity.super.finishAndRemoveTask();
                        }
                        else {
                            MainActivity.super.finish();
                        }
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                });

                //Dismiss the dialog and return to the main layout.
                secondaryBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                //Show the dialog.
                dialog.show();
            }
        });
    }

    //*********************************** CLASS METHODS ******************************************//

    //Find view by id methods.
    private EditText $et(int resourceId) { return (EditText) findViewById(resourceId); }
    private TextView $tv(int resourceId) { return (TextView) findViewById(resourceId); }
    private Button $btn(int resourceId) { return (Button) findViewById(resourceId); }

    /*================================= currentColor(EditText) ===================================//
     * Gets the current text color (The text color changes based on whether a value has been given
     * or not)
     */
    private int currentColor(EditText editText) {
        return editText.getCurrentTextColor();
    }

    /*============================ changeTextColor(EditText, int) ================================//
     * Changes the color of the text.
     */
    private void changeTextColor(EditText editText, int resourceId) {
        editText.setTextColor(ContextCompat.getColor(getApplicationContext(), resourceId));
    }

    /*================================ getResourceColor(int) =====================================//
     * Gets the resource color id in int form.
     */
    private int getResourceColor(int resourceId) {
        return ContextCompat.getColor(getApplicationContext(), resourceId);
    }

    /*============================ setTextDouble(EditText, double) ===============================//
     * Sets the text of an editText widget to a formatted double variable.
     */
    private void setTextDouble(EditText editText, double number) {
        editText.setText(decimalFormat.format(number));
    }

    /*========================== calcResistance() RETURNS resistance =============================//
     * Calculates the resistance by dividing volts by current and returns resistance.
     */
    private double calcResistance() throws NumberFormatException {
        try {
            resistance = volts / current;
        } catch (NumberFormatException e) {
            useToaster(getString(R.string.str_dev_error_values_not_set));
        }

        return resistance;
    }

    /*============================= calcCurrent() RETURNS current ================================//
     * Calculates the current by diving volts by resistance and returns current.
     */
    private double calcCurrent() {
        try {
            current = volts / resistance;
        } catch (NumberFormatException e) {
            useToaster(getString(R.string.str_dev_error_values_not_set));
        }

        return current;
    }

    /*============================== calcVolts() RETURNS volts ===================================//
     * Calculates the volts by multiplying current times resistance and returns volts.
     */
    private double calcVolts() {
        try {
            volts = current * resistance;
        } catch (NumberFormatException e) {
            useToaster(getString(R.string.str_dev_error_values_not_set));
        }

        return volts;
    }

    /*==================================== setValues() ===========================================//
     * Gets the values from the input widgets and sets the class variables with them.
     */
    private void setValues() {
        //Get the values from the EditText strings.
        volts = checkInput(et_volts);
        current = checkInput(et_current);
        resistance = checkInput(et_resistance);
    }

    /*========================== calcAndShowColor(EditText, int) =================================//
     * Calculates the unknown value based on the editText widget given.
     */
    private void calcAndShowColor(EditText editText, int resourceId) {

        //Get the resource ID and compare it to the inputs. Depending on the input, calculate the
        // missing variable.
        switch(editText.getId()) {
            case R.id.input_volts:
                setTextDouble(editText, calcVolts());
                break;
            case R.id.input_current:
                setTextDouble(editText, calcCurrent());
                break;
            case R.id.input_resistance:
                setTextDouble(editText, calcResistance());
                break;
        }

        changeTextColor(editText, resourceId);
    }

    /*================================== useToaster(String) ======================================//
     * Takes a string and shows it in a Toast on the screen.
     */
    private void useToaster(String text) {

        //Layout Inflater for setting the text on the button.
        LayoutInflater inflater = getLayoutInflater();
        //Reference the layout XML and layout Id for the inflater.
        View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toast_layout));

        //Reference the button resource id (This holds the error message).
        Button toast = (Button) layout.findViewById(R.id.toaster_msg);
        //Set the button text to the given string.
        toast.setText(text);

        //Create a new toast. Customize the settings, link it to the toast layout, and show.
        Toast errorToast = new Toast(getApplicationContext());
        errorToast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
        errorToast.setDuration(Toast.LENGTH_LONG);
        errorToast.setView(layout);
        errorToast.show();
    }

    /*==================================== checkInput(EditText) ==================================//
     * Checks that the input is a double and can be parsed. Returns the value if the number can be
     * parsed.
     */
    private double checkInput(EditText input) throws NumberFormatException {

        //Get the characters of the editText input without whitespaces.
        String string = input.getText().toString().trim();
        //Variable to hold the temp double result.
        double value = 0.0;

        try {
            //If the string is not empty, parse the double.
            if(!string.equals("") || string.length() != 0) {
                value = Double.parseDouble(string);
            }
        } catch (NumberFormatException e) {
            //Otherwise, throw an exception.
            useToaster(getString(R.string.str_dev_error_double_format));
        }

        return value;
    }

    /*############################## INNER CLASS INPUTWATCHER ######################################
    /* CLASS InputWatcher
     * This class implements TextWatcher and is used to watch the text as input is received.
     * When input is received, it changes the input color to blue and shows the units as a helper
     * text.
     * When there is no input, it hides the helper text and changes the input back to white.
     */
    private class InputWatcher implements TextWatcher {

        //Private variables to hold the TextView and EditText (This is for
        private TextView textView;
        private EditText editText;

        /*============================ beforeTextChanged(Editable) ===============================//
         * Constructor - Sets the private variables to the given arguments.
         */
        private InputWatcher(TextView textView, EditText editText) {
            this.textView = textView;
            this.editText = editText;
        }

        /*============================ beforeTextChanged(Editable) ===============================//
         * Required method for TextWatcher implementation.
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

       /*=================================== onTextChanged() ====================================//
        * Required method for TextWatcher implementation. This method will be used to change the
        * text to blue and show the helper text.
        */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
           if (editText.getText().length() == 0) {
               //Temp string to hold the units.
               String string;
               //Gets the correct units based on what the textView argument is.
               string = getUnits(textView);
               //Changes the input's color to blue.
               changeTextColor(editText, blue);
               //Sets the text in the unit textview.
               textView.setText(string);
           }
        }

       /*================================= afterTextChanged() ====================================//
        * Required method for TextWatcher implementation. This method will be used to check the
        * string and set the color of the string based on the input.
        */
        @Override
        public void afterTextChanged(Editable s) {

            //Temporary String
            String string;

            //If nothing is input, set the text to default.
            if (s.length() == 0) {
                textView.setText("");
            //Otherwise, show units next to the input area and change the input text color to blue.
            } else {
                string = getUnits(textView);
                textView.setText(string);
                changeTextColor(editText, blue);
            }
        }

        /*=================================== getUnits() =========================================//
         * Method that will get the unit text based on the TextView field associated with the
         * EditText component that's being edited.
         */
        private String getUnits(TextView textView) {

            //Temporary string.
            String string = "";

            //Get the id of the associated TextView.
            switch(textView.getId()) {
                //Set the text based on the id.
                case R.id.text_volts:
                    string = getString(R.string.str_units_volts);
                    break;
                case R.id.text_current:
                    string = getString(R.string.str_units_current);
                    break;
                case R.id.text_resistance:
                    string = getString(R.string.str_units_resistance);
                    break;
            }

            //Return the units.
            return string;
        }
    }
}
