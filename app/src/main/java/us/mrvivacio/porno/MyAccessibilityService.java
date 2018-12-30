// PorNo! Android
// MyAccessibilityService.java
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
import java.util.ArrayList;


// BIG THANK YOUs TO https://stackoverflow.com/questions/38783205/android-read-google-chrome-url-using-accessibility-service
// and https://stackoverflow.com/questions/42125940/how-to-use-accessibility-services-for-taking-action-for-users
public class MyAccessibilityService extends AccessibilityService {
    static String TAG = "dawgAccessibility";
    private long time;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyAccessibilityService", "onCreate");
    }

    // https://stackoverflow.com/questions/38783205/android-read-google-chrome-url-using-accessibility-service
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Faster way to search events for porn url?
        // Let's make the number of events we have to search as small as possible
        // Of those events, search for url as quickly as possible

        if (event.getPackageName() != null && event.getPackageName().toString().contains("com.android.chrome")) {
            String eventType = AccessibilityEvent.eventTypeToString(event.getEventType());

//            time = System.currentTimeMillis();

            // The user opens a URL from a different source (ie hyperlink, URL in SMS message...)
            if (eventType.contains("WINDOW")) {
                String className = event.getClassName().toString();

//                Log.d(TAG, "onAccessibilityEvent: event.class = " + event.getClassName());
                Log.d(TAG, "onAccessibilityEvent: className = " + className);

                // No null check cuz event.getClassName() will never return null...thank you Android <3
                Log.d(TAG, "onAccessibilityEvent: className = " + className);

                // 6 out of 6

                // $$ Clicking a hyperlink on a webpage
                // $$ Navigating using Android back button
                // $$ Navigating using Chrome forward navigation button
                if (className.equals("android.widget.EditText")) {
//                    // do nothing
//                    Log.d(TAG, "onAccessibilityEvent: event = " + event);
//
                    dfs(event.getSource());
                }

                // $$ Hyperlink from external source, such as an sms msg
                if (className.equals("org.chromium.chrome.browser.ChromeTabbedActivity")) {
//                    Log.d(TAG, "onAccessibilityEvent: event = " + event);
//
//                    dfs(event.getSource());
                }

                // $$ Typing the URL in
                // $$ Pasting the URL in
                // We have a source, so we can query
                // Omnibox sits in the class android.widget.ListView
                // We specify this class because a porn URL embedded in a webpage (even as just plaintext) will trigger
                //  the redirect. However! Webpage data is in the class android.widget.FrameLayout, so we can avoid that class
                if (className.equals("android.widget.ListView")){
                    // Trying to use the nodeID won't work -- the ids keep changing (probably for good reason)
                    // So much for saving cycles -- just check all window events, sorry phone

                    // Can't use .getText() strategy -- WINDOW event metadata doesn't contain URL info
                    //  when opened through hyperlinks therefore dfs() we go
//                    dfs(event.getSource());
                }

            }

            // If the user is typing in the omnibox,
            // TODO: Add handling to ensure the URL found is the omnibox...the resource id keeps changing UGHHHHHHHH
//            else if (eventType.contains("TEXT")) {
//                AccessibilityNodeInfo src = event.getSource();
//
//                // Nothin 2 do
//                if (src == null) {
//                    // Do nothing
//                }
//                // We have a source, so we can query
//                else {
//                    // Get the nodeInfo of the sender of this event
//                    String nodeInfo = src.toString().substring(48, src.toString().indexOf(';'));
//
//                    // The nodeId of the Chrome omnibox seems to begin with this for TEXT events
//                    // Save cycles by DFSing only on the events directly from the omnibox
////                    if (nodeInfo.contains("@75")) {
//                        Log.d(TAG, "onAccessibilityEvent TEXT__: src.NodeInfo = " + nodeInfo);
//                        Log.d(TAG, "onAccessibilityEvent: TEXT__: event.getTxt = " + event.getText());
//
//                        if (event.getText().toString().contains("yahoo.com")) {
//                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("about:blank"));
//                            intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//
//                            Log.d(TAG, "dfs: Speed = " + (System.currentTimeMillis() - time));
////                        }
////                        dfs(src);
//                    }
//                }
//            }
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

        if (info.getText() != null && info.getText().length() > 0) {
            String txt = info.getText().toString();
            txt = txt.toLowerCase();
            Log.d(TAG, "dfs: the text is " + txt);


//            if (txt.equals("about:blank")) {
//                Log.d(TAG, "dfs: it g ma");
//
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getRandomURL()));
//                intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }

            // Is the txt a banned URL?
//            if (porNo.isPorn(txt)) {
            if (txt.equals("yahoo.com")) {
                    Log.d(TAG, "dfs: the text is " + txt);

                    // Attempting direct redirection
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getRandomURL()));
//                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);

//                    // will this open in the current tab -- yes, but not before the porn site finishes loading smh
//                    // about:blank can be thought of as window.stop()
//                    // Thank you, https://android.stackexchange.com/questions/189074/android-chrome-browser-how-to-make-it-always-open-to-blank-page-and-new-tab-o
                    // First, we "stop" the page load of the porn site....
                // Why is hugesex.tv so fucking fast wtffffff
                // Todo: Clicking the back button to visit a porn site completely bypasses our url detection
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("about:blank"));
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

        Log.d("dawg", url);

        return url;
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
