package fr.becpg.repo.helper;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.ConcurrencyFailureException;

import fr.becpg.common.BeCPGException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

/**
 * Unit tests of {@link UserPreferenceWriter}: the write runs in its own transaction and a lock conflict,
 * however deeply wrapped, only skips the write.
 */
public class UserPreferenceWriterTest {

	private static final String USER_NAME = "jdoe";

	private static final Map<String, Serializable> PREFERENCES = Map.of("fr.becpg.DockBarHistory", "workspace://SpacesStore/1");

	private final PreferenceService preferenceService = mock(PreferenceService.class);

	private final TransactionService transactionService = mock(TransactionService.class);

	private final UserTransaction transaction = mock(UserTransaction.class);

	private final UserPreferenceWriter writer = new UserPreferenceWriter(preferenceService, transactionService);

	@Before
	public void setUp() throws Exception {
		when(transactionService.getNonPropagatingUserTransaction(anyBoolean())).thenReturn(transaction);
		when(transaction.getStatus()).thenReturn(Status.STATUS_ACTIVE);
	}

	@Test
	public void commitsTheWriteInADedicatedTransaction() throws Exception {
		writer.savePreferencesUnlessLocked(USER_NAME, PREFERENCES);

		verify(transactionService).getNonPropagatingUserTransaction(false);
		verify(preferenceService).setPreferences(USER_NAME, PREFERENCES);
		verify(transaction).commit();
		verify(transaction, never()).rollback();
	}

	@Test
	public void skipsTheWriteWhenTheContentServiceWrapsALockTimeout() throws Exception {
		doThrow(new ContentIOException("Failed to set content property on stream closure", new CannotAcquireLockException("Lock wait timeout exceeded")))
				.when(preferenceService).setPreferences(USER_NAME, PREFERENCES);

		writer.savePreferencesUnlessLocked(USER_NAME, PREFERENCES);

		verify(transaction).rollback();
		verify(transaction, never()).commit();
	}

	@Test
	public void skipsTheWriteWhenTheCommitLosesAConcurrentUpdate() throws Exception {
		RollbackException commitFailure = new RollbackException("Transaction rolled back");
		commitFailure.initCause(new ConcurrencyFailureException("Failed to update node"));
		doThrow(commitFailure).when(transaction).commit();
		when(transaction.getStatus()).thenReturn(Status.STATUS_ROLLEDBACK);

		writer.savePreferencesUnlessLocked(USER_NAME, PREFERENCES);

		verify(transaction, never()).rollback();
	}

	@Test
	public void rethrowsAnyOtherFailureAfterRollingBack() throws Exception {
		doThrow(new ContentIOException("Disk full")).when(preferenceService).setPreferences(USER_NAME, PREFERENCES);

		Assert.assertThrows(BeCPGException.class, () -> writer.savePreferencesUnlessLocked(USER_NAME, PREFERENCES));

		verify(transaction).rollback();
	}

}
