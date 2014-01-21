/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.repo.listvalue.impl.ListValueServiceImpl;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

public class ProductListValuePlugin extends EntityListValuePlugin {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ListValueServiceImpl.class);

	/** The Constant SUFFIX_ALL. */
	protected static final String SUFFIX_ALL = "*";

	/** The Constant SOURCE_TYPE_PRODUCT. */
	private static final String SOURCE_TYPE_PRODUCT = "product";


	/** The Constant SOURCE_TYPE_PRODUCT_REPORT. */
	private static final String SOURCE_TYPE_PRODUCT_REPORT = "productreport";

	protected static final String PARAM_VALUES_SEPARATOR = ",";

	/** The product report service. */
	private ReportTplService reportTplService;


	
	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}



	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_PRODUCT, SOURCE_TYPE_PRODUCT_REPORT};
	}

	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize,
			Map<String, Serializable> props) {

		String classNames = (String) props.get(ListValueService.PROP_CLASS_NAMES);
		String[] arrClassNames = classNames != null ? classNames.split(PARAM_VALUES_SEPARATOR) : null;
		String productType = (String) props.get(ListValueService.PROP_PRODUCT_TYPE);

		if (sourceType.equals(SOURCE_TYPE_PRODUCT)) {
			return suggestTargetAssoc(BeCPGModel.TYPE_PRODUCT, query, pageNum, pageSize, arrClassNames, props);
		} else if (sourceType.equals(SOURCE_TYPE_PRODUCT_REPORT)) {
			QName productTypeQName = QName.createQName(productType, namespaceService);
			return suggestProductReportTemplates(productTypeQName, query, pageNum, pageSize);

		}

		return null;
	}

	

	protected String prepareQueryCode(String query, QName type, String[] arrClassNames) {
		if (Pattern.matches(RepoConsts.REGEX_NON_NEGATIVE_INTEGER_FIELD, query)) {
			Long codeNumber = null;
			try {
				codeNumber = Long.parseLong(query);
			} catch (NumberFormatException e) {
				logger.debug(e, e);
			}

			if (codeNumber != null) {
				List<QName> types = new ArrayList<QName>();
				if (arrClassNames != null && arrClassNames.length > 0) {
					for (int i = 0; i < arrClassNames.length; i++) {
						types.add(QName.createQName(arrClassNames[i], namespaceService));
					}
				} else {
					types.add(type);
				}

				StringBuffer ret = new StringBuffer();
				for (QName typeTmp : types) {
					if (BeCPGModel.TYPE_PRODUCT.equals(typeTmp)) {
						for (QName subType : dictionaryService.getSubTypes(typeTmp, true)) {
							if (ret.length() > 0) {
								ret.append(" OR ");
							}
							ret.append(autoNumService.getPrefixedCode(subType, BeCPGModel.PROP_CODE, codeNumber));
						}
					} else {
						if (ret.length() > 0) {
							ret.append(" OR ");
						}
						ret.append(autoNumService.getPrefixedCode(typeTmp, BeCPGModel.PROP_CODE, codeNumber));
					}
				}
				return "(" + ret.toString() + ")";
			}
		}
		return query;
	}

	/**
	 * Get the report templates of the product type that user can choose from
	 * UI.
	 * 
	 * @param query
	 *            the query
	 * @return the map
	 */

	private ListValuePage suggestProductReportTemplates(QName nodeType, String query, Integer pageNum, Integer pageSize) {

		query = prepareQuery(query);
		List<NodeRef> tplsNodeRef = reportTplService.suggestUserReportTemplates(ReportType.Document, nodeType, query);

		return new ListValuePage(tplsNodeRef, pageNum, pageSize, new NodeRefListValueExtractor(ContentModel.PROP_NAME,
				nodeService));
	}

}
