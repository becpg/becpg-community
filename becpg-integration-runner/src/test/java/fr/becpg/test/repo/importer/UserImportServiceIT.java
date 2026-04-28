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
package fr.becpg.test.repo.importer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.authentication.BeCPGUserAccount;
import fr.becpg.repo.authentication.BeCPGUserAccountService;
import fr.becpg.repo.authentication.provider.IdentityServiceAccountProvider;
import fr.becpg.repo.importer.user.UserImporterService;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

public class UserImportServiceIT extends PLMBaseTestCase {

	private static final String CSV_MIMETYPE = "text/csv";

	private static final String XLSX_MIMETYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	@Autowired
	private UserImporterService userImporterService;

	@Autowired
	private BeCPGUserAccountService beCPGUserAccountService;

	private static final Log logger = LogFactory.getLog(UserImportServiceIT.class);

	private final Set<String> usersToCleanup = new LinkedHashSet<>();

	private final Set<NodeRef> homeFoldersToCleanup = new LinkedHashSet<>();

	@After
	public void cleanupUsersAndHomeFolders() {
		inWriteTx(() -> {
			Set<NodeRef> foldersToDelete = new LinkedHashSet<>(homeFoldersToCleanup);
			for (String username : usersToCleanup) {
				if (personService.personExists(username)) {
					NodeRef personNodeRef = personService.getPerson(username);
					NodeRef homeFolder = (NodeRef) nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
					if (homeFolder != null) {
						foldersToDelete.add(homeFolder);
					}
					personService.deletePerson(personNodeRef);
				}
			}
			for (NodeRef homeFolder : foldersToDelete) {
				if (homeFolder != null && nodeService.exists(homeFolder)) {
					nodeService.deleteNode(homeFolder);
				}
			}
			usersToCleanup.clear();
			homeFoldersToCleanup.clear();
			return null;
		});
	}

	@Test
	public void testImportUserXLSXShouldCreateUserWithDefaultImport() {
		String username = buildUsername("xlsx-default");
		trackUserForCleanup(username);
		NodeRef xlsx = inWriteTx(() -> createXlsxImport(buildFileName("xlsx-default", ".xlsx"),
				new String[] { "username", "password", "cm:firstName", "cm:lastName", "cm:email" },
				new String[] { username, "Password123", "Matthieu", "Dupont", "matthieu.dupont@test.com" }));

		inWriteTx(() -> {
			userImporterService.importUser(xlsx);
			return null;
		});

		assertUserProperties(username, "Matthieu", "Dupont", "matthieu.dupont@test.com", false);
	}

	@Test
	public void testImportUserCSVShouldCreateUserWithDefaultImport() {
		String username = buildUsername("csv-default");
		trackUserForCleanup(username);
		String csvContent = createCsvContent(
				new String[] { "username", "password", "cm:firstName", "cm:lastName", "cm:email" },
				new String[] { username, "Password123", "Matthieu", "Dupont", "matthieu.dupont@test.com" });
		NodeRef csv = inWriteTx(() -> createCsvImport(buildFileName("csv-default", ".csv"), csvContent));

		inWriteTx(() -> {
			userImporterService.importUser(csv);
			return null;
		});

		assertUserProperties(username, "Matthieu", "Dupont", "matthieu.dupont@test.com", false);
	}

	@Test
	public void testImportUserShouldKeepExistingValues() {
		assertMissingColumnDoesNotEraseValue("cm:firstName", false);
		assertMissingColumnDoesNotEraseValue("cm:lastName", false);
		assertMissingColumnDoesNotEraseValue("cm:email", false);
	}
	
	
	@Test
	public void testImportUserShouldKeepExistingValuesWithIDS() {
		assertMissingColumnDoesNotEraseValue("cm:firstName", true);
		assertMissingColumnDoesNotEraseValue("cm:lastName", true);
		assertMissingColumnDoesNotEraseValue("cm:email", true);
	}

