/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.designer.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO: Auto-generated Javadoc
/**
 * Class used to load the dynamic constraints.
 * 
 * @author matthieu
 */
public class DynModelConstraint extends ListOfValuesConstraint {

	public static String TYPE_NODE = "NODE_TYPE";

	public static String ASPECT_NODE = "ASPECT_TYPE";

	public static String DATA_NODE = "DATA_TYPE";

	/** The Constant UNDIFINED_CONSTRAINT_VALUE. */
	public static final String UNDIFINED_CONSTRAINT_VALUE = "-";


	/** The logger. */
	private static Log logger = LogFactory.getLog(DynModelConstraint.class);

	/** The constraint type. */
	private String constraintType = null;

	/** The service registry. */
	private static ServiceRegistry serviceRegistry;

	/**
	 * Sets the service registry.
	 * 
	 * @param serviceRegistry
	 *            the new service registry
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		DynModelConstraint.serviceRegistry = serviceRegistry;
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
		logger.debug("Init DynModelConstraint for constraintType :" + constraintType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint#
	 * getAllowedValues()
	 */
	@Override
	public List<String> getAllowedValues() {
		List<String> allowedValues = new ArrayList<String>();
		try {
			if (constraintType != null) {
				if (TYPE_NODE.equals(constraintType)) {
					allowedValues = getAvailableEntityTypeNames();
				} else if (ASPECT_NODE.equals(constraintType)) {
					allowedValues = getAvailableEntityAspectNames();
				} else  {
					allowedValues = getAvailableDataTypeNames();
				}
			} 

			if (allowedValues.size() == 0) {
				allowedValues.add(UNDIFINED_CONSTRAINT_VALUE);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("DynModelConstraint return " + allowedValues.size() + " values");
			}

		} catch (Exception e) {
			logger.error(e, e);
		}

		super.setAllowedValues(allowedValues);
		return allowedValues;
	}


	
	
	protected List<String> getAvailableDataTypeNames() {
		List<String> ret = new ArrayList<String>();
		Collection<QName> types = serviceRegistry.getDictionaryService().getAllDataTypes();
		if (types != null) {
			for (QName type : types) {
				DataTypeDefinition typeDef = serviceRegistry.getDictionaryService().getDataType(type);
				if (typeDef != null && typeDef.getTitle() != null) {
					ret.add(type.toString() + "|" + typeDef.getTitle());
				} else if (typeDef != null) {
					ret.add(type.toString());
				}
			}
		}

		return ret;
	}

	

	private List<String> getAvailableEntityTypeNames() {

		List<String> ret = new ArrayList<String>();
		Collection<QName> types = serviceRegistry.getDictionaryService().getAllTypes();
		if (types != null) {
			for (QName type : types) {
				TypeDefinition typeDef = serviceRegistry.getDictionaryService().getType(type);
				if (typeDef != null && typeDef.getTitle() != null) {
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
				if (typeDef != null && typeDef.getTitle() != null) {
					ret.add(aspect.toString() + "|" + typeDef.getTitle());
				}
			}
		}

		return ret;
	}

}
