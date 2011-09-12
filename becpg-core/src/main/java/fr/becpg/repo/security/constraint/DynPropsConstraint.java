/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.security.constraint;

import java.util.List;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.security.SecurityService;

// TODO: Auto-generated Javadoc
/**
 * Class used to load the dynamic constraints.
 * 
 * @author matthieu
 */
public class DynPropsConstraint extends ListOfValuesConstraint {

	/** The Constant UNDIFINED_CONSTRAINT_VALUE. */
	public static final String UNDIFINED_CONSTRAINT_VALUE = "-";

	private static final String ERR_NON_STRING = "d_dictionary.constraint.authority_name.non_string";

	/** The logger. */
	private static Log logger = LogFactory.getLog(DynPropsConstraint.class);

	@Override
	public String getType() {
		return "fr.becpg.repo.security.constraint.DynPropsConstraint";
	}
	
	@Override
	public String getShortName() {
		return "DynPropsConstraint";
	}
	
	/**
	 * The Security Service
	 */
	private static SecurityService securityService;

	/** The service registry. */
	private static ServiceRegistry serviceRegistry;

	public void setSecurityService(SecurityService securityService) {
		DynPropsConstraint.securityService = securityService;
	}

	/**
	 * Sets the service registry.
	 * 
	 * @param serviceRegistry
	 *            the new service registry
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		DynPropsConstraint.serviceRegistry = serviceRegistry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint#initialize
	 * ()
	 */
	@Override
	public void initialize() {
		logger.debug("Init DynPropsConstraint");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint#
	 * getAllowedValues()
	 */
	@Override
	public List<String> getAllowedValues() {
		List<String> allowedValues = null;
		try {
			allowedValues = serviceRegistry
					.getTransactionService()
					.getRetryingTransactionHelper()
					.doInTransaction(
							new RetryingTransactionCallback<List<String>>() {
								@Override
								public List<String> execute() throws Throwable {

									return securityService.getAvailablePropNames();

								}
							}, false, true);

			if (allowedValues.size() == 0) {
				allowedValues.add(UNDIFINED_CONSTRAINT_VALUE);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("DynPropsConstraint return "
						+ allowedValues.size() + " values");
			}

			super.setAllowedValues(allowedValues);
		} catch (Exception e) {
			logger.error(e, e);
		}
		return allowedValues;
	}

	@Override
	protected void evaluateSingleValue(Object value) {

		// ensure that the value can be converted to a String
		try {
			DefaultTypeConverter.INSTANCE.convert(String.class, value);
		} catch (TypeConversionException e) {
			throw new ConstraintException(ERR_NON_STRING, value);
		}

	}

}
