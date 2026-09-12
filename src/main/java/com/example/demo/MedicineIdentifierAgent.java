package com.example.demo;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Condition;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;

@Agent(name = "MedicineIdentifierAgent", description = "Identify a medicine and search its usage, side effect, dose , available brands", version = "1.0.0")
public class MedicineIdentifierAgent {

    record Medicine(String name){}
    record MedicineInfo(String usage, String sideEffects, String dose, String availableBrands){}

    @Action(description = "Identify the medicine name from the user input")
    public Medicine identifyMedicine(UserInput userInput, OperationContext context) {
        String userInputText = userInput.getContent();
        Medicine medicine = context.ai().withDefaultLlm()
                .creating(Medicine.class)
                .fromPrompt("""
                You are a medicine identifier agent. Identify the medicine name from the user input.
                User input: %s
                If there is no medicine name in the user input, return "Unknown".
                        """.formatted(userInputText));
        return medicine;
    }






    @Action(description = "Get medicine information based on the identified medicine name")

    public MedicineInfo getMedicineInfo(Medicine medicine, OperationContext context) {
        if(! medicine.name().equalsIgnoreCase("Unknown")) {
            MedicineInfo medicineInfo = context.ai().withDefaultLlm()
                    .creating(MedicineInfo.class)
                    .fromPrompt("""
                                        You are a medicine information agent. Provide the usage, side effects, dose, and available
                            brands for the medicine: %s
                            """.formatted(medicine.name()));
            return medicineInfo;
        }else{
            return null;
        }
    }


    @Action
    @AchievesGoal(description = "Provide the usage, side effects, dose, and available brands for the identified medicine")
    public String getMedicineInfoString(MedicineInfo medicineInfo) {
        return null!=medicineInfo? String.format("Usage: %s\nSide Effects: %s\nDose: %s\nAvailable Brands: %s",
                medicineInfo.usage(), medicineInfo.sideEffects(), medicineInfo.dose(), medicineInfo.availableBrands()):"Invalid medicine name. Please provide a valid medicine name.";
    }
}
