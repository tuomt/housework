package com.example.housework.ui.group;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
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
import com.example.housework.R;
import com.example.housework.api.ApiError;
import com.example.housework.api.ApiRequestHandler;
import com.example.housework.api.Constants;
import com.example.housework.data.User;
import com.example.housework.data.UserViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JoinGroupMasterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JoinGroupMasterFragment extends Fragment {

    private UserViewModel userViewModel;

    public static JoinGroup newInstance() {
        return new JoinGroup();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_join_group_master, container, false);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        Button joinGroupButton = view.findViewById(R.id.btn_join_group_master);
        final EditText groupNameEditText = view.findViewById(R.id.etxt_group_name_join_group_master);
        final EditText groupPasswordEditText = view.findViewById(R.id.etxt_group_password_join_group_master);

        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = groupNameEditText.getText().toString();
                String groupPassword = groupPasswordEditText.getText().toString();
                final String accessToken = userViewModel.getUser().getValue().getAccessToken();
                requestAuthenticateMember(groupName, groupPassword, accessToken);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void requestAuthenticateMember(String groupName, String groupPassword, final String accessToken) {
        String groupAuthenticationUrl = Constants.DOMAIN + "/api/credentials/group";
        JSONObject credentials = new JSONObject();
        try {
            credentials.put("group_name", groupName);
            credentials.put("group_password", groupPassword);
        } catch (JSONException e) {
            String msg = getResources().getString(R.string.error_failed_to_create_group_member);
            Toast.makeText(getActivity().getApplicationContext(),
                    msg,
                    Toast.LENGTH_LONG).show();
        }

        // Create an authentication request
        JsonObjectRequest authRequest = new JsonObjectRequest(Request.Method.POST, groupAuthenticationUrl, credentials,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            long groupId = response.getLong("group_id");
                            String groupToken = response.getString("group_token");
                            requestCreateMember(groupId, groupToken, accessToken);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String msg = getResources().getString(R.string.error_failed_to_create_group_member);
                            Toast.makeText(getActivity().getApplicationContext(),
                                    msg,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (error.networkResponse != null) {
                        ApiError apiError = new ApiError(error.networkResponse.data);
                        apiError.print();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    String msg = getResources().getString(R.string.error_failed_to_create_group_member);
                    Toast.makeText(getActivity().getApplicationContext(),
                            msg,
                            Toast.LENGTH_LONG).show();
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

        // Instantiate the RequestQueue.
        final RequestQueue queue = ApiRequestHandler.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // Add the request to the RequestQueue.
        queue.add(authRequest);
    }

    private void requestCreateMember(final long groupId, String groupToken, final String accessToken) {
        String url = Constants.DOMAIN + "/api/groups/" + groupId + "/members";

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("group_token", groupToken);
        } catch (JSONException e) {
            String msg = getResources().getString(R.string.error_failed_to_create_group_member);
            Toast.makeText(getActivity().getApplicationContext(),
                    msg,
                    Toast.LENGTH_LONG).show();
        }

        // Create a request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        User user = userViewModel.getUser().getValue();
                        user.setGroupId(groupId);
                        userViewModel.setUser(user);
                        String msg = getResources().getString(R.string.success_group_member_created);
                        Toast.makeText(getActivity().getApplicationContext(),
                                msg,
                                Toast.LENGTH_LONG).show();
                        if (getView() != null) {
                            Navigation.findNavController(getView()).navigate(R.id.nav_home);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String msg = getResources().getString(R.string.error_failed_to_create_group_member);
                Toast.makeText(getActivity().getApplicationContext(),
                        msg,
                        Toast.LENGTH_LONG).show();
                try {
                    if (error.networkResponse != null) {
                        ApiError apiError = new ApiError(error.networkResponse.data);
                        apiError.print();
                    }
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

        // Instantiate the RequestQueue.
        final RequestQueue queue = ApiRequestHandler.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // Add the request to the RequestQueue.
        queue.add(request);
    }
}