package fr.becpg.repo.designer.impl;

import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.designer.DesignerModel;




//https://github.com/Alfresco/alfresco-ng2-components/blob/development/lib/core/form/components/widgets/core/form-field.model.ts
/**
 * <p>FormJsonVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FormJsonVisitor {

	private NodeService nodeService;
	
	

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>visit.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param ret a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException
	 */
	public void visit(NodeRef nodeRef, JSONObject ret) throws JSONException {

		List<ChildAssociationRef> configEls = nodeService.getChildAssocs(nodeRef);
		DesignerHelper.sort(configEls, nodeService);
		for (ChildAssociationRef assoc : configEls) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_CONFIG_ELEMENTS)) {
				visitConfigEl(assoc.getChildRef(), ret);
			}
		}

	}

	private void visitConfigEl(NodeRef configElNodeRef, JSONObject parent) throws InvalidNodeRefException, JSONException {

		String formType =(String) nodeService.getProperty(configElNodeRef, DesignerModel.PROP_DSG_ID);
		
		JSONObject form = null;
		
		if(parent.has(formType)) {
			form = parent.getJSONObject(formType);
		} else {
			form = new JSONObject();
			parent.put(formType, form);
		}
		
		

		// form.put("evaluator", (String)
		// nodeService.getProperty(configElNodeRef,
		// DesignerModel.PROP_DSG_CONFIGEVALUATOR));

		List<ChildAssociationRef> forms = nodeService.getChildAssocs(configElNodeRef);
		DesignerHelper.sort(forms, nodeService);

		for (ChildAssociationRef assoc : forms) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_FORMS)) {
				visitForm(assoc.getChildRef(), form);
			}
		}

	}

	private void visitForm(NodeRef formNodeRef, JSONObject parent) throws JSONException {

		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(formNodeRef);
		DesignerHelper.sort(assocs, nodeService);

		JSONObject form = new JSONObject();

		String id = (String) nodeService.getProperty(formNodeRef, DesignerModel.PROP_DSG_ID);
		if ((id != null) && (id.length() > 0) && !"default".equals(id) && !"-".equals(id)) {
			parent.put(id, form);
		} else {
			parent.put("default", form);
		}
		
		//Datagrid is another format for now
		if(!"datagrid".equals(id)) {
		
			boolean isTab = false;
	
			String viewFormTemplate = (String) nodeService.getProperty(formNodeRef, DesignerModel.PROP_DSG_VIEWFORMTEMPLATE);
			String editFormTemplate = (String) nodeService.getProperty(formNodeRef, DesignerModel.PROP_DSG_EDITFORMTEMPLATE);
			String createFormTemplate = (String) nodeService.getProperty(formNodeRef, DesignerModel.PROP_DSG_CREATEFORMTEMPLATE);
			if (((viewFormTemplate != null) && !viewFormTemplate.isEmpty() && "/org/alfresco/components/form/tab-edit-form.ftl".equals(viewFormTemplate))
					|| ((editFormTemplate != null) && !editFormTemplate.isEmpty()
							&& "/org/alfresco/components/form/tab-edit-form.ftl".equals(editFormTemplate))
					|| ((createFormTemplate != null) && !createFormTemplate.isEmpty()
							&& "/org/alfresco/components/form/tab-edit-form.ftl".equals(createFormTemplate))) {
	
	
			}
	
			for (ChildAssociationRef assoc : assocs) {
	
				if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_SETS)) {
	
					if (isTab) {
						form.append("fields",visitSet(assoc.getChildRef(), null));
					} else {
						visitTab(assoc.getChildRef(), form);
					}
				}
				if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_FIELDS)) {
					form.append("fields",visitField(assoc.getChildRef(), null));
				}
			}

		}
	}

	// private void appendAtt(Element el, String attrName, NodeRef nodeRef,
	// QName qname) {
	// String attrValue = (String) nodeService.getProperty(nodeRef, qname);
	// if (!StringUtils.isEmpty(attrValue)) {
	// el.setAttribute(attrName, attrValue);
	// }
	//
	// }

	private void visitTab(NodeRef setNodeRef, JSONObject form) throws InvalidNodeRefException, JSONException {

		JSONObject tab = new JSONObject();
		form.append("tabs",tab);

		String tabId = (String) nodeService.getProperty(setNodeRef, DesignerModel.PROP_DSG_ID);
		
		tab.put("id", tabId);
		String label = (String)nodeService.getProperty(setNodeRef, DesignerModel.PROP_DSG_LABEL);
		if(label == null || label.isEmpty()) {
			label = (String)nodeService.getProperty(setNodeRef, DesignerModel.PROP_DSG_LABELID);
		}
		
		if(label!=null && ! label.isEmpty()) {
			tab.put("title", label );
		}

		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(setNodeRef);
		DesignerHelper.sort(assocs, nodeService);
		for (ChildAssociationRef assoc : assocs) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_SETS)) {
				form.append("fields",visitSet(assoc.getChildRef(), tabId));
			}
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_FIELDS)) {
				form.append("fields",visitField(assoc.getChildRef(), tabId));
			}

		}

	}

	@SuppressWarnings("unchecked")
	private JSONObject visitField(NodeRef fieldNodeRef, String tabId) throws InvalidNodeRefException, JSONException {
		boolean hide = (Boolean) nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_HIDE);

		JSONObject field = new JSONObject();
		field.put("tab", tabId);

		if (hide) {
			field.put("hide", true);
		}
		field.put("id", nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_ID));

		List<String> formModes = (List<String>) nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_FORMODE);
		String formMode = "";
		if ((formModes != null) && (formModes.size() > 0)) {
			for (String mode : formModes) {
				if (formMode.length() > 0) {
					formMode += ",";
				}
				formMode += mode;
			}

			if ((formMode.length() > 0) && !formMode.equals("-")) {
				field.put("for-mode", formMode);
			}
		}
		field.put("fieldType", "FormFieldRepresentation");
		if(Boolean.TRUE.equals(nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_FORCE))) {
			field.put("force", true);
		}

		
		String label = (String)nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_LABEL);
		if(label == null || label.isEmpty()) {
			label = (String)nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_LABELID);
		}
		if(label!=null && ! label.isEmpty()) {
			field.put("name", label );
		}
		//TODO Remove that
	//	field.put("type","text");
		
