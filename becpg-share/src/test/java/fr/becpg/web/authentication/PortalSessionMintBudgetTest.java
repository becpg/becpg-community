package fr.becpg.web.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * <p>PortalSessionMintBudgetTest class.</p>
 *
 * The budget is what a leaked shared secret runs into, so what matters here is that it counts
 * what the operator configured: one request is one mint, whatever the number of checks it goes
 * through, and one user cannot spend the budget of all the others.
 *
 * @author matthieu
 */
public class PortalSessionMintBudgetTest {

	private static final String USER = "supplier";

	private static final int GLOBAL_BUDGET = 4;

	private static final int USER_BUDGET = 3;

	/** A whole request costs one unit of the global budget, not one per check it goes through. */
	@Test
	public void chargesTheGlobalBudgetOncePerRequest() {
		PortalSessionMintBudget budget = new PortalSessionMintBudget();

		for (int mint = 1; mint <= GLOBAL_BUDGET; mint++) {
			assertTrue("mint " + mint + " is within a budget of " + GLOBAL_BUDGET, budget.tryConsumeGlobal(GLOBAL_BUDGET));
			budget.tryConsumeUser("user" + mint, USER_BUDGET);
		}

		assertFalse("the budget of " + GLOBAL_BUDGET + " is exhausted", budget.tryConsumeGlobal(GLOBAL_BUDGET));
	}

	/** One user reaching its own budget does not consume the budget of the others. */
	@Test
	public void refusesTheUserBeyondItsOwnBudget() {
		PortalSessionMintBudget budget = new PortalSessionMintBudget();

		for (int mint = 1; mint <= USER_BUDGET; mint++) {
			assertTrue("mint " + mint + " is within a user budget of " + USER_BUDGET, budget.tryConsumeUser(USER, USER_BUDGET));
		}

		assertFalse(budget.tryConsumeUser(USER, USER_BUDGET));
		assertTrue("another user still has its own budget", budget.tryConsumeUser("other", USER_BUDGET));
	}

}
