package softmates.ourspot;

import android.util.Log;
import android.util.StringBuilderPrinter;

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
        int time = Double.valueOf(timeSpan).intValue();
        if(time>60){
            time/=60;
            if(time > 24){
                timeSpan = "more than 24h ago";
            }
            else {
                timeSpan = String.valueOf(time) + "h ago";
            }
            this.timeSpan = timeSpan;
        }
        else {
            this.timeSpan = String.valueOf(time) + "min ago.";
        }
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


