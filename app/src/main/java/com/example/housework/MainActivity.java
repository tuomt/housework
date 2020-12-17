package com.example.housework;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.housework.api.ApiError;
import com.example.housework.api.ApiRequestHandler;
import com.example.housework.api.CachedJsonObjectRequest;
import com.example.housework.api.Constants;
import com.example.housework.data.User;
import com.example.housework.data.UserViewModel;
import com.example.housework.ui.login.LoginActivity;
import com.google.android.material.navigation.NavigationView;


import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.Create_task, R.id.search_task, R.id.EditGroup, R.id.mytasks)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        final Bundle bundle = getIntent().getExtras();

        // Check if a user id and access token were passed correctly
        if (bundle == null || bundle.getLong("user_id", -1) == -1 ||
                bundle.getString("access_token", null) == null) {

            // Notify the user of the error
            String msg = getResources().getString(R.string.error_general);
            Toast.makeText(getApplicationContext(),
                    msg,
                    Toast.LENGTH_LONG).show();

            // Change back to the login activity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);

            // Finish this activity
            finish();
            return;
        } else {
            // Initialize userViewModel instance
            userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
            Long userId = bundle.getLong("user_id");
            String accessToken  = bundle.getString("access_token");
            User user = new User(userId, null, null, -1, accessToken);
            userViewModel.setUser(user);
        }

        // Load the user name and email in the sidebar
        updateSidebarUserDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Update the user information in the sidebar.
     */
    private void updateSidebarUserDetails() {

        // Get the navigation header view
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        final TextView userNameTextView = headerView.findViewById(R.id.txt_sidebar_user_name);
        final TextView emailTextView = headerView.findViewById(R.id.txt_sidebar_user_email);


        User user = userViewModel.getUser().getValue();
        final long userId = user.getId();
        final String accessToken = user.getAccessToken();

        // Instantiate the RequestQueue
        final RequestQueue queue = ApiRequestHandler.getInstance(getApplicationContext()).getRequestQueue();

        String url = Constants.DOMAIN + "/api/users/" + userId;

        // Create a request for user data
        CachedJsonObjectRequest request = new CachedJsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            userNameTextView.setText(response.getString("name"));
                            emailTextView.setText(response.getString("email"));

                            if (response.isNull("groups")) {
                                // User does not belong to a group
                            } else {
                                JSONArray groups = response.getJSONArray("groups");
                                System.out.println(groups.getJSONObject(0).toString());
                                long groupId = groups.getJSONObject(0).getLong("group_id");
                                User user = new User(response.getLong("id"),
                                        response.getString("name"),
                                        response.getString("email"),
                                        groupId,
                                        accessToken);

                                userViewModel.setUser(user);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    ApiError apiError = new ApiError(error.networkResponse.data);
                    apiError.print();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }) {
            @Override
            public Map<String, String> getHeaders() {
                // Set Bearer token for the request
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(request);
    }
}
