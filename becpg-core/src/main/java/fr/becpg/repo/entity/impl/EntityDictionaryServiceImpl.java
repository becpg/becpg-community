package fr.becpg.repo.entity.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.cache.AsynchronouslyRefreshedCacheRegistry;
import org.alfresco.util.cache.RefreshableCacheEvent;
import org.alfresco.util.cache.RefreshableCacheListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.ParameterCheck;

import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;

/**
 * <p>EntityDictionaryServiceImpl class.</p>
 *
 * @author matthieu Fast and cached access to dataDictionary
 * @version $Id: $Id
 */
public class EntityDictionaryServiceImpl extends DictionaryComponent implements DictionaryService, EntityDictionaryService, RefreshableCacheListener, InitializingBean {

	private static Log logger = LogFactory.getLog(EntityDictionaryServiceImpl.class);

	private DictionaryDAO dictionaryDAO;

	private BeCPGCacheService beCPGCacheService;

	private AsynchronouslyRefreshedCacheRegistry registry;

	private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;
	
	protected NamespaceService namespaceService;
	
	private MessageService messageService;

	private Map<QName, QName> propDefMapping = new HashMap<>();
	
	private Map<QName, Set<QName>> extraAssocsDefMapping = new HashMap<>();
	
	/**
	 * <p>Setter for the field <code>messageService</code>.</p>
	 *
	 * @param messageService a {@link org.alfresco.repo.i18n.MessageService} object
	 */
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	/**
	 * <p>Setter for the field <code>repositoryEntityDefReader</code>.</p>
	 *
	 * @param repositoryEntityDefReader a {@link fr.becpg.repo.repository.RepositoryEntityDefReader} object
	 */
	public void setRepositoryEntityDefReader(RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader) {
		this.repositoryEntityDefReader = repositoryEntityDefReader;
	}

	/**
	 * <p>Setter for the field <code>registry</code>.</p>
	 *
	 * @param registry a {@link org.alfresco.util.cache.AsynchronouslyRefreshedCacheRegistry} object
	 */
	public void setRegistry(AsynchronouslyRefreshedCacheRegistry registry) {
		this.registry = registry;
	}

