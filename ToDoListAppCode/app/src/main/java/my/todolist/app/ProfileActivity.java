package my.todolist.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import io.github.muddz.styleabletoast.StyleableToast;
import my.todolist.app.activities.SignInActivity;
import my.todolist.app.databinding.ActivityProfileBinding;
import my.todolist.app.utilities.Constants;
import my.todolist.app.utilities.PreferenceManager;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        setProfileData();
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

    private void setProfileData() {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.numCoinsAll.setText(preferenceManager.getString(Constants.KEY_COINS));

        binding.textName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isButtonEnabled = !binding.textName.getText().toString().trim()
                        .equals(preferenceManager.getString(Constants.KEY_NAME))
                        && !binding.textName.getText().toString().trim().isEmpty();
                binding.buttonSave.setEnabled(isButtonEnabled);
                if(isButtonEnabled){
                    binding.buttonSave.setBackgroundColor(Color.parseColor("#4B8BB3"));
                }else{
                    binding.buttonSave.setBackgroundColor(Color.GRAY);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.imageLogout.setOnClickListener(v -> signOut());
        binding.buttonSave.setOnClickListener(v -> getResponseFromAlertDialog());
    }

    private void getResponseFromAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменение имени пользователя");
        builder.setMessage("Эта функция стоит 10 монет. Продолжить?");
        builder.setPositiveButton("Да", (dialog, which) -> {
            if(Integer.parseInt(preferenceManager.getString(Constants.KEY_COINS)) >= 10){
                saveData();
                binding.buttonSave.setEnabled(false);
                binding.buttonSave.setBackgroundColor(Color.GRAY);
                preferenceManager.putString(Constants.KEY_NAME, binding.textName
                        .getText().toString());
            }else{
                showToastError("Не хватает монет");
            }
        });
        builder.setNegativeButton("Нет", (dialog, which) -> {
            binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void saveData(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_NAME, binding.textName.getText().toString())
                .addOnFailureListener(e -> showToastError("Невозможно обновить имя пользователя"))
                .addOnSuccessListener(v -> {
                            preferenceManager.putString(Constants.KEY_NAME, binding.textName.getText().toString());
                            showToastSave("Имя пользователя успешно сохранено");
                        });
        String coins = Integer.toString(Integer.parseInt(
                preferenceManager.getString(Constants.KEY_COINS)) - 10);
        documentReference.update(Constants.KEY_COINS, coins)
                .addOnFailureListener(e -> showToastError("Невозможно обновить монеты"))
                .addOnSuccessListener(v -> {
                    preferenceManager.putString(Constants.KEY_COINS, coins);
                    binding.numCoinsAll.setText(coins);
                });
    }
    private void signOut(){
        showToastLogout("Производится выход из аккаунта...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToastError("Невозможно выйти из аккаунта"));
    }
    private void showToastSave(String message){
        StyleableToast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT,
                R.style.toastSave).show();
    }
    private void showToastLogout(String message){
        StyleableToast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT,
                R.style.toastLogout).show();
    }
    private void showToastError(String message){
        StyleableToast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT,
                R.style.toastError).show();
    }
}