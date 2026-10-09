package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
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
    /** Constant <code>NAME_TEMPLATE="%s - %s"</code> */
    private static final String NAME_TEMPLATE = "%s - %s";
    /** Constant <code>DEFAULT_AUTO_NUM</code> */
    private static final Long DEFAULT_AUTO_NUM = 1L;
    /** Constant <code>PREFIX_MSG_PREFIX="autonum.prefix."</code> */
    private static final String PREFIX_MSG_PREFIX = "autonum.prefix.";
    /** Constant <code>DEFAULT_PREFIX=""</code> */
    private static final String DEFAULT_PREFIX = "";
    /** Constant <code>DEFAULT_PATTERN</code> */
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("(^[A-Z]+)(\\d+$)");
    /** Constant <code>CACHE_KEY_SEPARATOR="-"</code> */
    private static final String CACHE_KEY_SEPARATOR = "-";
    
    /** Constant <code>logger</code> */
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

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private NodeDAO nodeDAO;

    /** {@inheritDoc} */
    @Override
    public String getAutoNumValue(QName className, QName propertyName) {
        validateInputs(className, propertyName);

        return writeCounter(className, propertyName, () -> generateNextAutoNumValue(className, propertyName));
    }

    /** {@inheritDoc} */
    @Override
    public boolean setAutoNumValue(QName className, QName propertyName, Long counter) {
        validateInputs(className, propertyName);

        if (counter == null) {
            logger.warn("Attempted to set null counter for " + className + "." + propertyName);
            return false;
        }

        return writeCounter(className, propertyName, () -> updateCounterValue(className, propertyName, counter));
    }

    /** {@inheritDoc} */
    @Override
    public void deleteAutoNumValue(QName className, QName propertyName) {
        validateInputs(className, propertyName);

        writeCounter(className, propertyName, () -> {
            deleteCounter(className, propertyName);
            return null;
        });
    }

    /** {@inheritDoc} */
    @Override
    public String getAutoNumMatchPattern(QName type, QName propertyName) {
        validateInputs(type, propertyName);
        
        String prefix = buildPrefixPattern(type, propertyName);
        return createMatchPattern(prefix);
    }

    /** {@inheritDoc} */
    @Override
    public String getPrefixedCode(QName type, QName propertyName, Long autoNumValue) {
        validateInputs(type, propertyName);
        
        if (autoNumValue == null) {
            throw new IllegalArgumentException("autoNumValue cannot be null");
        }
        
        String prefix = getAutoNumPrefix(type, propertyName);
        return formatCode(prefix, autoNumValue);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public String getOrCreateBeCPGCode(NodeRef nodeRef) {
        return getOrCreateCode(nodeRef, BeCPGModel.PROP_CODE);
    }

    /** {@inheritDoc} */
    @Override
    public NodeRef getAutoNumNodeRef(QName className, QName propertyName) {
        return findAutoNumNodeRef(className, propertyName).orElse(null);
    }

    // Private helper methods
    
    /**
     * <p>validateInputs.</p>
     *
     * @param className a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     */
    private void validateInputs(QName className, QName propertyName) {
        if (className == null) {
            throw new IllegalArgumentException("className cannot be null");
        }
        if (propertyName == null) {
            throw new IllegalArgumentException("propertyName cannot be null");
        }
    }

    /**
     * Runs a write on a counter node under the lock of that counter, in its own short transaction
     * whenever the counter is already committed.
     * <p>
     * The database row lock taken by the write is then released when the inner transaction commits,
     * before the Java lock is released. A caller transaction therefore never holds the counter row
     * while it waits for the Java lock: otherwise two transactions coding several entities each
     * wait for the other one, a cycle neither the JVM nor the database can detect, that only ends
     * with the lock wait timeout.
     * <p>
     * The write stays in the caller transaction when the counter does not exist yet, or when the
     * caller transaction has already written it: the inner transaction cannot see the counter, or
     * the folders it lives in, before the caller commits, and would wait for a row locked by its own
     * suspended caller.
     * <p>
     * A counter value consumed by a caller transaction that rolls back or retries is lost, leaving a
     * gap in the numbering.
     *
     * @param <T> the type returned by the write
     * @param className the class the counter belongs to
     * @param propertyName the property the counter numbers
     * @param write the write to run on the counter node
     * @return the value returned by the write
     */
    private <T> T writeCounter(QName className, QName propertyName, Supplier<T> write) {
        ReentrantLock lock = lockMap.computeIfAbsent(createLockKey(className, propertyName), k -> new ReentrantLock());

        lock.lock();
        try {
            if (isCommittedByAnotherTransaction(className, propertyName)) {
                return transactionService.getRetryingTransactionHelper().doInTransaction(() -> writeWithoutAuditing(write), false, true);
            }
            return write.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Tells whether the counter exists and was last written by a transaction other than the current one.
     *
     * @param className the class the counter belongs to
     * @param propertyName the property the counter numbers
     * @return true if the counter can be written in its own transaction
     */
    private boolean isCommittedByAnotherTransaction(QName className, QName propertyName) {
        return findAutoNumNodeRef(className, propertyName)
            .filter(nodeService::exists)
            .map(nodeRef -> !nodeDAO.isInCurrentTxn(nodeDAO.getNodeRefStatus(nodeRef).getDbId()))
            .orElse(false);
    }

    /**
     * Runs a counter write without updating the auditable properties of the counter, as the caller
     * transaction does for the entity it codes.
     *
     * @param <T> the type returned by the write
     * @param write the write to run on the counter node
     * @return the value returned by the write
     */
    private <T> T writeWithoutAuditing(Supplier<T> write) {
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        return write.get();
    }

    /**
     * Sets the value of an existing counter.
     *
     * @param className the class the counter belongs to
     * @param propertyName the property the counter numbers
     * @param counter the new value of the counter
     * @return true if the counter exists and was updated, false otherwise
     */
    private boolean updateCounterValue(QName className, QName propertyName, Long counter) {
        Optional<NodeRef> autoNumNodeRef = findAutoNumNodeRef(className, propertyName).filter(nodeService::exists);

        if (autoNumNodeRef.isEmpty()) {
            return false;
        }

        nodeService.setProperty(autoNumNodeRef.get(), BeCPGModel.PROP_AUTO_NUM_VALUE, counter);
        if (logger.isDebugEnabled()) {
            logger.debug("Updated autonum value to " + counter + " for " + className + "." + propertyName);
        }
        return true;
    }

    /**
     * Deletes a counter node and evicts it from the cache.
     *
     * @param className the class the counter belongs to
     * @param propertyName the property the counter numbers
     */
    private void deleteCounter(QName className, QName propertyName) {
        findAutoNumNodeRef(className, propertyName).ifPresent(nodeRef -> {
            beCPGCacheService.removeFromCache(AutoNumServiceImpl.class.getName(), createCacheKey(className, propertyName));

            if (nodeService.exists(nodeRef)) {
                nodeService.deleteNode(nodeRef);
                if (logger.isDebugEnabled()) {
                    logger.debug("Deleted autonum node for " + className + "." + propertyName);
                }
            }
        });
    }

    /**
     * <p>createLockKey.</p>
     *
     * @param className a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @return a {@link java.lang.String} object
     */
    private String createLockKey(QName className, QName propertyName) {
        return className.toString() + CACHE_KEY_SEPARATOR + propertyName.toString();
    }

    /**
     * <p>createCacheKey.</p>
     *
     * @param className a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @return a {@link java.lang.String} object
     */
    private String createCacheKey(QName className, QName propertyName) {
        return className.toString() + CACHE_KEY_SEPARATOR + propertyName.toString();
    }

    /**
     * <p>generateNextAutoNumValue.</p>
     *
     * @param className a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @return a {@link java.lang.String} object
     */
    private String generateNextAutoNumValue(QName className, QName propertyName) {
        Optional<NodeRef> autoNumNodeRef = findAutoNumNodeRef(className, propertyName);
        
        if (autoNumNodeRef.isPresent() && nodeService.exists(autoNumNodeRef.get())) {
            return incrementExistingAutoNum(autoNumNodeRef.get());
        } else {
            return createNewAutoNum(className, propertyName);
        }
    }

    /**
     * <p>incrementExistingAutoNum.</p>
     *
     * @param autoNumNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @return a {@link java.lang.String} object
     */
    private String incrementExistingAutoNum(NodeRef autoNumNodeRef) {
        Long currentValue = (Long) nodeService.getProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE);
        String prefix = getPrefix(autoNumNodeRef, DEFAULT_PREFIX);
        
        Long nextValue = (currentValue != null) ? currentValue + 1 : DEFAULT_AUTO_NUM;
        nodeService.setProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE, nextValue);
        
        return formatCode(prefix, nextValue);
    }

    /**
     * <p>createNewAutoNum.</p>
     *
     * @param className a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @return a {@link java.lang.String} object
     */
    private String createNewAutoNum(QName className, QName propertyName) {
        String prefix = getDefaultPrefix(className, propertyName);
        createAutoNum(className, propertyName, DEFAULT_AUTO_NUM, prefix);
        return formatCode(prefix, DEFAULT_AUTO_NUM);
    }

    /**
     * <p>findAutoNumNodeRef.</p>
     *
     * @param className a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @return a {@link java.util.Optional} object
     */
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

    /**
     * <p>buildPrefixPattern.</p>
     *
     * @param type a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @return a {@link java.lang.String} object
     */
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

    /**
     * <p>createMatchPattern.</p>
     *
     * @param prefix a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    private String createMatchPattern(String prefix) {
        return "(^" + prefix + ")(\\d*$)";
    }

    /**
     * <p>getAutoNumPrefix.</p>
     *
     * @param type a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @return a {@link java.lang.String} object
     */
    private String getAutoNumPrefix(QName type, QName propertyName) {
        return findAutoNumNodeRef(type, propertyName)
            .map(nodeRef -> getPrefix(nodeRef, DEFAULT_PREFIX))
            .orElse(getDefaultPrefix(type, propertyName));
    }

    /**
     * <p>processNodeCode.</p>
     *
     * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param codeQName a {@link org.alfresco.service.namespace.QName} object
     * @return a {@link java.lang.String} object
     */
    private String processNodeCode(NodeRef nodeRef, QName codeQName) {
        String existingCode = (String) nodeService.getProperty(nodeRef, codeQName);
        QName typeQName = nodeService.getType(nodeRef);
        
        if (existingCode != null && !existingCode.isEmpty()) {
            boolean codeExists = isCodeAlreadyUsed(typeQName, codeQName, existingCode, nodeRef);
            
            if (codeExists) {
                return generateAndSetNewCode(nodeRef, typeQName, codeQName);
            } else {
                writeCounter(typeQName, codeQName, () -> {
                    createOrUpdateAutoNumValue(typeQName, codeQName, existingCode);
                    return null;
                });
                return existingCode;
            }
        } else {
            return generateAndSetNewCode(nodeRef, typeQName, codeQName);
        }
    }

    /**
     * <p>isCodeAlreadyUsed.</p>
     *
     * @param typeQName a {@link org.alfresco.service.namespace.QName} object
     * @param codeQName a {@link org.alfresco.service.namespace.QName} object
     * @param code a {@link java.lang.String} object
     * @param excludeNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @return a boolean
     */
    private boolean isCodeAlreadyUsed(QName typeQName, QName codeQName, String code, NodeRef excludeNodeRef) {
        return BeCPGQueryBuilder.createQuery()
            .ofType(typeQName)
            .andPropEquals(codeQName, code)
            .andNotID(excludeNodeRef)
            .inDB()
            .singleValue() != null;
    }

    /**
     * <p>generateAndSetNewCode.</p>
     *
     * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param typeQName a {@link org.alfresco.service.namespace.QName} object
     * @param codeQName a {@link org.alfresco.service.namespace.QName} object
     * @return a {@link java.lang.String} object
     */
    private String generateAndSetNewCode(NodeRef nodeRef, QName typeQName, QName codeQName) {
        String newCode = getAutoNumValue(typeQName, codeQName);
        nodeService.setProperty(nodeRef, codeQName, newCode);
        return newCode;
    }

    /**
     * <p>createOrUpdateAutoNumValue.</p>
     *
     * @param className a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @param autoNumCode a {@link java.lang.String} object
     */
    private void createOrUpdateAutoNumValue(QName className, QName propertyName, String autoNumCode) {
        AutoNumInfo autoNumInfo = parseAutoNumCode(autoNumCode, className, propertyName);
        
        Optional<NodeRef> autoNumNodeRef = findAutoNumNodeRef(className, propertyName);
        
        if (autoNumNodeRef.isPresent() && nodeService.exists(autoNumNodeRef.get())) {
            updateExistingAutoNumIfNecessary(autoNumNodeRef.get(), autoNumInfo.value);
        } else {
            createAutoNum(className, propertyName, autoNumInfo.value, autoNumInfo.prefix);
        }
    }

    /**
     * <p>parseAutoNumCode.</p>
     *
     * @param autoNumCode a {@link java.lang.String} object
     * @param className a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @return a {@link fr.becpg.repo.entity.impl.AutoNumServiceImpl.AutoNumInfo} object
     */
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

    /**
     * <p>updateExistingAutoNumIfNecessary.</p>
     *
     * @param autoNumNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param newValue a {@link java.lang.Long} object
     */
    private void updateExistingAutoNumIfNecessary(NodeRef autoNumNodeRef, Long newValue) {
        Long currentValue = (Long) nodeService.getProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE);
        
        if (currentValue == null || currentValue < newValue) {
            nodeService.setProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE, newValue);
            if (logger.isDebugEnabled()) {
                logger.debug("Updated autonum value from " + currentValue + " to " + newValue);
            }
        }
    }

    /**
     * <p>getDefaultPrefix.</p>
     *
     * @param className a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @return a {@link java.lang.String} object
     */
    private String getDefaultPrefix(QName className, QName propertyName) {
        String messageKey = PREFIX_MSG_PREFIX + className.getLocalName() + "." + propertyName.getLocalName();
        String prefix = I18NUtil.getMessage(messageKey);
        
        return (prefix != null && !prefix.isEmpty()) ? prefix : DEFAULT_PREFIX;
    }

    /**
     * <p>createAutoNum.</p>
     *
     * @param className a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @param autoNumValue a {@link java.lang.Long} object
     * @param autoNumPrefix a {@link java.lang.String} object
     * @return a {@link java.lang.Long} object
     */
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

    /**
     * <p>createAutoNumProperties.</p>
     *
     * @param name a {@link java.lang.String} object
     * @param className a {@link org.alfresco.service.namespace.QName} object
     * @param propertyName a {@link org.alfresco.service.namespace.QName} object
     * @param autoNumValue a {@link java.lang.Long} object
     * @param autoNumPrefix a {@link java.lang.String} object
     * @return a {@link java.util.Map} object
     */
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

    /**
     * <p>getPrefix.</p>
     *
     * @param autoNumNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param defaultPrefix a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    private String getPrefix(NodeRef autoNumNodeRef, String defaultPrefix) {
        String prefix = (String) nodeService.getProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_PREFIX);
        return (prefix != null) ? prefix : defaultPrefix;
    }

    /**
     * <p>formatCode.</p>
     *
     * @param prefix a {@link java.lang.String} object
     * @param autoNumValue a {@link java.lang.Long} object
     * @return a {@link java.lang.String} object
     */
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
