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
package fr.becpg.repo.importer.user.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.repo.authentication.BeCPGUserAccount;
import fr.becpg.repo.authentication.BeCPGUserAccountService;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.importer.impl.ImportHelper;
import fr.becpg.repo.importer.user.UserImporterService;

/**
 * <p>UserImporterServiceImpl class.</p>
 *
 * @author matthieu Csv Format:
 *         cm:lastName";"cm:firstName";"cm:email";"cm:telephone";"cm:
 *         organization";"username";"password";"memberships";"groups";"notify"
 * @version $Id: $Id
 */
public class UserImporterServiceImpl implements UserImporterService {


	private static final Log logger = LogFactory.getLog(UserImporterServiceImpl.class);

	public static final String ATTR_REMOVE = "Remove";
	/** Constant <code>USERNAME="username"</code> */
	public static final String ATTR_USERNAME = "username";
	/** Constant <code>PASSWORD="password"</code> */
	public static final String ATTR_PASSWORD = "password";
	public static final String ATTR_SHOULD_GENERATE_PASSWORD = "should_generate_password";
	public static final String ATTR_IS_IDS_USER = "is_ids_user";
	public static final String ATTR_NEW_USERNAME = "new_username";
	public static final String ATTR_DISABLE = "disable";
	/** Constant <code>NOTIFY="notify"</code> */
	public static final String ATTR_NOTIFY = "notify";
	/** Constant <code>MEMBERSHIPS="memberships"</code> */
	public static final String ATTR_MEMBERSHIPS = "memberships";
	/** Constant <code>GROUPS="groups"</code> */
	public static final String ATTR_GROUPS = "groups";

	/** The Constant SEPARATOR. */
	private static final char SEPARATOR = ';';

	/** Constant <code>FIELD_SEPARATOR="\\|"</code> */
	protected static final String FIELD_SEPARATOR = "\\|";

	/** Constant <code>DEFAULT_PRESET="site-dashboard"</code> */
	protected static final String DEFAULT_PRESET = "site-dashboard";

	private NodeService nodeService;

	private ContentService contentService;

	private SiteService siteService;

	private MutableAuthenticationService authenticationService;

	private BeCPGUserAccountService beCPGUserAccountService;

	private NamespacePrefixResolver namespacePrefixResolver;

