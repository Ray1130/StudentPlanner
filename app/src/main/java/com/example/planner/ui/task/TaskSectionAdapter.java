package com.example.planner.ui.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.planner.R;
import java.util.List;

public class TaskSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<TaskUiModel> items;

    public TaskSectionAdapter(List<TaskUiModel> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TaskUiModel.TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.item_task_group_header, parent, false));
        } else if (viewType == TaskUiModel.TYPE_TABLE_HEADER) {
            return new TableHeaderViewHolder(inflater.inflate(R.layout.item_task_table_header, parent, false));
        } else if (viewType == TaskUiModel.TYPE_TABLE_ROW) {
            return new TableRowViewHolder(inflater.inflate(R.layout.item_task_table_row, parent, false));
        } else {
            return new ActionButtonViewHolder(inflater.inflate(R.layout.item_task_action_button, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskUiModel item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvGroupName.setText(item.getTitle());
        } else if (holder instanceof TableRowViewHolder) {
            TableRowViewHolder rowHolder = (TableRowViewHolder) holder;
            rowHolder.tvName.setText(item.getTitle());
            rowHolder.tvDeadline.setText(item.getDeadline());
            rowHolder.tvNote.setText(item.getNote());
            rowHolder.checkBox.setChecked(item.isChecked());
        } else if (holder instanceof ActionButtonViewHolder) {
            ActionButtonViewHolder actionHolder = (ActionButtonViewHolder) holder;
            if (item.getViewType() == TaskUiModel.TYPE_ACTION_NEW_PAGE) {
                actionHolder.tvAction.setText(R.string.action_new_page);
            } else {
                actionHolder.tvAction.setText(R.string.action_new_group);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // --- Các ViewHolders ---
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName;
        HeaderViewHolder(@NonNull View itemView) { 
            super(itemView); 
            tvGroupName = itemView.findViewById(R.id.tv_group_name); 
        }
    }

    static class TableHeaderViewHolder extends RecyclerView.ViewHolder {
        TableHeaderViewHolder(@NonNull View itemView) { super(itemView); }
    }

    static class TableRowViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDeadline, tvNote;
        CheckBox checkBox;
        TableRowViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_task_name);
            tvDeadline = itemView.findViewById(R.id.tv_task_deadline);
            tvNote = itemView.findViewById(R.id.tv_task_note);
            checkBox = itemView.findViewById(R.id.cb_task_done);
        }
    }

    static class ActionButtonViewHolder extends RecyclerView.ViewHolder {
        TextView tvAction;
        ActionButtonViewHolder(@NonNull View itemView) { 
            super(itemView); 
            tvAction = itemView.findViewById(R.id.tv_action_text); 
        }
    }
}