	/**
	 * <p>Setter for the field <code>beCPGCacheService</code>.</p>
	 *
	 * @param beCPGCacheService a {@link fr.becpg.repo.cache.BeCPGCacheService} object
	 */
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}
	
	

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Sets the Meta Model DAO
	 */
	@Override
	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		super.setDictionaryDAO(dictionaryDAO);
		this.dictionaryDAO = dictionaryDAO;
	}

	/** {@inheritDoc} */
	@Override
	public QName getDefaultPivotAssoc(QName dataListItemType) {
		return repositoryEntityDefReader.getDefaultPivoAssocName(dataListItemType);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isMultiLevelDataList(QName dataListItemType) {
		return repositoryEntityDefReader.isMultiLevelDataList(dataListItemType);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isMultiLevelLeaf(QName entityType) {
		return repositoryEntityDefReader.isMultiLevelLeaf(entityType);
	}

	/** {@inheritDoc} */
	@Override
	public QName getMultiLevelSecondaryPivot(QName dataListItemType) {
		return repositoryEntityDefReader.getMultiLevelSecondaryPivot(dataListItemType);
	}
	
	/** {@inheritDoc} */
	@Override
	public QName getMultiLevelGroupProperty(QName dataListItemType) {
		return repositoryEntityDefReader.getMultiLevelGroupProperty(dataListItemType);
	}

	/** {@inheritDoc} */
	@Override
	public void registerPropDefMapping(QName orig, QName dest) {
		propDefMapping.put(orig, dest);
	}
	
	/** {@inheritDoc} */
	@Override
	public void registerExtraAssocsDefMapping(QName orig, QName dest) {
		Set<QName> extra = extraAssocsDefMapping.computeIfAbsent(orig, (a) ->  new HashSet<>());
		extra.add(dest);
		extraAssocsDefMapping.put(orig, extra);
	}

	/** {@inheritDoc} */
	@Override
	public List<AssociationDefinition> getPivotAssocDefs(QName sourceType) {
		return getPivotAssocDefs(sourceType, false);
	}

	/** {@inheritDoc} */
	@Override
	public List<AssociationDefinition> getPivotAssocDefs(QName sourceType, boolean exactMatch) {
		List<AssociationDefinition> ret = new ArrayList<>();
		for (QName assocQName : getAllAssociations()) {
			AssociationDefinition assocDef = getAssociation(assocQName);
			if ((exactMatch && assocDef.getTargetClass().getName().equals(sourceType))
					|| (!exactMatch && isSubClass(assocDef.getTargetClass().getName(), sourceType))) {
				ret.add(assocDef);
			}
		}
		
		if(extraAssocsDefMapping.containsKey(sourceType)) {
			for (QName assocQName : extraAssocsDefMapping.get(sourceType)) {
				AssociationDefinition assocDef = getAssociation(assocQName);
				ret.add(assocDef);
			}
			
		}
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public QName getTargetType(QName assocName) {
		return getAssociation(assocName).getTargetClass().getName();
	}

	/** {@inheritDoc} */
	@Override
	public ClassAttributeDefinition findMatchingPropDef(QName itemType, QName newItemType, QName fieldQname) {

		if (propDefMapping.containsKey(fieldQname)) {
			return getPropDef(propDefMapping.get(fieldQname));
		}

		if (fieldQname.getLocalName().contains(itemType.getLocalName())) {
			QName newQname = QName.createQName(fieldQname.getNamespaceURI(),
					fieldQname.getLocalName().replace(itemType.getLocalName(), newItemType.getLocalName()));
			ClassAttributeDefinition ret = getPropDef(newQname);
			if (ret != null) {
				return ret;
			}
		}

		return getPropDef(fieldQname);
	}

	/** {@inheritDoc} */
	@Override
	public ClassAttributeDefinition getPropDef(final QName fieldQname) {

		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), fieldQname.toString() + ".propDef", () -> {
			ClassAttributeDefinition propDef = getProperty(fieldQname);
			if (propDef == null) {
				propDef = getAssociation(fieldQname);
			}
			return propDef;
		});

	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssoc(QName assocName) {
		return getAssociation(assocName) != null;
	}

	
	/** {@inheritDoc} */
	@Override
	public Collection<QName> getSubTypes(QName typeQname) {
		return getSubTypes(typeQname, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.alfresco.service.cmr.dictionary.DictionaryService#getSubTypes(org.alfresco.service.namespace.QName, boolean)
	 */
	/** {@inheritDoc} */
	@Override
	public Collection<QName> getSubTypes(QName superType, boolean follow) {

		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), superType.toString() + ".getSubTypes." + follow,
				() -> dictionaryDAO.getSubTypes(superType, follow));

	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.alfresco.service.cmr.dictionary.DictionaryService#getSubAspects(org.alfresco.service.namespace.QName, boolean)
	 */
	/** {@inheritDoc} */
	@Override
	public Collection<QName> getSubAspects(QName superAspect, boolean follow) {

		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), superAspect.toString() + ".getSubAspects." + follow,
				() -> dictionaryDAO.getSubAspects(superAspect, follow));

	}


	/** {@inheritDoc} */
	@Override
	public String toPrefixString(QName propertyQName) {
		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), propertyQName.toString() + ".toPrefixString" ,
				() -> propertyQName.toPrefixString(namespaceService));
	}
	
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.alfresco.repo.dictionary.DictionaryService#isSubClass(org.alfresco.repo.ref.QName, org.alfresco.repo.ref.QName)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isSubClass(QName className, QName ofClassName) {

		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(),
				className.toString() + "_" + ofClassName.toString() + ".isSubClass", () -> {

					// Validate arguments
					ParameterCheck.mandatory("className", className);
					ParameterCheck.mandatory("ofClassName", ofClassName);
					ClassDefinition classDef = getClass(className);
					if (classDef == null) {
						return false;
					}
					ClassDefinition ofClassDef = getClass(ofClassName);
					if (ofClassDef == null) {
						return false;
					}

					// Only check if both ends are either a type or an aspect
					boolean subClassOf = false;
					if (classDef.isAspect() == ofClassDef.isAspect()) {
						while (classDef != null) {
							if (classDef.equals(ofClassDef)) {
								subClassOf = true;
								break;
							}

							// No match yet, so go to parent class
							QName parentClassName = classDef.getParentName();
							classDef = (parentClassName == null) ? null : getClass(parentClassName);
						}
					}
					return subClassOf;
				});

	}



	/** {@inheritDoc} */
	@Override
	public void onRefreshableCacheEvent(RefreshableCacheEvent refreshableCacheEvent) {
		if("compiledModelsCache".equals(refreshableCacheEvent.getCacheId())){
			if (logger.isInfoEnabled()) {
				logger.info("Refreshing CachedDictionaryService cache: "+ refreshableCacheEvent.getCacheId() );
			}
			beCPGCacheService.clearCache(EntityDictionaryServiceImpl.class.getName());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getCacheId() {
		return EntityDictionaryServiceImpl.class.getName();
	}

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		registry.register(this);
		
	}
	
	/** {@inheritDoc} */
	@Override
	public String getTitle(ClassAttributeDefinition attributeDefinition, QName nodeType) {
		if (nodeType != null) {
			String title = messageService.getMessage(computeOverrideKey(attributeDefinition, nodeType) + ".title");
			if (title != null && !title.isBlank()) {
				return title;
			}
		}
		return attributeDefinition.getTitle(this);
	}
	
	/** {@inheritDoc} */
	@Override
	public String getDescription(ClassAttributeDefinition attributeDefinition, QName nodeType) {
		if (nodeType != null) {
			String description = messageService.getMessage(computeOverrideKey(attributeDefinition, nodeType) + ".description");
			if (description != null && !description.isBlank()) {
				return description;
			}
		}
		return attributeDefinition.getDescription(this);
	}

	private String computeOverrideKey(ClassAttributeDefinition attributeDefinition, QName nodeType) {
		return "model.override." + nodeType.toPrefixString(namespaceService).replace(":", "_") + "."
				+ attributeDefinition.getName().toPrefixString(namespaceService).replace(":", "_");
	}

}
