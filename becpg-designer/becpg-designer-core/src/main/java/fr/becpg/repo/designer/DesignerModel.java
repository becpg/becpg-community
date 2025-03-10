/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
 * <p>DesignerModel class.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public class DesignerModel {

	/** Constant <code>M2_URI="http://www.bcpg.fr/model/m2/1.0"</code> */
	public static final String M2_URI = "http://www.bcpg.fr/model/m2/1.0";

	/** Constant <code>DESIGNER_URI="http://www.bcpg.fr/model/designer/1.0"</code> */
	public static final String DESIGNER_URI = "http://www.bcpg.fr/model/designer/1.0";

	/**
	 * Types
	 */

	public static final QName TYPE_M2_MODEL = QName.createQName(M2_URI, "model");

	/** Constant <code>TYPE_M2_NAMESPACE</code> */
	public static final QName TYPE_M2_NAMESPACE = QName.createQName(M2_URI, "namespace");

	/** Constant <code>TYPE_M2_TYPE</code> */
	public static final QName TYPE_M2_TYPE = QName.createQName(M2_URI, "type");

	/** Constant <code>TYPE_M2_ASPECT</code> */
	public static final QName TYPE_M2_ASPECT = QName.createQName(M2_URI, "aspect");

	/** Constant <code>TYPE_M2_CHILD_ASSOCIATION</code> */
	public static final QName TYPE_M2_CHILD_ASSOCIATION = QName.createQName(M2_URI, "childAssociation");

	/** Constant <code>TYPE_M2_ASSOCIATION</code> */
	public static final QName TYPE_M2_ASSOCIATION = QName.createQName(M2_URI, "association");

	/** Constant <code>TYPE_M2_PROPERTY</code> */
	public static final QName TYPE_M2_PROPERTY = QName.createQName(M2_URI, "property");

	/** Constant <code>TYPE_M2_PROPERTY_OVERRIDE</code> */
	public static final QName TYPE_M2_PROPERTY_OVERRIDE = QName.createQName(M2_URI, "propertyOverride");

	/** Constant <code>TYPE_M2_CONSTRAINT</code> */
	public static final QName TYPE_M2_CONSTRAINT = QName.createQName(M2_URI, "constraint");
	
	/** Constant <code>TYPE_DSG_FORM</code> */
	public static final QName TYPE_DSG_FORM = QName.createQName(DESIGNER_URI, "form");

	/** Constant <code>TYPE_DSG_FORMSET</code> */
	public static final QName TYPE_DSG_FORMSET = QName.createQName(DESIGNER_URI, "formSet");

	/** Constant <code>TYPE_DSG_FORMFIELD</code> */
	public static final QName TYPE_DSG_FORMFIELD = QName.createQName(DESIGNER_URI, "formField");

	/** Constant <code>TYPE_DSG_FORMCONTROL</code> */
	public static final QName TYPE_DSG_FORMCONTROL = QName.createQName(DESIGNER_URI, "formControl");

	/** Constant <code>TYPE_DSG_CONTROLPARAMETER</code> */
	public static final QName TYPE_DSG_CONTROLPARAMETER = QName.createQName(DESIGNER_URI, "controlParameter");

	/** Constant <code>TYPE_DSG_CONFIG_ELEMENT</code> */
	public static final QName TYPE_DSG_CONFIG_ELEMENT = QName.createQName(DESIGNER_URI, "configElement");

	/** Constant <code>TYPE_DSG_CONFIG</code> */
	public static final QName TYPE_DSG_CONFIG = QName.createQName(DESIGNER_URI, "config");

	/** Constant <code>TYPE_WORKFLOW_DEFINITION</code> */
	public static final QName TYPE_WORKFLOW_DEFINITION = QName.createQName(DESIGNER_URI, "workflowDefinition");

	/** Constant <code>TYPE_DSG_FORMCONSTRAINT</code> */
	public static final QName TYPE_DSG_FORMCONSTRAINT = QName.createQName(DESIGNER_URI, "formConstraint");

	/** Constant <code>TYPE_DSG_CONSTRAINTPARAMETER</code> */
	public static final QName TYPE_DSG_CONSTRAINTPARAMETER = QName.createQName(DESIGNER_URI, "constraintParameter");
	/**
	 * Aspects
	 */

	public static final QName ASPECT_CONFIG = QName.createQName(DESIGNER_URI, "configAspect");

	/** Constant <code>PROP_PUBLISHED_CONFIG_NAME</code> */
	public static final QName PROP_PUBLISHED_CONFIG_NAME = QName.createQName(DESIGNER_URI, "publishedConfigName");

	/** Constant <code>ASPECT_MODEL</code> */
	public static final QName ASPECT_MODEL = QName.createQName(DESIGNER_URI, "modelAspect");

	/** Constant <code>ASPECT_MODEL_ERROR</code> */
	public static final QName ASPECT_MODEL_ERROR = QName.createQName(DESIGNER_URI, "modelError");

	/**
	 * Props
	 */

	public static final QName PROP_DSG_ID = QName.createQName(DESIGNER_URI, "id");

	/** Constant <code>PROP_M2_NAME</code> */
	public static final QName PROP_M2_NAME = QName.createQName(M2_URI, "name");

	/** Constant <code>PROP_M2_TITLE</code> */
	public static final QName PROP_M2_TITLE = QName.createQName(M2_URI, "title");
	/** Constant <code>PROP_M2_DESCRIPTION</code> */
	public static final QName PROP_M2_DESCRIPTION = QName.createQName(M2_URI, "description");

	/** Constant <code>PROP_M2_URI</code> */
	public static final QName PROP_M2_URI = QName.createQName(M2_URI, "uri");
	/** Constant <code>PROP_M2_PREFIX</code> */
	public static final QName PROP_M2_PREFIX = QName.createQName(M2_URI, "prefix");

	/** Constant <code>PROP_M2_REF</code> */
	public static final QName PROP_M2_REF = QName.createQName(M2_URI, "ref");

	/** Constant <code>PROP_M2_PARENT_NAME</code> */
	public static final QName PROP_M2_PARENT_NAME = QName.createQName(M2_URI, "parentName");

	/** Constant <code>PROP_M2_INDEX_MODE</code> */
	public static final QName PROP_M2_INDEX_MODE = QName.createQName(M2_URI, "indexTokenisationMode");

	/** Constant <code>PROP_M2_MANDATORYASPECTS</code> */
	public static final QName PROP_M2_MANDATORYASPECTS = QName.createQName(M2_URI, "mandatoryAspects");

	/** Constant <code>PROP_DSG_READ_ONLY_FILE</code> */
	public static final QName PROP_DSG_READ_ONLY_FILE = QName.createQName(DESIGNER_URI, "readOnlyFile");

	/** Constant <code>PROP_DSG_APPEARANCE</code> */
	public static final QName PROP_DSG_APPEARANCE = QName.createQName(DESIGNER_URI, "appearance");

	/** Constant <code>PROP_DSG_TEMPLATEPATH</code> */
	public static final QName PROP_DSG_TEMPLATEPATH = QName.createQName(DESIGNER_URI, "templatePath");

	/** Constant <code>PROP_DSG_OPTIONAL</code> */
	public static final QName PROP_DSG_OPTIONAL = QName.createQName(DESIGNER_URI, "optional");

	/** Constant <code>PROP_DSG_PARAMETERTYPE</code> */
	public static final QName PROP_DSG_PARAMETERTYPE = QName.createQName(DESIGNER_URI, "parameterType");

	/** Constant <code>PROP_DSG_PARAMETERVALUE</code> */
	public static final QName PROP_DSG_PARAMETERVALUE = QName.createQName(DESIGNER_URI, "parameterValue");

	/** Constant <code>PROP_DSG_PARAMETERDESCRIPTION</code> */
	public static final QName PROP_DSG_PARAMETERDESCRIPTION = QName.createQName(DESIGNER_URI, "parameterDescription");

	/** Constant <code>PROP_DSG_CONFIGEVALUATOR</code> */
	public static final QName PROP_DSG_CONFIGEVALUATOR = QName.createQName(DESIGNER_URI, "configEvaluator");

	/** Constant <code>PROP_DSG_CONFIGREPLACE</code> */
	public static final QName PROP_DSG_CONFIGREPLACE = QName.createQName(DESIGNER_URI, "configReplace");

	/** Constant <code>PROP_DSG_SUBMISSION_URL</code> */
	public static final QName PROP_DSG_SUBMISSION_URL = QName.createQName(DESIGNER_URI, "submissionUrl");
	
	/** Constant <code>PROP_DSG_CREATEFORMTEMPLATE</code> */
	public static final QName PROP_DSG_CREATEFORMTEMPLATE = QName.createQName(DESIGNER_URI, "createFormTemplate");
	
	/** Constant <code>PROP_DSG_EDITFORMTEMPLATE</code> */
	public static final QName PROP_DSG_EDITFORMTEMPLATE = QName.createQName(DESIGNER_URI, "editFormTemplate");
	
	/** Constant <code>PROP_DSG_VIEWFORMTEMPLATE</code> */
	public static final QName PROP_DSG_VIEWFORMTEMPLATE = QName.createQName(DESIGNER_URI, "viewFormTemplate");

	/** Constant <code>PROP_DSG_LABEL</code> */
	public static final QName PROP_DSG_LABEL = QName.createQName(DESIGNER_URI, "label");

	/** Constant <code>PROP_DSG_LABELID</code> */
	public static final QName PROP_DSG_LABELID = QName.createQName(DESIGNER_URI, "labelId");

	/** Constant <code>PROP_DSG_HIDE</code> */
	public static final QName PROP_DSG_HIDE = QName.createQName(DESIGNER_URI, "hide");

	/** Constant <code>PROP_DSG_FORMODE</code> */
	public static final QName PROP_DSG_FORMODE = QName.createQName(DESIGNER_URI, "forMode");

	/** Constant <code>PROP_DSG_FORCE</code> */
	public static final QName PROP_DSG_FORCE = QName.createQName(DESIGNER_URI, "force");

	/** Constant <code>PROP_DSG_DESCRIPTION</code> */
	public static final QName PROP_DSG_DESCRIPTION = QName.createQName(DESIGNER_URI, "description");

	/** Constant <code>PROP_DSG_DESCRIPTIONID</code> */
	public static final QName PROP_DSG_DESCRIPTIONID = QName.createQName(DESIGNER_URI, "descriptionId");

	/** Constant <code>PROP_DSG_HELP</code> */
	public static final QName PROP_DSG_HELP = QName.createQName(DESIGNER_URI, "help");

	/** Constant <code>PROP_DSG_HELPID</code> */
	public static final QName PROP_DSG_HELPID = QName.createQName(DESIGNER_URI, "helpId");

	/** Constant <code>PROP_DSG_READONLY</code> */
	public static final QName PROP_DSG_READONLY = QName.createQName(DESIGNER_URI, "readOnly");

	/** Constant <code>PROP_DSG_MANDATORY</code> */
	public static final QName PROP_DSG_MANDATORY = QName.createQName(DESIGNER_URI, "mandatory");
	
	/** Constant <code>PROP_DSG_VALIDATION_HANDLER</code> */
	public static final QName PROP_DSG_VALIDATION_HANDLER = QName.createQName(DESIGNER_URI, "validation-handler");
	
	/** Constant <code>PROP_DSG_TYPE</code> */
	public static final QName PROP_DSG_TYPE = QName.createQName(DESIGNER_URI, "type");
	
	/** Constant <code>PROP_DSG_EVENT</code> */
	public static final QName PROP_DSG_EVENT = QName.createQName(DESIGNER_URI, "event");

	/** Constant <code>PROP_WORKFLOW_DEFINITION_ACTIVE</code> */
	public static final QName PROP_WORKFLOW_DEFINITION_ACTIVE = QName.createQName(DESIGNER_URI, "workflowDefinitionActive");

	/** Constant <code>PROP_WORKFLOW_DEFINITION_NAME</code> */
	public static final QName PROP_WORKFLOW_DEFINITION_NAME = QName.createQName(DESIGNER_URI, "workflowDefinitionName");

	/** Constant <code>PROP_WORKFLOW_DEFINITION_DESCRIPTION</code> */
	public static final QName PROP_WORKFLOW_DEFINITION_DESCRIPTION = QName.createQName(DESIGNER_URI, "workflowDefinitionDescription");

	/** Constant <code>PROP_WORKFLOW_DEFINITION_PUBLISHED_DATE</code> */
	public static final QName PROP_WORKFLOW_DEFINITION_PUBLISHED_DATE = QName.createQName(DESIGNER_URI, "workflowDefinitionPublishedDate");

	/** Constant <code>PROP_WORKFLOW_DEFINITION_VERSION</code> */
	public static final QName PROP_WORKFLOW_DEFINITION_VERSION = QName.createQName(DESIGNER_URI, "workflowDefinitionVersion");

	/**
	 * Assoc
	 */

	public static final QName ASSOC_MODEL = QName.createQName(DESIGNER_URI, "model");

	/** Constant <code>ASSOC_M2_NAMESPACES</code> */
	public static final QName ASSOC_M2_NAMESPACES = QName.createQName(M2_URI, "namespaces");

	/** Constant <code>ASSOC_M2_TYPES</code> */
	public static final QName ASSOC_M2_TYPES = QName.createQName(M2_URI, "types");

	/** Constant <code>ASSOC_M2_IMPORTS</code> */
	public static final QName ASSOC_M2_IMPORTS = QName.createQName(M2_URI, "imports");

	/** Constant <code>ASSOC_M2_ASPECTS</code> */
	public static final QName ASSOC_M2_ASPECTS = QName.createQName(M2_URI, "aspects");

	/** Constant <code>ASSOC_M2_CONSTRAINTS</code> */
	public static final QName ASSOC_M2_CONSTRAINTS = QName.createQName(M2_URI, "constraints");

	/** Constant <code>ASSOC_M2_DATA_TYPE</code> */
	public static final QName ASSOC_M2_DATA_TYPE = QName.createQName(M2_URI, "dataTypes");

	/** Constant <code>ASSOC_M2_PROPERTIES</code> */
	public static final QName ASSOC_M2_PROPERTIES = QName.createQName(M2_URI, "properties");

	/** Constant <code>ASSOC_M2_PROPERTY_OVERRIDES</code> */
	public static final QName ASSOC_M2_PROPERTY_OVERRIDES = QName.createQName(M2_URI, "propertyOverrides");

	/** Constant <code>ASSOC_M2_ASSOCIATIONS</code> */
	public static final QName ASSOC_M2_ASSOCIATIONS = QName.createQName(M2_URI, "associations");

	/** Constant <code>ASSOC_DSG_FIELDS</code> */
	public static final QName ASSOC_DSG_FIELDS = QName.createQName(DESIGNER_URI, "fields");

	/** Constant <code>ASSOC_DSG_CONTROLS</code> */
	public static final QName ASSOC_DSG_CONTROLS = QName.createQName(DESIGNER_URI, "controls");

	/** Constant <code>ASSOC_DSG_SETS</code> */
	public static final QName ASSOC_DSG_SETS = QName.createQName(DESIGNER_URI, "sets");

	/** Constant <code>ASSOC_DSG_PARAMETERS</code> */
	public static final QName ASSOC_DSG_PARAMETERS = QName.createQName(DESIGNER_URI, "parameters");

	/** Constant <code>ASSOC_DSG_CONFIG_ELEMENTS</code> */
	public static final QName ASSOC_DSG_CONFIG_ELEMENTS = QName.createQName(DESIGNER_URI, "configElements");

	/** Constant <code>ASSOC_DSG_FORMS</code> */
	public static final QName ASSOC_DSG_FORMS = QName.createQName(DESIGNER_URI, "forms");

	/** Constant <code>ASSOC_DSG_CONFIG</code> */
	public static final QName ASSOC_DSG_CONFIG = QName.createQName(DESIGNER_URI, "config");
	
	/** Constant <code>ASSOC_DSG_CONSTRAINTS</code> */
	public static final QName ASSOC_DSG_CONSTRAINTS = QName.createQName(DESIGNER_URI, "constraints");
	
	/** Constant <code>ASSOC_DSG_CONSTRAINT_ASSOC</code> */
	public static final QName ASSOC_DSG_CONSTRAINT_ASSOC = QName.createQName(DESIGNER_URI, "constraintAssoc");


}
