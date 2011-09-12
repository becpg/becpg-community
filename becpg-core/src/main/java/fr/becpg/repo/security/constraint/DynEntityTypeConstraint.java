/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.security.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;

// TODO: Auto-generated Javadoc
/**
 * Class used to load the dynamic constraints.
 * 
 * @author matthieu
 */
public class DynEntityTypeConstraint extends ListOfValuesConstraint {

	/** The Constant UNDIFINED_CONSTRAINT_VALUE. */
	public static final String UNDIFINED_CONSTRAINT_VALUE = "-";

	private static final String ERR_NON_STRING = "d_dictionary.constraint.authority_name.non_string";

	@Override
	public String getType() {
		return "fr.becpg.repo.security.constraint.DynEntityTypeConstraint";
	}
	
	@Override
	public String getShortName() {
		return "DynEntityTypeConstraint";
	}
	
	
	
	
	/** The logger. */
	private static Log logger = LogFactory
			.getLog(DynEntityTypeConstraint.class);


	/** The service registry. */
	private static ServiceRegistry serviceRegistry;

	/**
	 * Sets the service registry.
	 * 
	 * @param serviceRegistry
	 *            the new service registry
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		DynEntityTypeConstraint.serviceRegistry = serviceRegistry;
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
		logger.debug("Init DynEntityTypeConstraint");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint#
	 * getAllowedValues()
	 */
	@Override
	public List<String> getAllowedValues() {
		List<String> allowedValues = getAvailableEntityTypeNames();		
							

			if (allowedValues.size() == 0) {
				allowedValues.add(UNDIFINED_CONSTRAINT_VALUE);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("DynEntityTypeConstraint return "
						+ allowedValues.size() + " values");
			}

		super.setAllowedValues(allowedValues);
		return allowedValues;
	}

	private List<String> getAvailableEntityTypeNames() {

		List<String> ret = new ArrayList<String>();
		Collection<QName> types = serviceRegistry.getDictionaryService().getSubTypes(
				BeCPGModel.TYPE_ENTITY, true);
		if (types != null) {
			for (QName type : types) {
				TypeDefinition typeDef = serviceRegistry.getDictionaryService().getType(type);
				if (typeDef != null) {
					ret.add(type.toString() + "|" + typeDef.getTitle());
				}
			}
		}

		return ret;
	}

	@Override
	protected void evaluateSingleValue(Object value) {

		// ensure that the value can be converted to a qname

		try {
			DefaultTypeConverter.INSTANCE.convert(QName.class, value);
		} catch (TypeConversionException e) {
			throw new ConstraintException(ERR_NON_STRING, value);
		}
	}

}
