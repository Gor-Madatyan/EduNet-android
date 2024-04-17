package com.example.edunet.ui.screen.graduations;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.R;
import com.example.edunet.data.service.CommunityService;
import com.example.edunet.ui.util.adapter.ListAdapter;
import com.example.edunet.ui.util.adapter.impl.GraduationAdapter;
import com.example.edunet.ui.util.adapter.impl.LazyAdapter;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GraduationsViewModel extends ViewModel {
    private static final String TAG = GraduationsViewModel.class.getSimpleName();
    private final CommunityService communityService;
    private final MutableLiveData<ListAdapter<?, Object>> _adapterLiveData = new MutableLiveData<>();
    final LiveData<ListAdapter<?, Object>> adapterLiveData = _adapterLiveData;

    @Inject
    public GraduationsViewModel(CommunityService communityService) {
        this.communityService = communityService;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    void setCommunity(String cid) {
        if (adapterLiveData.isInitialized())
            return;

        communityService.getCommunity(cid,
                community ->
                    _adapterLiveData.setValue(
                            new LazyAdapter<>(
                                    new GraduationAdapter(new ArrayList<>(), R.layout.name_avatar_element),
                                    communityService.getGraduationsEntryPaginator(
                                            community.getGraduations().entrySet()
                                                    .stream().map(p->new Pair<>(p.getKey(),p.getValue().toArray(new String[0]))).toArray(Pair[]::new)
                                    )
                            )
                    ),
                e -> Log.w(TAG,e)
        );
    }
}
