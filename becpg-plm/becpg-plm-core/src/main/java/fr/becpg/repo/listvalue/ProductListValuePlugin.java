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
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

@Service
public class ProductListValuePlugin extends EntityListValuePlugin {

	private static final String SOURCE_TYPE_PRODUCT = "product";

	private static final String SOURCE_TYPE_PRODUCT_REPORT = "productreport";

	@Autowired
	private ReportTplService reportTplService;

	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_PRODUCT, SOURCE_TYPE_PRODUCT_REPORT};
	}

	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize,
			Map<String, Serializable> props) {

		String classNames = (String) props.get(ListValueService.PROP_CLASS_NAMES);
		String[] arrClassNames = classNames != null ? classNames.split(PARAM_VALUES_SEPARATOR) : null;
		String productType = (String) props.get(ListValueService.PROP_PRODUCT_TYPE);

		if (sourceType.equals(SOURCE_TYPE_PRODUCT)) {
			return suggestTargetAssoc(PLMModel.TYPE_PRODUCT, query, pageNum, pageSize, arrClassNames, props);
		} else if (sourceType.equals(SOURCE_TYPE_PRODUCT_REPORT)) {
			QName productTypeQName = QName.createQName(productType, namespaceService);
			return suggestProductReportTemplates(productTypeQName, query, pageNum, pageSize);

		}

		return null;
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
		List<NodeRef> tplsNodeRef = reportTplService.getUserReportTemplates(ReportType.Document, nodeType, query);

		return new ListValuePage(tplsNodeRef, pageNum, pageSize, new NodeRefListValueExtractor(ContentModel.PROP_NAME,
				nodeService));
	}

}
