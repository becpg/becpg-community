/*
 * Copyright (C) 2010-2026 beCPG.
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
 */
package fr.becpg.repo.admin.patch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.olap.data.OlapChart;

/**
 * Gives the OLAP queries, dashboards and applications already installed the technical id they now
 * carry, so that nothing keeps identifying them by a translated label.
 *
 * <p>#24931: before this patch a resource was identified by its file name, which beCPG shipped
 * translated. Renaming the files therefore had to come with a migration, or every reference would
 * break at once and without a trace: a dashboard would point at a query that no longer exists, and
 * the Share dashlet would find nothing behind the query id stored in each user's preferences.
 *
 * <p>The three are migrated together and in that order, since the mapping from the old identity to
 * the new one is built while walking the folder:
 * <ol>
 *   <li>every installed resource receives {@code bcpg:olapQueryId} and a {@code cm:title} in every
 *       language shipped, its name being left untouched;</li>
 *   <li>the dashboards and applications have their query references rewritten to those ids;</li>
 *   <li>the OLAP dashlet preference of every user is rewritten to the same ids.</li>
 * </ol>
 *
 * <p>A query a user saved from Saiku carries no id: it is left exactly as it is, its name remaining
 * its identity.
 *
 * @author matthieu
 */
public class OlapQueryIdPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(OlapQueryIdPatch.class);

	private static final String MSG_SUCCESS = "patch.bcpg.olapQueryIdPatch.result";

	/** Bundle holding the shipped labels, keyed by technical id. */
	private static final String LABEL_BUNDLE = "beCPG.olap.olapQueries";

	private static final String LABEL_KEY_PREFIX = "olap.";

	/** Preference holding the query selected in an OLAP dashlet, one per dashlet region. */
	private static final String PREFERENCE_ROOT = "fr.becpg.olap.chart.dashlet";

	private static final String PREFERENCE_QUERY_SUFFIX = ".query";

	private static final String REFERENCE_PREFIX = "\"path\": \"/";

	private ContentService contentService;

	private PreferenceService preferenceService;

	private PersonService personService;

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>preferenceService</code>.</p>
	 *
	 * @param preferenceService a {@link org.alfresco.service.cmr.preference.PreferenceService} object
	 */
	public void setPreferenceService(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	/**
	 * <p>Setter for the field <code>personService</code>.</p>
	 *
	 * @param personService a {@link org.alfresco.service.cmr.security.PersonService} object
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {
		NodeRef folderNodeRef = repoService.getFolderByPath("/" + RepoConsts.PATH_SYSTEM + "/" + RepoConsts.PATH_OLAP_QUERIES);
		if (folderNodeRef == null) {
			logger.info("No OLAP query folder on this repository, nothing to migrate");
			return I18NUtil.getMessage(MSG_SUCCESS, 0, 0, 0);
		}

		Map<String, String> idByLabel = indexIdByLabel();
		Map<String, String> idByOldIdentity = new HashMap<>();

		int migrated = stampTechnicalIds(folderNodeRef, idByLabel, idByOldIdentity);
		int rewritten = rewriteReferences(folderNodeRef, idByOldIdentity);
		int preferences = rewritePreferences(idByOldIdentity);

		return I18NUtil.getMessage(MSG_SUCCESS, migrated, rewritten, preferences);
	}

	/**
	 * Stamps every installed resource that beCPG ships with its technical id and its translated
	 * title, and records what that resource used to be identified by.
	 *
	 * @param folderNodeRef the shared OLAP query folder
	 * @param idByLabel the technical id of every shipped label, in every language shipped
	 * @param idByOldIdentity filled with the old identities the resource answered to
	 * @return the number of resources stamped
	 */
	private int stampTechnicalIds(NodeRef folderNodeRef, Map<String, String> idByLabel, Map<String, String> idByOldIdentity) {
		int migrated = 0;

		for (ChildAssociationRef child : nodeService.getChildAssocs(folderNodeRef)) {
			NodeRef nodeRef = child.getChildRef();
			if (nodeService.getProperty(nodeRef, BeCPGModel.PROP_OLAP_QUERY_ID) != null) {
				continue;
			}

			String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			String label = stripExtension(name);
			String olapQueryId = idByLabel.get(label);
			if (olapQueryId == null) {
				logger.debug("Leaving " + name + " alone: not a resource shipped by beCPG");
				continue;
			}

			recordOldIdentities(nodeRef, label, olapQueryId, idByOldIdentity);

			nodeService.addAspect(nodeRef, BeCPGModel.ASPECT_OLAP_QUERY, null);
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_OLAP_QUERY_ID, olapQueryId);
			setTitles(nodeRef, olapQueryId);
			migrated++;
		}

		logger.info("Stamped " + migrated + " OLAP resources with their technical id");

		return migrated;
	}

	/**
	 * Records the identities a resource used to answer to: its translated name, and the query id
	 * its content declares, which the Share dashlet stored in the user preferences.
	 *
	 * @param nodeRef the resource being migrated
	 * @param label its name, extension stripped
	 * @param olapQueryId the technical id it now carries
	 * @param idByOldIdentity the mapping to fill
	 */
	private void recordOldIdentities(NodeRef nodeRef, String label, String olapQueryId, Map<String, String> idByOldIdentity) {
		idByOldIdentity.put(label, olapQueryId);

		try {
			ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
			if (reader == null) {
				return;
			}

			OlapChart chart = new OlapChart(label);
			chart.load(reader.getContentString());
			if (chart.getQueryId() != null) {
				idByOldIdentity.put(chart.getQueryId(), olapQueryId);
			}
		} catch (Exception e) {
			logger.warn("Cannot read the previous query id of " + label + ", its dashlet preference will be reset", e);
		}
	}

	/**
	 * Rewrites the query references a dashboard or an application holds, which used to be the
	 * translated file name of the query.
	 *
	 * @param folderNodeRef the shared OLAP query folder
	 * @param idByOldIdentity the mapping from the old identity to the technical id
	 * @return the number of documents rewritten
	 */
	private int rewriteReferences(NodeRef folderNodeRef, Map<String, String> idByOldIdentity) {
		int rewritten = 0;

		for (ChildAssociationRef child : nodeService.getChildAssocs(folderNodeRef)) {
			NodeRef nodeRef = child.getChildRef();
			String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			if ((name == null) || !(name.endsWith(".saikudash") || name.endsWith(".saikuapp"))) {
				continue;
			}

			if (rewriteDocumentReferences(nodeRef, idByOldIdentity)) {
				rewritten++;
			}
		}

		logger.info("Rewrote the query references of " + rewritten + " OLAP dashboards and applications");

		return rewritten;
	}

	private boolean rewriteDocumentReferences(NodeRef nodeRef, Map<String, String> idByOldIdentity) {
		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		if (reader == null) {
			return false;
		}

		String content = reader.getContentString();
		String updated = content;
		for (Map.Entry<String, String> entry : idByOldIdentity.entrySet()) {
			updated = updated.replace(REFERENCE_PREFIX + entry.getKey() + ".saiku\"", REFERENCE_PREFIX + entry.getValue() + ".saiku\"");
		}

		if (updated.equals(content)) {
			return false;
		}

		ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		writer.setMimetype(reader.getMimetype());
		writer.setEncoding(reader.getEncoding());
		writer.putContent(updated);

		return true;
	}

	/**
	 * Rewrites the OLAP dashlet preference of every user, which holds the query id it must select.
	 *
	 * <p>A preference pointing at something no longer shipped is left untouched: the dashlet falls
	 * back to its first query, which is a better outcome than silently choosing another one.
	 *
	 * @param idByOldIdentity the mapping from the old identity to the technical id
	 * @return the number of preferences rewritten
	 */
	private int rewritePreferences(Map<String, String> idByOldIdentity) {
		int rewritten = 0;

		Set<String> userNames = new HashSet<>();
		for (NodeRef person : personService.getAllPeople()) {
			userNames.add((String) nodeService.getProperty(person, ContentModel.PROP_USERNAME));
		}

		for (String userName : userNames) {
			rewritten += rewritePreferences(userName, idByOldIdentity);
		}

		logger.info("Rewrote " + rewritten + " OLAP dashlet preferences over " + userNames.size() + " users");

		return rewritten;
	}

	private int rewritePreferences(String userName, Map<String, String> idByOldIdentity) {
		Map<String, Serializable> preferences = preferenceService.getPreferences(userName, PREFERENCE_ROOT);
		if ((preferences == null) || preferences.isEmpty()) {
			return 0;
		}

		Map<String, Serializable> updated = new HashMap<>();
		for (Map.Entry<String, Serializable> entry : preferences.entrySet()) {
			if (!entry.getKey().endsWith(PREFERENCE_QUERY_SUFFIX) || !(entry.getValue() instanceof String value)) {
				continue;
			}

			String olapQueryId = idByOldIdentity.get(value);
			if ((olapQueryId != null) && !olapQueryId.equals(value)) {
				updated.put(entry.getKey(), olapQueryId);
			}
		}

		if (updated.isEmpty()) {
			return 0;
		}

		preferenceService.setPreferences(userName, updated);

		return updated.size();
	}

	/**
	 * Indexes the technical id of every shipped resource by the label it carries, in every language
	 * shipped: an instance installed in French must be recognised from its French names.
	 *
	 * @return the technical id of every known label
	 */
	private Map<String, String> indexIdByLabel() {
		Map<String, String> ret = new HashMap<>();

		for (String key : RepoConsts.SUPPORTED_UI_LOCALES.split(",")) {
			Locale locale = MLTextHelper.parseLocale(key);
			ResourceBundle bundle = bundleFor(locale);
			if (bundle == null) {
				continue;
			}
			for (String labelKey : bundle.keySet()) {
				if (labelKey.startsWith(LABEL_KEY_PREFIX)) {
					ret.put(bundle.getString(labelKey), labelKey.substring(LABEL_KEY_PREFIX.length()));
				}
			}
		}

		return ret;
	}

	/**
	 * Writes the multilingual title of a migrated resource.
	 *
	 * <p>Without the ML-aware flag the interceptor keeps only the value of the current locale, and
	 * every other language is lost on the way in.
	 *
	 * @param nodeRef the resource being migrated
	 * @param olapQueryId its technical id
	 */
	private void setTitles(NodeRef nodeRef, String olapQueryId) {
		MLText titles = new MLText();

		for (String key : RepoConsts.SUPPORTED_UI_LOCALES.split(",")) {
			Locale locale = MLTextHelper.parseLocale(key);
			ResourceBundle bundle = bundleFor(locale);
			if ((bundle != null) && locale.getLanguage().equals(bundle.getLocale().getLanguage())) {
				titles.addValue(locale, bundle.getString(LABEL_KEY_PREFIX + olapQueryId));
			}
		}

		boolean isMLAware = MLPropertyInterceptor.setMLAware(true);
		try {
			nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, titles);
		} finally {
			MLPropertyInterceptor.setMLAware(isMLAware);
		}
	}

	/**
	 * Loads the label bundle of a locale, or null when no module on the classpath ships one, which
	 * is the case of a repository installed without the PLM module.
	 *
	 * @param locale the locale to load
	 * @return the bundle, or null
	 */
	private static ResourceBundle bundleFor(Locale locale) {
		try {
			return ResourceBundle.getBundle(LABEL_BUNDLE, locale);
		} catch (MissingResourceException e) {
			return null;
		}
	}

	private static String stripExtension(String name) {
		if (name == null) {
			return "";
		}
		int dot = name.lastIndexOf('.');
		return (dot > 0) ? name.substring(0, dot) : name;
	}

}
