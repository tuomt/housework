package com.example.housework.ui.group;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.housework.R;
import com.example.housework.api.ApiError;
import com.example.housework.api.ApiRequestHandler;
import com.example.housework.api.CachedJsonObjectRequest;
import com.example.housework.api.Constants;
import com.example.housework.api.InputValidator;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateGroup#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateGroup extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextInputLayout textInputGroupName;
    private TextInputLayout textInputGroupPassword;

    public CreateGroup() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateGroup.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateGroup newInstance(String param1, String param2) {
        CreateGroup fragment = new CreateGroup();
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
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        final Button joinGroup = view.findViewById(R.id.btn_move_join_group);
        joinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_createGroup_to_joinGroupMaster);
            }
        });

        textInputGroupName = view.findViewById(R.id.txt_input_layout_group_name);
        textInputGroupPassword = view.findViewById(R.id.txt_input_layout_group_password);

        Button createGroupButton = view.findViewById(R.id.btn_create_group);
        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = textInputGroupName.getEditText().getText().toString();
                String groupPassword = textInputGroupPassword.getEditText().getText().toString();

                // Check if the group name and the password are valid
                if (validateInput(groupName, groupPassword)) {
                    Bundle extras = getActivity().getIntent().getExtras();
                    String accessToken = extras.getString("access_token");
                    // Send a request to create a new group
                    requestCreateGroup(groupName, groupPassword, accessToken);
                }
            }
        });

        return view;
    }

    private boolean validateInput(String groupName, String groupPassword) {

        boolean containsInvalidInput = false;

        // Validate username
        if (!InputValidator.isValidGroupName(groupName)) {
            String error = getResources().getString(R.string.requirements_group_name);
            textInputGroupName.setError(error);
            containsInvalidInput = true;
        } else {
            textInputGroupName.setError(null);
        }

        if (!InputValidator.isValidGroupPassword(groupPassword)) {
            String error = getResources().getString(R.string.requirements_group_password);
            textInputGroupPassword.setError(error);
            containsInvalidInput = true;
        } else {
            textInputGroupPassword.setError(null);
        }

        if (containsInvalidInput) {
            return false;
        } else return true;
    }


    private void requestCreateGroup(final String groupName, final String groupPassword, final String accessToken) {
        String url = Constants.DOMAIN + "/api/groups";

        JSONObject group = new JSONObject();
        try {
            group.put("name", groupName);
            group.put("password", groupPassword);
        } catch (JSONException e) {
            e.printStackTrace();
            String msg = getResources().getString(R.string.error_general);
            Toast.makeText(getActivity().getApplicationContext(),
                    msg,
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Create a request
        CachedJsonObjectRequest request = new CachedJsonObjectRequest(Request.Method.POST, url, group,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String msg = getResources().getString(R.string.success_group_created);
                        Toast.makeText(getActivity().getApplicationContext(),
                                msg,
                                Toast.LENGTH_LONG).show();
                        if (getActivity() != null) {
                            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.joinGroupMaster);
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
                    String msg = getResources().getString(R.string.error_failed_to_create_group);
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
        queue.add(request);
    }
}