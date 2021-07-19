package in.ac.iitm.sapp;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    SharedPreferences pref;
    public static String APP;
    private boolean darkTheme = false;

    @SuppressLint({"InlinedApi", "ApplySharedPref"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        APP = this.getPackageName();

        pref = getSharedPreferences(APP, Context.MODE_PRIVATE);
        boolean loggedIn = pref.getBoolean("loggedIn", false);
        darkTheme = pref.getBoolean("darkTheme", false);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (darkTheme) {
                setTheme(R.style.AppThemeDark);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackDark));
                window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorBackDark));
            } else {
                setTheme(R.style.AppTheme);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorBack));
                window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorBack));
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TypedValue tV = new TypedValue();
        getTheme().resolveAttribute(R.attr.splashAnimSrc, tV, false);
        LottieAnimationView splashAnim = findViewById(R.id.splash_anim_view);
        if(darkTheme)  {
            splashAnim.setAnimation(R.raw.splash_anim_dark);
        }  else  {
            splashAnim.setAnimation(R.raw.splash_anim);
        }
        splashAnim.playAnimation();

        Intent intent;
        if(loggedIn)  {
            //start main activity
            intent = new Intent(getApplicationContext(), NavigatorActivity.class);
        }  else  {
            pref.edit().putBoolean("loggedIn", false).commit();
            //start login activity
            intent = new Intent(getApplicationContext(), LoginActivity.class);
        }

        final Intent finalIntent = intent;
        splashAnim.addAnimatorUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            boolean started = false;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(animation.getAnimatedFraction() > 0.4 && !started)  {
                    started = true;
                    (new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(finalIntent);
                            finish();
                        }
                    })).start();
                }
            }
        });

    }
}
