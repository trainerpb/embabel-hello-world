package com.example.demo;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.ActionInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.AgentProcessStatusCode;
import com.embabel.agent.domain.io.UserInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
@Slf4j
@RequiredArgsConstructor
public class AgentController {
    private final AgentPlatform agentPlatform;

    @PostMapping("/mom")
    public MeetingMemo mom(@RequestBody String queryText){
        log.info("Received request to create meeting memo with query: {}", queryText);
        AgentInvocation<MeetingMemo> meetingMemoAgentInvocation = AgentInvocation.create(agentPlatform, MeetingMemo.class);
        return meetingMemoAgentInvocation.invoke(new UserInput(queryText));
    }

    @PostMapping("/replacement-or-refund")
    public ResponseEntity<?> replacementOrRefund(@RequestBody String userMessage) {
        log.info("Received userMessage for product replacement or refund: {}", userMessage);
        // 1. Execute asynchronously and join to get the Process envelope
        AgentInvocation<FinalDecision> replacementAgentInvocation = AgentInvocation.create(agentPlatform, FinalDecision.class);
        return ResponseEntity.ok(replacementAgentInvocation.invoke(new UserInput(userMessage)));
    }

    @GetMapping("/loan-agent")
    public ResponseEntity<?> loanAgent(@RequestParam("q") String userMessage) {
        log.info("Received userMessage for loan agent: {}", userMessage);
        AgentInvocation<LoanApprovalAgent.LoanDecision> loanAgentInvocation = AgentInvocation.create(agentPlatform, LoanApprovalAgent.LoanDecision.class);
        return ResponseEntity.ok(loanAgentInvocation.invoke(new UserInput(userMessage)));
    }

}
