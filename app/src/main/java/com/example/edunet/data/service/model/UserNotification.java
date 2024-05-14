package com.example.edunet.data.service.model;

import java.util.Date;

public record UserNotification(Role membersType,
                               UserOperation operationType,
                               boolean arePending,
                               Date date,
                               String[] users) {
}
