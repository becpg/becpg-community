/*
 *
 */
package fr.becpg.test.repo.action.executer;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.test.PLMBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ImporterActionExecuterTest.
 *
 * @author querephi
 */
public class ImporterActionExecuterIT extends PLMBaseTestCase {

	private static final String FILENAME_IMPORT_CSV = "import.csv";

	private static final Log logger = LogFactory.getLog(ImporterActionExecuterIT.class);

	@Autowired
	private Repository repository;

	/**
	 * Test add content to import.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testAddContentToImport() throws Exception {

		inWriteTx(() -> {

			NodeRef exchangeFolder = nodeService.getChildByName(repository.getCompanyHome(),
					ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_EXCHANGE));
			if (exchangeFolder == null) {
				throw new Exception("Missing exchange folder.");
			}
			NodeRef importFolder = nodeService.getChildByName(exchangeFolder, ContentModel.ASSOC_CONTAINS,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_IMPORT));
			if (importFolder == null) {
				throw new Exception("Missing import folder.");
			}
			NodeRef importToTreatFolder = nodeService.getChildByName(importFolder, ContentModel.ASSOC_CONTAINS,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_IMPORT_TO_TREAT));
			if (importToTreatFolder == null) {
				throw new Exception("Missing import folder.");
			}

			// Create file to import
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, FILENAME_IMPORT_CSV);

			NodeRef importNodeRef = nodeService.getChildByName(importToTreatFolder, ContentModel.ASSOC_CONTAINS,
					(String) properties.get(ContentModel.PROP_NAME));
			if (importNodeRef != null) {
				nodeService.deleteNode(importNodeRef);
			}
			NodeRef contentNodeRef = nodeService.createNode(importToTreatFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
							(String) properties.get(ContentModel.PROP_NAME)),
					ContentModel.TYPE_CONTENT, properties).getChildRef();

			ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
			logger.debug("import.csv");
			InputStream in = (new ClassPathResource("beCPG/import/Import.csv")).getInputStream();

			String mimetype = mimetypeService.guessMimetype(FILENAME_IMPORT_CSV);
			ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
			Charset charset = charsetFinder.getCharset(in, mimetype);
			String encoding = charset.name();
			writer.setMimetype(mimetype);
			writer.setEncoding(encoding);
			writer.putContent(in);

			return null;

		});

	}

	/**
	 * Check that import goes in the failed folder even if there is
	 * IntegrityException
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddContentToImportThatFailed() throws Exception {

		inWriteTx(() -> {

			NodeRef exchangeFolder = nodeService.getChildByName(repository.getCompanyHome(),
					ContentModel.ASSOC_CONTAINS, TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_EXCHANGE));
			if (exchangeFolder == null) {
				throw new Exception("Missing exchange folder.");
			}
			NodeRef importFolder = nodeService.getChildByName(exchangeFolder, ContentModel.ASSOC_CONTAINS,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_IMPORT));
			if (importFolder == null) {
				throw new Exception("Missing import folder.");
			}
			NodeRef importToTreatFolder = nodeService.getChildByName(importFolder, ContentModel.ASSOC_CONTAINS,
					TranslateHelper.getTranslatedPath(PlmRepoConsts.PATH_IMPORT_TO_TREAT));
			if (importToTreatFolder == null) {
				throw new Exception("Missing import folder.");
			}

			// Create file to import
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "import.csv");

			NodeRef importNodeRef = nodeService.getChildByName(importToTreatFolder, ContentModel.ASSOC_CONTAINS,
					(String) properties.get(ContentModel.PROP_NAME));
			if (importNodeRef != null) {
				nodeService.deleteNode(importNodeRef);
			}
			NodeRef contentNodeRef = nodeService.createNode(importToTreatFolder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
							(String) properties.get(ContentModel.PROP_NAME)),
					ContentModel.TYPE_CONTENT, properties).getChildRef();

			ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
			logger.debug("Import-with-IntegrityException.csv");
			InputStream in = (new ClassPathResource("beCPG/import/Import-with-IntegrityException.csv"))
					.getInputStream();

			String mimetype = mimetypeService.guessMimetype(FILENAME_IMPORT_CSV);
			ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
			Charset charset = charsetFinder.getCharset(in, mimetype);
			String encoding = charset.name();
			writer.setMimetype(mimetype);
			writer.setEncoding(encoding);
			writer.putContent(in);

			return null;

		});

	}

}
