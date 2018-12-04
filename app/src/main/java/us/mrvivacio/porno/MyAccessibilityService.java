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

import java.util.List;


// BIG THANK YOUs TO https://stackoverflow.com/questions/38783205/android-read-google-chrome-url-using-accessibility-service
// and https://stackoverflow.com/questions/42125940/how-to-use-accessibility-services-for-taking-action-for-users
public class MyAccessibilityService extends AccessibilityService {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyAccessibilityService", "onCreate");
    }

    // https://stackoverflow.com/questions/38783205/android-read-google-chrome-url-using-accessibility-service
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(AccessibilityEvent.eventTypeToString(event.getEventType()).contains("WINDOW")){
//            Log.d("fuk", "CONTAINS WINDOWWWWWW");

            AccessibilityNodeInfo nodeInfo = event.getSource();
            dfs(nodeInfo);
        }
    }

    public void dfs(AccessibilityNodeInfo info){
        if (info == null)
            return;

        if (info.getText() != null && info.getText().length() > 0) {
            String txt = info.getText().toString();
            txt = txt.toLowerCase();

            // Look for all urls
            if (txt.contains("http")) {
                // View the actions
                // URLs embedded on the page do not have (hoping) the ACTION_SELECT
                List<AccessibilityNodeInfo.AccessibilityAction> actions = info.getActionList();

                // Iterate through our actions and look for the ACTION_SELECT
                for (int i = 0; i < actions.size(); i++) {
                    String action = actions.get(i).toString();

                    // Seems like we found our action...which means we have our page url...print the url
                    if (action.contains(" ACTION_SELECT ")) {
//                        System.out.println(txt);

                        if (isPorn(txt)) {
//                            System.out.println("CONTAINS PORNNNNNN");

                            // DEMO DATAAAAAAAAA put me in my own methoddddddddddd
                            String [] urls = {
                                    "http://www.github.com/mrvivacious", "http://www.soundcloud.com/verb-sap-a",
                                    "https://mrvivacious.github.io/ido.html#projects", "https://chrome.google.com/webstore/detail/porno-beta/fkhfpbfakkjpkhnonhelnnbohblaeooj"
                            };

                            int random = (int) Math.floor(Math.random() * urls.length);
                            String randomURL = urls[random];

                            // [From isPorn()]...then we porNo!
//                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(randomURL));
//                            intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);

                            // Thank you, https://stackoverflow.com/questions/2201917/how-can-i-open-a-url-in-androids-web-browser-from-my-application
                            // How to change url within tab or even close the current tab and open a new one????
                            // Fast as fuck BUT opens a new tab while the porn tab still exists in the background...how to close the porn tab?
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(randomURL));
                            startActivity(browserIntent);

                        }
                    }
                }

//                System.out.println("ACTIONS:: " + info.getActionList());

                return;
            }
        }

        for(int i=0;i<info.getChildCount();i++){
            AccessibilityNodeInfo child = info.getChild(i);
            dfs(child);
            if(child != null){
                child.recycle();
            }
        }
    }

    // Function isPorn
    // UNREFINED !! put me in another dedicated class of my own pleaseeeee
    // Checks only if the url contains any mention of a porn url
    // Ya boi Vivek out here writing a porn filter part 2!!!!!!!!!
    // @param url The url whose domain name we check against the porn sites
    public boolean isPorn(String url) {
        // Strip mobile. or m.
        int stop = url.length();

        // Avoid fightthenewdrug and github
        if (!url.contains("fightthenewdrug") && !url.contains("github")) {

            // O(n) worst case feels bad but whO(l)esome porn-checker feels good
            for (int i = 0; i < porNo.pornLinks.length; i++) {
                if (url.contains(porNo.pornLinks[i])) {
                    // GET THE FUCK OUTTTTTTTTTTTTTTT

                    // will this open in the current tab -- yes, but not before the porn site finishes loading smh
                    // about:blank can be thought of as window.stop()
                    // Thank you, https://android.stackexchange.com/questions/189074/android-chrome-browser-how-to-make-it-always-open-to-blank-page-and-new-tab-o
                    // First, we "stop" the page load of the porn site....
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("about:blank"));
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    return true;
                }
            }
        }

        // Inconclusive
        return false;
    }

    @Override
    public void onInterrupt() {
    }
}
