package com.example.demo;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Condition;

/**
 * Minimal Embabel agent showing how @Condition and @Action(pre = ...) work together.
 *
 * Flow the GOAP planner can build from this class:
 *
 *   LoanApplication --checkCredit()--> CreditCheckResult --+--> approveLoan()  [pre: creditScoreSufficient]
 *                                                           +--> rejectLoan()  [pre: creditScoreInsufficient]
 *
 * checkCredit() has no precondition, so it runs as soon as a LoanApplication
 * is on the blackboard. Once its output (CreditCheckResult) exists, the
 * planner re-evaluates every known @Condition and picks whichever gated
 * action's precondition is now satisfied - approveLoan() or rejectLoan().
 * Conditions are re-checked after every action, so the plan adapts if
 * upstream state changes.
 */
@Agent(description = "Evaluates and decides on a personal loan application")
public class LoanApprovalAgent {

    private static final int MIN_CREDIT_SCORE = 650;

    // ---- Domain model (kept as nested records to keep this demo one file) ----

    public record LoanApplication(String applicantName, double requestedAmount, int creditScore) {
    }

    public record CreditCheckResult(int creditScore, boolean flaggedForReview) {
    }

    public record LoanDecision(String applicantName, double amount, boolean approved, String reason) {
    }

    // ---- Conditions ----
    // A @Condition method is just a plain method the planner can call with
    // whatever domain objects are currently on the blackboard - here, the
    // LoanApplication that started the process. "name" is the string that
    // @Action.pre refers to below; if omitted it defaults to the method name.
    // cost = 0.0 tells the planner this check is cheap (pure Java, no I/O or LLM call).

    @Condition(name = "creditScoreSufficient", cost = 0.0)
    public boolean creditScoreSufficient(LoanApplication application) {
        return application.creditScore() >= MIN_CREDIT_SCORE;
    }

    // A mirror-image condition. Embabel's annotation-based "pre" takes plain
    // condition names (each one is required to be true) rather than a negation
    // operator, so the reliable way to express "not sufficient" is a second,
    // explicit @Condition - which also keeps both branches independently testable.
    @Condition(name = "creditScoreInsufficient", cost = 0.0)
    public boolean creditScoreInsufficient(LoanApplication application) {
        return !creditScoreSufficient(application);
    }

    // ---- Actions ----

    /** No "pre" attribute: this runs unconditionally once a LoanApplication exists. */
    @Action
    public CreditCheckResult checkCredit(LoanApplication application) {
        boolean flagged = application.requestedAmount() > 500_000;
        return new CreditCheckResult(application.creditScore(), flagged);
    }

    /**
     * pre = "creditScoreSufficient" means the planner will only ever schedule
     * this action once that named condition evaluates true for the
     * LoanApplication on the blackboard.
     */
    @Action(pre = "creditScoreSufficient")
    @AchievesGoal(description = "Approve a loan for an eligible applicant")
    public LoanDecision approveLoan(LoanApplication application, CreditCheckResult creditCheckResult) {
        String reason = creditCheckResult.flaggedForReview()
                ? "Approved, but flagged for manual review because of the loan size"
                : "Approved - credit score " + creditCheckResult.creditScore() + " meets the minimum";
        return new LoanDecision(application.applicantName(), application.requestedAmount(), true, reason);
    }

    /**
     * The mirror-image action, gated on the mirror-image condition, so the
     * planner always has a route to a goal regardless of the applicant's score.
     */
    @Action(pre = "creditScoreInsufficient")
    @AchievesGoal(description = "Reject a loan for an ineligible applicant")
    public LoanDecision rejectLoan(LoanApplication application, CreditCheckResult creditCheckResult) {
        String reason = "Credit score " + creditCheckResult.creditScore()
                + " is below the required minimum of " + MIN_CREDIT_SCORE;
        return new LoanDecision(application.applicantName(), application.requestedAmount(), false, reason);
    }
}