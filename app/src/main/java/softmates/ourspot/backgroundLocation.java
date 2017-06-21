package softmates.ourspot;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.security.Timestamp;

/**
 * Created by elvinaslukasevicius on 08/06/2017.
 */

public class backgroundLocation extends Service {


    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private double[][] blackList;
    final boolean[] overThirty = {false};
    final boolean[] underNine = {false};
    final boolean[] parked = {false};
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
                //over 30km/h
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
                //if (notInBlackList(ourSpot.getBlacklist(), location)) {
                NotificationUtils.displayNotification(getApplicationContext(), latLocation, longLocation);
                //}



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

    public boolean notInBlackList(double[][] blackList, Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Log.d("BlackGPS",String.valueOf(latitude) + String.valueOf(longitude));
        double lat1;
        double lon1;
        Log.d("EnteredInBlack", "Y");
        boolean blackListed = true;
        if(blackList !=null) {
            Log.d("EnteredInBlack1", "Y");

            if (blackList.length != 0) {
                Log.d("EnteredInBlack2", "Y");
                for (int i = 0; i < blackList.length; i++) {
                    Log.d("EnteredInBlack3", "Y");
                    lat1 = blackList[i][0];
                    lon1 = blackList[i][1];
                    blackListed = ourSpot.distance(lat1, latitude, lon1, longitude) < 50;
                    Log.d("BlackDistance", String.valueOf(ourSpot.distance(lat1, latitude, lon1, longitude) < 50));
                }
            }
        }

        return blackListed;
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
       /* Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }*/
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

 /*   public void dialog(final String latLocation, final String longLocation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(backgroundLocation.this);
        builder.setMessage("Are there any parking space around?");
        //Button One : Yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    new connectionMng().sendLocation(latLocation, longLocation, "Y", ourSpot.id(getApplicationContext()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Thank You, Your submission has been added.", Toast.LENGTH_LONG).show();
            }
        });
        //Button Two : No
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    new connectionMng().sendLocation(latLocation, longLocation, "N", ourSpot.id(getApplicationContext()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Thank You, Your submission has been added.", Toast.LENGTH_LONG).show();
                dialog.cancel();
            }
        });
        //Button Three : Neutral
        builder.setNeutralButton("Don't ask again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO Add to blacklist
                Toast.makeText(getApplicationContext(), "Place is added to our blacklist", Toast.LENGTH_LONG).show();
                dialog.cancel();
            }
        });
        AlertDialog diag = builder.create();
        diag.show();
    }*/

    public void dialog(String a, String b) throws InterruptedException {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(backgroundLocation.this);
        builder.setMessage("Are there any parking space around?");
        //Button One : Yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(backgroundLocation.this, "Thank You, Your submission has been added.", Toast.LENGTH_LONG).show();
            }
        });
        //Button Two : No
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(backgroundLocation.this, "No button Clicked!", Toast.LENGTH_LONG).show();
                //dialog.cancel();
            }
        });
        //Button Three : Neutral
        builder.setNeutralButton("Don't ask again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(backgroundLocation.this, "Place is added to our blacklist", Toast.LENGTH_LONG).show();
                //dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        //AlertDialog.Builder dialog  = new AlertDialog.Builder(getActivity());
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();*/
    }
}

