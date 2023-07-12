package com.bot.processor.registration;

import com.bot.processor.SubAction;
import com.bot.processor.SubActionFactory;
import com.bot.model.Operations;
import com.bot.processor.registration.subActions.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class RegSubActionFactory implements SubActionFactory {
    @Autowired
    private StartRegistration startRegistration;
    @Autowired
    private RegisterBrandSelection brandSelection;
    @Autowired
    private RegisterCitySelection citySelection;
    @Autowired
    private RegisterConcernSelection concernSelection;
    @Autowired
    private EndRegistration endRegistration;
    @Autowired
    private RegisterEngineSelection engineSelection;
    @Autowired
    private RegisterModelSelection modelSelection;
    @Autowired
    private RegistrationError registrationError;

    @Override
    public SubAction get(Operations operation) {
        switch (operation) {
            case START -> {
                return startRegistration;
            }
            case CITY_SELECTION -> {
                return citySelection;
            }
            case CONCERN_SELECTION -> {
                return concernSelection;
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
            case END -> {
                return endRegistration;
            }
            default -> {
                return registrationError;
            }
        }
    }
}
