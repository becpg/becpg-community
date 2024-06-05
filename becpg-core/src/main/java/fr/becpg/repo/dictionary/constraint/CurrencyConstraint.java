/*
 *  Copyright (C) 2010-2021 beCPG. All rights reserved.
 */
package fr.becpg.repo.dictionary.constraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;

import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * Class used to load supported currencies constraints
 *
 * @author rabah
 * @version $Id: $Id
 */
public class CurrencyConstraint extends ListOfValuesConstraint {


	/** Constant <code>UNDIFINED_CONSTRAINT_VALUE="-"</code> */
	public static final String UNDIFINED_CONSTRAINT_VALUE = "-";
	private static final String ERR_NON_STRING = "d_dictionary.constraint.string_length.non_string";
	private static final String ERR_INVALID_VALUE = "d_dictionary.constraint.list_of_values.invalid_value";
	
	private static BeCPGCacheService beCPGCacheService;
	
	private static SystemConfigurationService systemConfigurationService;
	
	private List<String> allowedValuesSuffix = null;
	
	private static String propConstraints() {
		return systemConfigurationService.confValue("beCPG.currency.supported");
	}
	
	/**
	 * <p>Setter for the field <code>beCPGCacheService</code>.</p>
	 *
	 * @param beCPGCacheService a {@link fr.becpg.repo.cache.BeCPGCacheService} object.
	 */
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		CurrencyConstraint.beCPGCacheService = beCPGCacheService;
	}
	
	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		CurrencyConstraint.systemConfigurationService = systemConfigurationService;
	}
	
	/**
	 * <p>Setter for the field <code>allowedValuesSuffix</code>.</p>
	 *
	 * @param allowedValuesSuffix a {@link java.util.List} object.
	 */
	public void setAllowedValuesSuffix(List<String> allowedValuesSuffix) {
		this.allowedValuesSuffix = allowedValuesSuffix;
	}

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		if(allowedValuesSuffix != null) {
			checkPropertyNotNull("allowedValuesSuffix", allowedValuesSuffix);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public List<String> getAllowedValues() {
		return getAllowedValuesFromCache();
	}
	
	/** {@inheritDoc} */
	@Override
	public String getDisplayLabel(String constraintAllowableValue, MessageLookup messageLookup) {
		return constraintAllowableValue;
	}
	
	/** {@inheritDoc} */
	@Override
	protected void evaluateSingleValue(Object value) {
		// convert the value to a String
		String valueStr;
		try {
			valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
		} catch (TypeConversionException e) {
			throw new ConstraintException(ERR_NON_STRING, value);
		}

		if (!getAllowedValues().contains(valueStr)) {
			throw new ConstraintException(ERR_INVALID_VALUE, value);
		}
	}

	/**
	 * <p>getAllowedValuesFromCache.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public  List<String> getAllowedValuesFromCache() {
		return beCPGCacheService.getFromCache(CurrencyConstraint.class.getName(), getShortName(), () -> {
			if(propConstraints() !=  null) {
				final List<String> allowedValueList =  new ArrayList<>();
				List<String> supportedCurrencies = Arrays.asList(propConstraints().split(","));
				
				if(allowedValuesSuffix != null) {
					supportedCurrencies.forEach(constraint -> {
						allowedValuesSuffix.forEach(suffix -> {
							allowedValueList.add(constraint + suffix);
						});
					});
				} 
				
				if (allowedValueList.isEmpty()) {
					return supportedCurrencies;
				} else {
					return allowedValueList;
				}
			}

			return Collections.singletonList(UNDIFINED_CONSTRAINT_VALUE);
		});
	}
	



}
