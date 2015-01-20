/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportFileReader;
import fr.becpg.repo.importer.ImportService;
import fr.becpg.repo.importer.ImportType;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Import service.
 * 
 * @author querephi
 */

@Service("importService")
public class ImportServiceImpl implements ImportService {

	private static final String FULL_PATH_IMPORT_MAPPING = "/cm:System/cm:Exchange/cm:Import/cm:Mapping";
	private static final String FULL_PATH_IMPORT_FAILED_FOLDER = "/app:company_home/cm:Exchange/cm:Import/cm:ImportFailed";
	private static final String FULL_PATH_IMPORT_LOG_FOLDER = "/app:company_home/cm:Exchange/cm:Import/cm:ImportLog";
	private static final String FULL_PATH_IMPORT_SUCCEEDED_FOLDER = "/app:company_home/cm:Exchange/cm:Import/cm:ImportSucceeded";

	private static final String PFX_COMMENT = "#";

	private static final String PFX_MAPPING = "MAPPING";

	private static final String PFX_PATH = "PATH";

	private static final String PFX_TYPE = "TYPE";

	private static final String PFX_ENTITY_TYPE = "ENTITY_TYPE";

	private static final String PFX_STOP_ON_FIRST_ERROR = "STOP_ON_FIRST_ERROR";

	private static final String PFX_DELETE_DATALIST = "DELETE_DATALIST";

	private static final String PFX_COLUMS = "COLUMNS";

	private static final String PFX_VALUES = "VALUES";

	private static final String PFX_IMPORT_TYPE = "IMPORT_TYPE";

	private static final String PFX_DOCS_BASE_PATH = "DOCS_BASE_PATH";

	private static final String PFX_DISABLED_POLICIES = "DISABLED_POLICIES";

	private static final String PATH_SITES = "st:sites";

	private static final String MSG_INFO_IMPORT_BATCH = "import_service.info.import_batch";

	private static final String MSG_ERROR_UNSUPPORTED_PREFIX = "import_service.error.err_unsupported_prefix";
	private static final String MSG_ERROR_MAPPING_NOT_FOUND = "import_service.error.err_mapping_not_found";
	private static final String MSG_ERROR_READING_MAPPING = "import_service.error.err_reading_mapping";
	private static final String MSG_ERROR_UNDEFINED_LINE = "import_service.error.err_undefined_line";

	public static final char SEPARATOR = ';';

	private static final int COLUMN_PREFIX = 0;
	private static final int COLUMN_MAPPING = 1;
	private static final int COLUMN_PATH = 1;
	private static final int COLUMN_TYPE = 1;
	private static final int COLUMN_ENTITY_TYPE = 1;
	private static final int COLUMN_IMPORT_TYPE = 1;
	private static final int COLUMN_DISABLED_POLICIES = 1;

	private static final int BATCH_SIZE = 10;

	private static Log logger = LogFactory.getLog(ImportServiceImpl.class);

	@Autowired
	private NodeService nodeService;
	@Autowired
	private ContentService contentService;
	@Autowired
	@Qualifier("ServiceRegistry")
	private ServiceRegistry serviceRegistry;
	@Autowired
	private RepoService repoService;
	@Autowired
	private Repository repositoryHelper;
	@Autowired
	private DictionaryService dictionaryService;
	@Autowired
	private BehaviourFilter policyBehaviourFilter;
	@Autowired
	private MimetypeService mimetypeService;

	// TODO could be better with plugin
	@Autowired
	@Qualifier("importNodeVisitor")
	private ImportVisitor importNodeVisitor;
	@Autowired
	@Qualifier("importProductVisitor")
	private ImportVisitor importProductVisitor;
	@Autowired
	@Qualifier("importEntityListAspectVisitor")
	private ImportVisitor importEntityListAspectVisitor;
	@Autowired
	@Qualifier("importEntityListItemVisitor")
	private ImportVisitor importEntityListItemVisitor;
	@Autowired
	@Qualifier("importCommentsVisitor")
	private ImportVisitor importCommentsVisitor;

