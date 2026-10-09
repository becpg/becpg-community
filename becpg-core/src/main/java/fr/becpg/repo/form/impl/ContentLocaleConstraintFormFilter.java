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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.form.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.dictionary.constraint.RegisteredConstraint;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.helper.TranslateHelper;

/**
 * Translates the labels of the fixed lists of values with the content language
 * instead of the interface language, as the dynamic lists already do.
 *
 * @param <ItemType> the kind of item the form is generated for
 * @author matthieu
 * @version $Id: $Id
 */
public class ContentLocaleConstraintFormFilter<ItemType> extends AbstractFilter<ItemType, Object> {

	private static final String LABEL_SEPARATOR = "|";

	private static final String PREFIX_SEPARATOR = ":";

	private DictionaryService dictionaryService;

	private NamespacePrefixResolver namespacePrefixResolver;

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>Setter for the field <code>namespacePrefixResolver</code>.</p>
	 *
	 * @param namespacePrefixResolver a {@link org.alfresco.service.namespace.NamespacePrefixResolver} object
	 */
	public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}

	/** {@inheritDoc} */
	@Override
	public void afterGenerate(ItemType item, List<String> fields, List<String> forcedFields, Form form, Map<String, Object> context) {

		if ((form == null) || (form.getFieldDefinitions() == null)) {
			return;
		}

		for (FieldDefinition fieldDefinition : form.getFieldDefinitions()) {
			if (fieldDefinition instanceof PropertyFieldDefinition propertyFieldDefinition) {
				translateListConstraints(propertyFieldDefinition);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void beforeGenerate(ItemType item, List<String> fields, List<String> forcedFields, Form form, Map<String, Object> context) {
		// Nothing to do before the form is generated
	}

	/** {@inheritDoc} */
	@Override
	public void beforePersist(ItemType item, FormData data) {
		// Labels are only translated for display
	}

	/** {@inheritDoc} */
	@Override
	public void afterPersist(ItemType item, FormData data, Object persistedObject) {
		// Labels are only translated for display
	}

	private void translateListConstraints(PropertyFieldDefinition fieldDefinition) {

		List<FieldConstraint> fieldConstraints = fieldDefinition.getConstraints();

		if ((fieldConstraints == null) || fieldConstraints.isEmpty()) {
			return;
		}

		Optional<PropertyDefinition> propertyDef = findPropertyDefinition(fieldDefinition.getName());

		if (propertyDef.isEmpty() || (propertyDef.get().getConstraints().size() != fieldConstraints.size())) {
			return;
		}

		List<ConstraintDefinition> constraintDefs = propertyDef.get().getConstraints();

		for (int i = 0; i < fieldConstraints.size(); i++) {
			FieldConstraint fieldConstraint = fieldConstraints.get(i);

			findListConstraintName(constraintDefs.get(i)).ifPresent(constraintName -> translateAllowedValues(fieldConstraint, constraintName));
		}
	}

	private Optional<PropertyDefinition> findPropertyDefinition(String fieldName) {

		if ((fieldName == null) || !fieldName.contains(PREFIX_SEPARATOR)) {
			return Optional.empty();
		}

		return Optional.ofNullable(dictionaryService.getProperty(QName.createQName(fieldName, namespacePrefixResolver)));
	}

	private Optional<String> findListConstraintName(ConstraintDefinition constraintDef) {

		Constraint constraint = constraintDef.getConstraint();

		if (constraint instanceof RegisteredConstraint registeredConstraint) {
			constraint = registeredConstraint.getRegisteredConstraint();
		}

		if (constraint instanceof ListOfValuesConstraint listConstraint) {
			return Optional.ofNullable(listConstraint.getShortName()).map(shortName -> shortName.replace(PREFIX_SEPARATOR, "_"));
		}

		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	private void translateAllowedValues(FieldConstraint fieldConstraint, String constraintName) {

		Object allowedValues = fieldConstraint.getParameters().get(ListOfValuesConstraint.ALLOWED_VALUES_PARAM);

		if (!(allowedValues instanceof List)) {
			return;
		}

		List<String> values = (List<String>) allowedValues;
		List<String> translatedValues = new ArrayList<>(values.size());

		for (String allowedValue : values) {
			translatedValues.add(translateAllowedValue(allowedValue, constraintName));
		}

		fieldConstraint.getParameters().put(ListOfValuesConstraint.ALLOWED_VALUES_PARAM, translatedValues);
	}

	/**
	 * Allowed values are provided by Alfresco as "value|label", the label being
	 * resolved with the interface language. Only the values having a translation
	 * for the content language are overridden, so that the constraints computing
	 * their own labels keep them.
	 */
	private String translateAllowedValue(String allowedValue, String constraintName) {

		int separatorIndex = allowedValue.indexOf(LABEL_SEPARATOR);
		String value = separatorIndex > -1 ? allowedValue.substring(0, separatorIndex) : allowedValue;

		return TranslateHelper.findConstraintLabel(constraintName, value, I18NUtil.getContentLocale()).map(label -> value + LABEL_SEPARATOR + label)
				.orElse(allowedValue);
	}

}
