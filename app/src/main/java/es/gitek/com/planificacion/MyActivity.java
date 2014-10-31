package es.gitek.com.planificacion;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MyActivity extends Activity implements ActivitySwipeDetector.SwipeInterface {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private ProgressDialog pDialog;
    // URL to get contacts JSON
//    private static String base_url = "http://superlinea.grupogureak.com:8081/api/getplanificacion/";
    private static String base_url = "http://gitek2.grupogureak.com/api/getplanificacion/";

    // JSON Node names
    private static final String TAG_REF = "ref";
    private static final String TAG_AMAITUTA = "amaituta";
    private static final String TAG_LINEA = "linea";

    // PUSH NOTIFICATIONS
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "821035048649";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCMDemo";


    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;

    // contacts JSONArray
    JSONArray contacts = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> ofList;
    ArrayList<HashMap<String, String>> lLinea1;
    ArrayList<HashMap<String, String>> lLinea2;
    ArrayList<HashMap<String, String>> lLinea3;
    private ListView list;
    private Date gaur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        Calendar c = Calendar.getInstance();
        gaur = c.getTime();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String eguna = df.format(c.getTime());


        setTitle(eguna);

        ofList = new ArrayList<HashMap<String, String>>();

        list=(ListView)findViewById(android.R.id.list);

        String url = base_url + eguna;

        // Calling async task to get json
        new GetOfs().execute(url);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnTouchListener(new ActivitySwipeDetector(this, MyActivity.this));
        context = getApplicationContext();

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        }else {
            Log.i("IKER", "No valid Google Play Services APK found.");
        }
    }

    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("IKER", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }



    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetOfs extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MyActivity.this);
            pDialog.setMessage("Itxaron pixka bat...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... params) {

            String miurl = params[0];
            Servicioweb sh = new Servicioweb();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(miurl, Servicioweb.GET);

            if (jsonStr != null) {
                try {
                    ofList.clear();
                    JSONArray jsonArr = new JSONArray(jsonStr);

                    lLinea1 = new ArrayList<HashMap<String, String>>();
                    lLinea2 = new ArrayList<HashMap<String, String>>();
                    lLinea3 = new ArrayList<HashMap<String, String>>();


                    for (int i=0; i < jsonArr.length(); i++) {

                        JSONObject miof = jsonArr.getJSONObject(i);


                        String miref = miof.getString(TAG_REF).replace("<br >","<br/>").replace("<BR >","<br/>").replace("< br>","<br/>").replace("< BR>","<br/>").replace("< br >","<br/>").replace("<br>","<br/>").replace("<BR>","<br/>");
                        String amaituta = miof.getString(TAG_AMAITUTA);

                        String[] separated = miref.split("<br/>");
                        String ref="";
                        String of="";
                        if (separated.length ==2 ) {
                            ref=separated[0];
                            of=separated[1];
                        } else {
                            ref=miref;
                        }

                        // tmp hashmap for single contact
                        HashMap<String, String> orden = new HashMap<String, String>();
                        orden.put("ref",ref);
                        orden.put("of",of);
                        orden.put("amaituta",amaituta);

                        switch(Integer.parseInt(miof.getString(TAG_LINEA).toString())) {
                            case 1:
                                lLinea1.add(orden);
                                break;
                            case 2:
                                lLinea2.add(orden);
                                break;
                            case 3:
                                lLinea3.add(orden);
                        }
                    }

                    if (lLinea1.size() > 0) {
                        HashMap<String, String> orden = new HashMap<String, String>();
                        orden.put("ref","====== SIPLACE ======");
                        orden.put("of","---");
                        orden.put("amaituta","0");
                        ofList.add(orden);

                        int size = lLinea1.size();

                        for(int j = 0; j < size; j++) {
                            ofList.add(lLinea1.get(j));
                        }
                    }

                    if (lLinea2.size() > 0) {
                        HashMap<String, String> orden = new HashMap<String, String>();
                        orden.put("ref","====== ASSAMBLEON ======");
                        orden.put("of","---");
                        orden.put("amaituta","0");
                        ofList.add(orden);

                        int size = lLinea2.size();

                        for(int j = 0; j < size; j++) {
                            ofList.add(lLinea2.get(j));
                        }
                    }

                    if (lLinea3.size() > 0) {
                        HashMap<String, String> orden = new HashMap<String, String>();
                        orden.put("ref","======= MONTAJE ======");
                        orden.put("of","---");
                        orden.put("amaituta","0");
                        ofList.add(orden);

                        int size = lLinea3.size();

                        for(int j = 0; j < size; j++) {
                            ofList.add(lLinea3.get(j));
                        }
                    }




                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("Servicioweb", "Ez da daturik aurkitu.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */

//            list=(ListView)findViewById(android.R.id.list);


//            ListAdapter adapter = new SimpleAdapter(
//                    MyActivity.this, ofList,
//                       R.layout.of_list_item, new String[] { "ref","of", "amaituta" }, new int[] { R.id.ref, R.id.of, R.id.amaituta });
//
//
//            list.setAdapter(adapter);

            list=(ListView)findViewById(android.R.id.list);
            CustomAdapter adapter = new CustomAdapter(MyActivity.this, ofList);
            list.setAdapter(adapter);

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            //Calendar c = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd");
            String eguna = df.format(gaur.getTime());
            String url = base_url + eguna;

            new GetOfs().execute(url);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLeftToRight(View v)
    {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setCurrentItem(0);

        Date dtStartDate=gaur;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(dtStartDate);
        c.add(Calendar.DATE, -1);  // number of days to add

        String eguna = df.format(c.getTime());
        gaur = c.getTime();

        setTitle(eguna);

        String url = base_url + eguna;

        // Calling async task to get json
        new GetOfs().execute(url);

    }

    @Override
    public void onRightToLeft(View v)
    {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setCurrentItem(0);

        Date dtStartDate=gaur;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(dtStartDate);
        c.add(Calendar.DATE, 1);  // number of days to add

        String eguna = df.format(c.getTime());
        gaur = c.getTime();

        setTitle(eguna);

        String url = base_url + eguna;

        // Calling async task to get json
        new GetOfs().execute(url);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_my, container, false);
            return rootView;
        }
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MyActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask() {
            @Override
            protected void onPostExecute(Object msg) {
                Log.e("IKER", msg.toString());
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                Object b = msg;
                Log.e("IKER", msg);
                return b;
            }
        }.execute(null, null, null);

    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Explicitly specify that GcmIntentService will handle the intent.
            ComponentName comp = new ComponentName(context.getPackageName(),
                    GcmIntentService.class.getName());
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        }
    }
}
