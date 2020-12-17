package com.example.housework.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

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
import com.example.housework.api.Constants;
import com.example.housework.data.User;
import com.example.housework.data.UserViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private UserViewModel userViewModel;
    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        User user = userViewModel.getUser().getValue();

        requestTasks(user.getId(), user.getGroupId(), user.getAccessToken());

        return root;
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

            // Clear earlier entries from cache
            queue.getCache().remove(request.getCacheKey());

            // Add the request to the RequestQueue.
            queue.add(request);
        }

        private void displayTasks(JSONArray tasks, final long userId, final long groupId, final String accessToken) {
            for (int i = 0; i < tasks.length(); i++) {
                try {
                    JSONObject task = tasks.getJSONObject(i);
                    final long taskId = task.getLong("id");
                    String taskName = task.getString("name");
                    String startDate = task.getString("start_date");

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    LinearLayout layout = getActivity().findViewById(R.id.layout_week_tasks);

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
                            Navigation.findNavController(v).navigate(R.id.action_nav_home_to_tasksInfo, bundle);
                    }
                });
                    layout.addView(date);
                    layout.addView(button);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
}