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
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AttachmentHelper;

public class ExportUsersWebScript extends AbstractWebScript {

	private NodeService nodeService;

	private PersonService personService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

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
			String csvHeader = "\"cm:lastName\";\"cm:firstName\";\"cm:email\";\"cm:telephone\";\"cm:organization\";\"username\";\"new_username\";\"password\";\"should_generate_password\";\"memberships\";\"groups\";\"notify\";\"is_ids_user\";\"disable\";\"delete\"\n";
			out.write(csvHeader.getBytes(StandardCharsets.UTF_8));

			PagingResults<PersonInfo> people = personService.getPeople(searchTerm, filterProps, null, new PagingRequest(Integer.MAX_VALUE));

			for (PersonInfo userNode : people.getPage()) {
				NodeRef userRef = userNode.getNodeRef();

				String lastName = getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_LASTNAME));
				String firstName = getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_FIRSTNAME));
				String email = getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_EMAIL));
				String telephone = getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_TELEPHONE));
				String organization = getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_ORGANIZATION));
				String userName = getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_USERNAME));
				String isSsoUser = getOrEmpty(nodeService.getProperty(userRef, BeCPGModel.PROP_IS_SSO_USER));
				String disabled = getOrEmpty(nodeService.hasAspect(userRef, ContentModel.ASPECT_PERSON_DISABLED));

				String csvRow = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s%n", lastName, firstName, email, telephone, organization,
						userName, "", "", "", "", "", "", isSsoUser, disabled, "");

				out.write(csvRow.getBytes(StandardCharsets.UTF_8));

			}
			out.flush();
		}
	}

	private String getOrEmpty(Object prop) {
		return prop == null ? "" : prop.toString();
	}

}
