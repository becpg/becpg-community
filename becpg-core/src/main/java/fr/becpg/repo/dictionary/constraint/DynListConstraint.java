/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.dictionary.constraint;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Class used to load the dynamic constraints.
 * 
 * @author querephi
 */
public class DynListConstraint extends ListOfValuesConstraint {

	public static final String UNDIFINED_CONSTRAINT_VALUE = "-";
	private static final String ERR_NO_VALUES = "d_dictionary.constraint.list_of_values.no_values";
	private static final String ERR_NON_STRING = "d_dictionary.constraint.string_length.non_string";
	private static final String ERR_INVALID_VALUE = "d_dictionary.constraint.list_of_values.invalid_value";

	private static final Log logger = LogFactory.getLog(DynListConstraint.class);

	private static ServiceRegistry serviceRegistry;

	private List<String> paths = null;

	private String constraintType = null;
	private String constraintProp = null;

	private String level = null;
	private String levelProp = null;

	private Boolean addEmptyValue = null;

	private final Map<String, Map<String, MLText>> allowedValues = new HashMap<>();

	public void setPath(List<String> paths) {

		if (paths == null) {
			throw new DictionaryException(ERR_NO_VALUES);
		}
		int valueCount = paths.size();
		if (valueCount == 0) {
			throw new DictionaryException(ERR_NO_VALUES);
		}
		this.paths = paths;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		DynListConstraint.serviceRegistry = serviceRegistry;
	}

	public void setConstraintType(String constraintType) {
		this.constraintType = constraintType;
	}

	public void setConstraintProp(String constraintProp) {
		this.constraintProp = constraintProp;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public void setLevelProp(String levelProp) {
		this.levelProp = levelProp;
	}

	public void setAddEmptyValue(Boolean addEmptyValue) {
		this.addEmptyValue = addEmptyValue;
	}

	@Override
	public void initialize() {
		checkPropertyNotNull("paths", paths);
		checkPropertyNotNull("constraintType", constraintType);
		checkPropertyNotNull("constraintProp", constraintProp);
		if (level != null) {
			checkPropertyNotNull("levelProp", levelProp);
		}
		logger.debug("Initialize DynListConstraint for " + paths + " " + constraintType);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint#
	 * getAllowedValues()
	 */
	@Override
	public List<String> getAllowedValues() {
		if (MTDictionnarySupport.shouldCleanConstraint() || allowedValues.get(TenantUtil.getCurrentDomain()) == null) {
			allowedValues.put(TenantUtil.getCurrentDomain(),
					serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Map<String, MLText>>() {
						@Override
						public Map<String, MLText> execute() throws Throwable {

							Map<String, MLText> allowedValues = new LinkedHashMap<String, MLText>();

							if (addEmptyValue != null && addEmptyValue) {
								allowedValues.put("", null);
							}

							for (String path : paths) {
								boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);

								try {

									NamespaceService namespaceService = serviceRegistry.getNamespaceService();
									Map<String, MLText> values = getAllowedValues(path, QName.createQName(constraintType,
											namespaceService), QName.createQName(constraintProp, namespaceService));
									allowedValues.putAll(values);

								} finally {
									MLPropertyInterceptor.setMLAware(wasMLAware);
								}
							}

							return allowedValues;

						}
					}, true, false));

			if (allowedValues.get(TenantUtil.getCurrentDomain()).isEmpty()) {
				allowedValues.get(TenantUtil.getCurrentDomain()).put(UNDIFINED_CONSTRAINT_VALUE, null);
			}

			logger.debug("Fill allowedValues  for :" + TenantUtil.getCurrentDomain());
		} 
		return new LinkedList<String>(allowedValues.get(TenantUtil.getCurrentDomain()).keySet());
	}

	/**
	 * @see org.alfresco.repo.dictionary.constraint.AbstractConstraint#evaluateSingleValue(java.lang.Object)
	 */
	@Override
	protected void evaluateSingleValue(Object value) {
		// convert the value to a String
		String valueStr;
		try {
			valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
		} catch (TypeConversionException e) {
			throw new ConstraintException(ERR_NON_STRING, value);
		}

		if (!getAllowedValues().contains(valueStr)) {
			throw new ConstraintException(ERR_INVALID_VALUE, value);
		}

	}

	private Map<String, MLText> getAllowedValues(final String path, final QName constraintType, final QName constraintProp) {

		return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Map<String, MLText>>() {
			@Override
			public Map<String, MLText> doWork() throws Exception {
				Map<String, MLText> allowedValues = new LinkedHashMap<>();

				try {
					List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().selectNodesByPath(serviceRegistry.getNodeService().getRootNode(RepoConsts.SPACES_STORE),
							"/app:company_home/" + BeCPGQueryBuilder.encodePath(path) + "/*");

					Collections.sort(nodeRefs, new Comparator<NodeRef>() {

						@Override
						public int compare(NodeRef o1, NodeRef o2) {
							Integer sort1 = (Integer) serviceRegistry.getNodeService().getProperty(o1, BeCPGModel.PROP_SORT);
							Integer sort2 = (Integer) serviceRegistry.getNodeService().getProperty(o2, BeCPGModel.PROP_SORT);
							if (sort1 == null && sort2 == null) {
								return 0;
							}
							if (sort1 == null) {
								return -1;
							}
							if (sort2 == null) {
								return 1;
							}

							return sort1.compareTo(sort2);
						}

					});

					for (NodeRef nodeRef : nodeRefs) {
						if (serviceRegistry.getNodeService().exists(nodeRef) && serviceRegistry.getNodeService().getType(nodeRef).equals(constraintType)) {
							MLText mlText = (MLText) serviceRegistry.getNodeService().getProperty(nodeRef, constraintProp);

							if (mlText != null) {
								allowedValues.put(mlText.getDefaultValue(), mlText);
							}
						} else {
							logger.warn("Node doesn't exist : " + nodeRef);
						}
					}

					if (logger.isDebugEnabled()) {
						logger.debug("allowedValues.size() : " + allowedValues.size());
						logger.debug("allowed values: " + allowedValues.toString());
					}
				} catch (InvalidStoreRefException e) {
					logger.warn("Please reload constraint once tenant created: " + e.getMessage());
				}

				return allowedValues;

			}

		});
	}

	@Override
	public String getDisplayLabel(String constraintAllowableValue, MessageLookup messageLookup) {

		if (!allowedValues.get(TenantUtil.getCurrentDomain()).containsKey(constraintAllowableValue)) {
			return null;
		}
		MLText mlText = allowedValues.get(TenantUtil.getCurrentDomain()).get(constraintAllowableValue);
		
		if (mlText != null && mlText.getClosestValue(I18NUtil.getLocale()) != null && !mlText.getClosestValue(I18NUtil.getLocale()).isEmpty()) {
			return mlText.getClosestValue(I18NUtil.getLocale());
		}

		return constraintAllowableValue;
	}

}
