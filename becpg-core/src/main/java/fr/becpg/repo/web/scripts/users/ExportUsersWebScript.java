package fr.becpg.repo.web.scripts.users;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AttachmentHelper;

/**
 * <p>ExportUsersWebScript class.</p>
 *
 * @author matthieu
 */
public class ExportUsersWebScript extends AbstractWebScript {

	private NodeService nodeService;

	private PersonService personService;
	
	private AuthorityService authorityService;

	private static final String LAST_NAME = "cm:lastName";
	private static final String FIRST_NAME = "cm:firstName";
	private static final String EMAIL = "cm:email";
	private static final String TELEPHONE = "cm:telephone";
	private static final String ORGANIZATION = "cm:organization";
	private static final String USERNAME = "username";
	private static final String NEW_USERNAME = "new_username";
	private static final String PASSWORD = "password";
	private static final String SHOULD_GENERATE_PASSWORD = "should_generate_password";
	private static final String MEMBERSHIPS = "memberships";
	private static final String GROUPS = "groups";
	private static final String NOTIFY = "notify";
	private static final String IS_IDS_USER = "is_ids_user";
	private static final String DISABLE = "disable";
	private static final String DELETE = "delete";

	private static final List<String> CSV_COLUMNS = List.of(
			LAST_NAME,
			FIRST_NAME,
			EMAIL,
			TELEPHONE,
			ORGANIZATION,
			USERNAME,
			NEW_USERNAME,
			PASSWORD,
			SHOULD_GENERATE_PASSWORD,
			MEMBERSHIPS,
			GROUPS,
			NOTIFY,
			IS_IDS_USER,
			DISABLE,
			DELETE
	);
	
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	
	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
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
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String searchTerm = req.getParameter("search");
		ParameterCheck.mandatory("searchTerm", searchTerm);
		
		List<QName> filterProps = null;
		if (!searchTerm.equals("*")) {
			searchTerm = searchTerm.replace("\\", "").replace("\"", "");
			filterProps = new ArrayList<>(3);
			filterProps.add(ContentModel.PROP_FIRSTNAME);
			filterProps.add(ContentModel.PROP_LASTNAME);
			filterProps.add(ContentModel.PROP_USERNAME);
		}

		res.setContentType("text/csv");
		res.setContentEncoding("UTF-8");
		AttachmentHelper.setAttachment(req, res, "exported_users.csv");
		res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		res.setHeader("Pragma", "no-cache");

		try (OutputStream out = res.getOutputStream()) {
			
			String csvHeader = String.join("", CSV_COLUMNS.stream().map(c -> "\"" + c + "\";").toList()) + "\n";
			out.write(csvHeader.getBytes(StandardCharsets.UTF_8));

			PagingResults<PersonInfo> people = personService.getPeople(searchTerm, filterProps, null, new PagingRequest(Integer.MAX_VALUE));

			for (PersonInfo userNode : people.getPage()) {
				NodeRef userRef = userNode.getNodeRef();
				List<String> csvValues = new ArrayList<>();
				for (String column : CSV_COLUMNS) {
					if (LAST_NAME.equals(column)) {
						csvValues.add(getOrEmpty(userNode.getLastName()));
					} else if (FIRST_NAME.equals(column)) {
						csvValues.add(getOrEmpty(userNode.getFirstName()));
					} else if (EMAIL.equals(column)) {
						csvValues.add(getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_EMAIL)));
					} else if (TELEPHONE.equals(column)) {
						csvValues.add(getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_TELEPHONE)));
					} else if (ORGANIZATION.equals(column)) {
						csvValues.add(getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_ORGANIZATION)));
					} else if (USERNAME.equals(column)) {
						csvValues.add(getOrEmpty(userNode.getUserName()));
					} else if (IS_IDS_USER.equals(column)) {
						csvValues.add(getOrEmpty(nodeService.getProperty(userRef, BeCPGModel.PROP_IS_SSO_USER)));
					} else if (DISABLE.equals(column)) {
						csvValues.add(getOrEmpty(nodeService.getProperty(userRef, ContentModel.ASPECT_PERSON_DISABLED)));
					} else if (GROUPS.equals(column)) {
						csvValues.add("\"" + getOrEmpty(String.join(",", authorityService.getContainingAuthoritiesInZone(AuthorityType.GROUP,
								userNode.getUserName(), AuthorityService.ZONE_APP_DEFAULT, null, 1000))) + "\"");
					} else {
						csvValues.add("");
					}
				}
				String csvRow = String.join("", csvValues.stream().map(v -> v + ";").toList()) + "\n";
				out.write(csvRow.getBytes(StandardCharsets.UTF_8));
			}
			out.flush();
		}
	}

	private String getOrEmpty(Object prop) {
		return prop == null ? "" : prop.toString();
	}

}
