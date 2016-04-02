package com.subbu.trackit.utils;

import com.subbu.trackit.database.DatabaseAdapter;

/**
 * Created by bhanuchander.belladi on 01-04-2016.
 */
public class Cache {

    private static DatabaseAdapter databaseAdapter = null;

    public static void setDatabaseAdapter(DatabaseAdapter databaseAdapter) {
        Cache.databaseAdapter = databaseAdapter;
    }

    public static DatabaseAdapter getDatabaseAdapter() {
        return databaseAdapter;
    }

}