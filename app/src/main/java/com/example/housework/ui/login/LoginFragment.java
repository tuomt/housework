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
import com.example.housework.api.Constants;
import com.example.housework.api.ApiRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

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

        // Instantiate the RequestQueue.
        final RequestQueue queue = ApiRequestHandler.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();

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
                }

                // Authentication URL
                String url = Constants.DOMAIN + "/api/credentials/user";

                // Create a request for user authentication
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, user,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Create intent for main activity
                                Intent intent = new Intent(getActivity(), MainActivity.class);

                                // Save user id and access_token for main activity
                                try {
                                    Bundle b = new Bundle();
                                    b.putLong("user_id", response.getLong("user_id"));
                                    b.putString("access_token", response.getString("access_token"));
                                    intent.putExtras(b);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    String msg = getResources().getString(R.string.error_general);
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            msg,
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // Get shared preferences
                                SharedPreferences sharedPreferences = getActivity()
                                        .getSharedPreferences(String.valueOf(R.string.preferences_file_name),
                                                Context.MODE_PRIVATE);
                                SharedPreferences.Editor prefEditor = sharedPreferences.edit();

                                // Save user id and refresh token if automatic login is enabled
                                if (autoLoginSwitch.isChecked()) {
                                    try {
                                        prefEditor.putLong("user_id", response.getLong("user_id"));
                                        prefEditor.putString("refresh_token", response.getString("refresh_token"));
                                        prefEditor.putBoolean("auto_login", true);
                                        prefEditor.apply();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        String msg = getResources().getString(R.string.error_general);
                                        Toast.makeText(getActivity().getApplicationContext(),
                                                msg,
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                } else {
                                    // Clear user id and the refresh token from preferences
                                    prefEditor.remove("user_id");
                                    prefEditor.remove("refresh_token");
                                    prefEditor.putBoolean("auto_login", false);
                                    prefEditor.apply();
                                }

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
                            }
                        }
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(request);
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
}