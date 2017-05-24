package softmates.ourspot;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class connectionMng
{
    public connectionMng()
    {
    }
    final OkHttpClient client = new OkHttpClient();
    JSONArray Jarray;
    public void sendLocation(String latitude, String longitude, String available) throws Exception
    {
        RequestBody formBody = new FormBody.Builder()
                .add("Latitude", latitude)
                .add("Longitude", longitude)
                .add("Available", available)
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
                Log.d("Submission sent", response.body().string());
            }
        });
    }
    public JSONArray getTable() throws JSONException {
        Request request = new Request.Builder()
                .url("http://51.255.166.173/backend/api/submissions")
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
}
