/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public class ReportModel {

	public static final String REPORT_URI = "http://www.bcpg.fr/model/report/1.0";

	public static final String REPORT_PREFIX = "rep";

	public static final QName MODEL = QName.createQName(REPORT_URI, "repmodel");
	
	// reportType
	public static final QName TYPE_REPORT_TPL = QName.createQName(REPORT_URI,
	"reportTpl");
	public static final QName TYPE_REPORT = QName.createQName(REPORT_URI,
	"report");
	
	public static final QName PROP_REPORT_TPL_TYPE = QName.createQName(REPORT_URI,
	"reportTplType");
	public static final QName PROP_REPORT_TPL_CLASS_NAME = QName.createQName(REPORT_URI,
	"reportTplClassName");
	public static final QName PROP_REPORT_TPL_IS_SYSTEM = QName.createQName(REPORT_URI,
	"reportTplIsSystem");
	public static final QName PROP_REPORT_TPL_IS_DEFAULT = QName.createQName(REPORT_URI,
	"reportTplIsDefault");
	public static final QName PROP_REPORT_TPL_FORMAT = QName.createQName(REPORT_URI,
	"reportTplFormat");
	public static final QName PROP_REPORT_TPL_IS_DISABLED = QName.createQName(REPORT_URI,
			"reportTplIsDisabled");
	
	public static final QName PROP_REPORT_TPL_ITERATION_PARAMETER = QName.createQName(REPORT_URI,
			"reportTplIterationParameter");
	
	public static final QName PROP_REPORT_TEXT_PARAMETERS = QName.createQName(REPORT_URI,
			"reportTextParameters");
	
	public static final QName PROP_REPORT_PARAMETERS = QName.createQName(REPORT_URI,
			"reportParameters");

		
	// reportEntity
	public static final QName ASPECT_REPORT_ENTITY = QName.createQName(REPORT_URI,
			"reportEntityAspect");
	public static final QName PROP_REPORT_ENTITY_GENERATED = QName.createQName(REPORT_URI,
			"reportEntityGenerated");
	public static final QName ASSOC_REPORTS = QName.createQName(REPORT_URI,
			"reports");
	
	public static final QName ASSOC_REPORT_TPL = QName.createQName(REPORT_URI,
			"reportTplAssoc");
	
	public static final QName ASSOC_REPORT_ASSOCIATED_TPL_FILES= QName.createQName(REPORT_URI,
			"reportTplAssociatedFiles");
	
	public static final QName PROP_REPORT_LOCALES= QName.createQName(REPORT_URI,
				"reportLocales");
	

	public static final QName ASPECT_REPORT_LOCALES =QName.createQName(REPORT_URI,"reportLocalesAspect");
	
	// reportTpls
	public static final QName ASPECT_REPORT_TEMPLATES = QName.createQName(REPORT_URI,
			"reportTplsAspect");
	public static final QName ASSOC_REPORT_TEMPLATES = QName.createQName(REPORT_URI,
			"reportTpls");

	public static final QName PROP_REPORT_IS_DEFAULT =  QName.createQName(REPORT_URI,
	"reportIsDefault");;

	
	
}
