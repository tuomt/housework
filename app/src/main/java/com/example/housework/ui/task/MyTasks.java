package com.example.housework.ui.task;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyTasks#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyTasks extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private UserViewModel userViewModel;

    public MyTasks() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyTasks newInstance(String param1, String param2) {
        MyTasks fragment = new MyTasks();
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
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        System.out.println("USER NAME: " + userViewModel.getUser().getValue().getName());
        requestAndDisplayTasks();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_tasks, container, false);
    }

    private void requestAndDisplayTasks() {
        User user = userViewModel.getUser().getValue();
        Bundle extras = getActivity().getIntent().getExtras();
        long userId = user.getId();
        String accessToken = user.getAccessToken();
        requestUser(userId, accessToken);
    }

    private void requestUser(final long userId, final String accessToken) {
        // Instantiate the RequestQueue
        final RequestQueue queue = ApiRequestHandler.getInstance(getActivity().
                getApplicationContext()).
                getRequestQueue();

        String url = Constants.DOMAIN + "/api/users/" + userId;

        // Create a request for user data
        CachedJsonObjectRequest request = new CachedJsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.isNull("groups")) {
                                System.out.println("NO GROUPS!!!");
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

                                if (getActivity() != null) {
                                    // Request tasks
                                    requestTasks(userId, groupId, accessToken);
                                }
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

    private void requestTasks(final long userId, final long groupId, final String accessToken) {

        String url = Constants.DOMAIN + "/api/groups/" + groupId + "/tasks";

        // Create a request for tasks of the group
        CachedJsonArrayRequest request = new CachedJsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        displayTasks(response, userId, groupId, accessToken);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (error.networkResponse != null) {
                        ApiError apiError = new ApiError(error.networkResponse.data);
                        apiError.print();
                        // TODO: Check for failed authorization
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

        // Fix Volley sending requests twice
        request.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    private void displayTasks(JSONArray tasks, final long userId, final long groupId, final String accessToken) {
        for (int i = 0; i < tasks.length(); i++) {
            try {
                JSONObject task = tasks.getJSONObject(i);
                if (isDoer(task, userId)) {
                    final long taskId = task.getLong("id");
                    String taskName = task.getString("name");
                    String startDate = task.getString("start_date");

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    LinearLayout layout = getActivity().findViewById(R.id.layout_my_tasks);

                    // Create date text for the button
                    TextView date = new TextView(getContext());
                    date.setText(startDate);
                    date.setGravity(Gravity.CENTER_HORIZONTAL);
                    date.setLayoutParams(params);

                    // Create a button for the task
                    Button button = new Button(getContext());
                    button.setText(taskName);
                    button.setBackgroundColor(Color.WHITE);
                    button.setTextColor(Color.BLACK);
                    button.setLayoutParams(params);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putLong("task_id", taskId);
                            bundle.putLong("group_id", groupId);
                            Navigation.findNavController(v).navigate(R.id.action_mytasks_to_tasksInfo, bundle);
                        }
                    });
                    layout.addView(date);
                    layout.addView(button);
                } else {
                    System.out.println("GROUP TASK: " + task.getString("name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isDoer(JSONObject task, long userId) throws JSONException {
        if (task.isNull("doers")) {
            return false;
        } else {
            JSONArray doers = task.getJSONArray("doers");
            for (int i = 0; i < doers.length(); i++) {
                JSONObject doer = doers.getJSONObject(i);
                if (doer.getLong("user_id") == userId) {
                    return true;
                }
            }
        }
        return false;
    }
}