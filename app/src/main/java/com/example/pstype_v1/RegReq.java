package com.example.pstype_v1;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Derelanin on 27.05.2017.
 */

public class RegReq extends StringRequest {
    private static final String URL = "http://pstype-pstype.1d35.starter-us-east-1.openshiftapps.com/api/v1/signup";
    private Map<String, String> params;

    public RegReq(String username, String password, String age, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, URL, listener, errorListener);
        params = new HashMap<>();
        params.put("age", age);
        params.put("username", username);
        params.put("password", password);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
