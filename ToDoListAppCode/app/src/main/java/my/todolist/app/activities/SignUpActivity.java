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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import io.github.muddz.styleabletoast.StyleableToast;
import my.todolist.app.ProfileActivity;
import my.todolist.app.R;
import my.todolist.app.databinding.ActivitySignUpBinding;
import my.todolist.app.utilities.Constants;
import my.todolist.app.utilities.PreferenceManager;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        setListenersForInputData();
        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValidSignUpDetails()){
                signUp();
            }
        });
    }

    private void setListenersForInputData() {
        binding.inputName.addTextChangedListener(loginTextWatcher);
        binding.inputEmail.addTextChangedListener(loginTextWatcher);
        binding.inputPassword.addTextChangedListener(loginTextWatcher);
        binding.inputConfirmPassword.addTextChangedListener(loginTextWatcher);
    }

    private final TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean isButtonEnabled = !binding.inputName.getText().toString().trim().isEmpty()
                    && !binding.inputEmail.getText().toString().trim().isEmpty()
                    && !binding.inputPassword.getText().toString().trim().isEmpty()
                    && !binding.inputConfirmPassword.getText().toString().trim().isEmpty();
            binding.buttonSignUp.setEnabled(isButtonEnabled);
            if(isButtonEnabled){
                binding.buttonSignUp.setBackgroundColor(Color.parseColor("#4B8BB3"));
            }else{
                binding.buttonSignUp.setBackgroundColor(Color.GRAY);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    //не работает
    /* private boolean isEmailAlreadyExist() {
       boolean isEmailExist = false;
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0){
                    }
                });
        return isEmailExist;
    }*/

    private void showToast(String message){
        StyleableToast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT,
                R.style.toastError).show();
    }
    private void signUp(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_COINS, "0");
        user.put(Constants.KEY_IMAGE, null);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, null);
                    preferenceManager.putString(Constants.KEY_COINS, "0");
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception ->{
                    loading(false);
                    showToast(exception.getMessage());
                });
    }
    private Boolean isValidSignUpDetails(){
        if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Введите корректный адрес электронной почты");
            return false;
        }/*else if(isEmailAlreadyExist()){
            showToast("Данный адрес электронной почты уже существует");
            return false;
        }*/else if(!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())){
            showToast("Пароли должны быть одинаковыми");
            return false;
        }else{
            return true;
        }
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}