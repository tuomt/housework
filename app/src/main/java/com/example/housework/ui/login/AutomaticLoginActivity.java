package com.example.housework.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.housework.MainActivity;
import com.example.housework.R;
import com.example.housework.api.ApiError;
import com.example.housework.api.ApiRequestHandler;
import com.example.housework.api.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class AutomaticLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_login);

        SharedPreferences sharedPreferences = getSharedPreferences(String.valueOf(R.string.preferences_file_name),
                Context.MODE_PRIVATE);

        boolean autoLogin = sharedPreferences.getBoolean("auto_login", false);
        long userId = sharedPreferences.getLong("user_id", -1);
        final String refreshToken = sharedPreferences.getString("refresh_token", null);

        Bundle b = getIntent().getExtras();

        // Login automatically after creating a new user
        if (b != null &&
                b.getString("email") != null &&
                b.getString("password") != null) {
            String email = b.getString("email");
            String password = b.getString("password");
            authenticateUser(email, password);
        } else if (autoLogin && userId != -1 && refreshToken != null) {
            // Login automatically if auto login is enabled
            authenticateUser(userId, refreshToken);
        } else {
            // Change to login activity for manual login
            Intent intent = new Intent(AutomaticLoginActivity.this, LoginActivity.class);
            startActivity(intent);

            // Finish this activity once successful
            finish();
        }
    }

    private void authenticateUser(final long userId, final String refreshToken) {
        // URL for getting creating a new access token
        final String url = Constants.DOMAIN + "/api/users/" + userId + "/access-token";

        // Create a request for authentication
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Create an intent for main activity
                        Intent intent = new Intent(AutomaticLoginActivity.this, MainActivity.class);

                        // Save the user id and the access token for main activity
                        try {
                            Bundle b = new Bundle();
                            b.putLong("user_id", userId);
                            b.putString("access_token", response.getString("access_token"));
                            // Pass the bundle to main activity
                            intent.putExtras(b);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String msg = getResources().getString(R.string.error_failed_to_enable_auto_login);
                            Toast.makeText(getApplicationContext(),
                                    msg,
                                    Toast.LENGTH_LONG).show();
                        }

                        // Notify the user of a successful login
                        String loginMsg = getResources().getString(R.string.success_login);
                        Toast.makeText(getApplicationContext(),
                                loginMsg,
                                Toast.LENGTH_LONG).show();

                        // Change to main activity
                        startActivity(intent);
                        // Finish this activity once successful
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String msg = getResources().getString(R.string.error_auto_login_failed);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                // Change to login activity
                Intent intent = new Intent(AutomaticLoginActivity.this, LoginActivity.class);
                startActivity(intent);

                // Finish this activity once successful
                finish();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                // Set Bearer token
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + refreshToken);
                return headers;
            }
        };

        // Instantiate the RequestQueue.
        final RequestQueue queue = ApiRequestHandler.getInstance(getApplicationContext()).getRequestQueue();

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    private void authenticateUser(String email, String password) {
        // Authentication URL
        final String url = Constants.DOMAIN + "/api/credentials/user";

        JSONObject user = new JSONObject();
        try {
            user.put("email", email);
            user.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            String msg = getResources().getString(R.string.error_general);
            Toast.makeText(getApplicationContext(),
                    msg,
                    Toast.LENGTH_LONG).show();
        }

        // Send an authentication request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, user,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Save user id and tokens
                        try {
                            SharedPreferences sharedPreferences = getSharedPreferences(String.valueOf(R.string.preferences_file_name),
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor prefEditor = sharedPreferences.edit();
                            prefEditor.putBoolean("auto_login", false);
                            prefEditor.putLong("user_id", response.getLong("user_id"));
                            prefEditor.putString("refresh_token", response.getString("refresh_token"));
                            prefEditor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String msg = getResources().getString(R.string.error_general);
                            Toast.makeText(getApplicationContext(),
                                    msg,
                                    Toast.LENGTH_LONG).show();
                        }

                        // Create an intent for main activity
                        Intent intent = new Intent(AutomaticLoginActivity.this, MainActivity.class);

                        // Save the user id and the access token for main activity
                        try {
                            Bundle b = new Bundle();
                            b.putLong("user_id", response.getLong("user_id"));
                            b.putString("access_token", response.getString("access_token"));
                            // Pass the bundle to main activity
                            intent.putExtras(b);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String msg = getResources().getString(R.string.error_general);
                            Toast.makeText(getApplicationContext(),
                                    msg,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Notify the user of a successful login
                        String loginMsg = getResources().getString(R.string.success_login);
                        Toast.makeText(getApplicationContext(),
                                loginMsg,
                                Toast.LENGTH_LONG).show();

                        // Change to main activity
                        startActivity(intent);

                        // Finish this activity
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String msg = getResources().getString(R.string.error_auto_login_failed);
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                // Change to login activity
                Intent intent = new Intent(AutomaticLoginActivity.this, LoginActivity.class);
                startActivity(intent);

                // Finish this activity
                finish();
            }
        });

        // Instantiate the RequestQueue.
        final RequestQueue queue = ApiRequestHandler.getInstance(getApplicationContext()).getRequestQueue();

        // Add the request to the RequestQueue.
        queue.add(request);
    }
}
