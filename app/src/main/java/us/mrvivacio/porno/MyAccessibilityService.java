// PorNo! Android
//
// MyAccessibilityService.java
// This file handles the reception of Accessibility events
// We analyze the events related to web-browsing and text-editing for any sign of porn
//
// @author Vivek Bhookya

package us.mrvivacio.porno;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Todo:
// "chrome://bookmarks" triggers redirection | RE: it wasn't chrome://bookmarks, it was the URL to Chrome PorNo! lmao

// BIG THANK YOUs TO https://stackoverflow.com/questions/38783205/android-read-google-chrome-url-using-accessibility-service
// and https://stackoverflow.com/questions/42125940/how-to-use-accessibility-services-for-taking-action-for-users
public class MyAccessibilityService extends AccessibilityService {
    static Map<String, Boolean> dict2 = new HashMap<String, Boolean>();;

    static String TAG = "dawgAccessibility";
    private String omnibox = "zz";
    private long time;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ATTEMPTING TO CREATE");

        // Static shout out mister David Wang pair programming ftw
        dict2 = Domains.init();
        Log.d(TAG, "onCreate: we saved our dict2 lez see wat hapn " + dict2.size());
        Log.d("onCreate", "onCreate");
    }

    // todo why the fuck does my github porNo.js page crash whenever it wants to
    // https://stackoverflow.com/questions/38783205/android-read-google-chrome-url-using-accessibility-service
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.d(TAG, "onAccessibilityEvent: event = " + event);
//                Log.d(TAG, "onAccessibilityEvent: className = " + event.getClassName());

        if (event.getPackageName() != null && event.getPackageName().toString().contains("com.android.chrome")) {
            String eventType = AccessibilityEvent.eventTypeToString(event.getEventType());

//            time = System.currentTimeMillis();
//                Log.d(TAG, "onAccessibilityEvent: event = " + event);
//                Log.d(TAG, "onAccessibilityEvent: className = " + event.getClassName());

            // The user opens a URL from a different source (ie hyperlink, URL in SMS message...)
            if (eventType.contains("WINDOW")) {
                String className = event.getClassName().toString();


//                Log.d(TAG, "onAccessibilityEvent: event = " + event);
//                Log.d(TAG, "onAccessibilityEvent: className = " + className);

                // No null check cuz event.getClassName() will never return null...thank you Android <3

                ////////////////////////////////////////////////////
                // 6 out of 6 setup
                // 4 out of 6 is/are correct
                // $$ = detects in regular mode
                // ^^ = redirects in regular mode
                // ## = detects in incognito
                // ** = redirects in incognito

                // $$ Clicking a hyperlink on a webpage
                // $$ ^^ Navigating using Android back button ## ^^ COULD BE FASTER
                // $$ ^^ Navigating using Chrome forward navigation button ## ^^ COULD BE FASTER
                if (className.equals("android.widget.EditText")) {
////                    // do nothing

//                    Log.d(TAG, "onAccessibilityEvent: inside ET $$$$$$");
                    dfs(event.getSource());
                }

                // $$ ^^ Hyperlink from external source, such as an sms msg (Can't test for incognito -- no
                //  "Open in incognito" option exists, that I've seen so far, thus we will say this is correct)
                // COULD BE FASTER
                // Triggers even when porn url is in the body of a website :O
                if (className.equals("org.chromium.chrome.browser.ChromeTabbedActivity")) {
//                    Log.d(TAG, "onAccessibilityEvent: event = " + event);
//
                    dfs(event.getSource());
                }

                // $$ ^^ Typing the URL in ## **
                // $$ ^^ Pasting the URL in ## ** (works through TYPE_VIEW_TEXT events)
                // We have a source, so we can query
                // Omnibox sits in the class android.widget.ListView
                // We specify this class because a porn URL embedded in a webpage (even as just plaintext) will trigger
                //  the redirect. However! Webpage data is in the class android.widget.FrameLayout, so we can avoid that class
                if (className.equals("android.widget.ListView")){
                    // Trying to use the nodeID won't work -- the ids keep changing (probably for good reason)
                    // So much for saving cycles -- just check all window events, sorry phone

                    // Can't use .getText() strategy -- WINDOW event metadata doesn't contain URL info
                    //  when opened through hyperlinks therefore dfs() we go
                    dfs(event.getSource());
                }
            }

            // If the user is typing in the omnibox,
            else if (eventType.contains("TYPE_VIEW_TEXT")) {
                String text = event.getText().toString();
                // Nothin 2 do
                if (text == null || text.length() < 3) {
                    // Do nothing
                }
                // We have some text!
                else {
                    while (text.contains(" ")) {
                        text = text.replaceAll(" ", "");
                    }

                    Log.d(TAG, "onAccessibilityEvent: our text is " + text);

                    if (porNo.isPornDomain(text)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getRandomURL()));
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

//                        Log.d(TAG, "dfs: Speed = " + (System.currentTimeMillis() - time));
//                        dfs(src);
                    }

                    // cuz omni not needed to evaluate just text
                    if (event.getSource() != null) {
                        String src = event.getSource().toString();
                        String current = getId(src);

//                        Log.d(TAG, "onAccessibilityEvent: current = " + current);

                        // Save ID of omnibox
                        if (!omnibox.equals(current)) {
//                            Log.d(TAG, "onAccessibilityEvent: UPDATE -- " + omnibox);
                            omnibox = current;
                        }

//                        Log.d(TAG, "onAccessibilityEvent: from TYPE_VIEW_TEXT: " + event.getSource());
                    }
                }
            }
        }

