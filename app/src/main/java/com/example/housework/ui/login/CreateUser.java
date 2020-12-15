package com.example.housework.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.housework.R;
import com.example.housework.api.ApiError;
import com.example.housework.api.ApiRequestHandler;
import com.example.housework.api.Constants;
import com.example.housework.api.InputValidator;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateUser extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_user, container, false);

        final Button createUserButton = view.findViewById(R.id.btn_create_user);
        final TextInputLayout textInputUserName = view.findViewById(R.id.txt_input_username);
        final TextInputLayout textInputEmail = view.findViewById(R.id.txt_input_email);
        final TextInputLayout textInputPassword = view.findViewById(R.id.txt_input_password);

        // Instantiate the RequestQueue.
        final RequestQueue queue = ApiRequestHandler.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        createUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userName = textInputUserName.getEditText().getText().toString();
                final String email = textInputEmail.getEditText().getText().toString();
                final String password = textInputPassword.getEditText().getText().toString();

                boolean containsInvalidInput = false;

                // Validate username
                if (!InputValidator.isValidUserName(userName)) {
                    String error = getResources().getString(R.string.requirements_username);
                    textInputUserName.setError(error);
                    containsInvalidInput = true;
                }

                if (!InputValidator.isValidEmail(email)) {
                     String error = getResources().getString(R.string.requirements_email);
                     textInputEmail.setError(error);
                    containsInvalidInput = true;
                }

                if (!InputValidator.isValidPassword(password)) {
                     String error = getResources().getString(R.string.requirements_password);
                     textInputPassword.setError(error);
                     containsInvalidInput = true;
                }

                if (containsInvalidInput) {
                    return;
                }

                JSONObject user = new JSONObject();
                try {
                    user.put("name", userName);
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

                String url = Constants.DOMAIN + "/api/users";

                // Send a request
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, user,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                Intent intent = new Intent(getActivity(), AutomaticLoginActivity.class);
                                Bundle b = new Bundle();
                                b.putString("email", email);
                                b.putString("password", password);

                                // Pass email and password to automatic login activity
                                intent.putExtras(b);

                                // Notify the user
                                String msg = getResources().getString(R.string.success_user_created);
                                Toast.makeText(getActivity().getApplicationContext(),
                                        msg,
                                        Toast.LENGTH_LONG).show();

                                // Start automatic login activity
                                startActivity(intent);
                                // Complete and destroy this activity once successful
                                FragmentActivity activity = getActivity();
                                if (activity != null) {
                                    activity.finish();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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
                });

                // Add the request to the RequestQueue.
                queue.add(request);
            }
        });

        return view;
    }
}