/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
package fr.becpg.repo.report.template.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.report.template.ReportTplInformation;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.report.client.ReportFormat;

/**
 * <p>ReportTplServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("reportTplService")
public class ReportTplServiceImpl implements ReportTplService {

	private static final Log logger = LogFactory.getLog(ReportTplServiceImpl.class);

	@Autowired
	private NodeService nodeService;
	@Autowired
	private ContentService contentService;
	@Autowired
	private MimetypeService mimetypeService;
	@Autowired
	private AssociationService associationService;

	/**
	 * {@inheritDoc}
	 *
	 * Get the system report template
	 */
	@Override
	public List<NodeRef> getSystemReportTemplates(ReportType reportType, QName nodeType) {
		return getReportTpls(reportType, nodeType, true, null, null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the system report template
	 */
	@Override
	public NodeRef getSystemReportTemplate(ReportType reportType, QName nodeType, String tplName) {
		List<NodeRef> ret = getReportTpls(reportType, nodeType, true, null, tplName);

		return (ret != null) && !ret.isEmpty() ? ret.get(0) : null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the report templates of the product type that user can choose from
	 * UI.
	 */
	@Override
	public List<NodeRef> getUserReportTemplates(ReportType reportType, QName nodeType, String tplName) {
		return getReportTpls(reportType, nodeType, false, null, tplName);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Get the report template of the product type by name
	 */
	@Override
	public NodeRef getUserReportTemplate(ReportType reportType, QName nodeType, String tplName) {
		List<NodeRef> ret = getReportTpls(reportType, nodeType, false, null, tplName);

		return (ret != null) && !ret.isEmpty() ? ret.get(0) : null;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getDefaultReportTemplate(ReportType reportType, QName nodeType) {
		List<NodeRef> ret = getReportTpls(reportType, nodeType, false, true, null);

		return (ret != null) && !ret.isEmpty() ? ret.get(0) : null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create the rptdesign node for the report
	 */
	@Override
	public NodeRef createTplRptDesign(NodeRef parentNodeRef, String tplName, String tplFilePath, ReportTplInformation reportTplInformation, boolean overrideTpl) throws IOException {

		NodeRef reportTplNodeRef = null;
		ClassPathResource resource = new ClassPathResource(tplFilePath);
		if (resource.exists()) {
			try (InputStream in = new BufferedInputStream(resource.getInputStream())) {
				String extension = RepoConsts.REPORT_EXTENSION_BIRT;
				int i = tplFilePath.lastIndexOf('.');
				if (i > 0) {
					extension = tplFilePath.substring(i + 1);
				}
				String tplFullName = tplName + "." + extension;
				reportTplNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, tplFullName);

				Map<QName, Serializable> properties = new HashMap<>();
				properties.put(ContentModel.PROP_NAME, tplFullName);
				properties.put(ReportModel.PROP_REPORT_TPL_TYPE, reportTplInformation.getReportType());
				properties.put(ReportModel.PROP_REPORT_TPL_FORMAT, reportTplInformation.getReportFormat());
				properties.put(ReportModel.PROP_REPORT_TPL_CLASS_NAME, reportTplInformation.getNodeType() != null ? reportTplInformation.getNodeType().toString() : null);
				properties.put(ReportModel.PROP_REPORT_TPL_IS_SYSTEM, reportTplInformation.isSystemTpl());
				properties.put(ReportModel.PROP_REPORT_TPL_IS_DEFAULT, reportTplInformation.isDefaultTpl());
				if(reportTplInformation.getTextParameter()!=null) {
					properties.put(ReportModel.PROP_REPORT_TEXT_PARAMETERS, reportTplInformation.getTextParameter());
				}
				if(reportTplInformation.getSupportedLocale()!=null){
					properties.put(ReportModel.PROP_REPORT_LOCALES,(Serializable) reportTplInformation.getSupportedLocale());
				}
				

				if (reportTplNodeRef != null) {
					if (overrideTpl || (!nodeService.hasAspect(reportTplNodeRef, ContentModel.ASPECT_VERSIONABLE)
							|| RepoConsts.INITIAL_VERSION.equals(nodeService.getProperty(reportTplNodeRef, ContentModel.PROP_VERSION_LABEL)))) {
						if (overrideTpl) {
							logger.info("Override report content and properties: " + tplFullName);
							nodeService.addProperties(reportTplNodeRef, properties);
						} else {
							logger.info("Updating report content: " + tplFullName);
							
							List<NodeRef> ressources = associationService.getTargetAssocs(reportTplNodeRef, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES);
							if(reportTplInformation.getResources()!=null ) {
								ressources.addAll(reportTplInformation.getResources());
								
								associationService.update(reportTplNodeRef,  ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES, ressources);
							}
							
						}
					} else {
						return reportTplNodeRef;
					}
				} else {
					logger.info("Creating report template: " + tplFullName);

					reportTplNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
							ReportModel.TYPE_REPORT_TPL, properties).getChildRef();
					

					if(reportTplInformation.getReportKindAspectProperties()!=null) {
						nodeService.addAspect(reportTplNodeRef, ReportModel.ASPECT_REPORT_KIND, reportTplInformation.getReportKindAspectProperties());
					}
					
					if(reportTplInformation.getResources()!=null ) {
						associationService.update(reportTplNodeRef,  ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES, reportTplInformation.getResources());
					}
					
				}

				ContentWriter writer = contentService.getWriter(reportTplNodeRef, ContentModel.PROP_CONTENT, true);

				String mimetype = mimetypeService.guessMimetype(tplFilePath);
				ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
				Charset charset = charsetFinder.getCharset(in, mimetype);
				String encoding = charset.name();

				writer.setMimetype(mimetype);
				writer.setEncoding(encoding);
				writer.putContent(in);
			}
		} else {
			logger.error("Path doesn't exists: " + tplFilePath);
		}

		return reportTplNodeRef;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a ressource for the report
	 */
	@Override
	public NodeRef createTplRessource(NodeRef parentNodeRef, String xmlFilePath, boolean overrideRessource) throws IOException {

		ClassPathResource resource = new ClassPathResource(xmlFilePath);

		NodeRef fileNodeRef = null;
		if (resource.exists()) {
			try (InputStream in = new BufferedInputStream(resource.getInputStream())) {
				fileNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, resource.getFilename());

				if ((fileNodeRef == null) || overrideRessource || (!nodeService.hasAspect(fileNodeRef, ContentModel.ASPECT_VERSIONABLE)
						|| RepoConsts.INITIAL_VERSION.equals(nodeService.getProperty(fileNodeRef, ContentModel.PROP_VERSION_LABEL)))) {

					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, resource.getFilename());
					if (fileNodeRef == null) {
						fileNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
								ContentModel.TYPE_CONTENT, properties).getChildRef();
					}

					ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);

					String mimetype = mimetypeService.guessMimetype(xmlFilePath);
					ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
					Charset charset = charsetFinder.getCharset(in, mimetype);
					String encoding = charset.name();

					writer.setMimetype(mimetype);
					writer.setEncoding(encoding);
					writer.putContent(in);
				}
			}

		} else {
			logger.error("Resource not found. Path: " + xmlFilePath);
		}

		return fileNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> cleanDefaultTpls(List<NodeRef> tplsNodeRef) {

		List<NodeRef> defaultTplsNodeRef = new ArrayList<>();
		NodeRef userDefaultTplNodeRef = null;

		for (NodeRef tplNodeRef : tplsNodeRef) {

			Boolean isDefault = (Boolean) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT);
			Boolean isSystem = (Boolean) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_IS_SYSTEM);

			if (Boolean.TRUE.equals(isDefault)) {

				if (!Boolean.TRUE.equals(isSystem) && (userDefaultTplNodeRef == null)) {
					userDefaultTplNodeRef = tplNodeRef;
				} else {
					defaultTplsNodeRef.add(tplNodeRef);
				}
			}
		}

		// no user default tpl, take the first system default
		if ((userDefaultTplNodeRef == null) && !defaultTplsNodeRef.isEmpty()) {
			defaultTplsNodeRef.remove(0);
		}

		// remove the other system default
		for (NodeRef tplNodeRef : defaultTplsNodeRef) {

			tplsNodeRef.remove(tplNodeRef);
		}

		return tplsNodeRef;
	}

	/** {@inheritDoc} */
	@Override
	public ReportFormat getReportFormat(NodeRef tplNodeRef) {

		ReportType reportType = ReportType.parse((String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_TYPE));
		String format = (String) nodeService.getProperty(tplNodeRef, ReportModel.PROP_REPORT_TPL_FORMAT);

		if (format == null) {
			if (ReportType.ExportSearch.equals(reportType)) {
				return ReportFormat.XLSX;
			} else {
				return ReportFormat.PDF;
			}
		}

		return ReportFormat.valueOf(format);
	}

	private List<NodeRef> getReportTpls(ReportType reportType, QName nodeType, Boolean isSystem, Boolean isDefault, String tplName) {

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(ReportModel.TYPE_REPORT_TPL)
				.andPropEquals(ReportModel.PROP_REPORT_TPL_TYPE, reportType.toString());

		// Full text
		if ((tplName != null) && !Objects.equals(tplName, "*")) {
			queryBuilder.andPropQuery(ContentModel.PROP_NAME, tplName);
		} else {
			queryBuilder.inDB();
		}

		List<NodeRef> ret = new ArrayList<>();
		for (NodeRef rTplNodeRef : queryBuilder.list()) {

			QName classType = (QName) nodeService.getProperty(rTplNodeRef, ReportModel.PROP_REPORT_TPL_CLASS_NAME);

			if ((((classType == null) && (nodeType == null)) || ((classType != null) && classType.equals(nodeType)))
					&& ((isSystem == null) || isSystem.equals(nodeService.getProperty(rTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_SYSTEM)))
					&& ((isDefault == null) || isDefault.equals(nodeService.getProperty(rTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT)))
					&& !Boolean.TRUE.equals(nodeService.getProperty(rTplNodeRef, ReportModel.PROP_REPORT_TPL_IS_DISABLED))) {
				ret.add(rTplNodeRef);
			}
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getAssociatedReportTemplate(NodeRef nodeRef) {
		return associationService.getTargetAssoc(nodeRef, ReportModel.ASSOC_REPORT_TPL);

	}
}
