package in.ac.iitm.sapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences pref;
    public static String APP;
    public static String SERVER_LOGIN;
    public static String TAG = "LOGIN";
    public static final int RC_SIGN_IN = 4546;
    public static final String ROLL_PATT = "^[a-zA-Z]{2}\\d{2}[a-zA-Z]\\d{3}$";
    public boolean darkTheme;
    EditText rollInput;
    EditText passInput;
    GoogleSignInClient gsic;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsic = GoogleSignIn.getClient(this, gso);

        APP = this.getPackageName();
        SERVER_LOGIN = getString(R.string.server_login);

        pref = getSharedPreferences(APP, Context.MODE_PRIVATE);
        darkTheme = pref.getBoolean("darkTheme", false);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (darkTheme) {
                setTheme(R.style.AppThemeDark);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackDark));
                window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorBackDark));
            } else {
                setTheme(R.style.AppTheme);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorBack));
                window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorBack));
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        rollInput = findViewById(R.id.rollInput);
        passInput = findViewById(R.id.passInput);

        rollInput.setOnFocusChangeListener(new View.OnFocusChangeListener()  {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)  {
                    v.setAlpha(1.0f);
                    v.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), (darkTheme) ? R.color.colorPrimaryDark : R.color.colorPrimary));
                }  else  {
                    v.setAlpha(0.5f);
                    v.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), (darkTheme) ? R.color.colorTextDark : R.color.colorText));
                }
            }
        });
        passInput.setOnFocusChangeListener(new View.OnFocusChangeListener()  {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)  {
                    v.setAlpha(1.0f);
                    v.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), (darkTheme) ? R.color.colorPrimaryDark : R.color.colorPrimary));
                }  else  {
                    v.setAlpha(0.5f);
                    v.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), (darkTheme) ? R.color.colorTextDark : R.color.colorText));
                }
            }
        });

        requestQueue = Volley.newRequestQueue(this);

