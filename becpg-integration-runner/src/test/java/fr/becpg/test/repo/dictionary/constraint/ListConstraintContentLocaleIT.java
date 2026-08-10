/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.dictionary.constraint;

import java.util.List;
import java.util.Locale;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormService;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Integration tests checking that the labels of the fixed lists of values follow
 * the content language, as the dynamic ones do.
 *
 * @author matthieu
 */
public class ListConstraintContentLocaleIT extends PLMBaseTestCase {

	private static final String SYSTEM_STATE_CONSTRAINT = "bcpg_systemState";

	private static final String TO_VALIDATE_VALUE = "ToValidate";

	private static final String TO_VALIDATE_LABEL_FR = "A valider";

	private static final String TO_VALIDATE_LABEL_EN = "To validate";

	@Autowired
	private FormService formService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Test
	public void testDisplayValueFollowsContentLocale() {

		Locale currentLocale = I18NUtil.getLocale();
		Locale currentContentLocale = I18NUtil.getContentLocale();

		try {
			I18NUtil.setLocale(Locale.ENGLISH);

			I18NUtil.setContentLocale(Locale.FRENCH);
			assertEquals(TO_VALIDATE_LABEL_FR, TranslateHelper.getConstraint(SYSTEM_STATE_CONSTRAINT, TO_VALIDATE_VALUE, false));

			I18NUtil.setContentLocale(Locale.ENGLISH);
			assertEquals(TO_VALIDATE_LABEL_EN, TranslateHelper.getConstraint(SYSTEM_STATE_CONSTRAINT, TO_VALIDATE_VALUE, false));
		} finally {
			I18NUtil.setLocale(currentLocale);
			I18NUtil.setContentLocale(currentContentLocale);
		}
	}

	@Test
	public void testFormLabelsFollowContentLocale() {

		final NodeRef productNodeRef = inWriteTx(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Product " + ListConstraintContentLocaleIT.class.getSimpleName());
			finishedProduct.setUnit(ProductUnit.kg);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();
		});

		Locale currentLocale = I18NUtil.getLocale();
		Locale currentContentLocale = I18NUtil.getContentLocale();

		try {
			I18NUtil.setLocale(Locale.ENGLISH);
			I18NUtil.setContentLocale(Locale.FRENCH);

			inReadTx(() -> {
				List<String> allowedValues = extractProductStateValues(productNodeRef);

				assertTrue("Check the state label is translated in the content language: " + allowedValues,
						allowedValues.contains(TO_VALIDATE_VALUE + "|" + TO_VALIDATE_LABEL_FR));

				return null;
			});
		} finally {
			I18NUtil.setLocale(currentLocale);
			I18NUtil.setContentLocale(currentContentLocale);
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> extractProductStateValues(NodeRef productNodeRef) {

		Form form = formService.getForm(new Item("node", productNodeRef.toString()));

		for (FieldDefinition fieldDefinition : form.getFieldDefinitions()) {
			if (PLMModel.PROP_PRODUCT_STATE.toPrefixString(namespaceService).equals(fieldDefinition.getName())
					&& (fieldDefinition instanceof PropertyFieldDefinition propertyFieldDefinition)) {

				for (FieldConstraint fieldConstraint : propertyFieldDefinition.getConstraints()) {
					Object allowedValues = fieldConstraint.getParameters().get(ListOfValuesConstraint.ALLOWED_VALUES_PARAM);

					if (allowedValues instanceof List) {
						return (List<String>) allowedValues;
					}
				}
			}
		}

		return List.of();
	}

}
