package com.example.edunet.ui.screen.adminpanel.members;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.R;
import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.Role;
import com.example.edunet.data.service.model.User;
import com.example.edunet.data.service.util.selector.Selector;
import com.example.edunet.data.service.util.selector.SetSelector;
import com.example.edunet.ui.util.adapter.ListAdapter;
import com.example.edunet.ui.util.adapter.impl.EntityAdapter;
import com.example.edunet.ui.util.adapter.impl.LazyAdapter;
import com.example.edunet.ui.util.adapter.impl.SelectableAdapter;
import com.example.edunet.ui.util.adapter.util.ListAdapterUtils;
import com.example.edunet.ui.util.adapter.util.SelectableAdapterUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MembersViewModel extends ViewModel {
    private static final String TAG = MembersViewModel.class.getSimpleName();
    private static final int PAGINATOR_LIMIT = 20;
    private final CommunityService communityService;
    private final AccountService accountService;
    private Role role;
    private final MutableLiveData<Boolean> _selectionModeLiveData = new MutableLiveData<>();
    final LiveData<Boolean> selectionModeLiveData = _selectionModeLiveData;
    LiveData<ListAdapter<?, User>> entityAdapterLiveData;
    LiveData<Boolean> isNodeLiveData;
    private final MediatorLiveData<Pair<Boolean, Integer>> _selectionLiveData = new MediatorLiveData<>();
    final LiveData<Pair<Boolean, Integer>> selectionLiveData = _selectionLiveData;
    private int lastManagedItemPosition;
    final Selector<Integer> selector = new SetSelector<>();

    {
        _selectionLiveData.addSource(selectionModeLiveData, mode ->
                _selectionLiveData.setValue(new Pair<>(mode, selector.size()))
        );
        _selectionLiveData.addSource(selector.getLiveData(), size -> {
            if (Objects.requireNonNull(selectionModeLiveData.getValue()))
                _selectionLiveData.setValue(new Pair<>(selectionModeLiveData.getValue(), size));
        });
    }

    @Inject
    MembersViewModel(CommunityService communityService, AccountService accountService) {
        this.communityService = communityService;
        this.accountService = accountService;
    }

    void setCommunity(@NonNull String communityId, @NonNull Role role, LifecycleOwner owner) {
        assert role == Role.ADMIN || role == Role.PARTICIPANT;
        MutableLiveData<Boolean> _isNode = new MutableLiveData<>();
        isNodeLiveData = _isNode;

        communityService.observeSubCommunities(owner, communityId,
                (e, communities) -> {
                    if(e != null){
                        Log.w(TAG,e);
                        return;
                    }
                    _isNode.setValue(communities.length == 0);
                }
        );

        if (this.role == role)
            return;
        this.role = role;
        MutableLiveData<ListAdapter<?, User>> _entityAdapter = new MutableLiveData<>();
        entityAdapterLiveData = _entityAdapter;

        communityService.getCommunity(communityId,
                community ->
                        _entityAdapter.setValue(
                                new LazyAdapter<>(
                                        new SelectableAdapter<>(
                                                new EntityAdapter<>(
                                                        new ArrayList<>(),
                                                        R.layout.selectable_name_avatar_element,
                                                        (item, data) ->
                                                                item.setOnClickListener(
                                                                        v -> {
                                                                            if (getSelectionMode()) {
                                                                                SelectableAdapterUtils.toggle(data.getPosition(), selector, entityAdapterLiveData.getValue());
                                                                            }
                                                                        }
                                                                )
                                                ),
                                                selector
                                        )
                                        , accountService.getUserArrayPaginator(
                                        (role == Role.ADMIN ? community.getAdmins() : community.getParticipants()).toArray(new String[0]),
                                        PAGINATOR_LIMIT)
                                )),
                e ->
                        Log.e(TAG, e.toString())
        );
    }

    void toggleSelectionMode() {
        boolean value = !Objects.requireNonNullElse(_selectionModeLiveData.getValue(), false);
        _selectionModeLiveData.setValue(value);
    }

    public int getLastManagedItemPosition() {
        return lastManagedItemPosition;
    }

    public void setLastManagedItemPosition(int lastManagedItemPosition) {
        this.lastManagedItemPosition = lastManagedItemPosition;
    }

    public void removeLastManagedItem() {
        ListAdapterUtils.remove(Objects.requireNonNull(entityAdapterLiveData.getValue()), getLastManagedItemPosition());
    }

    public void removeSelections() {
        assert role == Role.PARTICIPANT;
        ListAdapter<?, User> entityAdapter = entityAdapterLiveData.getValue();
        assert entityAdapter != null;
        List<Integer> selected = selector.getSelections();
        selected.sort(Collections.reverseOrder());

        for (int position : selected)
            ListAdapterUtils.remove(entityAdapter, position);

        selector.reset();
    }

    public boolean getSelectionMode() {
        return Objects.requireNonNullElse(selectionModeLiveData.getValue(), false);
    }

    public boolean isNode() {
        return Objects.requireNonNullElse(isNodeLiveData.getValue(), false);
    }
}
