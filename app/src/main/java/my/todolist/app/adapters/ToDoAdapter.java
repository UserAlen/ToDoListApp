package my.todolist.app.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import io.github.muddz.styleabletoast.StyleableToast;
import my.todolist.app.R;
import my.todolist.app.activities.MainActivity;
import my.todolist.app.databinding.ItemContainerTaskBinding;
import my.todolist.app.models.ToDoModel;
import my.todolist.app.utilities.Constants;
import my.todolist.app.utilities.PreferenceManager;

public class ToDoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final List<ToDoModel> toDoList;
    private OnTaskListener mOnTaskListener;
    private static PreferenceManager preferenceManager;
    private MainActivity activity;

    public ToDoAdapter(List<ToDoModel> toDoList, OnTaskListener onTaskListener,
                       PreferenceManager preferenceManager,
                       MainActivity activity){
        this.toDoList = toDoList;
        this.mOnTaskListener = onTaskListener;
        this.preferenceManager = preferenceManager;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ToDoViewHolder(
                ItemContainerTaskBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                ),
                mOnTaskListener
        );

    }


    public Context getContext(){
        return activity;
    }

    public void deleteTask(int position){
        ToDoModel item = toDoList.get(position);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_TASKS).
                document(item.taskId).delete();
        showToast("Удалено задание \"" + toDoList.get(position).task + "\"");
        toDoList.remove(position);
        notifyItemRemoved(position);
    }

    private void showToast(String message){
        StyleableToast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT,
                R.style.toastDelete).show();
    }

    /*public void editTask(int position){
        ToDoModel item = toDoList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("id" , item.taskId);
        bundle.putString("task" , item.task);
        AddTask task = new AddTask();
        task.show(activity.getSupportFragmentManager(), "AddTask");
    }*/

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ToDoViewHolder) holder).setData(toDoList.get(position));
    }

    @Override
    public int getItemCount() {
        return toDoList.size();
    }
    static class ToDoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ItemContainerTaskBinding binding;
        OnTaskListener onTaskListener;

        ToDoViewHolder(ItemContainerTaskBinding ItemContainerTaskBinding, OnTaskListener onTaskListener){
            super(ItemContainerTaskBinding.getRoot());
            binding = ItemContainerTaskBinding;
            this.onTaskListener = onTaskListener;
            binding.checkBox.setOnClickListener(this);
        }

        void setData(ToDoModel toDoList){
            binding.checkBox.setText(toDoList.task);
            binding.checkBox.setOnCheckedChangeListener(null);//без этого список неверно работает
            binding.checkBox.setChecked(toBoolean(Integer.parseInt(toDoList.isChecked)));
            binding.checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                DocumentReference documentReference =
                        database.collection(Constants.KEY_COLLECTION_USERS).document(
                                preferenceManager.getString(Constants.KEY_USER_ID)
                        );
                DocumentReference documentReferenceCheck =
                        database.collection(Constants.KEY_COLLECTION_TASKS).document(
                                toDoList.taskId
                        );
                int coins = Integer.parseInt(preferenceManager.getString(Constants.KEY_COINS));
                preferenceManager.putString(Constants.KEY_PREVIOUS_COINS, String.valueOf(coins));
                if(isChecked){
                    coins++;
                    documentReferenceCheck.update(Constants.KEY_IS_CHECKED, "1");
                }else{
                    if(coins !=0){
                        coins--;
                    }
                    documentReferenceCheck.update(Constants.KEY_IS_CHECKED, "0");
                }
                preferenceManager.putString(Constants.KEY_COINS, Integer.toString(coins));
                documentReference.update(Constants.KEY_COINS, Integer.toString(coins));
            });
        }
        public boolean toBoolean(int num){
            return num!=0;
        }

        @Override
        public void onClick(View view) {
            onTaskListener.onTaskClick(getAdapterPosition());
        }
    }

    public interface OnTaskListener{
        void onTaskClick(int position);
    }
}
