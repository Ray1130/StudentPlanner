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
import android.widget.RadioGroup;
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
    private ViewGroup containerStep1;
    private ViewGroup containerStep2;
    private ViewGroup containerOptional;
    private RadioGroup rgTaskType;
    private ViewGroup rowSubject;
    private ViewGroup rowActivityGroup;
    private ViewGroup rowOptional;
    private EditText etTaskNote;
    private TextView tvActivityGroupValue;
    private ImageView ivToggleOptional;

    private TaskViewModel viewModel;
    private Calendar calendar = Calendar.getInstance();
    private Subject selectedSubject;
    private List<Subject> subjects;
    private String selectedPriority = "low";
    private String selectedActivityGroup = "Cá nhân";

    private TaskUiModel editingTask;
    private ImageView btnDelete;

    public static TaskCreateSheetFragment newInstance(TaskUiModel task) {
        TaskCreateSheetFragment fragment = new TaskCreateSheetFragment();
        fragment.editingTask = task;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_create_side_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        initViews(view);
        setupListeners(view);

        if (editingTask != null) {
            setupEditMode();
        } else {
            // Kiểm tra ngày mặc định từ Bundle (nếu tạo từ màn hình Schedule)
            if (getArguments() != null && getArguments().containsKey("default_date")) {
                long defaultDate = getArguments().getLong("default_date");
                if (defaultDate > 0) {
                    calendar.setTimeInMillis(defaultDate);
                    updateDeadlineText();
                }
            }
            containerStep1.setVisibility(View.VISIBLE);
            containerStep2.setVisibility(View.GONE);
        }

        observeSubjects();
    }

    private void initViews(View view) {
        containerStep1 = view.findViewById(R.id.container_step_1);
        containerStep2 = view.findViewById(R.id.container_step_2);
        containerOptional = view.findViewById(R.id.container_optional);

        rgTaskType = view.findViewById(R.id.rg_task_type);
        etTaskTitle = view.findViewById(R.id.et_task_title);
        etTaskNote = view.findViewById(R.id.et_task_note);

        tvDeadlineValue = view.findViewById(R.id.tv_deadline_value);
        tvTimeValue = view.findViewById(R.id.tv_time_value);
        tvSubjectValue = view.findViewById(R.id.tv_subject_value);
        tvPriorityValue = view.findViewById(R.id.tv_priority_value);
        cbCompleted = view.findViewById(R.id.cb_completed);
        cbReminder = view.findViewById(R.id.cb_reminder);
        btnDelete = view.findViewById(R.id.btn_delete_task);

        rowSubject = view.findViewById(R.id.row_subject);
        rowActivityGroup = view.findViewById(R.id.row_activity_group);
        rowOptional = view.findViewById(R.id.row_optional);
        tvActivityGroupValue = view.findViewById(R.id.tv_activity_group_value);
        ivToggleOptional = view.findViewById(R.id.iv_toggle_optional);

        updateDeadlineText();
        updateTimeText();
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.row_deadline).setOnClickListener(v -> showDatePicker());
        view.findViewById(R.id.row_time).setOnClickListener(v -> toggleReminder());
        view.findViewById(R.id.row_subject).setOnClickListener(v -> showSubjectPicker());
        view.findViewById(R.id.row_priority).setOnClickListener(v -> showPriorityPicker());
        view.findViewById(R.id.row_activity_group).setOnClickListener(v -> showActivityGroupPicker());
        view.findViewById(R.id.row_optional).setOnClickListener(v -> toggleOptionalSection());
        view.findViewById(R.id.btn_continue).setOnClickListener(v -> handleContinue());
        view.findViewById(R.id.btn_save_task).setOnClickListener(v -> {
            Log.d("TaskCreateSheet", "Nút Lưu Task được nhấn");
            saveTask();
        });
    }

    private void handleContinue() {
        String title = etTaskTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên nhiệm vụ", Toast.LENGTH_SHORT).show();
            return;
        }

        containerStep1.setVisibility(View.GONE);
        containerStep2.setVisibility(View.VISIBLE);

        if (rgTaskType.getCheckedRadioButtonId() == R.id.rb_study) {
            rowSubject.setVisibility(View.VISIBLE);
            rowActivityGroup.setVisibility(View.GONE);
            selectedActivityGroup = "Học tập";
        } else {
            rowSubject.setVisibility(View.GONE);
            rowActivityGroup.setVisibility(View.VISIBLE);
            if (selectedActivityGroup == null || selectedActivityGroup.equals("Học tập")) {
                selectedActivityGroup = "Cá nhân";
            }
            tvActivityGroupValue.setText(selectedActivityGroup);
        }
    }

    private void toggleOptionalSection() {
        if (containerOptional.getVisibility() == View.VISIBLE) {
            containerOptional.setVisibility(View.GONE);
            ivToggleOptional.setRotation(0);
        } else {
            containerOptional.setVisibility(View.VISIBLE);
            ivToggleOptional.setRotation(180);
        }
    }

    private void setupEditMode() {
        containerStep1.setVisibility(View.GONE);
        containerStep2.setVisibility(View.VISIBLE);

        etTaskTitle.setText(editingTask.getTitle());
        tvDeadlineValue.setText(editingTask.getDeadline());
        cbCompleted.setChecked(editingTask.isChecked());
        etTaskNote.setText(editingTask.getNote());

        boolean isReminder = editingTask.isReminderEnabled();
        cbReminder.setChecked(isReminder);

        selectedPriority = editingTask.getPriority();
        updatePriorityText(selectedPriority);

        btnDelete.setVisibility(View.VISIBLE);
        btnDelete.setOnClickListener(v -> deleteTask());

        // Determine type based on subjectId
        if (editingTask.getSubjectId() > 0) {
            rowSubject.setVisibility(View.VISIBLE);
            rowActivityGroup.setVisibility(View.GONE);
            rgTaskType.check(R.id.rb_study);
            selectedActivityGroup = "Học tập";
        } else {
            rowSubject.setVisibility(View.GONE);
            rowActivityGroup.setVisibility(View.VISIBLE);
            rgTaskType.check(R.id.rb_extra);
            selectedActivityGroup = editingTask.getCategory();
            if (selectedActivityGroup == null || selectedActivityGroup.isEmpty()
                    || selectedActivityGroup.equals("Học tập")) {
                selectedActivityGroup = "Cá nhân";
            }
            tvActivityGroupValue.setText(selectedActivityGroup);
        }

        parseDeadline(editingTask.getDeadline());
    }

    private void parseDeadline(String deadline) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            calendar.setTime(sdf.parse(deadline));
            updateTimeText();
        } catch (Exception e) {
            try {
                SimpleDateFormat sdfDateOnly = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                calendar.setTime(sdfDateOnly.parse(deadline));
                tvTimeValue.setText("Tắt");
            } catch (Exception e2) {
                Log.e("TaskCreateSheet", "Error parsing deadline", e2);
            }
        }
    }

    private void observeSubjects() {
        viewModel.getAllSubjects().observe(getViewLifecycleOwner(), subjectsList -> {
            if (subjectsList != null) {
                this.subjects = subjectsList;
                if (editingTask != null) {
                    updateSelectedSubjectInEditMode();
                }
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

    private void updatePriorityText(String priority) {
        switch (priority) {
            case "high":
                tvPriorityValue.setText("Cao");
                break;
            case "medium":
                tvPriorityValue.setText("Trung bình");
                break;
            case "low":
            default:
                tvPriorityValue.setText("Thấp");
                break;
        }
    }

    private void showActivityGroupPicker() {
        String[] groups = { "CLB", "Tình nguyện", "Hiến máu", "Thể thao", "Cá nhân" };
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn nhóm hoạt động")
                .setItems(groups, (dialog, which) -> {
                    selectedActivityGroup = groups[which];
                    tvActivityGroupValue.setText(selectedActivityGroup);
                }).show();
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDeadlineText();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDeadlineText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDeadlineValue.setText(sdf.format(calendar.getTime()));
    }

    private void showTimePicker() {
        new android.app.TimePickerDialog(requireContext(),
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                (view, hourOfDay, minute) -> {
                    // Validate: Check if selected time is not in the past
                    Calendar selectedDateTime = Calendar.getInstance();
                    selectedDateTime.setTimeInMillis(calendar.getTimeInMillis());
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);

                    long selectedTime = selectedDateTime.getTimeInMillis();
                    long currentTime = System.currentTimeMillis();

                    if (selectedTime < currentTime) {
                        // Time is in the past
                        Toast.makeText(getContext(), " Thời gian nhắc nhở không thể ở quá khứ", Toast.LENGTH_SHORT)
                                .show();
                        cbReminder.setChecked(false);
                        updateTimeText();
                        return;
                    }

                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    cbReminder.setChecked(true);
                    updateTimeText();
                }, 0, 0, true) // Always show 0:00 (00:00)
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
            cbReminder.setChecked(false);
            updateTimeText();
        } else {
            showTimePicker();
        }
    }

    private void showSubjectPicker() {
        if (subjects == null) {
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
                        showCreateSubjectDialog();
                    } else {
                        selectedSubject = subjects.get(which);
                        tvSubjectValue.setText(selectedSubject.name);
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
                        Subject newSubject = new Subject("", name);
                        viewModel.insertSubject(newSubject, createdSubject -> {
                            if (createdSubject != null && getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    selectedSubject = createdSubject;
                                    tvSubjectValue.setText(createdSubject.name);
                                    Toast.makeText(getContext(), "Đã tạo môn học mới", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showPriorityPicker() {
        String[] priorities = { "Thấp", "Trung bình", "Cao" };
        String[] priorityValues = { "low", "medium", "high" };

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn mức độ ưu tiên")
                .setItems(priorities, (dialog, which) -> {
                    selectedPriority = priorityValues[which];
                    tvPriorityValue.setText(priorities[which]);
                }).show();
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên task", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isStudy = rowSubject.getVisibility() == View.VISIBLE;
        int subjectId = isStudy && selectedSubject != null ? selectedSubject.id : 0;
        String category = isStudy ? "Học tập" : selectedActivityGroup;

        if (editingTask != null) {
            // Update mode
            com.example.planner.data.model.Task task = new com.example.planner.data.model.Task(
                    title,
                    calendar.getTimeInMillis(),
                    subjectId);
            task.id = editingTask.getId();
            task.isCompleted = cbCompleted.isChecked();

            // Set expiry: 2 days if completed
            if (task.isCompleted) {
                task.expiryTimestamp = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L);
            } else {
                task.expiryTimestamp = 0;
            }
            Log.d("TaskCreateSheet", "Saving updated task with expiry: " + task.expiryTimestamp);

            task.priority = selectedPriority;
            task.isReminderEnabled = cbReminder.isChecked();
            task.note = etTaskNote.getText().toString().trim();
            task.category = category;

            viewModel.update(task, () -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        dismiss();
                        if (getActivity() instanceof TaskActivity) {
                            ((TaskActivity) getActivity()).fetchTasksFromServer();
                        } else if (getActivity() instanceof com.example.planner.ui.main.MainActivity) {
                            // Optional: Trigger refresh for MainActivity if needed,
                            // though LiveData should handle it.
                        }
                    });
                }
            });
        } else {
            // Create mode
            Log.d("TaskCreateSheet",
                    "Bắt đầu lưu Task: " + title + " | SubjectID: " + subjectId + " | Priority: " + selectedPriority);

            com.example.planner.data.model.Task newTask = new com.example.planner.data.model.Task(
                    title,
                    calendar.getTimeInMillis(),
                    subjectId);
            newTask.isCompleted = cbCompleted.isChecked();

            // Set expiry: 2 days if completed
            if (newTask.isCompleted) {
                newTask.expiryTimestamp = System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000L);
            } else {
                newTask.expiryTimestamp = 0;
            }
            Log.d("TaskCreateSheet", "Saving new task with expiry: " + newTask.expiryTimestamp);

            newTask.priority = selectedPriority;
            newTask.isReminderEnabled = cbReminder.isChecked();
            newTask.note = etTaskNote.getText().toString().trim();
            newTask.category = category;

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

    private void deleteTask() {
        if (editingTask == null)
            return;

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa nhiệm vụ")
                .setMessage("Bạn có chắc chắn muốn xóa nhiệm vụ này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.delete(editingTask.getId(), () -> {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                dismiss();
                                Toast.makeText(getContext(), "Đã xóa nhiệm vụ", Toast.LENGTH_SHORT).show();
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
}