//		field.put("description", nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_DESCRIPTION));
//		field.put("description-id", nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_DESCRIPTIONID));
//		field.put("help", nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_HELP));
//		field.put("help-id", nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_HELPID));

		field.put("readOnly", nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_READONLY));
		field.put("required", nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_MANDATORY));

		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(fieldNodeRef);
		DesignerHelper.sort(assocs, nodeService);
		for (ChildAssociationRef assoc : assocs) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_CONTROLS)) {
				visitControl(assoc.getChildRef(), field);
			}
		}
		return field;
	}

	private JSONObject visitSet(NodeRef setNodeRef, String tabId) throws JSONException {

		JSONObject set = new JSONObject();
		set.put("fieldType", "ContainerRepresentation");
		set.put("id", nodeService.getProperty(setNodeRef, DesignerModel.PROP_DSG_ID));
		set.put("tab", tabId);
		
		String label = (String)nodeService.getProperty(setNodeRef, DesignerModel.PROP_DSG_LABEL);
		if(label == null || label.isEmpty()) {
			label = (String)nodeService.getProperty(setNodeRef, DesignerModel.PROP_DSG_LABELID);
		}
		
		if(label!=null && ! label.isEmpty()) {
			set.put("name", label );
		}
		
//		Header	group	ContainerWidgetComponent
//		N/A	container	ContainerWidgetComponent (layout component)
//		N/A	N/A	UnknownWidgetComponent
		
		int column = 1;
		
		String template = (String) nodeService.getProperty(setNodeRef, DesignerModel.PROP_DSG_TEMPLATEPATH);
		if ((template != null) && !template.isEmpty()) {
			// 2-column-set.ftl
			// 1-column-set.ftl
			// set-message.ftl
			// 3-column-set.ftl
			
			if(template.endsWith("set-message.ftl")) {
				set.put("type","readonly-text");
			} else if(template.endsWith("2-column-set.ftl")) {
				set.put("numberOfColumns",2);
				column = 2;
			} else if(template.endsWith("3-column-set.ftl")) {
				set.put("numberOfColumns",3);
				column = 3;
			} else {
				set.put("numberOfColumns",1);
				JSONObject params = new JSONObject();
				params.put("allowCollapse", true);
				params.put("collapseByDefault", false);
				set.put("params", params);
				
				
			}
			
		}

		String setAppearance = (String) nodeService.getProperty(setNodeRef, DesignerModel.PROP_DSG_APPEARANCE);
		if ((setAppearance != null) && (setAppearance.length() > 0) && !setAppearance.equals("-")) {

			if(setAppearance.endsWith("bordered-panel")) {
				set.put("type","group");
			}
			
//		<value>fieldset</value>
//		<value>panel</value>
//		<value>bordered-panel</value>
//		<value>title</value>
//		<value>whitespace</value>
		}

		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(setNodeRef);
		DesignerHelper.sort(assocs, nodeService);
		int i = 0;
		
		for (ChildAssociationRef assoc : assocs) {
			String colName = ""+((i%column)+1);
		  	JSONArray columnJson = new JSONArray();
		  	if(set.has("fields") && set.getJSONObject("fields").has(colName)) {
		  		columnJson = set.getJSONObject("fields").getJSONArray(colName);
		  	} else {
		  		JSONObject fields = new JSONObject();
		  		if(set.has("fields")){
		  			fields = set.getJSONObject("fields");
		  		} else {
		  			set.put("fields",fields);
		  		}
		  		
		  		fields.put(colName,columnJson);
		  	}
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_SETS)) {
				columnJson.put(visitSet(assoc.getChildRef(), null));
			}
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_FIELDS)) {
				columnJson.put(visitField(assoc.getChildRef(), null));
			}
			 i++;
		}

		return set;
	}

	private void visitControl(NodeRef controlRef, JSONObject field) throws JSONException {

		
//		Text	text	TextWidgetComponent
//		Multi-line text	multi-line-text	MultilineTextWidgetComponentComponent
//		Number	integer	NumberWidgetComponent
//		Checkbox	boolean	CheckboxWidgetComponent
//		Date	date	DateWidgetComponent
//		Dropdown	dropdown	DropdownWidgetComponent
//		Typeahead	typeahead	TypeaheadWidgetComponent
//		Amount	amount	AmountWidgetComponent
//		Radio buttons	radio-buttons	RadioButtonsWidgetComponent
//		People	people	PeopleWidgetComponent
//		Group of people	functional-group	FunctionalGroupWidgetComponent
//		Dynamic table	dynamic-table	DynamicTableWidgetComponent
//		Hyperlink	hyperlink	HyperlinkWidgetComponent		
//		Attach File	upload	AttachWidgetComponent or UploadWidgetComponent (based on metadata)
//		Display value	readonly	TextWidgetComponent
//		Display text	readonly-text	DisplayTextWidgetComponent

		
		
		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(controlRef);
		DesignerHelper.sort(assocs, nodeService);
		for (ChildAssociationRef assoc : assocs) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_PARAMETERS)) {
				
				String template = (String) nodeService.getProperty(assoc.getChildRef(), DesignerModel.PROP_DSG_TEMPLATEPATH);
				if ((template != null) && !template.isEmpty()) {
					if(template.endsWith("date.ftl")) {
						field.put("type","date");
//						<control-param name="showTime" optional="true" type="boolean"/>
					} else if(template.endsWith("info.ftl")) {
						field.put("type","readonly-text");
					} else if(template.endsWith("checkbox.ftl")) {
						field.put("type","boolean");
					} else if(template.endsWith("number.ftl")) {
						field.put("type","integer");
//						<control-param name="maxLength" optional="true" type="int"/>
					} else if(template.endsWith("selectone.ftl")) {
						field.put("type","dropdown");
//						<control-param name="options" mandatory="true"
//								type="comma separated string"/>
					} else if(template.endsWith("selectmany.ftl")) {
						field.put("type","dropdown");
//						<control-param name="options" mandatory="true"
//								type="comma separated string"/>

					
					} else if(template.endsWith("textarea.ftl")) {
						field.put("type","multi-line-text");
						
//						<control-param name="rows" optional="true" type="int"/>
//						<control-param name="columns" optional="true" type="int"/>
//						<control-param name="activateLinks" optional="true"
//							type="boolean"/>

					
					} else if(template.endsWith("textfield.ftl")) {
						field.put("type","text");
//						<control-param name="maxLength" optional="true" type="int"/>

					
					} else if(template.endsWith("autocomplete.ftl")) {
						field.put("type","text");
//						<control-param name="ds" optional="false" type="string" default="becpg/autocomplete/..."/>
//						<control-param name="parent" optional="true" type="string" />
//						<control-param name="local" optional="true" type="boolean" />
//						<control-param name="style" optional="true" type="string"/>
//						<control-param name="maxLength" optional="true" type="int"/>
//						<control-param name="size" optional="true" type="int"/>
//						<control-param name="showTooltip" optional="true" type="boolean" />
//						<control-param name="showPage" optional="true" type="boolean" />
//						<control-param name="saveTitle" optional="true" type="boolean" />

					
					} else if(template.endsWith("autocomplete-association.ftl")) {
						field.put("type","text");
//						<control-param name="ds" optional="true" type="string" />
//				    	<control-param name="style" optional="true" type="string"/>
//						<control-param name="maxLength" optional="true" type="int"/>
//						<control-param name="size" optional="true" type="int"/>
//						<control-param name="parent" optional="true" type="string"/>
					} else if(template.endsWith("mtlangue.ftl")) {
						field.put("type","text");
					} else if(template.endsWith("selectcolorpicker.ftl")) {
						field.put("type","text");
					} else if(template.endsWith("period.ftl")) {
						field.put("type","text");
					} else if(template.endsWith("authority.ftl")) {
						field.put("type","text");
					} else if(template.endsWith("association.ftl")) {
						field.put("type","text");
//						<control-param name="compactMode" optional="true" type="boolean"/>
//						<control-param name="displayMode" optional="true" type="string" default="items"/>
//						<control-param name="showTargetLink" optional="true"/>
//						<control-param name="selectedValueContextProperty"/>
//						<control-param name="selectActionLabel" optional="true" default="select"/>
//						<control-param name="selectActionLabelId" optional="true"/>
//						<control-param name="forceEditable" optional="true" default="false"/>
//						<control-param name="startLocation" optional="true" default="{company home}"/>
//						<control-param name="startLocationParams" optional="true"/>
//						<control-param name="allowNavigationToContentChildren"/>
//						<control-param name="editorAppearance"/>
					}  else if(template.endsWith("number-unit.ftl")) {
						field.put("type","integer");
//						<control-param name="unit">kg</control-param>
					}else if(template.endsWith("nutrient-class.ftl")) {
						field.put("type","text");
					} else if(template.endsWith("numberrange.ftl")) {
						field.put("type","text");
					} else if(template.endsWith("daterange.ftl")) {
						field.put("type","text");
					} else if(template.endsWith("spel-editor.ftl")) {
						field.put("type","text");
					}
					
					
				}
				
				
				// visitParameterXml(assoc.getChildRef(), control);
			}
		}
	}
	//
	// private void visitParameterXml(NodeRef parameterRef, Element control) {
	// String value = (String) nodeService.getProperty(parameterRef,
	// DesignerModel.PROP_DSG_PARAMETERVALUE);
	// if (!StringUtils.isEmpty(value)) {
	// Element controlParam = DOMUtils.createElement(control, "control-param");
	// controlParam.setAttribute("name", (String)
	// nodeService.getProperty(parameterRef, DesignerModel.PROP_DSG_ID));
	// controlParam.setTextContent(value);
	// }
	// }

}
