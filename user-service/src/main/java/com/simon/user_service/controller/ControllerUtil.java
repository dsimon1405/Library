package com.simon.user_service.controller;

import com.simon.exception.ExistsException;
import com.simon.name.Headers;
import jakarta.servlet.http.HttpServletRequest;

public class ControllerUtil {

    public static int getUserIdFromHeader(HttpServletRequest request) {
        String userId_header = request.getHeader(Headers.USER_ID);
        if (userId_header == null) throw new ExistsException("No valid headers");
        return Integer.parseInt(userId_header);
    }
}
