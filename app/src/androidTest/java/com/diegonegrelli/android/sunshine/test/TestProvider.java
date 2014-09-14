package com.diegonegrelli.android.sunshine.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.diegonegrelli.android.sunshine.data.WeatherContract.LocationEntry;
import com.diegonegrelli.android.sunshine.data.WeatherContract.WeatherEntry;
import com.diegonegrelli.android.sunshine.data.WeatherDbHelper;


/**
 * Created by diegon on 9/13/14.
 */
public class TestProvider extends AndroidTestCase {

    private final static String LOG_TAG = TestProvider.class.getSimpleName();

    public void testDeleteDb() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadProvider() {
        String testName = "North Pole";
        String testLocationSetting = "99705";
        double testLatitude = 64.772;
        double testLongitude = -147.355;

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, testName);
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);

        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New location row id: " + locationRowId);

        String[] locationColumns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        Cursor locationCursor = mContext.getContentResolver().query(LocationEntry.CONTENT_URI, null, null, null, null, null);

        if(locationCursor.moveToFirst()) {
            String location = locationCursor.getString(locationCursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING));
            String name = locationCursor.getString(locationCursor.getColumnIndex(LocationEntry.COLUMN_CITY_NAME));
            double latitude = locationCursor.getDouble(locationCursor.getColumnIndex(LocationEntry.COLUMN_COORD_LAT));
            double longitude = locationCursor.getDouble(locationCursor.getColumnIndex(LocationEntry.COLUMN_COORD_LONG));

            assertEquals(location, testLocationSetting);
            assertEquals(name, testName);
            assertEquals(latitude, testLatitude);
            assertEquals(longitude, testLongitude);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

            long weatherRowId;
            weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);

            assertTrue(weatherRowId != -1);
            Log.d(LOG_TAG, "New weather row id: " + weatherRowId);

            String[] weatherColumns = {
                    WeatherEntry.COLUMN_LOC_KEY,
                    WeatherEntry.COLUMN_DATETEXT,
                    WeatherEntry.COLUMN_DEGREES,
                    WeatherEntry.COLUMN_HUMIDITY,
                    WeatherEntry.COLUMN_PRESSURE,
                    WeatherEntry.COLUMN_MAX_TEMP,
                    WeatherEntry.COLUMN_MIN_TEMP,
                    WeatherEntry.COLUMN_SHORT_DESC,
                    WeatherEntry.COLUMN_WIND_SPEED,
                    WeatherEntry.COLUMN_WEATHER_ID
            };

            Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI, null, null, null, null);


            if(weatherCursor.moveToFirst()) {
                long locationReturned = weatherCursor.getLong(weatherCursor.getColumnIndex(WeatherEntry.COLUMN_LOC_KEY));
                assertEquals(locationRowId, locationReturned);
            } else {
                fail("No weather values returned");
            }

            weatherCursor.close();
            locationCursor.close();
            db.close();
        } else {
            fail("No location values returned");
        }
    }

}
