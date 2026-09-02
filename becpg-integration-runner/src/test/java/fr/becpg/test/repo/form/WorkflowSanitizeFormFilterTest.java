package fr.becpg.test.repo.form;

import org.alfresco.repo.forms.FormData;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.form.filter.WorkflowSanitizeFormFilter;

/**
 * Unit tests for {@link WorkflowSanitizeFormFilter}.
 */
public class WorkflowSanitizeFormFilterTest {

	@Test
	public void testBeforePersistSanitizesNonBmpFields() {
		WorkflowSanitizeFormFilter<Object> filter = new WorkflowSanitizeFormFilter<>();
		FormData formData = new FormData();
		formData.addFieldData("prop_bpm_comment", "Validation OK 🦄", false);
		formData.addFieldData("prop_other_field", "Texte normal", false);

		filter.beforePersist(new Object(), formData);

		Assert.assertEquals("Validation OK ", formData.getFieldData("prop_bpm_comment").getValue());
		Assert.assertEquals("Texte normal", formData.getFieldData("prop_other_field").getValue());
	}

	@Test
	public void testBeforePersistNullFormData() {
		WorkflowSanitizeFormFilter<Object> filter = new WorkflowSanitizeFormFilter<>();
		filter.beforePersist(new Object(), null);
	}
}
