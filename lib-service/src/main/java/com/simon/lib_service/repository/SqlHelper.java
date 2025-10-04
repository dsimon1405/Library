package com.simon.lib_service.repository;

public class SqlHelper {

    public static String toLike(String str) {
        return "%" + str + "%";
    }
}
