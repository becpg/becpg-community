/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.test.repo.web.scripts.report;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.I18NUtil;

import com.google.gdata.util.common.net.UriEncoder;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.test.utils.TestWebscriptExecuters;
import fr.becpg.test.utils.TestWebscriptExecuters.GetRequest;
import fr.becpg.test.utils.TestWebscriptExecuters.Response;

// TODO: Auto-generated Javadoc
/**
 * The Class ExportSearchWebScriptTest.
 *
 * @author querephi
 */
public class ExportSearchWebScriptTest extends fr.becpg.test.PLMBaseTestCase {

	/** The logger. */
	private static final Log logger = LogFactory.getLog(ExportSearchWebScriptTest.class);

	private static final String EXPORT_PRODUCTS_REPORT_RPTFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearch.rptdesign";
	private static final String EXPORT_PRODUCTS_REPORT_XMLFILE_PATH = "beCPG/birt/exportsearch/product/ExportSearchQuery.xml";

	@Resource
	private ReportTplService reportTplService;

	@Resource
	private EntityService entityService;



	
	/**
	 * Inits the tests.
	 * 
	 * @throws IOException
	 */
	private void initTests() throws IOException {

		logger.debug("look for report template");

		// reports folder
		NodeRef reportsFolder = repoService.getOrCreateFolderByPath(systemFolderNodeRef, RepoConsts.PATH_REPORTS,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS));
		assertNotNull("Check reports folder", reportsFolder);

