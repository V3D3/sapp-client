package in.ac.iitm.sapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.strictmode.CredentialProtectedWhileLockedViolation;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class NavigatorActivity extends AppCompatActivity {
    SharedPreferences pref;
    public static String APP;
    public static String SERVER;
    public static String SERVER_HASH;
    public static String SERVER_CALENDAR;
    public static String SERVER_MESS;
    public static String SERVER_INSTIEVENTS;
    public static String SERVER_CONTACTS;
    public boolean darkTheme;
    private static int activeFrame = 0;
    private ImageView[] navbarImg;
    private TextView[] navbarLabel;
    private FrameLayout[] navbarFrame;
    private FrameLayout mainView;
    RequestQueue requestQueue;

    private boolean updateCalendar = false; //NOTE Calendar is Academic Events
    private boolean updateMess = false;
    private boolean updateInstievents = false;
    private boolean updateContacts = false;

    private enum SM  {
        CALENDAR,
        MESS,
        INSTIEVENTS,
        CONTACTS,
        SCHEDULE
    }

    private static final int FRAME_HOME = 0;
    private static final int FRAME_SCHEDULE = 1;
    private static final int FRAME_MESS = 2;
    private static final int FRAME_INSTIEVENTS = 3;
    private static final int FRAME_PEOPLE = 4;
    private static final int FRAME_CONTACTS = 5;

    private static String TAG = "NAVIGATOR";

    private boolean navbarEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        APP = this.getPackageName();
        SERVER = getString(R.string.server);
        SERVER_HASH = getString(R.string.server_hash);
        SERVER_CALENDAR = getString(R.string.server_calendar);
        SERVER_MESS = getString(R.string.server_mess);
        SERVER_INSTIEVENTS = getString(R.string.server_instievents);
        SERVER_CONTACTS = getString(R.string.server_contacts);
        pref = getSharedPreferences(APP, Context.MODE_PRIVATE);
        darkTheme = pref.getBoolean("darkTheme", false);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (darkTheme) {
                setTheme(R.style.AppThemeDark);
                window.setStatusBarColor(getResources().getColor(R.color.colorMainBGDark));
                window.setNavigationBarColor(getResources().getColor(R.color.colorNavbarBGDark));
            } else {
                setTheme(R.style.AppTheme);
                window.setStatusBarColor(getResources().getColor(R.color.colorMainBG));
                window.setNavigationBarColor(getResources().getColor(R.color.colorNavbarBG));
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigator);

        (new Handler()).post(new Runnable() {
            @Override
            public void run() {
                bindNavbarAndUpdate();
                checkForUpdatedData();
            }
        });
//        (new Handler()).post(new Runnable() {
//            @Override
//            public void run() {

//            }
//        });
    }

    private void  bindNavbarAndUpdate()  {
        requestQueue = Volley.newRequestQueue(this);

        navbarImg = new ImageView[6];
        navbarLabel = new TextView[6];
        navbarFrame = new FrameLayout[6];
        mainView = findViewById(R.id.MainView);
        //get navbar menu items in labels, imgs array
        navbarImg[0] = findViewById(R.id.nav_home_img);
        navbarImg[1] = findViewById(R.id.nav_timetable_img);
        navbarImg[2] = findViewById(R.id.nav_mess_img);
        navbarImg[3] = findViewById(R.id.nav_events_img);
        navbarImg[4] = findViewById(R.id.nav_people_img);
        navbarImg[5] = findViewById(R.id.nav_contacts_img);
        navbarLabel[0] = findViewById(R.id.nav_home_label);
        navbarLabel[1] = findViewById(R.id.nav_timetable_label);
        navbarLabel[2] = findViewById(R.id.nav_mess_label);
        navbarLabel[3] = findViewById(R.id.nav_events_label);
        navbarLabel[4] = findViewById(R.id.nav_people_label);
        navbarLabel[5] = findViewById(R.id.nav_contacts_label);
        navbarFrame[0] = findViewById(R.id.nav_home_frame);
        navbarFrame[1] = findViewById(R.id.nav_timetable_frame);
        navbarFrame[2] = findViewById(R.id.nav_mess_frame);
        navbarFrame[3] = findViewById(R.id.nav_events_frame);
        navbarFrame[4] = findViewById(R.id.nav_people_frame);
        navbarFrame[5] = findViewById(R.id.nav_contacts_frame);
        for(int i = 0; i < 6; i++)  {
            navbarImg[i].setEnabled(false);
            navbarImg[i].setScaleX(0.8f);
            navbarImg[i].setScaleY(0.8f);
            navbarImg[i].setTranslationY(0);
            navbarLabel[i].setScaleX(0f);
            navbarLabel[i].setScaleY(0f);
            navbarFrame[i].setTranslationY(0);
        }
        navbarImg[activeFrame].setEnabled(true);
        navbarImg[activeFrame].setScaleX(1f);
        navbarImg[activeFrame].setScaleY(1f);
        navbarImg[activeFrame].setTranslationY(-6);
        navbarLabel[activeFrame].setScaleX(1f);
        navbarLabel[activeFrame].setScaleY(1f);
        navbarFrame[activeFrame].setTranslationY(-6);
        updateFragment();
    }
    private void checkForUpdatedData()  {
        try {
            if(isNetworkAvailable() && isConnected())  {
                Log.d(TAG, "Connected to Internet, requesting hashes.");
                StringRequest hashReq = new StringRequest(Request.Method.POST, SERVER_HASH, new Response.Listener<String>() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onResponse(String strResponse) {
                        try {
                            Log.d(TAG, "Received response for hashes.");
                            JSONObject response = new JSONObject(strResponse);
                            boolean authPass = response.getBoolean("authpass");

                            if(!authPass)  {
                                //unauthorized access
                                Toast.makeText(getApplicationContext(), R.string.main_invalid_auth, Toast.LENGTH_LONG).show();
                                logout();
                            }  else  {
                                Log.d(TAG, "Response valid, authorization valid.");
                                //set some sharedprefs
                                JSONObject hashData = response.getJSONObject("payload");
                                String calendarHash = hashData.getString("calendar_hash");
                                String messHash = hashData.getString("mess_hash");
                                String instieventsHash = hashData.getString("instievents_hash");
                                String contactsHash = hashData.getString("contacts_hash");
                                String timetableHash = hashData.getString("timetable_hash");

                                updateCalendar = !calendarHash.equals(pref.getString("calendarHash", ""));
                                updateMess = !messHash.equals(pref.getString("messHash", ""));
                                updateInstievents = !instieventsHash.equals(pref.getString("instieventsHash", ""));
                                updateContacts = !contactsHash.equals(pref.getString("contactsHash", ""));

                                if(updateCalendar)  {
                                    Log.d(TAG, "Updating calendar.");
                                    startDataUpdate(SERVER_CALENDAR, SM.CALENDAR);
                                    //refresh calendarActivity if active
                                }
                                if(updateMess)  {
                                    Log.d(TAG, "Updating mess.");
                                    startDataUpdate(SERVER_MESS, SM.MESS);
                                    if(activeFrame == FRAME_MESS)  {
                                        //request mess update
                                    }
                                }
                                if(updateInstievents)  {
                                    Log.d(TAG, "Updating instievents.");
                                    startDataUpdate(SERVER_INSTIEVENTS, SM.INSTIEVENTS);
                                    if(activeFrame == FRAME_INSTIEVENTS)  {
                                        //request instievents update
                                    }
                                }
                                if(updateContacts)  {
                                    Log.d(TAG, "Updating contacts.");
                                    startDataUpdate(SERVER_CONTACTS, SM.CONTACTS);
                                    if(activeFrame == FRAME_CONTACTS)  {
                                        //request contacts update
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Invalid JSON response from server.");
                            Log.e(TAG, "Response:\n" + strResponse);
                        }
                    }
                }, new Response.ErrorListener()  {
                    @Override
                    public void onErrorResponse(VolleyError err)  {
                        //TODO: split into proper error handling
                        notifyPossibleDeprecation();
                        Log.e(TAG, "No response/error communicating with server: " + err.getMessage());
                    }
                }){
                    @Override
                    protected Map<String,String> getParams()  {
                        Map<String,String> data = new HashMap<String,String>();
                        data.put("authcode", pref.getString("authcode", ""));
                        return data;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("Content-Type","application/x-www-form-urlencoded");
                        return params;
                    }
                };

                requestQueue.add(hashReq);
            }  else  {
                notifyPossibleDeprecation();
            }
        } catch (InterruptedException | IOException e) {
            notifyPossibleDeprecation();
            e.printStackTrace();
        }
    }

    public void notifyPossibleDeprecation()  {
        Toast.makeText(this, "Couldn't connect to the server.\nData may be outdated.", Toast.LENGTH_LONG).show();
    }

    public void navSwitch(View view)  {

        if(!navbarEnabled)  {
            return;
        }

        int requestedFrame = 0;
        switch(view.getId())  {
            case R.id.nav_timetable_frame:
                requestedFrame = 1;
                break;
            case R.id.nav_mess_frame:
                requestedFrame = 2;
                break;
            case R.id.nav_events_frame:
                requestedFrame = 3;
                break;
            case R.id.nav_people_frame:
                requestedFrame = 4;
                break;
            case R.id.nav_contacts_frame:
                requestedFrame = 5;
                break;
        }

        if(requestedFrame == activeFrame)  {
            return;
        }

        long duration = getResources().getInteger(R.integer.nav_anim_duration);
        AnimatorSet navTrans = new AnimatorSet();

        //out anims
        PropertyValuesHolder labelScaleX = PropertyValuesHolder.ofFloat("scaleX", 0f);
        PropertyValuesHolder labelScaleY = PropertyValuesHolder.ofFloat("scaleY", 0f);
        PropertyValuesHolder imgScaleX = PropertyValuesHolder.ofFloat("scaleX", 0.8f);
        PropertyValuesHolder imgScaleY = PropertyValuesHolder.ofFloat("scaleY", 0.8f);
        PropertyValuesHolder imgDown = PropertyValuesHolder.ofFloat("translationY", 0f);
        ObjectAnimator labelOut = ObjectAnimator.ofPropertyValuesHolder(navbarLabel[activeFrame], labelScaleX, labelScaleY);
        ObjectAnimator imgOut = ObjectAnimator.ofPropertyValuesHolder(navbarImg[activeFrame], imgScaleX, imgScaleY, imgDown);
        ObjectAnimator frameOut = ObjectAnimator.ofFloat(navbarFrame[activeFrame], "translationY", 0f);

        //out anims addition
        navTrans.playTogether(labelOut, imgOut);
        navTrans.playTogether(labelOut, frameOut);
        navTrans.setDuration(duration);

        //in anims
        PropertyValuesHolder labelScaleXi = PropertyValuesHolder.ofFloat("scaleX", 1f);
        PropertyValuesHolder labelScaleYi = PropertyValuesHolder.ofFloat("scaleY", 1f);
        PropertyValuesHolder imgScaleXi = PropertyValuesHolder.ofFloat("scaleX", 1f);
        PropertyValuesHolder imgScaleYi = PropertyValuesHolder.ofFloat("scaleY", 1f);
        PropertyValuesHolder imgUp = PropertyValuesHolder.ofFloat("translationY", -6f);
        ObjectAnimator labelIn = ObjectAnimator.ofPropertyValuesHolder(navbarLabel[requestedFrame], labelScaleXi, labelScaleYi);
        ObjectAnimator imgIn = ObjectAnimator.ofPropertyValuesHolder(navbarImg[requestedFrame], imgScaleXi, imgScaleYi, imgUp);
        ObjectAnimator frameIn = ObjectAnimator.ofFloat(navbarFrame[requestedFrame], "translationY", -6f);

        //in anims addition
        navTrans.playTogether(labelIn, imgIn);
        navTrans.playTogether(labelIn, frameIn);
        navTrans.setDuration(duration);

        navTrans.playTogether(labelIn, labelOut);
        navTrans.start();

        navbarImg[activeFrame].setEnabled(false);
        navbarImg[requestedFrame].setEnabled(true);
        activeFrame = requestedFrame;

        //actually change frame here
        updateFragment();
    }

    private void updateFragment() {
        Bundle fb = new Bundle();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch(activeFrame)  {
            case 1:
                ft.replace(R.id.MainView, new TimetableFragment());
                break;
            case 2:
                ft.replace(R.id.MainView, new MessFragment());
                break;
            case 3:
                ft.replace(R.id.MainView, new EventsFragment());
                break;
            case 4:
                ft.replace(R.id.MainView, new PeopleFragment());
                break;
            case 5:
                ft.replace(R.id.MainView, new ContactsFragment());
                break;
            default:
                fb.putString("homedata", loadHomeData());
                fb.putBoolean("darktheme", darkTheme);
                HomeFragment fg = new HomeFragment();
                fg.setArguments(fb);
                ft.replace(R.id.MainView, fg);
                break;
        }

        ft.commit();
    }

    private String loadHomeData()  {
        return loadDataStr(getString(R.string.home_data_store));
    }

    public void dimNavbar()  {
        long duration = getResources().getInteger(R.integer.dim_anim_duration);
        float finalOpacity = Float.parseFloat(getString(R.string.float_overlay_opacity));
        ObjectAnimator navbarFadeOut = ObjectAnimator.ofFloat(findViewById(R.id.navbarFadeOverlay), "alpha", finalOpacity);
        navbarFadeOut.setDuration(duration);
        navbarFadeOut.start();
        navbarEnabled = false;
    }
    public void undimNavbar()  {
        long duration = getResources().getInteger(R.integer.dim_anim_duration);
        ObjectAnimator navbarFadeIn = ObjectAnimator.ofFloat(findViewById(R.id.navbarFadeOverlay), "alpha", 0f);
        navbarFadeIn.setDuration(duration);
        navbarFadeIn.start();
        navbarEnabled = true;
    }
    public void logout()  {
        //method is public since errors on various fragments could require a logout
    }
    @SuppressLint("ApplySharedPref")
    public void switchTheme() {
        SharedPreferences pref = getSharedPreferences(APP, Context.MODE_PRIVATE);
        pref.edit().putBoolean("darkTheme", !darkTheme).commit();
        recreate();
    }

    public JSONObject loadData(String dataFileName)  {
        String dataAsString = loadDataStr(dataFileName);

//        try {
//            FileInputStream dataFile = getApplicationContext().openFileInput(dataFileName);
//            int currChar = 0;
//            while((currChar = dataFile.read()) != -1)  {
//                dataAsString.append((char) currChar);
//            }
//        } catch (FileNotFoundException e) {
//            //not gonna come here, no
//            Log.e(TAG, "A FileNotFoundException occured when opening " + dataFileName);
//            Log.e(TAG, "Requested data file (" + dataFileName + ") missing!");
//            e.printStackTrace();
//            logout();
//        } catch (IOException e)  {
//            //not sure what happens here, no
//            Log.e(TAG, "An IOException occured while reading " + dataFileName);
//            e.printStackTrace();
//            logout();
//        }

        try {
            return new JSONObject(dataAsString);
        }  catch  (JSONException e) {
            Log.e(TAG, "the data stored in data.json doesn't seem to be valid JSON");
            e.printStackTrace();
            logout();
        }
        return new JSONObject();
    }
    public String loadDataStr(String dataFileName)  {
        File file = new File(getFilesDir(), dataFileName);
        Scanner scan = null;
        try {
            scan = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Requested data file " + dataFileName + " missing!");
            logout();
            return "";
        }
        scan.useDelimiter("\\Z");
        String dataAsString = scan.next();
        scan.close();
        return dataAsString;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public boolean isConnected() throws InterruptedException, IOException {
        final String command = "ping -i 5 -c 1 " + SERVER;
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }

    private void startDataUpdate(final String SERV, final SM saveMethod)  {
        StringRequest dataReq = new StringRequest(Request.Method.POST, SERV, new Response.Listener<String>() {
            @Override
            public void onResponse(String strResponse) {
                try {
                    JSONObject response = new JSONObject(strResponse);
                    boolean authPass = response.getBoolean("authpass");

                    if(!authPass)  {
                        Toast.makeText(getApplicationContext(), R.string.main_invalid_auth, Toast.LENGTH_LONG).show();
                        logout();
                    }  else  {
                        JSONObject dataObj = response.getJSONObject("payload");

                        //sticking with switch for easy upgrading
                        switch(saveMethod)  {
                            case CALENDAR: {
                                JSONObject homeObj = loadData(getString(R.string.events_data_store));
                                homeObj.put("academic", dataObj);

                                boolean dataSaved = saveData(homeObj, getString(R.string.events_data_store));
                                if(!dataSaved)  {
                                    //doom
                                    Log.e(TAG, "Failure saving events data");
                                }
                                break;
                            }
                            default: {
                                boolean dataSaved = saveData(dataObj, getString(R.string.mess_data_store));
                                if(!dataSaved)  {
                                    //doom
                                    Log.e(TAG, "Failure saving mess data");
                                }
                                break;
                            }
                        }
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
                if(err instanceof TimeoutError)  {
                    startDataUpdate(SERV, saveMethod);
                }
                if(err instanceof NoConnectionError)  {
                    Log.e(TAG, "No response/error communicating with server: " + err.getMessage());
                }
            }
        }){
            @Override
            protected Map<String,String> getParams()  {
                Map<String,String> data = new HashMap<String,String>();
                data.put("authcode", pref.getString("authcode", ""));
                return data;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        requestQueue.add(dataReq);
    }

    private boolean saveData(JSONObject data, String filename) {
        try (FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)) {
            Log.v(TAG, "Saved " + filename + " data");
            fos.write(data.toString().getBytes());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failure writing " + filename);
            return false;
        }
    }
    //TODO: Implement logout
    //TODO: Check Internet connectivity
    //TODO: Check hashes
    //TODO: If invalid authcode, logout
    //TODO: If valid, if hashes changed, fetch data asynchronously (immediately)
}
