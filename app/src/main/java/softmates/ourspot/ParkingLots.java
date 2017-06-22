package softmates.ourspot; /**
 * Created by Allan on 6/20/2017.
 */

public class ParkingLots {

        private double Latitude;
        private double Longitude;
        private double Price;
        private int Availability;
        private int Spaces;

        public ParkingLots (double Latitude, double Longitude, int Availability, double Price, int Spaces) {
            this.Latitude = Latitude;
            this.Longitude = Longitude;
            this.Availability = Availability;
            this.Price = Price;
            this.Spaces = Spaces;
        }


        public double getLatitude()
        {
            return Latitude;
        }

        public double getLongitude()
        {
            return Longitude;
        }

        public int getAvailability(){return Availability;}
          public double getPrice(){return Price;}

        public int getSpaces() {
            return Spaces;
        }

    }

