package fr.becpg.repo.batch;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests of the cancellation and of the authentication of the batches run by {@link BatchQueueServiceImpl}.
 */
public class BatchQueueServiceImplTest {

	private static final String DEMO = "demo.becpg.fr";

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
	public void refusesToCancelUnknownBatch() {
		BatchQueueServiceImpl batchQueueService = new BatchQueueServiceImpl();

		Assert.assertFalse(batchQueueService.cancelBatch("import_HKFoods Import - "));
	}

	@Test
	public void defaultBatchIgnoresTenantLeftOnThread() {
		AuthenticationUtil.setMtEnabled(true);
		TenantContextHolder.setTenantDomain(DEMO);

		BatchQueueServiceImpl.setFullyAuthenticatedBatchUser(AuthenticationUtil.getSystemUserName(), TenantService.DEFAULT_DOMAIN);

		Assert.assertEquals(TenantService.DEFAULT_DOMAIN, TenantUtil.getCurrentDomain());
		Assert.assertEquals(AuthenticationUtil.getSystemUserName(), AuthenticationUtil.getFullyAuthenticatedUser());
	}

	@Test
	public void tenantBatchRunsInItsTenant() {
		AuthenticationUtil.setMtEnabled(true);

		BatchQueueServiceImpl.setFullyAuthenticatedBatchUser(AuthenticationUtil.getSystemUserName() + TenantService.SEPARATOR + DEMO, DEMO);

		Assert.assertEquals(DEMO, TenantUtil.getCurrentDomain());
	}

	@Test
	public void threadGetsBackItsAuthenticationAndTenantAfterTheBatch() {
		AuthenticationUtil.setMtEnabled(true);
		AuthenticationUtil.setFullyAuthenticatedUser("admin");

		BatchQueueServiceImpl.runRestoringAuthentication(() -> BatchQueueServiceImpl
				.setFullyAuthenticatedBatchUser(AuthenticationUtil.getSystemUserName() + TenantService.SEPARATOR + DEMO, DEMO));

		Assert.assertEquals("admin", AuthenticationUtil.getFullyAuthenticatedUser());
		Assert.assertEquals(TenantService.DEFAULT_DOMAIN, TenantUtil.getCurrentDomain());
	}

	@Test
	public void threadGetsBackItsAuthenticationWhenTheBatchFails() {
		AuthenticationUtil.setFullyAuthenticatedUser("admin");

		Assert.assertThrows(IllegalStateException.class, () -> BatchQueueServiceImpl.runRestoringAuthentication(() -> {
			BatchQueueServiceImpl.setFullyAuthenticatedBatchUser(AuthenticationUtil.getSystemUserName(), null);
			throw new IllegalStateException("batch failed");
		}));

		Assert.assertEquals("admin", AuthenticationUtil.getFullyAuthenticatedUser());
	}

	@Test
	public void keepsThreadTenantWithoutMultiTenancy() {
		BatchQueueServiceImpl.setFullyAuthenticatedBatchUser(AuthenticationUtil.getSystemUserName(), null);

		Assert.assertEquals(AuthenticationUtil.getSystemUserName(), AuthenticationUtil.getFullyAuthenticatedUser());
		Assert.assertEquals(TenantService.DEFAULT_DOMAIN, TenantUtil.getCurrentDomain());
	}
}
