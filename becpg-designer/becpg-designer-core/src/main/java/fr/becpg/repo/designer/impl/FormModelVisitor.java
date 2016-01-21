/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.designer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.becpg.common.dom.DOMUtils;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.data.FormControl;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
public class FormModelVisitor {

	private static final Log logger = LogFactory.getLog(FormModelVisitor.class);

	private NodeService nodeService;

	/**
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Build a list of controls
	 * 
	 * @param is
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 */
	public List<FormControl> visitControls(InputStream is) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
		List<FormControl> ret = new LinkedList<>();

		Document doc = DOMUtils.parse(is);

		NodeList list = doc.getElementsByTagName("control");
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);
			FormControl formControl = new FormControl();
			formControl.setId(elem.getAttribute("id"));
			formControl.setDescription(I18NUtil.getMessage("control." + elem.getAttribute("id") + ".description"));
			ret.add(formControl);
		}

		return ret;
	}

	public void visitModelTemplate(NodeRef ret, QName nodeTypeQname, String controlId, InputStream is) throws SAXException, IOException, ParserConfigurationException,
			FactoryConfigurationError {

		if (controlId != null) {
			Document doc = DOMUtils.parse(is);

			NodeList list = doc.getElementsByTagName("control");
			for (int i = 0; i < list.getLength(); i++) {
				Element elem = (Element) list.item(i);
				if (controlId.equals(elem.getAttribute("id"))) {
					logger.debug("found control : " + controlId);
					nodeService.setProperty(ret, DesignerModel.PROP_DSG_TEMPLATEPATH, elem.getAttribute("template"));
					nodeService.setProperty(ret, DesignerModel.PROP_DSG_ID, controlId);
					NodeList params = elem.getElementsByTagName("control-param");
					for (int j = 0; j < params.getLength(); j++) {
						Element param = (Element) params.item(j);
						ChildAssociationRef childAssociationRef = nodeService.createNode(ret, DesignerModel.ASSOC_DSG_PARAMETERS, DesignerModel.ASSOC_DSG_PARAMETERS,
								DesignerModel.TYPE_DSG_CONTROLPARAMETER);
						NodeRef paramRef = childAssociationRef.getChildRef();
						nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_OPTIONAL, param.getAttribute("optional"));
						nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_ID, param.getAttribute("name"));
						nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_PARAMETERTYPE, param.getAttribute("type"));
						nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_PARAMETERVALUE, param.getAttribute("default"));
						nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_PARAMETERDESCRIPTION,
								I18NUtil.getMessage("control." + controlId + ".param." + param.getAttribute("name")));

					}

				}
			}
		}

	}

	/**
	 * @param nodeRef
	 * @param out
	 * @throws FactoryConfigurationError
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public void visitConfigXml(NodeRef nodeRef, OutputStream out) throws ParserConfigurationException, FactoryConfigurationError, TransformerException {

		Element config = DOMUtils.createDoc("alfresco-config");

		List<ChildAssociationRef> configEls = nodeService.getChildAssocs(nodeRef);
		
		DesignerHelper.sort(configEls,nodeService);
		for (ChildAssociationRef assoc : configEls) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_CONFIG_ELEMENTS)) {
				visitConfigElXml(assoc.getChildRef(), config);
			}
		}

		DOMUtils.serialise(config.getOwnerDocument(), out);

	}

	private void visitConfigElXml(NodeRef configElNodeRef, Element parent) {
		Element configEl = DOMUtils.createElement(parent, "config");

		List<ChildAssociationRef> forms = nodeService.getChildAssocs(configElNodeRef);
		DesignerHelper.sort(forms,nodeService);
		appendAtt(configEl, "evaluator", configElNodeRef, DesignerModel.PROP_DSG_CONFIGEVALUATOR);
		appendAtt(configEl, "condition", configElNodeRef, DesignerModel.PROP_DSG_ID);

		Boolean replace = (Boolean) nodeService.getProperty(configElNodeRef, DesignerModel.PROP_DSG_CONFIGREPLACE);
		if (replace != null) {
			configEl.setAttribute("replace", replace.toString());
		}

		Element formsEl = DOMUtils.createElement(configEl, "forms");
		
		for (ChildAssociationRef assoc : forms) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_FORMS)) {
				visitFormXml(assoc.getChildRef(), formsEl);
			}
		}

	}
	

	private void visitFormXml(NodeRef formNodeRef, Element parent) {
		Element formEl = DOMUtils.createElement(parent, "form");
		Element fieldVisibility = DOMUtils.createElement(formEl, "field-visibility");
		Element appearance = DOMUtils.createElement(formEl, "appearance");

		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(formNodeRef);
		DesignerHelper.sort(assocs,nodeService);
		
		String id = (String) nodeService.getProperty(formNodeRef, DesignerModel.PROP_DSG_ID);
		if (id != null && id.length() > 0 && !"default".equals(id) && !"-".equals(id)) {
			formEl.setAttribute("id", id);
		}
		appendAtt(formEl, "submission-url", formNodeRef, DesignerModel.PROP_DSG_SUBMISSION_URL);
		for (ChildAssociationRef assoc : assocs) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_SETS)) {
				visitSetXml(assoc.getChildRef(), formEl, fieldVisibility, appearance, "");
			}
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_FIELDS)) {
				visitFieldXml(assoc.getChildRef(), formEl, fieldVisibility, appearance, "");
			}
		}
		
		String editFormTemplate = (String) nodeService.getProperty(formNodeRef, DesignerModel.PROP_DSG_EDITFORMTEMPLATE);
		if(editFormTemplate!=null && !editFormTemplate.isEmpty()){
			Element formTemplateEl = DOMUtils.createElement(formEl, "edit-form");
			formTemplateEl.setAttribute("template", editFormTemplate);
		}
		
		String viewFormTemplate = (String) nodeService.getProperty(formNodeRef, DesignerModel.PROP_DSG_VIEWFORMTEMPLATE);
		if(viewFormTemplate!=null && !viewFormTemplate.isEmpty()){
			Element formTemplateEl = DOMUtils.createElement(formEl, "view-form");
			formTemplateEl.setAttribute("template", viewFormTemplate);
		}
		String createFormTemplate = (String) nodeService.getProperty(formNodeRef, DesignerModel.PROP_DSG_CREATEFORMTEMPLATE);
		if(createFormTemplate!=null && !createFormTemplate.isEmpty()){
			Element formTemplateEl = DOMUtils.createElement(formEl, "create-form");
			formTemplateEl.setAttribute("template", createFormTemplate);
		}
	}

	private void appendAtt(Element el, String attrName, NodeRef nodeRef, QName qname) {
		String attrValue = (String) nodeService.getProperty(nodeRef, qname);
		if (!StringUtils.isEmpty(attrValue)) {
			el.setAttribute(attrName, attrValue);
		}

	}

	@SuppressWarnings("unchecked")
	private void visitFieldXml(NodeRef fieldNodeRef, Element parent, Element fieldVisibility, Element appearance, String setId) {
		boolean hide = (Boolean) nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_HIDE);
		Element el;
		if (hide) {
			el = DOMUtils.createElement(fieldVisibility, "hide");
		} else {
			el = DOMUtils.createElement(fieldVisibility, "show");
		}
		el.setAttribute("id", (String) nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_ID));
		List<String> formModes = (List<String>) nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_FORMODE);
		String formMode = "";
		if (formModes != null && formModes.size() > 0) {
			for (String mode : formModes) {
				if (formMode.length() > 0) {
					formMode += ",";
				}
				formMode += mode;
			}

			if (formMode.length()>0 && !formMode.equals("-")) {
				el.setAttribute("for-mode", formMode);
			}
		}

		el.setAttribute("force", nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_FORCE).toString());

		Element field = DOMUtils.createElement(appearance, "field");
		field.setAttribute("id", (String) nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_ID));
		if (!StringUtils.isEmpty(setId)) {
			field.setAttribute("set", setId);
		}
		appendAtt(field, "label", fieldNodeRef, DesignerModel.PROP_DSG_LABEL);
		appendAtt(field, "label-id", fieldNodeRef, DesignerModel.PROP_DSG_LABELID);
		appendAtt(field, "description", fieldNodeRef, DesignerModel.PROP_DSG_DESCRIPTION);
		appendAtt(field, "description-id", fieldNodeRef, DesignerModel.PROP_DSG_DESCRIPTIONID);
		appendAtt(field, "help", fieldNodeRef, DesignerModel.PROP_DSG_HELP);
		appendAtt(field, "help-id", fieldNodeRef, DesignerModel.PROP_DSG_HELPID);
		field.setAttribute("read-only", nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_READONLY).toString());
		field.setAttribute("mandatory", nodeService.getProperty(fieldNodeRef, DesignerModel.PROP_DSG_MANDATORY).toString());

		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(fieldNodeRef);

		for (ChildAssociationRef assoc : assocs) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_CONTROLS)) {
				visitControlXml(assoc.getChildRef(), field);
			}
		}

	}

	private void visitSetXml(NodeRef setNodeRef, Element formEl, Element fieldVisibility, Element appearance, String parentSetId) {
		Element set = DOMUtils.createElement(appearance, "set");
		appendAtt(set, "id", setNodeRef, DesignerModel.PROP_DSG_ID);
		appendAtt(set, "template", setNodeRef, DesignerModel.PROP_DSG_TEMPLATEPATH);
		String setAppearance = (String) nodeService.getProperty(setNodeRef, DesignerModel.PROP_DSG_APPEARANCE);
		if (setAppearance != null && setAppearance.length()>0 && !setAppearance.equals("-")) {
			set.setAttribute("appearance", setAppearance);
		}
		appendAtt(set, "label", setNodeRef, DesignerModel.PROP_DSG_LABEL);
		appendAtt(set, "label-id", setNodeRef, DesignerModel.PROP_DSG_LABELID);
		if (!StringUtils.isEmpty(parentSetId)) {
			set.setAttribute("parent", parentSetId);
		}

		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(setNodeRef);

		for (ChildAssociationRef assoc : assocs) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_SETS)) {
				visitSetXml(assoc.getChildRef(), formEl, fieldVisibility, appearance, (String) nodeService.getProperty(setNodeRef, DesignerModel.PROP_DSG_ID));
			}
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_FIELDS)) {
				visitFieldXml(assoc.getChildRef(), formEl, fieldVisibility, appearance, (String) nodeService.getProperty(setNodeRef, DesignerModel.PROP_DSG_ID));
			}

		}

	}

	private void visitControlXml(NodeRef controlRef, Element field) {
		Element control = DOMUtils.createElement(field, "control");
		appendAtt(control, "template", controlRef, DesignerModel.PROP_DSG_TEMPLATEPATH);
		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(controlRef);

		for (ChildAssociationRef assoc : assocs) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_PARAMETERS)) {
				visitParameterXml(assoc.getChildRef(), control);
			}
		}
	}

	private void visitParameterXml(NodeRef parameterRef, Element control) {
		String value = (String) nodeService.getProperty(parameterRef, DesignerModel.PROP_DSG_PARAMETERVALUE);
		if (!StringUtils.isEmpty(value)) {
			Element controlParam = DOMUtils.createElement(control, "control-param");
			controlParam.setAttribute("name", (String) nodeService.getProperty(parameterRef, DesignerModel.PROP_DSG_ID));
			controlParam.setTextContent(value);
		}
	}

	/**
	 * 
	 * @param configNodeRef
	 * @param in
	 * @throws FactoryConfigurationError
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
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
	// <constraint type="id" validation-handler="function" [message-id="string"]
	// [message="string"] [event="string"] />
	// </constraint-handlers>
	// </field>
	// </appearance>
	// </form>
	// </forms>
	// </config>
	public void visitConfigNodeRef(NodeRef configNodeRef, InputStream in) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
		logger.debug("visitConfigNodeRef");
		if (in != null) {
			Document doc = DOMUtils.parse(in);
			NodeList list = doc.getElementsByTagName("config");
			for (int i = 0; i < list.getLength(); i++) {
				Element elem = (Element) list.item(i);
				ChildAssociationRef childAssociationRef = nodeService.createNode(configNodeRef, DesignerModel.ASSOC_DSG_CONFIG_ELEMENTS, DesignerModel.ASSOC_DSG_CONFIG_ELEMENTS,
						DesignerModel.TYPE_DSG_CONFIG_ELEMENT);
				NodeRef configElNodeRef = childAssociationRef.getChildRef();
				nodeService.setProperty(configElNodeRef, DesignerModel.PROP_DSG_CONFIGEVALUATOR, elem.getAttribute("evaluator"));
				nodeService.setProperty(configElNodeRef, DesignerModel.PROP_DSG_CONFIGREPLACE, elem.getAttribute("replace"));
				nodeService.setProperty(configElNodeRef, DesignerModel.PROP_DSG_ID, elem.getAttribute("condition"));
				nodeService.setProperty(configElNodeRef,BeCPGModel.PROP_SORT,i*100);
				visitFormElement(configElNodeRef, elem);
			}
		}

	}

	private void visitFormElement(NodeRef configElNodeRef, Element configEl) {
		logger.debug("visitFormElement");
		NodeList list = configEl.getElementsByTagName("form");
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);
			ChildAssociationRef childAssociationRef = nodeService.createNode(configElNodeRef, DesignerModel.ASSOC_DSG_FORMS, DesignerModel.ASSOC_DSG_FORMS,
					DesignerModel.TYPE_DSG_FORM);
			NodeRef formNodeRef = childAssociationRef.getChildRef();
			nodeService.setProperty(formNodeRef, DesignerModel.PROP_DSG_ID, elem.getAttribute("id"));
			nodeService.setProperty(formNodeRef, DesignerModel.PROP_DSG_SUBMISSION_URL, elem.getAttribute("submission-url"));
			nodeService.setProperty(formNodeRef,BeCPGModel.PROP_SORT,i*100);
			int sortOrder = visitFormSets(formNodeRef, elem, "");
			visitFormFields(formNodeRef, elem, "",sortOrder);
            visitFormTemplates(formNodeRef, elem);
		}

	}


	private int visitFormSets(NodeRef nodeRef, Element parentEl, String parent) {
		logger.debug("visitFormSets with parent : " + parent);
		NodeList list = parentEl.getElementsByTagName("set");
		int sortOrder = 0;
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);
			String parentId = elem.getAttribute("parent");
			if ((parentId != null && parentId.equals(parent)) || (parentId == null && parent == null)) {
				ChildAssociationRef childAssociationRef = nodeService.createNode(nodeRef, DesignerModel.ASSOC_DSG_SETS, DesignerModel.ASSOC_DSG_SETS,
						DesignerModel.TYPE_DSG_FORMSET);
				NodeRef setNodeRef = childAssociationRef.getChildRef();
				String setId = elem.getAttribute("id");
				nodeService.setProperty(setNodeRef, DesignerModel.PROP_DSG_ID, setId);
				String appearance = elem.getAttribute("appearance");
				nodeService.setProperty(setNodeRef, DesignerModel.PROP_DSG_APPEARANCE, (appearance != null && appearance.length() > 0) ? appearance : "");
				nodeService.setProperty(setNodeRef, DesignerModel.PROP_DSG_TEMPLATEPATH, elem.getAttribute("template"));
				nodeService.setProperty(setNodeRef, DesignerModel.PROP_DSG_LABEL, elem.getAttribute("label"));
				nodeService.setProperty(setNodeRef, DesignerModel.PROP_DSG_LABELID, elem.getAttribute("label-id"));
				nodeService.setProperty(setNodeRef,BeCPGModel.PROP_SORT,sortOrder);
				if (!Objects.equals(setId, parentId)) {
					visitFormSets(setNodeRef, parentEl, setId);
				}
				visitFormFields(setNodeRef, parentEl, setId, 0);
			}
			sortOrder = i*100;
		}
		return sortOrder;
	}

	private void visitFormFields(NodeRef nodeRef, Element parentEl, String setId, int sortOrder) {
		logger.debug("visitFormFields for set : " + setId);
		NodeList fields = parentEl.getElementsByTagName("field");
		NodeList hides = parentEl.getElementsByTagName("hide");
		NodeList shows = parentEl.getElementsByTagName("show");

		sortOrder  = visitFormFields(nodeRef, fields, shows, true, setId, sortOrder);

		visitFormFields(nodeRef, fields, hides, false, setId, sortOrder);

	}
	
	

	private void visitFormTemplates(NodeRef formNodeRef, Element parentEl) {
		NodeList editForms = parentEl.getElementsByTagName("edit-form");
		if(editForms!=null && editForms.getLength()>0){
			Element elem = (Element) editForms.item(0);
			nodeService.setProperty(formNodeRef, DesignerModel.PROP_DSG_EDITFORMTEMPLATE, elem.getAttribute("template"));
		}
		
		NodeList viewForms = parentEl.getElementsByTagName("view-form");
		if(viewForms!=null && viewForms.getLength()>0){
			Element elem = (Element) editForms.item(0);
			nodeService.setProperty(formNodeRef, DesignerModel.PROP_DSG_VIEWFORMTEMPLATE, elem.getAttribute("template"));
		}
		
		NodeList createForms = parentEl.getElementsByTagName("create-form");
		if(createForms!=null && createForms.getLength()>0){
			Element elem = (Element) editForms.item(0);
			nodeService.setProperty(formNodeRef, DesignerModel.PROP_DSG_CREATEFORMTEMPLATE, elem.getAttribute("template"));
		}
		
	}

	private int visitFormFields(NodeRef nodeRef, NodeList fields, NodeList shows, boolean show, String setId, int sortOrder) {

		for (int i = 0; i < shows.getLength(); i++) {

			
			Element elem = (Element) shows.item(i);
			String fieldId = elem.getAttribute("id");
			logger.debug("Try to add " + fieldId);
			boolean added = false;
			for (int j = 0; j < fields.getLength(); j++) {
				Element field = (Element) fields.item(j);
				logger.debug("Compare " + fieldId + "/" + field.getAttribute("id") + " set " + setId + "/" + field.getAttribute("set"));
				if (fieldId.equals(field.getAttribute("id"))) {
					added = true;
					if (setId.equals(field.getAttribute("set"))) {
						createField(nodeRef, elem, field, show, sortOrder);

						break;
					}
				}
			}
			if (Objects.equals(setId, "") && !added) {
				createField(nodeRef, elem, null, show, sortOrder);
			}
			sortOrder+=i*100;
		}
		return sortOrder;
	}

	private void createField(NodeRef parent, Element elem, Element field, boolean show, int sortOrder) {
		logger.debug("Create field with id :" + elem.getAttribute("id"));
		ChildAssociationRef childAssociationRef = nodeService.createNode(parent, DesignerModel.ASSOC_DSG_FIELDS, DesignerModel.ASSOC_DSG_FIELDS, DesignerModel.TYPE_DSG_FORMFIELD);
		NodeRef fieldNodeRef = childAssociationRef.getChildRef();
		nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_ID, elem.getAttribute("id"));
		nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_HIDE, !show);
		nodeService.setProperty(fieldNodeRef,BeCPGModel.PROP_SORT,sortOrder);
		
		if (!StringUtils.isEmpty(elem.getAttribute("for-mode"))) {
			nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_FORMODE, (Serializable) Arrays.asList(elem.getAttribute("for-mode").split(",")));
		}
		if (!StringUtils.isEmpty(elem.getAttribute("force"))) {
			nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_FORCE, elem.getAttribute("force"));
		}

		if (field != null) {
			nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_LABEL, field.getAttribute("label"));
			nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_LABELID, field.getAttribute("label-id"));
			nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_DESCRIPTION, field.getAttribute("description"));
			nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_DESCRIPTIONID, field.getAttribute("description-id"));
			nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_HELP, field.getAttribute("help"));
			nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_HELPID, field.getAttribute("help-id"));
			if (!StringUtils.isEmpty(field.getAttribute("read-only"))) {
				nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_READONLY, field.getAttribute("read-only"));
			}
			if (!StringUtils.isEmpty(field.getAttribute("mandatory"))) {
				nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_MANDATORY, field.getAttribute("mandatory"));
			}
			visitFormControl(fieldNodeRef, field);
		}

	}

	private void visitFormControl(NodeRef parentNodeRef, Element parentEl) {
		logger.debug("visitFormControl");
		NodeList list = parentEl.getElementsByTagName("control");
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);
			ChildAssociationRef childAssociationRef = nodeService.createNode(parentNodeRef, DesignerModel.ASSOC_DSG_CONTROLS, DesignerModel.ASSOC_DSG_CONTROLS,
					DesignerModel.TYPE_DSG_FORMCONTROL);
			NodeRef ret = childAssociationRef.getChildRef();
			nodeService.setProperty(ret, DesignerModel.PROP_DSG_TEMPLATEPATH, elem.getAttribute("template"));
			String[] splitted = elem.getAttribute("template").replace(".ftl", "").split("/");
			nodeService.setProperty(ret, DesignerModel.PROP_DSG_ID, splitted[splitted.length - 1]);
			NodeList params = elem.getElementsByTagName("control-param");
			for (int j = 0; j < params.getLength(); j++) {
				Element param = (Element) params.item(j);
				childAssociationRef = nodeService.createNode(ret, DesignerModel.ASSOC_DSG_PARAMETERS, DesignerModel.ASSOC_DSG_PARAMETERS, DesignerModel.TYPE_DSG_CONTROLPARAMETER);
				NodeRef paramRef = childAssociationRef.getChildRef();
				nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_ID, param.getAttribute("name"));
				nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_PARAMETERVALUE, param.getTextContent());

			}
		}
	}

	public NodeRef visitM2Type(NodeRef from, NodeRef to) {
		String typeName = (String) nodeService.getProperty(from, DesignerModel.PROP_M2_NAME);
		String parentName = (String) nodeService.getProperty(from, DesignerModel.PROP_M2_PARENT_NAME);
		NodeRef configElNodeRef;

		if ("bpm:activitiStartTask".equals(parentName) || "bpm:startTask".equals(parentName) || "bpm:workflowTask".equals(parentName)) {

			configElNodeRef = createForm("task-type", typeName, "-", from, to);
			ChildAssociationRef childAssociationRef = nodeService.createNode(configElNodeRef, DesignerModel.ASSOC_DSG_FORMS, DesignerModel.ASSOC_DSG_FORMS,
					DesignerModel.TYPE_DSG_FORM);
			NodeRef formNodeRef = childAssociationRef.getChildRef();
			nodeService.setProperty(formNodeRef, DesignerModel.PROP_DSG_ID, "workflow-details");

			visitM2Properties(formNodeRef, from);

		} else {

			createForm("node-type", typeName, "-", from, to);
			configElNodeRef = createForm("model-type", typeName, "-", from, to);

		}
		return configElNodeRef;

	}

	private NodeRef createForm(String evaluator, String condition, String formId, NodeRef from, NodeRef to) {
		// Create config elements
		ChildAssociationRef childAssociationRef = nodeService.createNode(to, DesignerModel.ASSOC_DSG_CONFIG_ELEMENTS, DesignerModel.ASSOC_DSG_CONFIG_ELEMENTS,
				DesignerModel.TYPE_DSG_CONFIG_ELEMENT);
		NodeRef configElNodeRef = childAssociationRef.getChildRef();
		nodeService.setProperty(configElNodeRef, DesignerModel.PROP_DSG_CONFIGEVALUATOR, evaluator);
		nodeService.setProperty(configElNodeRef, DesignerModel.PROP_DSG_ID, condition);

		childAssociationRef = nodeService.createNode(configElNodeRef, DesignerModel.ASSOC_DSG_FORMS, DesignerModel.ASSOC_DSG_FORMS, DesignerModel.TYPE_DSG_FORM);
		NodeRef formNodeRef = childAssociationRef.getChildRef();
		nodeService.setProperty(formNodeRef, DesignerModel.PROP_DSG_ID, formId);

		visitM2Properties(formNodeRef, from);
		return configElNodeRef;
	}

	public NodeRef visitM2Properties(NodeRef formNodeRef, NodeRef typeNodeRef) {

		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(typeNodeRef);

		for (ChildAssociationRef assoc : assocs) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_M2_PROPERTIES) || assoc.getTypeQName().equals(DesignerModel.ASSOC_M2_PROPERTY_OVERRIDES)
					|| assoc.getTypeQName().equals(DesignerModel.ASSOC_M2_ASSOCIATIONS)) {
				NodeRef propNodeRef = assoc.getChildRef();
				ChildAssociationRef childAssociationRef = nodeService.createNode(formNodeRef, DesignerModel.ASSOC_DSG_FIELDS, DesignerModel.ASSOC_DSG_FIELDS,
						DesignerModel.TYPE_DSG_FORMFIELD);
				NodeRef fieldNodeRef = childAssociationRef.getChildRef();
				nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_ID, nodeService.getProperty(propNodeRef, DesignerModel.PROP_M2_NAME));
				nodeService.setProperty(fieldNodeRef, DesignerModel.PROP_DSG_HIDE, false);
			}

		}
		return formNodeRef;
	}

}
