package com.example.housework.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.housework.MainActivity;
import com.example.housework.R;
import com.example.housework.api.ApiError;
import com.example.housework.api.CachedJsonObjectRequest;
import com.example.housework.api.Constants;
import com.example.housework.api.ApiRequestHandler;
import com.example.housework.data.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);
        final EditText emailText = view.findViewById(R.id.etxt_email);
        final EditText passwordText = view.findViewById(R.id.etxt_password);
        final Button createUserButton = view.findViewById(R.id.btn_move_to_create_user);
        final Button joinGroupButton = view.findViewById(R.id.btn_join_group);
        final Button loginButton = view.findViewById(R.id.btn_login);
        final SwitchCompat autoLoginSwitch = view.findViewById(R.id.switch_auto_login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();

                email = email.trim();
                if (email.length() > 0 && password.length() > 0) {
                    // Disable all user actions such as buttons
                    setActionsEnabled(false);
                    // Try to log in
                    requestAuthenticate(email, password, autoLoginSwitch.isChecked());
                }
            }
        });

        createUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_createUser);
            }
        });

        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_joinGroup);
            }
        });

        return view;
    }

    private void enableAutoLogin(long userId, String refreshToken) {
        // Get shared preferences
        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(String.valueOf(R.string.preferences_file_name),
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();

        // Save user id, refresh token and auto login state
        prefEditor.putLong("user_id", userId);
        prefEditor.putString("refresh_token", refreshToken);
        prefEditor.putBoolean("auto_login", true);
        prefEditor.apply();
    }

    private void disableAutoLogin() {
        // Get shared preferences
        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(String.valueOf(R.string.preferences_file_name),
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();

        // Clear shared preferences
        prefEditor.remove("user_id");
        prefEditor.remove("refresh_token");
        prefEditor.putBoolean("auto_login", false);
        prefEditor.apply();
    }

    /**
     * Set the enabled state of all components.
     *
     * @param enabled state of the components
     */
    private void setActionsEnabled(boolean enabled) {
        View view = getActivity().findViewById(R.id.login_screen);

        EditText emailText = view.findViewById(R.id.etxt_email);
        EditText passwordText = view.findViewById(R.id.etxt_password);
        Button createUserButton = view.findViewById(R.id.btn_move_to_create_user);
        Button joinGroupButton = view.findViewById(R.id.btn_join_group);
        Button loginButton = view.findViewById(R.id.btn_login);
        SwitchCompat autoLoginSwitch = view.findViewById(R.id.switch_auto_login);

        emailText.setEnabled(enabled);
        passwordText.setEnabled(enabled);
        createUserButton.setEnabled(enabled);
        joinGroupButton.setEnabled(enabled);
        loginButton.setEnabled(enabled);
        autoLoginSwitch.setEnabled(enabled);
    }

    private void requestAuthenticate(String email, String password, final boolean autoLogin) {
        JSONObject user = new JSONObject();
        try {
            user.put("email", email);
            user.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            String msg = getResources().getString(R.string.error_general);
            Toast.makeText(getActivity().getApplicationContext(),
                    msg,
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Authentication URL
        String url = Constants.DOMAIN + "/api/credentials/user";

        // Create a request for user authentication
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, user,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        long userId;
                        String accessToken;
                        String refreshToken;

                        // Get user id, access token and refresh token from the response
                        try {
                            userId = response.getLong("user_id");
                            accessToken = response.getString("access_token");
                            refreshToken = response.getString("refresh_token");
                        } catch (JSONException e) {
                            e.printStackTrace();

                            // Show an error message to the user
                            String msg = getResources().getString(R.string.error_general);
                            Toast.makeText(getActivity().getApplicationContext(),
                                    msg,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Save user id and refresh token if automatic login is enabled
                        if (autoLogin) {
                            enableAutoLogin(userId, refreshToken);
                        } else {
                            disableAutoLogin();
                        }

                        requestUser(userId, accessToken);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    // Network error
                    String msg = getResources().getString(R.string.error_general_network);
                    Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                } else {
                    // API error
                    try {
                        ApiError apiError = new ApiError(error.networkResponse.data);
                        apiError.print();
                        apiError.displayToUser(getActivity().getApplicationContext());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        String msg = getResources().getString(R.string.error_general);
                        Toast.makeText(getActivity().getApplicationContext(),
                                msg,
                                Toast.LENGTH_LONG).show();
                    }
                }

                // Enable all user actions
                setActionsEnabled(true);
            }
        });

        // Instantiate the RequestQueue.
        final RequestQueue queue = ApiRequestHandler.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    private void requestUser(final long userId, final String accessToken) {
        String url = Constants.DOMAIN + "/api/users/" + userId;

        // Create a request for user data
        CachedJsonObjectRequest request = new CachedJsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        long groupId = -1;

                        if (!response.isNull("groups")) {
                            // Get group id
                            try {
                                JSONArray groups = response.getJSONArray("groups");
                                groupId = groups.getJSONObject(0).getLong("group_id");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        // Switch to main activity
                        switchToMainActivity(userId, groupId, accessToken);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    // Network error
                    String msg = getResources().getString(R.string.error_general_network);
                    Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                } else {
                    // API error
                    try {
                        ApiError apiError = new ApiError(error.networkResponse.data);
                        apiError.print();
                        apiError.displayToUser(getActivity().getApplicationContext());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        String msg = getResources().getString(R.string.error_general);
                        Toast.makeText(getActivity().getApplicationContext(),
                                msg,
                                Toast.LENGTH_LONG).show();
                    }
                }

                // Auto login has to be disabled because the login operation failed
                disableAutoLogin();
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

        // Instantiate the RequestQueue
        final RequestQueue queue = ApiRequestHandler.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    private void switchToMainActivity(long userId, long groupId, String accessToken) {
        // Create intent for main activity
        Intent intent = new Intent(getActivity(), MainActivity.class);

        // Pass user id, group id and access token to main activity
        Bundle b = new Bundle();
        b.putLong("user_id", userId);
        b.putLong("group_id", groupId);
        b.putString("access_token", accessToken);
        intent.putExtras(b);

        // Notify the user of a successful login
        String loginMsg = getResources().getString(R.string.success_login);
        Toast.makeText(getActivity().getApplicationContext(),
                loginMsg,
                Toast.LENGTH_LONG).show();

        // Change to main activity
        startActivity(intent);

        // Complete and destroy login activity once successful
        FragmentActivity loginActivity = getActivity();
        if (loginActivity != null) {
            loginActivity.finish();
        }

    }
}