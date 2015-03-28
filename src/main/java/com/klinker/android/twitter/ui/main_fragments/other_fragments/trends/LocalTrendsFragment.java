package com.klinker.android.twitter.ui.main_fragments.other_fragments.trends;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.klinker.android.twitter.R;
import com.klinker.android.twitter.ui.drawer_activities.DrawerActivity;
import com.klinker.android.twitter.utils.Utils;
import twitter4j.GeoLocation;
import twitter4j.ResponseList;
import twitter4j.Trends;
import twitter4j.Twitter;

public class LocalTrendsFragment extends TrendsFragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean connected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        buildGoogleApiClient();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    Location mLastLocation;

    @Override
    public void onConnected(Bundle bundle) {
        Log.v("location", "connected");
        connected = true;
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        mGoogleApiClient.disconnect();
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(context, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
    }


    @Override
    protected Trends getTrends() {
        try {
            Twitter twitter = Utils.getTwitter(context, DrawerActivity.settings);

            Trends trends;

            if (sharedPrefs.getBoolean("manually_config_location", false)) {
                trends = twitter.getPlaceTrends(sharedPrefs.getInt("woeid", 2379574)); // chicago to default
            } else {

                mGoogleApiClient.connect();
                connected = false;

                int i = 0;
                while (!connected && i < 5) {
                    try {
                        Thread.sleep(1500);
                    } catch (Exception e) {

                    }

                    i++;
                }

                Location location = mLastLocation;

                ResponseList<twitter4j.Location> locations = twitter.getClosestTrends(new GeoLocation(location.getLatitude(), location.getLongitude()));
                trends = twitter.getPlaceTrends(locations.get(0).getWoeid());
            }

            return trends;
        } catch (Exception e) {
            return null;
        }
    }
}
