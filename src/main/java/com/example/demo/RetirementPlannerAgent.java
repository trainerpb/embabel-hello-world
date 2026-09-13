package com.example.demo;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;

import java.util.Arrays;
import java.util.List;

/***
 * @author Soham Sengupta
 * @since 2026-09-02
 * @version 1.0
 * @description This agent provides retirement planning advice.
 * This agent can help users plan for their retirement by providing personalized advice based on their present age and target retirement age.
 * It suggests a list of investments and savings strategies to help users achieve their retirement goals.
 * Statutory warnings:
 * This agent is for informational purposes only and should NOT be considered as financial advice.
 * Users should consult with a certified financial advisor before making any investment decisions.
 * Finally, investments are subject to market risks, and past performance is not indicative of future results. Users should be aware of the risks involved in investing and make informed decisions.
 */
@Agent(name = "RetirementPlannerAgent", description = "This agent provides retirement planning advice.")
public class RetirementPlannerAgent {

    /***
     * This record represents the user input for retirement planning.
     * @param presentAge
     * @param targetRetirementAge
     * @param annualIncome
     */
    record RetirementUserInput(int presentAge, int targetRetirementAge,double annualIncome) {
    }
    /***
     * This record represents the retirement plan advice provided by the agent.
     * @param advice
     */
    record RetirementPlanAdvice(String advice) {
    }

    /***
     * This record represents a list of retirement plan advices provided by the agent.
     * @param advices
     */
    record  RetirementPlanAdvices(List<RetirementPlanAdvice> advices) {
    }
    enum RiskToleranceLevel {
        LOW, MEDIUM, HIGH
    }


    @Action(description = "Identify the present age, target retirement age, and annual income of the user from the user message.")
    public RetirementUserInput identifyRetirementUserInput(UserInput userInput, OperationContext context) {
        String content = userInput.getContent();
        return context.ai().withDefaultLlm()
                .creating(RetirementUserInput.class)
                .fromPrompt("""
                        Identify the present age, target retirement age, and annual income of the user from the following message and return them as a JSON object.
                        User message: %s
                        """.formatted(content));
    }

    @Action(description = "Identify the risk tolerance level of the user based on the present age, target retirement age, and annual income.")
    public RiskToleranceLevel identifyRiskToleranceLevel(RetirementUserInput retirementUserInput, OperationContext context) {
        return context.ai().withDefaultLlm()
                .creating(RiskToleranceLevel.class)
                .fromPrompt("""
                        Identify the risk tolerance level of the user based on the following information and return it as a JSON object.
                        Permitted values for risk tolerance level are: %s
                        Present age: %d
                        Target retirement age: %d
                        Maximum years to retirement: %d
                        Annual income: %.2f
                        """.formatted(Arrays.toString(RiskToleranceLevel.values()), retirementUserInput.presentAge(),
                               retirementUserInput.targetRetirementAge(),
                                (retirementUserInput.targetRetirementAge()-retirementUserInput.presentAge()),
                                retirementUserInput.annualIncome()));

    }

    @Action(description = "Provide retirement plan advice based on the user's risk tolerance level.")
    @AchievesGoal(description = "Provide retirement plan advice based on the user's risk tolerance level.")
    public RetirementPlanAdvices provideRetirementPlanAdvice(RiskToleranceLevel riskToleranceLevel, OperationContext context) {
        String systemPrompt = """
                You are a retirement planning advisor. Based on the user's risk tolerance level, provide a list of retirement plan advices.
                The advice should be practical, actionable, and tailored to the user's risk tolerance level.
                The advice should be in the form of a list of strings.
                The advice should be in JSON format.
                Search Internet and mention a few investment plans in India that are suitable for the user's risk tolerance level.
                """;
        return context.ai().withDefaultLlm()
                .creating(RetirementPlanAdvices.class)
                .fromPrompt("""
                        %s
                        User's risk tolerance level: %s
                        """.formatted(systemPrompt, riskToleranceLevel.name()));
    }

}
