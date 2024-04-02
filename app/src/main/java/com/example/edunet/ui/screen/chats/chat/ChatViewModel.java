package com.example.edunet.ui.screen.chats.chat;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.util.Consumer;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;

import com.example.edunet.data.service.AccountService;
import com.example.edunet.data.service.MessagingService;
import com.example.edunet.data.service.exception.UserFriendlyException;
import com.example.edunet.data.service.model.Message;
import com.example.edunet.data.service.util.common.Paginator;
import com.example.edunet.ui.adapter.LazyMessageAdapter;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatViewModel extends ViewModel {
    public final static int PAGINATOR_LIMIT = 20;
    private static final String TAG = ChatViewModel.class.getSimpleName();
    private String communityId;
    private final MessagingService messagingService;
    private final AccountService accountService;

    @Inject
    ChatViewModel(MessagingService messagingService, AccountService accountService) {
        this.messagingService = messagingService;
        this.accountService = accountService;
    }

    void setCommunity(@NonNull String communityId) {
        this.communityId = communityId;
    }

    void sendMessage(@NonNull String message, @NonNull Consumer<Error> onResult) {
        assert communityId != null;
        messagingService.sendMessage(message, communityId,
                e ->
                        onResult.accept(e == null ? null : new Error(e.getId()))
        );
    }

    void listenNewMessages(LifecycleOwner lifecycleOwner, Consumer<List<Message>> onSuccess, Consumer<UserFriendlyException> onFailure) {
        messagingService.listenNewMessages(lifecycleOwner, communityId, new Date(),
                onSuccess::accept,
                e -> {
                    Log.w(TAG, e);
                    onFailure.accept(e);
                }
        );
    }

    public  boolean isUserOwner(Message message){
        return messagingService.isCurrentUserOwner(message);
    }

    public LazyMessageAdapter createAdapter(){
        return new LazyMessageAdapter(
                getPaginator(),
                accountService,
                messagingService
        );
    }

    private Paginator<Message> getPaginator() {
        return messagingService.getDescendingMessagePaginator(communityId, PAGINATOR_LIMIT);
    }

}

record Error(@StringRes int message) {
}