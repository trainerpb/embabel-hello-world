package com.example.demo;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.example.demo.SupportType.*;

@Slf4j
@RequiredArgsConstructor
@Agent(name = "CustomerSupportAgent", description = "Understands user queries and routes them to the appropriate support type.")
public class CustomerSupportAgent {

    private final BillingSupport billingSupport;
    private final TechnicalSupport technicalSupport;
    private final GeneralSupport generalSupport;

    // METHOD 1: Takes UserInput and packs the result into our new SupportRoutingTicket carrier
    @Action(description = "Analyze the raw user input and determine the required support tier type")
    public SupportRoutingTicket determineSupportType(UserInput userInput, Ai ai) {
        String query = userInput.getContent();
        log.info("CustomerSupportAgent Step 1: Determining support type for query: {}", query);

        String prompt = "Determine the support type for the following user query. " +
                "Output ONLY one of these exact words: TECHNICAL_SUPPORT, BILLING_SUPPORT, GENERAL_INQUIRY. " +
                "Query: " + query;

        String supportTypeString = ai.withDefaultLlm()
                .creating(String.class)
                .fromPrompt(prompt)
                .trim()
                .toUpperCase();

        SupportType supportType = SupportType.valueOf(supportTypeString);

        // We return the carrier record here!
        return new SupportRoutingTicket(supportType, query);
    }

    // METHOD 2: Receives the carrier record directly from Method 1
    @Action(description = "Route a prepared support routing ticket to its final destination service")
    @AchievesGoal(description = "User query has been routed to the appropriate support type and handled")
    public String routeToSupport(SupportRoutingTicket ticket) {
        log.info("CustomerSupportAgent Step 2: Routing to service for type: {}", ticket.type());

        return switch (ticket.type()) {
            case TECHNICAL_SUPPORT -> technicalSupport.support(ticket.queryText());
            case BILLING_SUPPORT -> billingSupport.support(ticket.queryText());
            case GENERAL_INQUIRY -> generalSupport.support(ticket.queryText());
        };
    }
}

