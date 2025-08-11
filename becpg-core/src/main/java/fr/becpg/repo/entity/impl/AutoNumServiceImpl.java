package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Enhanced implementation of AutoNumService with improved thread safety,
 * error handling, and performance optimizations.
 *
 * @author querephi
 * @version $Id: $Id
 */
@Service("autoNumService")
public class AutoNumServiceImpl implements AutoNumService {

    // Constants
    private static final String NAME_TEMPLATE = "%s - %s";
    private static final Long DEFAULT_AUTO_NUM = 1L;
    private static final String PREFIX_MSG_PREFIX = "autonum.prefix.";
    private static final String DEFAULT_PREFIX = "";
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("(^[A-Z]+)(\\d+$)");
    private static final String CACHE_KEY_SEPARATOR = "-";
    
    private static final Log logger = LogFactory.getLog(AutoNumServiceImpl.class);

    // Fine-grained locking for better performance
    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Autowired
    private NodeService nodeService;

    @Autowired
    private RepoService repoService;

    @Autowired
    private Repository repositoryHelper;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private BeCPGCacheService beCPGCacheService;

    @Autowired
    private BehaviourFilter policyBehaviourFilter;

    @Override
    public String getAutoNumValue(QName className, QName propertyName) {
        validateInputs(className, propertyName);
        
        String lockKey = createLockKey(className, propertyName);
        ReentrantLock lock = lockMap.computeIfAbsent(lockKey, k -> new ReentrantLock());
        
        lock.lock();
        try {
            return generateNextAutoNumValue(className, propertyName);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean setAutoNumValue(QName className, QName propertyName, Long counter) {
        validateInputs(className, propertyName);
        
        if (counter == null) {
            logger.warn("Attempted to set null counter for " + className + "." + propertyName);
            return false;
        }

        Optional<NodeRef> autoNumNodeRef = findAutoNumNodeRef(className, propertyName);
        
        return autoNumNodeRef
            .filter(nodeService::exists)
            .map(nodeRef -> {
                nodeService.setProperty(nodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE, counter);
                if (logger.isDebugEnabled()) {
                logger.debug("Updated autonum value to " + counter + " for " + className + "." + propertyName);
            }
                return true;
            })
            .orElse(false);
    }

    @Override
    public void deleteAutoNumValue(QName className, QName propertyName) {
        validateInputs(className, propertyName);
        
        Optional<NodeRef> autoNumNodeRef = findAutoNumNodeRef(className, propertyName);
        
        autoNumNodeRef.ifPresent(nodeRef -> {
            String cacheKey = createCacheKey(className, propertyName);
            beCPGCacheService.removeFromCache(AutoNumServiceImpl.class.getName(), cacheKey);
            
            if (nodeService.exists(nodeRef)) {
                nodeService.deleteNode(nodeRef);
                if (logger.isDebugEnabled()) {
                    logger.debug("Deleted autonum node for " + className + "." + propertyName);
                }
            }
        });
    }

    @Override
    public String getAutoNumMatchPattern(QName type, QName propertyName) {
        validateInputs(type, propertyName);
        
        String prefix = buildPrefixPattern(type, propertyName);
        return createMatchPattern(prefix);
    }

    @Override
    public String getPrefixedCode(QName type, QName propertyName, Long autoNumValue) {
        validateInputs(type, propertyName);
        
        if (autoNumValue == null) {
            throw new IllegalArgumentException("autoNumValue cannot be null");
        }
        
        String prefix = getAutoNumPrefix(type, propertyName);
        return formatCode(prefix, autoNumValue);
    }

    @Override
    public String getOrCreateCode(NodeRef nodeRef, QName codeQName) {
        if (nodeRef == null || codeQName == null) {
            throw new IllegalArgumentException("nodeRef and codeQName cannot be null");
        }

        boolean wasEnabledBehaviour = policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE);
        
        try {
            policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
            return processNodeCode(nodeRef, codeQName);
        } finally {
            if (wasEnabledBehaviour) {
                policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            }
        }
    }

    @Override
    public String getOrCreateBeCPGCode(NodeRef nodeRef) {
        return getOrCreateCode(nodeRef, BeCPGModel.PROP_CODE);
    }

    @Override
    public NodeRef getAutoNumNodeRef(QName className, QName propertyName) {
        return findAutoNumNodeRef(className, propertyName).orElse(null);
    }

    // Private helper methods
    
    private void validateInputs(QName className, QName propertyName) {
        if (className == null) {
            throw new IllegalArgumentException("className cannot be null");
        }
        if (propertyName == null) {
            throw new IllegalArgumentException("propertyName cannot be null");
        }
    }

    private String createLockKey(QName className, QName propertyName) {
        return className.toString() + CACHE_KEY_SEPARATOR + propertyName.toString();
    }

    private String createCacheKey(QName className, QName propertyName) {
        return className.toString() + CACHE_KEY_SEPARATOR + propertyName.toString();
    }

    private String generateNextAutoNumValue(QName className, QName propertyName) {
        Optional<NodeRef> autoNumNodeRef = findAutoNumNodeRef(className, propertyName);
        
        if (autoNumNodeRef.isPresent() && nodeService.exists(autoNumNodeRef.get())) {
            return incrementExistingAutoNum(autoNumNodeRef.get());
        } else {
            return createNewAutoNum(className, propertyName);
        }
    }

    private String incrementExistingAutoNum(NodeRef autoNumNodeRef) {
        Long currentValue = (Long) nodeService.getProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE);
        String prefix = getPrefix(autoNumNodeRef, DEFAULT_PREFIX);
        
        Long nextValue = (currentValue != null) ? currentValue + 1 : DEFAULT_AUTO_NUM;
        nodeService.setProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE, nextValue);
        
