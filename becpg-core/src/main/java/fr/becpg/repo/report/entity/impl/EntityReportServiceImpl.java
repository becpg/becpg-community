/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
package fr.becpg.repo.report.entity.impl;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.report.engine.BeCPGReportEngine;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.EntityReportExtractorPlugin;
import fr.becpg.repo.report.entity.EntityReportExtractorPlugin.EntityReportExtractorPriority;
import fr.becpg.repo.report.entity.EntityReportParameters;
import fr.becpg.repo.report.entity.EntityReportParameters.EntityReportParameter;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.report.client.ReportException;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;

/**
 * <p>EntityReportServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("entityReportService")
public class EntityReportServiceImpl implements EntityReportService {

	private static final String PREF_REPORT_PREFIX = "fr.becpg.repo.report.";
	private static final String PREF_REPORT_SUFFIX = ".view";
	private static final String REPORT_PARAM_SEPARATOR = "#";
	private static final String REPORT_LIST_CACHE_KEY = "REPORT_KIND_CACHE_KEY";

	private static final Log logger = LogFactory.getLog(EntityReportServiceImpl.class);

	@Value("${beCPG.report.name.format}")
	private String reportNameFormat;

	@Value("${beCPG.report.title.format}")
	private String reportTitleFormat;
	
	@Value("${beCPG.report.includeReportInSearch}")
	private Boolean includeReportInSearch;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private PreferenceService preferenceService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	@Qualifier("policyBehaviourFilter")
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private ReportTplService reportTplService;

	@Autowired
	private BeCPGReportEngine[] engines;

	@Autowired
	private MimetypeService mimetypeService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private EntityReportExtractorPlugin[] entityExtractors;

	@Autowired
	private EntityService entityService;

	@Autowired
	private EntityActivityService entityActivityService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	protected RepoService repoService;

	@Autowired
	protected EntitySystemService entitySystemService;

	@Autowired
	private BeCPGCacheService beCPGCacheService;

	@Autowired
	private RuleService ruleService;

	/** {@inheritDoc} */
	@Override
	public void generateReports(final NodeRef entityNodeRef) {

		if (entityNodeRef == null) {
			throw new IllegalArgumentException("nodeRef is null");
		}

		L2CacheSupport.doInCacheContext(() -> {

			RunAsWork<Object> actionRunAs = () -> {
				if (nodeService.exists(entityNodeRef)) {
					Locale currentLocal = I18NUtil.getLocale();
					Locale currentContentLocal = I18NUtil.getContentLocale();
					try {
						Locale defaultLocale = MLTextHelper.getNearestLocale(Locale.getDefault());

						I18NUtil.setLocale(defaultLocale);
						I18NUtil.setContentLocale(defaultLocale);

						ruleService.disableRules();
						policyBehaviourFilter.disableBehaviour(entityNodeRef);
						if (logger.isDebugEnabled()) {
							logger.debug("Generate reports for entity: " + entityNodeRef + " - "
									+ nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
						}

						Date generatedDate = Calendar.getInstance().getTime();

						Date modified = (Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_MODIFIED);
						Date formulatedDate = (Date) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_FORMULATED_DATE);

						if ((formulatedDate != null) && (modified != null) && (formulatedDate.getTime() > modified.getTime())) {
							logger.trace("Using formulated date instead of modified");
							modified = formulatedDate;
						}

						Calendar deprecatedDate = Calendar.getInstance();
						deprecatedDate.setTime(modified);
						deprecatedDate.add(Calendar.HOUR, -1);

						List<NodeRef> tplsNodeRef = getReportTplsToGenerate(entityNodeRef);

						tplsNodeRef = reportTplService.cleanDefaultTpls(tplsNodeRef);

						List<NodeRef> newReports = new ArrayList<>();

						if (!tplsNodeRef.isEmpty()) {
							StopWatch watch = null;
							if (logger.isDebugEnabled()) {
								watch = new StopWatch();
								watch.start();
							}

							final NodeRef selectedReportNodeRef = getSelectedReport(entityNodeRef);

							List<Locale> entityReportLocales = getEntityReportLocales(entityNodeRef);

							final Boolean hideDefaultLocal;
							if (!entityReportLocales.contains(defaultLocale)) {
								hideDefaultLocal = true;
								entityReportLocales.add(defaultLocale);
							} else {
								hideDefaultLocal = false;
							}

							for (Locale locale : entityReportLocales) {

								I18NUtil.setLocale(locale);
								I18NUtil.setContentLocale(locale);

								tplsNodeRef.stream().forEach(tplNodeRef -> {

									for (EntityReportParameters reportParameters : getEntityReportParametersList(tplNodeRef, entityNodeRef)) {

										if (isLocaleEnableOnTemplate(tplNodeRef, locale, hideDefaultLocal)) {

											Boolean isDefault = (Boolean) this.nodeService.getProperty(tplNodeRef,
													ReportModel.PROP_REPORT_TPL_IS_DEFAULT);

											// prepare
											String reportFormat = (String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT);
											String documentName = getReportDocumentName(entityNodeRef, tplNodeRef, reportFormat, locale,
													reportParameters, reportParameters.getReportNameFormat(reportNameFormat));

											String documentTitle = getReportDocumentName(entityNodeRef, tplNodeRef, null, locale, reportParameters,
													reportParameters.getReportTitleFormat(reportTitleFormat));

											NodeRef documentNodeRef = getReportDocumenNodeRef(entityNodeRef, tplNodeRef, documentName, locale,
													reportParameters);

											if (documentNodeRef != null) {

												// Run report
												try {
													policyBehaviourFilter.disableBehaviour(documentNodeRef, ContentModel.ASPECT_AUDITABLE);

													ContentWriter writer = contentService.getWriter(documentNodeRef, ContentModel.PROP_CONTENT, true);

													if ((entityReportLocales.size() > 1) && !MLTextHelper.isDefaultLocale(locale)) {
														isDefault = false;
													}

													if (writer != null) {

														if (((selectedReportNodeRef != null) && (documentNodeRef != null)
																&& selectedReportNodeRef.toString().equals(documentNodeRef.toString()))
																|| ((selectedReportNodeRef == null) && Boolean.TRUE.equals(isDefault))) {

															BeCPGReportEngine engine = getReportEngine(tplNodeRef,
																	ReportFormat.valueOf(reportFormat));

															EntityReportData reportData = retrieveExtractor(entityNodeRef, engine)
																	.extract(entityNodeRef, reportParameters.getPreferences());

															reportData.setParameters(reportParameters);

															String mimetype = mimetypeService.guessMimetype(documentName);
															writer.setMimetype(mimetype);
															Map<String, Object> params = new HashMap<>();

															params.put(ReportParams.PARAM_IMAGES, reportData.getDataObjects());
															params.put(ReportParams.PARAM_FORMAT, ReportFormat.valueOf(reportFormat));
															params.put(ReportParams.PARAM_LANG, MLTextHelper.localeKey(locale));
															params.put(ReportParams.PARAM_ASSOCIATED_TPL_FILES, associationService
																	.getTargetAssocs(tplNodeRef, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES));
															params.put(BeCPGReportEngine.PARAM_DOCUMENT_NODEREF, documentNodeRef);
															params.put(BeCPGReportEngine.PARAM_ENTITY_NODEREF, entityNodeRef);

															logger.debug("Update report: " + entityNodeRef + " for document " + documentName + " ("
																	+ documentNodeRef + ")");

															if (engine.isXmlEngine()) {
																if (reportData.getXmlDataSource() == null) {
																	throw new IllegalArgumentException("nodeElt is null");
																}

																if (logger.isTraceEnabled()) {
																	logger.trace(
																			"DataSource XML : \n" + reportData.getXmlDataSource().asXML() + "\n\n");
																}

																filterByReportKind(reportData.getXmlDataSource(), tplNodeRef);

																if (logger.isTraceEnabled()) {
																	logger.trace("Filtered DataSource XML : \n"
																			+ reportData.getXmlDataSource().asXML() + "\n\n");
																}

															}

															engine.createReport(tplNodeRef, reportData, writer.getContentOutputStream(), params);

															nodeService.setProperty(documentNodeRef, ReportModel.PROP_REPORT_IS_DIRTY, false);
														} else {
															writer.setMimetype("text/plain");
															writer.putContent("Loading ...");
															logger.debug("Mark durty report: " + entityNodeRef + " for document " + documentName
																	+ " (" + documentNodeRef + ")");

															nodeService.setProperty(documentNodeRef, ReportModel.PROP_REPORT_IS_DIRTY, true);
														}

														I18NUtil.setLocale(Locale.getDefault());
														I18NUtil.setContentLocale(Locale.getDefault());

														nodeService.setProperty(documentNodeRef, ContentModel.PROP_MODIFIED, generatedDate);

														nodeService.setProperty(documentNodeRef, ContentModel.PROP_NAME, documentName);

														nodeService.setProperty(documentNodeRef, ContentModel.PROP_TITLE, documentTitle);
														
														if(!Boolean.TRUE.equals(includeReportInSearch)) {
															nodeService.setProperty(documentNodeRef, ContentModel.PROP_IS_INDEXED, false);
														} else {
															nodeService.setProperty(documentNodeRef, ContentModel.PROP_IS_INDEXED, true);
															nodeService.setProperty(documentNodeRef, ContentModel.PROP_IS_CONTENT_INDEXED, false);
														}
														
														if (reportParameters.isEmpty()) {
															nodeService.removeProperty(documentNodeRef, ReportModel.PROP_REPORT_TEXT_PARAMETERS);
														} else {
															nodeService.setProperty(documentNodeRef, ReportModel.PROP_REPORT_TEXT_PARAMETERS,
																	reportParameters.toJSONString());
														}

														nodeService.setProperty(documentNodeRef, ReportModel.PROP_REPORT_LOCALES,
																MLTextHelper.localeKey(locale));
														nodeService.setProperty(documentNodeRef, ReportModel.PROP_REPORT_IS_DEFAULT, isDefault);

														I18NUtil.setLocale(locale);
														I18NUtil.setContentLocale(locale);
													}

												} catch (ReportException e) {
													logger.error("Failed to execute report for template : " + tplNodeRef, e);
												} finally {
													policyBehaviourFilter.enableBehaviour(documentNodeRef, ContentModel.ASPECT_AUDITABLE);
												}

												// Set Assoc
												newReports.add(documentNodeRef);

											}
										}

									}

								});

							}

							if (logger.isDebugEnabled() && watch!=null) {
								watch.stop();
								logger.debug("Reports generated in  " + watch.getTotalTimeSeconds() + " seconds for node " + entityNodeRef);
							}
						} else {
							logger.debug("No report tpls found, delete existing ones");
						}

						updateReportsAssoc(entityNodeRef, newReports);
						// set reportNodeGenerated property to now
						nodeService.setProperty(entityNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED, generatedDate);

						entityActivityService.postEntityActivity(entityNodeRef, ActivityType.Report, ActivityEvent.Update, null);

					} finally {
						I18NUtil.setLocale(currentLocal);
						I18NUtil.setContentLocale(currentContentLocal);
						ruleService.enableRules();
						policyBehaviourFilter.enableBehaviour(entityNodeRef);
					}
				}
				return true;
			};
			AuthenticationUtil.runAsSystem(actionRunAs);
		}, false, true);
	}

	@SuppressWarnings("unchecked")
	private void filterByReportKind(Element dataXml, NodeRef tplNodeRef) {
		StopWatch stopWatch = null;
		if (logger.isDebugEnabled()) {
			stopWatch = new StopWatch();
			stopWatch.start();
		}

		if (getFromCacheListFolderNodeRef(RepoConsts.PATH_REPORT_KINDLIST) == null) {
			return;
		}

		String reportKindCode = "", reportKindNoneCode = "None";
		if (tplNodeRef != null) {
			List<String> reportKindProp = (List<String>) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_KINDS);
			if ((reportKindProp != null) && !reportKindProp.isEmpty()) {
				reportKindCode = reportKindProp.get(0);
			}
		}

		// Filter XML report by reportKind
		String[] entityParams = null;
		for (Iterator<Element> entityIterator = dataXml.elementIterator(); entityIterator.hasNext();) {
			Element entityEl = entityIterator.next();
			if (entityEl.getName().equals("dataLists")) {

				for (Iterator<Element> datalistsIterator = entityEl.elementIterator(); datalistsIterator.hasNext();) {
					Element dlEl = datalistsIterator.next();
					boolean hasReportKindAspect = false;

					for (Iterator<Element> elIterator = dlEl.elementIterator(); elIterator.hasNext();) {
						Element itemEl = elIterator.next();
						String[] repKindCodes = itemEl.valueOf("@" + ReportModel.PROP_REPORT_KINDS_CODE.getLocalName()).split("\\s*,\\s*");

						if (Arrays.asList(repKindCodes).contains(reportKindNoneCode)) {
							dlEl.remove(itemEl);
							continue;
						}

						if (Arrays.asList(repKindCodes).contains(reportKindCode)) {
							hasReportKindAspect = true;
						}

					}

					if (hasReportKindAspect) {
						for (Iterator<Element> elIterator = dlEl.elementIterator(); elIterator.hasNext();) {
							Element itemEl = elIterator.next();
							String[] repKindCodes = itemEl.valueOf("@" + ReportModel.PROP_REPORT_KINDS_CODE.getLocalName()).split("\\s*,\\s*");
							if (!Arrays.asList(repKindCodes).contains(reportKindCode) || (repKindCodes == null)) {
								dlEl.remove(itemEl);
							}
						}
					}
				}
			}

			// get report parameters
			if (entityEl.getName().equals(ReportModel.PROP_REPORT_PARAMETERS.getLocalName())) {
				entityParams = entityEl.getStringValue().split("\\s*,\\s*");
			}
		}

		// Filter XML report by parameters
		NodeRef reportParamsFolderNodRef = getFromCacheListFolderNodeRef(RepoConsts.PATH_REPORT_PARAMS);
		Map<String, String> valideCode = new HashMap<>();
		List<ChildAssociationRef> assocList = nodeService.getChildAssocs(reportParamsFolderNodRef);
		assocList.forEach(val -> {
			String paramCode = (String) nodeService.getProperty(val.getChildRef(), BeCPGModel.PROP_LV_CODE);
			String paramValue = (String) nodeService.getProperty(val.getChildRef(), BeCPGModel.PROP_LV_VALUE);
			if (isValidReportParams(paramCode)) {
				valideCode.put(paramValue, paramCode);
			}

		});

		if (valideCode.size() > 0) {
			filterByParams(dataXml,
					getFilteredParams(valideCode, Arrays.asList(entityParams != null ? entityParams : new String[0]), reportKindCode));
		}
		if (logger.isDebugEnabled() && stopWatch!=null) {
			stopWatch.stop();
			logger.debug("Filter XML takes : " + stopWatch.getTotalTimeSeconds() + "s");
		}

	}

	/**
	 * <p>getFromCacheListFolderNodeRef.</p>
	 *
	 * @param listPath a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getFromCacheListFolderNodeRef(String listPath) {
		return beCPGCacheService.getFromCache(EntityReportServiceImpl.class.getName(), REPORT_LIST_CACHE_KEY + listPath, () -> {
			NodeRef systemFolderNodeRef = repoService.getFolderByPath(RepoConsts.PATH_SYSTEM);
			NodeRef listsFolder = entitySystemService.getSystemEntity(systemFolderNodeRef, RepoConsts.PATH_LISTS);
			return entitySystemService.getSystemEntityDataList(listsFolder, listPath);
		});
	}

	private void filterByParams(Element entity, List<String> filteredParams) {

		for (Iterator<Element> entityIter = entity.elementIterator(); entityIter.hasNext();) {
			Element itemEl = entityIter.next();

			String prefixValue = itemEl.valueOf("@" + "prefix");
			prefixValue = prefixValue.isEmpty() ? "" : prefixValue + "_";

			if (filteredParams.contains(prefixValue + itemEl.getName())) {
				entity.remove(itemEl);
				continue;
			}

			for (String paramValue : filteredParams) {
				Attribute att = itemEl.attribute(paramValue);
				if (att != null) {
					itemEl.remove(att);
				}
			}

			Iterator<Element> itemElIter = itemEl.elementIterator();
			if (itemElIter.hasNext()) {
				filterByParams(itemEl, filteredParams);
			}

		}

	}

	private List<String> getFilteredParams(Map<String, String> params, List<String> paramList, String reportKind) {
		List<String> ret = new ArrayList<>();

		params.forEach((val, code) -> {
			String[] codes = code.split(REPORT_PARAM_SEPARATOR);
			if ((codes.length == 3) && !codes[1].equals(reportKind)) {
				return;
			}
			if ((!paramList.contains(val) && code.startsWith("show")) || (paramList.contains(val) && code.startsWith("hide"))) {

				ret.add(codes[codes.length - 1]);
			}
		});

		return ret;
	}

	private boolean isValidReportParams(String codeParams) {
		if(codeParams!=null && !codeParams.isEmpty()) {
			String[] strParams = codeParams.split(REPORT_PARAM_SEPARATOR);
			if ((strParams[0].equals("hide") || strParams[0].equals("show")) && ((strParams.length == 2) || (strParams.length == 3))) {
				return true;
			}
		}
		return false;
	}

	private Map<String, String> getMergedPreferences(List<NodeRef> tplsNodeRef) {

		Map<String, String> prefs = new HashMap<>();

		for (NodeRef tplNodeRef : tplsNodeRef) {

			EntityReportParameters defaultEntityReportParameter = EntityReportParameters
					.createFromJSON((String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TEXT_PARAMETERS));

			if (defaultEntityReportParameter != null) {

				for (Map.Entry<String, String> tmp : defaultEntityReportParameter.getPreferences().entrySet()) {

					if (prefs.containsKey(tmp.getKey())) {
						String ret = prefs.get(tmp.getKey());
						if (!ret.isEmpty()) {
							if ("true".equalsIgnoreCase(ret) || "false".equalsIgnoreCase(ret)) {
								if ("true".equalsIgnoreCase(tmp.getValue())) {
									prefs.put(tmp.getKey(), tmp.getValue());
								}
							} else {
								ret += "," + tmp.getValue();
								prefs.put(tmp.getKey(), ret);
							}

						} else {
							prefs.put(tmp.getKey(), tmp.getValue());
						}

					} else {
						prefs.put(tmp.getKey(), tmp.getValue());
					}

				}

			}

		}

		return prefs;
	}

	@SuppressWarnings("unchecked")
	private List<EntityReportParameters> getEntityReportParametersList(NodeRef tplNodeRef, NodeRef entityNodeRef) {

		EntityReportParameters defaultEntityReportParameter = EntityReportParameters
				.createFromJSON((String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TEXT_PARAMETERS));

		if (!defaultEntityReportParameter.isParametersEmpty()) {

			if (logger.isDebugEnabled()) {
				logger.debug("Found multi-param report: " + defaultEntityReportParameter.toString());
			}

			List<EntityReportParameters> ret = new LinkedList<>();

			String iterationKey = defaultEntityReportParameter.getIterationKey();
			if ((iterationKey != null) && !iterationKey.isEmpty()) {
				QName iterationPropQName = QName.createQName(iterationKey, namespaceService);

				if (entityDictionaryService.isSubClass(iterationPropQName, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {

					NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);

					if (listContainerNodeRef != null) {
						NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, iterationPropQName);

						if (dataListNodeRef != null) {
							for (NodeRef dataListItem : entityListDAO.getListItems(dataListNodeRef, iterationPropQName)) {
								EntityReportParameters tmp = readParameters(dataListItem, defaultEntityReportParameter);
								if (tmp != null) {
									ret.add(tmp);
								}

							}
						}
					}

				} else {
					ClassAttributeDefinition propDef = entityDictionaryService.getPropDef(iterationPropQName);
					List<NodeRef> nodeRefs = null;
					if (propDef instanceof PropertyDefinition) {
						if (((PropertyDefinition) propDef).isMultiValued()) {
							nodeRefs = (List<NodeRef>) nodeService.getProperty(entityNodeRef, iterationPropQName);
						} else {
							logger.warn("Cannot iterate on prop: " + iterationPropQName);
						}
					} else if (propDef instanceof AssociationDefinition) {
						if (propDef instanceof ChildAssociationDefinition) {
							nodeRefs = associationService.getChildAssocs(entityNodeRef, iterationPropQName);
						} else {
							nodeRefs = associationService.getTargetAssocs(entityNodeRef, iterationPropQName);
						}

					}

					if (nodeRefs != null) {
						for (NodeRef dataListItem : nodeRefs) {
							EntityReportParameters tmp = readParameters(dataListItem, defaultEntityReportParameter);
							if (tmp != null) {
								ret.add(tmp);
							}

						}
					}

				}

				return ret;
			}

		}

		return Arrays.asList(defaultEntityReportParameter);
	}

	private EntityReportParameters readParameters(EntityReportParameters config) {
		return readParameters(null, config);

	}

	private EntityReportParameters readParameters(NodeRef itemNodeRef, EntityReportParameters config) {
		EntityReportParameters ret = new EntityReportParameters(config);
		for (EntityReportParameter configParam : config.getParameters()) {
			if (itemNodeRef == null) {
				itemNodeRef = configParam.getNodeRef();
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Reading parameter: " + itemNodeRef + " " + configParam.getProp());
			}

			if ((itemNodeRef != null) && nodeService.exists(itemNodeRef) && (configParam.getProp() != null)) {
				Serializable value = null;
				String[] props = configParam.getProp().split("\\|");
				if (props.length > 1) {
					NodeRef assocNodeRef = associationService.getTargetAssoc(itemNodeRef, QName.createQName(props[0], namespaceService));
					if (assocNodeRef != null) {
						value = nodeService.getProperty(assocNodeRef, QName.createQName(props[1], namespaceService));
					}
				} else {
					value = nodeService.getProperty(itemNodeRef, QName.createQName(props[0], namespaceService));
				}

				if (value != null) {
					EntityReportParameter param = configParam.clone();
					param.setNodeRef(itemNodeRef);
					if (value instanceof MLText) {
						param.setValue(MLTextHelper.getClosestValue((MLText) value, Locale.getDefault()));
					} else {
						param.setValue(value.toString());
					}

					ret.getParameters().add(param);
				}
			}

		}

		return ret;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public void generateReport(final NodeRef entityNodeRef, final NodeRef documentNodeRef) {

		if (entityNodeRef == null) {
			throw new IllegalArgumentException("entityNodeRef is null");
		}

		if (documentNodeRef == null) {
			throw new IllegalArgumentException("documentNodeRef is null");
		}

		L2CacheSupport.doInCacheContext(() -> {

			RunAsWork<Object> actionRunAs = () -> {
				if (nodeService.exists(entityNodeRef)) {
					Locale currentLocal = I18NUtil.getLocale();
					Locale currentContentLocal = I18NUtil.getContentLocale();
					try {
						policyBehaviourFilter.disableBehaviour(entityNodeRef);
						ruleService.disableRules();
						if (logger.isDebugEnabled()) {
							logger.debug(
									"Generate report: " + entityNodeRef + " - " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
						}

						Date generatedDate = Calendar.getInstance().getTime();

						StopWatch watch = null;
						if (logger.isDebugEnabled()) {
							watch = new StopWatch();
							watch.start();
						}

						NodeRef tplNodeRef = associationService.getTargetAssoc(documentNodeRef, ReportModel.ASSOC_REPORT_TPL);

						if (tplNodeRef == null) {
							throw new IllegalArgumentException("tplNodeRef is null");
						}

						Locale locale = MLTextHelper.getNearestLocale(Locale.getDefault());

						I18NUtil.setLocale(locale);
						I18NUtil.setContentLocale(locale);

						Boolean isDefault = (Boolean) this.nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT);

						EntityReportParameters reportParameters = readParameters(EntityReportParameters
								.createFromJSON((String) nodeService.getProperty(documentNodeRef, ReportModel.PROP_REPORT_TEXT_PARAMETERS)));

						if (nodeService.hasAspect(documentNodeRef, ReportModel.ASPECT_REPORT_LOCALES)) {
							List<String> langs = (List<String>) nodeService.getProperty(documentNodeRef, ReportModel.PROP_REPORT_LOCALES);
							if ((langs != null) && !langs.isEmpty()) {
								locale = MLTextHelper.parseLocale(langs.get(0));
								I18NUtil.setLocale(locale);
								I18NUtil.setContentLocale(locale);
							}
						}

						if (!MLTextHelper.isDefaultLocale(locale)) {
							isDefault = false;
						}

						String reportFormat = (String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT);
						String documentName = getReportDocumentName(entityNodeRef, tplNodeRef, reportFormat, locale, reportParameters,
								reportParameters.getReportNameFormat(reportNameFormat));

						String documentTitle = getReportDocumentName(entityNodeRef, tplNodeRef, null, locale, reportParameters,
								reportParameters.getReportTitleFormat(reportTitleFormat));

						BeCPGReportEngine engine = getReportEngine(tplNodeRef, ReportFormat.valueOf(reportFormat));

						EntityReportData reportData = retrieveExtractor(entityNodeRef, engine).extract(entityNodeRef,
								reportParameters.getPreferences());

						reportData.setParameters(reportParameters);

						try {
							policyBehaviourFilter.disableBehaviour(documentNodeRef, ContentModel.ASPECT_AUDITABLE);

							ContentWriter writer = contentService.getWriter(documentNodeRef, ContentModel.PROP_CONTENT, true);

							if (writer != null) {
								String mimetype = mimetypeService.guessMimetype(documentName);
								writer.setMimetype(mimetype);
								Map<String, Object> params = new HashMap<>();

								params.put(ReportParams.PARAM_IMAGES, reportData.getDataObjects());
								params.put(ReportParams.PARAM_FORMAT, ReportFormat.valueOf(reportFormat));
								params.put(ReportParams.PARAM_LANG, MLTextHelper.localeKey(locale));
								params.put(ReportParams.PARAM_ASSOCIATED_TPL_FILES,
										associationService.getTargetAssocs(tplNodeRef, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES));
								params.put(BeCPGReportEngine.PARAM_DOCUMENT_NODEREF, documentNodeRef);
								params.put(BeCPGReportEngine.PARAM_ENTITY_NODEREF, entityNodeRef);

								logger.debug("beCPGReportEngine createReport: " + entityNodeRef + " for document " + documentName + " ("
										+ documentNodeRef + ")");

								if (engine.isXmlEngine()) {

									if (reportData.getXmlDataSource() == null) {
										throw new IllegalArgumentException("nodeElt is null");
									}

									filterByReportKind(reportData.getXmlDataSource(), tplNodeRef);

									if (logger.isTraceEnabled()) {
										logger.trace("Filtered DataSource XML : \n" + reportData.getXmlDataSource().asXML() + "\n\n");
									}

								}

								engine.createReport(tplNodeRef, reportData, writer.getContentOutputStream(), params);

								I18NUtil.setLocale(Locale.getDefault());
								I18NUtil.setContentLocale(Locale.getDefault());

								if (reportParameters.isEmpty()) {
									nodeService.removeProperty(documentNodeRef, ReportModel.PROP_REPORT_TEXT_PARAMETERS);
								} else {
									nodeService.setProperty(documentNodeRef, ReportModel.PROP_REPORT_TEXT_PARAMETERS,
											reportParameters.toJSONString());
								}

								nodeService.setProperty(documentNodeRef, ContentModel.PROP_MODIFIED, generatedDate);

								nodeService.setProperty(documentNodeRef, ReportModel.PROP_REPORT_IS_DIRTY, false);

								nodeService.setProperty(documentNodeRef, ContentModel.PROP_NAME, documentName);

								nodeService.setProperty(documentNodeRef, ContentModel.PROP_TITLE, documentTitle);
								
								if(!Boolean.TRUE.equals(includeReportInSearch)) {
									nodeService.setProperty(documentNodeRef, ContentModel.PROP_IS_INDEXED, false);
								}  else {
									nodeService.setProperty(documentNodeRef, ContentModel.PROP_IS_INDEXED, true);
									nodeService.setProperty(documentNodeRef, ContentModel.PROP_IS_CONTENT_INDEXED, false);
								}
								
								nodeService.setProperty(documentNodeRef, ReportModel.PROP_REPORT_IS_DEFAULT, isDefault);

							}

						} catch (ReportException e) {
							logger.error("Failed to execute report for template : " + tplNodeRef, e);
						} finally {
							policyBehaviourFilter.enableBehaviour(documentNodeRef, ContentModel.ASPECT_AUDITABLE);
						}

						if (logger.isDebugEnabled() && watch!=null) {
							watch.stop();
							logger.debug("Reports generated in  " + watch.getTotalTimeSeconds() + " seconds for node " + entityNodeRef);
						}

					} finally {
						I18NUtil.setLocale(currentLocal);
						I18NUtil.setContentLocale(currentContentLocal);
						ruleService.enableRules();
						policyBehaviourFilter.enableBehaviour(entityNodeRef);
					}
				}
				return true;
			};
			AuthenticationUtil.runAsSystem(actionRunAs);
		}, false, true);
	}

	/** {@inheritDoc} */
	@Override
	public void generateReport(NodeRef entityNodeRef, NodeRef templateNodeRef, EntityReportParameters reportParameters, Locale locale,
			ReportFormat reportFormat, OutputStream outputStream) {
		AuthenticationUtil.runAsSystem(() -> {
			Locale currentLocal = I18NUtil.getLocale();
			Locale currentContentLocal = I18NUtil.getContentLocale();
			try {

				I18NUtil.setLocale(locale);
				I18NUtil.setContentLocale(locale);

				BeCPGReportEngine engine = getReportEngine(templateNodeRef, reportFormat);

				EntityReportData reportData = retrieveExtractor(entityNodeRef, engine).extract(entityNodeRef, reportParameters.getPreferences());

				reportData.setParameters(reportParameters);

				try {

					Map<String, Object> params = new HashMap<>();

					params.put(ReportParams.PARAM_IMAGES, reportData.getDataObjects());
					params.put(ReportParams.PARAM_FORMAT, reportFormat);
					params.put(ReportParams.PARAM_LANG, MLTextHelper.localeKey(locale));
					params.put(ReportParams.PARAM_ASSOCIATED_TPL_FILES,
							associationService.getTargetAssocs(templateNodeRef, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES));

					if (engine.isXmlEngine()) {

						if (reportData.getXmlDataSource() == null) {
							throw new IllegalArgumentException("nodeElt is null");
						}

						filterByReportKind(reportData.getXmlDataSource(), templateNodeRef);

						if (logger.isTraceEnabled()) {
							logger.trace("Filtered DataSource XML : \n" + reportData.getXmlDataSource().asXML() + "\n\n");
						}
					}

					engine.createReport(templateNodeRef, reportData, outputStream, params);

				} catch (ReportException e) {
					logger.error("Failed to execute report for template : " + templateNodeRef, e);
				}

			} finally {
				I18NUtil.setLocale(currentLocal);
				I18NUtil.setContentLocale(currentContentLocal);
			}
			return true;
		});
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public void generateReport(NodeRef entityNodeRef, NodeRef documentNodeRef, ReportFormat reportFormat, OutputStream outputStream) {

		NodeRef templateNodeRef = associationService.getTargetAssoc(documentNodeRef, ReportModel.ASSOC_REPORT_TPL);

		if (templateNodeRef == null) {
			throw new IllegalArgumentException("templateNodeRef is null");
		}

		Locale locale = MLTextHelper.getNearestLocale(Locale.getDefault());

		EntityReportParameters reportParameters = readParameters(
				EntityReportParameters.createFromJSON((String) nodeService.getProperty(documentNodeRef, ReportModel.PROP_REPORT_TEXT_PARAMETERS)));

		if (nodeService.hasAspect(documentNodeRef, ReportModel.ASPECT_REPORT_LOCALES)) {

			List<String> langs = (List<String>) nodeService.getProperty(documentNodeRef, ReportModel.PROP_REPORT_LOCALES);
			if ((langs != null) && !langs.isEmpty()) {
				locale = MLTextHelper.parseLocale(langs.get(0));
			}
		}

		generateReport(entityNodeRef, templateNodeRef, reportParameters, locale, reportFormat, outputStream);

	}

	/** {@inheritDoc} */
	@Override
	public String getXmlReportDataSource(NodeRef entityNodeRef, Locale locale, EntityReportParameters reportParameters) {

		StopWatch watch = null;
		Locale currentLocal = I18NUtil.getLocale();
		Locale currentContentLocal = I18NUtil.getContentLocale();
		try {
			I18NUtil.setLocale(locale);
			I18NUtil.setContentLocale(locale);
			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}
			Map<String, String> preferences = null;

			if (reportParameters == null) {
				reportParameters = new EntityReportParameters();
				// Add a fake parameter
				EntityReportParameter param1 = new EntityReportParameter();
				param1.setId("sampleParam1");
				param1.setNodeRef(entityNodeRef);
				param1.setProp("ext1:sampleParam");
				param1.setValue("sampleValue");
				reportParameters.getParameters().add(param1);
				List<NodeRef> tplsNodeRef = getReportTplsToGenerate(entityNodeRef);
				preferences = getMergedPreferences(tplsNodeRef);
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("Using reportParameters: " + reportParameters.toString());
				}

				preferences = reportParameters.getPreferences();
			}

			EntityReportData reportData = retrieveExtractor(entityNodeRef).extract(entityNodeRef, preferences);

			reportData.setParameters(reportParameters);

			filterByReportKind(reportData.getXmlDataSource(), null);

			return reportData.getXmlDataSource().asXML();
		} finally {
			I18NUtil.setLocale(currentLocal);
			I18NUtil.setContentLocale(currentContentLocal);

			if (logger.isDebugEnabled() && watch!=null) {
				watch.stop();
				logger.debug("XmlReportDataSource generated in  " + watch.getTotalTimeSeconds() + " seconds for node " + entityNodeRef);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public EntityReportExtractorPlugin retrieveExtractor(NodeRef entityNodeRef) {
		return retrieveExtractor(entityNodeRef, null);
	}

	private EntityReportExtractorPlugin retrieveExtractor(NodeRef entityNodeRef, BeCPGReportEngine engine) {

		EntityReportExtractorPlugin ret = null;

		QName type = nodeService.getType(entityNodeRef);

		for (EntityReportExtractorPlugin entityReportExtractorPlugin : entityExtractors) {
			if (engine!=null && !engine.isXmlEngine() && (entityReportExtractorPlugin instanceof NoXmlEntityReportExtractor)) {
				return entityReportExtractorPlugin;
			}

			EntityReportExtractorPriority priority = entityReportExtractorPlugin.getMatchPriority(type);
			if (!EntityReportExtractorPriority.NONE.equals(priority)) {
				if ((ret == null) || priority.isHigherPriority(ret.getMatchPriority(type))) {
					ret = entityReportExtractorPlugin;
				}
			}
		}

		return ret;
	}

	private BeCPGReportEngine getReportEngine(NodeRef templateNodeRef, ReportFormat reportFormat) throws ReportException {
		if (engines != null) {
			for (BeCPGReportEngine engine : engines) {
				if (engine.isApplicable(templateNodeRef, reportFormat)) {
					return engine;
				}
			}
		}
		throw new ReportException("No report engine found for  tpl:"+templateNodeRef+" "+reportFormat);
	}

	private String getReportDocumentName(NodeRef entityNodeRef, NodeRef tplNodeRef, String reportFormat, Locale locale,
			EntityReportParameters reportParameters, String nameFormat) {

		String lang = MLTextHelper.localeKey(locale);

		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(nameFormat);
		StringBuffer sb = new StringBuffer();
		while (patternMatcher.find()) {

			String propQname = patternMatcher.group(1);
			String replacement = "";
			if (propQname.contains("|")) {
				for (String propQnameAlt : propQname.split("\\|")) {
					replacement = extractPropText(entityNodeRef, tplNodeRef, lang, propQnameAlt, reportParameters.getParameters());
					if ((replacement != null) && !replacement.isEmpty()) {
						break;
					}
				}

			} else {
				replacement = extractPropText(entityNodeRef, tplNodeRef, lang, propQname, reportParameters.getParameters());
			}

			patternMatcher.appendReplacement(sb, replacement != null ? replacement.replace("$", "") : "");

		}
		patternMatcher.appendTail(sb);

		String documentName = sb.toString().replace("-  -", "-").replaceAll("\\s{2,}", " ").replace("- -", "-").trim().replaceAll("\\-$|\\(\\)", "")
				.trim().replaceAll("\\-$|\\(\\)", "").replaceAll("\\." + RepoConsts.REPORT_EXTENSION_BIRT, "").replaceAll("\\." + RepoConsts.REPORT_EXTENSION_JXLS, "").trim();

		if (reportFormat != null) {
			documentName += "." + reportFormat.toLowerCase();
		}

		return documentName;
	}

	private String extractPropText(NodeRef nodeRef, NodeRef tplNodeRef, String lang, String propQname, List<EntityReportParameter> params) {
		if (propQname != null) {
			if (propQname.indexOf("report_") == 0) {
				return (String) nodeService.getProperty(tplNodeRef, QName.createQName(propQname.replace("report_", ""), namespaceService));
			} else if (propQname.indexOf("entity_") == 0) {
				return (String) nodeService.getProperty(nodeRef, QName.createQName(propQname.replace("entity_", ""), namespaceService));
			} else if ("locale".equals(propQname)) {
				return lang;
			} else {
				for (EntityReportParameter param : params) {
					if (param.getId().equals(propQname)) {
						return param.getValue();
					}

				}

			}

			return (String) nodeService.getProperty(nodeRef, QName.createQName(propQname, namespaceService));
		}
		return null;
	}

	private NodeRef getReportDocumenNodeRef(NodeRef entityNodeRef, NodeRef tplNodeRef, String documentName, Locale locale,
			EntityReportParameters reportParameters) {
		NodeRef documentNodeRef = null;
		NodeRef documentsFolderNodeRef = entityService.getOrCreateDocumentsFolder(entityNodeRef);
		if (documentsFolderNodeRef != null) {

			for (FileInfo fileInfo : fileFolderService.listFiles(documentsFolderNodeRef)) {
				if (tplNodeRef.equals(associationService.getTargetAssoc(fileInfo.getNodeRef(), ReportModel.ASSOC_REPORT_TPL))) {
					if (reportParameters.isParametersEmpty() || reportParameters.match(EntityReportParameters
							.createFromJSON((String) nodeService.getProperty(fileInfo.getNodeRef(), ReportModel.PROP_REPORT_TEXT_PARAMETERS)))) {
						for (Locale tmpLocale : getEntityReportLocales(fileInfo.getNodeRef())) {
							if (tmpLocale.equals(locale)) {
								documentNodeRef = fileInfo.getNodeRef();
								break;
							}
						}
						if (documentNodeRef != null) {
							break;
						}
					}
				}
			}
			if (documentNodeRef == null) {

				documentNodeRef = nodeService.getChildByName(documentsFolderNodeRef, ContentModel.ASSOC_CONTAINS, documentName);

				if (logger.isDebugEnabled()) {
					logger.debug("Create new report document " + documentName + " (" + documentNodeRef + ")");
				}

				
				if (documentNodeRef == null) {
					documentNodeRef = fileFolderService.create(documentsFolderNodeRef, documentName, ReportModel.TYPE_REPORT).getNodeRef();
				}

				// We don't update permissions. If permissions are modified
				// ->
				// admin
				// should use the action update-permissions from the
				// reportTpl
				setPermissions(tplNodeRef, documentNodeRef);
				associationService.update(documentNodeRef, ReportModel.ASSOC_REPORT_TPL, tplNodeRef);

			}
		}
		return documentNodeRef;
	}

	@SuppressWarnings("unchecked")
	private boolean isLocaleEnableOnTemplate(NodeRef tplNodeRef, Locale locale, boolean hideDefaultLocal) {

		List<String> langs = (List<String>) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_LOCALES);
		if ((langs != null) && !langs.isEmpty()) {
			for (String lang : langs) {
				if (locale.equals(MLTextHelper.parseLocale(lang)) && !(hideDefaultLocal && MLTextHelper.isDefaultLocale(locale))) {
					return true;
				}
			}
			return false;
		}

		return MLTextHelper.isDefaultLocale(locale);
	}

	@SuppressWarnings("unchecked")
	private List<Locale> getEntityReportLocales(NodeRef entityNodeRef) {
		List<Locale> ret = new ArrayList<>();
		List<String> langs = (List<String>) nodeService.getProperty(entityNodeRef, ReportModel.PROP_REPORT_LOCALES);
		if (langs != null) {
			for (String lang : langs) {
				ret.add(MLTextHelper.parseLocale(lang));
			}
		} else {
			ret.add(MLTextHelper.getNearestLocale(Locale.getDefault()));
		}
		return ret;
	}

	private void updateReportsAssoc(NodeRef entityNodeRef, List<NodeRef> newReports) {

		// #417 : refresh reports assoc (delete obsolete reports if we rename
		// entity)
		if (!nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_WORKING_COPY)) {
			List<NodeRef> dbReports = associationService.getTargetAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS, false);
			for (NodeRef dbReport : dbReports) {
				if (!newReports.contains(dbReport)) {
					logger.debug("delete old report: " + dbReport);
					nodeService.addAspect(dbReport, ContentModel.ASPECT_TEMPORARY, null);
					nodeService.deleteNode(dbReport);
				}
			}
		}
		// Foec

		associationService.update(entityNodeRef, ReportModel.ASSOC_REPORTS, newReports, true);
	}

	/**
	 * Get the report templates to generate.
	 *
	 * @param nodeRef
	 *            the product node ref
	 * @return the report tpls to generate
	 */
	public List<NodeRef> getReportTplsToGenerate(NodeRef nodeRef) {

		List<NodeRef> tplsToReturnNodeRef = new ArrayList<>();

		// system reports
		QName nodeType = nodeService.getType(nodeRef);
		List<NodeRef> tplsNodeRef = reportTplService.getSystemReportTemplates(ReportType.Document, nodeType);

		for (NodeRef tplNodeRef : tplsNodeRef) {

			tplsToReturnNodeRef.add(tplNodeRef);
		}

		// selected user reports
		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, ReportModel.ASSOC_REPORT_TEMPLATES);

		for (AssociationRef assocRef : assocRefs) {

			NodeRef tplNodeRef = assocRef.getTargetRef();
			tplsToReturnNodeRef.add(tplNodeRef);
		}

		return tplsToReturnNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public void setPermissions(NodeRef tplNodeRef, NodeRef documentNodeRef) {

		Set<AccessPermission> tplAccessPermissions = permissionService.getAllSetPermissions(tplNodeRef);
		permissionService.deletePermissions(documentNodeRef);
		boolean inheritParentPermissions = true;

		if (!tplAccessPermissions.isEmpty()) {
			logger.debug("set permissions size " + tplAccessPermissions.size());
			if (logger.isDebugEnabled()) {
				for (AccessPermission a : tplAccessPermissions) {
					logger.debug("Authority: " + a.getAuthority() + " status " + a.getAccessStatus() + " " + a.getPermission());
				}
			}
			for (AccessPermission tplAccessPermission : tplAccessPermissions) {
				if (!tplAccessPermission.isInherited()) {
					permissionService.setPermission(documentNodeRef, tplAccessPermission.getAuthority(), tplAccessPermission.getPermission(), true);
					inheritParentPermissions = false;
				}
			}
		}

		permissionService.setInheritParentPermissions(documentNodeRef, inheritParentPermissions);
	}

	/** {@inheritDoc} */
	@Override
	public boolean shouldGenerateReport(NodeRef entityNodeRef, NodeRef documentNodeRef) {
		StopWatch watch = new StopWatch();
		if (logger.isDebugEnabled()) {
			watch.start();
		}
		try {

			if (documentNodeRef != null) {
				Boolean dirty = (Boolean) nodeService.getProperty(documentNodeRef, ReportModel.PROP_REPORT_IS_DIRTY);

				if (Boolean.TRUE.equals(dirty)) {
					return true;
				}
			}

			Date modified = (Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_MODIFIED);
			Date formulatedDate = (Date) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_FORMULATED_DATE);

			if ((formulatedDate != null) && (modified != null) && (formulatedDate.getTime() > modified.getTime())) {
				logger.debug("taking formulated date");
				modified = formulatedDate;
			}

			Date generatedReportDate = (Date) nodeService.getProperty(entityNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);

			if (documentNodeRef != null) {

				Date documentGeneratedDate = (Date) nodeService.getProperty(documentNodeRef, ContentModel.PROP_MODIFIED);
				if ((documentGeneratedDate != null)
						&& ((generatedReportDate == null) || (generatedReportDate.getTime() < documentGeneratedDate.getTime()))) {
					generatedReportDate = documentGeneratedDate;
					logger.debug("taking report date");
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug(" generated date :" + generatedReportDate);
				logger.debug(" modified date :" + modified);
			}

			if ((modified == null) || (generatedReportDate == null) || (modified.getTime() > generatedReportDate.getTime())) {
				return true;
			}

			List<NodeRef> tplsNodeRef = getReportTplsToGenerate(entityNodeRef);

			for (NodeRef tplNodeRef : tplsNodeRef) {
				modified = (Date) nodeService.getProperty(tplNodeRef, ContentModel.PROP_MODIFIED);
				if ((modified == null) || (generatedReportDate == null) || (modified.getTime() > generatedReportDate.getTime())) {
					return true;
				}
			}

			for (NodeRef tplNodeRef : tplsNodeRef) {
				for (NodeRef associatedTplFile : associationService.getTargetAssocs(tplNodeRef, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES)) {
					modified = (Date) nodeService.getProperty(associatedTplFile, ContentModel.PROP_MODIFIED);
					if ((modified == null) || (generatedReportDate == null) || (modified.getTime() > generatedReportDate.getTime())) {
						return true;
					}
				}
			}

			logger.debug("Check from extractor");

			return retrieveExtractor(entityNodeRef).shouldGenerateReport(entityNodeRef, generatedReportDate);
		} finally {
			if (logger.isDebugEnabled() && watch!=null) {
				watch.stop();
				logger.debug("ShouldGenerateReport executed in  " + watch.getTotalTimeSeconds() + " seconds ");
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getSelectedReport(NodeRef entityNodeRef) {

		String reportName = getSelectedReportName(entityNodeRef);

		List<NodeRef> dbReports = associationService.getTargetAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS, false);

		NodeRef ret = null;

		for (NodeRef reportNodeRef : dbReports) {
			if (permissionService.hasPermission(reportNodeRef, "Read") == AccessStatus.ALLOWED) {

				String reportTitle = (String) nodeService.getProperty(reportNodeRef, ContentModel.PROP_TITLE);
				if (reportTitle == null) {
					reportTitle = (String) nodeService.getProperty(reportNodeRef, ContentModel.PROP_NAME);
				}
				if (reportName != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Test " + reportName + " against " + reportTitle);
					}

					// Test 10. Barcode report (en) against
					// 10. Barcode report (en)

					if ((reportTitle != null) && reportTitle.equalsIgnoreCase(reportName)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Found selected report for title: " + reportName + " " + reportNodeRef);
						}

						return reportNodeRef;
					}
				}

				Boolean isDefault = (Boolean) this.nodeService.getProperty(reportNodeRef, ReportModel.PROP_REPORT_IS_DEFAULT);
				if (Boolean.TRUE.equals(isDefault)) {
					ret = reportNodeRef;
				}

			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Selected report for title: " + reportName + " not  found returning default " + ret);
		}
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	@Nullable
	public String getSelectedReportName(NodeRef entityNodeRef) {

		String username = AuthenticationUtil.getFullyAuthenticatedUser();

		if (!AuthenticationUtil.SYSTEM_USER_NAME.equals(username)) {
			String typeName = nodeService.getType(entityNodeRef).toPrefixString(namespaceService).replace(":", "_");

			Map<String, Serializable> preferences = preferenceService.getPreferences(username);

			String reportName = (String) preferences.get(PREF_REPORT_PREFIX + typeName + PREF_REPORT_SUFFIX);

			if (logger.isDebugEnabled()) {
				logger.debug("Getting: " + reportName + " from preference for: " + username + " and type: " + typeName);
			}

			return reportName;
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getOrRefreshReport(NodeRef entityNodeRef, NodeRef documentNodeRef) {
		if (shouldGenerateReport(entityNodeRef, documentNodeRef)) {
			logger.debug("Entity report is not up to date for entity " + entityNodeRef + " document " + documentNodeRef);
			if (documentNodeRef == null) {
				generateReports(entityNodeRef);
			} else {
				generateReport(entityNodeRef, documentNodeRef);
			}
		}

		if ((documentNodeRef == null) || !nodeService.exists(documentNodeRef)) {
			documentNodeRef = getSelectedReport(entityNodeRef);
		}
		return documentNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	@Nullable
	public NodeRef getEntityNodeRef(NodeRef reportNodeRef) {
		List<NodeRef> entityNodeRefs = associationService.getSourcesAssocs(reportNodeRef, ReportModel.ASSOC_REPORTS);
		if (entityNodeRefs != null && !entityNodeRefs.isEmpty()) {
			return entityNodeRefs.get(0);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	@Nullable
	public NodeRef getAssociatedDocumentNodeRef(NodeRef entityNodeRef, NodeRef tplNodeRef, EntityReportParameters reportParameters, Locale locale,
			ReportFormat reportFormat) {
		String documentName = getReportDocumentName(entityNodeRef, tplNodeRef, reportFormat.toString(), locale, reportParameters,
				reportParameters.getReportNameFormat(reportNameFormat));

		NodeRef documentNodeRef = null;
		NodeRef documentsFolderNodeRef = entityService.getDocumentsFolder(entityNodeRef, false);
		if (documentsFolderNodeRef != null) {

			documentNodeRef = nodeService.getChildByName(documentsFolderNodeRef, ContentModel.ASSOC_CONTAINS, documentName);

			if ((documentNodeRef != null) && !shouldGenerateReport(entityNodeRef, documentNodeRef)) {
				return documentNodeRef;
			}
		}

		return null;

	}

}
