/*
 * Copyright 2016 - 2024 Acosix GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.repo.nodelocator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * This implementation provides an XPath based {@link NodeLocator} which uses the transactionally-safe and fully XPath-supporting
 * {@link SearchService#selectNodes(org.alfresco.service.cmr.repository.NodeRef, String, org.alfresco.service.cmr.search.QueryParameterDefinition[], org.alfresco.service.namespace.NamespacePrefixResolver, boolean)
 * selectNodes} API instead of the index-based, partial {@link SearchService#LANGUAGE_XPATH XPath search language} used by the
 * {@link org.alfresco.repo.nodelocator.XPathNodeLocator default implementation}.
 *
 * @author Axel Faust
 */
public class XPathNodeLocator extends AbstractNodeLocator implements InitializingBean {

	public static final String NAME = "xpath";
	public static final String QUERY_KEY = "query";
	public static final String STORE_TYPE_KEY = "store_type";
	public static final String STORE_ID_KEY = "store_id";

	protected NamespaceService namespaceService;

	protected NodeService nodeService;

	protected SearchService searchService;

	protected StoreRef defaultStore;

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() {
		PropertyCheck.mandatory(this, "namespaceService", this.namespaceService);
		PropertyCheck.mandatory(this, "nodeService", this.nodeService);
		PropertyCheck.mandatory(this, "searchService", this.searchService);
		PropertyCheck.mandatory(this, "defaultStore", this.defaultStore);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeRef getNode(final NodeRef source, final Map<String, Serializable> params) {
		final String query = DefaultTypeConverter.INSTANCE.convert(String.class, params.get(QUERY_KEY));
		ParameterCheck.mandatoryString("query", query);
		final StoreRef store;
		if (source != null) {
			store = source.getStoreRef();
		} else {
			final String storeType = DefaultTypeConverter.INSTANCE.convert(String.class, params.get(STORE_TYPE_KEY));
			final String storeId = DefaultTypeConverter.INSTANCE.convert(String.class, params.get(STORE_ID_KEY));
			if (storeType != null && storeId != null) {
				store = new StoreRef(storeType, storeId);
			} else {
				store = this.defaultStore;
			}
		}

		final NodeRef rootNode = this.nodeService.getRootNode(store);
		final List<NodeRef> nodes = this.searchService.selectNodes(rootNode, query, new QueryParameterDefinition[0], this.namespaceService, true);
		if (!nodes.isEmpty()) {
			return nodes.get(0);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public List<ParameterDefinition> getParameterDefinitions()
    {
        List<ParameterDefinition> paramDefs = new ArrayList<ParameterDefinition>(2);
		paramDefs.add(new ParameterDefinitionImpl(QUERY_KEY, DataTypeDefinition.TEXT, true, "Query"));
		paramDefs.add(new ParameterDefinitionImpl(STORE_TYPE_KEY, DataTypeDefinition.TEXT, false, "Store Type"));
		paramDefs.add(new ParameterDefinitionImpl(STORE_ID_KEY, DataTypeDefinition.TEXT, false, "Store Id"));
		return paramDefs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @param namespaceService
	 *            the namespaceService to set
	 */
	public void setNamespaceService(final NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(final NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * @param searchService
	 *            the searchService to set
	 */
	public void setSearchService(final SearchService searchService) {
		this.searchService = searchService;
	}

	/**
	 * @param defaultStoreStr
	 *            the defaultStoreStr to set
	 */
	public void setDefaultStore(final String defaultStoreStr) {
		this.defaultStore = new StoreRef(defaultStoreStr);
	}
}