//
//            // User is using Samsung Internet
//            else if (event.getPackageName().toString().contains("com.sec.android.app.sbrowser")) {
//                // implement()
//            }

    }

    public void dfs(AccessibilityNodeInfo info){
        if (info == null) {
            return;
        }

        if (info.getText() != null && info.getText().toString().trim().length() > 0) {
            String txt = info.getText().toString().trim();
            txt = txt.toLowerCase();

//            Log.d(TAG, "dfs: the text is " + txt);


            if (txt.contains(" ")) {
                return;
            }

            if (!txt.contains(".")) {
                return;
            }

            // Ensure we only check the omnibox
            // If omnibox is zz, it's uninitialized, so proceed with dfs
            // Otherwise, we have a value for omnibox, so this evaluates
            //  to true when we have a node that isn't the omnibox
            if (!omnibox.equals("zz") && !getId(info.toString()).equals(omnibox)) {
//                Log.d(TAG, "dfs: rip... info:omnibox = " + getId(info.toString()) + " : " + omnibox);

                return;
            }

            // Else, let's check this out
            String host = getHostName(txt);
            Log.d(TAG, "dfs: the URL is " + txt);
            Log.d(TAG, "dfs: the URL, thru URI, is " + host);

            // Is the txt a banned URL?
            if (porNo.isPorn(txt)) {
//                Log.d(TAG, "dfs: source info =  " + info);

//            if (txt.equals("yahoo.com")) {

                    // Attempting direct redirection
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getRandomURL()));

                    // Will this open in the current tab -- yes, but not before the porn site finishes loading smh
                    // about:blank can be thought of as window.stop()
                    // Thank you, https://android.stackexchange.com/questions/189074/android-chrome-browser-how-to-make-it-always-open-to-blank-page-and-new-tab-o
                    // First, we "stop" the page load of the porn site....
                // Why is hugesex.tv so fucking fast wtffffff
                // Todo: Clicking the back button to visit a porn site completely bypasses our url detection
                // fuck u stop watching porn >:(
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("about:blank"));
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

//                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getRandomURL()));
//                startActivity(intent);


//                    Log.d(TAG, "dfs: Speed = " + (System.currentTimeMillis() - time));
                    return;
//                }
            }
        }

        for (int i = 0 ; i < info.getChildCount(); i++) {
//            Log.d(TAG, "onAccessibilityEvent: Iteration " + i + "/" + info.getChildCount());

            AccessibilityNodeInfo child = info.getChild(i);
            dfs(child);

            if (child != null) {
                child.recycle();
            }
        }
    }

    /*
     * Get the associated nodeId of the passed in event source
     */
    public String getId(String src) {
        int start = src.indexOf("@");
        int stop = src.indexOf(";");
        
        return src.substring(start, stop);
    }


    public String getRandomURL() {
        File filesDir = getFilesDir();
        ArrayList<String> items;

//        Log.d("dawg", filesDir.toString());
        File todoFile = new File(filesDir, "todo.txt");

        // Get our saved urls
        try {
            items = new ArrayList<String>(FileUtils.readLines(todoFile));
        } catch (IOException ioe) {
            return "https://fightthenewdrug.com";
        }

//        Log.d("dawg", items.toString());


        // Select a url at random and parse it
        int random = (int) Math.floor(Math.random() * items.size());

        String item = items.get(random);
        int idxPipe = item.indexOf('|');
        String url = item.substring(0, idxPipe - 1);

//        Log.d("dawg", url);

        return url;
    }

    // Thank you, https://stackoverflow.com/questions/23079197/extract-host-name-domain-name-from-url-string/23079402
    public String getHostName(String url) {
        URI uri = null;
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

        return hostName;
    }


    @Override
    public void onInterrupt() {
    }
}


//    // View the actions
//    // URLs embedded on the page do not have (hoping) the ACTION_SELECT
//    List<AccessibilityNodeInfo.AccessibilityAction> actions = info.getActionList();
//
//// Iterate through our actions and look for the ACTION_SELECT
//                for (int i = 0; i < actions.size(); i++) {
//        String action = actions.get(i).toString();
//
//        // Seems like we found our action...which means we have our page url...print the url
//        if (action.contains(" ACTION_SELECT ")) {
////                        System.out.println(txt);
//        Log.d("dawg", "action select");
//
//        if (isPorn(txt)) {
//        Log.d("dawg", "porNo!");
//
//        // Put me in my own methoddddddddddd
//        String randomURL = getRandomURL();
//
//        // [From isPorn()]...then we porNo!
////                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(randomURL));
////                            intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
////                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                            startActivity(intent);
//
//        // Thank you, https://stackoverflow.com/questions/2201917/how-can-i-open-a-url-in-androids-web-browser-from-my-application
//        // How to change url within tab or even close the current tab and open a new one????
//        // Fast as fuck BUT opens a new tab while the porn tab still exists in the background...how to close the porn tab?
//        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(randomURL));
//        startActivity(browserIntent);
//
//        }
//        }
//        }
//
////                System.out.println("ACTIONS:: " + info.getActionList());
