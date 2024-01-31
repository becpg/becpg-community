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
package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.api.BeCPGPublicApi;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>ProductAutoCompletePlugin class.</p>
 * 
 * @author matthieu
 * @version $Id: $Id
 * 
 * Autocomplete plugin class that allows to find product 
 * Product are automatically boost by product state and query, Archived product are excluded : query^10 AND (Valid^4 OR ToValidate^2 OR Simulation)
 * 
 * Example:
 * <pre>
 * {@code
 * <control template="/org/alfresco/components/form/controls/autocomplete-association.ftl">
 *		<control-param name="ds">becpg/autocomplete/product?classNames=bcpg:rawMaterial%5E4,bcpg:finishedProduct,bcpg:localSemiFinishedProduct%5E2,bcpg:semiFinishedProduct%5E2</control-param>
 *	</control>
 * }
 * </pre>
 *						
 *	Datasource:
 *
 * 
 * ds: /becpg/autocomplete/targetassoc/product/?classNames={classNames?}
 * param: {className} type of item to retrieve
 * param: {classNames} (optional)  comma separated lists of classNames, can be uses to filter by aspect or boost certain types (inc_ or ^)
 * param: {andProps} (optional/deprecated) comma separated of property|value pair that item should have  (filter=prop_to_filter|value)
 * param: {filter} (optional) same as andProps
 * param: {excludeProps} (optional) comma separated of property|value pair that item should not have
 * param: {excludeClassNames} (optional) comma separated lists of classNames that will be excluded
 * param: {extra.filterByAssoc} return item that has same assoc that in current entity
 *
 *
 * See TargetAssocAutoCompletePlugin for params example
 */
@Service("productAutoCompletePlugin")
@BeCPGPublicApi
public class ProductAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final String SOURCE_TYPE_PRODUCT = "product";

	private static final String SOURCE_TYPE_COLLECTION_PRODUCT = "collectionproduct";

	private static final Log logger = LogFactory.getLog(ProductAutoCompletePlugin.class);

	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	private String productSearchTemplate() {
		return systemConfigurationService.confValue("beCPG.product.searchTemplate");
	}

	@Autowired
	private EntityListDAO entityListDAO;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_PRODUCT, SOURCE_TYPE_COLLECTION_PRODUCT };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String classNames = (String) props.get(AutoCompleteService.PROP_CLASS_NAMES);
		String[] arrClassNames = classNames != null ? classNames.split(PARAM_VALUES_SEPARATOR) : null;

		if (sourceType.equals(SOURCE_TYPE_PRODUCT)) {
			return suggestProducts(query, pageNum, pageSize, arrClassNames, props);
		} else if (sourceType.equals(SOURCE_TYPE_COLLECTION_PRODUCT)) {
			String parent = (String) props.get(AutoCompleteService.PROP_PARENT);
			if ((parent == null) || parent.isEmpty() || !NodeRef.isNodeRef(parent)) {
				return suggestProducts(query, pageNum, pageSize, arrClassNames, props);
			} else {
				return suggestByDataListAssoc(query, pageNum, pageSize, arrClassNames, new NodeRef(parent), PLMModel.TYPE_PRODUCTLIST,
						PLMModel.ASSOC_PRODUCTLIST_PRODUCT);
			}

		}

		return null;
	}

	private AutoCompletePage suggestByDataListAssoc(String query, Integer pageNum, Integer pageSize, String[] arrClassNames, NodeRef entityNodeRef,
			QName listQName, QName assocQName) {

		NodeRef listsContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listsContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, listQName);

			if (dataListNodeRef != null) {

				List<AutoCompleteEntry> result = new ArrayList<>();
				for (NodeRef dataListItemNodeRef : entityListDAO.getListItems(dataListNodeRef, listQName)) {
					NodeRef targetNode = associationService.getTargetAssoc(dataListItemNodeRef, assocQName);

					if (targetNode != null) {
						QName type = nodeService.getType(targetNode);
						String name = attributeExtractorService.extractPropName(type, targetNode);
						if (isQueryMatch(query, name) && accept(type, arrClassNames)) {
							String cssClass = attributeExtractorService.extractMetadata(type, targetNode);
							result.add(new AutoCompleteEntry(targetNode.toString(), name, cssClass));
						}
					}
				}
				return new AutoCompletePage(result, pageNum, pageSize, null);

			} else {
				logger.warn("No datalists productList found ");
			}
		}
		return null;
	}

	private boolean accept(QName type, String[] arrClassNames) {
		if (arrClassNames != null && arrClassNames.length > 0) {

			for (String className : arrClassNames) {
				QName classQName;
				if (className.contains("^")) {
					String[] splitted = className.split("\\^");
					classQName = QName.createQName(splitted[0], namespaceService);
				} else {
					if (className.startsWith("inc_")) {
						classQName = QName.createQName(className.replace("inc_", ""), namespaceService);
					} else {
						classQName = QName.createQName(className, namespaceService);
					}
				}

				if (type.equals(classQName)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	private AutoCompletePage suggestProducts(String query, Integer pageNum, Integer pageSize, String[] arrClassNames,
			Map<String, Serializable> props) {
		if (logger.isDebugEnabled()) {
			if (arrClassNames != null) {
				logger.debug("suggestTargetAssoc with arrClassNames : " + Arrays.toString(arrClassNames));
			}
		}

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_PRODUCT).excludeDefaults()
				.excludeArchivedEntities()
				.inSearchTemplate(productSearchTemplate()).locale(I18NUtil.getContentLocale()).andOperator().ftsLanguage();

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


		return new AutoCompletePage(filter(queryBuilder, null, arrClassNames, pageSize, props), pageNum, pageSize, targetAssocValueExtractor);
	}


}