	@Test
	public void testImportUserCSVShouldCreateIdsUserWhenIdentityServiceIsEnabled() {
		IdentityServiceAccountProvider originalProvider = (IdentityServiceAccountProvider) ReflectionTestUtils
				.getField(beCPGUserAccountService, "identityServiceAccountProvider");
		IdentityServiceAccountProvider mockProvider = createIdentityServiceAccountProviderMock();
		ReflectionTestUtils.setField(beCPGUserAccountService, "identityServiceAccountProvider", mockProvider);

		try {
			String username = buildUsername("ids-default");
			trackUserForCleanup(username);
			String csvContent = createCsvContent(
					new String[] { "username", "cm:firstName", "cm:lastName", "cm:email", "is_ids_user" },
					new String[] { username, "Ids", "User", "ids.user@test.com", "true" });
			NodeRef csv = inWriteTx(() -> createCsvImport(buildFileName("ids-default", ".csv"), csvContent));

			inWriteTx(() -> {
				userImporterService.importUser(csv);
				return null;
			});

			ArgumentCaptor<BeCPGUserAccount> userAccountCaptor = ArgumentCaptor.forClass(BeCPGUserAccount.class);
			Mockito.verify(mockProvider).registerAccount(userAccountCaptor.capture());
			BeCPGUserAccount userAccount = userAccountCaptor.getValue();

			assertEquals(username, userAccount.getUserName());
			assertEquals("Ids", userAccount.getFirstName());
			assertEquals("User", userAccount.getLastName());
			assertEquals("ids.user@test.com", userAccount.getEmail());
			assertUserProperties(username, "Ids", "User", "ids.user@test.com", true);
		} finally {
			ReflectionTestUtils.setField(beCPGUserAccountService, "identityServiceAccountProvider", originalProvider);
		}
	}

