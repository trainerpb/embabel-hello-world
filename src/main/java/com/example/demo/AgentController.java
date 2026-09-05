package com.example.demo;

import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
}
