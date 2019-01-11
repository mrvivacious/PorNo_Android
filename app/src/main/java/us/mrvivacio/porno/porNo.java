// PorNo! Android
// porNo.java
// @author Vivek Bhookya

package us.mrvivacio.porno;

import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class porNo {
    private static String TAG = "dawg";
    // read me https://stackoverflow.com/questions/4833480/have-i-reached-the-limits-of-the-size-of-objects-javascript-in-my-browser-can-ha

    /*
     * Function isPorn
     * UNREFINED !! put me in another dedicated class of my own pleaseeeee
     * Checks only if the url contains any mention of a porn url
     * Ya boi Vivek out here writing a porn filter part 2!!!!!!!!!
     * @param url The url whose domain name we check against the porn sites
     */
    public static boolean isPorn(String url) {
        Map<String, Boolean> dict2 = MyAccessibilityService.dict2;

        url = url.trim().toLowerCase();

        if (url.length() < 4) {
            return false;
        }

        if (url.contains(" ")) {
            return false;
        }

        if (!url.contains(".")) {
            return false;
        }


        // Avoid fightthenewdrug and github
        if (!url.contains("fightthenewdrug") && !url.contains("github")) {
//            url = getHostName(url);
            Log.d(TAG, "isPorn: URL = " + url);
            Log.d(TAG, "isPorn: dict2 get = " + dict2.get(url));


            // O(n) worst case feels bad but whO(l)esome porn-checker feels good
            if (dict2.get(url) != null) {
                return true;
            }

        }

        // Inconclusive
        return false;
    }

    public static boolean isPornDomain(String url) {
        long old = System.currentTimeMillis();
        Map<String, Boolean> dict2 = MyAccessibilityService.dict2;

        // Strip mobile. or m.
        int stop = url.length();
        url = url.trim().toLowerCase();

        // 4 because p.co (example) could be a real porn site and don't wanna skip those
        if (url.length() < 4) {
            return false;
        }


        // Return false for these URLs to avoid disrupting browsing experience
        // ie trying to look at porNo.js on my Github shouldn't trigger lmao
        if (    url.contains("fightthenewdrug") ||
                url.contains("github") ||
                url.contains("chrome")   ) {
            return false;
        }

        // Avoid fightthenewdrug and github
        else {
            url = url.substring(1);
            url = url.substring(0, url.length() - 1);

            Log.d(TAG, "isPornDomain: URL = " + url);

            // O(n) worst case feels bad but whO(l)esome porn-checker feels good
            Log.d(TAG, "isPornDomain: dict2.size = " + dict2.size());

            Log.d(TAG, "isPornDomain: dict2 " + dict2.get(url));
            if (dict2.get(url) != null) {
                Log.d(TAG, "isPornDomain: only took " + (old - System.currentTimeMillis()));
                return true;
            }

//            for (int i = 0; i < Domains.domains.length; i++) {
//                if (url.contains(Domains.domains[i])) {
//                    // GET THE FUCK OUTTTTTTTTTTTTTTT
////                    Log.d(TAG, "isPorn: TRUE");
//
//                    return true;
//                }
//            }
        }

        // Inconclusive
        return false;
    }

    public static String getHostName(String url) {
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

}
