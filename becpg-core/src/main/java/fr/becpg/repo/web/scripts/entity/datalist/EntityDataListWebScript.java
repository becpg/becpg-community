/*
 *
 */
package fr.becpg.repo.web.scripts.entity.datalist;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.repo.entity.datalist.AsyncPaginatedExtractorWrapper;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.web.scripts.BrowserCacheHelper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Webscript that send the result of a datalist
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityDataListWebScript extends AbstractEntityDataListWebScript {

	private static final Log logger = LogFactory.getLog(EntityDataListWebScript.class);

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
			logger.debug("EntityDataListWebScript executeImpl()");
		}

		Locale currentLocal = null;
		if (req.getParameter(PARAM_LOCALE) != null) {
			currentLocal = I18NUtil.getContentLocale();
			I18NUtil.setContentLocale(MLTextHelper.parseLocale(req.getParameter(PARAM_LOCALE)));
		}

		String async = req.getParameter(PARAM_ASYNC);

		try {

			DataListFilter dataListFilter = getOrCreateDataListFilter(req);

			if (logger.isDebugEnabled()) {
				logger.debug("Filter:" + dataListFilter.toString());
				logger.debug("Pagination:" + dataListFilter.getPagination().toString());
				logger.debug("MetadataFields:" + dataListFilter.getMetadataFields().toString());
				logger.debug("SearchQuery:" + dataListFilter.getSearchQuery());
			}

			DataListExtractor extractor = dataListExtractorFactory.getExtractor(dataListFilter);

			if (logger.isDebugEnabled()) {
				logger.debug("Using extractor: " + extractor.getClass().getSimpleName());
			}

			boolean hasWriteAccess = !dataListFilter.isVersionFilter();
			boolean hasReadAccess = true;
			if (!dataListFilter.getEntityNodeRefs().isEmpty()) {
				NodeRef entityNodeRef = dataListFilter.getEntityNodeRefs().get(0);
				QName entityNodeRefType = nodeService.getType(entityNodeRef);

				int accessMode = securityService.computeAccessMode(entityNodeRef, entityNodeRefType, dataListFilter.getDataType());
				hasReadAccess = accessMode != SecurityService.NONE_ACCESS;

				if (hasReadAccess && hasWriteAccess) {

					hasWriteAccess = extractor.hasWriteAccess() && !nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_CHECKED_OUT)
							&& !nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
							&& (lockService.getLockStatus(entityNodeRef) == LockStatus.NO_LOCK) && (accessMode == SecurityService.WRITE_ACCESS)
							&& becpgLicenseManager.hasWriteLicense() && isExternalUserAllowed(dataListFilter);

					if (hasWriteAccess && (dataListFilter.getParentNodeRef() != null) && (dataListFilter.getDataType() != null)
							&& !dataListFilter.getDataType().getLocalName().equals(dataListFilter.getDataListName())) {
						String dataListType = (String) nodeService.getProperty(dataListFilter.getParentNodeRef(),
								DataListModel.PROP_DATALISTITEMTYPE);

						if ((dataListType != null) && !dataListType.isEmpty()) {
							QName dataListTypeQName = QName.createQName(dataListType, namespaceService);
							hasWriteAccess = securityService.computeAccessMode(entityNodeRef, entityNodeRefType,
									dataListTypeQName) == SecurityService.WRITE_ACCESS;
						}
					}
				}
			}
			PaginatedExtractedItems extractedItems;
			if (hasReadAccess) {
				dataListFilter.setHasWriteAccess(hasWriteAccess);

				Date lastModified = extractor.computeLastModified(dataListFilter);

				if (BrowserCacheHelper.shouldReturnNotModified(req, lastModified)) {
					res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					if (logger.isDebugEnabled()) {
						logger.debug("Send Not_MODIFIED status");
					}
					return;
				}

				Cache cache = new Cache(getDescription().getRequiredCache());
				cache.setIsPublic(false);
				cache.setMustRevalidate(true);
				cache.setNeverCache(false);
				cache.setMaxAge(0L);
				cache.setLastModified(lastModified);
				res.setCache(cache);

				if ("true".equals(async)) {
					extractedItems = new AsyncPaginatedExtractorWrapper(extractor, dataListFilter);
				} else {
					extractedItems = extractor.extract(dataListFilter);
				}
			} else {
				extractedItems = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());
			}

			datalistOutputWriterFactory.write(req, res, dataListFilter, extractedItems);

		} catch (JSONException e) {
			throw new WebScriptException("Unable to parse JSON", e);
		} finally {
			if (currentLocal != null) {
				I18NUtil.setContentLocale(currentLocal);
			}

			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("EntityDataListWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

}
