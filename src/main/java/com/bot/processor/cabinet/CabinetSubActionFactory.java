package com.bot.processor.cabinet;

import com.bot.model.Operations;
import com.bot.processor.SubAction;
import com.bot.processor.SubActionFactory;
import com.bot.processor.cabinet.subActions.CabinetError;
import com.bot.processor.cabinet.subActions.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CabinetSubActionFactory implements SubActionFactory {

    @Autowired
    private CabinetBrandSelection brandSelection;
    @Autowired
    private CabinetEditCar editCar;
    @Autowired
    private End end;
    @Autowired
    private CabinetEngineSelection engineSelection;
    @Autowired
    private CabinetError error;
    @Autowired
    private CabinetModelSelection modelSelection;
    @Autowired
    private MyAccommodation myAccommodation;
    @Autowired
    private StartCabinet start;


    @Override
    public SubAction get(Operations operation) {
        switch (operation) {
            case START -> {
                return start;
            }
            case EDIT_CAR -> {
                return editCar;
            }
            case BRAND_SELECTION -> {
                return brandSelection;
            }
            case MODEL_SELECTION -> {
                return modelSelection;
            }
            case ENGINE_SELECTION -> {
                return engineSelection;
            }

            case MY_ACCOMMODATION -> {
                return myAccommodation;
            }
            case END -> {
                return end;
            }
            default -> {
                return error;
            }
        }
    }
}