        return formatCode(prefix, nextValue);
    }

    private String createNewAutoNum(QName className, QName propertyName) {
        String prefix = getDefaultPrefix(className, propertyName);
        createAutoNum(className, propertyName, DEFAULT_AUTO_NUM, prefix);
        return formatCode(prefix, DEFAULT_AUTO_NUM);
    }

    private Optional<NodeRef> findAutoNumNodeRef(QName className, QName propertyName) {
        String cacheKey = createCacheKey(className, propertyName);
        
        return Optional.ofNullable(
            beCPGCacheService.getFromCache(
                AutoNumServiceImpl.class.getName(), 
                cacheKey,
                () -> BeCPGQueryBuilder.createQuery()
                    .ofType(BeCPGModel.TYPE_AUTO_NUM)
                    .andPropEquals(BeCPGModel.PROP_AUTO_NUM_CLASS_NAME, className.toString())
                    .andPropEquals(BeCPGModel.PROP_AUTO_NUM_PROPERTY_NAME, propertyName.toString())
                    .inDB()
                    .ftsLanguage()
                    .singleValue()
            )
        );
    }

    private String buildPrefixPattern(QName type, QName propertyName) {
        var subTypes = dictionaryService.getSubTypes(type, true);
        
        if (subTypes.isEmpty()) {
            return getAutoNumPrefix(type, propertyName);
        }
        
        return subTypes.stream()
            .map(subType -> getAutoNumPrefix(subType, propertyName))
            .reduce((prefix1, prefix2) -> prefix1 + "|" + prefix2)
            .orElse(DEFAULT_PREFIX);
    }

    private String createMatchPattern(String prefix) {
        return "(^" + prefix + ")(\\d*$)";
    }

    private String getAutoNumPrefix(QName type, QName propertyName) {
        return findAutoNumNodeRef(type, propertyName)
            .map(nodeRef -> getPrefix(nodeRef, DEFAULT_PREFIX))
            .orElse(getDefaultPrefix(type, propertyName));
    }

    private String processNodeCode(NodeRef nodeRef, QName codeQName) {
        String existingCode = (String) nodeService.getProperty(nodeRef, codeQName);
        QName typeQName = nodeService.getType(nodeRef);
        
        if (existingCode != null && !existingCode.isEmpty()) {
            boolean codeExists = isCodeAlreadyUsed(typeQName, codeQName, existingCode, nodeRef);
            
            if (codeExists) {
                return generateAndSetNewCode(nodeRef, typeQName, codeQName);
            } else {
                createOrUpdateAutoNumValue(typeQName, codeQName, existingCode);
                return existingCode;
            }
        } else {
            return generateAndSetNewCode(nodeRef, typeQName, codeQName);
        }
    }

    private boolean isCodeAlreadyUsed(QName typeQName, QName codeQName, String code, NodeRef excludeNodeRef) {
        return BeCPGQueryBuilder.createQuery()
            .ofType(typeQName)
            .andPropEquals(codeQName, code)
            .andNotID(excludeNodeRef)
            .inDB()
            .singleValue() != null;
    }

    private String generateAndSetNewCode(NodeRef nodeRef, QName typeQName, QName codeQName) {
        String newCode = getAutoNumValue(typeQName, codeQName);
        nodeService.setProperty(nodeRef, codeQName, newCode);
        return newCode;
    }

    private void createOrUpdateAutoNumValue(QName className, QName propertyName, String autoNumCode) {
        AutoNumInfo autoNumInfo = parseAutoNumCode(autoNumCode, className, propertyName);
        
        Optional<NodeRef> autoNumNodeRef = findAutoNumNodeRef(className, propertyName);
        
        if (autoNumNodeRef.isPresent() && nodeService.exists(autoNumNodeRef.get())) {
            updateExistingAutoNumIfNecessary(autoNumNodeRef.get(), autoNumInfo.value);
        } else {
            createAutoNum(className, propertyName, autoNumInfo.value, autoNumInfo.prefix);
        }
    }

    private AutoNumInfo parseAutoNumCode(String autoNumCode, QName className, QName propertyName) {
        String prefix = getDefaultPrefix(className, propertyName);
        Long value = DEFAULT_AUTO_NUM;
        
        java.util.regex.Matcher matcher = DEFAULT_PATTERN.matcher(autoNumCode);
        if (matcher.matches()) {
            prefix = matcher.group(1);
            try {
                value = Long.parseLong(matcher.group(2));
            } catch (NumberFormatException e) {
                logger.warn("Cannot parse autoNum value: " + matcher.group(2) + " for " + className + "." + propertyName, e);
            }
        } else {
            try {
                value = Long.parseLong(autoNumCode);
            } catch (NumberFormatException e) {
                logger.warn("Cannot parse autoNum code: " + autoNumCode + " for " + className + "." + propertyName, e);
            }
        }
        
        return new AutoNumInfo(prefix, value);
    }

    private void updateExistingAutoNumIfNecessary(NodeRef autoNumNodeRef, Long newValue) {
        Long currentValue = (Long) nodeService.getProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE);
        
        if (currentValue == null || currentValue < newValue) {
            nodeService.setProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE, newValue);
            if (logger.isDebugEnabled()) {
                logger.debug("Updated autonum value from " + currentValue + " to " + newValue);
            }
        }
    }

    private String getDefaultPrefix(QName className, QName propertyName) {
        String messageKey = PREFIX_MSG_PREFIX + className.getLocalName() + "." + propertyName.getLocalName();
        String prefix = I18NUtil.getMessage(messageKey);
        
        return (prefix != null && !prefix.isEmpty()) ? prefix : DEFAULT_PREFIX;
    }

    private Long createAutoNum(QName className, QName propertyName, Long autoNumValue, String autoNumPrefix) {
        String cacheKey = createCacheKey(className, propertyName);
        beCPGCacheService.removeFromCache(AutoNumServiceImpl.class.getName(), cacheKey);

        NodeRef systemNodeRef = repoService.getOrCreateFolderByPath(
            repositoryHelper.getCompanyHome(), 
            RepoConsts.PATH_SYSTEM,
            TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM)
        );
        
        NodeRef autoNumFolderNodeRef = repoService.getOrCreateFolderByPath(
            systemNodeRef, 
            RepoConsts.PATH_AUTO_NUM,
            TranslateHelper.getTranslatedPath(RepoConsts.PATH_AUTO_NUM)
        );

        String name = String.format(NAME_TEMPLATE, className.getLocalName(), propertyName.getLocalName());
        Map<QName, Serializable> properties = createAutoNumProperties(name, className, propertyName, autoNumValue, autoNumPrefix);
        
        nodeService.createNode(
            autoNumFolderNodeRef, 
            ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), 
            BeCPGModel.TYPE_AUTO_NUM, 
            properties
        );

        if (logger.isDebugEnabled()) {
            logger.debug("Created new autonum node: " + name + " with value: " + autoNumValue + " and prefix: " + autoNumPrefix);
        }
        
        return autoNumValue;
    }

    private Map<QName, Serializable> createAutoNumProperties(String name, QName className, 
            QName propertyName, Long autoNumValue, String autoNumPrefix) {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(BeCPGModel.PROP_AUTO_NUM_CLASS_NAME, className);
        properties.put(BeCPGModel.PROP_AUTO_NUM_PROPERTY_NAME, propertyName);
        properties.put(BeCPGModel.PROP_AUTO_NUM_VALUE, autoNumValue);
        properties.put(BeCPGModel.PROP_AUTO_NUM_PREFIX, autoNumPrefix);
        return properties;
    }

    private String getPrefix(NodeRef autoNumNodeRef, String defaultPrefix) {
        String prefix = (String) nodeService.getProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_PREFIX);
        return (prefix != null) ? prefix : defaultPrefix;
    }

    private String formatCode(String prefix, Long autoNumValue) {
        return prefix + autoNumValue;
    }

    // Helper class for auto number information
    private static class AutoNumInfo {
        final String prefix;
        final Long value;

        AutoNumInfo(String prefix, Long value) {
            this.prefix = prefix;
            this.value = value;
        }
    }
}