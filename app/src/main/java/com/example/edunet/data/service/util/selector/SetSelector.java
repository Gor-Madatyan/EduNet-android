package com.example.edunet.data.service.util.selector;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class SetSelector<T> implements Selector<T> {
    private final HashSet<T> set = new HashSet<>();
    private final MutableLiveData<Integer> mutableLiveData = new MutableLiveData<>();

    @Override
    public void select(@NonNull T key) {
        set.add(key);
        onChange();
    }

    @Override
    public void selectAll(Iterator<T> iterator) {
        while (iterator.hasNext()) {
            T key = iterator.next();
            set.add(key);
        }
        onChange();
    }

    @Override
    public void unselect(@NonNull T key) {
        set.remove(key);
        onChange();
    }

    @Override
    public void reset() {
        set.clear();
        onChange();
    }

    @Override
    public boolean isSelected(@NonNull T key) {
        return set.contains(key);
    }

    @NonNull
    @Override
    public List<T> getSelections() {
        return new ArrayList<>(set);
    }

    @Override
    public int size() {
        return set.size();
    }

    @NonNull
    @Override
    public LiveData<Integer> getLiveData() {
        return mutableLiveData;
    }

    private void onChange(){
        mutableLiveData.setValue(set.size());
    }
}
