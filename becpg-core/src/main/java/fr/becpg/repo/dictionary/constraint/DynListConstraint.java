/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.dictionary.constraint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;

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

	private static Log logger = LogFactory.getLog(DynListConstraint.class);

	private static ServiceRegistry serviceRegistry;

	private List<String> paths = null;

	private String constraintType = null;
	private String constraintProp = null;

	private String level = null;
	private String levelProp = null;

	private Boolean addEmptyValue = null;

	private List<String> allowedValues;

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint#
	 * getAllowedValues()
	 */
	@Override
	public List<String> getAllowedValues() {

		if(allowedValues==null) {
			allowedValues = serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<String>>() {
				@Override
				public List<String> execute() throws Throwable {
	
					List<String> allowedValues = new ArrayList<String>();
	
					for (String path : paths) {
						NamespaceService namespaceService = serviceRegistry.getNamespaceService();
						List<String> values = getAllowedValues(path, QName.createQName(constraintType, namespaceService), QName.createQName(constraintProp, namespaceService));
						allowedValues.addAll(values);
					}
					
					if (addEmptyValue != null && addEmptyValue && !allowedValues.contains("")) {
						allowedValues.add("");
					}
	
					return allowedValues;
	
				}
			}, true, false);
	
			if (allowedValues.isEmpty()) {
				allowedValues.add(UNDIFINED_CONSTRAINT_VALUE);
			}
		}

		return allowedValues;

	}

	/**
	 * @see org.alfresco.repo.dictionary.constraint.AbstractConstraint#evaluateSingleValue(java.lang.Object)
	 */
	@Override
	protected void evaluateSingleValue(Object value) {
		// convert the value to a String
		String valueStr = null;
		try {
			valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
		} catch (TypeConversionException e) {
			throw new ConstraintException(ERR_NON_STRING, value);
		}

		if (allowedValues == null) {
			getAllowedValues();
		}

		if (!allowedValues.contains(valueStr)) {
			throw new ConstraintException(ERR_INVALID_VALUE, value);
		}

	}

	private List<String> getAllowedValues(final String path, final QName constraintType, final QName constraintProp) {

		return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<List<String>>() {
			@Override
			public List<String> doWork() throws Exception {
				List<String> allowedValues = new ArrayList<String>();
				String encodedPath = LuceneHelper.encodePath(path.substring(1));

				String queryPath = String.format(RepoConsts.PATH_QUERY_LIST_CONSTRAINTS, encodedPath, constraintType);

				ResultSet resultSet = null;
				SearchParameters sp = new SearchParameters();
				sp.addStore(RepoConsts.SPACES_STORE);
				sp.setLanguage(SearchService.LANGUAGE_LUCENE);
				sp.setQuery(queryPath);
				sp.setLimitBy(LimitBy.UNLIMITED);
				sp.addLocale(Locale.getDefault());
				sp.setPermissionEvaluation(PermissionEvaluationMode.EAGER);
				sp.excludeDataInTheCurrentTransaction(false);
				sp.addSort("@" + BeCPGModel.PROP_SORT, true);
				try {
					resultSet = serviceRegistry.getSearchService().query(sp);
					if (logger.isDebugEnabled()) {
						logger.debug("queryPath : " + queryPath);
						logger.debug("resultSet.length() : " + resultSet.length());
					}

					if (resultSet.length() != 0) {
						for (ResultSetRow row : resultSet) {
							NodeRef nodeRef = row.getNodeRef();
							if (serviceRegistry.getNodeService().exists(nodeRef)) {
								String value = (String) serviceRegistry.getNodeService().getProperty(nodeRef, constraintProp);
								if (!allowedValues.contains(value) && value != null && checkLevel(nodeRef)) {
									allowedValues.add(value);
								}
							} else {
								logger.warn("Node doesn't exist : " + nodeRef);
							}
						}
					}
					if (logger.isDebugEnabled()) {
						logger.debug("allowedValues.size() : " + allowedValues.size());
						logger.debug("allowed values: " + allowedValues.toString());
					}
					return allowedValues;
				} finally {
					if (resultSet != null)
						resultSet.close();
				}
			}

			private boolean checkLevel(NodeRef nodeRef) {
				if (level != null) {
					try {
						int l = Integer.parseInt(level);

						return l == computeLevel(nodeRef, QName.createQName(levelProp, serviceRegistry.getNamespaceService()));

					} catch (Exception e) {
						logger.warn("Cannot check level", e);
					}
				}

				return true;
			}

			private int computeLevel(NodeRef nodeRef, QName createQName) {
				Set<QName> qnames = new HashSet<QName>();
				qnames.add(createQName);

				NodeRef parentNode = (NodeRef) serviceRegistry.getNodeService().getProperty(nodeRef, createQName);
				if (parentNode != null) {
					return 1 + computeLevel(parentNode, createQName);
				}

				return 0;
			}
		}, AuthenticationUtil.getSystemUserName());
	}

}
