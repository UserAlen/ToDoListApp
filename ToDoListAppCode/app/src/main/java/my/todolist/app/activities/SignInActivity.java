package my.todolist.app.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import io.github.muddz.styleabletoast.StyleableToast;
import my.todolist.app.ProfileActivity;
import my.todolist.app.R;
import my.todolist.app.databinding.ActivitySignInBinding;
import my.todolist.app.utilities.Constants;
import my.todolist.app.utilities.PreferenceManager;

public class SignInActivity extends AppCompatActivity {

    ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        setAnimation();
    }

    private void setAnimation(){
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }

    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        setListenersForInputData();
        binding.buttonSignIn.setOnClickListener(v ->{
            if (isValidSignInDetails()){
                signIn();
            }
        });
    }
    private void setListenersForInputData() {
        binding.inputEmail.addTextChangedListener(loginTextWatcher);
        binding.inputPassword.addTextChangedListener(loginTextWatcher);
    }

    private final TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean isButtonEnabled = !binding.inputEmail.getText().toString().trim().isEmpty()
                    && !binding.inputPassword.getText().toString().trim().isEmpty();
            binding.buttonSignIn.setEnabled(isButtonEnabled);
            if(isButtonEnabled){
                binding.buttonSignIn.setBackgroundColor(Color.parseColor("#4B8BB3"));
            }else{
                binding.buttonSignIn.setBackgroundColor(Color.GRAY);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void signIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(this::onComplete);
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message){
        StyleableToast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT,
                R.style.toastError).show();
    }
    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Введите адрес электронной почты");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Введите корректный адрес электронной почты");
            return false;
        }else{
            return true;
        }
    }

    private void onComplete(Task<QuerySnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null
                && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
            preferenceManager.putString(Constants.KEY_COINS, documentSnapshot.getString(Constants.KEY_COINS));
            preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            loading(false);
            showToast("Такого пользователя не существует");
        }
    }
}