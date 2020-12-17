package com.example.housework.ui.task;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

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
import com.example.housework.ui.login.AutomaticLoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.CacheRequest;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateTask extends Fragment {

    private UserViewModel userViewModel;
    private String selectedDate;
    private Format dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public static CreateTask newInstance() {
        return new CreateTask();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_create_task, container, false);

        final CalendarView calendar = view.findViewById(R.id.calendar_create_task);
        selectedDate = dateFormatter.format(calendar.getDate());

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar c = Calendar.getInstance();
                c.set(year, month, dayOfMonth);
                selectedDate = dateFormatter.format(c.getTimeInMillis());
                System.out.println(selectedDate);
            }
        });

        Button createTaskButton = view.findViewById(R.id.btn_create_task);
        createTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText taskNameEditText = view.findViewById(R.id.etxt_task_name);
                EditText descriptionEditText = view.findViewById(R.id.ettxt_task_desc);
                String taskName = taskNameEditText.getText().toString();
                String description = descriptionEditText.getText().toString();
                requestCreateTask(taskName, selectedDate, description);
            }
        });


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void requestCreateTask(String taskName, String startDate, String description) {
        User user = userViewModel.getUser().getValue();
        long groupId = user.getGroupId();
        final String accessToken = user.getAccessToken();

        String url = Constants.DOMAIN + "/api/groups/" + groupId + "/tasks";

        JSONObject task = new JSONObject();
        try {
            task.put("name", taskName);
            task.put("start_date", startDate);
            task.put("end_date", JSONObject.NULL);
            task.put("recurring", JSONObject.NULL);
            task.put("saved", 0);
            task.put("progress", 1);
            task.put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
            String msg = getResources().getString(R.string.error_general);
            Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            return;
        }

        // Send a request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, task,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String msg = getResources().getString(R.string.success_task_created);
                        Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
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
                    String msg = getResources().getString(R.string.error_general);
                    Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    try {
                        ApiError apiError = new ApiError(error.networkResponse.data);
                        apiError.print();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

}