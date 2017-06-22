package softmates.ourspot;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by elvinaslukasevicius on 08/06/2017.
 */

public class backgroundLocation extends Service {

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    final boolean[] overThirty = {false};
    final boolean[] underNine = {false};
    final boolean[] parked = {false};
    private double[][] blackList;
    final long[] createdMillis = {System.currentTimeMillis()};
    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;
        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            float speed = location.getSpeed() * 3600 / 1000;
            final String latLocation = ("" + location.getLatitude());
            final String longLocation = ("" + location.getLongitude());
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            Log.d("ThreadRunning", "Y");
            Log.d("LocLatitude", String.valueOf(mLastLocation.getLatitude()));
            Log.d("SpeedGps", String.valueOf(speed));
            Log.d("Speed", String.valueOf(mLastLocation.getSpeed()));
            if (speed > 30 && !overThirty[0]) {
                overThirty[0] = true;
                Log.d("overthirty", "Y");
            }
            if (speed < 4 && overThirty[0]) {
                parked[0] = true;
                Log.d("Parked", "Y");
            }
            if (overThirty[0] && speed < 9 && speed > 0 && !underNine[0] && parked[0]) {
                underNine[0] = true;
                Log.d("underNine", "Y");
                createdMillis[0] = System.currentTimeMillis();
            }
            if (underNine[0] && parked[0] && overThirty[0] && speed > 17) {
                underNine[0] = false;
                parked[0] = false;
                Log.d("TooFast", "Y");
            }
            if (underNine[0] && parked[0] && ((System.currentTimeMillis() - createdMillis[0]) / 1000) > 9) {

                overThirty[0] = false;
                underNine[0] = false;
                Log.d("Detected", "Y");
                if (notInBlackList(location)) {
                    NotificationUtils.displayNotification(getApplicationContext(), latLocation, longLocation);
                }
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    public boolean notInBlackList(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Log.d("BlackGPS",String.valueOf(latitude) + String.valueOf(longitude));
        double lat1;
        double lon1;
        Log.d("EnteredInBlack", "Y");
        Log.d("nullblacklsit",String.valueOf(blackList==null));
        boolean notBlackListed = true;
        if(blackList !=null) {
            Log.d("EnteredInBlack1", "Y");
            if (blackList.length != 0) {
                Log.d("EnteredInBlack2", "Y");
                for (int i = 0; i < blackList.length; i++) {
                    Log.d("EnteredInBlack3", "Y");
                    lat1 = blackList[i][0];
                    lon1 = blackList[i][1];
                    Log.d(String.valueOf(lat1),String.valueOf(lon1));
                    notBlackListed = ourSpot.distance(lat1, latitude, lon1, longitude) > 50;
                }
            }
        }

        return notBlackListed;
    }


    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        blackList = (double[][])intent.getExtras().getSerializable("blackList");
        Log.d("onStart", intent.toString());
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d("createeeeed", "should work");
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {

    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


}

