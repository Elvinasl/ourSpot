package softmates.ourspot;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ourSpot extends FragmentActivity implements OnMapReadyCallback,
        //This callback will have a public function onConnected() which will be called whenever device is connected and disconnected.
        GoogleApiClient.ConnectionCallbacks,
        //Provides callbacks for scenarios that result in a failed attempt to connect the client to the service.
        // Whenever connection is failed onConnectionFailed() will be called.
        GoogleApiClient.OnConnectionFailedListener,
        //This callback will be called whenever there is change in location of device. Function onLocationChanged() will be called.
        View.OnClickListener,
        //For button
        LocationListener
{
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private softmates.ourspot.connectionMng conn = new softmates.ourspot.connectionMng();
    private ArrayList<Submission> SubmissionArray = new ArrayList<Submission>();
    private ArrayList<ParkingLots> OwnerArray = new ArrayList<ParkingLots>();
    int REQUEST_CHECK_SETTINGS = 100;
    private int PROXIMITY_RADIUS = 10000;
    private static String sID;
    private static final String INSTALLATION = "INSTALLATION";
    private Timer timerExecutor = new Timer();
    private TimerTask doAsynchronousTaskExecutor;
    private LocationManager mLocationManager;
    private LocationProvider locationProvider;
    private LocationListener locationListener;
    private LocationRequest mLocationRequest;
    private static double[][] blackList;
    private static Context context;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        //insert check for gps enabled
        Context activity = this;
        context = getBaseContext();
        //startService(new Intent(this, backgroundLocation.class));
        ourSpot.mContext = getApplicationContext();
        setContentView(R.layout.activity_our_spot);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //empty button
        Button mClickButton1 = (Button)findViewById(R.id.btnEmpty);
        mClickButton1.setOnClickListener(this);
        //directions button
        Button mClickButton2 = (Button)findViewById(R.id.btnDirections);
        mClickButton2.setOnClickListener(this);
        //taken button
        Button mClickButton3 = (Button)findViewById(R.id.btnTaken);
        mClickButton3.setOnClickListener(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //setBlacklist();
        activity.startService(new Intent(activity,
                backgroundLocation.class));

    }
    public static Context getAppContext() {
        return ourSpot.mContext;
    }
    public static String getID(){
        return ourSpot.sID;
    }
    public void setBlacklist(){
        JSONArray Jarray = null;
        Log.d("Iscontext", String.valueOf(this==null));
        try {
            Jarray = conn.getBlacklist(id(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(Jarray!=null) {
            double [][] blackListTemp= new double [Jarray.length()][2];
            if (Jarray.length() != 0) {
                for (int i = 0; i < Jarray.length(); i++) {
                    try {
                        blackListTemp[i][0] = Jarray.getJSONObject(i).getDouble("Latitude");
                        blackListTemp[i][1] = Jarray.getJSONObject(i).getDouble("Longitude");
                        Log.d("BlackLat", String.valueOf(blackListTemp[i][0]));
                        Log.d("BlackLong", String.valueOf(blackListTemp[i][1]));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            blackList = blackListTemp;
        }

    }
    public static synchronized double[][] getBlacklist(){
        Log.d("blacklistssss",ourSpot.blackList.toString());
        return ourSpot.blackList;
    }
    //This function is called when map is ready to be used. Here we can add all markers, listeners and other functional attributes.
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setPadding(0, 0, 0, 110);
        //Initialize Google Play Services

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }
    //to intialize Google Play Services.
    protected synchronized void buildGoogleApiClient()
    {
        //used to configure client.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                //provides callbacks that are called when client connected or disconnected.
                .addConnectionCallbacks(this)
                //covers scenarios of failed attempt of connect client to service.
                .addOnConnectionFailedListener(this)
                //adds the LocationServices API endpoint from Google Play Services.
                .addApi(LocationServices.API)
                .build();
        //client must be connected before excecuting any operation.
        mGoogleApiClient.connect();
    }

    //Fused Location Provider analyses GPS, Cellular and Wi-Fi network location data in order to provide the highest accuracy data.
    // It uses different device sensors to define if a user is walking, riding a bicycle, driving a car or just standing in order to adjust the frequency of location updates.
    // It helps in android location tracking. We will use this to get the last updated location.
    @Override
    public void onConnected(Bundle bundle)
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setSmallestDisplacement(0);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // Location request builder
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        builder.build()
                );

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(ourSpot.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i)
    {
    }

    //What to do whenever user location change.
    // Function onLocationChanged  will be called as soon as user location change.
    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if (mCurrLocationMarker != null)
        {

            mCurrLocationMarker.remove();
        }


        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //move map camera
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.5f));
        //stop location updates
        if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        try {
            populateMap();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //show nearby parkings
        gerNearbyParkings();
        getOwnerLots();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }
    //to ask user for a permission one-by-one at runtime.
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION))
            {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else
        {
            return true;
        }
    }
    //A dialog box is presented whenever any App requests permissions. When the user responds, the system invokes app’s
    // onRequestPermissionsResult() method, passing it the user response. Our app has to override this method to find out
    // whether the permission was granted.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void populateMap() throws JSONException
    {
        // Clear submission array and the map with a delay of 0.2 seconds for syncing purposes
        try {
            if(mLastLocation!=null){
                gerNearbyParkings();
                getOwnerLots();
            }
            mMap.clear();
            SubmissionArray.clear();
            Thread.sleep(200);
        } catch(InterruptedException ex) {
            // intrreputed
        }

        //Populate Submission Array with submissions
        JSONArray Jarray = conn.getTable(mLastLocation);
        /*if(Jarray.length() > 0)
        {

            //TODO: Why for???????????????????????????
        }*/
        for (int i = 0; i < Jarray.length(); i++)
        {
            try
            {
                SubmissionArray.add(new Submission(
                        Jarray.getJSONObject(i).getDouble("Latitude"),
                        Jarray.getJSONObject(i).getDouble("Longitude"),
                        Jarray.getJSONObject(i).getString("Available"),
                        Jarray.getJSONObject(i).getString("Date")));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        //Populate map with markers
        for (Submission submissionToAdd : SubmissionArray)
        {
            if(submissionToAdd.getEmpty())
            {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(submissionToAdd.getLatitude(), submissionToAdd.getLongitude()))
                        .title("Available")
                        .snippet(submissionToAdd.getTimeSpan())
                        .icon(BitmapDescriptorFactory.defaultMarker(125)));
            }
            else
            {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(submissionToAdd.getLatitude(), submissionToAdd.getLongitude()))
                        .title("Taken")
                        .snippet(submissionToAdd.getTimeSpan()));
            }
        }
    }
    //When the button clicked
    @Override
    public void onClick(View v)
    {
        //converts latitude and longtitude to a string
        String latLocation =  ("" + mLastLocation.getLatitude());
        String longLocation =  ("" + mLastLocation.getLongitude());
        switch (v.getId())
        {
            ///When user press button "Empty"
            case R.id.btnEmpty:
                //do something when
                try
                {
                    conn.sendLocation(latLocation, longLocation, "Y",id(this));
                    populateMap();
                    //show Popup
                    Toast.makeText(ourSpot.this,"Thank You, Your submission has been added.", Toast.LENGTH_LONG).show();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                Log.d("IDTest", id(this));
                break;
            case R.id.btnDirections:
                //When user press button "Directions"
                findClosest();
                break;
            case R.id.btnTaken:
                //When user press button "Taken"
                try
                {
                    conn.sendLocation(latLocation, longLocation, "N",id(this));
                    populateMap();
                    Toast.makeText(ourSpot.this,"Thank You, Your submission has been added.", Toast.LENGTH_LONG).show();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    public static double distance(double lat1, double lat2, double lon1, double lon2)
    {

        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        distance = Math.pow(distance, 2);
        return Math.sqrt(distance);
    }

    //finds the closest pin to user's location
    public void findClosest()
    {

        Submission closest = null;
        double minDistance = 0;
        double currDistance = 0;
        for (Submission submission : SubmissionArray){
            currDistance = distance(submission.getLatitude(), mLastLocation.getLatitude(), submission.getLongitude(), mLastLocation.getLongitude());
            if (closest == null) {
                if (submission.getEmpty()) {
                    closest = submission;
                    minDistance = distance(submission.getLatitude(), mLastLocation.getLatitude(), submission.getLongitude(), mLastLocation.getLongitude());
                }
            }
            else if (submission.getEmpty()){
                closest = currDistance < minDistance ? submission : closest;
            }
        }
        if(closest !=null) {
            LatLng latLng = new LatLng(closest.getLatitude(), closest.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15.5f));
        }
        else{
            //no parking space available popup
            Toast.makeText(ourSpot.this,"There is no parking space available.", Toast.LENGTH_LONG).show();
        }
    }

    //pupulate maps with google results of parkings nearby
    public void gerNearbyParkings()
    {
        //what to search
        String prk = "parking";
        String url = getUrl(mLastLocation.getLatitude(), mLastLocation.getLongitude(), prk);
        Object[] DataTransfer = new Object[2];
        DataTransfer[0] = mMap;
        DataTransfer[1] = url;
        Log.d("onClick", url);
        GetParkingNearby getParking = new GetParkingNearby();
        getParking.execute(DataTransfer);
    }
    public void getOwnerLots()
    {
        //Populate Owner Array with submissions

        JSONArray Jarray = null;
        try {
            Jarray = conn.getOwnerLots(mLastLocation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(Jarray.length() > 0)
        {

        }
        for (int i = 0; i < Jarray.length(); i++)
        {
            try
            {
                OwnerArray.add(new ParkingLots(
                        Jarray.getJSONObject(i).getDouble("Latitude"),
                        Jarray.getJSONObject(i).getDouble("Longitude"),
                        Jarray.getJSONObject(i).getInt("Availability"),
                        Jarray.getJSONObject(i).getDouble("Price"),
                        Jarray.getJSONObject(i).getInt("Spaces")));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        //Populate map with markers
        for (ParkingLots parkingToAdd : OwnerArray)
        {
            if(parkingToAdd.getAvailability() == 1) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(parkingToAdd.getLatitude(), parkingToAdd.getLongitude()))
                        .title("Price: " + "€ " + parkingToAdd.getPrice() )
                        .snippet("Spaces: " + parkingToAdd.getSpaces())
                        .icon(BitmapDescriptorFactory.defaultMarker(240)));
            }
            }
    }

    //gets google url
    private String getUrl(double latitude, double longitude, String nearbyPlace)
    {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyCbeOCD4Nowf5X671daoc2AoAbIstfWBr8");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }



    //gets unique device id
    public synchronized static String id(Context context) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists())
                    writeInstallationFile(installation);
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
    public static double[][] getBlackList(){
        if(blackList!=null) {

            Log.d("blackListisNotnull",String.valueOf(blackList[0][0]));
            return blackList;
        }
        else{
            // TODO Change to 0 0 array;
            Log.d("blackListisNotnull",String.valueOf(blackList[0][0]));
            return null;
        }
    }
    /*public  void dialog(String a, String b) throws InterruptedException {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setMessage("Are there any parking space around?");
        //Button One : Yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              //  Toast.makeText(ourSpot., "Thank You, Your submission has been added.", Toast.LENGTH_LONG).show();
                //dialog.cancel();
            }
        });
        //Button Two : No
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(ourSpot.this, "No button Clicked!", Toast.LENGTH_LONG).show();
                dialog.cancel();
            }
        });
        //Button Three : Neutral
        builder.setNeutralButton("Don't ask again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(backgroundLocation.this, "Place is added to our blacklist", Toast.LENGTH_LONG).show();
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        //AlertDialog.Builder dialog  = new AlertDialog.Builder(getActivity());
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }*/


    public void Natific(){


/*
        // prepare intent which is triggered if the
// notification is selected

        Intent intent = new Intent(this, ourSpot.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

// build notification
// the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("OurSpot")
                .setContentText("Is there any parking space around?")
                .setSmallIcon(R.drawable.p)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                //.addAction(null,"a",pIntent)
                //.addAction(P."Yes", pIntent)
                .addAction(R.drawable.p, "No", pIntent)
                .addAction(R.drawable.p, "Don't ask again", pIntent).build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);*/
    }
}