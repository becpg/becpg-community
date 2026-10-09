package fr.becpg.repo.form.filter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.AbstractFilter;

import fr.becpg.repo.helper.UnicodeHelper;

/**
 * Filter that sanitizes non-BMP characters from workflow form submissions
 * to prevent MySQL utf8mb3 collation errors on Activiti tables.
 *
 * @param <ItemType> the form item type (WorkflowTask or WorkflowDefinition)
 * @author beCPG
 */
public class WorkflowSanitizeFormFilter<ItemType> extends AbstractFilter<ItemType, Object> {

	/** {@inheritDoc} */
	@Override
	public void beforeGenerate(ItemType item, List<String> fields, List<String> forcedFields, Form form, Map<String, Object> context) {
		// Nothing to do before the form is generated
	}

	/** {@inheritDoc} */
	@Override
	public void afterGenerate(ItemType item, List<String> fields, List<String> forcedFields, Form form, Map<String, Object> context) {
		// Nothing to do after the form is generated
	}

	/** {@inheritDoc} */
	@Override
	public void beforePersist(ItemType item, FormData data) {
		if (data == null) {
			return;
		}

		Map<String, String> sanitizedFields = collectSanitizedFields(data);
		for (Map.Entry<String, String> entry : sanitizedFields.entrySet()) {
			data.addFieldData(entry.getKey(), entry.getValue(), true);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void afterPersist(ItemType item, FormData data, Object persistedObject) {
		// Nothing to do after persist
	}

	private Map<String, String> collectSanitizedFields(FormData data) {
		Map<String, String> sanitizedFields = new LinkedHashMap<>();
		for (Iterator<FieldData> iterator = data.iterator(); iterator.hasNext();) {
			FieldData fieldData = iterator.next();
			if ((fieldData != null) && fieldData.getValue() instanceof String strVal) {
				String sanitized = UnicodeHelper.sanitizeBmp(strVal);
				if (!Objects.equals(strVal, sanitized)) {
					sanitizedFields.put(fieldData.getName(), sanitized);
				}
			}
		}
		return sanitizedFields;
	}
}
