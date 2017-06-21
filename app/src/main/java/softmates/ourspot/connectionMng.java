package softmates.ourspot;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class connectionMng
{
    public connectionMng()
    {
    }
    final  OkHttpClient client = new OkHttpClient();
    public void sendLocation(String latitude, String longitude, String available, String ID) throws Exception
    {
        RequestBody formBody = new FormBody.Builder()
                .add("Latitude", latitude)
                .add("Longitude", longitude)
                .add("Available", available)
                .add("DeviceID", ID)
                .build();
        Request request = new Request.Builder()
                .url("http://51.255.166.173/backend/api/submissions")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback()
        {
            @Override public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException
            {
                // Logging purposes to be deleted on production.
                Log.d("Submission sent", response.body().string());
            }
        });
    }
    public JSONArray getTable(Location location) throws JSONException {
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("51.255.166.173")
                .addPathSegment("backend")
                .addPathSegment("api")
                .addPathSegment("submissions")
                .addQueryParameter("Latitude" , latitude)
                .addQueryParameter("Longitude" , longitude)
                .build();
        Log.d("url",url.toString());
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try
        {
            response = client.newCall(request).execute();
            String jsonData = response.body().string();
            return new JSONArray(jsonData);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new JSONArray("");
        }

    }
    public JSONArray getBlacklist(String ID) throws JSONException {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("51.255.166.173")
                .addPathSegment("backend")
                .addPathSegment("api")
                .addPathSegment("blacklist")
                .addQueryParameter("Device_ID" , ID)
                .build();
        Log.d("url",url.toString());
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try
        {
            response = client.newCall(request).execute();
            String jsonData = response.body().string();
            return new JSONArray(jsonData);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new JSONArray("");
        }

    }
    public void sendBlacklist(String latitude, String longitude, String ID){
        Log.d("Vars",latitude+longitude+ID);
        RequestBody formBody = new FormBody.Builder()
                .add("Latitude", latitude)
                .add("Longitude", longitude)
                .add("DeviceID", ID)
                .build();
        Request request = new Request.Builder()
                .url("http://51.255.166.173/backend/api/blacklist")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback()
        {
            @Override public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
                Log.d("failed","a");
            }

            @Override public void onResponse(Call call, Response response) throws IOException
            {
                // Logging purposes to be deleted on production.
                Log.d("Blacklist sent", response.body().string());
            }
        });
    }
    public JSONArray  getOwnerLots(Location location)throws JSONException {

        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("51.255.166.173")
                .addPathSegment("backend")
                .addPathSegment("api")
                .addPathSegment("parkinglots")
                .addQueryParameter("Latitude" , latitude)
                .addQueryParameter("Longitude" , longitude)
                .build();
        Log.d("url",url.toString());
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try
        {
            response = client.newCall(request).execute();
            String jsonData = response.body().string();
            Log.d("parkinglot", jsonData);
            return new JSONArray(jsonData);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new JSONArray("");
        }
    }
}
