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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

// Todo:

// BIG THANK YOUs TO https://stackoverflow.com/questions/38783205/android-read-google-chrome-url-using-accessibility-service
// and https://stackoverflow.com/questions/42125940/how-to-use-accessibility-services-for-taking-action-for-users
public class MyAccessibilityService extends AccessibilityService {
    static Map<String, Boolean> dict2 = new HashMap<>();

    static boolean isFound = false;
    static String currURL = "zz";

    static String TAG = "dawgAccessibility";
    private String omnibox = "zz";

    @Override
    public void onCreate() {
        MainActivity.readDB();
        super.onCreate();

        // Static shout out mister David Wang pair programming ftw
        dict2 = Domains.init();
//        Log.d(TAG, "onCreate: we saved our dict2 lez see wat hapn " + dict2.size());
//        Log.d("onCreate", "onCreate");
    }

    // NAW IT FIXED clicking hyperlink from google results page bypasses algo
    // NAW IT FIXED and so does using the back button to move backwards after having been redirected
    // https://stackoverflow.com/questions/38783205/android-read-google-chrome-url-using-accessibility-service
    public void onAccessibilityEvent(AccessibilityEvent event) {
//                Log.d(TAG, "onAccessibilityEvent: Looking for touch event = " + event);

        // Chrome support
        if (event.getPackageName() != null && event.getPackageName().toString().contains("com.android.chrome")) {
            String eventType = AccessibilityEvent.eventTypeToString(event.getEventType());

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

                // $$ Clicking a hyperlink on a webpage ## **
                // $$ ^^ Navigating using Android back button ## ^^ COULD BE FASTER
                // $$ ^^ Navigating using Chrome forward navigation button ## ^^ COULD BE FASTER
                if (className.equals("android.widget.EditText")) {
////                    // do nothing

//                    Log.d(TAG, "onAccessibilityEvent: inside ET $$$$$$");
                    dfs(event.getSource());
                }

                // $$ ^^ Hyperlink from external source, such as an sms msg (Can't test for incognito -- no
                //  "Open in incognito" option exists, that I've seen so far, thus we will say this is correct)
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

                    text = text.substring(1, text.length() - 1);
                    text = getHostName(text);

//                    Log.d(TAG, "onAccessibilityEvent: our text is " + text);

                    if (porNo.isPornDomain(text)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getRandomURL()));
                        intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

//                        Log.d(TAG, "dfs: Speed = " + (System.currentTimeMillis() - time));
                    }

                    // Save the resourceID for the omnibox to reduce some cycles
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

            // If we have a non-zz value for the omnibox AND the current node corresponds to
            //  the omnibox, then return to prevent rest of dfs -- omnibox gets searched in code more above
            //
            // If omnibox is zz, it's uninitialized, so proceed with dfs
            // Otherwise, we have a value for omnibox, so this evaluates
            //  to true when we have a node that isn't the omnibox
            if (!omnibox.equals("zz") && !getId(info.toString()).equals(omnibox)) {
//                Log.d(TAG, "dfs: rip... info:omnibox = " + getId(info.toString()) + " : " + omnibox);

                return;
            }

            // Else, let's check this out
            String host = getHostName(txt);

            // If we have the redirect url, we can start processing stuff again
            // Otherwise, check if we are still in the REDIRECTION state
//            Log.d(TAG, "dfs: host -- currURL : " + host + " -- " + currURL);
            
            if (host.contains(currURL)) {
//                Log.d(TAG, "dfs: host does equal currURL");
                isFound = false;
            }
            if (isFound) {
//                Log.d(TAG, "dfs: isFound evaluated to true: host - currURL = " + host + " - " + currURL);
                return;
            }

//            Log.d(TAG, "dfs: the URL is " + txt);
//            Log.d(TAG, "dfs: the URL, thru URI, is " + host);
//            Log.d(TAG, "isFound = " + isFound);

            // Is the txt a banned URL?
            if (porNo.isPorn(host)) {
                isFound = true;

//                Log.d(TAG, "dfs: source info =  " + info);

//            if (txt.equals("yahoo.com")) {

                    // Attempting direct redirection
                    String randomURL = getRandomURL();
                    currURL = getHostName(randomURL);

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(randomURL));


                    // First, we "stop" the page load of the porn site....
                // Why is hugesex.tv so fucking fast wtffffff
                // NAW FIXEDDDDD: Clicking the back button to visit a porn site completely bypasses our url detection
                // fuck u stop watching porn >:(
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);


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
        return Utilities.getRandomURL();
    }

    // Thank you, https://stackoverflow.com/questions/23079197/extract-host-name-domain-name-from-url-string/23079402
    public static String getHostName(String url) {
        URI uri;
//        Log.d(TAG, "getHostName: url = " + url);

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
            hostName = hostName.substring(7);
        }
        else if (hostName.contains("m.")) {
            hostName = hostName.substring(2);
        }

        // Fuck you no path allowed
        if (hostName.contains("/")) {
            return hostName.substring(0, hostName.indexOf("/"));
        }

//        Log.d(TAG, "getHostName: url++ = " + hostName);
        return hostName;
    }


    @Override
    public void onInterrupt() {
    }
}
