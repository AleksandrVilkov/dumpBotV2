package com.bot.processor.admin;

import com.bot.processor.oprations.Operations;
import com.bot.processor.SubAction;
import com.bot.processor.SubActionFactory;
import com.bot.processor.admin.subActions.*;
import com.bot.processor.admin.subActions.AdminError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdminSubActionFactory implements SubActionFactory {
    @Autowired
    StartAdmin start;
    @Autowired
    Approved approved;
    @Autowired
    Rejected rejected;
    @Autowired
    SendRejected sendRejected;
    @Autowired
    CommonEditRequest commonEditRequest;
    @Autowired
    EditDescription editDescription;

    @Autowired
    EnterNewDescription enterNewDescription;
    @Autowired
    AdminError error;

    @Override
    public SubAction get(Operations operation) {
        final String ACTION_NAME = "ADMIN_QUERY_PROCESSING";
        switch (operation) {
            case START -> {
                return start;
            }
            case APPROVED_REQUEST -> {
                return approved;
            }
            case REJECTED_REQUEST -> {
                return rejected;
            }
            case SEND_REJECTED_REQUEST -> {
                return sendRejected;
            }
            case EDIT_REQUEST -> {
                return commonEditRequest;
            }
            case EDIT_DESCRIPTION -> {
                return editDescription;
            }
            case ENTER_NEW_DESCRIPTION -> {
                return enterNewDescription;
            }
            default -> {
               return error;
            }
        }
    }
}
