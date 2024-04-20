package com.example.edunet.ui.screen.graduations;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.R;
import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.User;
import com.example.edunet.ui.util.adapter.ListAdapter;
import com.example.edunet.ui.util.adapter.impl.EntityAdapter;
import com.example.edunet.ui.util.adapter.impl.GraduationAdapter;
import com.example.edunet.ui.util.adapter.impl.LazyAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GraduationsViewModel extends ViewModel {
    private static final String TAG = GraduationsViewModel.class.getSimpleName();
    private static final int PAGINATOR_LIMIT = 20;
    private final CommunityService communityService;
    private final AccountService accountService;
    private final MutableLiveData<ListAdapter<?, Object>> _graduationsAdapterLiveData = new MutableLiveData<>();
    final LiveData<ListAdapter<?, Object>> graduationsAdapterLiveData = _graduationsAdapterLiveData;
    private final MutableLiveData<ListAdapter<?, User>> _viewerClassAdapterLiveData = new MutableLiveData<>();
    final LiveData<ListAdapter<?, User>> viewerClassAdapterLiveData = _viewerClassAdapterLiveData;
    private final MutableLiveData<Boolean> _isShowingViewerClassLiveData = new MutableLiveData<>();
    final LiveData<Boolean> isShowingViewerClassLiveData = _isShowingViewerClassLiveData;

    @Inject
    public GraduationsViewModel(CommunityService communityService, AccountService accountService) {
        this.communityService = communityService;
        this.accountService = accountService;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void setCommunity(String cid) {
        if (graduationsAdapterLiveData.isInitialized())
            return;

        String uid = accountService.getUid();
        assert uid != null;
        communityService.getCommunity(cid,
                community -> {
                    for (List<String> pack : community.getGraduations().values()) {
                        if (pack.contains(uid)) {
                            _viewerClassAdapterLiveData.setValue(
                                    new LazyAdapter<>(
                                            new EntityAdapter<>(new ArrayList<>(), R.layout.name_avatar_element, (v, c) -> {}),
                                            accountService.getUserArrayPaginator(pack.toArray(new String[0]), PAGINATOR_LIMIT)));
                            break;
                        }
                    }

                    _graduationsAdapterLiveData.setValue(
                            new LazyAdapter<>(
                                    new GraduationAdapter(new ArrayList<>(), R.layout.name_avatar_element),
                                    communityService.getGraduationsEntryPaginator(
                                            community.getGraduations().entrySet()
                                                    .stream().map(p -> new Pair<>(p.getKey(), p.getValue().toArray(new String[0]))).toArray(Pair[]::new)
                                    )
                            )
                    );
                },
                e -> Log.w(TAG, e)
        );
    }

    boolean isShowingViewerClass(){
        return Objects.requireNonNullElse(isShowingViewerClassLiveData.getValue(),false);
    }

    void toggleShowingViewerClass(){
        _isShowingViewerClassLiveData.setValue(!isShowingViewerClass());
    }
}
