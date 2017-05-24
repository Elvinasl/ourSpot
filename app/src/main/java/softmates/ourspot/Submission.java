package softmates.ourspot;

import android.util.Log;

public class Submission
{
    private double Latitude;
    private double Longitude;
    private boolean empty;
    private String timeSpan;

    public Submission (double Latitude, double Longitude, String empty, String timeSpan)
    {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.empty = empty.equals("Y") ? true : false;
        this.timeSpan = timeSpan + "min ago.";
    }

    public double getLatitude()
    {
        return Latitude;
    }

    public double getLongitude()
    {
        return Longitude;
    }

    public boolean getEmpty()
    {

        return empty;
    }

    public String getTimeSpan()
    {
        return timeSpan;
    }

}


