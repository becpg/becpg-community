/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.alfresco.query.ListBackedPagingResults;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class RemoteEntityServiceTest.
 *
 * @author matthieu
 */
public class RemoteEntityServiceIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(RemoteEntityServiceIT.class);

	@Autowired
	private RemoteEntityService remoteEntityService;

	@Autowired
	private NamespaceService namespaceService;

	@Test
	public void testRemoteEntity() throws FileNotFoundException {

		// create product
		final NodeRef sfNodeRef = inWriteTx(() -> BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef()));

		inWriteTx(() -> {
			try {

				File tempFile = File.createTempFile("remoteEntity", "xml");
				File tempFile2 = File.createTempFile("remoteEntity2", "xml");

				List<NodeRef> entities = new ArrayList<>();
				entities.add(sfNodeRef);

				remoteEntityService.listEntities(new ListBackedPagingResults<>(entities),
						new FileOutputStream(tempFile2), new RemoteParams(RemoteEntityFormat.xml));

				remoteEntityService.getEntity(sfNodeRef, new FileOutputStream(tempFile),
						new RemoteParams(RemoteEntityFormat.xml));

				nodeService.deleteNode(sfNodeRef);

				NodeRef tmpNodeRef = remoteEntityService.createOrUpdateEntity(sfNodeRef, new FileInputStream(tempFile),
						new RemoteParams(RemoteEntityFormat.xml), null);

				remoteEntityService.getEntity(tmpNodeRef, new FileOutputStream(tempFile2),
						new RemoteParams(RemoteEntityFormat.xml));

				remoteEntityService.getEntityData(tmpNodeRef, new FileOutputStream(tempFile2),
						new RemoteParams(RemoteEntityFormat.xml));

				remoteEntityService.getEntityData(tmpNodeRef, new FileOutputStream(tempFile),
						new RemoteParams(RemoteEntityFormat.xml));

				remoteEntityService.addOrUpdateEntityData(tmpNodeRef, new FileInputStream(tempFile),
						new RemoteParams(RemoteEntityFormat.xml));

				tempFile.delete();

			} catch (BeCPGException e) {
				logger.error(e, e);
				Assert.fail(e.getMessage());
			}

			return null;
		});
	}

	@Test
	public void testRemoteJSONEntity() throws FileNotFoundException {

		// create product
		final NodeRef sfNodeRef = inWriteTx(() -> BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef()));

		inWriteTx(() -> {
			try {

				File tempFile = File.createTempFile("remoteEntity", "json");
				File tempFile2 = File.createTempFile("remoteEntitySchema", "json");

				remoteEntityService.getEntity(sfNodeRef, new FileOutputStream(tempFile),
						new RemoteParams(RemoteEntityFormat.json));
				remoteEntityService.getEntity(sfNodeRef, new FileOutputStream(tempFile2),
						new RemoteParams(RemoteEntityFormat.json_schema));

				ObjectMapper mapper = new ObjectMapper();

				JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
				JsonSchema jsonSchema = factory.getSchema(new FileInputStream(tempFile2));
				JsonNode jsonNode = mapper.readTree(tempFile);
				Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);

				assertTrue(errors.isEmpty());

				tempFile.delete();
				tempFile2.delete();

			} catch (Exception e) {
				logger.error(e, e);
				Assert.fail(e.getMessage());
			}

			return null;
		});
	}

	@Test
	public void testRemoteFullXmlEntity() throws FileNotFoundException {

		inWriteTx(() -> {
			try {
				ruleService.disableRules();
				ClassPathResource res = new ClassPathResource("beCPG/remote/entity_fullxml.xml");

				NodeRef tmpNodeRef = remoteEntityService.createOrUpdateEntity(null, res.getInputStream(),
						new RemoteParams(RemoteEntityFormat.xml), null);
				Assert.assertNotNull(tmpNodeRef);

			} catch (BeCPGException e) {
				logger.error(e, e);
				Assert.fail(e.getMessage());
			} finally {
				ruleService.enableRules();
			}

			return null;
		});
	}

	/**
	 * Test Remote filters
	 */
	@Test
	public void testRemoteEntityFilteredListsAndFields() {
		inWriteTx(() -> {
			// Get Filtered Entity
			List<String> filteredLists = Arrays.asList(new String[] { "compoList" });
			List<String> filteredFields = Arrays
					.asList(new String[] { "bcpg:legalName", "bcpg:compoListProduct|cm:name" });
			NodeRef nodeRef = createFinishedProduct();
			OutputStream out = new ByteArrayOutputStream();
			RemoteParams params = new RemoteParams(RemoteEntityFormat.xml);
			params.setFilteredLists(filteredLists);
			params.setFilteredFields(filteredFields, namespaceService);

			remoteEntityService.getEntity(nodeRef, out, params);
			ByteArrayOutputStream buffer = (ByteArrayOutputStream) out;
			String xmlString = new String(buffer.toByteArray());

			try {
				// Parse XML
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
				domFactory.setNamespaceAware(true);
				DocumentBuilder builder = domFactory.newDocumentBuilder();
				Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
				Element entity = (Element) doc.getFirstChild();
				// erpCode Property
				assertTrue(entity.hasAttribute("erpCode"));
				assertEquals("Extract Property erpCode", "erp0001", entity.getAttribute("erpCode"));

				// Properties/Assoc filter
				NodeList properties = entity.getChildNodes();
				int checks = 0;
				for (int j = 0; j < properties.getLength(); j++) {
					if (properties.item(j) instanceof Element) {
						Element property = ((Element) properties.item(j));
						String propName = property.getNodeName();
						if ("bcpg:legalName".equals(propName)) {
							checks++;
						}
						assertFalse("Entity property filter : " + propName, propName.equals("bcpg:netVolume"));
						assertFalse("Entity property filter : " + propName, propName.equals("bcpg:productUnit"));
						assertFalse("Entity property filter : " + propName, propName.equals("bcpg:erpCode"));
					}
				}

				assertEquals("Entity property filter : ", 1, checks);

				// Lists filter
				NodeList dataLists = entity.getElementsByTagName("dl:dataList");
				for (int i = 0; i < dataLists.getLength(); i++) {
					Element dataList = ((Element) dataLists.item(i));
					assertEquals("Entity list filter ", "compoList", dataList.getAttribute("name"));
				}

				// Extract assoc properties
				checks = 0;
				NodeList compoProductList = entity.getElementsByTagName("bcpg:compoListProduct");
				for (int i = 0; i < compoProductList.getLength(); i++) {
					Element compoProduct = ((Element) compoProductList.item(i));
					Element assoc = (Element) compoProduct.getChildNodes().item(0);
					NodeList assocProperties = assoc.getChildNodes();

					for (int j = 0; j < properties.getLength(); j++) {
						if (assocProperties.item(j) instanceof Element) {
							Element assocProp = ((Element) assocProperties.item(j));
							assertEquals("Entity assoc properties extraction : ", "cm:name", assocProp.getNodeName());
							assertEquals("Entity assoc properties extraction : ", "MP test filtered entity",
									assocProp.getTextContent());
							checks++;
						}
					}
				}
				assertEquals("Entity assoc properties extraction", 1, checks);

			} catch (Exception e) {
				logger.error(e);
			}

			return null;
		});

	}

	private NodeRef createFinishedProduct() {
		// Create finished composite product with ActivityList
		return inWriteTx(() -> {
			// Create product
			NodeRef rawMaterialNodeRef = BeCPGPLMTestHelper.createRawMaterial(getTestFolderNodeRef(),
					"MP test filtered entity");
			FinishedProductData productData = new FinishedProductData();
			productData.setParentNodeRef(getTestFolderNodeRef());
			productData.setName("finished-product");
			productData.setLegalName("finished-product legalName");
			productData.setNetVolume(1d);
			productData.setErpCode("erp0001");
			productData.setUnit(ProductUnit.kg);
			List<CompoListDataItem> compoList = new LinkedList<>();
			/*
			 * CompoListDataItem compoListItem = new CompoListDataItem(null, null, 1d, 1d,
			 * ProductUnit.P, 0d, DeclarationType.Declare, rawMaterialNodeRef);
			 */
			CompoListDataItem compoListItem = CompoListDataItem.build().withQty(1d).withQtyUsed(1d)
					.withUnit(ProductUnit.P).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterialNodeRef);
			compoList.add(compoListItem);
			productData.getCompoListView().setCompoList(compoList);
			List<PackagingListDataItem> packList = new ArrayList<>();
			productData.getPackagingListView().setPackagingList(packList);
			alfrescoRepository.save(productData);
			return productData.getNodeRef();

		});
	}

}
