package my.todolist.app.loadings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import my.todolist.app.R;
import my.todolist.app.activities.SignInActivity;
import my.todolist.app.databinding.ActivityLoadingScreenBinding;

public class LoadingScreenActivity extends AppCompatActivity {

    ActivityLoadingScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoadingScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Animation animation = AnimationUtils.loadAnimation(this,R.anim.anim);
        binding.picture.startAnimation(animation);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(LoadingScreenActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
    protected void onDestroy(){
        super.onDestroy();
    };
}