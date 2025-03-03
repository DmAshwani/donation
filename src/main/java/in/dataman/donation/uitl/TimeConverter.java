package in.dataman.donation.uitl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeConverter {
    public static String formatUnixTimestamp(String unixTime) {
        try {
            // Convert string to long
            long timestamp = Long.parseLong(unixTime) * 1000; // Convert seconds to milliseconds
            
            // Create Date object
            Date date = new Date(timestamp);

            // Define the format
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
            sdf.setTimeZone(TimeZone.getDefault()); // Set to system timezone

            return sdf.format(date);
        } catch (NumberFormatException e) {
            return "Invalid timestamp";
        }
    }

}

