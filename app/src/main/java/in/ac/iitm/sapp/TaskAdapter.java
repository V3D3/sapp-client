package in.ac.iitm.sapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private static final String TAG = "TaskAdapter";
    private ArrayList<HomeFragment.TaskData> tasks;
    private Map<Integer, Integer> categoryColor;
    private Map<Integer, String> categoryName;
    private float scale;
    @ColorInt private int completeTextColor;

    public TaskAdapter(Context context, ArrayList<HomeFragment.TaskData> taskSource, Map<Integer, Integer> catColS, Map<Integer, String> catNameS) {
        categoryColor = catColS;
        categoryName = catNameS;
        tasks = taskSource;
        TypedValue tV = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorTaskCompleteText, tV, true);
        completeTextColor = tV.data;
        scale = context.getResources().getDisplayMetrics().density;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.task_card, parent, false));
    }

    private int toDP(int px)  {
        return (int) ((px * scale) + 0.5f);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        int catIndex = tasks.get(position).category;
        holder.taskLabel.setText(tasks.get(position).name);
        holder.categoryLabel.setText(categoryName.get(catIndex));
        holder.categoryLabel.setTextColor(categoryColor.get(catIndex));

        int time = Integer.parseInt(tasks.get(position).time);
        String timeS, timeMinS;
        if(time % 100 < 10)  {
            timeMinS = "0" + (time % 100);
        }  else  {
            timeMinS = "" + (time % 100);
        }
        if(time >= 1200)  {
            timeS = String.format("%d:%s PM", time / 100, timeMinS);
        }  else if(time < 100)  {
            timeS = String.format("12:%s AM", timeMinS);
        }  else  {
            timeS = String.format("%d:%s AM", time / 100, timeMinS);
        }
        holder.timeLabel.setText(timeS);

        //TODO: support anytime

        if(position == getItemCount() - 1)  {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.thisView.getLayoutParams();
            lp.setMargins(toDP(30), toDP(12), toDP(30), toDP(8));
        }

        if(tasks.get(position).complete)  {
            holder.taskComplete.setEnabled(false);
            //note false is for completion, to take care of animation
            holder.taskLabel.setPaintFlags(holder.taskLabel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskLabel.setTextColor(completeTextColor);
        }  else  {
            holder.taskComplete.setEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder  {
        ImageView taskComplete;
        TextView taskLabel;
        TextView timeLabel;
        TextView categoryLabel;
        View thisView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskComplete = itemView.findViewById(R.id.task_complete);
            taskLabel = itemView.findViewById(R.id.task_title);
            timeLabel = itemView.findViewById(R.id.task_time);
            categoryLabel = itemView.findViewById(R.id.task_category);
            thisView = itemView;
        }
    }
}
