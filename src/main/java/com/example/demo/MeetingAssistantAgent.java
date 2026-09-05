package com.example.demo;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Agent(name = "MeetingAssistantAgent", description = "An agent that can summarize meetings and generate action items.")
@Slf4j
public class MeetingAssistantAgent {


    @Action(description = "Summarizes meeting notes and generates action items.")
    public MeetingSummary summarizeMeeting(UserInput userInput, OperationContext context) {
        log.info("Summarizing meeting notes: {}", userInput.getContent());
        MeetingSummary meetingSummary = context.ai().withDefaultLlm().creating(MeetingSummary.class)
                .fromPrompt("Please summarize the following meeting notes:\n" + userInput.getContent());
        return meetingSummary;
    }

    @Action(description = "Extract action items from meeting notes.")
    public ActionItems generateActionItems(UserInput userInput, OperationContext context) {
        log.info("Generating action items from meeting notes: {}", userInput.getContent());
        ActionItems actionItems = context.ai().withDefaultLlm().creating(ActionItems.class)
                .fromPrompt("Please extract action items from the following meeting notes:\n" + userInput.getContent());
        return actionItems;
    }

    @Action
    @AchievesGoal(description = "Create a meeting memo that includes a summary and action items.")
    public MeetingMemo createMeetingMemo(MeetingSummary summary,ActionItems actionItems, OperationContext context) {
        log.info("Creating meeting memo from summary and action items.");
        MeetingMemo meetingMemo = new MeetingMemo(summary, actionItems);
        return meetingMemo;
    }
}
