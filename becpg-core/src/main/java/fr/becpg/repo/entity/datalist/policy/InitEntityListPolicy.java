/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.datalist.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * Store in the name of the product list folder the type of product list.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class InitEntityListPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	private static final int MAX_SIBLING_LISTS = 20;

	private static final Log logger = LogFactory.getLog(InitEntityListPolicy.class);

	/** The namespace service. */
	private NamespaceService namespaceService;

	/** The dictionary service. */
	private EntityDictionaryService entityDictionaryService;

	private NodeService mlNodeService;

	/**
	 * Sets the namespace service.
	 *
	 * @param namespaceService
	 *            the new namespace service
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>mlNodeService</code>.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Inits the.
	 */
	@Override
	public void doInit() {
		logger.debug("Init ProductListPolicies...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, DataListModel.TYPE_DATALIST,
				new JavaBehaviour(this, "onUpdateProperties"));

		// Copy
		disableOnCopyBehaviour(DataListModel.TYPE_DATALIST);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Initialize the name of the datalist with the local name if it is a
	 * entityDataList.
	 */
	@Override
	public void onUpdateProperties(NodeRef dataListNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (after.containsKey(DataListModel.PROP_DATALISTITEMTYPE)) {

			String beforeDLType = (String) before.get(DataListModel.PROP_DATALISTITEMTYPE);
			String afterDLType = (String) after.get(DataListModel.PROP_DATALISTITEMTYPE);

			if ((afterDLType != null) && !afterDLType.isEmpty() && !afterDLType.equals(beforeDLType)) {

				QName dataListTypeQName = QName.createQName(afterDLType, namespaceService);
				if (entityDictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {

					String dataListName = (String) nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME);
					if (!dataListName.startsWith(RepoConsts.WUSED_PREFIX) && !dataListName.startsWith(RepoConsts.CUSTOM_VIEW_PREFIX)
							&& !dataListName.startsWith(RepoConsts.SMART_CONTENT_PREFIX)
							&& !dataListTypeQName.getLocalName().equals(dataListName) && !BeCPGModel.TYPE_LIST_VALUE.equals(dataListTypeQName)) {

						nodeService.moveNode(dataListNodeRef, nodeService.getPrimaryParent(dataListNodeRef).getParentRef(),
								ContentModel.ASSOC_CONTAINS, dataListTypeQName);
						nodeService.setProperty(dataListNodeRef, ContentModel.PROP_NAME,
								createName(dataListNodeRef, dataListTypeQName.getLocalName()));

						ClassDefinition classDef = entityDictionaryService.getClass(dataListTypeQName);

						MLText title = (MLText) mlNodeService.getProperty(dataListNodeRef, ContentModel.PROP_TITLE);
						MLText description = (MLText) mlNodeService.getProperty(dataListNodeRef, ContentModel.PROP_DESCRIPTION);

						MLText classTitleMLText = TranslateHelper.getTemplateTitleMLText(classDef.getName());
						MLText classDescritptionMLText = TranslateHelper.getTemplateDescriptionMLText(classDef.getName());

						if ((title != null) && (classTitleMLText != null)) {
							mlNodeService.setProperty(dataListNodeRef, ContentModel.PROP_TITLE, MLTextHelper.merge(title, classTitleMLText));
						} else if(classTitleMLText != null) {
							mlNodeService.setProperty(dataListNodeRef, ContentModel.PROP_TITLE, classTitleMLText);
						}
						if ((description != null) && (classDescritptionMLText != null)) {
							mlNodeService.setProperty(dataListNodeRef, ContentModel.PROP_DESCRIPTION,
									MLTextHelper.merge(description, classDescritptionMLText));
						} if(classDescritptionMLText != null) {
							mlNodeService.setProperty(dataListNodeRef, ContentModel.PROP_DESCRIPTION, classDescritptionMLText);
						}

					}
				}
			}
		}
	}

	private Serializable createName(NodeRef dataListNodeRef, String localName) {
		int count = 0;
		if (localName.contains("@")) {
			count = Integer.parseInt(localName.split("@")[1]);
		}
		count++;
		if ((count < MAX_SIBLING_LISTS) && (nodeService.getChildByName(nodeService.getPrimaryParent(dataListNodeRef).getParentRef(),
				ContentModel.ASSOC_CONTAINS, localName) != null)) {
			return createName(dataListNodeRef, localName.split("@")[0] + "@" + count);
		}
		return localName;
	}

}
