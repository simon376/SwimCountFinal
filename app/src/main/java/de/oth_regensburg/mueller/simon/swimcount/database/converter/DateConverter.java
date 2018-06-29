package de.oth_regensburg.mueller.simon.swimcount.database.converter;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
