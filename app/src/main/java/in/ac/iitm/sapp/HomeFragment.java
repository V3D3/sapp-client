package in.ac.iitm.sapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class HomeFragment extends Fragment implements TaskEventManagement {
    private static final String TAG = "HOME";

    private boolean darkTheme = false;

    private JSONObject homeData;
    private JSONObject personalData;
    private JSONArray categorySource;
    private JSONArray taskSource;
    private View thisView;
    private View fadeOverlay;
    private CardView hamMenu;
    private ArrayList<TaskData> tasks = new ArrayList<>();
    private ArrayList<CategoryData> categories = new ArrayList<>();
    private Map<Integer, Integer> categoryColor = new HashMap<>();
    private Map<Integer, String> categoryName = new HashMap<>();

    private int totalTasks = 0, completedTasks = 0, leftTasks = 0;

    //TODO: Replace seperator views with background drawables on HamMenu Frames

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        fadeOverlay = view.findViewById(R.id.fade_overlay);
        hamMenu = view.findViewById(R.id.ham_frame);
        thisView = view;

        try {
            homeData = new JSONObject(getArguments().getString("homedata"));
            darkTheme = getArguments().getBoolean("darktheme");
            loadIndividualData();
            calculateTaskInts();
            setGreettexts();
            setDate();
            bindSwitchTheme();
            if(totalTasks > 0)  {
                if(categorySource.length() == 1) {
                    setupForOneCat();
                }
                setupNormal();
                renderCategoryList();
                renderTaskList();
            }  else  {
                setupForZeroTasks();
                //set up for 0 tasks
            }

            bindHamToggle();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Invalid JSON passed for homedata");
        }
    }

    private boolean loadIndividualData()  {
        try {
            personalData = homeData.getJSONObject("personal");
            categorySource = homeData.getJSONArray("task_categories");
            taskSource = homeData.getJSONArray("tasks");

            JSONObject currCat;
            for(int i = 0; i < categorySource.length(); i++)  {
                currCat = categorySource.getJSONObject(i);
                categoryName.put(currCat.getInt("id"), currCat.getString("name"));
                categoryColor.put(currCat.getInt("id"), Color.parseColor(currCat.getString("color")));
                //TODO: Refactor data-scheme so index is id, ensure order consistency on server end? (if parse)
                categories.add(new CategoryData(currCat));
            }
            for(int i = 0; i < taskSource.length(); i++)  {
                tasks.add(new TaskData(taskSource.getJSONObject(i)));
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading personal data.");
            ((NavigatorActivity)getActivity()).logout();
            return false;
        }
    }
    private void calculateTaskInts() throws JSONException {
        for(int i = 0; i < categorySource.length(); i++)  {
            totalTasks += categorySource.getJSONObject(i).getJSONArray("tasks").length();
            completedTasks += categorySource.getJSONObject(i).getJSONArray("completed").length();
        }
        leftTasks = totalTasks - completedTasks;
    }

    private void setGreettexts() throws JSONException  {
        //main greettext
        ((TextView)thisView.findViewById(R.id.profile_greettext)).setText(getString(R.string.home_greettext, personalData.getString("name")));

        //subtext
        String subText;
        if(totalTasks == 0)  {
            subText = getString(R.string.home_subtext_zero);
        }  else  {
            if(totalTasks == 1)  {
                subText = getString(R.string.home_subtext, "1", "");
            }  else  {
                subText = getString(R.string.home_subtext, String.valueOf(totalTasks), "s");
            }
        }
        ((TextView)thisView.findViewById(R.id.profile_subtext)).setText(subText);
    }
    private void setDate()  {
        Date c = Calendar.getInstance().getTime();
        ((TextView) thisView.findViewById(R.id.date_label)).setText((new SimpleDateFormat("dd MMMM yyyy")).format(c));
    }
    private void bindHamToggle()  {
        final ImageView hamToggle = thisView.findViewById(R.id.ham_toggle);
        thisView.findViewById(R.id.ham_toggle_frame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hamToggle.isEnabled())  {
                    hamToggled(false);
                }  else  {
                    hamToggled(true);
                }
            }
        });
    }
    private void bindSwitchTheme()  {
        if(darkTheme)  {
            ((TextView) thisView.findViewById(R.id.ham_darktheme_label)).setText("Light Mode");
            ((ImageView) thisView.findViewById(R.id.ham_darktheme_icon)).setEnabled(false);
        }
        ((ConstraintLayout) thisView.findViewById(R.id.ham_darktheme)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((NavigatorActivity) getActivity()).switchTheme();
            }
        });
    }

    private void setupForOneCat() {
        //just enable 1-cat-img
    }
    private void setupForZeroTasks()  { }
    private void setupNormal()  {
        thisView.findViewById(R.id.overall_progress_help).setAlpha(0);
        thisView.findViewById(R.id.overall_progress_hint).setAlpha(1);
        thisView.findViewById(R.id.overall_progress_indic).setAlpha(1);

        ((TextView)thisView.findViewById(R.id.overall_progress_indic)).setText(getString(R.string.home_overall_progress_indic, (int)(((double) completedTasks / totalTasks) * 100)));
        ((ProgressBar)thisView.findViewById(R.id.overall_progress)).setProgress((int)(((double) completedTasks / totalTasks) * 100));
    }

    private void renderCategoryList()  {
        LinearLayoutManager linLayMan = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        RecyclerView categoryList = thisView.findViewById(R.id.cat_list);
        categoryList.setLayoutManager(linLayMan);
        CategoryAdapter adapter = new CategoryAdapter(getActivity(), categories);
        categoryList.setAdapter(adapter);
    }
    private void renderTaskList()  {
        LinearLayoutManager linLayMan2 = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        RecyclerView taskList = thisView.findViewById(R.id.tasks_list);
        taskList.setLayoutManager(linLayMan2);
        TaskAdapter adapter2 = new TaskAdapter(getActivity(), tasks, categoryColor, categoryName);
        taskList.setAdapter(adapter2);
    }

    private void hamToggled(boolean reqstate)  {
        getView().findViewById(R.id.ham_toggle).setEnabled(reqstate);
        if(!reqstate)  {
            ((NavigatorActivity)getActivity()).dimNavbar();
            dimHome();
            openHam();
        }  else  {
            ((NavigatorActivity)getActivity()).undimNavbar();
            undimHome();
            closeHam();
        }
    }
    private void openHam()  {
        final float scale = getResources().getDisplayMetrics().density;
        long duration = getResources().getInteger(R.integer.home_ham_duration);

        final int height = (int) (216 * scale + 0.5f);

        hamMenu.setVisibility(VISIBLE);

        final ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float perc = animation.getAnimatedFraction();
                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) hamMenu.getLayoutParams();
                lp.height = (int) (height * perc) + 1;
                hamMenu.setLayoutParams(lp);
            }
        });
        anim.setDuration(duration);
        anim.start();
    }
    private void closeHam()  {
        final float scale = getResources().getDisplayMetrics().density;
        long duration = getResources().getInteger(R.integer.home_ham_duration);

        final int height = (int) (216 * scale + 0.5f);

        final ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float perc = 1f - animation.getAnimatedFraction();
                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) hamMenu.getLayoutParams();
                lp.height = (int) (height * perc) + 1;
                hamMenu.setLayoutParams(lp);
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hamMenu.setVisibility(GONE);
            }
        });
        anim.setDuration(duration);
        anim.start();
    }

    private void dimHome()  {
        fadeOverlay.setClickable(true);

        long duration = getResources().getInteger(R.integer.dim_anim_duration);
        float finalOpacity = Float.parseFloat(getString(R.string.float_overlay_opacity));
        ObjectAnimator fragmentFadeOut = ObjectAnimator.ofFloat(fadeOverlay, "alpha", finalOpacity);
        fragmentFadeOut.setDuration(duration);
        fragmentFadeOut.start();
    }
    private void undimHome()  {
        fadeOverlay.setClickable(false);

        long duration = getResources().getInteger(R.integer.dim_anim_duration);
        ObjectAnimator fragmentFadeIn = ObjectAnimator.ofFloat(fadeOverlay, "alpha", 0);
        fragmentFadeIn.setDuration(duration);
        fragmentFadeIn.start();
    }

    @Override
    public void setTaskCompletion(int id, boolean status) {

    }

    @Override
    public void deleteTask(int id) {

    }

    @Override
    public void editTask(int id, JSONObject updatedParams) {

    }

    @Override
    public void saveTasks() {

    }

    public class TaskData  {
        public String name;
        public String time;
        public int category;
        public boolean complete;
        public boolean anytime;
        public String date;

        public TaskData(JSONObject source)  {
            try {
                name = source.getString("name");
                time = source.getString("start_time");
                complete = source.getBoolean("completed");
                category = source.getInt("cat");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "JSON parse error in Task List building");
            }
        }
    }
    public class CategoryData  {
        public String name;
        public int totalTasks;
        public int completedTasks;
        public int color;

        public CategoryData(JSONObject source)  {
            try {
                name = categoryName.get(source.getInt("id"));
                totalTasks = source.getJSONArray("tasks").length();
                completedTasks = source.getJSONArray("completed").length();
                color = categoryColor.get(source.getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "JSON parse error in Category List building");
            }
        }
    }
}
