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
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>ProductListValuePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ProductListValuePlugin extends EntityListValuePlugin {

	private static final String SOURCE_TYPE_PRODUCT = "product";

	private static final String SOURCE_TYPE_PRODUCT_REPORT = "productreport";

	private static final Log logger = LogFactory.getLog(ProductListValuePlugin.class);

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private AssociationService associationService;

	@Value("${beCPG.product.searchTemplate}")
	private String productSearchTemplate = "%(cm:name  bcpg:erpCode bcpg:code bcpg:legalName)";

	@Autowired
	private ReportTplService reportTplService;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_PRODUCT, SOURCE_TYPE_PRODUCT_REPORT };
	}

	/** {@inheritDoc} */
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
				.inSearchTemplate(productSearchTemplate).locale(I18NUtil.getContentLocale()).andOperator().ftsLanguage();

		StringBuilder ftsQuery = new StringBuilder();

		if (!isAllQuery(query)) {
			if (query.length() > 2) {
				ftsQuery.append("(" + prepareQuery(query.trim()) + ") OR ");
			}
			ftsQuery.append("(" + query + ")");

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

		NodeRef entityNodeRef = null;
		if (props.get(ListValueService.PROP_NODEREF) != null) {
			entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_NODEREF));
			queryBuilder.andNotID(entityNodeRef);
		}

		String queryFilter = (String) props.get(ListValueService.PROP_FILTER);

		if ((queryFilter != null) && (!queryFilter.isEmpty())) {
			String[] splitted = queryFilter.split("\\|");

			String filterValue = splitted[1];
			String propQName = splitted[0];
			if ((filterValue != null) && !filterValue.isEmpty()) {		
				if (filterValue.contains("{")) {
					if (entityNodeRef != null) {
						filterValue = attributeExtractorService.extractExpr(filterValue, entityNodeRef);
					}
				}
				if ((filterValue != null) && !filterValue.isEmpty() && !filterValue.contains("{")) {
					boolean isOrOperand = false;
					if(propQName.endsWith("_or")) {
						isOrOperand = true;
						propQName = propQName.replace("_or", "");
					}
					
					if (filterValue.contains(",")) {
						if(isOrOperand)  {
							queryBuilder.andPropQuery(QName.createQName(propQName, namespaceService), filterValue.replace(",", " or "));
						} else {
							queryBuilder.andPropQuery(QName.createQName(propQName, namespaceService), filterValue.replace(",", " and "));
						}
					} else {
						queryBuilder.andPropEquals(QName.createQName(propQName, namespaceService), filterValue);
					}
				}
			}
		}

		// filter by classNames
		filterByClass(queryBuilder, arrClassNames);

		queryBuilder.maxResults(RepoConsts.MAX_SUGGESTIONS);

		List<NodeRef> ret = null;

		Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
		if (extras != null) {
			String filterByAssoc = extras.get(PROP_FILTER_BY_ASSOC);
			if ((filterByAssoc != null) && (filterByAssoc.length() > 0) && (entityNodeRef != null)) {
				QName assocQName = QName.createQName(filterByAssoc, namespaceService);

				List<NodeRef> targetNodeRefs = associationService.getTargetAssocs(entityNodeRef, assocQName);

				if ((targetNodeRefs != null) && !targetNodeRefs.isEmpty()) {
					List<NodeRef> tmp = queryBuilder.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();
					List<NodeRef> nodesToKeep = new ArrayList<>();

					for (NodeRef assocNodeRef : targetNodeRefs) {
						nodesToKeep.addAll(associationService.getSourcesAssocs(assocNodeRef, assocQName));
					}

					tmp.retainAll(nodesToKeep);
					if (!RepoConsts.MAX_RESULTS_UNLIMITED.equals(pageSize)) {
						ret = tmp.subList(0, Math.min(RepoConsts.MAX_SUGGESTIONS, tmp.size()));
					}
				}
			}
		}

		if (ret == null) {
			try {
				ret = queryBuilder.list();
			} catch (LuceneQueryParserException e) {
				logger.error("Bad list value query:" + queryBuilder.toString());
			}
		}

		return new ListValuePage(ret, pageNum, pageSize, targetAssocValueExtractor);
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
