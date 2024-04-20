package com.example.edunet.ui.screen.members;

import android.util.Log;

import androidx.annotation.NonNull;
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
    private final MutableLiveData<Role> _viewerRoleLiveData = new MutableLiveData<>();
    final LiveData<Role> viewerRoleLiveData = _viewerRoleLiveData;
    private final MutableLiveData<Boolean> _selectionModeLiveData = new MutableLiveData<>();
    final LiveData<Boolean> selectionModeLiveData = _selectionModeLiveData;
    private final MutableLiveData<ListAdapter<?, User>> _entityAdapter = new MutableLiveData<>();
    final LiveData<ListAdapter<?, User>> entityAdapterLiveData = _entityAdapter;
    private final MutableLiveData<Boolean> _isNode = new MutableLiveData<>();
    final LiveData<Boolean> isNodeLiveData = _isNode;
    private final MediatorLiveData<SelectionState> _selectionStateLiveData = new MediatorLiveData<>();
    final LiveData<SelectionState> selectionStateLiveData = _selectionStateLiveData;
    private int lastManagedItemPosition;
    final Selector<Integer> selector = new SetSelector<>();

    record SelectionState(boolean selectionMode, Role viewerRole, boolean isNode, int size) {
    }

    {
        _selectionStateLiveData.addSource(viewerRoleLiveData, viewerRole ->
                _selectionStateLiveData.setValue(new SelectionState(getSelectionMode(), viewerRole, isNode(), selector.size())
                ));
        _selectionStateLiveData.addSource(selectionModeLiveData, mode ->
                _selectionStateLiveData.setValue(new SelectionState(mode, getViewerRole(), isNode(), selector.size())
                ));
        _selectionStateLiveData.addSource(selector.getLiveData(), size -> {
            if (Objects.requireNonNull(selectionModeLiveData.getValue()))
                _selectionStateLiveData.setValue(new SelectionState(getSelectionMode(), getViewerRole(), isNode(), size));
        });
        _selectionStateLiveData.addSource(isNodeLiveData, isNode ->
                _selectionStateLiveData.setValue(new SelectionState(getSelectionMode(), getViewerRole(), isNode, selector.size()))
        );
    }

    @Inject
    MembersViewModel(CommunityService communityService, AccountService accountService) {
        this.communityService = communityService;
        this.accountService = accountService;
    }

    void setCommunity(@NonNull String communityId, @NonNull Role role, LifecycleOwner owner) {
        assert role == Role.ADMIN || role == Role.PARTICIPANT;
        communityService.observeSubCommunities(owner, communityId,
                (e, communities) -> {
                    if (e != null) {
                        Log.w(TAG, e);
                        return;
                    }
                    _isNode.setValue(communities.length == 0);
                }
        );

        if (this.role == role)
            return;
        this.role = role;

        communityService.getCommunity(communityId,
                community -> {
            String uid = accountService.getUid();
            assert uid != null;

                    _viewerRoleLiveData.setValue(community.getUserRole(uid));
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
                            ));
                },
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

    public Role getViewerRole() {
        return Objects.requireNonNullElse(viewerRoleLiveData.getValue(), Role.GUEST);
    }

    public boolean isNode() {
        return Objects.requireNonNullElse(isNodeLiveData.getValue(), false);
    }
}
