package com.example.edunet.data.service.util.selector;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.Iterator;
import java.util.List;

public interface Selector<T> {
    void select(@NonNull T key);

    void selectAll(Iterator<T> iterator);

    void unselect(@NonNull T key);

    void reset();

    boolean isSelected(@NonNull T key);

    @NonNull
    List<T> getSelections();

    int size();

    @NonNull
    LiveData<Integer> getLiveData();
}
