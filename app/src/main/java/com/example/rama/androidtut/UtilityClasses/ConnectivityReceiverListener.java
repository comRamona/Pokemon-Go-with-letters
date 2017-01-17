package com.example.rama.androidtut.UtilityClasses;

/**
 * Interface to implement for classes which monitor network connection status
 */

public interface ConnectivityReceiverListener {
    void onNetworkConnectionChanged(boolean isConnected);
}
