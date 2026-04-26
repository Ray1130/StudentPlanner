package com.example.planner.ui.task;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.planner.R;
import com.example.planner.data.model.Subject;
import com.example.planner.data.model.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskCreateSheetFragment extends BottomSheetDialogFragment {

    private EditText etTaskTitle;
    private TextView tvDeadlineValue;
    private TextView tvTimeValue;
    private TextView tvSubjectValue;
    private TextView tvPriorityValue;
    private CheckBox cbCompleted;
    private CheckBox cbReminder;

    private TaskViewModel viewModel;
    private Calendar calendar = Calendar.getInstance();
    private Subject selectedSubject;
    private List<Subject> subjects;
    private String selectedPriority = "low";

    private TaskUiModel editingTask;
    private ImageView btnDelete;

    public static TaskCreateSheetFragment newInstance(TaskUiModel task) {
        TaskCreateSheetFragment fragment = new TaskCreateSheetFragment();
        fragment.editingTask = task;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_create_side_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("TaskCreateSheet", "onViewCreated: Khởi tạo Side Sheet");
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        etTaskTitle = view.findViewById(R.id.et_task_title);
        tvDeadlineValue = view.findViewById(R.id.tv_deadline_value);
        tvTimeValue = view.findViewById(R.id.tv_time_value);
        tvSubjectValue = view.findViewById(R.id.tv_subject_value);
        tvPriorityValue = view.findViewById(R.id.tv_priority_value);
        cbCompleted = view.findViewById(R.id.cb_completed);
        cbReminder = view.findViewById(R.id.cb_reminder);
        btnDelete = view.findViewById(R.id.btn_delete_task);

        // Xử lý ngày mặc định từ bundle nếu có
        if (getArguments() != null && getArguments().containsKey("default_date")) {
            long defaultTimestamp = getArguments().getLong("default_date");
            calendar.setTimeInMillis(defaultTimestamp);
        }
        updateDeadlineText();
        updateTimeText();

        view.findViewById(R.id.row_deadline).setOnClickListener(v -> showDatePicker());
        view.findViewById(R.id.row_time).setOnClickListener(v -> toggleReminder());
        view.findViewById(R.id.row_subject).setOnClickListener(v -> showSubjectPicker());
        view.findViewById(R.id.row_priority).setOnClickListener(v -> showPriorityPicker());
        view.findViewById(R.id.btn_save_task).setOnClickListener(v -> {
            Log.d("TaskCreateSheet", "Nút Lưu Task được nhấn");
            saveTask();
        });

        if (editingTask != null) {
            setupEditMode();
        }

        // Nạp dữ liệu môn học ngay khi view được tạo
        viewModel.getAllSubjects().observe(getViewLifecycleOwner(), subjectsList -> {
            if (subjectsList != null) {
                Log.d("TaskCreateSheet", "Đã nhận danh sách môn học: " + subjectsList.size() + " môn");
                this.subjects = subjectsList;
                if (editingTask != null) {
                    updateSelectedSubjectInEditMode();
                }
            } else {
                Log.w("TaskCreateSheet", "Danh sách môn học nhận được là null");
            }
        });
    }

    private void updateSelectedSubjectInEditMode() {
        if (subjects != null && editingTask != null) {
            for (Subject s : subjects) {
                if (s.id == editingTask.getSubjectId()) {
                    selectedSubject = s;
                    tvSubjectValue.setText(s.name);
                    break;
                }
            }
        }
    }

    private void setupEditMode() {
        etTaskTitle.setText(editingTask.getTitle());
        tvDeadlineValue.setText(editingTask.getDeadline());
        cbCompleted.setChecked(editingTask.isChecked());
        
        boolean isReminder = editingTask.isReminderEnabled();
        cbReminder.setChecked(isReminder);
        
        selectedPriority = editingTask.getPriority();
        updatePriorityText(selectedPriority);
        
        btnDelete.setVisibility(View.VISIBLE);
        btnDelete.setOnClickListener(v -> deleteTask());

        // Parse deadline string to calendar
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            calendar.setTime(sdf.parse(editingTask.getDeadline()));
            if (isReminder) {
                updateTimeText();
            } else {
                tvTimeValue.setText("Tắt");
            }
        } catch (Exception e) {
            try {
                SimpleDateFormat sdfDateOnly = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                calendar.setTime(sdfDateOnly.parse(editingTask.getDeadline()));
                tvTimeValue.setText("Tắt");
            } catch (Exception e2) {
                Log.e("TaskCreateSheet", "Error parsing deadline", e2);
            }
        }
    }

    private void updatePriorityText(String priority) {
        switch (priority) {
            case "high": tvPriorityValue.setText("Cao"); break;
            case "medium": tvPriorityValue.setText("Trung bình"); break;
            case "low": default: tvPriorityValue.setText("Thấp"); break;
        }
    }

    private void deleteTask() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa task")
                .setMessage("Bạn có chắc chắn muốn xóa task này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.delete(editingTask.getId(), () -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                dismiss();
                                if (getActivity() instanceof TaskActivity) {
                                    ((TaskActivity) getActivity()).fetchTasksFromServer();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDeadlineText();
            Log.d("TaskCreateSheet", "Đã chọn ngày: " + tvDeadlineValue.getText());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDeadlineText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDeadlineValue.setText(sdf.format(calendar.getTime()));
    }

    private void showTimePicker() {
        // Sử dụng style Spinner cho TimePickerDialog
        new android.app.TimePickerDialog(requireContext(), 
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar, // Style spinner/wheel
            (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                cbReminder.setChecked(true);
                updateTimeText();
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
            .show();
    }

    private void updateTimeText() {
        if (cbReminder.isChecked()) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTimeValue.setText(sdf.format(calendar.getTime()));
        } else {
            tvTimeValue.setText("Tắt");
        }
    }

    private void toggleReminder() {
        if (cbReminder.isChecked()) {
            // Đang bật -> Tắt đi
            cbReminder.setChecked(false);
            updateTimeText();
        } else {
            // Đang tắt -> Mở picker để chọn giờ và bật
            showTimePicker();
        }
    }


    private void showSubjectPicker() {
        Log.d("TaskCreateSheet", "Mở bộ chọn môn học");
        if (subjects == null) {
            Log.w("TaskCreateSheet", "Danh sách môn học chưa sẵn sàng (null)");
            Toast.makeText(getContext(), "Đang tải danh sách môn học...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] subjectNames = new String[subjects.size() + 1];
        for (int i = 0; i < subjects.size(); i++) {
            subjectNames[i] = subjects.get(i).name;
        }
        subjectNames[subjects.size()] = "+ Tạo môn mới";

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn môn học")
                .setItems(subjectNames, (dialog, which) -> {
                    if (which == subjects.size()) {
                        Log.d("TaskCreateSheet", "Chọn: Tạo môn mới");
                        showCreateSubjectDialog();
                    } else {
                        selectedSubject = subjects.get(which);
                        tvSubjectValue.setText(selectedSubject.name);
                        Log.d("TaskCreateSheet", "Đã chọn môn: " + selectedSubject.name + " (ID: " + selectedSubject.id + ")");
                    }
                }).show();
    }

    private void showCreateSubjectDialog() {
        EditText input = new EditText(requireContext());
        new AlertDialog.Builder(requireContext())
            .setTitle("Môn mới")
            .setView(input)
            .setPositiveButton("Lưu", (dialog, which) -> {
                String name = input.getText().toString().trim();
                if (!name.isEmpty()) {
                    Log.d("TaskCreateSheet", "Đang yêu cầu tạo môn mới: " + name);
                    Subject newSubject = new Subject("", name);
                    viewModel.insertSubject(newSubject, createdSubject -> {
                        if (createdSubject != null && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.d("TaskCreateSheet", "Tạo môn mới thành công, ID nhận được: " + createdSubject.id);
                                selectedSubject = createdSubject; 
                                tvSubjectValue.setText(createdSubject.name);
                                Toast.makeText(getContext(), "Đã tạo môn học mới", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            Log.e("TaskCreateSheet", "Thất bại khi tạo môn mới trên Server");
                        }
                    });
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void showPriorityPicker() {
        String[] priorities = {"Thấp", "Trung bình", "Cao"};
        String[] priorityValues = {"low", "medium", "high"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn mức độ ưu tiên")
                .setItems(priorities, (dialog, which) -> {
                    selectedPriority = priorityValues[which];
                    tvPriorityValue.setText(priorities[which]);
                    Log.d("TaskCreateSheet", "Đã chọn mức độ: " + selectedPriority);
                }).show();
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Log.w("TaskCreateSheet", "Không thể lưu: Tên task trống");
            Toast.makeText(getContext(), "Vui lòng nhập tên task", Toast.LENGTH_SHORT).show();
            return;
        }

        int subjectId = selectedSubject != null ? selectedSubject.id : 0;
        
        if (editingTask != null) {
            // Update mode
            com.example.planner.data.model.Task task = new com.example.planner.data.model.Task(
                    title,
                    calendar.getTimeInMillis(),
                    subjectId
            );
            task.id = editingTask.getId();
            task.isCompleted = cbCompleted.isChecked();
            task.priority = selectedPriority;
            task.isReminderEnabled = cbReminder.isChecked();
            task.note = editingTask.getNote();

            viewModel.update(task, () -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        dismiss();
                        if (getActivity() instanceof TaskActivity) {
                            ((TaskActivity) getActivity()).fetchTasksFromServer();
                        }
                    });
                }
            });
        } else {
            // Create mode
            Log.d("TaskCreateSheet", "Bắt đầu lưu Task: " + title + " | SubjectID: " + subjectId + " | Priority: " + selectedPriority);

            com.example.planner.data.model.Task newTask = new com.example.planner.data.model.Task(
                title,
                calendar.getTimeInMillis(),
                subjectId
            );
            newTask.isCompleted = cbCompleted.isChecked();
            newTask.priority = selectedPriority;
            newTask.isReminderEnabled = cbReminder.isChecked();
            newTask.note = ""; // Default empty note
            
            viewModel.saveTask(newTask, () -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d("TaskCreateSheet", "Callback thành công: Đóng Side Sheet và cập nhật UI");
                        dismiss();
                        Toast.makeText(getContext(), "Đã lưu task và đồng bộ server", Toast.LENGTH_SHORT).show();
                        if (getActivity() instanceof TaskActivity) {
                            ((TaskActivity) getActivity()).fetchTasksFromServer();
                        }
                    });
                } else {
                    Log.e("TaskCreateSheet", "Callback thành công nhưng Activity đã bị hủy");
                }
            });
        }
    }
}
