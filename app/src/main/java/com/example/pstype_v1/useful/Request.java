package com.example.pstype_v1.useful;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Derelanin on 16.09.2017.
 */

public class Request extends StringRequest {
    private Map<String, String> params;

    public Request (String[] headers, String[] values, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, listener, errorListener);
        params = new HashMap<>();
        for (int i=0; i<headers.length; i++)
            if (!values[i].equals(""))
                params.put(headers[i],values[i]);
    }
    public Request (String[] headers, String[] values, JSONObject value, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, listener, errorListener);
        params = new HashMap<>();
        for (int i=0; i<headers.length; i++)
            if (!values[i].equals(""))
                params.put(headers[i],values[i]);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
