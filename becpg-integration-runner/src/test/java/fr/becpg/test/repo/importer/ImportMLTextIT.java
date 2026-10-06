/*
 *
 */
package fr.becpg.test.repo.importer;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.importer.ImportService;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Checks the import of the translations of a multilingual property, one column per locale (#36417).
 *
 * @author matthieu
 */
public class ImportMLTextIT extends PLMBaseTestCase {

	private static final String COLUMN_SEPARATOR = ";";

	private static final String LINE_SEPARATOR = "\n";

	private static final String GERMAN_NAME = "Aprikose";

	private static final String NEW_GERMAN_NAME = "Aprikose neu";

	private static final String US_NAME = "Apricot US";

	private static final String NEW_US_NAME = "Apricot US 2";

	private static final String CAS_NUMBER = "7732-18-5";

	@Autowired
	private ImportService importService;

	@Autowired
	@Qualifier("mlAwareNodeService")
	private NodeService mlAwareNodeService;

	private String ingName;

	private String regulatoryCode;

	private NodeRef ingNodeRef;

	/**
	 * Every translation column carrying the MLText annotation is read on its own: the last one used to overwrite the
	 * translations read by the previous ones, so the file added the German name but not the US one.
	 */
	@Test
	public void testImportSeveralTranslationColumns() throws Exception {

		createIngredient();

		importFile("import-36417-translations.csv", translationsOnlyFile());

		inReadTx(() -> {
			MLText charactName = readCharactName();
			Assert.assertEquals("The US translation is added", US_NAME, charactName.getValue(Locale.US));
			Assert.assertEquals("The German translation is updated", NEW_GERMAN_NAME, charactName.getValue(Locale.GERMAN));
			Assert.assertEquals("The default name is kept", ingName, charactName.getValue(I18NUtil.getContentLocaleLang()));
			return null;
		});
	}

	/**
	 * A translation column following the multilingual property used as the key was rejected as an unknown column.
	 */
	@Test
	public void testImportTranslationColumnAfterKey() throws Exception {

		createIngredient();

		importFile("import-36417-key.csv", translationAfterKeyFile());

		inReadTx(() -> {
			MLText charactName = readCharactName();
			Assert.assertEquals("The US translation is added after the key column", NEW_US_NAME, charactName.getValue(Locale.US));
			Assert.assertEquals("The German translation is kept", GERMAN_NAME, charactName.getValue(Locale.GERMAN));
			Assert.assertEquals("The default name is kept", ingName, charactName.getValue(I18NUtil.getContentLocaleLang()));
			return null;
		});
	}

	/**
	 * A column following the last translation of a multilingual property was read as its default value, so the CAS
	 * number of the file replaced the name of the ingredient.
	 */
	@Test
	public void testImportColumnAfterTranslations() throws Exception {

		createIngredient();

		importFile("import-36417-after-translations.csv", columnAfterTranslationsFile());

		inReadTx(() -> {
			MLText charactName = readCharactName();
			Assert.assertEquals("The default name is kept", ingName, charactName.getValue(I18NUtil.getContentLocaleLang()));
			Assert.assertEquals("The US translation is added", US_NAME, charactName.getValue(Locale.US));
			Assert.assertEquals("The German translation is updated", NEW_GERMAN_NAME, charactName.getValue(Locale.GERMAN));
			Assert.assertEquals("The following column is imported on its own property", CAS_NUMBER,
					mlAwareNodeService.getProperty(ingNodeRef, PLMModel.PROP_CAS_NUMBER));
			return null;
		});
	}

	/**
	 * A translation column in the content locale was replaced by the empty default value, so the import did not
	 * update the default name.
	 */
	@Test
	public void testImportContentLocaleTranslationColumn() throws Exception {

		createIngredient();

		String newName = ingName + " new";
		importFile("import-36417-content-locale.csv", contentLocaleTranslationFile(newName));

		inReadTx(() -> {
			MLText charactName = readCharactName();
			Assert.assertEquals("The default name is updated", newName, charactName.getValue(I18NUtil.getContentLocaleLang()));
			Assert.assertEquals("The German translation is kept", GERMAN_NAME, charactName.getValue(Locale.GERMAN));
			return null;
		});
	}

	private void createIngredient() {
		long timestamp = Calendar.getInstance().getTimeInMillis();
		ingName = "Abricot 36417 " + timestamp;
		regulatoryCode = "36417-" + timestamp;

		ingNodeRef = inWriteTx(() -> {
			MLText charactName = new MLText();
			charactName.addValue(I18NUtil.getContentLocaleLang(), ingName);
			charactName.addValue(Locale.GERMAN, GERMAN_NAME);

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, charactName);
			properties.put(PLMModel.PROP_REGULATORY_CODE, regulatoryCode);

			return mlAwareNodeService.createNode(getIngsFolder(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, ingName), PLMModel.TYPE_ING, properties).getChildRef();
		});

		waitForSolr();
	}

	private NodeRef getIngsFolder() {
		NodeRef systemFolder = repoService.getFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM);
		return entitySystemService.getSystemEntityDataList(systemFolder, RepoConsts.PATH_CHARACTS, PlmRepoConsts.PATH_INGS);
	}

	private MLText readCharactName() {
		return (MLText) mlAwareNodeService.getProperty(ingNodeRef, BeCPGModel.PROP_CHARACT_NAME);
	}

	private String translationsOnlyFile() {
		return String.join(LINE_SEPARATOR, header(),
				String.join(COLUMN_SEPARATOR, "COLUMNS_PARAMS", "@Key", "@MLText", "@MLText"),
				String.join(COLUMN_SEPARATOR, "COLUMNS", "bcpg:regulatoryCode", "bcpg:charactName_en_US", "bcpg:charactName_de"),
				String.join(COLUMN_SEPARATOR, "VALUES", regulatoryCode, US_NAME, NEW_GERMAN_NAME), "");
	}

	private String translationAfterKeyFile() {
		return String.join(LINE_SEPARATOR, header(), String.join(COLUMN_SEPARATOR, "COLUMNS_PARAMS", "@Key"),
				String.join(COLUMN_SEPARATOR, "COLUMNS", "bcpg:charactName", "bcpg:charactName_en_US"),
				String.join(COLUMN_SEPARATOR, "VALUES", ingName, NEW_US_NAME), "");
	}

	private String columnAfterTranslationsFile() {
		return String.join(LINE_SEPARATOR, header(),
				String.join(COLUMN_SEPARATOR, "COLUMNS_PARAMS", "@Key", "@MLText", "@MLText"),
				String.join(COLUMN_SEPARATOR, "COLUMNS", "bcpg:regulatoryCode", "bcpg:charactName_en_US", "bcpg:charactName_de", "bcpg:casNumber"),
				String.join(COLUMN_SEPARATOR, "VALUES", regulatoryCode, US_NAME, NEW_GERMAN_NAME, CAS_NUMBER), "");
	}

	private String contentLocaleTranslationFile(String newName) {
		return String.join(LINE_SEPARATOR, header(), String.join(COLUMN_SEPARATOR, "COLUMNS_PARAMS", "@Key", "@MLText"),
				String.join(COLUMN_SEPARATOR, "COLUMNS", "bcpg:regulatoryCode", "bcpg:charactName_" + I18NUtil.getContentLocaleLang().getLanguage()),
				String.join(COLUMN_SEPARATOR, "VALUES", regulatoryCode, newName), "");
	}

	private String header() {
		return String.join(LINE_SEPARATOR, String.join(COLUMN_SEPARATOR, "TYPE", "bcpg:ing"),
				String.join(COLUMN_SEPARATOR, "PATH", "/System/Characts/bcpg:entityLists/Ings"),
				String.join(COLUMN_SEPARATOR, "DELETE_DATALIST", "false"), String.join(COLUMN_SEPARATOR, "STOP_ON_FIRST_ERROR", "true"));
	}

	private void importFile(String fileName, String content) throws InterruptedException {
		BatchInfo batchInfo = inWriteTx(() -> {
			NodeRef fileNodeRef = createImportFile(fileName, content);
			return importService.importText(fileNodeRef, true, false, null);
		});

		waitForBatchEnd(batchInfo);
	}

	private NodeRef createImportFile(String fileName, String content) {
		NodeRef previousFile = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, fileName);
		if (previousFile != null) {
			nodeService.deleteNode(previousFile);
		}

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, fileName);

		NodeRef fileNodeRef = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName), ContentModel.TYPE_CONTENT, properties).getChildRef();

		ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
		writer.setMimetype("text/csv");
		writer.setEncoding("UTF-8");
		writer.putContent(content);

		return fileNodeRef;
	}

}
