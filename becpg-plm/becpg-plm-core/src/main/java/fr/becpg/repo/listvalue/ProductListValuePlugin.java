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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service
public class ProductListValuePlugin extends EntityListValuePlugin {

	private static final String SOURCE_TYPE_PRODUCT = "product";

	private static final String SOURCE_TYPE_PRODUCT_REPORT = "productreport";

	private final static Log logger = LogFactory.getLog(ProductListValuePlugin.class);

	@Autowired
	private ReportTplService reportTplService;

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_PRODUCT, SOURCE_TYPE_PRODUCT_REPORT };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String classNames = (String) props.get(ListValueService.PROP_CLASS_NAMES);
		String[] arrClassNames = classNames != null ? classNames.split(PARAM_VALUES_SEPARATOR) : null;

		if (sourceType.equals(SOURCE_TYPE_PRODUCT)) {
			return suggestProducts(query, pageNum, pageSize, arrClassNames, props);
		} else if (sourceType.equals(SOURCE_TYPE_PRODUCT_REPORT)) {
			String productType = (String) props.get(ListValueService.PROP_PRODUCT_TYPE);

			QName productTypeQName = QName.createQName(productType, namespaceService);
			return suggestProductReportTemplates(productTypeQName, query, pageNum, pageSize);

		}

		return null;
	}

	private ListValuePage suggestProducts(String query, Integer pageNum, Integer pageSize, String[] arrClassNames, Map<String, Serializable> props) {
		if (logger.isDebugEnabled()) {
			if (arrClassNames != null) {
				logger.debug("suggestTargetAssoc with arrClassNames : " + Arrays.toString(arrClassNames));
			}
		}

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_PRODUCT).excludeDefaults()
				.inSearchTemplate(searchTemplate).locale(I18NUtil.getContentLocale()).andOperator().ftsLanguage();

		StringBuilder ftsQuery = new StringBuilder();

		if (!isAllQuery(query)) {
			if (query.length() > 2) {
				ftsQuery.append(prepareQuery(query.trim()));
				ftsQuery.append(" ");
			}
			ftsQuery.append(query);

			ftsQuery.append(")^10 AND +(");
		}

		ftsQuery.append("@");
		ftsQuery.append(PLMModel.PROP_PRODUCT_STATE.toString());
		ftsQuery.append(":");
		ftsQuery.append(SystemState.Valid.toString());
		ftsQuery.append("^4 or @");
		ftsQuery.append(PLMModel.PROP_PRODUCT_STATE.toString());
		ftsQuery.append(":");
		ftsQuery.append(SystemState.ToValidate.toString());
		ftsQuery.append("^2 or @");
		ftsQuery.append(PLMModel.PROP_PRODUCT_STATE.toString());
		ftsQuery.append(":");
		ftsQuery.append(SystemState.Simulation.toString());

		queryBuilder.andFTSQuery(ftsQuery.toString());

		String queryFilter = (String) props.get(ListValueService.PROP_FILTER);

		
		if ((queryFilter != null) && (!queryFilter.isEmpty())) {
			String[] splitted = queryFilter.split("\\|");

			String filterValue = splitted[1];
			if ((filterValue != null) && !filterValue.isEmpty()) {
				if (filterValue.contains("{")) {
					NodeRef entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_NODEREF));
					if (entityNodeRef != null) {
						filterValue = extractExpr(entityNodeRef, filterValue);
					}
				}
				if ((filterValue != null) && !filterValue.isEmpty()) {
					queryBuilder.andPropEquals(QName.createQName(splitted[0], namespaceService), filterValue);
				}
			}
		}

		// filter by classNames
		filterByClass(queryBuilder, arrClassNames);

		queryBuilder.maxResults(RepoConsts.MAX_SUGGESTIONS);

		return new ListValuePage(queryBuilder.list(), pageNum, pageSize, targetAssocValueExtractor);
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

		return new ListValuePage(tplsNodeRef, pageNum, pageSize, new NodeRefListValueExtractor(ContentModel.PROP_NAME, nodeService));
	}

}