		// export search report
		NodeRef exportSearchNodeRef = repoService.getOrCreateFolderByPath(reportsFolder, RepoConsts.PATH_REPORTS_EXPORT_SEARCH,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_REPORTS_EXPORT_SEARCH));
		NodeRef exportSearchProductsNodeRef = repoService.getOrCreateFolderByPath(exportSearchNodeRef,
				PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS,
				TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS));

		exportProductReportTpl = reportTplService.createTplRptDesign(exportSearchProductsNodeRef,
				TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_REPORTS_EXPORT_SEARCH_PRODUCTS), EXPORT_PRODUCTS_REPORT_RPTFILE_PATH,
				ReportType.ExportSearch, ReportFormat.XLSX, PLMModel.TYPE_PRODUCT, false, true, true);

		reportTplService.createTplRessource(exportSearchProductsNodeRef, EXPORT_PRODUCTS_REPORT_XMLFILE_PATH, false);
	}

	/**
	 * Adds the product image.
	 *
	 * @param parentNodeRef
	 *            the parent node ref
	 * @throws IOException
	 * @throws ContentIOException
	 */
	@Deprecated
	// Use writeImages of entityService instead
	// merge with productserviceTest or inside helper
	private void addProductImage(NodeRef parentNodeRef) throws ContentIOException, IOException {

		/*-- add product image--*/
		logger.debug("/*-- add product image--*/");
		String imageName = I18NUtil.getMessage(RepoConsts.PATH_LOGO_IMAGE) + ".jpg";
		logger.debug("image name: " + imageName);
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, imageName);
		NodeRef imageNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
				ContentModel.TYPE_CONTENT, properties).getChildRef();

		ContentWriter writer = contentService.getWriter(imageNodeRef, ContentModel.PROP_CONTENT, true);

		ClassPathResource img = new ClassPathResource("beCPG/birt/productImage.jpg");

		String mimetype = mimetypeService.guessMimetype(img.getFilename());
		ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
		Charset charset = charsetFinder.getCharset(img.getInputStream(), mimetype);
		String encoding = charset.name();

		logger.debug("mimetype : " + mimetype);
		logger.debug("encoding : " + encoding);
		writer.setMimetype(mimetype);
		writer.setEncoding(encoding);
		writer.putContent(img.getInputStream());
	}

	private NodeRef localSF1NodeRef;
	private NodeRef rawMaterial1NodeRef;
	private NodeRef rawMaterial2NodeRef;
	private NodeRef localSF2NodeRef;
	private NodeRef rawMaterial3NodeRef;
	private NodeRef rawMaterial4NodeRef;
	private NodeRef exportProductReportTpl;

	
	/**
	 * Test export search.
	 */
	@Test
	public void testExportSearch() {

		// TODO : merge CompareProductReportWebScript avec
		// CompareProductServiceTest => beaucoup de code en commun !
		// init objects

		Date startTime = new Date();

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// Create comparison product report
				initTests();
				return null;

			}
		}, false, true);
		
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {


				/*-- Create raw materials --*/
				logger.debug("/*-- Create raw materials --*/");
				/*-- Raw material 1 --*/
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				rawMaterial1.setLegalName("Legal Raw material 1");
				rawMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

				/*-- Raw material 2 --*/
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				rawMaterial2.setLegalName("Legal Raw material 2");
				rawMaterial2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();

				/*-- Raw material 3 --*/
				RawMaterialData rawMaterial3 = new RawMaterialData();
				rawMaterial3.setName("Raw material 3");
				rawMaterial3.setLegalName("Legal Raw material 3");
				rawMaterial3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial3).getNodeRef();

				/*-- Raw material 4 --*/
				RawMaterialData rawMaterial4 = new RawMaterialData();
				rawMaterial4.setName("Raw material 4");
				rawMaterial4.setLegalName("Legal Raw material 4");
				rawMaterial4NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial4).getNodeRef();

				/*-- Raw material 5 --*/
				RawMaterialData rawMaterial5 = new RawMaterialData();
				rawMaterial5.setName("Raw material 5");
				rawMaterial5.setLegalName("Legal Raw material 5");
				alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial5).getNodeRef();

				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
				localSF1.setName("Local semi finished 1");
				localSF1.setLegalName("Legal Local semi finished 1");
				localSF1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF1).getNodeRef();

				/*-- Local semi finished product 1 --*/
				LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
				localSF2.setName("Local semi finished 2");
				localSF2.setLegalName("Legal Local semi finished 2");
				localSF2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF2).getNodeRef();

				return null;

			}
		}, false, true);
		
		

		waitForSolr(startTime);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				logger.debug("createRawMaterial 1");

				FinishedProductData fp1 = new FinishedProductData();
				fp1.setName("FP 1");

				// Costs
				List<CostListDataItem> costList = new ArrayList<>();
				for (NodeRef cost1 : costs) {
					CostListDataItem costListItemData = new CostListDataItem(null, 12.2d, "€/kg", null, cost1, false);
					costList.add(costListItemData);
				}
				fp1.setCostList(costList);

				// create an MP for the allergens
				RawMaterialData allergenRawMaterial = new RawMaterialData();
				allergenRawMaterial.setName("MP allergen");
				NodeRef allergenRawMaterialNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), allergenRawMaterial).getNodeRef();

				// Allergens
				List<AllergenListDataItem> allergenList = new ArrayList<>();
				for (NodeRef allergen : allergens) {
					List<NodeRef> voluntarySources = new ArrayList<>();
					voluntarySources.add(allergenRawMaterialNodeRef);

					AllergenListDataItem allergenListItemData = new AllergenListDataItem(null, null, true, false, voluntarySources, null,
							allergen, false);
					allergenList.add(allergenListItemData);
				}
				fp1.setAllergenList(allergenList);

				List<CompoListDataItem> compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare,
						rawMaterial1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						localSF2NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(3), 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare,
						rawMaterial3NodeRef));
				fp1.getCompoListView().setCompoList(compoList);

				alfrescoRepository.create(getTestFolderNodeRef(), fp1).getNodeRef();

				logger.debug("create FP 2");

				FinishedProductData fp2 = new FinishedProductData();
				fp2.setName("FP 2");

				// Costs
				costList = new ArrayList<>();
				for (NodeRef cost : costs) {
					CostListDataItem costListItemData = new CostListDataItem(null, 12.4d, "$/kg", null, cost, false);
					costList.add(costListItemData);
				}
				fp2.setCostList(costList);

				// Allergens
				allergenList = new ArrayList<>();
				for (int j = 0; j < allergens.size(); j++) {
					List<NodeRef> allSources = new ArrayList<>();
					allSources.add(allergenRawMaterialNodeRef);
					AllergenListDataItem allergenListItemData;

					if (j < 5) {
						allergenListItemData = new AllergenListDataItem(null, null, true, false, allSources, null, allergens.get(j), false);
					} else {
						allergenListItemData = new AllergenListDataItem(null, null, false, true, null, allSources, allergens.get(j), false);
					}

					allergenList.add(allergenListItemData);
				}
				fp2.setAllergenList(allergenList);

				compoList = new ArrayList<>();
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						localSF1NodeRef));
				compoList.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Declare,
						rawMaterial1NodeRef));
				compoList
						.add(new CompoListDataItem(null, compoList.get(0), 2d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
				compoList.add(new CompoListDataItem(null, null, 1d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail,
						localSF2NodeRef));
				compoList
						.add(new CompoListDataItem(null, compoList.get(3), 2d, 0d, CompoListUnit.P, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
				compoList
						.add(new CompoListDataItem(null, compoList.get(3), 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial4NodeRef));
				fp2.getCompoListView().setCompoList(compoList);

				alfrescoRepository.create(getTestFolderNodeRef(), fp2).getNodeRef();

				/*-- Create images folder --*/
				NodeRef imagesNodeRef = fileFolderService.create(getTestFolderNodeRef(), TranslateHelper.getTranslatedPath(RepoConsts.PATH_IMAGES),
						ContentModel.TYPE_FOLDER).getNodeRef();
				addProductImage(imagesNodeRef);

				return null;

			}
		}, false, true);

		waitForSolr(startTime);

		// search on date range
		try {

			String url = "/becpg/report/exportsearch/"
					+ exportProductReportTpl.toString().replace("://", "/")
					+ "/Excel.xlsx?repo=true&term=&query={\"prop_cm_name\":\"\",\"prop_bcpg_legalName\":\"\",\"prop_bcpg_productHierarchy1\":\"\",\"prop_bcpg_productHierarchy2\":\"\",\"prop_bcpg_productState\":\"\",\"prop_bcpg_productCode\":\"\",\"prop_bcpg_eanCode\":\"\",\"assoc_bcpg_supplierAssoc\":\"\",\"assoc_bcpg_supplierAssoc_added\":\"\",\"assoc_bcpg_supplierAssoc_removed\":\"\",\"prop_cm_modified-date-range\":\"2011-04-17T00:00:00%2B02:00|2011-05-23T00:00:00%2B02:00\",\"prop_cm_modifier\":\"\",\"assoc_bcpg_ingListIng\":\"\",\"assoc_bcpg_ingListIng_added\":\"\",\"assoc_bcpg_ingListIng_removed\":\"\",\"assoc_bcpg_ingListGeoOrigin\":\"\",\"assoc_bcpg_ingListGeoOrigin_added\":\"\",\"assoc_bcpg_ingListGeoOrigin_removed\":\"\",\"assoc_bcpg_ingListBioOrigin\":\"\",\"assoc_bcpg_ingListBioOrigin_added\":\"\",\"assoc_bcpg_ingListBioOrigin_removed\":\"\",\"datatype\":\"bcpg:product\"}";

			Response response = TestWebscriptExecuters.sendRequest(new GetRequest(UriEncoder.encode(url)), 200, "admin");
			logger.debug("Response: " + response.getContentAsString());
		} catch (Exception e) {
			logger.error("Failed to execute webscript", e);
			assertNull("Should not throw an exception", e);
		}

		// search on cm:name
		try {

			String url = "/becpg/report/exportsearch/"
					+ exportProductReportTpl.toString().replace("://", "/")
					+ "/Excel.xlsx?repo=true&term=&query={\"prop_cm_name\":\"FP\",\"prop_cm_title\":\"\",\"prop_cm_description\":\"\",\"prop_mimetype\":\"\",\"prop_cm_modified-date-range\":\"\",\"prop_cm_modifier\":\"\",\"datatype\":\"cm:content\"}";

			Response response = TestWebscriptExecuters.sendRequest(new GetRequest(UriEncoder.encode(url)), 200, "admin");
			logger.debug("Response: " + response.getContentAsString());
		} catch (Exception e) {
			logger.error("Failed to execute webscript", e);
			assertNull("Should not throw an exception", e);
		}

	}

	/**
	 * Test get export search tpls.
	 */
	@Test
	public void testGetExportSearchTpls() {

		Date startTime = new Date();

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// Create product report
				initTests();

				return null;

			}
		}, false, true);

		waitForSolr(startTime);

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// List<NodeRef> reportTpls =
				// exportSearchService.getReportTpls();
				List<NodeRef> reportTpls = reportTplService.getUserReportTemplates(ReportType.ExportSearch, PLMModel.TYPE_PRODUCT, "*");

				for (NodeRef n : reportTpls) {
					logger.debug("report name: " + nodeService.getProperty(n, ContentModel.PROP_NAME));
				}

				assertEquals("There is one report", 1, reportTpls.size());
				assertEquals("Check report nodeRef", exportProductReportTpl, reportTpls.get(0));

				return null;

			}
		}, false, true);

		try {

			String url = "/becpg/report/exportsearch/templates/bcpg:product";

			Response response = TestWebscriptExecuters.sendRequest(new GetRequest(url), 200, "admin");

			logger.debug("response: " + response.getContentAsString());
		} catch (Exception e) {
			logger.error("Failed to execute webscript", e);
		}

	}

}
