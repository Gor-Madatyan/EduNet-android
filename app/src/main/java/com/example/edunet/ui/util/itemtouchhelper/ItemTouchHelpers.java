package com.example.edunet.ui.util.itemtouchhelper;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.util.Supplier;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.function.IntConsumer;

public final class ItemTouchHelpers {


    public static ItemTouchHelper getRightSwipableItemTouchHelper(@NonNull IntConsumer onSwipe, @NonNull Supplier<Boolean> swipeEnabled, @ColorInt int backgroundColor, @NonNull Drawable icon) {
        return new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.RIGHT
                ) {
                    @Override
                    public boolean isItemViewSwipeEnabled() {
                        return swipeEnabled.get();
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        onSwipe.accept(viewHolder.getAdapterPosition());
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                        View item = viewHolder.itemView;
                        int top = item.getTop();
                        int bottom = item.getBottom();


                        ColorDrawable background = new ColorDrawable(backgroundColor);
                        background.setBounds(
                                0,
                                top,
                                Math.round(dX),
                                bottom
                        );
                        background.draw(c);

                        int iconTop = top + 55;
                        int iconBottom = bottom - 55;
                        int iconLeft = 10;
                        int iconRight = (iconBottom - iconTop + iconLeft);

                        icon.setBounds(
                                iconLeft,
                                iconTop,
                                iconRight,
                                iconBottom
                        );
                        icon.draw(c);
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                });
    }

}
