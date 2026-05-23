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

    private View containerStep1, containerStep2, containerOptional;
    private RadioGroup rgTaskType;
    private EditText etTaskTitle, etTaskNote;
    private TextView tvDeadlineValue, tvTimeValue, tvSubjectValue, tvPriorityValue, tvActivityGroupValue;
    private View rowSubject, rowActivityGroup, rowDeadline, rowTime, rowPriority, btnToggleOptional;
    private ImageView ivToggleOptional;
    private CheckBox cbCompleted, cbReminder;

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
        tvActivityGroupValue = view.findViewById(R.id.tv_activity_group_value);

        rowSubject = view.findViewById(R.id.row_subject);
        rowActivityGroup = view.findViewById(R.id.row_activity_group);
        rowDeadline = view.findViewById(R.id.row_deadline);
        rowTime = view.findViewById(R.id.row_time);
        rowPriority = view.findViewById(R.id.row_priority);

        btnToggleOptional = view.findViewById(R.id.btn_toggle_optional);
        ivToggleOptional = view.findViewById(R.id.iv_toggle_optional);

        cbCompleted = view.findViewById(R.id.cb_completed);
        cbReminder = view.findViewById(R.id.cb_reminder);
        btnDelete = view.findViewById(R.id.btn_delete_task);

        updateDeadlineText();
        updateTimeText();
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.btn_continue).setOnClickListener(v -> handleContinue());

        rowDeadline.setOnClickListener(v -> showDatePicker());
        rowTime.setOnClickListener(v -> toggleReminder());
        rowSubject.setOnClickListener(v -> showSubjectPicker());
        rowActivityGroup.setOnClickListener(v -> showActivityGroupPicker());
        rowPriority.setOnClickListener(v -> showPriorityPicker());

        btnToggleOptional.setOnClickListener(v -> toggleOptionalSection());

        view.findViewById(R.id.btn_save_task).setOnClickListener(v -> saveTask());
    }

    private void handleContinue() {
        String title = etTaskTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên công việc", Toast.LENGTH_SHORT).show();
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
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
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

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên task", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isStudy = rowSubject.getVisibility() == View.VISIBLE;
        int subjectId = isStudy && selectedSubject != null ? selectedSubject.id : 0;
        String category = isStudy ? "Học tập" : selectedActivityGroup;

        Task task = new Task(title, calendar.getTimeInMillis(), subjectId);
        if (editingTask != null)
            task.id = editingTask.getId();

        task.isCompleted = cbCompleted.isChecked();
        task.priority = selectedPriority;
        task.isReminderEnabled = cbReminder.isChecked();
        task.note = etTaskNote.getText().toString().trim();
        task.category = category;

        if (editingTask != null) {
            viewModel.update(task, this::onSaveSuccess);
        } else {
            viewModel.saveTask(task, this::onSaveSuccess);
        }
    }

    private void onSaveSuccess() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                dismiss();
                Toast.makeText(getContext(), "Đã lưu thành công", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof TaskActivity) {
                    ((TaskActivity) getActivity()).fetchTasksFromServer();
                }
            });
        }
    }
}
