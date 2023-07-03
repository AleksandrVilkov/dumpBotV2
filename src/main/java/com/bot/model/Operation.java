package com.bot.model;

public enum Operation {
    //main
    START,   END,
    CITY_SELECTION,
    //car
    CONCERN_SELECTION, BRAND_SELECTION, MODEL_SELECTION, ENGINE_SELECTION, NEED_CAR,
    //photo
    PRE_PHOTO, PHOTO,
    DESCRIPTION,
    //admin
    ENTER_NEW_DESCRIPTION, APPROVED_REQUEST, EDIT_REQUEST, EDIT_DESCRIPTION, REJECTED_REQUEST, SEND_REJECTED_REQUEST,
    //cabinet
    EDIT_CAR, EDIT_USER, MY_ACCOMMODATION
}