//        final int disabledColor = (darkTheme) ? R.color.colorAccentDark : R.color.colorAccent;
//
//        rollInput.addTextChangedListener(new TextWatcher()  {
//            @Override
//            public void afterTextChanged(Editable s) {}
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count)  {
//                if(count != 0)  {
//                    rollPut = true;
//                    if(passPut)  {
//                        //enable signin button
//                        findViewById(R.id.loginButton).setEnabled(true);
//                    }
//                }  else  {
//                    rollPut = false;
//                    findViewById(R.id.loginButton).setEnabled(false);
//                    findViewById(R.id.loginButton).setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), disabledColor));
//                }
//            }
//        });
//        passInput.addTextChangedListener(new TextWatcher()  {
//            @Override
//            public void afterTextChanged(Editable s) {}
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count)  {
//                if(count != 0)  {
//                    passPut = true;
//                    if(rollPut)  {
//                        //enable signin button
//                        findViewById(R.id.loginButton).setEnabled(true);
//                    }
//                }  else  {
//                    passPut = false;
//                    findViewById(R.id.loginButton).setEnabled(false);
//                    findViewById(R.id.loginButton).setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), disabledColor));
//                }
//            }
//        });

    }

    public void validateAndLogin(View view)  {
        //TODO: REMOVE CLEARTEXT NETWORK ATTRIBUTES
        final String roll = rollInput.getText().toString();
        final String pass = passInput.getText().toString();

        Matcher rp = Pattern.compile(ROLL_PATT).matcher(roll);

        //CONFIRM TOAST APPROACH WITH DESIGNER
        if(roll.length() > 0) {
            if (pass.length() > 0) {
                if (rp.find()) {
                    //attempt login
                    Log.v(TAG, "roll and pass passed prelim tests");

                    StringRequest loginReq = new StringRequest(Request.Method.POST, SERVER_LOGIN, new Response.Listener<String>() {
                        @SuppressLint("ApplySharedPref")
                        @Override
                        public void onResponse(String strResponse) {
                            try {
                                JSONObject response = new JSONObject(strResponse);
                                boolean rollPass = response.getBoolean("rollpass");
                                boolean passPass = response.getBoolean("passpass");

                                if(!rollPass)  {
                                    //incorrect roll number
                                    Toast.makeText(getApplicationContext(), R.string.login_incorrect_roll, Toast.LENGTH_LONG).show();
                                }  else if(!passPass)  {
                                    //incorrect password
                                    Toast.makeText(getApplicationContext(), R.string.login_incorrect_pass, Toast.LENGTH_LONG).show();
                                }  else  {
                                    //both are correct
                                    //set some sharedprefs
                                    JSONObject appData = response.getJSONObject("app");
                                    pref.edit().putString("authcode", appData.getString("authcode")).commit();
                                    pref.edit().putBoolean("loggedIn", true).commit();
                                    pref.edit().putBoolean("darkTheme", appData.getBoolean("darktheme")).commit();
                                    pref.edit().putString("latestVersion", appData.getString("version")).commit();
                                    pref.edit().putString("calendarHash", appData.getString("calendar_hash")).commit();
                                    pref.edit().putString("messHash", appData.getString("mess_hash")).commit();
                                    pref.edit().putString("instieventsHash", appData.getString("instievents_hash")).commit();
                                    pref.edit().putString("contactsHash", appData.getString("contacts_hash")).commit();
                                    pref.edit().putString("timetableHash", appData.getString("timetable_hash")).commit();
                                    //save data
                                    //TODO: check free space?

                                    boolean saveHome = saveData(response, "home", getString(R.string.home_data_store));
                                    boolean saveEvents = saveData(response, "events", getString(R.string.events_data_store));
                                    boolean saveSchedule = saveData(response, "schedule", getString(R.string.schedule_data_store));
                                    boolean saveMess = saveData(response, "mess", getString(R.string.mess_data_store));
                                    boolean saveContacts = saveData(response, "contacts", getString(R.string.contacts_data_store));
                                    boolean saveInstievents = saveData(response, "instievents", getString(R.string.instievents_data_store));

                                    if(!(saveHome && saveEvents && saveSchedule && saveMess && saveContacts && saveInstievents))  {
                                        //TODO: notify error here
                                        Log.e(TAG, "Failure in saving one or more data files");
                                    }

                                    (new Handler()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(getApplicationContext(), NavigatorActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 200);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "Invalid JSON response from server.");
                            }
                        }
                    }, new Response.ErrorListener()  {
                        @Override
                        public void onErrorResponse(VolleyError err)  {
                            //TODO: split into proper error handling
                            Log.e(TAG, "No response/error communicating with server: " + err.getMessage());
                        }
                    }){
                        @Override
                        protected Map<String,String> getParams()  {
                            Map<String,String> data = new HashMap<String,String>();
                            data.put("roll", roll);
                            data.put("pass", pass);
                            return data;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String,String> params = new HashMap<String, String>();
                            params.put("Content-Type","application/x-www-form-urlencoded");
                            return params;
                        }
                    };

                    requestQueue.add(loginReq);

                    findViewById(R.id.loginButton).setEnabled(false);
                } else {
                    //say "NO"
                    Toast.makeText(getApplicationContext(), R.string.login_invalid_roll, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.login_empty_password, Toast.LENGTH_LONG).show();
            }
        }  else  {
            Toast.makeText(getApplicationContext(), R.string.login_empty_roll, Toast.LENGTH_LONG).show();
        }

        //check if username and password are valid DONE
        //then post to server DONE

        //if can't connect, toast

        //else continue DONE

        //if correct, take the returned data (with auth code) and put in offline storage DONE
        //then launch main screen

        //if incorrect, indicate
        //if roll incorrect, animate roll bar
        //if pass incorrect, animate pass bar
        //create animation using VectorDrawable

        //LOGOUT: delete authcode when logging out from app (server)
        //delete app data
        //reset SharedPrefs
    }

    //OFFLINE DATA (SharedPrefs) NEEDS A LOGIN METHOD FIELD

    public void smailLogin(View view)  { //DEFERRED

        //attempt smail login
//        Intent signInIntent = gsic.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);

        //if device login successful, check email agaist regex
        //QUIRK: aliased SMail will fail

        //if validated, send idToken to server, get auth code and data and store
        //SERVER: get email and validate from idToken, if valid perform steps as normal ldap login
        //launch main screen

        //if invalid, logout
        //notify user by toast of invalidity

        //if device login unsuccessful, notify user by toast

        //NOTE: remember to logout on device and delete authcode when logging out from app
        //LOGOUT: GLogout on device, delete authcode on server
        //delete data from app
    }

    private boolean saveData(JSONObject data, String keyname, String filename)  {
        try (FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)) {
            Log.v(TAG, "Saved " + keyname + " data");
            fos.write(data.getJSONObject(keyname).toString().getBytes());
            return true;
        }  catch(JSONException e)  {
            Log.e(TAG, "Invalid JSON key " + keyname + " while attempting to write " + filename);
            //TODO: actually handle exceptions
            return false;
        }  catch(IOException e)  {
            Log.e(TAG, "Failure writing " + filename);
            return false;
        }
    }
}


