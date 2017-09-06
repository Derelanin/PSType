package com.example.pstype_v1.data;

import android.provider.BaseColumns;

/**
 * Created by Derelanin on 06.09.2017.
 */

public class Contract {
    private Contract(){
    }

    public static final class track implements BaseColumns {
        public final static String TABLE_NAME = "track";

        public final static String COLUMN_DATE = "date";
        public final static String COLUMN_TIME = "time";
        public final static String COLUMN_SPEED = "speed";
        public final static String COLUMN_LAT = "latitude";
        public final static String COLUMN_LON = "longitude";
    }

}
