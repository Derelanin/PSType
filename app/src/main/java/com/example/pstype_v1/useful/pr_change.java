package com.example.pstype_v1.useful;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Derelanin on 16.09.2017.
 */

public class pr_change extends StringRequest {
    private static final String LOGIN_REQUEST_URL = "http://pstype-pstype.1d35.starter-us-east-1.openshiftapps.com/api/v1/change";
    private Map<String, String> params;

    public pr_change (String token, String age, Boolean sex, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, LOGIN_REQUEST_URL, listener, errorListener);
        params = new HashMap<>();
        params.put("token", token);
        params.put("age", age);
        params.put("sex", String.valueOf(sex));
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

}
