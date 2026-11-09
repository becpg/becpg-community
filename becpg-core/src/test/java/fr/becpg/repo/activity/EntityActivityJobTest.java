package fr.becpg.repo.activity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
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
 * Unit tests of the tenant context of the activity cleanup job.
 */
public class EntityActivityJobTest {

	private final List<String> cleanedDomains = new ArrayList<>();

	@BeforeClass
	public static void initAuthenticationUtil() throws Exception {
		AuthenticationUtil authenticationUtil = new AuthenticationUtil();
		authenticationUtil.setDefaultAdminUserName("admin");
		authenticationUtil.setDefaultGuestUserName("guest");
		authenticationUtil.afterPropertiesSet();
	}

	@After
	public void clearAuthentication() {
		AuthenticationUtil.clearCurrentSecurityContext();
	}

	@Test
	public void cleansDefaultDomainWithoutMultiTenancy() {
		EntityActivityJob.cleanActivities(recordingService(), null);

		assertEquals(List.of(TenantService.DEFAULT_DOMAIN), cleanedDomains);
	}

	@Test
	public void cleansEveryEnabledTenantInItsOwnDomain() {
		EntityActivityJob.cleanActivities(recordingService(), tenantAdminService(tenant("demo.becpg.fr", true), tenant("cosm.becpg.fr", true)));

		assertEquals(List.of(TenantService.DEFAULT_DOMAIN, "demo.becpg.fr", "cosm.becpg.fr"), cleanedDomains);
	}

	@Test
	public void skipsDisabledTenants() {
		EntityActivityJob.cleanActivities(recordingService(), tenantAdminService(tenant("demo.becpg.fr", false), tenant("cosm.becpg.fr", true)));

		assertEquals(List.of(TenantService.DEFAULT_DOMAIN, "cosm.becpg.fr"), cleanedDomains);
	}

	@Test
	public void setsTenantDomainWhenThreadIsAuthenticated() {
		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

		EntityActivityJob.cleanActivities(recordingService(), tenantAdminService(tenant("demo.becpg.fr", true)));

		assertEquals(List.of(TenantService.DEFAULT_DOMAIN, "demo.becpg.fr"), cleanedDomains);
	}

	private EntityActivityService recordingService() {
		EntityActivityService service = mock(EntityActivityService.class);
		when(service.cleanActivities()).thenAnswer(invocation -> {
			cleanedDomains.add(TenantUtil.getCurrentDomain());
			return null;
		});
		return service;
	}

	@SuppressWarnings("deprecation")
	private static TenantAdminService tenantAdminService(Tenant... tenants) {
		TenantAdminService tenantAdminService = mock(TenantAdminService.class);
		when(tenantAdminService.isEnabled()).thenReturn(true);
		when(tenantAdminService.getAllTenants()).thenReturn(List.of(tenants));
		when(tenantAdminService.getDomainUser(anyString(), anyString()))
				.thenAnswer(invocation -> invocation.getArgument(0) + TenantService.SEPARATOR + invocation.getArgument(1));
		return tenantAdminService;
	}

	private static Tenant tenant(String domain, boolean enabled) {
		return new Tenant(domain, enabled, null, null);
	}
}
