package com.bot.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TempObject implements Cloneable {
    private Action action;
    private String userId;
    private Operation operation;
    private SelectedData selectedData;
    private OptionData option;
    private Navigation navigation;
    private String description;
    private AdministrationData administrationData;
    private List<String> deleteMsgsIdInNxtStep;

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
            if (this.getOption() != null) {
                clone.setOption(this.getOption().clone());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
