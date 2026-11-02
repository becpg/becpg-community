package org.alfresco.repo.workflow.activiti.script;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.junit.After;
import org.junit.Test;

/**
 * Unit tests of the beCPG resolution of the 'runAs' user of the workflow scripts.
 */
public class ActivitiScriptBaseTest {

	private static final String ADMIN = "admin";

	private static final String USER_ONE = "user1";

	@After
	public void resetMultiTenancy() {
		AuthenticationUtil.setMtEnabled(false);
	}

	@Test
	public void adminRunsAsAdminWhenItExists() {
		assertEquals(ADMIN, scriptBaseWithPersons(ADMIN).resolveRunAsUser(ADMIN));
	}

	@Test
	public void adminRunsAsSystemWhenItDoesNotExist() {
		assertEquals(AuthenticationUtil.getSystemUserName(), scriptBaseWithPersons(USER_ONE).resolveRunAsUser(ADMIN));
	}

	@Test
	public void adminRunsAsSystemOnMultiTenantRepository() {
		AuthenticationUtil.setMtEnabled(true);

		assertEquals(AuthenticationUtil.getSystemUserName(), scriptBaseWithPersons(ADMIN).resolveRunAsUser(ADMIN));
	}

	@Test
	public void existingUserRunsAsItself() {
		assertEquals(USER_ONE, scriptBaseWithPersons(USER_ONE).resolveRunAsUser(USER_ONE));
	}

	@Test(expected = WorkflowException.class)
	public void missingUserOtherThanAdminIsRejected() {
		scriptBaseWithPersons(ADMIN).resolveRunAsUser(USER_ONE);
	}

	private ActivitiScriptBase scriptBaseWithPersons(String... userNames) {
		Set<String> persons = Set.of(userNames);
		return new ActivitiScriptBase() {
			@Override
			protected boolean runAsUserExists(String runAsUser) {
				return persons.contains(runAsUser);
			}
		};
	}
}
