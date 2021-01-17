package com.example.easytripplanner.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import timber.log.Timber;

public class NetworkMonitorUtil {
    private static final String TAG = "NetworkMonitorUtil";
    public static boolean networkStatus;
    private static ConnectivityManager connectivityManager;
    private static ConnectivityManager.NetworkCallback networkCallback;


    private static ConnectivityManager.NetworkCallback getNetworkCallbackInstance() {
        if (networkCallback == null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    Timber.i("The default network is now: %s", network);
                    networkStatus = true;
                }

                @Override
                public void onLost(Network network) {
                    networkStatus = false;
                    Timber.i("The application no longer has a default network. The last default network was: %s", network);
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    Timber.i("The default network changed capabilities: %s", networkCapabilities);
                    // WIFI or CELLULAR
                    networkStatus = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                }

                @Override
                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                    Timber.i("The default network changed link properties: %s", linkProperties);
                }
            };
        }
        return networkCallback;
    }


    public static void startTriggerNetwork(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            connectivityManager = context.getSystemService(ConnectivityManager.class);

            if (connectivityManager.getActiveNetwork() == null) {
                // UNAVAILABLE
                networkStatus = false;
                return;
            }
            connectivityManager.registerDefaultNetworkCallback(getNetworkCallbackInstance());

        } else {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                activeNetwork.isConnectedOrConnecting();
                networkStatus = true;
            } else networkStatus = false;
        }
    }

    public static void stopTriggerNetwork() {
        if (connectivityManager != null)
            connectivityManager.unregisterNetworkCallback(getNetworkCallbackInstance());
        networkCallback = null;
        connectivityManager = null;
    }


}
