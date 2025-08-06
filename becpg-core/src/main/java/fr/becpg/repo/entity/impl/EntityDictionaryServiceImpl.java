package fr.becpg.repo.entity.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.cache.RefreshableCacheListener;
import org.springframework.beans.factory.InitializingBean;

import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;

/**
 * Enhanced implementation of EntityDictionaryService with performance optimizations
 * including improved caching, reduced object creation, and optimized data structures.
 *
 * @author matthieu Fast and cached access to dataDictionary
 * @version $Id: $Id
 */
public class EntityDictionaryServiceImpl extends CachedDictionaryServiceImpl
		implements DictionaryService, EntityDictionaryService, RefreshableCacheListener, InitializingBean {

	// Constants for cache keys
	private static final String PROP_DEF_CACHE_SUFFIX = ".propDef";
	private static final String PREFIX_STRING_CACHE_SUFFIX = ".toPrefixString";
	private static final String ASSOC_INDEX_CACHE_SUFFIX = ".assocIndex";
	private static final String ASSOC_INDEX_PROPERTY_SUFFIX = "AssocIndex";

	private static final String MODEL_OVERRIDE_PREFIX = "model.override.";
	private static final String TITLE_SUFFIX = ".title";
	private static final String DESCRIPTION_SUFFIX = ".description";

	private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;
	private NamespaceService namespaceService;
	private MessageService messageService;

	// Use ConcurrentHashMap for thread-safe access without synchronization overhead
	private final Map<QName, QName> propDefMapping = new ConcurrentHashMap<>();
	private final Map<QName, Set<QName>> extraAssocsDefMapping = new ConcurrentHashMap<>();

	// Cache for frequently computed strings to avoid repeated computations
	private final Map<QName, String> prefixStringCache = new ConcurrentHashMap<>();
	private final Map<String, String> overrideKeyCache = new ConcurrentHashMap<>();

	// Setters
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public void setRepositoryEntityDefReader(RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader) {
		this.repositoryEntityDefReader = repositoryEntityDefReader;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	// Repository entity methods - delegated to reader
	@Override
	public QName getDefaultPivotAssoc(QName dataListItemType) {
		return repositoryEntityDefReader.getDefaultPivoAssocName(dataListItemType);
	}

	@Override
	public boolean isMultiLevelDataList(QName dataListItemType) {
		return repositoryEntityDefReader.isMultiLevelDataList(dataListItemType);
	}

	@Override
	public boolean isMultiLevelLeaf(QName entityType) {
		return repositoryEntityDefReader.isMultiLevelLeaf(entityType);
	}

	@Override
	public QName getMultiLevelSecondaryPivot(QName dataListItemType) {
		return repositoryEntityDefReader.getMultiLevelSecondaryPivot(dataListItemType);
	}

	@Override
	public QName getMultiLevelGroupProperty(QName dataListItemType) {
		return repositoryEntityDefReader.getMultiLevelGroupProperty(dataListItemType);
	}

	// Mapping registration methods
	@Override
	public void registerPropDefMapping(QName orig, QName dest) {
		propDefMapping.put(orig, dest);
	}

	@Override
	public void registerExtraAssocsDefMapping(QName orig, QName dest) {
		extraAssocsDefMapping.computeIfAbsent(orig, k -> ConcurrentHashMap.newKeySet()).add(dest);
	}

	@Override
	public List<AssociationDefinition> getPivotAssocDefs(QName sourceType) {
		return getPivotAssocDefs(sourceType, false);
	}

	@Override
	public List<AssociationDefinition> getPivotAssocDefs(QName sourceType, boolean exactMatch) {
		// Use ArrayList with initial capacity to avoid resizing
		List<AssociationDefinition> ret = new ArrayList<>(16);

		// Cache associations list to avoid repeated calls
		Collection<QName> allAssociations = getAllAssociations();

		for (QName assocQName : allAssociations) {
			AssociationDefinition assocDef = getAssociation(assocQName);
			if (assocDef != null) {
				QName targetClassName = assocDef.getTargetClass().getName();
				if ((exactMatch && targetClassName.equals(sourceType)) || (!exactMatch && isSubClass(targetClassName, sourceType))) {
					ret.add(assocDef);
				}
			}
		}

		// Add extra associations if present
		Set<QName> extraAssocs = extraAssocsDefMapping.get(sourceType);
		if ((extraAssocs != null) && !extraAssocs.isEmpty()) {
			for (QName assocQName : extraAssocs) {
				AssociationDefinition assocDef = getAssociation(assocQName);
				if (assocDef != null) {
					ret.add(assocDef);
				}
			}
		}

		return ret;
	}

	@Override
	public QName getTargetType(QName assocName) {
		AssociationDefinition assocDef = getAssociation(assocName);
		return assocDef != null ? assocDef.getTargetClass().getName() : null;
	}

	@Override
	public ClassAttributeDefinition findMatchingPropDef(QName itemType, QName newItemType, QName fieldQname) {
		// Check mapping first
		QName mappedQName = propDefMapping.get(fieldQname);
		if (mappedQName != null) {
			return getPropDef(mappedQName);
		}

		// Try name-based matching with caching
		String fieldLocalName = fieldQname.getLocalName();
		String itemLocalName = itemType.getLocalName();

		if (fieldLocalName.contains(itemLocalName)) {
			QName newQname = QName.createQName(fieldQname.getNamespaceURI(), fieldLocalName.replace(itemLocalName, newItemType.getLocalName()));
			ClassAttributeDefinition ret = getPropDef(newQname);
			if (ret != null) {
				return ret;
			}
		}

		return getPropDef(fieldQname);
	}

	@Override
	public ClassAttributeDefinition getPropDef(final QName fieldQname) {
		String cacheKey = buildCacheKey(fieldQname.toString(), PROP_DEF_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			ClassAttributeDefinition propDef = getProperty(fieldQname);
			if (propDef == null) {
				propDef = getAssociation(fieldQname);
			}
			return propDef;
		});
	}

	@Override
	public boolean isAssoc(QName assocName) {
		return getAssociation(assocName) != null;
	}

	@Override
	public Collection<QName> getSubTypes(QName typeQname) {
		return getSubTypes(typeQname, true);
	}

	@Override
	public String toPrefixString(QName propertyQName) {
		return prefixStringCache.computeIfAbsent(propertyQName, qname -> {
			String cacheKey = buildCacheKey(qname.toString(), PREFIX_STRING_CACHE_SUFFIX);
			return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey,
					() -> qname.toPrefixString(namespaceService));
		});
	}

	@Override
	public String getTitle(ClassAttributeDefinition attributeDefinition, QName nodeType) {
		if (nodeType != null) {
			String overrideKey = computeOverrideKey(attributeDefinition, nodeType);
			String title = messageService.getMessage(overrideKey + TITLE_SUFFIX);
			if ((title != null) && !title.isBlank()) {
				return title;
			}
		}
		return attributeDefinition.getTitle(this);
	}

	@Override
	public String getDescription(ClassAttributeDefinition attributeDefinition, QName nodeType) {
		if (nodeType != null) {
			String overrideKey = computeOverrideKey(attributeDefinition, nodeType);
			String description = messageService.getMessage(overrideKey + DESCRIPTION_SUFFIX);
			if ((description != null) && !description.isBlank()) {
				return description;
			}
		}
		return attributeDefinition.getDescription(this);
	}

	@Override
	public QName getAssocIndexQName(QName assocQName) {
		String cacheKey = buildCacheKey(assocQName.toString(), ASSOC_INDEX_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			QName indexPropertyQName = QName.createQName(assocQName.getNamespaceURI(), assocQName.getLocalName() + ASSOC_INDEX_PROPERTY_SUFFIX);
			ClassAttributeDefinition indexProp = getPropDef(indexPropertyQName);
			return indexProp != null ? indexProp.getName() : null;
		});
	}

	/**
	 * Computes override keys with caching to avoid repeated string operations
	 */
	private String computeOverrideKey(ClassAttributeDefinition attributeDefinition, QName nodeType) {
		String key = nodeType.toString() + "|" + attributeDefinition.getName().toString();

		return overrideKeyCache.computeIfAbsent(key, k -> {
			String nodeTypePrefix = nodeType.toString();
			String attrNamePrefix = attributeDefinition.getName().toString();
			return MODEL_OVERRIDE_PREFIX + nodeTypePrefix + "." + attrNamePrefix;
		});
	}
}