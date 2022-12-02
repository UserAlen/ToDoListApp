package my.todolist.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.github.muddz.styleabletoast.StyleableToast;
import my.todolist.app.ProfileActivity;
import my.todolist.app.R;
import my.todolist.app.RecyclerViewTouchHelper;
import my.todolist.app.adapters.ToDoAdapter;
import my.todolist.app.databinding.ActivityMainBinding;
import my.todolist.app.models.ToDoModel;
import my.todolist.app.utilities.Constants;
import my.todolist.app.utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity implements ToDoAdapter.OnTaskListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<ToDoModel> toDoList;
    private ToDoAdapter toDoAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        getToken();
        init();
        setListeners();
        setAnimation();
        listenTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.numCoins.setText(preferenceManager.getString(Constants.KEY_COINS));
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        toDoList = new ArrayList<>();
        toDoAdapter = new ToDoAdapter(
                toDoList,
                this,
                preferenceManager,
                this
        );
        binding.tasksRecyclerView.setAdapter(toDoAdapter);
        binding.tasksRecyclerView.setVisibility(View.VISIBLE);
        binding.numCoins.setText(preferenceManager.getString(Constants.KEY_COINS));
        database = FirebaseFirestore.getInstance();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerViewTouchHelper(toDoAdapter));
        itemTouchHelper.attachToRecyclerView(binding.tasksRecyclerView);
    }

    private void addTask(String taskName){
        HashMap<String, Object> task = new HashMap<>();
        task.put(Constants.KEY_OWNER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        task.put(Constants.KEY_TIMESTAMP, new Date());
        task.put(Constants.KEY_TASK, taskName); //from popup window
        task.put(Constants.KEY_IS_CHECKED, "0"); //изначально чекбокс пустой
        database.collection(Constants.KEY_COLLECTION_TASKS).add(task);
    }

    private void listenTasks(){
        database.collection(Constants.KEY_COLLECTION_TASKS)
                .whereEqualTo(Constants.KEY_OWNER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        //binding.textList.setVisibility(View.GONE);
        if(error != null){
            return;
        }
        if(value != null){
            //binding.textList.setVisibility(View.GONE);
            int count = toDoList.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    ToDoModel toDoModel= new ToDoModel();
                    toDoModel.ownerId = documentChange.getDocument().getString(Constants.KEY_OWNER_ID);
                    toDoModel.task = documentChange.getDocument().getString(Constants.KEY_TASK);
                    toDoModel.isChecked = documentChange.getDocument().getString(Constants.KEY_IS_CHECKED);
                    toDoModel.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    toDoModel.taskId = documentChange.getDocument().getId();
                    toDoList.add(toDoModel);
                }/*else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for(int i = 0; i < toDoList.size(); i++){
                        String taskId = documentChange.getDocument().getId();
                        if(toDoList.get(i).taskId.equals(taskId)){
                            toDoList.get(i).isChecked = documentChange.getDocument().getString(Constants.KEY_IS_CHECKED);
                            break;
                        }
                    }
                }*/
            }
            Collections.sort(toDoList, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0){
                toDoAdapter.notifyDataSetChanged();
            }else{
                toDoAdapter.notifyItemRangeInserted(toDoList.size(), toDoList.size());
            }
            binding.tasksRecyclerView.setVisibility(View.VISIBLE);
        }
    };
    private void setAnimation(){
        Fade fade = new Fade();
        View decor = getWindow().getDecorView();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
    }

    private void setListeners() {
        binding.profilePic.setOnClickListener(view -> {
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                    MainActivity.this,
                            binding.profilePic,
                            ViewCompat.getTransitionName(binding.profilePic));
            startActivity(new Intent(getApplicationContext(),
                    ProfileActivity.class), options.toBundle());
                });
        binding.buttonNewTask.setOnClickListener(v -> openDialogNewTask());
    }

    private void openDialogNewTask(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.add_new_task_container, null);
        dialogBuilder.setView(popupView);
        AlertDialog dialogNewTask = dialogBuilder.create();
        EditText taskName = popupView.findViewById(R.id.task);
        AppCompatButton buttonSave = popupView.findViewById(R.id.buttonSave);
        dialogNewTask.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogNewTask.show();
        dialogNewTask.setCancelable(false);
        taskName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isButtonEnabled = !taskName.getText().toString().trim().isEmpty();
                buttonSave.setEnabled(isButtonEnabled);
                if(isButtonEnabled){
                    buttonSave.setBackgroundColor(Color.parseColor("#4B8BB3"));
                }else{
                    buttonSave.setBackgroundColor(Color.GRAY);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        buttonSave.setOnClickListener(view -> {
            addTask(taskName.getText().toString());
            dialogNewTask.dismiss();
        });
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Невозможно обновить токен"));
    }
    private void showToast(String message){
        StyleableToast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT,
                R.style.toastError).show();
    }

    private void showToastCoin(String message, boolean isCoinAdded){
        if(isCoinAdded){
            StyleableToast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT,
                    R.style.toastCoinAdd).show();
        }else{
            StyleableToast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT,
                    R.style.toastCoinNotAdd).show();
        }
    }

    @Override
    public void onTaskClick(int position) {
        toDoList.get(position);
        String prevCoins = preferenceManager.getString(Constants.KEY_PREVIOUS_COINS);
        String coins = preferenceManager.getString(Constants.KEY_COINS);
        if(Integer.parseInt(prevCoins) > Integer.parseInt(coins)){
            showToastCoin("Вычтена 1 монета", false);
        }else if (Integer.parseInt(prevCoins) < Integer.parseInt(coins)){
            showToastCoin("Добавлена 1 монета", true);
        }
        binding.numCoins.setText(preferenceManager.getString(Constants.KEY_COINS));
    }

}
