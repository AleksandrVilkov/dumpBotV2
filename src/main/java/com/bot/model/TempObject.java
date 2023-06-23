package com.bot.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TempObject implements Cloneable {
    private Action action;
    private String userId;
    private int step;
    private SelectedData selectedData;
    private OptionData option;
    private Navigation navigation;

    public TempObject() {
        this.selectedData = new SelectedData();
        this.option = new OptionData();
        this.navigation = new Navigation();
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TempObject clone() {
        try {
            TempObject clone = (TempObject) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
