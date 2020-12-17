package com.example.housework.ui.task;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.housework.R;
import com.example.housework.api.ApiError;
import com.example.housework.api.ApiRequestHandler;
import com.example.housework.api.CachedJsonObjectRequest;
import com.example.housework.api.Constants;
import com.example.housework.data.UserViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TasksInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksInfo extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_GROUP_ID = "group_id";

    // TODO: Rename and change types of parameters
    private long mTaskId;
    private long mGroupId;

    private UserViewModel userViewModel;

    public TasksInfo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TasksInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static TasksInfo newInstance(long param1, long param2) {
        TasksInfo fragment = new TasksInfo();
        Bundle args = new Bundle();
        args.putLong(ARG_TASK_ID, param1);
        args.putLong(ARG_GROUP_ID, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        if (getArguments() != null) {
            mTaskId = getArguments().getLong(ARG_TASK_ID);
            mGroupId = getArguments().getLong(ARG_GROUP_ID);
            requestAndDisplayTask(mTaskId, mGroupId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tasks_info, container, false);
    }

    private void displayTask(JSONObject task) {
        View view = getView();
        EditText taskNameEditText = view.findViewById(R.id.etxt_task_info_edit_name);
        EditText descriptionEditText = view.findViewById(R.id.etxt_task_description);
        SeekBar progressBar = view.findViewById(R.id.bar_task_progress);

        try {
            taskNameEditText.setText(task.getString("name"));
            if (task.isNull("description")) {
                descriptionEditText.setText("");
            } else {
                descriptionEditText.setText(task.getString("description"));
            }

            String progress = task.getString("progress");
            System.out.println("Progress: " + progress);
            switch (progress) {
                case "not in progress":
                    progressBar.setProgress(0);
                    break;
                case "in progress":
                    progressBar.setProgress(1);
                    break;
                case "completed":
                    progressBar.setProgress(2);
                    break;
            }
    } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestAndDisplayTask(final long taskId, final long groupId) {
        // Instantiate the RequestQueue
        final RequestQueue queue = ApiRequestHandler.getInstance(getActivity().
                getApplicationContext()).
                getRequestQueue();

        final String accessToken = userViewModel.getUser().getValue().getAccessToken();

        String url = Constants.DOMAIN + "/api/groups/" + groupId + "/tasks/" + taskId;

        // Create a request for the task
        CachedJsonObjectRequest request = new CachedJsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        displayTask(response);
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