	private SysAdminParams sysAdminParams;
	
	
	
	
	/**
	 * <p>Setter for the field <code>sysAdminParams</code>.</p>
	 *
	 * @param sysAdminParams a {@link org.alfresco.repo.admin.SysAdminParams} object
	 */
	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}

	/**
	 * <p>Setter for the field <code>namespacePrefixResolver</code>.</p>
	 *
	 * @param namespacePrefixResolver a {@link org.alfresco.service.namespace.NamespacePrefixResolver} object.
	 */
	public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>siteService</code>.</p>
	 *
	 * @param siteService a {@link org.alfresco.service.cmr.site.SiteService} object.
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * <p>Setter for the field <code>beCPGUserAccountService</code>.</p>
	 *
	 * @param beCPGUserAccountService a {@link fr.becpg.repo.authentication.BeCPGUserAccountService} object
	 */
	public void setBeCPGUserAccountService(BeCPGUserAccountService beCPGUserAccountService) {
		this.beCPGUserAccountService = beCPGUserAccountService;
	}



	/**
	 * <p>Setter for the field <code>authenticationService</code>.</p>
	 *
	 * @param authenticationService a {@link org.alfresco.service.cmr.security.MutableAuthenticationService} object
	 */
	public void setAuthenticationService(MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	/** {@inheritDoc} */
	@Override
	public void importUser(NodeRef nodeRef) throws ImporterException {
		if ((nodeRef == null) || !nodeService.exists(nodeRef)) {
			throw new ImporterException("Invalid parameter");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Node exists : " + nodeRef.getId());
		}

		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

		try (InputStream is = reader.getContentInputStream()) {

			if (logger.isDebugEnabled()) {
				logger.debug("Reading Import File");
			}
			Charset charset = ImportHelper.guestCharset(is, reader.getEncoding());
			if (logger.isDebugEnabled()) {
				logger.debug("reader.getEncoding() : " + reader.getEncoding());
				logger.debug("finder.getEncoding() : " + charset);
			}

			proccessUpload(is, (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME), charset);

		} catch (IOException e) {
			logger.error(e, e);
			throw new ImporterException("Cannot import user : " + e.getMessage(), e);
		} catch (Exception e) {
			if (RetryingTransactionHelper.extractRetryCause(e) != null) {
			    throw e;
            }
			logger.error(e, e);
			throw new ImporterException("Cannot import user : " + e.getMessage(), e);
		}

	}

	private void proccessUpload(InputStream input, String filename, Charset charset) throws IOException, ImporterException {
		if ((filename != null) && (filename.length() > 0)) {
			if (filename.endsWith(".csv")) {
				processCSVUpload(input, charset);
				return;
			}
			if (filename.endsWith(".xls")) {
				processXLSUpload(input);
				return;
			}
			if (filename.endsWith(".xlsx")) {
				processXLSXUpload(input);
				return;
			}
		}
		// If in doubt, assume it's probably a .csv
		processCSVUpload(input, charset);

	}

	private void processXLSXUpload(InputStream input) {
		logger.info("Not wet implemented");
	}

	private void processXLSUpload(InputStream input) {
		logger.info("Not wet implemented");
	}

	private void processCSVUpload(InputStream input, Charset charset) throws IOException, ImporterException {

		try (InputStreamReader reader = new InputStreamReader(input, charset)) {
			try (CSVReader csvReader = new CSVReader(reader, SEPARATOR)) {
				String[] splitted;
				boolean isFirst = true;
				Map<String, Integer> headers = new HashMap<>();
				while ((splitted = csvReader.readNext()) != null) {
					if (isFirst) {
						headers = processHeaders(splitted);
						isFirst = false;
					} else if (splitted.length == headers.size()) {
						processRow(headers, splitted);
					}
				}
			}
		}

	}

	private void processRow(final Map<String, Integer> headers, final String[] splitted) {

		if ((splitted != null) && (headers != null)) {
			BeCPGUserAccount userAccount = new BeCPGUserAccount();

			String username = splitted[headers.get(ATTR_USERNAME)];
			username = username != null ? username.toLowerCase() : null;
			
			userAccount.setUserName(username);
			if (headers.containsKey(ATTR_PASSWORD)) {
				userAccount.setPassword(splitted[headers.get(ATTR_PASSWORD)]);
			}
			if (headers.containsKey(ATTR_SHOULD_GENERATE_PASSWORD)) {
				userAccount.setGeneratePassword(Boolean.parseBoolean(splitted[headers.get(ATTR_SHOULD_GENERATE_PASSWORD)].toLowerCase()));
			}
			if (headers.containsKey(ATTR_IS_IDS_USER)) {
				userAccount.setSynchronizeWithIDS(Boolean.parseBoolean(splitted[headers.get(ATTR_IS_IDS_USER)].toLowerCase()));
			}
			if (headers.containsKey(ATTR_DISABLE)) {
				userAccount.setDisable(Boolean.parseBoolean(splitted[headers.get(ATTR_DISABLE)].toLowerCase()));
			}
			if (headers.containsKey(ATTR_NEW_USERNAME)) {
				userAccount.setNewUserName(splitted[headers.get(ATTR_NEW_USERNAME)]);
			}
			userAccount.setNotify(headers.containsKey(ATTR_NOTIFY) && Boolean.parseBoolean(splitted[headers.get(ATTR_NOTIFY)].toLowerCase()));

			for (Map.Entry<String, Integer> entry : headers.entrySet()) {
				if (isPropQname(entry.getKey()) && !splitted[entry.getValue()].isEmpty()) {
					QName prop = QName.resolveToQName(namespacePrefixResolver, entry.getKey());
					String value = splitted[entry.getValue()];

					logger.debug("Adding : " + prop + " " + value);
					userAccount.getExtraProps().put(prop, value);

				}

			}

			if (headers.containsKey(ATTR_GROUPS)) {
				String[] groups = splitted[headers.get(ATTR_GROUPS)].split(FIELD_SEPARATOR);
				for (String group : groups) {
					userAccount.getAuthorities().add(group);
				}
			}

			 beCPGUserAccountService.getOrCreateUser(userAccount);

			if (headers.containsKey(ATTR_MEMBERSHIPS)) {
				AuthenticationUtil.runAsSystem(() -> {
					if ((splitted[headers.get(ATTR_MEMBERSHIPS)] != null) && !splitted[headers.get(ATTR_MEMBERSHIPS)].isEmpty()) {

						String[] memberships = splitted[headers.get(ATTR_MEMBERSHIPS)].split(FIELD_SEPARATOR);
						for (String membership : memberships) {

							String[] sites = membership.split("_");
							String siteName = formatSiteName(sites[0]);
							String role = SiteModel.SITE_CONSUMER;
							if (sites.length > 1) {
								role = formatRole(sites[1]);
							}

							if (logger.isDebugEnabled()) {
								logger.debug("Adding role " + role + " to " + userAccount.getUserName() + " on site " + siteName);
							}
							String finalSiteName = cleanSiteName(siteName);
							if (siteService.getSite(finalSiteName) != null) {
								if (ATTR_REMOVE.equals(role)) {
									siteService.removeMembership(finalSiteName, userAccount.getUserName());
								} else {
									siteService.setMembership(finalSiteName, userAccount.getUserName(), role);
								}
							} else if (!ATTR_REMOVE.equals(role)) {
								logger.debug("Site " + siteName + " doesn't exist.");

								SiteInfo siteInfo = siteService.createSite(DEFAULT_PRESET, finalSiteName, siteName, "",
										SiteVisibility.PUBLIC);
								try {

									URL url = new URL(sysAdminParams.getShareProtocol()+"://"+sysAdminParams.getShareHost()
									+":"+sysAdminParams.getSharePort()+"/share/service/modules/enable-site?url=" + siteInfo.getShortName()
											+ "&preset=" + DEFAULT_PRESET + "&alf_ticket=" + authenticationService.getCurrentTicket());
									URLConnection con = url.openConnection();

									InputStream in = con.getInputStream();
									if (in != null) {
										in.close();
									}

								} catch (IOException e) {
									logger.error("Unable to enable site", e);
								}

								siteService.setMembership(siteInfo.getShortName(), userAccount.getUserName(), role);

							}

						}
					}
					return null;
				});

			}
		}
	}

	private String cleanSiteName(String siteName) {
		return siteName.toLowerCase().replace("&", "").replaceAll("\\s", "").replaceAll("[àáâãäå]", "a").replace("æ", "ae").replace("ç", "c")
				.replaceAll("[èéêë]", "e").replaceAll("[ìíîï]", "i").replace("ñ", "n").replaceAll("[òóôõö]", "o").replace("œ", "oe")
				.replaceAll("[ùúûü]", "u").replaceAll("[ýÿ]", "y");
	}

	private boolean isPropQname(String key) {
		return !(ATTR_GROUPS.equals(key) || ATTR_MEMBERSHIPS.equals(key) || ATTR_PASSWORD.equals(key) || ATTR_USERNAME.equals(key) || ATTR_NOTIFY.equals(key));
	}

	private Map<String, Integer> processHeaders(String[] splitted) throws ImporterException {
		Map<String, Integer> headers = new HashMap<>();
		for (int i = 0; i < splitted.length; i++) {
			logger.debug("Adding header: " + splitted[i]);
			headers.put(splitted[i], i);
		}
		verifyHeaders(headers);
		return headers;
	}

	private void verifyHeaders(Map<String, Integer> headers) throws ImporterException {
		if (!headers.containsKey(ATTR_USERNAME) && (headers.size() < 4)) {
			throw new ImporterException("Invalid headers");
		}
	}

	private String formatRole(String role) {
		if (role != null) {
			if (role.trim().equalsIgnoreCase("Contributor")) {
				return SiteModel.SITE_CONTRIBUTOR;
			} else if (role.trim().equalsIgnoreCase("Collaborator")) {
				return SiteModel.SITE_COLLABORATOR;
			} else if (role.trim().equalsIgnoreCase("Manager")) {
				return SiteModel.SITE_MANAGER;
			} else if (role.trim().equalsIgnoreCase(ATTR_REMOVE)) {
				return ATTR_REMOVE;
			}
		}

		return SiteModel.SITE_CONSUMER;
	}

	private String formatSiteName(String siteName) {
		return siteName.replace(" ", "");
	}

}
