/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.helper;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;

import fr.becpg.common.BeCPGException;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;

/**
 * Saves best-effort user preferences, such as UI history, without putting the calling request at risk.
 * <p>
 * Every preference write rewrites the whole <code>cm:preferenceValues</code> content of the person node.
 * When another transaction holds a lock on that node or on <code>alf_content_url</code>, the write fails
 * with a {@link ConcurrencyFailureException} that the content service wraps into a
 * <code>ContentIOException</code>. Run inside the caller's transaction, that failure either aborts the
 * whole transaction or makes the web script container replay the request, each attempt waiting for the
 * database lock timeout again. The write therefore runs in a transaction of its own and is skipped when
 * the cause chain reveals a lock conflict.
 *
 * @author matthieu
 */
@Service("userPreferenceWriter")
public class UserPreferenceWriter {

	private static final Log logger = LogFactory.getLog(UserPreferenceWriter.class);

	private final PreferenceService preferenceService;

	private final TransactionService transactionService;

	/**
	 * <p>Constructor for UserPreferenceWriter.</p>
	 *
	 * @param preferenceService the service storing the user preferences
	 * @param transactionService the service providing the dedicated write transaction
	 */
	@Autowired
	public UserPreferenceWriter(@Qualifier("PreferenceService") PreferenceService preferenceService,
			@Qualifier("TransactionService") TransactionService transactionService) {
		this.preferenceService = preferenceService;
		this.transactionService = transactionService;
	}

	/**
	 * Saves the preferences in a dedicated transaction, and skips the write when it loses a lock conflict.
	 *
	 * @param userName the user owning the preferences
	 * @param preferences the preferences to add or replace
	 * @throws BeCPGException if the write fails for any reason other than a lock conflict
	 */
	public void savePreferencesUnlessLocked(String userName, Map<String, Serializable> preferences) {
		UserTransaction transaction = transactionService.getNonPropagatingUserTransaction(false);
		try {
			transaction.begin();
			preferenceService.setPreferences(userName, preferences);
			transaction.commit();
		} catch (Exception e) {
			rollbackQuietly(transaction);
			if (!isLockConflict(e)) {
				throw new BeCPGException("Unable to save the preferences of user " + userName, e);
			}
			logger.warn("Preference write skipped due to a concurrent lock on user '" + userName + "': " + e.getMessage());
		}
	}

	private boolean isLockConflict(Throwable e) {
		return ExceptionStackUtil.getCause(e, ConcurrencyFailureException.class) != null;
	}

	private void rollbackQuietly(UserTransaction transaction) {
		try {
			int status = transaction.getStatus();
			if ((status != Status.STATUS_NO_TRANSACTION) && (status != Status.STATUS_ROLLEDBACK) && (status != Status.STATUS_COMMITTED)) {
				transaction.rollback();
			}
		} catch (Exception e) {
			logger.debug("Rollback of the preference transaction failed", e);
		}
	}

}
