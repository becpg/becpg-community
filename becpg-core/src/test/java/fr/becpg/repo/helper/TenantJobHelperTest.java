package fr.becpg.repo.helper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests of the tenant context given to the work of a scheduled job.
 */
public class TenantJobHelperTest {

	private static final String DEMO = "demo.becpg.fr";

	private static final String COSM = "cosm.becpg.fr";

	private final List<String> runs = new ArrayList<>();

	@BeforeClass
	public static void initAuthenticationUtil() throws Exception {
		AuthenticationUtil authenticationUtil = new AuthenticationUtil();
		authenticationUtil.setDefaultAdminUserName("admin");
		authenticationUtil.setDefaultGuestUserName("guest");
		authenticationUtil.afterPropertiesSet();
	}

	@After
	public void resetAuthentication() {
		AuthenticationUtil.clearCurrentSecurityContext();
		AuthenticationUtil.setMtEnabled(false);
	}

	@Test
	public void runsInDefaultDomainWithoutMultiTenancy() {
		TenantJobHelper.runForEachTenant(null, this::recordRun);

		assertEquals(List.of(run(TenantService.DEFAULT_DOMAIN)), runs);
	}

	@Test
	public void runsInEachEnabledTenant() {
		AuthenticationUtil.setMtEnabled(true);

		TenantJobHelper.runForEachTenant(tenantAdminService(tenant(DEMO, true), tenant(COSM, false), tenant(TenantService.DEFAULT_DOMAIN, true)),
				this::recordRun);

		assertEquals(List.of(run(TenantService.DEFAULT_DOMAIN), run(DEMO)), runs);
	}

	@Test
	public void runsInEachTenantWhenThreadHoldsAnotherAuthentication() {
		AuthenticationUtil.setMtEnabled(true);
		AuthenticationUtil.setFullyAuthenticatedUser("admin" + TenantService.SEPARATOR + COSM);

		TenantJobHelper.runForEachTenant(tenantAdminService(tenant(DEMO, true)), this::recordRun);

		assertEquals(List.of(run(TenantService.DEFAULT_DOMAIN), run(DEMO)), runs);
	}

	private void recordRun() {
		runs.add(TenantUtil.getCurrentDomain() + "/" + AuthenticationUtil.getRunAsUser());
	}

	private static String run(String tenantDomain) {
		return tenantDomain + "/" + AuthenticationUtil.getSystemUserName();
	}

	@SuppressWarnings("deprecation")
	private static TenantAdminService tenantAdminService(Tenant... tenants) {
		TenantAdminService tenantAdminService = mock(TenantAdminService.class);
		when(tenantAdminService.isEnabled()).thenReturn(true);
		when(tenantAdminService.getAllTenants()).thenReturn(List.of(tenants));
		return tenantAdminService;
	}

	private static Tenant tenant(String domain, boolean enabled) {
		return new Tenant(domain, enabled, null, null);
	}
}
