package fr.becpg.repo.web.scripts.users;

import java.io.IOException;
import java.io.OutputStream;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AttachmentHelper;
import fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles;

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

	private static final List<String> XLSX_COLUMNS = List.of(
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
	
	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object
	 */
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

		res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		res.setContentEncoding("UTF-8");
		AttachmentHelper.setAttachment(req, res, "exported_users.xlsx");
		res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		res.setHeader("Pragma", "no-cache");

		try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream out = res.getOutputStream()) {
			
			Sheet sheet = workbook.createSheet("Users");
			
			ExcelCellStyles excelCellStyles = new ExcelCellStyles(workbook);

			// Create header row
			Row headerRow = sheet.createRow(0);
			for (int i = 0; i < XLSX_COLUMNS.size(); i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(XLSX_COLUMNS.get(i));
				cell.setCellStyle(excelCellStyles.getHeaderStyle());
			}

			PagingResults<PersonInfo> people = personService.getPeople(searchTerm, filterProps, null, new PagingRequest(Integer.MAX_VALUE));

			int rowNum = 1;
			for (PersonInfo userNode : people.getPage()) {
				NodeRef userRef = userNode.getNodeRef();
				Row row = sheet.createRow(rowNum++);
				
				for (int i = 0; i < XLSX_COLUMNS.size(); i++) {
					String column = XLSX_COLUMNS.get(i);
					Cell cell = row.createCell(i);
					cell.setCellStyle(excelCellStyles.getTextCellStyle());
					
					String value = "";
					if (LAST_NAME.equals(column)) {
						value = getOrEmpty(userNode.getLastName());
					} else if (FIRST_NAME.equals(column)) {
						value = getOrEmpty(userNode.getFirstName());
					} else if (EMAIL.equals(column)) {
						value = getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_EMAIL));
					} else if (TELEPHONE.equals(column)) {
						value = getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_TELEPHONE));
					} else if (ORGANIZATION.equals(column)) {
						value = getOrEmpty(nodeService.getProperty(userRef, ContentModel.PROP_ORGANIZATION));
					} else if (USERNAME.equals(column)) {
						value = getOrEmpty(userNode.getUserName());
					} else if (IS_IDS_USER.equals(column)) {
						value = getOrEmpty(nodeService.getProperty(userRef, BeCPGModel.PROP_IS_SSO_USER));
					} else if (DISABLE.equals(column)) {
						value = getOrEmpty(nodeService.getProperty(userRef, ContentModel.ASPECT_PERSON_DISABLED));
					} else if (GROUPS.equals(column)) {
						value = getOrEmpty(String.join(",", authorityService.getContainingAuthoritiesInZone(AuthorityType.GROUP,
								userNode.getUserName(), AuthorityService.ZONE_APP_DEFAULT, null, 1000)));
					}
					
					cell.setCellValue(value);
				}
			}
			
			// Auto-size columns
			for (int i = 0; i < XLSX_COLUMNS.size(); i++) {
				sheet.autoSizeColumn(i);
			}
			
			workbook.write(out);
			out.flush();
		}
	}

	private String getOrEmpty(Object prop) {
		return prop == null ? "" : prop.toString();
	}
	
}