	/**
	 * Import a text file
	 * 
	 * @throws ParseException
	 * @throws IOException
	 */
	@Override
	public List<String> importText(final NodeRef nodeRef, boolean doUpdate, boolean requiresNewTransaction) throws ImporterException, IOException,
			ParseException, Exception {

		logger.debug("start import");

		// prepare context
		ImportContext importContext = serviceRegistry.getTransactionService().getRetryingTransactionHelper()
				.doInTransaction(new RetryingTransactionCallback<ImportContext>() {

					@Override
					public ImportContext execute() throws Exception {

						ImportContext importContext = new ImportContext();
						InputStream is = null;
						try {

							ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
							is = reader.getContentInputStream();

							if (logger.isDebugEnabled()) {
								logger.debug("Reading Import File");
							}
							Charset charset = ImportHelper.guestCharset(is, reader.getEncoding());
							String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
							String mimeType = mimetypeService.guessMimetype(fileName);

							if (logger.isDebugEnabled()) {
								logger.debug("reader.getEncoding() : " + reader.getEncoding());
								logger.debug("finder.getEncoding() : " + charset);
								logger.debug("MimeType :" + mimeType);
							}

							importContext.setImportFileName(fileName);

							ImportFileReader imporFileReader;
							if (MimetypeMap.MIMETYPE_EXCEL.equals(mimeType) || MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET.equals(mimeType)) {
								imporFileReader = new ImportExcelFileReader(is, importContext.getPropertyFormats());
							} else {
								imporFileReader = new ImportCSVFileReader(is, charset, SEPARATOR);
							}

							importContext.setImportFileReader(imporFileReader);

						} finally {
							IOUtils.closeQuietly(is);
						}
						return importContext;
					}
				}, true, requiresNewTransaction);

		importContext.setDoUpdate(doUpdate);

		logger.debug("Start batch import of size :" + BATCH_SIZE);

		int lastIndex = importContext.getImportFileReader().getTotalLineCount();
		int nbBatches = (lastIndex / BATCH_SIZE) + 1;

		for (int z_idx = 0; z_idx < nbBatches; z_idx++) {

			importContext.setImportIndex(z_idx * BATCH_SIZE);
			int tempIndex = (z_idx + 1) * BATCH_SIZE;
			final int finalLastIndex = tempIndex > lastIndex ? lastIndex : tempIndex;
			final ImportContext finalImportContext = importContext;

			// add info message in log and file import
			if (logger.isInfoEnabled()) {

				logger.info(I18NUtil.getMessage(MSG_INFO_IMPORT_BATCH, (z_idx + 1), nbBatches, importContext.getImportFileName(),
						(importContext.getImportIndex() + 1), (finalLastIndex + 1)));

			}

			// use transaction
			importContext = serviceRegistry.getTransactionService().getRetryingTransactionHelper()
					.doInTransaction(new RetryingTransactionCallback<ImportContext>() {
						public ImportContext execute() throws Exception {

							return importInBatch(finalImportContext, finalLastIndex);
						}
					}, false, requiresNewTransaction);

		}

		if (importContext.getLog().size() > 0 && importContext.getImportFileReader() instanceof ImportExcelFileReader) {

			final ImportContext finalImportContext = importContext;

			importContext = serviceRegistry.getTransactionService().getRetryingTransactionHelper()
					.doInTransaction(new RetryingTransactionCallback<ImportContext>() {
						public ImportContext execute() throws Exception {
							serviceRegistry.getRuleService().disableRules();
							try {

								finalImportContext.getImportFileReader().writeErrorInFile(
										contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true));
								return finalImportContext;
							} finally {
								serviceRegistry.getRuleService().enableRules();
							}
						}
					}, false, requiresNewTransaction);
		}

