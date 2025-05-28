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

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.autocomplete.impl.extractors.NodeRefAutoCompleteExtractor;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>SpelEditorListValuePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("spelEditorListAutoCompletePlugin")
public class SpelEditorListAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final Log logger = LogFactory.getLog(SpelEditorListAutoCompletePlugin.class);

	private static final String SOURCE_TYPE_SPELEDITOR = "speleditor";

	@Autowired
	private EntityListDAO entityListDAO;

	/**
	 * {@inheritDoc}
	 *
	 * <p>getHandleSourceTypes.</p>
	 */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_SPELEDITOR };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String className = (String) props.get(AutoCompleteService.PROP_CLASS_NAME);

		if ((className != null) && !className.isBlank() && className.contains(PARAM_VALUES_SEPARATOR)) {
			String[] arrClassNames = className.split(PARAM_VALUES_SEPARATOR);
			return suggestTargetAssoc(null, PLMModel.TYPE_PRODUCT, query, pageNum, pageSize, arrClassNames, props);
		}
		// Class is a Java class
		try {
			Class<?> c = Class.forName(className);
			BeanWrapper beanWrapper = new BeanWrapperImpl(c);
			List<AutoCompleteEntry> ret = new ArrayList<>();

			for (PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {
				if (((pd.getReadMethod() != null) && (pd.getWriteMethod() != null)) && !pd.getReadMethod().isAnnotationPresent(InternalField.class)) {
					ret.add(new AutoCompleteEntry(pd.getName(), pd.getName(), pd.getPropertyType().getSimpleName()));
				}
			}

			return new AutoCompletePage(ret, pageNum, pageSize, null);

		} catch (ClassNotFoundException e) {
			logger.debug(e, e);
		}

		QName type = QName.createQName(className, namespaceService);

		if (type.equals(PLMModel.TYPE_DYNAMICCHARACTLIST)) {

			return suggestDynCharact(new NodeRef((String) props.get(AutoCompleteService.PROP_ENTITYNODEREF)), type,
					PLMModel.PROP_DYNAMICCHARACT_TITLE, query, pageNum, pageSize);
		}

		return suggestTargetAssoc(null, type, query, pageNum, pageSize, null, props);
	}

	private AutoCompletePage suggestDynCharact(NodeRef entityNodeRef, QName datalistType, QName propertyQName, String query, Integer pageNum,
			Integer pageSize) {

		List<NodeRef> ret = new ArrayList<>();

		NodeRef listsContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listsContainerNodeRef != null) {
			List<NodeRef> dataListNodeRefs = entityListDAO.getExistingListsNodeRef(listsContainerNodeRef);
			for (NodeRef dataListNodeRef : dataListNodeRefs) {
				BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(datalistType)
						.inParent(dataListNodeRef).maxResults(RepoConsts.MAX_SUGGESTIONS);

				ret.addAll(queryBuilder.list());
			}

		}

		return new AutoCompletePage(ret, pageNum, pageSize, new NodeRefAutoCompleteExtractor(propertyQName, nodeService));
	}

}
