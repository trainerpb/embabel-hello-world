package com.example.demo;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Condition;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Agent(name = "ProductReplacementAgent", description = "An agent that decides replacement or refund based on inventory availability")
@Slf4j
public class ProductReplacementAgent {

    Map<String, Integer> inventory = Map.<String, Integer>of(
            "SKU123", 10,
            "SKU456", 5,
            "SKU789", 0,
            "SKU321", 2,
            "SKU654", 1,
            "SKU987", 0,
            "SKU111", 3,
            "SKU222", 4,
            "SKU333", 0
    );

    @Action
    public ReplacementRequest processReplacementRequest(UserInput userInput, OperationContext context) {
        log.info("[AGENT] :: Processing replacement request : {}", userInput.getContent());

        return context.ai().withDefaultLlm()
                .creating(ReplacementRequest.class)
                .fromPrompt("Extract the product SKU and reason for replacement from the following user input:\n" + userInput.getContent());
    }

    @Action(post = {"isInStock", "isOutOfStock"})
    public InventoryStatus checkInventoryStatus(ReplacementRequest request, OperationContext context) {
        log.info("[AGENT] :: Checking inventory for SKU: {}", request.sku());
        int availableQuantity = inventory.getOrDefault(request.sku(), 0);
        context.setCondition("isInStock", availableQuantity > 0);        //  set the actual value at runtime
        context.setCondition("isOutOfStock", availableQuantity == 0);

        log.info("[AGENT] :: Available quantity: {}", availableQuantity);

        return new InventoryStatus(request.sku(), availableQuantity);
    }

    // 1. Define conditions that inspect the InventoryStatus domain object
    @Condition(name = "isInStock")
    public boolean isInStock(InventoryStatus status) {
        return status.availableQuantity() > 0;
    }

    @Condition(name = "isOutOfStock")
    public boolean isOutOfStock(InventoryStatus status) {
        return status.availableQuantity() == 0;
    }

    // 2. Reference the condition names directly in the pre attribute (no '#' prefix needed for framework conditions)
    @Action(pre = {"isInStock"})
    public Decision replace(InventoryStatus status, OperationContext context) {
        log.info("[AGENT] :: Item is in stock. Processing replacement for {}", status.sku());
        return new ReplaceDecision(status.sku(), "Replacement possible");
    }

    @Action(pre = {"isOutOfStock"})
    public Decision refund(InventoryStatus status, OperationContext context) {
        log.info("[AGENT] :: Item is out of stock. Processing refund for {}", status.sku());
        return new RefundDecision(status.sku(), "Refund possible");
    }

    @Action
    @AchievesGoal(description = "Decides whether to replace the product or suggest a refund")
    public FinalDecision makeFinalDecision(Decision decision, OperationContext context) {
        log.info("[AGENT] :: Final decision made: {}", decision.decision());
        return new FinalDecision(decision.decision());
    }
}