	@Test
	public void testImportUserShouldRenameUserNameToLowerCase() {
		String originalUsername = QName.createValidLocalName(name.getMethodName() + "-User");
		String lowerCaseUsername = originalUsername.toLowerCase();
		trackUserForCleanup(lowerCaseUsername);
		createExistingUser(originalUsername, "ExistingFirst", "ExistingLast", "existing@test.com");

		String csvContent = createCsvContent(
				new String[] { "username", "new_username", "cm:firstName", "cm:lastName", "cm:email" },
				new String[] { originalUsername, lowerCaseUsername, "ExistingFirst", "ExistingLast", "existing@test.com" });
		NodeRef csv = inWriteTx(() -> createCsvImport(buildFileName("rename-to-lowercase", ".csv"), csvContent));

		inWriteTx(() -> {
			userImporterService.importUser(csv);
			return null;
		});

		inReadTx(() -> {
			assertTrue(personService.personExists(lowerCaseUsername));
			NodeRef personNodeRef = personService.getPerson(lowerCaseUsername);
			assertEquals(lowerCaseUsername, nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME));
			assertEquals(lowerCaseUsername, nodeService.getProperty(personNodeRef, ContentModel.PROP_OWNER));
			assertUserProperties(lowerCaseUsername, "ExistingFirst", "ExistingLast", "existing@test.com", false);
			return null;
		});
	}

	@Test
	public void testImportUserShouldRenameHomeFolderWhenTargetDeletedUserFolderAlreadyExists() {
		String user1 = "user-" + System.currentTimeMillis() + "-1";
		String user2 = "user-" + System.currentTimeMillis() + "-2";
		trackUserForCleanup(user1);

		NodeRef user1NodeRef = createExistingUser(user1, "User", "One", "user1@test.com");
		NodeRef user2NodeRef = createExistingUser(user2, "User", "Two", "user2@test.com");

		NodeRef user1HomeFolder = inReadTx(() -> (NodeRef) nodeService.getProperty(user1NodeRef, ContentModel.PROP_HOMEFOLDER));
		NodeRef user2HomeFolder = inReadTx(() -> (NodeRef) nodeService.getProperty(user2NodeRef, ContentModel.PROP_HOMEFOLDER));

		inWriteTx(() -> {
			personService.deletePerson(user1NodeRef);
			return null;
		});

		String csvContent = createCsvContent(
				new String[] { "username", "new_username", "cm:firstName", "cm:lastName", "cm:email" },
				new String[] { user2, user1, "User", "Two", "user2@test.com" });
		NodeRef csv = inWriteTx(() -> createCsvImport(buildFileName("rename-home-folder-conflict", ".csv"), csvContent));

		inWriteTx(() -> {
			userImporterService.importUser(csv);
			return null;
		});

		inReadTx(() -> {
			assertTrue(personService.personExists(user1));
			NodeRef renamedUserNodeRef = personService.getPerson(user1);
			assertEquals(user2NodeRef, renamedUserNodeRef);
			assertFalse(personService.personExists(user2));

			assertEquals(user1, nodeService.getProperty(user2HomeFolder, ContentModel.PROP_NAME));
			assertEquals(user1, nodeService.getProperty(user2HomeFolder, ContentModel.PROP_OWNER));
			assertEquals(user1 + " (1)", nodeService.getProperty(user1HomeFolder, ContentModel.PROP_NAME));
			return null;
		});
	}

	private void assertMissingColumnDoesNotEraseValue(String missingColumn, boolean idsEnabled) {
		String userSuffix = missingColumn.replace(':', '-');
		String username = buildUsername(userSuffix + (idsEnabled ? "-ids" : "-std"));
		trackUserForCleanup(username);
		createExistingUser(username, "ExistingFirst", "ExistingLast", "existing@test.com");

		IdentityServiceAccountProvider originalProvider = null;
		IdentityServiceAccountProvider mockProvider = null;
		if (idsEnabled) {
			originalProvider = (IdentityServiceAccountProvider) ReflectionTestUtils.getField(beCPGUserAccountService,
					"identityServiceAccountProvider");
			mockProvider = createIdentityServiceAccountProviderMock();
			ReflectionTestUtils.setField(beCPGUserAccountService, "identityServiceAccountProvider", mockProvider);
		}

		try {
			List<String> headers = new ArrayList<>();
			List<String> values = new ArrayList<>();

			headers.add("username");
			values.add(username);
			if (idsEnabled) {
				headers.add("is_ids_user");
				values.add("true");
			}
			if (!"cm:firstName".equals(missingColumn)) {
				headers.add("cm:firstName");
				values.add("UpdatedFirst");
			}
			if (!"cm:lastName".equals(missingColumn)) {
				headers.add("cm:lastName");
				values.add("UpdatedLast");
			}
			if (!"cm:email".equals(missingColumn)) {
				headers.add("cm:email");
				values.add("updated@test.com");
			}

			String csvContent = createCsvContent(headers.toArray(new String[0]), values.toArray(new String[0]));
			NodeRef csv = inWriteTx(() -> createCsvImport(buildFileName(userSuffix, ".csv"), csvContent));

			inWriteTx(() -> {
				userImporterService.importUser(csv);
				return null;
			});

			String expectedFirstName = "cm:firstName".equals(missingColumn) ? "ExistingFirst" : "UpdatedFirst";
			String expectedLastName = "cm:lastName".equals(missingColumn) ? "ExistingLast" : "UpdatedLast";
			String expectedEmail = "cm:email".equals(missingColumn) ? "existing@test.com" : "updated@test.com";
			assertUserProperties(username, expectedFirstName, expectedLastName, expectedEmail, idsEnabled);

			if (mockProvider != null) {
				Mockito.verify(mockProvider).registerAccount(Mockito.any(BeCPGUserAccount.class));
			}
		} finally {
			if (originalProvider != null) {
				ReflectionTestUtils.setField(beCPGUserAccountService, "identityServiceAccountProvider", originalProvider);
			}
		}
	}

	private NodeRef createExistingUser(String username, String firstName, String lastName, String email) {
		return inWriteTx(() -> {
			trackUserForCleanup(username);
			NodeRef personNodeRef = BeCPGPLMTestHelper.createUser(username);
			nodeService.setProperty(personNodeRef, ContentModel.PROP_FIRSTNAME, firstName);
			nodeService.setProperty(personNodeRef, ContentModel.PROP_LASTNAME, lastName);
			nodeService.setProperty(personNodeRef, ContentModel.PROP_EMAIL, email);
			trackHomeFolderForCleanup((NodeRef) nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER));
			return personNodeRef;
		});
	}

	private void trackUserForCleanup(String username) {
		usersToCleanup.add(username);
	}

	private void trackHomeFolderForCleanup(NodeRef homeFolder) {
		if (homeFolder != null) {
			homeFoldersToCleanup.add(homeFolder);
		}
	}

	private void assertUserProperties(String username, String firstName, String lastName, String email, boolean idsEnabled) {
		inReadTx(() -> {
			assertTrue(personService.personExists(username));
			NodeRef personNodeRef = personService.getPerson(username);
			assertEquals(firstName, nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME));
			assertEquals(lastName, nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME));
			assertEquals(email, nodeService.getProperty(personNodeRef, ContentModel.PROP_EMAIL));
			assertEquals(Boolean.valueOf(idsEnabled), Boolean.valueOf(Boolean.TRUE.equals(nodeService.getProperty(personNodeRef, BeCPGModel.PROP_IS_SSO_USER))));
			return null;
		});
	}

	private IdentityServiceAccountProvider createIdentityServiceAccountProviderMock() {
		IdentityServiceAccountProvider mockProvider = Mockito.mock(IdentityServiceAccountProvider.class);
		Mockito.when(mockProvider.isEnabled()).thenReturn(Boolean.TRUE);
		Mockito.when(mockProvider.registerAccount(Mockito.any(BeCPGUserAccount.class))).thenReturn(true);
		Mockito.when(mockProvider.getZoneId()).thenReturn("AUTH.EXT.TEST");
		return mockProvider;
	}

	private NodeRef createCsvImport(String fileName, String csvContent) throws IOException {
		return createImportFile(fileName, CSV_MIMETYPE,
				new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));
	}

	private NodeRef createXlsxImport(String fileName, String[] headers, String[] rowValues) throws IOException {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("Users");
			Row headerRow = sheet.createRow(0);
			for (int i = 0; i < headers.length; i++) {
				headerRow.createCell(i).setCellValue(headers[i]);
			}

			Row row = sheet.createRow(1);
			for (int i = 0; i < rowValues.length; i++) {
				row.createCell(i).setCellValue(rowValues[i]);
			}

			workbook.write(outputStream);
			return createImportFile(fileName, XLSX_MIMETYPE, new ByteArrayInputStream(outputStream.toByteArray()));
		}
	}

	private NodeRef createImportFile(String fileName, String mimetype, InputStream inputStream) throws IOException {
		ChildAssociationRef assocRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(fileName)), ContentModel.TYPE_CONTENT);
		NodeRef nodeRef = assocRef.getChildRef();
		nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, fileName);
		nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, fileName);
		nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, fileName);

		ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		if (writer == null) {
			throw new IOException("Cannot write import file: " + fileName);
		}

		try (InputStream stream = inputStream) {
			writer.setMimetype(mimetype);
			writer.putContent(stream);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Import file successfully created: " + fileName);
		}
		return nodeRef;
	}

	private String createCsvContent(String[] headers, String[] rowValues) {
		return String.join(";", headers) + "\n" + String.join(";", rowValues);
	}

	private String buildUsername(String suffix) {
		return QName.createValidLocalName((name.getMethodName() + "-" + suffix).toLowerCase());
	}

	private String buildFileName(String suffix, String extension) {
		return QName.createValidLocalName(name.getMethodName() + "-" + suffix) + extension;
	}

}
