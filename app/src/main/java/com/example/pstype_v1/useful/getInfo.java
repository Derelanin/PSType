package com.example.pstype_v1.useful;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Derelanin on 14.09.2017.
 */

public class getInfo extends StringRequest {
    private static final String LOGIN_REQUEST_URL = "http://pstype-pstype.1d35.starter-us-east-1.openshiftapps.com/api/v1/change/data";
    private Map<String, String> params;

    public getInfo(String token, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, LOGIN_REQUEST_URL, listener, errorListener);
        params = new HashMap<>();
        params.put("token", token);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

}
