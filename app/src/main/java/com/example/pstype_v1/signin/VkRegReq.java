package com.example.pstype_v1.signin;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Derelanin on 08.07.2017.
 */

public class VkRegReq extends StringRequest {
    private static final String URLvk = "http://pstype-pstype.1d35.starter-us-east-1.openshiftapps.com/api/v1/vksignup";
    private Map<String, String> params;

    public VkRegReq(String username, String id, boolean sex, int age, Response.Listener<String> listener2, Response.ErrorListener errorListener2) {
        super(Method.POST, URLvk, listener2, errorListener2);
        params = new HashMap<>();
        params.put("usernamevk", username);
        params.put("idvk", id);
        params.put("sex", String.valueOf(sex));
        if ((age>=14) && (age<=110)){
            params.put("age", String.valueOf(age));
        }
        //params.put("sex", String.valueOf(sex));
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

}
