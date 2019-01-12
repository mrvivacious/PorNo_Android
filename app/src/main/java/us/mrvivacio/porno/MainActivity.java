package us.mrvivacio.porno;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "dawg";
    private ArrayList<String> items;

    private ArrayAdapter<String> itemsAdapter;
    private ListView lvItems;

    // This holds the redirect links for MyAccessibilityService to refer to
    public static ArrayList<String> URLs;

    // This holds the latest porn domains from database
    public static ArrayList<String> realtimeBannedLinks = new ArrayList<String>();


    // Thank you, https://stackoverflow.com/questions/39052127/how-to-add-an-actionbar-in-android-studio-for-beginners
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Remind user to enable PorNo! service
        // Thank you for toast lmao: https://stackoverflow.com/questions/3500197/how-to-display-toast-in-android
        if (!isAccessibilitySettingsOn(this)) {
            alertUser();
        }

        // Tutorial code
        // Thank you, https://guides.codepath.com/android/Basic-Todo-App-Tutorial#configuring-android-studio
        lvItems = (ListView) findViewById(R.id.lv_Items);
        items = new ArrayList<String>();

        // Populate the listView with the link names saved in sharedPref
        initList();

        itemsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);

        lvItems.setAdapter(itemsAdapter);
        setupTouchListeners();
    }

    // Attaches a long click listener
    private void setupTouchListeners() {
        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
                        // DELETE FROM SHARED PREF
                        String name = items.get(pos);
                        deleteItem(name);

                        Log.d(TAG, "deleting : " + items.get(pos));

                        // Remove the item within array at position
                        items.remove(pos);

                        // Refresh the adapter
                        itemsAdapter.notifyDataSetChanged();

                        // Return true consumes the long click event (marks it handled)
                        return true;
                    }
                }
        );
        // OpenURL(), essentially
        lvItems.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                        // Get the text value of the clicked item and parse the url
                        String text = items.get(pos);

                        String toOpen = getItem(text);

                        if (toOpen == null) {
                            // Lol how did this happen
                            return;
                        }

                        openURL(toOpen);

                    }
                }
        );
    }

    // Open all the saved URLs
    public void onEmergency(View v) {
        ArrayList<String> names = new ArrayList<String>();

        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        Map<String, ?> allLinks = prefs.getAll();

        for (Map.Entry<String, ?> entry : allLinks.entrySet()) {
            String URL = entry.getValue().toString();
            Log.d(TAG, "opening + " + URL);

            openURL(URL);
        }
    }

    // Open a URL in a new window
    private void openURL(String URL) {
        // Open the url
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
        startActivity(browserIntent);
    }

    // Return the URL of the passed in name key
    private String getItem(String key) {
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        String URLtoOpen = prefs.getString(key, null);

        return URLtoOpen;
    }

    // Delete name:url from Shared Preferences
    private void deleteItem(String key) {
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove(key);
        editor.apply();

        Toast.makeText(this, key + " was deleted ~", Toast.LENGTH_LONG).show();
    }

    // Get keys from Shared Preferences and initialize our list
    private void initList() {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> URLList = new ArrayList<String>();

        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        Map<String, ?> allLinks = prefs.getAll();

        Log.d(TAG, "keys  =  " + allLinks.keySet());

        // Thank you, https://stackoverflow.com/questions/22089411/how-to-get-all-keys-of-sharedpreferences-programmatically-in-android
        for (Map.Entry<String, ?> entry : allLinks.entrySet()) {
            String name = entry.getKey();
            String URL = entry.getValue().toString();

            if (porNo.isPorn(getHostName(URL))) {
                // Shame on you
                deleteItem(name);
            }
            // The URL isn't in our porn map, so keep it le mao
            else {
                Log.d(TAG, "entry.key : val = " + name + " : " + URL);

                names.add(name);
                URLList.add(URL);      // In order to reference URLs during redirection
            }
        }

        items = names;
        URLs = URLList;
    }

    // Save name:url to Shared Preferences
    private void writeItems(String name, String URL) {
        // Get prefs, then save as NAME:URL
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(name, URL);
        editor.apply();
    }

    public String getHostName(String url) {
        URI uri;

        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return url;
        }

        String hostName = uri.getHost();

        // If null, return the original url
        if (hostName == null) {
            return url;
        }
        else if (hostName.contains("www.")) {
            hostName = hostName.substring(4);
        }

        // Fuck you websites that use mobile prefix and break my hashmap
        if (hostName.contains("mobile.")) {
            return hostName.substring(7);
        }
        else if (hostName.contains("m.")) {
            return hostName.substring(2);
        }

        return hostName;
    }

    // To check if service is enabled
    // Thank you, https://stackoverflow.com/questions/18094982/detect-if-my-accessibility-service-is-enabled
    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = getPackageName() + "/" + MyAccessibilityService.class.getCanonicalName();

        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
//            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

//                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
//                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    // Explain to user how to enable PorNo!
    // Thank you, https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
    private void alertUser() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        // Build the alert
        builder.setTitle("PorNo! is off, but that's ok -- ")
                .setMessage("Please go to \n> Settings \n> Accessibility \n> PorNo! \nto turn on PorNo!")
                .setPositiveButton("Let's go to Accessibility", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Open accessibility screen
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("I'll go there myself", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////// CORRESPONDS TO ACTION BAR MENU /////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Screen the URL and add it if the URL isn't in our porn map
    public void onAddItem(View v) {
        EditText url = findViewById(R.id.et_NewItem);
        EditText name = findViewById(R.id.et_NewItem2);

        String urlText = url.getText().toString().trim();
        String nameText = name.getText().toString().trim();

        if (porNo.isPorn(getHostName(urlText))) {
            // tf u doing
            Toast.makeText(this, "That link isn't going to work, sorry.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (urlText.length() < 1) {
            // No link? No action
            return;
        }
        else if (nameText.length() < 1) {
            // No name provided? Use the url as the name
            nameText = urlText;
        }

        // Save this link to Shared Preferences
        writeItems(nameText, urlText);

        itemsAdapter.add(nameText);
        url.setText("");
        name.setText("");
    }

    public void newToast(MenuItem item) {
        Toast.makeText(this, "testing", Toast.LENGTH_LONG).show();
    }

    public void showInfo(MenuItem item) {
        Toast.makeText(this, "Showing info", Toast.LENGTH_LONG).show();
    }

    public void updateLinks(MenuItem item) {

    }

    // Open the site corresponding to the Chrome extension
    public void openChrome(MenuItem item) {
        openURL("https://chrome.google.com/webstore/detail/porno-beta/fkhfpbfakkjpkhnonhelnnbohblaeooj");
    }

    public void showAbout(MenuItem item) {
        Toast.makeText(this, "about page open", Toast.LENGTH_LONG).show();
    }
}
