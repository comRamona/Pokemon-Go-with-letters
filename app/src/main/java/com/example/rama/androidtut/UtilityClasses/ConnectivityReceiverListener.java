package com.example.rama.androidtut.UtilityClasses;

/**
 * Interface for acting when the status of the network connection has changed
 */

public interface ConnectivityReceiverListener {
    void onNetworkConnectionChanged(boolean isConnected);
}
