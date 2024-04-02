package com.example.edunet.ui.screen.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.CommunityService;
import com.example.edunet.data.service.model.Community;
import com.example.edunet.data.service.util.common.Paginator;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SearchViewModel extends ViewModel {
    private final static int PAGINATOR_LIMIT = 20;
    private final CommunityService communityService;
    private final MutableLiveData<Paginator<Community>> _paginatorLiveData = new MutableLiveData<>();
    final LiveData<Paginator<Community>> paginatorLiveData = _paginatorLiveData;


    @Inject
    SearchViewModel(CommunityService communityService) {
        this.communityService = communityService;
    }

    void setQuery(String namePrefix) {
        _paginatorLiveData.setValue(communityService.getCommunityPaginator(namePrefix, PAGINATOR_LIMIT));
    }

}
