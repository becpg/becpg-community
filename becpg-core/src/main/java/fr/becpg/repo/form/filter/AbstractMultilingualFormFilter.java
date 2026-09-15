package fr.becpg.repo.form.filter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.helper.MLTextHelper;

/**
 * <p>Accepts a <b>whole multilingual value in a single form submission</b>.</p>
 *
 * <p>Until now a {@code d:mltext} property could only be written one language at a time. The main
 * form posts the current content locale as a plain scalar, and the other languages go through a
 * second, dedicated call — {@code POST becpg/form/multilingual/field/{field}} — which is what
 * Share's translation dialog does. Any caller that did not know about that second call, and posted
 * the map it had in hand, got the map <b>serialised into the property as text</b>: measured on
 * dev.becpg.fr, {@code bcpg:legalName} held the seven characters-plus string
 * {@code {"fr":"Sucre cristal"}} instead of a translation.</p>
 *
 * <p>This filter closes that gap at the only place that can close it for every caller at once: the
 * form processor's {@code beforePersist}. A field whose <b>dictionary definition</b> says
 * {@code d:mltext} and whose value is a JSON object is turned into a real {@link MLText} before the
 * processor persists it — {@code ContentModelFormProcessor} puts a non-String value straight into
 * the property map, so nothing else has to change.</p>
 *
 * <h3>The guard, and why it is not optional</h3>
 *
 * <p>A text field legitimately containing JSON must not be mistaken for a translation map. So the
 * conversion happens only when <b>every</b> key of the object is a locale the repository supports
 * ({@link MLTextHelper#getSupportedLocalesList()}). One unknown key and the value is left exactly
 * as it was — the filter never guesses, and never destroys what it does not understand.</p>
 *
 * <h3>Merge, not replace</h3>
 *
 * <p>On an existing node the incoming map is merged into the stored {@link MLText}: a language the
 * caller did not send is <b>kept</b>, and a language sent blank is <b>removed</b>. That is exactly
 * the semantics of {@code MultilingualFieldWebScript}, and having the two paths agree is the whole
 * point — a value must not depend on which endpoint wrote it. On a creation there is nothing to
 * merge with, and the map is the value.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractMultilingualFormFilter<ItemType> extends AbstractFilter<ItemType, NodeRef> {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(AbstractMultilingualFormFilter.class);

	/** Prefix Alfresco's form engine gives to a property field. */
	private static final String PROP_PREFIX = "prop_";

	private DictionaryService dictionaryService;

	private NamespaceService namespaceService;

	private NodeService mlNodeService;

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>mlNodeService</code>.</p>
	 *
	 * <b>Must be wired to {@code mlAwareNodeService}</b>, never to {@code nodeService}: the latter
	 * runs through {@code MLPropertyInterceptor}, which collapses an {@code MLText} to the single
	 * value of the content locale. Reading the stored translations through it would return a
	 * String, the merge below would find nothing to keep, and every language the caller did not
	 * resend would be silently dropped on save.
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	/**
	 * Replaces every multilingual field of the submission by an {@link MLText}.
	 *
	 * @param data the form data, modified in place
	 * @param existingNodeRef the node being updated, or {@code null} on a creation
	 */
	protected void convertMlTextFields(FormData data, NodeRef existingNodeRef) {
		if ((data == null) || (dictionaryService == null)) {
			return;
		}

		// Collected first, applied after: `FormData` is iterated here and
		// `addFieldData` writes into the same backing map. The conversion runs
		// once per field — it reads the node, and doing it twice would double
		// the cost of every form carrying a translation.
		Map<String, MLText> converted = new LinkedHashMap<>();

		for (Iterator<FieldData> iterator = data.iterator(); iterator.hasNext();) {
			FieldData fieldData = iterator.next();
			MLText mlText = toMLText(fieldData, existingNodeRef);
			if (mlText != null) {
				converted.put(fieldData.getName(), mlText);
			}
		}

		for (Map.Entry<String, MLText> entry : converted.entrySet()) {
			data.addFieldData(entry.getKey(), entry.getValue(), true);
		}
	}

	/**
	 * The {@link MLText} a field carries, or {@code null} when it carries anything else.
	 *
	 * @param fieldData one submitted field
	 * @param existingNodeRef the node being updated, or {@code null}
	 * @return the merged value, or {@code null} to leave the field untouched
	 */
	private MLText toMLText(FieldData fieldData, NodeRef existingNodeRef) {
		if ((fieldData == null) || (fieldData.getName() == null) || !fieldData.getName().startsWith(PROP_PREFIX)) {
			return null;
		}
		if (!(fieldData.getValue() instanceof String)) {
			return null;
		}

		String raw = ((String) fieldData.getValue()).trim();
		if (!raw.startsWith("{") || !raw.endsWith("}")) {
			return null;
		}

		QName propertyQName = toPropertyQName(fieldData.getName());
		if (propertyQName == null) {
			return null;
		}

		PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
		if ((propertyDef == null) || !DataTypeDefinition.MLTEXT.equals(propertyDef.getDataType().getName())) {
			return null;
		}

		Map<Locale, String> submitted = parseLocaleMap(raw);
		if (submitted == null) {
			return null;
		}

		MLText mlText = readExisting(existingNodeRef, propertyQName);

		for (Map.Entry<Locale, String> entry : submitted.entrySet()) {
			if (entry.getValue().isBlank()) {
				mlText.removeValue(entry.getKey());
			} else {
				mlText.addValue(entry.getKey(), entry.getValue().trim());
			}
		}

		return mlText;
	}

	/**
	 * The value already stored, so a language the caller did not send survives.
	 *
	 * @param nodeRef the node, or {@code null} on a creation
	 * @param propertyQName the property
	 * @return the stored value, never {@code null}
	 */
	private MLText readExisting(NodeRef nodeRef, QName propertyQName) {
		if ((nodeRef == null) || (mlNodeService == null) || !mlNodeService.exists(nodeRef)) {
			return new MLText();
		}
		// `mlAwareNodeService` — see the setter for why the plain one would lose data.
		Object current = mlNodeService.getProperty(nodeRef, propertyQName);
		MLText existing = new MLText();
		if (current instanceof MLText mltext) {
			// `MLText` has no copy constructor — checked against
			// alfresco-data-model 25.3.0.81 — but it extends HashMap<Locale, String>.
			// Copying rather than mutating the stored instance: the value handed
			// back by the NodeService must not be edited in place.
			existing.putAll(mltext);
		} else if (current instanceof String text && !text.isBlank()) {
			// A property written as a plain string by an older path: it belongs to
			// the default locale, and dropping it would lose the very value the
			// caller is translating.
			existing.addValue(MLText.getDefaultLocale(), text);
		}
		return existing;
	}

	/**
	 * Reads a JSON object as a locale map — <b>all or nothing</b>.
	 *
	 * @param raw the submitted string
	 * @return the map, or {@code null} when this is not a translation map
	 */
	private Map<Locale, String> parseLocaleMap(String raw) {
		JSONObject json;
		try {
			json = new JSONObject(raw);
		} catch (JSONException e) {
			// A sentence that happens to look like an object. Not our business.
			return null;
		}

		if (json.length() == 0) {
			return null;
		}

		List<String> supported = MLTextHelper.getSupportedLocalesList();
		Map<Locale, String> values = new LinkedHashMap<>();

		for (Iterator<String> keys = json.keys(); keys.hasNext();) {
			String key = keys.next();
			if (!supported.contains(key)) {
				// One key that is not a locale and the whole value is left alone:
				// this is the guard that keeps a JSON snippet typed into a text
				// field from being silently reinterpreted as translations.
				return null;
			}
			Object value = json.opt(key);
			if (!(value instanceof String)) {
				return null;
			}
			Locale locale = MLTextHelper.parseLocale(key);
			if (locale == null) {
				return null;
			}
			values.put(locale, (String) value);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Multilingual field submitted with " + values.size() + " locale(s)");
		}

		return values;
	}

	/**
	 * {@code prop_bcpg_legalName} → {@code bcpg:legalName}.
	 *
	 * <p>The form engine replaces the colon by an underscore, so the <b>first</b> underscore after
	 * the prefix is the separator; a local name containing underscores keeps them.</p>
	 *
	 * @param fieldName the submitted field name
	 * @return the property, or {@code null} when the name is not a qualified property
	 */
	private QName toPropertyQName(String fieldName) {
		String name = fieldName.substring(PROP_PREFIX.length());
		int separator = name.indexOf('_');
		if (separator <= 0) {
			return null;
		}
		try {
			return QName.createQName(name.substring(0, separator) + ":" + name.substring(separator + 1), namespaceService);
		} catch (Exception e) {
			// An unknown prefix is not an error here: the processor will report it.
			return null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void beforeGenerate(ItemType item, List<String> fields, List<String> forcedFields, Form form,
			Map<String, Object> context) {
		// Intentionally empty: this filter only acts on the write path.
	}

	/** {@inheritDoc} */
	@Override
	public void afterGenerate(ItemType item, List<String> fields, List<String> forcedFields, Form form,
			Map<String, Object> context) {
		// Intentionally empty: this filter only acts on the write path.
	}

	/** {@inheritDoc} */
	@Override
	public void afterPersist(ItemType item, FormData data, NodeRef persistedObject) {
		// Intentionally empty: the conversion happens before the properties are written.
	}

}