		return importContext.getLog();
	}

	@Override
	public void moveImportedFile(final NodeRef nodeRef, final boolean hasFailed, final String titleLog, final String fileLog) {

		RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>() {
			@Override
			public Object execute() throws Exception {
				if (nodeService.exists(nodeRef)) {

					// delete files that have the same name before moving it in
					// the succeeded or failed folder
					String csvFileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

					// failed
					NodeRef failedFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(),
							FULL_PATH_IMPORT_FAILED_FOLDER);

					if (failedFolder != null) {
						NodeRef csvNodeRef = nodeService.getChildByName(failedFolder, ContentModel.ASSOC_CONTAINS, csvFileName);
						if (csvNodeRef != null) {
							nodeService.deleteNode(csvNodeRef);
						}
					}

					// succeeded
					NodeRef succeededFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(),
							FULL_PATH_IMPORT_SUCCEEDED_FOLDER);

					if (succeededFolder != null) {
						NodeRef targetNodeRef = nodeService.getChildByName(succeededFolder, ContentModel.ASSOC_CONTAINS, csvFileName);
						if (targetNodeRef != null) {
							nodeService.deleteNode(targetNodeRef);
						}
					}

					// log
					if (fileLog != null && !fileLog.isEmpty()) {
						NodeRef logFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(),
								FULL_PATH_IMPORT_LOG_FOLDER);
						if (logFolder != null) {
							String logFileName = csvFileName.substring(0, csvFileName.length() - 4) + RepoConsts.EXTENSION_LOG;
							createLogFile(logFolder, logFileName, fileLog);
						}
					}

					// move nodeRef in the right folder
					NodeRef parentNodeRef = hasFailed ? failedFolder : succeededFolder;
					if (parentNodeRef != null) {
						nodeService.moveNode(nodeRef, parentNodeRef, ContentModel.ASSOC_CONTAINS, nodeService.getType(nodeRef));
					}

					nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, titleLog);
				}

				return null;
			}
		};
		serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
	}

	private String cleanPath(String pathValue) {
		if (pathValue.startsWith("/")) {
			return pathValue.substring(1);
		}
		return pathValue;
	}

	/**
	 * Import a batch of lines
	 * 
	 * @param importContext
	 * @param lastIndex
	 * @return
	 * @throws Exception
	 */
	private ImportContext importInBatch(ImportContext importContext, final int lastIndex) throws Exception {

		Element mappingElt = null;
		String[] arrStr = null;

		while (importContext.getImportIndex() < lastIndex && (arrStr = importContext.nextLine()) != null) {

			String prefix = PropertiesHelper.cleanValue(arrStr[COLUMN_PREFIX]);

			if (prefix.equals(PFX_PATH)) {

				importContext.setParentNodeRef(null);

				String pathValue = arrStr[COLUMN_PATH];
				importContext.setPath(cleanPath(pathValue));

				if (pathValue.isEmpty())
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_PATH, importContext.getImportIndex()));

				if (pathValue.startsWith(PATH_SITES) || pathValue.startsWith(RepoConsts.PATH_SEPARATOR + PATH_SITES)) {
					importContext.setSiteDocLib(true);
				} else {
					importContext.setSiteDocLib(false);
				}

				List<String> paths = new ArrayList<String>();
				String[] arrPath = pathValue.split(RepoConsts.PATH_SEPARATOR);
				for (String path : arrPath)
					paths.add(path);

				NodeRef parentNodeRef = repoService.getOrCreateFolderByPaths(repositoryHelper.getCompanyHome(), paths);
				importContext.setParentNodeRef(parentNodeRef);
			} else if (prefix.equals(PFX_DOCS_BASE_PATH)) {

				String importDocsBasePath = arrStr[COLUMN_MAPPING];
				if (!importDocsBasePath.isEmpty()) {
					importContext.setDocsBasePath(importDocsBasePath);
				}

			} else if (prefix.equals(PFX_IMPORT_TYPE)) {

				importContext.setImportType(null);

				String importTypeValue = arrStr[COLUMN_IMPORT_TYPE];
				if (importTypeValue.isEmpty()) {
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_IMPORT_TYPE, importContext.getImportIndex()));
				}

				ImportType importType = ImportType.valueOf(importTypeValue);
				importContext.setImportType(importType);
			} else if (prefix.equals(PFX_DISABLED_POLICIES)) {

				importContext.getDisabledPolicies().clear();

				String disabledPoliciesValue = arrStr[COLUMN_DISABLED_POLICIES];
				if (!disabledPoliciesValue.isEmpty()) {
					for (String disabledPolicy : disabledPoliciesValue.split(RepoConsts.MULTI_VALUES_SEPARATOR)) {
						importContext.getDisabledPolicies().add(QName.createQName(disabledPolicy, serviceRegistry.getNamespaceService()));
					}

				}
			} else if (prefix.equals(PFX_TYPE)) {

				importContext.setType(null);

				String typeValue = arrStr[COLUMN_TYPE];
				if (typeValue.isEmpty())
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_TYPE, importContext.getImportIndex()));

				QName type = QName.createQName(typeValue, serviceRegistry.getNamespaceService());
				importContext.setType(type);

			} else if (prefix.equals(PFX_ENTITY_TYPE)) {

				importContext.setEntityType(null);

				String typeValue = arrStr[COLUMN_ENTITY_TYPE];
				if (typeValue.isEmpty())
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_ENTITY_TYPE, importContext.getImportIndex()));

				QName entityType = QName.createQName(typeValue, serviceRegistry.getNamespaceService());
				importContext.setEntityType(entityType);
			} else if (prefix.equals(PFX_STOP_ON_FIRST_ERROR)) {

				String stopOnFirstErrorValue = arrStr[COLUMN_TYPE];
				if (!stopOnFirstErrorValue.isEmpty()) {
					importContext.setStopOnFirstError(Boolean.valueOf(stopOnFirstErrorValue));
				}
			} else if (prefix.equals(PFX_DELETE_DATALIST)) {

				String deleteDataList = arrStr[COLUMN_TYPE];
				if (!deleteDataList.isEmpty()) {
					importContext.setDeleteDataList(Boolean.valueOf(deleteDataList));
				}

			} else if (prefix.equals(PFX_COLUMS)) {

				boolean undefinedColumns = true;
				List<String> columns = new ArrayList<String>(arrStr.length);
				for (int z_idx = 1; z_idx < arrStr.length; z_idx++) {
					if (!arrStr[z_idx].isEmpty()) {
						columns.add(arrStr[z_idx]);
						undefinedColumns = false;
					}
				}

				if (undefinedColumns) {
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_COLUMS, importContext.getImportIndex()));
				}

				importContext = importNodeVisitor.loadMappingColumns(mappingElt, columns, importContext);

			} else if (prefix.equals(PFX_MAPPING)) {

				mappingElt = null;

				String mappingValue = arrStr[COLUMN_MAPPING];
				if (mappingValue.isEmpty())
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_MAPPING, importContext.getImportIndex()));

				mappingElt = loadMapping(mappingValue);
				importContext = importNodeVisitor.loadClassMapping(mappingElt, importContext);
			} else if (prefix.equals(PFX_VALUES)) {

				try {

					boolean undefinedValues = true;
					List<String> values = new ArrayList<String>(arrStr.length);
					for (int z_idx = 1; z_idx < arrStr.length; z_idx++) {

						String value = PropertiesHelper.cleanValue(arrStr[z_idx]);
						values.add(value);

						if (!value.isEmpty()) {
							// one value defined, OK
							undefinedValues = false;
						}
					}

					if (undefinedValues) {
						throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, PFX_VALUES, importContext.getImportIndex()));
					}

					// disable policy
					for (QName disabledPolicy : importContext.getDisabledPolicies()) {
						logger.debug("disableBehaviour: " + disabledPolicy);
						policyBehaviourFilter.disableBehaviour(disabledPolicy);
					}

					// TODO Use factory or @annotation instead of if
					if (ImportType.Comments.equals(importContext.getImportType())) {
						importCommentsVisitor.importNode(importContext, values);
					} else if (dictionaryService.isSubClass(importContext.getType(), PLMModel.TYPE_PRODUCT)) {
						importProductVisitor.importNode(importContext, values);
					} else if (ImportType.EntityListAspect.equals(importContext.getImportType())) {
						importEntityListAspectVisitor.importNode(importContext, values);
					} else if (ImportType.EntityListItem.equals(importContext.getImportType())) {
						importEntityListItemVisitor.importNode(importContext, values);
					} else {
						importNodeVisitor.importNode(importContext, values);
					}
					// Do not remove that
					logger.info(importContext.markCurrLineSuccess());
				} catch (ImporterException e) {

					if (importContext.isStopOnFirstError()) {
						throw e;
					} else {
						// Do not remove that
						logger.error(importContext.markCurrLineError(e));
					}
				} catch (Exception e) {

					if (importContext.isStopOnFirstError()) {
						throw e;
					} else {
						// store the exception and the printStack and continue
						// import...
						// Do not remove that
						logger.error(importContext.markCurrLineError(e), e);
					}
				} finally {
					// enable policy
					for (QName disabledPolicy : importContext.getDisabledPolicies()) {
						policyBehaviourFilter.enableBehaviour(disabledPolicy);
					}
				}
			} else if (!(prefix.isEmpty() || prefix.equals(PFX_COMMENT))) {
				throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNSUPPORTED_PREFIX, importContext.getImportIndex(), prefix));
			}
		}

		return importContext;
	}

	/**
	 * Load the mapping definition file.
	 * 
	 * @param name
	 *            the name
	 * @return the element
	 */

	private Element loadMapping(String name) throws ImporterException {

		Element mappingElt = null;
		NodeRef mappingNodeRef = null;

		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().inPath(FULL_PATH_IMPORT_MAPPING)
				.andPropEquals(ContentModel.PROP_NAME, name + ".xml");

		logger.debug(queryBuilder.toString());

		mappingNodeRef = queryBuilder.singleValue();

		if (mappingNodeRef == null) {
			String msg = I18NUtil.getMessage(MSG_ERROR_MAPPING_NOT_FOUND, name);
			logger.error(msg);
			throw new ImporterException(msg);
		}

		ContentReader reader = contentService.getReader(mappingNodeRef, ContentModel.PROP_CONTENT);
		InputStream is = reader.getContentInputStream();
		SAXReader saxReader = new SAXReader();

		try {
			Document doc = saxReader.read(is);
			mappingElt = doc.getRootElement();
		} catch (DocumentException e) {
			String msg = I18NUtil.getMessage(MSG_ERROR_READING_MAPPING, name, e.getMessage());
			logger.error(msg, e);
			throw new ImporterException(msg);
		} finally {
			IOUtils.closeQuietly(is);
		}

		return mappingElt;
	}

	private void createLogFile(NodeRef parentNodeRef, String fileName, String content) {

		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, fileName);

		NodeRef nodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, fileName);
		if (nodeRef == null) {
			nodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					ContentModel.TYPE_CONTENT, properties).getChildRef();
		}

		InputStream is = null;
		try {
			ContentWriter contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
			contentWriter.setEncoding("UTF-8");
			contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
			is = new ByteArrayInputStream(content.getBytes());
			contentWriter.putContent(is);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	@Override
	public void writeLogInFileTitle(final NodeRef nodeRef, final String log, final boolean hasFailed) {

		RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>() {
			@Override
			public Object execute() throws Exception {
				if (nodeService.exists(nodeRef)) {
					serviceRegistry.getRuleService().disableRules();
					try {
						if (hasFailed) {
							nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, log);
						} else {
							nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, "");
						}
					} finally {
						serviceRegistry.getRuleService().enableRules();
					}
				}
				return null;
			}
		};
		serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
	}
}
