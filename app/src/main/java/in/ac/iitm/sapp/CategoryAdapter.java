package in.ac.iitm.sapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private static final String TAG = "CategoryAdapter";
    private float scale;
    private ArrayList<HomeFragment.CategoryData> categories = new ArrayList<>();

    public CategoryAdapter(Context context, ArrayList<HomeFragment.CategoryData> categorySource) {
        categories = categorySource;
        scale = context.getResources().getDisplayMetrics().density;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        int progress = (int) Math.floor(((double) categories.get(position).completedTasks * 100) / categories.get(position).totalTasks);
        if(Build.VERSION.SDK_INT >= 24)  {
            holder.progressIndic.setProgress(progress, true);
        }  else  {
            holder.progressIndic.setProgress(progress);
        }
        holder.categoryLabel.setText(categories.get(position).name);
        String s = "1 task";
        if(categories.get(position).totalTasks > 1)  {
            s = categories.get(position).totalTasks + " tasks";
        }
        holder.tasksLabel.setText(s);
        holder.progressLabel.setText(progress + "%");

	    //TODO: replace standard progressBar with https://stackoverflow.com/questions/36639660/android-circular-progress-bar-with-rounded-corners

        holder.progressLabel.setTextColor(categories.get(position).color);
        holder.progressIndic.setBackgroundTintList(ColorStateList.valueOf(categories.get(position).color));
        holder.progressIndic.setProgressTintList(ColorStateList.valueOf(categories.get(position).color));

        if(position == 0)  {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.thisView.getLayoutParams();
            lp.setMarginStart((int) ((32 * scale) + 0.5f));
        }  else if(position == getItemCount() - 1)  {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.thisView.getLayoutParams();
            lp.setMarginEnd((int) ((32 * scale) + 0.5f));
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder  {
        ProgressBar progressIndic;
        TextView progressLabel;
        TextView categoryLabel;
        TextView tasksLabel;
        CardView thisView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            progressIndic = itemView.findViewById(R.id.cat_progress_indic);
            progressLabel = itemView.findViewById(R.id.cat_progress_label);
            categoryLabel = itemView.findViewById(R.id.cat_title);
            tasksLabel = itemView.findViewById(R.id.cat_tasks);
            thisView = (CardView) itemView;
        }
    }
}
