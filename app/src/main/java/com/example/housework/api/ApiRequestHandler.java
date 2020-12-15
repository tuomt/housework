package com.example.housework.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ApiRequestHandler {
    private static ApiRequestHandler instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private ApiRequestHandler(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized ApiRequestHandler getInstance(Context context) {
        if (instance == null) {
            instance = new ApiRequestHandler(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
