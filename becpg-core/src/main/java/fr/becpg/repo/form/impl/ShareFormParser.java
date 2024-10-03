package fr.becpg.repo.form.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.becpg.common.dom.DOMUtils;
import fr.becpg.repo.form.FormParser;

/**
 * <p>ShareFormParser class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ShareFormParser implements FormParser{

	private static Log logger = LogFactory.getLog(ShareFormParser.class);

	// <config evaluator="node-type" condition="type">
	// <forms>
	// <form [id="string"] [submission-url="url"]>
	// <view-form template="path" />
	// <edit-form template="path" />
	// <create-form template="path" />
	// <field-visibility>
	// <show id="string" [for-mode="view|edit|create"] [force="boolean"] />
	// <hide id="string" [for-mode="view|edit|create"] />
	// </field-visibility>
	// <appearance>
	// <set id="string"
	// appearance="fieldset|panel|bordered-panel|title|whitespace|"
	// [parent="string"] [label="string"] [label-id="key"] [template="path"] />
	//
	// <field id="string" [label-id="key"] [label="string"]
	// [description-id="key"] [description="string"]
	// [help="string"] [help-id="key"] [read-only="boolean"]
	// [mandatory="boolean"] [set="string"]>
	// <control [template="path"]>
	// <control-param name="name">value</control-param>
	// </control>
	// <constraint-handlers>
	// <constraint type=BecpgFormDefinition.PROP_ID validation-handler="function" [message-id="string"]
	// [message="string"] [event="string"] />
	// </constraint-handlers>
	// </field>
	// </appearance>
	// </form>
	// </forms>
	// </config>

	/**
	 * {@inheritDoc}
	 *
	 * <p>visitConfig.</p>
	 */
	@Override
	public void visitConfig(Map<String, Map<String, BecpgFormDefinition>> definitions, InputStream in) throws JSONException, SAXException, IOException, ParserConfigurationException, FactoryConfigurationError
		 {
		logger.debug("visitConfigNodeRef");
		if (in != null) {
			Document doc = DOMUtils.parse(in);
			NodeList list = doc.getElementsByTagName("config");
			for (int i = 0; i < list.getLength(); i++) {
				Element elem = (Element) list.item(i);

				Map<String, BecpgFormDefinition> forms = new HashMap<>();
				if (definitions.containsKey(elem.getAttribute("condition")) && !"true".equals(elem.getAttribute("replace"))) {
					forms = definitions.get(elem.getAttribute("condition"));
				} else {
					definitions.put(elem.getAttribute("condition"), forms);
				}

				visitFormElement(elem, forms, elem.getAttribute("evaluator"));
			}
		}

	}
	

	/** {@inheritDoc} */
	@Override
	public void visitConfig(Map<String, Map<String, BecpgFormDefinition>> definitions, String defs) throws IOException, JSONException, SAXException, ParserConfigurationException, FactoryConfigurationError  {
		try(InputStream in = new ByteArrayInputStream(defs.getBytes())){
			visitConfig(definitions, in);
		}
	}


	private void visitFormElement(Element configEl, Map<String, BecpgFormDefinition> forms, String evaluator) throws JSONException {
		logger.debug("visitFormElement");
		NodeList list = configEl.getElementsByTagName("form");
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);

			String formId = elem.getAttribute(BecpgFormDefinition.PROP_ID);

			if ((formId == null) || ("-".equals(formId)) || formId.isEmpty()) {
				if ("model".equals(evaluator)) {
					formId = "create";
				} else {
					formId = "default";
				}
			}

			BecpgFormDefinition formDef = new BecpgFormDefinition();
			if (forms.containsKey(formId)) {
				formDef = forms.get(formId);
			} else {
				forms.put(formId, formDef);
			}

			boolean hasTab = false;

			String viewFormTemplate = null;
			String editFormTemplate = null;
			String createFormTemplate = null;

			NodeList editForms = configEl.getElementsByTagName("edit-form");
			if ((editForms != null) && (editForms.getLength() > 0)) {
				Element tmpEL = (Element) editForms.item(0);
				editFormTemplate = tmpEL.getAttribute("template");
			}

			NodeList viewForms = configEl.getElementsByTagName("view-form");
			if ((viewForms != null) && (viewForms.getLength() > 0)) {
				Element tmpEL = (Element) viewForms.item(0);
				viewFormTemplate = tmpEL.getAttribute("template");
			}

			NodeList createForms = configEl.getElementsByTagName("create-form");
			if ((createForms != null) && (createForms.getLength() > 0)) {
				Element tmpEL = (Element) createForms.item(0);
				createFormTemplate = tmpEL.getAttribute("template");
			}

			if (((viewFormTemplate != null) && !viewFormTemplate.isEmpty()
					&& "/org/alfresco/components/form/tab-edit-form.ftl".equals(viewFormTemplate))
					|| ((editFormTemplate != null) && !editFormTemplate.isEmpty()
							&& "/org/alfresco/components/form/tab-edit-form.ftl".equals(editFormTemplate))
					|| ((createFormTemplate != null) && !createFormTemplate.isEmpty()
							&& "/org/alfresco/components/form/tab-edit-form.ftl".equals(createFormTemplate))) {

				hasTab = true;
			}

			visitFormSets(formDef, elem, hasTab);
			visitFormFields(formDef, elem);
		}

	}

	private void visitFormSets(BecpgFormDefinition formDef, Element parentEl, boolean hasTab) throws JSONException {
		NodeList list = parentEl.getElementsByTagName("set");
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);
			String parentId = elem.getAttribute("parent");

			String setId = elem.getAttribute(BecpgFormDefinition.PROP_ID);

			String label = elem.getAttribute(BecpgFormDefinition.PROP_LABEL);
			if ((label == null) || label.isEmpty()) {
				label = I18NUtil.getMessage(elem.getAttribute("label-id"));
			}

			if (hasTab && ((parentId == null) || parentId.isEmpty())) {

				JSONObject tab = new JSONObject();

				tab.put(BecpgFormDefinition.PROP_ID, setId);

				if ((label != null) && !label.isEmpty()) {
					tab.put("title", label);
				}

				formDef.addTab(setId, tab);

			} else {

				JSONObject set = new JSONObject();
				set.put("fieldType", "ContainerRepresentation");
				set.put(BecpgFormDefinition.PROP_ID, setId);

			

				if ((label != null) && !label.isEmpty()) {
					set.put(BecpgFormDefinition.PROP_LABEL, label);
				}

				// Header group ContainerWidgetComponent
				// N/A container ContainerWidgetComponent (layout component)
				// N/A N/A UnknownWidgetComponent

				String template = elem.getAttribute("template");
				if ((template != null) && !template.isEmpty()) {
					// 2-column-set.ftl
					// 1-column-set.ftl
					// set-message.ftl
					// 3-column-set.ftl

					if (template.endsWith("set-message.ftl")) {
						set.put("type", "readonly-text");
					} else if (template.endsWith("2-column-set.ftl")) {
						set.put("numberOfColumns", 2);
					} else if (template.endsWith("3-column-set.ftl")) {
						set.put("numberOfColumns", 3);
					} else {
						set.put("numberOfColumns", 1);
						JSONObject params = new JSONObject();
						params.put("allowCollapse", true);
						params.put("collapseByDefault", false);
						set.put("params", params);

					}

				}

				String setAppearance = elem.getAttribute("appearance");
				if ((setAppearance != null) && (setAppearance.length() > 0) && !setAppearance.equals("-")) {

					if (setAppearance.endsWith("bordered-panel")) {
						set.put("type", "group");
					}

					// <value>fieldset</value>
					// <value>panel</value>
					// <value>bordered-panel</value>
					// <value>title</value>
					// <value>whitespace</value>
				}


				formDef.addSet(parentId, set);
				
			

			}
		}
	}

	private void visitFormFields(BecpgFormDefinition formDef, Element parentEl) throws JSONException {
		NodeList fields = parentEl.getElementsByTagName("field");
		NodeList hides = parentEl.getElementsByTagName("hide");
		NodeList shows = parentEl.getElementsByTagName("show");

		visitFormFields(formDef, fields, shows, true);

		visitFormFields(formDef, fields, hides, false);

	}

	private void visitFormFields(BecpgFormDefinition formDef, NodeList fields, NodeList shows, boolean show) throws JSONException {

		for (int i = 0; i < shows.getLength(); i++) {

			Element elem = (Element) shows.item(i);
			String fieldId = elem.getAttribute(BecpgFormDefinition.PROP_ID);
			boolean added = false;
			for (int j = 0; j < fields.getLength(); j++) {
				Element field = (Element) fields.item(j);
				if (fieldId.equals(field.getAttribute(BecpgFormDefinition.PROP_ID))) {
					added = true;
					createField(formDef, elem, field, show);
				}
			}
			if (!added) {
				createField(formDef, elem, null, show);
			}
		}
	}

	private void createField(BecpgFormDefinition formDef, Element elem, Element field, boolean show) throws JSONException {
		logger.debug("Create field with id :" + elem.getAttribute(BecpgFormDefinition.PROP_ID));

		JSONObject jsonField = new JSONObject();

		String fieldName = elem.getAttribute(BecpgFormDefinition.PROP_ID);
		jsonField.put(BecpgFormDefinition.PROP_ID, fieldName);
		if (!show) {
			jsonField.put("hide", true);
		}

		if (!StringUtils.isEmpty(elem.getAttribute("for-mode"))) {
			jsonField.put("mode", elem.getAttribute("for-mode"));
		}

		jsonField.put("fieldType", "FormFieldRepresentation");

		boolean force = false;
		if (!StringUtils.isEmpty(elem.getAttribute(PROP_FORCE)) && "true".equals(elem.getAttribute(PROP_FORCE))) {
			force = true;
		}

		String setId = null;

		if (field != null) {
			setId = field.getAttribute("set");

			String label = field.getAttribute(BecpgFormDefinition.PROP_LABEL);
			if ((label == null) || label.isEmpty()) {
				label = I18NUtil.getMessage(field.getAttribute("label-id"));
			}
			if ((label != null) && !label.isEmpty()) {
				jsonField.put(BecpgFormDefinition.PROP_LABEL, label);
			}
			if (!StringUtils.isEmpty(field.getAttribute("read-only"))) {
				jsonField.put(PROP_READONLY, field.getAttribute("read-only"));
			}
			if (!StringUtils.isEmpty(field.getAttribute(BecpgFormDefinition.PROP_MANDATORY))) {
				jsonField.put(BecpgFormDefinition.PROP_MANDATORY, field.getAttribute(BecpgFormDefinition.PROP_MANDATORY));
			}

			visitFormControl(jsonField, field);
		}

		formDef.addField(setId, jsonField, force);

	}

	private void visitFormControl(JSONObject field, Element parentEl) throws JSONException {
		logger.debug("visitFormControl");
		NodeList list = parentEl.getElementsByTagName("control");
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);

			String template = elem.getAttribute("template");

			// Text text TextWidgetComponent
			// Multi-line text multi-line-text
			// MultilineTextWidgetComponentComponent
			// Number integer NumberWidgetComponent
			// Checkbox boolean CheckboxWidgetComponent
			// Date date DateWidgetComponent
			// Dropdown dropdown DropdownWidgetComponent
			// Typeahead typeahead TypeaheadWidgetComponent
			// Amount amount AmountWidgetComponent
			// Radio buttons radio-buttons RadioButtonsWidgetComponent
			// People people PeopleWidgetComponent
			// Group of people functional-group FunctionalGroupWidgetComponent
			// Dynamic table dynamic-table DynamicTableWidgetComponent
			// Hyperlink hyperlink HyperlinkWidgetComponent
			// Attach File upload AttachWidgetComponent or UploadWidgetComponent
			// (based on metadata)
			// Display value readonly TextWidgetComponent
			// Display text readonly-text DisplayTextWidgetComponent

			if ((template != null) && !template.isEmpty()) {
				if (template.endsWith("date.ftl")) {
					field.put("type", "date");
					// <control-param name="showTime" optional="true"
					// type="boolean"/>
				} else if (template.endsWith("info.ftl")) {
					field.put("type", "readonly-text");
				} else if (template.endsWith("checkbox.ftl")) {
					field.put("type", "boolean");
				} else if (template.endsWith("number.ftl")) {
					field.put("type", "integer");
					// <control-param name="maxLength" optional="true"
					// type="int"/>
				} else if (template.endsWith("selectone.ftl")) {
					field.put("type", "dropdown");
					// <control-param name="options" mandatory="true"
					// type="comma separated string"/>
				} else if (template.endsWith("selectmany.ftl")) {
					field.put("type", "dropdown");
					// <control-param name="options" mandatory="true"
					// type="comma separated string"/>

				} else if (template.endsWith("textarea.ftl")) {
					field.put("type", "multi-line-text");

					// <control-param name="rows" optional="true" type="int"/>
					// <control-param name="columns" optional="true"
					// type="int"/>
					// <control-param name="activateLinks" optional="true"
					// type="boolean"/>

				} else if (template.endsWith("textfield.ftl")) {
					field.put("type", "text");
					// <control-param name="maxLength" optional="true"
					// type="int"/>

				} else if (template.endsWith("autocomplete.ftl")) {
					field.put("type", "autocomplete");
					// <control-param name="ds" optional="false" type="string"
					// default="becpg/autocomplete/..."/>
					// <control-param name="parent" optional="true"
					// type="string" />
					// <control-param name="local" optional="true"
					// type="boolean" />
					// <control-param name="style" optional="true"
					// type="string"/>
					// <control-param name="maxLength" optional="true"
					// type="int"/>
					// <control-param name="size" optional="true" type="int"/>
					// <control-param name="showTooltip" optional="true"
					// type="boolean" />
					// <control-param name="showPage" optional="true"
					// type="boolean" />
					// <control-param name="saveTitle" optional="true"
					// type="boolean" />

				} else if (template.endsWith("autocomplete-association.ftl")) {
					field.put("type", "autocomplete");
					
					
					
					// <control-param name="ds" optional="true" type="string" />
					// <control-param name="style" optional="true"
					// type="string"/>
					// <control-param name="maxLength" optional="true"
					// type="int"/>
					// <control-param name="size" optional="true" type="int"/>
					// <control-param name="parent" optional="true"
					// type="string"/>
				} else if (template.endsWith("mtlangue.ftl")) {
					field.put("type", "mtlangue");
				} else if (template.endsWith("selectcolorpicker.ftl")) {
					field.put("type", "color");
				} else if (template.endsWith("period.ftl")) {
					field.put("type", "text");
				} else if (template.endsWith("authority.ftl")) {
					field.put("type", "person");
				} else if (template.endsWith("association.ftl")) {
					field.put("type", "autocomplete");
					// <control-param name="compactMode" optional="true"
					// type="boolean"/>
					// <control-param name="displayMode" optional="true"
					// type="string" default="items"/>
					// <control-param name="showTargetLink" optional="true"/>
					// <control-param name="selectedValueContextProperty"/>
					// <control-param name="selectActionLabel" optional="true"
					// default="select"/>
					// <control-param name="selectActionLabelId"
					// optional="true"/>
					// <control-param name="forceEditable" optional="true"
					// default="false"/>
					// <control-param name="startLocation" optional="true"
					// default="{company home}"/>
					// <control-param name="startLocationParams"
					// optional="true"/>
					// <control-param name="allowNavigationToContentChildren"/>
					// <control-param name="editorAppearance"/>
				} else if (template.endsWith("number-unit.ftl")) {
					field.put("type", "number-unit");
					// <control-param name="unit">kg</control-param>
				} else if (template.endsWith("nutrient-class.ftl")) {
					field.put("type", "nutrient-score");
				} else if (template.endsWith("numberrange.ftl")) {
					field.put("type", "number-range");
				} else if (template.endsWith("daterange.ftl")) {
					field.put("type", "date-range");
				} else if (template.endsWith("spel-editor.ftl")) {
					field.put("type", "spel-editor");
				}

				
				visitParam(elem,field);
			}

		}

	}

	private void visitParam(Element elem, JSONObject field) throws JSONException {
		JSONObject jsonParams = new JSONObject();
		 boolean hasParam = false;
		 NodeList params = elem.getElementsByTagName("control-param");
		 for (int j = 0; j < params.getLength(); j++) {
			 hasParam = true;
			 Element param = (Element) params.item(j);
			 jsonParams.put( param.getAttribute("name"),  param.getTextContent());
		}
		 if(hasParam) {
			 field.put("params",jsonParams);
		 }
		
	}

}
