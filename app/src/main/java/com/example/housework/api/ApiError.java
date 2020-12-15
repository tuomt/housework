package com.example.housework.api;

import android.content.Context;
import android.widget.Toast;

import com.example.housework.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ApiError {
    private String type;
    private String message;
    private String details;

    public ApiError(String type, String message, String details) {
        this.type = type;
        this.message = message;
        this.details = details;
    }

    public ApiError(byte[] responseData) throws JSONException {
        String responseBody = new String(responseData, StandardCharsets.UTF_8);
        JSONObject data = new JSONObject(responseBody);
        this.type = data.optString("type");
        this.message = data.optString("message");
        this.details = data.optString("details");
    }

    public String getType() {
        return this.type;
    }

    public String getMessage() {
        return this.message;
    }

    public String getDetails() {
        return this.details;
    }

    /**
     * Print the error to system out
     */
    public void print() {
        String details = this.getDetails();
        if (details == null) details = "-";
        System.out.println(String.format("ApiError (type: %s, message: %s, details: %s)", this.type, this.message, details));
    }

    /**
     * Display an appropriate error message to the user via Toast
     */
    public void displayToUser(Context context) {
        String type = this.getType();
        String msg;

        if (type.equals("incorrect_password")) {
            msg = context.getResources().getString(R.string.error_incorrect_password);
        } else if (type.equals("user_not_found")) {
            msg = context.getResources().getString(R.string.error_user_not_found);
        } else {
            msg = context.getResources().getString(R.string.error_general);
        }

        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
