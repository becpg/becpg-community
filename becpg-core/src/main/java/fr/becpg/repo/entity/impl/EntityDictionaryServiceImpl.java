package fr.becpg.repo.entity.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.tenant.TenantUtil;
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
 * Enhanced implementation of EntityDictionaryService with performance optimizations
 * including improved caching, reduced object creation, and optimized data structures.
 *
 * @author matthieu Fast and cached access to dataDictionary
 * @version $Id: $Id
 */
public class EntityDictionaryServiceImpl extends DictionaryComponent
		implements DictionaryService, EntityDictionaryService, RefreshableCacheListener, InitializingBean {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(EntityDictionaryServiceImpl.class);

	// Constants for cache keys
	/** Constant <code>MODEL_OVERRIDE_PREFIX="model.override."</code> */
	private static final String MODEL_OVERRIDE_PREFIX = "model.override.";
	/** Constant <code>TITLE_SUFFIX=".title"</code> */
	private static final String TITLE_SUFFIX = ".title";
	/** Constant <code>DESCRIPTION_SUFFIX=".description"</code> */
	private static final String DESCRIPTION_SUFFIX = ".description";
	/** Constant <code>COLON_REPLACEMENT="_"</code> */
	private static final String COLON_REPLACEMENT = "_";
	/** Constant <code>COMPILED_MODELS_CACHE="compiledModelsCache"</code> */
	private static final String COMPILED_MODELS_CACHE = "compiledModelsCache";

	private DictionaryDAO dictionaryDAO;
	private BeCPGCacheService beCPGCacheService;
	private AsynchronouslyRefreshedCacheRegistry registry;
	private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;
	private NamespaceService namespaceService;
	private MessageService messageService;

	// Use ConcurrentHashMap for thread-safe access without synchronization overhead
	private final Map<QName, QName> propDefMapping = new ConcurrentHashMap<>();
	private final Map<QName, Set<QName>> extraAssocsDefMapping = new ConcurrentHashMap<>();

	// Cache for frequently computed strings to avoid repeated computations
	private final Map<QName, String> prefixStringCache = new ConcurrentHashMap<>();
	private final Map<String, String> overrideKeyCache = new ConcurrentHashMap<>();
	
	// Tenant-aware caches
	private final Map<String, Map<QName, Collection<QName>>> subTypesCache = new ConcurrentHashMap<>();
	private final Map<String, Map<QName, Collection<QName>>> subTypesNoFollowCache = new ConcurrentHashMap<>();
	private final Map<String, Map<QName, ClassAttributeDefinition>> propDefCache = new ConcurrentHashMap<>();
	private final Map<String, Map<QName, ClassDefinition>> classDefCache = new ConcurrentHashMap<>();
	private final Map<String, Map<QName, List<AssociationDefinition>>> pivotAssocDefsCache = new ConcurrentHashMap<>();
	private final Map<String, Map<QName, Map<QName, Boolean>>> isSubClassCache = new ConcurrentHashMap<>();
	private final Map<String, Map<QName, List<QName>>> defaultPivotAssocsFromTargetTypeCache = new ConcurrentHashMap<>();
	private final Map<String, Map<QName, List<QName>>> targetTypesFromAssocCache = new ConcurrentHashMap<>();

	private <K, V> Map<K, V> getTenantCache(Map<String, Map<K, V>> caches) {
		String domain = TenantUtil.getCurrentDomain();
		return caches.computeIfAbsent(domain, k -> new ConcurrentHashMap<>());
	}

	// Setters
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
	 * @since 23.4.2.28
	 */
	public void setRegistry(AsynchronouslyRefreshedCacheRegistry registry) {
		this.registry = registry;
	}

	/**
	 * <p>Setter for the field <code>beCPGCacheService</code>.</p>
	 *
	 * @param beCPGCacheService a {@link fr.becpg.repo.cache.BeCPGCacheService} object
	 * @since 23.4.2.28
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

	/** {@inheritDoc} */
	@Override
	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		super.setDictionaryDAO(dictionaryDAO);
		this.dictionaryDAO = dictionaryDAO;
	}

	// Repository entity methods - delegated to reader
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

	// Mapping registration methods
	/** {@inheritDoc} */
	@Override
	public void registerPropDefMapping(QName orig, QName dest) {
		propDefMapping.put(orig, dest);
	}

	/** {@inheritDoc} */
	@Override
	public void registerExtraAssocsDefMapping(QName orig, QName dest) {
		extraAssocsDefMapping.computeIfAbsent(orig, k -> ConcurrentHashMap.newKeySet()).add(dest);
	}

	/** {@inheritDoc} */
	@Override
	public List<AssociationDefinition> getPivotAssocDefs(QName sourceType) {
		return getPivotAssocDefs(sourceType, false);
	}

	/** {@inheritDoc} */
	@Override
	public List<AssociationDefinition> getPivotAssocDefs(QName sourceType, boolean exactMatch) {
		if (exactMatch) {
			return computePivotAssocDefs(sourceType, true);
		}

		return getTenantCache(pivotAssocDefsCache).computeIfAbsent(sourceType, k -> computePivotAssocDefs(k, false));
	}

	private List<AssociationDefinition> computePivotAssocDefs(QName sourceType, boolean exactMatch) {
		List<AssociationDefinition> ret = new ArrayList<>(16);
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

		Set<QName> extraAssocs = extraAssocsDefMapping.get(sourceType);
		if (extraAssocs != null && !extraAssocs.isEmpty()) {
			for (QName assocQName : extraAssocs) {
				AssociationDefinition assocDef = getAssociation(assocQName);
				if (assocDef != null) {
					ret.add(assocDef);
				}
			}
		}

		return Collections.unmodifiableList(ret);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<QName> getDefaultPivotAssocsFromTargetType(QName targetType) {
		return getTenantCache(defaultPivotAssocsFromTargetTypeCache).computeIfAbsent(targetType, k -> {
			List<QName> assocs = new ArrayList<>();
			for (QName defaultPivotAssoc : repositoryEntityDefReader.getDefaultPivotAssocs()) {
				String defaultPivotTargetTypes = repositoryEntityDefReader.getDataListAssocToTargetTypes().get(defaultPivotAssoc);
				if (defaultPivotTargetTypes != null && !defaultPivotTargetTypes.isBlank()) {
					String[] targetTypes = defaultPivotTargetTypes.split(",");
					for (String targetTypeStr : targetTypes) {
						if (targetType.toPrefixString(namespaceService).equals(targetTypeStr.trim())) {
							assocs.add(defaultPivotAssoc);
							break;
						}
					}
				} else {
					AssociationDefinition defaultPivotAssocDef = getAssociation(defaultPivotAssoc);
					// Une association déclarée comme pivot mais absente du dictionnaire :
					// le modèle qui la porte n'est pas déployé, ou plus. Sans cette garde
					// le déréférencement fait tomber TOUTE la datalist en 500 — pour tous
					// les utilisateurs et sur toutes les listes du type. Constaté le
					// 04/08/2026 sur dev : ingList et nutList inaccessibles, le portail
					// affichant « connexion impossible » alors que beCPG répondait.
					// Une association inconnue n'est simplement pas un pivot pour ce type.
					if (defaultPivotAssocDef == null) {
						logger.warn("Default pivot assoc not found in dictionary, ignoring: " + defaultPivotAssoc);
					} else if (targetType.equals(defaultPivotAssocDef.getTargetClass().getName())) {
						assocs.add(defaultPivotAssocDef.getName());
					}
				}
			}
			return assocs;
		});
	}
	
    /** {@inheritDoc} */
	@Override
    public List<QName> getTargetTypes(QName assocName) {
		return getTenantCache(targetTypesFromAssocCache).computeIfAbsent(assocName, k -> {
			List<QName> targetTypes = new ArrayList<>();
			String targetTypesString = repositoryEntityDefReader.getDataListAssocToTargetTypes().get(assocName);
			if (targetTypesString != null && !targetTypesString.isBlank()) {
				String[] targetTypeStrs = targetTypesString.split(",");
				for (String targetTypeStr : targetTypeStrs) {
					targetTypes.add(QName.createQName(targetTypeStr.trim(), namespaceService));
				}
			} else {
				targetTypes.add(getTargetType(assocName));
			}
			return targetTypes;
		});
    }

	/** {@inheritDoc} */
	@Override
	public QName getTargetType(QName assocName) {
		AssociationDefinition assocDef = getAssociation(assocName);
		return assocDef != null ? assocDef.getTargetClass().getName() : null;
	}

	/** {@inheritDoc} */
	@Override
	public ClassAttributeDefinition findMatchingPropDef(QName itemType, QName newItemType, QName fieldQname) {
		QName mappedQName = propDefMapping.get(fieldQname);
		if (mappedQName != null) {
			return getPropDef(mappedQName);
		}

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

	/** {@inheritDoc} */
	@Override
	public ClassAttributeDefinition getPropDef(final QName fieldQname) {
		if (fieldQname == null) {
			return null;
		}
		Map<QName, ClassAttributeDefinition> cache = getTenantCache(propDefCache);
		ClassAttributeDefinition propDef = cache.get(fieldQname);
		if (propDef == null) {
			propDef = getProperty(fieldQname);
			if (propDef == null) {
				propDef = getAssociation(fieldQname);
			}
			if (propDef != null) {
				cache.put(fieldQname, propDef);
			}
		}
		return propDef;
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

	/** {@inheritDoc} */
	@Override
	public Collection<QName> getSubTypes(QName superType, boolean follow) {
		Map<String, Map<QName, Collection<QName>>> caches = follow ? subTypesCache : subTypesNoFollowCache;
		return getTenantCache(caches).computeIfAbsent(superType, k -> {
			Collection<QName> result = dictionaryDAO.getSubTypes(superType, follow);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	/** {@inheritDoc} */
	@Override
	public Collection<QName> getSubAspects(QName superAspect, boolean follow) {
		Map<String, Map<QName, Collection<QName>>> caches = follow ? subTypesCache : subTypesNoFollowCache;
		return getTenantCache(caches).computeIfAbsent(superAspect, k -> {
			Collection<QName> result = dictionaryDAO.getSubAspects(superAspect, follow);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	/** {@inheritDoc} */
	@Override
	public String toPrefixString(QName propertyQName) {
		return prefixStringCache.computeIfAbsent(propertyQName, qname -> qname.toPrefixString(namespaceService));
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSubClass(QName className, QName ofClassName) {
		if (className == null || ofClassName == null) {
			return false;
		}

		if (className.equals(ofClassName)) {
			return true;
		}

		return getTenantCache(isSubClassCache).computeIfAbsent(className, k -> new ConcurrentHashMap<>())
				.computeIfAbsent(ofClassName, k -> computeIsSubClass(className, ofClassName));
	}

	/** {@inheritDoc} */
	@Override
	public void onRefreshableCacheEvent(RefreshableCacheEvent refreshableCacheEvent) {
		if (COMPILED_MODELS_CACHE.equals(refreshableCacheEvent.getCacheId())) {
			if (logger.isInfoEnabled()) {
				logger.info("Refreshing CachedDictionaryService cache: " + refreshableCacheEvent.getCacheId());
			}
			beCPGCacheService.clearCache(EntityDictionaryServiceImpl.class.getName());
			prefixStringCache.clear();
			overrideKeyCache.clear();
			subTypesCache.clear();
			subTypesNoFollowCache.clear();
			propDefCache.clear();
			classDefCache.clear();
			pivotAssocDefsCache.clear();
			isSubClassCache.clear();
			defaultPivotAssocsFromTargetTypeCache.clear();
			targetTypesFromAssocCache.clear();
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
			String overrideKey = computeOverrideKey(attributeDefinition, nodeType);
			String title = messageService.getMessage(overrideKey + TITLE_SUFFIX);
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
			String overrideKey = computeOverrideKey(attributeDefinition, nodeType);
			String description = messageService.getMessage(overrideKey + DESCRIPTION_SUFFIX);
			if (description != null && !description.isBlank()) {
				return description;
			}
		}
		return attributeDefinition.getDescription(this);
	}

	/** {@inheritDoc} */
	@Override
	public QName getAssocIndexQName(QName assocQName) {
		QName indexPropertyQName = QName.createQName(assocQName.getNamespaceURI(), assocQName.getLocalName() + "AssocIndex");
		ClassAttributeDefinition indexProp = getPropDef(indexPropertyQName);
		return indexProp != null ? indexProp.getName() : null;
	}

	/** {@inheritDoc} */
	@Override
	public ClassDefinition getClass(QName name) {
		if (name == null) {
			return null;
		}
		Map<QName, ClassDefinition> cache = getTenantCache(classDefCache);
		ClassDefinition classDef = cache.get(name);
		if (classDef == null) {
			classDef = super.getClass(name);
			if (classDef != null) {
				cache.put(name, classDef);
			}
		}
		return classDef;
	}

	// Private helper methods

	/**
	 * Computes the actual isSubClass logic separated for better readability
	 *
	 * @param className a {@link org.alfresco.service.namespace.QName} object
	 * @param ofClassName a {@link org.alfresco.service.namespace.QName} object
	 * @return a boolean
	 */
	private boolean computeIsSubClass(QName className, QName ofClassName) {
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

		if (classDef.isAspect() != ofClassDef.isAspect()) {
			return false;
		}

		while (classDef != null) {
			if (classDef.equals(ofClassDef)) {
				return true;
			}
			QName parentClassName = classDef.getParentName();
			classDef = (parentClassName == null) ? null : getClass(parentClassName);
		}

		return false;
	}

	/**
	 * Computes override keys with caching to avoid repeated string operations
	 *
	 * @param attributeDefinition a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link java.lang.String} object
	 */
	private String computeOverrideKey(ClassAttributeDefinition attributeDefinition, QName nodeType) {
		String key = nodeType.toString() + "|" + attributeDefinition.getName().toString();

		return overrideKeyCache.computeIfAbsent(key, k -> {
			String nodeTypePrefix = nodeType.toPrefixString(namespaceService).replace(":", COLON_REPLACEMENT);
			String attrNamePrefix = attributeDefinition.getName().toPrefixString(namespaceService).replace(":", COLON_REPLACEMENT);
			return MODEL_OVERRIDE_PREFIX + nodeTypePrefix + "." + attrNamePrefix;
		});
	}
}
