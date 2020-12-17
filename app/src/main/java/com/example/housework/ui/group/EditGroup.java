package com.example.housework.ui.group;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
import android.icu.number.NumberFormatter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.example.housework.R;
import com.example.housework.api.ApiError;
import com.example.housework.api.ApiRequestHandler;
import com.example.housework.api.CachedJsonArrayRequest;
import com.example.housework.api.CachedJsonObjectRequest;
import com.example.housework.api.Constants;
import com.example.housework.data.User;
import com.example.housework.data.UserViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditGroup extends Fragment {

    private EditGroupViewModel EditGroupViewModel;
    private UserViewModel userViewModel;

    public static EditGroup newInstance() {
        return new EditGroup();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view =   inflater.inflate(R.layout.fragment_edit_group, container, false);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        // If user does not belong to a group, change to create group view
        final long groupId = userViewModel.getUser().getValue().getGroupId();
        if (groupId == -1) {
            System.out.println("NAVIGATING TO createGroup!");
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.createGroup);
        } else {
            Log.d("EditGroup", "groupId: " + groupId);
        }


        EditGroupViewModel =
                ViewModelProviders.of(this).get(EditGroupViewModel.class);
        View root = inflater.inflate(R.layout.fragment_edit_group, container, false);
        final TextView textView = root.findViewById(R.id.etxt_group_name_edit_group);
        EditGroupViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        requestAndDisplayGroup();
        requestAndDisplayMembers();
        return view;
    }

    private void displayGroup(JSONObject group) {
        try {
            if (getActivity() != null) {
                EditText groupNameEditText = getActivity().findViewById(R.id.etxt_group_name_edit_group);
                String groupName = group.getString("name");
                groupNameEditText.setText(groupName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayMembers(JSONArray members) {
        if (getActivity() != null) {
            System.out.println("Trying to display members...");
            LinearLayout layout = getActivity().findViewById(R.id.layout_group_members);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            System.out.println(members);

            for (int i = 0; i < members.length(); i++) {
                TextView t = new TextView(getContext());
                t.setTextColor(Color.BLACK);
                t.setTextSize(20);
                t.setText("Käyttäjä");
                try {
                    JSONObject member = members.getJSONObject(i);
                    t.setText(member.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                layout.addView(t, params);
            }
        }
    }

    public void requestAndDisplayGroup() {
        User user = userViewModel.getUser().getValue();
        final String accessToken = user.getAccessToken();
        long groupId = user.getGroupId();

        String url = Constants.DOMAIN + "/api/groups/" + groupId;

        // Create a request
        CachedJsonObjectRequest request = new CachedJsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        displayGroup(response);
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

        // Instantiate the RequestQueue
        final RequestQueue queue = ApiRequestHandler.getInstance(getActivity().
                getApplicationContext()).
                getRequestQueue();

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    public void requestAndDisplayMembers() {
        User user = userViewModel.getUser().getValue();
        final String accessToken = user.getAccessToken();
        long groupId = user.getGroupId();

        String url = Constants.DOMAIN + "/api/groups/" + groupId + "/members";

        // Create a request
        CachedJsonArrayRequest request = new CachedJsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        displayMembers(response);
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

        // Fix Volley sending requests twice
        request.setRetryPolicy(new DefaultRetryPolicy(0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Instantiate the RequestQueue
        final RequestQueue queue = ApiRequestHandler.getInstance(getActivity().
                getApplicationContext()).
                getRequestQueue();

        // Add the request to the RequestQueue.
        queue.add(request);
    }
}