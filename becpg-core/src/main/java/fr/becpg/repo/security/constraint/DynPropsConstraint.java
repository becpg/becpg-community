/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.security.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.security.SecurityService;

// TODO: Auto-generated Javadoc
/**
 * Class used to load the dynamic constraints.
 * 
 * @author matthieu
 */
public class DynPropsConstraint extends ListOfValuesConstraint {

	public static String TYPE_NODE = "NODE_TYPE";
	
	public static String ASPECT_NODE = "ASPECT_TYPE";
	
	/** The Constant UNDIFINED_CONSTRAINT_VALUE. */
	public static final String UNDIFINED_CONSTRAINT_VALUE = "-";

	private static final String ERR_NON_STRING = "d_dictionary.constraint.authority_name.non_string";

	/** The logger. */
	private static Log logger = LogFactory.getLog(DynPropsConstraint.class);

	/** The constraint type. */
	private String constraintType = null;
	
	
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

	
	
	
	public void setConstraintType(String constraintType) {
		this.constraintType = constraintType;
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
		checkPropertyNotNull("constraintType", constraintType);
		logger.debug("Init DynPropsConstraint for constraintType :"+constraintType);
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
									if(TYPE_NODE.equals(constraintType)){
										return getAvailableEntityTypeNames();
									} else if(ASPECT_NODE.equals(constraintType)){
										return getAvailableEntityAspectNames();
									}
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
			if(TYPE_NODE.equals(constraintType)){
				DefaultTypeConverter.INSTANCE.convert(QName.class, value);
			}
			
			
		} catch (TypeConversionException e) {
			throw new ConstraintException(ERR_NON_STRING, value);
		}

		
	}
	
	
	private List<String> getAvailableEntityTypeNames() {

		List<String> ret = new ArrayList<String>();
		Collection<QName> types = serviceRegistry.getDictionaryService().getSubTypes(
				BeCPGModel.TYPE_ENTITY_V2, true);
		if (types != null) {
			for (QName type : types) {
				TypeDefinition typeDef = serviceRegistry.getDictionaryService().getType(type);
				if (typeDef != null  && typeDef.getTitle()!=null) {
					ret.add(type.toString() + "|" + typeDef.getTitle());
				}
			}
		}

		return ret;
	}
	
	private List<String> getAvailableEntityAspectNames() {

		List<String> ret = new ArrayList<String>();
		Collection<QName> aspects = serviceRegistry.getDictionaryService().getAllAspects();
		if (aspects != null) {
			for (QName aspect : aspects) {
				AspectDefinition typeDef = serviceRegistry.getDictionaryService().getAspect(aspect);
				if (typeDef != null && typeDef.getTitle()!=null) {
					ret.add(aspect.toString() + "|" + typeDef.getTitle());
				}
			}
		}

		return ret;
	}
	
	
	

}
