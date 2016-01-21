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
package fr.becpg.repo.designer;

import org.alfresco.service.namespace.QName;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
public class DesignerModel {

	public static final String M2_URI = "http://www.bcpg.fr/model/m2/1.0";

	public static final String DESIGNER_URI = "http://www.bcpg.fr/model/designer/1.0";

	/**
	 * Types
	 */

	public static final QName TYPE_M2_MODEL = QName.createQName(M2_URI, "model");

	public static final QName TYPE_M2_NAMESPACE = QName.createQName(M2_URI, "namespace");

	public static final QName TYPE_M2_TYPE = QName.createQName(M2_URI, "type");

	public static final QName TYPE_M2_ASPECT = QName.createQName(M2_URI, "aspect");

	public static final QName TYPE_M2_CHILD_ASSOCIATION = QName.createQName(M2_URI, "childAssociation");

	public static final QName TYPE_M2_ASSOCIATION = QName.createQName(M2_URI, "association");

	public static final QName TYPE_M2_PROPERTY = QName.createQName(M2_URI, "property");

	public static final QName TYPE_M2_PROPERTY_OVERRIDE = QName.createQName(M2_URI, "propertyOverride");

	public static final QName TYPE_M2_CONSTRAINT = QName.createQName(M2_URI, "constraint");

	public static final QName TYPE_DSG_FORM = QName.createQName(DESIGNER_URI, "form");

	public static final QName TYPE_DSG_FORMSET = QName.createQName(DESIGNER_URI, "formSet");

	public static final QName TYPE_DSG_FORMFIELD = QName.createQName(DESIGNER_URI, "formField");

	public static final QName TYPE_DSG_FORMCONTROL = QName.createQName(DESIGNER_URI, "formControl");

	public static final QName TYPE_DSG_CONTROLPARAMETER = QName.createQName(DESIGNER_URI, "controlParameter");

	public static final QName TYPE_DSG_CONFIG_ELEMENT = QName.createQName(DESIGNER_URI, "configElement");

	public static final QName TYPE_DSG_CONFIG = QName.createQName(DESIGNER_URI, "config");

	public static final QName TYPE_WORKFLOW_DEFINITION = QName.createQName(DESIGNER_URI, "workflowDefinition");

	/**
	 * Aspects
	 */

	public static final QName ASPECT_CONFIG = QName.createQName(DESIGNER_URI, "configAspect");

	public static final QName ASPECT_MODEL = QName.createQName(DESIGNER_URI, "modelAspect");

	public static final QName ASPECT_MODEL_ERROR = QName.createQName(DESIGNER_URI, "modelError");

	/**
	 * Props
	 */

	public static final QName PROP_DSG_ID = QName.createQName(DESIGNER_URI, "id");

	public static final QName PROP_M2_NAME = QName.createQName(M2_URI, "name");

	public static final QName PROP_M2_TITLE = QName.createQName(M2_URI, "title");
	public static final QName PROP_M2_DESCRIPTION = QName.createQName(M2_URI, "description");

	public static final QName PROP_M2_URI = QName.createQName(M2_URI, "uri");
	public static final QName PROP_M2_PREFIX = QName.createQName(M2_URI, "prefix");

	public static final QName PROP_M2_REF = QName.createQName(M2_URI, "ref");

	public static final QName PROP_M2_PARENT_NAME = QName.createQName(M2_URI, "parentName");

	public static final QName PROP_M2_INDEX_MODE = QName.createQName(M2_URI, "indexTokenisationMode");

	public static final QName PROP_M2_MANDATORYASPECTS = QName.createQName(M2_URI, "mandatoryAspects");

	public static final QName PROP_DSG_READ_ONLY_FILE = QName.createQName(DESIGNER_URI, "readOnlyFile");

	public static final QName PROP_DSG_APPEARANCE = QName.createQName(DESIGNER_URI, "appearance");

	public static final QName PROP_DSG_TEMPLATEPATH = QName.createQName(DESIGNER_URI, "templatePath");

	public static final QName PROP_DSG_OPTIONAL = QName.createQName(DESIGNER_URI, "optional");

	public static final QName PROP_DSG_PARAMETERTYPE = QName.createQName(DESIGNER_URI, "parameterType");

	public static final QName PROP_DSG_PARAMETERVALUE = QName.createQName(DESIGNER_URI, "parameterValue");

	public static final QName PROP_DSG_PARAMETERDESCRIPTION = QName.createQName(DESIGNER_URI, "parameterDescription");

	public static final QName PROP_DSG_CONFIGEVALUATOR = QName.createQName(DESIGNER_URI, "configEvaluator");