/*
    TODO: On login, perform initial data download.
    TODO: On Home Activity launch, ping for latest info hashes for: instievents, contacts, app, messmenus, timetable
    TODO: If hash differs, get data *asynchronously*, show with round spinning progress indicator
    NOTE: ALL REQUESTS MUST REQUIRE authcode.
    TODO: Encryption for authcode and data.

    DATA CATEGORIES:
        PERSONAL - REQUESTED ON LOGIN
        APP - REQUESTED ON MAIN ACTIVITY AND LOGIN
        TASKS - REQUESTED ON LOGIN
        CALENDAR - REQUESTED ON LOGIN AND MAIN ACTIVITY IF HASH CHANGED

    0 is Monday, towards Tuesday



    //abbreviations:
    //  cat: category
    //  img: image
    //  authcode: authorization code
    Initial data download (upon login): (also pretty much the data schematic)
    {
        //          PERSONAL
        rollpass: true
        passpass: true
        authcode: <authcode>
        name: <first/display name>
        fullname: <name as in insti records>
        roll: <roll>
        hostel: <hostel>
        room: <room>
        img_url: <img_url>      //see image storage scheme below

        //          APP
        version: "1.0"
        instievents_hash: "0123456789abcdef"
        contacts_hash: "0123456789abcdef"
        messmenus_hash: "0123456789abcdef"
        timetable_hash: "0123456789abcdef"

        //          TASKS
        task_categories:    [
                                {
                                    id: 1
                                    name: "Personal"
                                    color: "#1e90ff"
                                    tasks: [1, 2, 5, 7, 9]
                                    completed: [1, 2]
                                }, ...
                            ]
        tasks:     [
                        {
                            id: 1
                            name: "Do Laundry"
                            description: "Use RR13 Washing Machine, ours' not working. Check up on detergent."
                            cat: 1
                            anytime: false
                            start_time: "1500"
                            end_time: "1550"
                            completed: true
                            date: "15052020" //creation date
                            complete_date: "15052020"
                        }, ...
                    ]

        //          CALENDAR
        academic_events:     [
                                {
                                    id: 1
                                    name: "Insti Ethnic Day"
                                    description: "A day to revel in the... I'm too tired to type."
                                    date: "21052020"
                                    remind_time: "9999"
                                }, ...
                            ]
        personal_events:     [
                        {
                            id: 1
                            name: "Prof's Birthday"
                            description: "Don't bunk today"
                            date: "21052020"
                            remind_time: "1500"
                        }, ...
                    ]

        //          TIMETABLE
        courses:    [
                        {
                            id: "ED5017"
                            name: "Digital Signal Processing"
                            slots:  [
                                        {
                                            day: 0
                                            slot:



        //          MESSMENU
        //DEFAULT COLOR ARRAY: ["#9cdb9c", ...]
        mess_menu_timings:  [
                                {
                                    id: 1
                                    name: "Breakfast"
                                    timings: ["0645", "1000"]
                                    color: "#9cdb9c"
                                }, ...
                            ]
        food_categories:    [
                                {
                                    id: 1
                                    name: "beverage_hot"
                                    icon: <base64/svg>
                                }, ...
                            ]
        mess_categories:    [
                                {
                                    id: 1
                                    shortname: "NORTH"
                                    longname: "NORTH INDIAN"
                                    messes: [1, 2, 3, 4]
                                }, ...
                            ]
        menus:  [
                    {
                        id: 1
                        offerings:  { //keys are days, 0 is Monday, towards Tuesday
                                        0:  [
                                                {
                                                    timings: 1
                                                    items:  [
                                                                {
                                                                    name: "Milk"
                                                                    cat: 1
                                                                }, ...
                                                            ]
                                                },
                                            ]
                                        1: ...
                                    }
                    }, ...
                ]
        //TODO: Work on menu compression.
        //NOTE: current menu specification does not support prices for items (strings can be modified though)
        //NOTE: FC is not supported - it sports a dynamic menu
        messes:     [
                        {
                            name: "R GOURAS"
                            cat: 1
                            menu: 1
                        }, ...
                    ]

        //          CONTACTS
        contact_categories: [
                                {
                                    id: 1
                                    name: "Administrative"
                                    contacts: [1, 2, 3, 4, 5, 6]
                                }
                            ]
        contacts:   [
                        {
                            id: 1
                            name: "Medical Emergency"
                            cat: 1
                            number: "0123-4567890"
                            icon: <base64/svg>
                        }, ...
                    ]

        //          INSTIEVENTS
        //NOTE: Send 10 events by default, allow search on server
        //TODO: Load more method for instievents
        insti_events:   [
                            {
                                id: 1
                                name: "Basics of Guitar"
                                date: "27042020"
                                location: "CLT"
                                time: "0800"
                                cover_url: server/photos/instievents/id
                                likes: 0
                                like_rolls: []
                                description: "It would be very muzak."
                            }, ...
                        ]
    }
 */

/*
    Image storage scheme:
    photos.iitm.ac.in -> gen insti photo
    server/photos/mugshots/... -> uploaded (changed) photos, only last update, delete if deleted
    on image request, server checks mugshots for existing photo for user, returns if found, else returns gen insti photo

    image is downloaded after initial data downloaded, from the provided img_url
    this is done *asynchronously* -> store img download state as sharedPref.
    disallow image change if current image is not downloaded (as indicated by sharedPref).
    attempt download on each start.

    Image sites (app access):
    server/photos/instievents/...
    server/photos/mugshots/...

    Image sites (server access):
    server/icons/contacts/...
    server/icons/food_categories/...


    TODO: edit home activity to kickstart image download.
 */