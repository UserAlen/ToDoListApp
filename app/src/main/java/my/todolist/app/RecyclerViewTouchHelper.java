package my.todolist.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import io.github.muddz.styleabletoast.StyleableToast;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;
import my.todolist.app.adapters.ToDoAdapter;

public class RecyclerViewTouchHelper extends ItemTouchHelper.SimpleCallback{
    private ToDoAdapter adapter;

    public RecyclerViewTouchHelper(ToDoAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        //if (direction == ItemTouchHelper.RIGHT){
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setTitle("Удаление задания");
            builder.setMessage("Удалить?");
            builder.setPositiveButton("Да", (dialog, which) -> {
                adapter.deleteTask(position);
            });
            builder.setNegativeButton("Нет", (dialog, which) -> {
                adapter.notifyItemChanged(position);
            });
            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
        //}
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeLeftBackgroundColor(Color.parseColor("#B00020"))
                .addSwipeLeftActionIcon(R.drawable.ic_delete_white)
                .addSwipeRightBackgroundColor(Color.parseColor("#B00020"))
                .addSwipeRightActionIcon(R.drawable.ic_delete_white)
                .create()
                .decorate();
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