	public static final QName PROP_DSG_CONFIGREPLACE = QName.createQName(DESIGNER_URI, "configReplace");

	public static final QName PROP_DSG_SUBMISSION_URL = QName.createQName(DESIGNER_URI, "submissionUrl");
	
	public static final QName PROP_DSG_CREATEFORMTEMPLATE = QName.createQName(DESIGNER_URI, "createFormTemplate");
	
	public static final QName PROP_DSG_EDITFORMTEMPLATE = QName.createQName(DESIGNER_URI, "editFormTemplate");
	
	public static final QName PROP_DSG_VIEWFORMTEMPLATE = QName.createQName(DESIGNER_URI, "viewFormTemplate");

	public static final QName PROP_DSG_LABEL = QName.createQName(DESIGNER_URI, "label");

	public static final QName PROP_DSG_LABELID = QName.createQName(DESIGNER_URI, "labelId");

	public static final QName PROP_DSG_HIDE = QName.createQName(DESIGNER_URI, "hide");

	public static final QName PROP_DSG_FORMODE = QName.createQName(DESIGNER_URI, "forMode");

	public static final QName PROP_DSG_FORCE = QName.createQName(DESIGNER_URI, "force");

	public static final QName PROP_DSG_DESCRIPTION = QName.createQName(DESIGNER_URI, "description");

	public static final QName PROP_DSG_DESCRIPTIONID = QName.createQName(DESIGNER_URI, "descriptionId");

	public static final QName PROP_DSG_HELP = QName.createQName(DESIGNER_URI, "help");

	public static final QName PROP_DSG_HELPID = QName.createQName(DESIGNER_URI, "helpId");

	public static final QName PROP_DSG_READONLY = QName.createQName(DESIGNER_URI, "readOnly");

	public static final QName PROP_DSG_MANDATORY = QName.createQName(DESIGNER_URI, "mandatory");

	public static final QName PROP_WORKFLOW_DEFINITION_ACTIVE = QName.createQName(DESIGNER_URI, "workflowDefinitionActive");

	public static final QName PROP_WORKFLOW_DEFINITION_NAME = QName.createQName(DESIGNER_URI, "workflowDefinitionName");

	public static final QName PROP_WORKFLOW_DEFINITION_DESCRIPTION = QName.createQName(DESIGNER_URI, "workflowDefinitionDescription");

	public static final QName PROP_WORKFLOW_DEFINITION_PUBLISHED_DATE = QName.createQName(DESIGNER_URI, "workflowDefinitionPublishedDate");

	public static final QName PROP_WORKFLOW_DEFINITION_VERSION = QName.createQName(DESIGNER_URI, "workflowDefinitionVersion");

	/**
	 * Assoc
	 */

	public static final QName ASSOC_MODEL = QName.createQName(DESIGNER_URI, "model");

	public static final QName ASSOC_M2_NAMESPACES = QName.createQName(M2_URI, "namespaces");

	public static final QName ASSOC_M2_TYPES = QName.createQName(M2_URI, "types");

	public static final QName ASSOC_M2_IMPORTS = QName.createQName(M2_URI, "imports");

	public static final QName ASSOC_M2_ASPECTS = QName.createQName(M2_URI, "aspects");

	public static final QName ASSOC_M2_CONSTRAINTS = QName.createQName(M2_URI, "constraints");

	public static final QName ASSOC_M2_DATA_TYPE = QName.createQName(M2_URI, "dataTypes");

	public static final QName ASSOC_M2_PROPERTIES = QName.createQName(M2_URI, "properties");

	public static final QName ASSOC_M2_PROPERTY_OVERRIDES = QName.createQName(M2_URI, "propertyOverrides");

	public static final QName ASSOC_M2_ASSOCIATIONS = QName.createQName(M2_URI, "associations");

	public static final QName ASSOC_DSG_FIELDS = QName.createQName(DESIGNER_URI, "fields");

	public static final QName ASSOC_DSG_CONTROLS = QName.createQName(DESIGNER_URI, "controls");

	public static final QName ASSOC_DSG_SETS = QName.createQName(DESIGNER_URI, "sets");

	public static final QName ASSOC_DSG_PARAMETERS = QName.createQName(DESIGNER_URI, "parameters");

	public static final QName ASSOC_DSG_CONFIG_ELEMENTS = QName.createQName(DESIGNER_URI, "configElements");

	public static final QName ASSOC_DSG_FORMS = QName.createQName(DESIGNER_URI, "forms");

	public static final QName ASSOC_DSG_CONFIG = QName.createQName(DESIGNER_URI, "config");


}
