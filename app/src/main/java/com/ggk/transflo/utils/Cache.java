package com.ggk.transflo.utils;

import com.ggk.transflo.database.DatabaseAdapter;


public class Cache {

    private static DatabaseAdapter databaseAdapter = null;

    public static void setDatabaseAdapter(DatabaseAdapter databaseAdapter) {
        Cache.databaseAdapter = databaseAdapter;
    }

    public static DatabaseAdapter getDatabaseAdapter() {
        return databaseAdapter;
    }

}
