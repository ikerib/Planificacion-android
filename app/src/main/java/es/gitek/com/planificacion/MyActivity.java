package es.gitek.com.planificacion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
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
    private static String base_url = "http://superlinea.grupogureak.com:8081/api/getplanificacion/";

    // JSON Node names
    private static final String TAG_REF = "ref";
    private static final String TAG_LINEA = "linea";

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

            list=(ListView)findViewById(android.R.id.list);


            ListAdapter adapter = new SimpleAdapter(
                    MyActivity.this, ofList,
                       R.layout.of_list_item, new String[] { "ref","of" }, new int[] { R.id.ref, R.id.of });


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

}
