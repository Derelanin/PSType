package com.example.pstype_v1.signin;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Derelanin on 27.05.2017.
 */

public class SignReq extends StringRequest {
    private static final String LOGIN_REQUEST_URL = "http://pstype-pstype2.1d35.starter-us-east-1.openshiftapps.com/api/v1/signin";
    private Map<String, String> params;

    public SignReq(String username, String password, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, LOGIN_REQUEST_URL, listener, errorListener);
        params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

}