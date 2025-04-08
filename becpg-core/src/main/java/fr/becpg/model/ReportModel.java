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
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * <p>ReportModel class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ReportModel {
	
	private ReportModel() {
		//Do Nothing
	}

	/** Constant <code>REPORT_URI="http://www.bcpg.fr/model/report/1.0"</code> */
	public static final String REPORT_URI = "http://www.bcpg.fr/model/report/1.0";

	/** Constant <code>REPORT_PREFIX="rep"</code> */
	public static final String REPORT_PREFIX = "rep";

	/** Constant <code>MODEL</code> */
	public static final QName MODEL = QName.createQName(REPORT_URI, "repmodel");

	// reportType
	/** Constant <code>TYPE_REPORT_TPL</code> */
	public static final QName TYPE_REPORT_TPL = QName.createQName(REPORT_URI, "reportTpl");
	/** Constant <code>TYPE_REPORT</code> */
	public static final QName TYPE_REPORT = QName.createQName(REPORT_URI, "report");

	/** Constant <code>PROP_REPORT_TPL_TYPE</code> */
	public static final QName PROP_REPORT_TPL_TYPE = QName.createQName(REPORT_URI, "reportTplType");
	/** Constant <code>PROP_REPORT_TPL_CLASS_NAME</code> */
	public static final QName PROP_REPORT_TPL_CLASS_NAME = QName.createQName(REPORT_URI, "reportTplClassName");
	/** Constant <code>PROP_REPORT_TPL_IS_SYSTEM</code> */
	public static final QName PROP_REPORT_TPL_IS_SYSTEM = QName.createQName(REPORT_URI, "reportTplIsSystem");
	/** Constant <code>PROP_REPORT_TPL_IS_DEFAULT</code> */
	public static final QName PROP_REPORT_TPL_IS_DEFAULT = QName.createQName(REPORT_URI, "reportTplIsDefault");
	/** Constant <code>PROP_REPORT_TPL_FORMAT</code> */
	public static final QName PROP_REPORT_TPL_FORMAT = QName.createQName(REPORT_URI, "reportTplFormat");
	/** Constant <code>PROP_REPORT_TPL_IS_DISABLED</code> */
	public static final QName PROP_REPORT_TPL_IS_DISABLED = QName.createQName(REPORT_URI, "reportTplIsDisabled");

	/** Constant <code>PROP_REPORT_TPL_ITERATION_PARAMETER</code> */
	public static final QName PROP_REPORT_TPL_ITERATION_PARAMETER = QName.createQName(REPORT_URI, "reportTplIterationParameter");

	/** Constant <code>PROP_REPORT_TEXT_PARAMETERS</code> */
	public static final QName PROP_REPORT_TEXT_PARAMETERS = QName.createQName(REPORT_URI, "reportTextParameters");

	/** Constant <code>PROP_REPORT_PARAMETERS</code> */
	public static final QName PROP_REPORT_PARAMETERS = QName.createQName(REPORT_URI, "reportParameters");

	/** Constant <code>PROP_REPORT_TPL_SEARCH_QUERY</code> */
	public static final QName PROP_REPORT_TPL_SEARCH_QUERY = QName.createQName(REPORT_URI, "reportTplSearchQuery");

	/** Constant <code>PROP_REPORT_TPL_SEARCH_LIMIT</code> */
	public static final QName PROP_REPORT_TPL_SEARCH_LIMIT = QName.createQName(REPORT_URI, "reportTplSearchLimit");

	// reportEntity
	/** Constant <code>ASPECT_REPORT_ENTITY</code> */
	public static final QName ASPECT_REPORT_ENTITY = QName.createQName(REPORT_URI, "reportEntityAspect");
	/** Constant <code>PROP_REPORT_ENTITY_GENERATED</code> */
	public static final QName PROP_REPORT_ENTITY_GENERATED = QName.createQName(REPORT_URI, "reportEntityGenerated");
	/** Constant <code>ASSOC_REPORTS</code> */
	public static final QName ASSOC_REPORTS = QName.createQName(REPORT_URI, "reports");

	/** Constant <code>ASSOC_REPORT_TPL</code> */
	public static final QName ASSOC_REPORT_TPL = QName.createQName(REPORT_URI, "reportTplAssoc");

	/** Constant <code>ASSOC_REPORT_ASSOCIATED_TPL_FILES</code> */
	public static final QName ASSOC_REPORT_ASSOCIATED_TPL_FILES = QName.createQName(REPORT_URI, "reportTplAssociatedFiles");

	/** Constant <code>PROP_REPORT_LOCALES</code> */
	public static final QName PROP_REPORT_LOCALES = QName.createQName(REPORT_URI, "reportLocales");

	/** Constant <code>ASPECT_REPORT_LOCALES</code> */
	public static final QName ASPECT_REPORT_LOCALES = QName.createQName(REPORT_URI, "reportLocalesAspect");

	// reportKind
	/** Constant <code>ASPECT_REPORT_KIND</code> */
	public static final QName ASPECT_REPORT_KIND = QName.createQName(REPORT_URI, "reportKindAspect");

	/** Constant <code>PROP_REPORT_KINDS</code> */
	public static final QName PROP_REPORT_KINDS = QName.createQName(REPORT_URI, "reportKinds");

	/** Constant <code>PROP_REPORT_KINDS_CODE</code> */
	public static final QName PROP_REPORT_KINDS_CODE = QName.createQName(REPORT_URI, "reportKindsCode");

	// reportTpls
	/** Constant <code>ASPECT_REPORT_TEMPLATES</code> */
	public static final QName ASPECT_REPORT_TEMPLATES = QName.createQName(REPORT_URI, "reportTplsAspect");
	/** Constant <code>ASSOC_REPORT_TEMPLATES</code> */
	public static final QName ASSOC_REPORT_TEMPLATES = QName.createQName(REPORT_URI, "reportTpls");

	/** Constant <code>PROP_REPORT_IS_DEFAULT</code> */
	public static final QName PROP_REPORT_IS_DEFAULT = QName.createQName(REPORT_URI, "reportIsDefault");

	/** Constant <code>PROP_REPORT_IS_DIRTY</code> */
	public static final QName PROP_REPORT_IS_DIRTY = QName.createQName(REPORT_URI, "reportIsDirty");

}
