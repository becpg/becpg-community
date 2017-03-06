/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityEvent;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.report.engine.BeCPGReportEngine;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.EntityReportExtractorPlugin;
import fr.becpg.repo.report.entity.EntityReportExtractorPlugin.EntityReportExtractorPriority;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.report.client.ReportException;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;

@Service("entityReportService")
public class EntityReportServiceImpl implements EntityReportService {

	private static final String REPORT_NAME = "%s - %s";

	private static final String PREF_REPORT_PREFIX = "fr.becpg.repo.report.";
	private static final String PREF_REPORT_SUFFIX = ".view";

	private static final Log logger = LogFactory.getLog(EntityReportServiceImpl.class);

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
	private BeCPGReportEngine beCPGReportEngine;

	@Autowired
	private MimetypeService mimetypeService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private EntityReportExtractorPlugin[] entityExtractors;

	@Autowired
	private EntityService entityService;

	@Autowired
	private EntityActivityService entityActivityService;
	
	@Override
	public void generateReports(final NodeRef entityNodeRef) {

		if (entityNodeRef == null) {
			throw new IllegalArgumentException("nodeRef is null");
		}

		L2CacheSupport.doInCacheContext(() -> {

			RunAsWork<Object> actionRunAs = () -> {
				RetryingTransactionCallback<Object> actionCallback = () -> {
					if (nodeService.exists(entityNodeRef)) {
						try {
							policyBehaviourFilter.disableBehaviour(entityNodeRef);
							if (logger.isDebugEnabled()) {
								logger.debug(
										"Generate report: " + entityNodeRef + " - " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
							}

							Date generatedDate = Calendar.getInstance().getTime();

							List<NodeRef> tplsNodeRef = getReportTplsToGenerate(entityNodeRef);

							tplsNodeRef = reportTplService.cleanDefaultTpls(tplsNodeRef);

							List<NodeRef> newReports = new ArrayList<>();

							if (!tplsNodeRef.isEmpty()) {
								StopWatch watch = null;
								if (logger.isDebugEnabled()) {
									watch = new StopWatch();
									watch.start();
								}
								
								List<Locale> entityReportLocales = getEntityReportLocales(entityNodeRef);

								for (Locale locale : entityReportLocales) {

									I18NUtil.setLocale(locale);

									EntityReportData reportData = retrieveExtractor(entityNodeRef).extract(entityNodeRef);

									Element xmlDataSource = reportData.getXmlDataSource();

									if (xmlDataSource == null) {
										throw new IllegalArgumentException("nodeElt is null");
									}

									tplsNodeRef.stream().forEach(tplNodeRef -> {

										if (isLocaleEnableOnTemplate(tplNodeRef, locale)) {

											// prepare
											String reportFormat = (String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT);
											String documentName = getReportDocumentName(entityNodeRef, tplNodeRef, reportFormat, locale);

											NodeRef documentNodeRef = getReportDocumenNodeRef(entityNodeRef, tplNodeRef, documentName, locale);

											if (documentNodeRef != null) {
												// Run report
												try {
													policyBehaviourFilter.disableBehaviour(documentNodeRef, ContentModel.ASPECT_AUDITABLE);

													ContentWriter writer = contentService.getWriter(documentNodeRef, ContentModel.PROP_CONTENT, true);

													if (writer != null) {
														String mimetype = mimetypeService.guessMimetype(documentName);
														writer.setMimetype(mimetype);
														Map<String, Object> params = new HashMap<>();

														params.put(ReportParams.PARAM_IMAGES, reportData.getDataObjects());
														params.put(ReportParams.PARAM_FORMAT, ReportFormat.valueOf(reportFormat));
														params.put(ReportParams.PARAM_LANG, locale.getLanguage());
														params.put(ReportParams.PARAM_ASSOCIATED_TPL_FILES, associationService
																.getTargetAssocs(tplNodeRef, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES));

														logger.debug("beCPGReportEngine createReport: " + entityNodeRef + " for document "
																+ documentName + " (" + documentNodeRef + ")");

														beCPGReportEngine.createReport(tplNodeRef,
																new ByteArrayInputStream(xmlDataSource.asXML().getBytes()),
																writer.getContentOutputStream(), params);

														nodeService.setProperty(documentNodeRef, ContentModel.PROP_MODIFIED, generatedDate);
														
														nodeService.setProperty(documentNodeRef, ContentModel.PROP_NAME, documentName);

														if (!(Locale.getDefault().getLanguage().equals(locale.getLanguage()) && entityReportLocales.size()==1)) {
															nodeService.setProperty(documentNodeRef, ReportModel.PROP_REPORT_LOCALES,
																	locale.getLanguage());
														} else {
															nodeService.removeProperty(documentNodeRef, ReportModel.PROP_REPORT_LOCALES);
														}

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

									});

								}

								if (logger.isDebugEnabled()) {
									watch.stop();
									logger.debug("Reports generated in  " + watch.getTotalTimeSeconds() + " seconds for node " + entityNodeRef);
								}
							} else {
								logger.debug("No report tpls found, delete existing ones");
							}

							updateReportsAssoc(entityNodeRef, newReports);
							// set reportNodeGenerated property to now
							nodeService.setProperty(entityNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED, generatedDate);
							
							entityActivityService.postEntityActivity(entityNodeRef, ActivityType.Report, ActivityEvent.Update);

						} finally {
							policyBehaviourFilter.enableBehaviour(entityNodeRef);
						}
					}
					return null;
				};
				return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, false);
			};
			AuthenticationUtil.runAsSystem(actionRunAs);
		}, false, true);
	}

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
				RetryingTransactionCallback<Object> actionCallback = () -> {
					if (nodeService.exists(entityNodeRef)) {
						try {
							policyBehaviourFilter.disableBehaviour(entityNodeRef);
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

							Locale locale = I18NUtil.getLocale();

							if (nodeService.hasAspect(documentNodeRef, ReportModel.ASPECT_REPORT_LOCALES)) {
								@SuppressWarnings("unchecked")
								List<String> langs = (List<String>) nodeService.getProperty(documentNodeRef, ReportModel.PROP_REPORT_LOCALES);
								if ((langs != null) && !langs.isEmpty()) {
									locale = new Locale(langs.get(0));
									I18NUtil.setLocale(locale);
								}
							}

							String reportFormat = (String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT);
							String documentName = getReportDocumentName(entityNodeRef, tplNodeRef, reportFormat, locale);

							EntityReportData reportData = retrieveExtractor(entityNodeRef).extract(entityNodeRef);

							if (reportData.getXmlDataSource() == null) {
								throw new IllegalArgumentException("nodeElt is null");
							}

							try {
								policyBehaviourFilter.disableBehaviour(documentNodeRef, ContentModel.ASPECT_AUDITABLE);

								ContentWriter writer = contentService.getWriter(documentNodeRef, ContentModel.PROP_CONTENT, true);

								if (writer != null) {
									String mimetype = mimetypeService.guessMimetype(documentName);
									writer.setMimetype(mimetype);
									Map<String, Object> params = new HashMap<>();

									params.put(ReportParams.PARAM_IMAGES, reportData.getDataObjects());
									params.put(ReportParams.PARAM_FORMAT, ReportFormat.valueOf(reportFormat));
									params.put(ReportParams.PARAM_LANG, locale.getLanguage());
									params.put(ReportParams.PARAM_ASSOCIATED_TPL_FILES,
											associationService.getTargetAssocs(tplNodeRef, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES));

									logger.debug("beCPGReportEngine createReport: " + entityNodeRef + " for document " + documentName + " ("
											+ documentNodeRef + ")");

									beCPGReportEngine.createReport(tplNodeRef,
											new ByteArrayInputStream(reportData.getXmlDataSource().asXML().getBytes()),
											writer.getContentOutputStream(), params);

									nodeService.setProperty(documentNodeRef, ContentModel.PROP_MODIFIED, generatedDate);
									
									nodeService.setProperty(documentNodeRef, ContentModel.PROP_NAME, documentName);
								}

							} catch (ReportException e) {
								logger.error("Failed to execute report for template : " + tplNodeRef, e);
							} finally {
								policyBehaviourFilter.enableBehaviour(documentNodeRef, ContentModel.ASPECT_AUDITABLE);
							}

							if (logger.isDebugEnabled()) {
								watch.stop();
								logger.debug("Reports generated in  " + watch.getTotalTimeSeconds() + " seconds for node " + entityNodeRef);
							}

						} finally {
							policyBehaviourFilter.enableBehaviour(entityNodeRef);
						}
					}
					return null;
				};
				return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, false);
			};
			AuthenticationUtil.runAsSystem(actionRunAs);
		}, false, true);
	}

	@Override
	public void generateReport(NodeRef entityNodeRef, NodeRef documentNodeRef, ReportFormat reportFormat, OutputStream outputStream) {

		NodeRef templateNodeRef = associationService.getTargetAssoc(documentNodeRef, ReportModel.ASSOC_REPORT_TPL);

		if (templateNodeRef == null) {
			throw new IllegalArgumentException("templateNodeRef is null");
		}

		Locale locale = I18NUtil.getLocale();

		if (nodeService.hasAspect(documentNodeRef, ReportModel.ASPECT_REPORT_LOCALES)) {
			@SuppressWarnings("unchecked")
			List<String> langs = (List<String>) nodeService.getProperty(documentNodeRef, ReportModel.PROP_REPORT_LOCALES);
			if ((langs != null) && !langs.isEmpty()) {
				locale = new Locale(langs.get(0));
				I18NUtil.setLocale(locale);
			}
		}

		EntityReportData reportData = retrieveExtractor(entityNodeRef).extract(entityNodeRef);

		if (reportData.getXmlDataSource() == null) {
			throw new IllegalArgumentException("nodeElt is null");
		}

		try {

			Map<String, Object> params = new HashMap<>();

			params.put(ReportParams.PARAM_IMAGES, reportData.getDataObjects());
			params.put(ReportParams.PARAM_FORMAT, reportFormat);
			params.put(ReportParams.PARAM_LANG, locale.getLanguage());
			params.put(ReportParams.PARAM_ASSOCIATED_TPL_FILES,
					associationService.getTargetAssocs(templateNodeRef, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES));

			beCPGReportEngine.createReport(templateNodeRef, new ByteArrayInputStream(reportData.getXmlDataSource().asXML().getBytes()), outputStream,
					params);

		} catch (ReportException e) {
			logger.error("Failed to execute report for template : " + templateNodeRef, e);
		}

	}

	@Override
	public String getXmlReportDataSource(NodeRef entityNodeRef) {

		StopWatch watch = null;
		try {
			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}

			EntityReportData reportData = retrieveExtractor(entityNodeRef).extract(entityNodeRef);

			return reportData.getXmlDataSource().asXML();
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("XmlReportDataSource generated in  " + watch.getTotalTimeSeconds() + " seconds for node " + entityNodeRef);
			}
		}
	}

	@Override
	public EntityReportExtractorPlugin retrieveExtractor(NodeRef entityNodeRef) {
		QName type = nodeService.getType(entityNodeRef);

		EntityReportExtractorPlugin ret = null;
		for (EntityReportExtractorPlugin entityReportExtractorPlugin : entityExtractors) {
			EntityReportExtractorPriority priority = entityReportExtractorPlugin.getMatchPriority(type);
			if (!EntityReportExtractorPriority.NONE.equals(priority)) {
				if ((ret == null) || priority.isHigherPriority(ret.getMatchPriority(type))) {
					ret = entityReportExtractorPlugin;
				}
			}
		}

		return ret;
	}

	private String getReportDocumentName(NodeRef entityNodeRef, NodeRef tplNodeRef, String reportFormat, Locale locale) {

		String documentName = String.format(REPORT_NAME, nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME),
				nodeService.getProperty(tplNodeRef, ContentModel.PROP_NAME));

		String extension = (String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT);
		if (documentName.endsWith(RepoConsts.REPORT_EXTENSION_BIRT) && (extension != null)) {
			documentName = documentName.replace(RepoConsts.REPORT_EXTENSION_BIRT, extension.toLowerCase());
		}

		if (!(Locale.getDefault().getLanguage().equals(locale.getLanguage()) && getEntityReportLocales(entityNodeRef).size() == 1)) {
			documentName = documentName.substring(0, documentName.lastIndexOf(".")) + " - " + locale.getLanguage()
					+ documentName.substring(documentName.lastIndexOf("."), documentName.length());
		}

		return documentName;
	}

	private NodeRef getReportDocumenNodeRef(NodeRef entityNodeRef, NodeRef tplNodeRef, String documentName, Locale locale) {

		NodeRef documentsFolderNodeRef = entityService.getOrCreateDocumentsFolder(entityNodeRef);
		NodeRef documentNodeRef = null;
		for (FileInfo fileInfo : fileFolderService.listFiles(documentsFolderNodeRef)) {
			if (tplNodeRef.equals(associationService.getTargetAssoc(fileInfo.getNodeRef(), ReportModel.ASSOC_REPORT_TPL))) {

				for (Locale tmpLocale : getEntityReportLocales(fileInfo.getNodeRef())) {

					if (tmpLocale.getLanguage().equals(locale.getLanguage())) {
						documentNodeRef = fileInfo.getNodeRef();
					}
					break;
				}
				if (documentNodeRef != null) {
					break;
				}
			}
		}
		if (documentNodeRef == null) {
			documentNodeRef = fileFolderService.create(documentsFolderNodeRef, documentName, ReportModel.TYPE_REPORT).getNodeRef();
			// We don't update permissions. If permissions are modified -> admin
			// should use the action update-permissions from the reportTpl
			setPermissions(tplNodeRef, documentNodeRef);
			associationService.update(documentNodeRef, ReportModel.ASSOC_REPORT_TPL, tplNodeRef);
		}

		return documentNodeRef;
	}

	@SuppressWarnings("unchecked")
	private boolean isLocaleEnableOnTemplate(NodeRef tplNodeRef, Locale locale) {

		List<String> langs = (List<String>) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_LOCALES);
		if ((langs != null) && !langs.isEmpty()) {
			for (String lang : langs) {
				if (locale.equals(new Locale(lang))) {
					return true;
				}
			}
			return false;
		}

		return Locale.getDefault().getLanguage().equals(locale.getLanguage());
	}

	@SuppressWarnings("unchecked")
	private List<Locale> getEntityReportLocales(NodeRef entityNodeRef) {
		List<Locale> ret = new ArrayList<>();
		List<String> langs = (List<String>) nodeService.getProperty(entityNodeRef, ReportModel.PROP_REPORT_LOCALES);
		if (langs != null) {
			for (String lang : langs) {
				ret.add(new Locale(lang));
			}
		} else {
			ret.add(new Locale(Locale.getDefault().getLanguage()));
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
		associationService.update(entityNodeRef, ReportModel.ASSOC_REPORTS, newReports);
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

	@Override
	public boolean shouldGenerateReport(NodeRef entityNodeRef, NodeRef documentNodeRef) {
		StopWatch watch = new StopWatch();
		if (logger.isDebugEnabled()) {
			watch.start();
		}
		try {
			Date modified = (Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_MODIFIED);
			Date formulatedDate = (Date) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_FORMULATED_DATE);
			
			if(formulatedDate!=null && modified!=null && formulatedDate.getTime()>modified.getTime()){
				modified = formulatedDate;
			}
			
			Date generatedReportDate = (Date) nodeService.getProperty(entityNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED);

			if (documentNodeRef != null) {
				Date documentGeneratedDate = (Date) nodeService.getProperty(documentNodeRef, ContentModel.PROP_MODIFIED);
				if ((documentGeneratedDate != null)
						&& ((generatedReportDate == null) || (generatedReportDate.getTime() < documentGeneratedDate.getTime()))) {
					generatedReportDate = documentGeneratedDate;
				}
			}

			if(logger.isDebugEnabled() ){
				logger.debug(" generated date :" +generatedReportDate);
				logger.debug(" modified date :" +modified);
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

			logger.debug("Check from extractor");

			return retrieveExtractor(entityNodeRef).shouldGenerateReport(entityNodeRef, generatedReportDate);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("ShouldGenerateReport executed in  " + watch.getTotalTimeSeconds() + " seconds ");
			}
		}
	}

	@Override
	public NodeRef getSelectedReport(NodeRef entityNodeRef) {

		String reportName = getSelectedReportName(entityNodeRef);

		List<NodeRef> dbReports = associationService.getTargetAssocs(entityNodeRef, ReportModel.ASSOC_REPORTS, false);

		NodeRef ret = null;

		for (NodeRef reportNodeRef : dbReports) {
			if (permissionService.hasPermission(reportNodeRef, "Read") == AccessStatus.ALLOWED) {

				NodeRef reportTemplateNodeRef = reportTplService.getAssociatedReportTemplate(reportNodeRef);
				if (reportTemplateNodeRef != null) {
					String templateName = (String) this.nodeService.getProperty(reportTemplateNodeRef, ContentModel.PROP_NAME);
					if (templateName.endsWith(RepoConsts.REPORT_EXTENSION_BIRT)) {
						templateName = templateName.replace("." + RepoConsts.REPORT_EXTENSION_BIRT, "");
					}

					Boolean isDefault = (Boolean) this.nodeService.getProperty(reportTemplateNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT);

					if (nodeService.hasAspect(reportNodeRef, ReportModel.ASPECT_REPORT_LOCALES)) {
						@SuppressWarnings("unchecked")
						List<String> langs = (List<String>) nodeService.getProperty(reportNodeRef, ReportModel.PROP_REPORT_LOCALES);
						if ((langs != null) && !langs.isEmpty()) {
							templateName += " - " + langs.get(0);
							isDefault = false;
						}
					}

					if (isDefault) {
						ret = reportNodeRef;
					}

					if (templateName.equalsIgnoreCase(reportName)) {
						return reportNodeRef;
					}
				}

			}
		}
		return ret;
	}

	@Override
	public String getSelectedReportName(NodeRef entityNodeRef) {

		String username = AuthenticationUtil.getFullyAuthenticatedUser();
		String typeName = nodeService.getType(entityNodeRef).toPrefixString(namespaceService).replace(":", "_");

		Map<String, Serializable> preferences = preferenceService.getPreferences(username);

		String reportName = (String) preferences.get(PREF_REPORT_PREFIX + typeName + PREF_REPORT_SUFFIX);

		if (logger.isDebugEnabled()) {
			logger.debug("Getting: " + reportName + " from preference for: " + username + " and type: " + typeName);
		}

		return reportName;
	}

	@Override
	public NodeRef getOrRefreshReport(NodeRef entityNodeRef, NodeRef documentNodeRef) {
		if (shouldGenerateReport(entityNodeRef, documentNodeRef)) {
			logger.debug("Entity report is not up to date for entity " + entityNodeRef+" document "+documentNodeRef);
			if(documentNodeRef == null){
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

	@Override
	public NodeRef getEntityNodeRef(NodeRef reportNodeRef) {
		List<NodeRef> entityNodeRefs = associationService.getSourcesAssocs(reportNodeRef, ReportModel.ASSOC_REPORTS);
		if (entityNodeRefs != null) {
			for (NodeRef entityNodeRef : entityNodeRefs) {
				return entityNodeRef;
			}
		}
		return null;
	}

}
