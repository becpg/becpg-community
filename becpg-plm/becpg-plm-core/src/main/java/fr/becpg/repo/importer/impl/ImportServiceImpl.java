/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
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

import fr.becpg.config.mapping.MappingException;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchPriority;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.BatchStepAdapter;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.ImportFileReader;
import fr.becpg.repo.importer.ImportService;
import fr.becpg.repo.importer.ImportType;
import fr.becpg.repo.importer.ImportVisitor;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.importer.MappingLoaderFactory;
import fr.becpg.repo.importer.MappingType;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Import service.
 *
 * @author querephi
 * @version $Id: $Id
 */

@Service("importService")
public class ImportServiceImpl implements ImportService {

	private static final String FULL_PATH_IMPORT_MAPPING = "/cm:System/cm:Exchange/cm:Import/cm:Mapping";
	private static final String FULL_PATH_IMPORT_FAILED_FOLDER = "/app:company_home/cm:Exchange/cm:Import/cm:ImportFailed";
	private static final String FULL_PATH_IMPORT_LOG_FOLDER = "/app:company_home/cm:Exchange/cm:Import/cm:ImportLog";
	private static final String FULL_PATH_IMPORT_SUCCEEDED_FOLDER = "/app:company_home/cm:Exchange/cm:Import/cm:ImportSucceeded";

	private static final String PATH_SITES = "st:sites";

	private static final String MSG_INFO_IMPORT_BATCH = "import_service.info.import_batch";

	private static final String MSG_ERROR_UNSUPPORTED_PREFIX = "import_service.error.err_unsupported_prefix";
	private static final String MSG_ERROR_MAPPING_NOT_FOUND = "import_service.error.err_mapping_not_found";
	private static final String MSG_ERROR_READING_MAPPING = "import_service.error.err_reading_mapping";
	private static final String MSG_ERROR_UNDEFINED_LINE = "import_service.error.err_undefined_line";

	/** Constant <code>SEPARATOR=';'</code> */
	public static final char SEPARATOR = ';';

	private static final int COLUMN_PREFIX = 0;
	private static final int COLUMN_MAPPING = 1;
	private static final int COLUMN_PATH = 1;
	private static final int COLUMN_TYPE = 1;
	private static final int COLUMN_LIST_TYPE = 1;
	private static final int COLUMN_ENTITY_TYPE = 1;
	private static final int COLUMN_IMPORT_TYPE = 1;
	private static final int COLUMN_DISABLED_POLICIES = 1;
	
	private static final String LOG_STARTING_DATE = "Starting date: ";
	private static final String LOG_ENDING_DATE = "Ending date: ";
	private static final String LOG_ERROR = "Error: ";
	private static final int ERROR_LOGS_LIMIT = 50;
	private static final String LOG_ERROR_MAX_REACHED = "More than " + ERROR_LOGS_LIMIT + " errors, stop printing";
	private static final String LOG_SEPARATOR = "\n";


	private static final int BATCH_SIZE = 10;

	private static final Log logger = LogFactory.getLog(ImportServiceImpl.class);

	@Autowired
	private NodeService nodeService;
	@Autowired
	private ContentService contentService;
	@Autowired
	private NamespaceService nameSpaceService;
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

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private RuleService ruleService;

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
	@Autowired
	private MappingLoaderFactory mappingLoaderFactory;
	
	@Autowired
	private BatchQueueService batchQueueService;

	/**
	 * {@inheritDoc}
	 *
	 * Import a text file
	 */
	@Override
	public BatchInfo importText(final NodeRef nodeRef, boolean doUpdate, boolean requiresNewTransaction, Boolean doNotMoveNode)  {

		logger.debug("start import");
		
		String startlog = LOG_STARTING_DATE + Calendar.getInstance().getTime();

		// prepare context
		final ImportContext importContext = transactionService.getRetryingTransactionHelper().doInTransaction(() -> createImportContext(nodeRef), true, requiresNewTransaction);

		importContext.setDoUpdate(doUpdate);

		logger.debug("Start batch import of size :" + BATCH_SIZE);

		int lastIndex = importContext.getImportFileReader().getTotalLineCount();
		int nbBatches = (lastIndex / BATCH_SIZE) + 1;
		
		BatchInfo batchInfo = new BatchInfo("import_" + importContext.getImportFileName(), "becpg.batch.import", importContext.getImportFileName());
		
		batchInfo.setWorkerThreads(1);
		batchInfo.setBatchSize(1);
		batchInfo.setPriority(BatchPriority.VERY_HIGH);
		
		BatchStep<Integer> batchStep = new BatchStep<>();
		
		BatchStepAdapter batchStepAdapter = new BatchStepAdapter() {
			@Override
			public void afterStep() {
				afterImport(importContext, startlog, doNotMoveNode);
			}
			
			@Override
			public void onError(String lastErrorEntryId, String lastError) {
				importContext.setErrorLogs(importContext.getErrorLogs() + LOG_ERROR + lastError);
			};
		};
		
		batchStep.setBatchStepListener(batchStepAdapter);
		
		batchStep.setProcessWorker(new BatchProcessWorkerAdaptor<Integer>() {

			private boolean hasToStop = false;
			
			@Override
			public void process(Integer index) throws Throwable {

				if (hasToStop) {
					return;
				}
				
				importContext.setImportIndex(index * BATCH_SIZE);
				int tempIndex = (index + 1) * BATCH_SIZE;
				final int finalLastIndex = tempIndex > lastIndex ? lastIndex : tempIndex;
				final ImportContext finalImportContext = importContext;

				// add info message in log and file import
				if (logger.isInfoEnabled()) {

					logger.info(I18NUtil.getMessage(MSG_INFO_IMPORT_BATCH, (index + 1), nbBatches, importContext.getImportFileName(),
							(importContext.getImportIndex() + 1), (finalLastIndex + 1)));

				}
				try {
					importInBatch(finalImportContext, finalLastIndex);
				} catch (Exception e) {
					if (RetryingTransactionHelper.extractRetryCause(e) != null) {
					    throw e;
	                }
					if (importContext.isStopOnFirstError()) {
						hasToStop = true;
					}
					
					logger.error("Failed to import file text", e);

					// set printStackTrance in description
					try (StringWriter sw = new StringWriter()) {
						try (PrintWriter pw = new PrintWriter(sw)) {
							e.printStackTrace(pw);
							String stackTrace = sw.toString();
							finalImportContext.setErrorLogs(finalImportContext.getErrorLogs() + LOG_ERROR + stackTrace);
						}
					}
				}
			}
		});
		
		List<Integer> indexEntries = new ArrayList<>();
		
		for (int index = 0; index < nbBatches; index++) {
			indexEntries.add(index);
		}
		
		batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(indexEntries));
		
		batchQueueService.queueBatch(batchInfo, List.of(batchStep));
		
		if ((!importContext.getLog().isEmpty()) && (importContext.getImportFileReader() instanceof ImportExcelFileReader)) {

			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
				ruleService.disableRules();
				try {

					importContext.getImportFileReader().writeErrorInFile(contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true));
					return importContext;
				} finally {
					ruleService.enableRules();
				}
			}, false, requiresNewTransaction);
		}
		
		return batchInfo;
	}
	
	private void afterImport(ImportContext importContext, String startlog, Boolean doNotMoveNode) {
			
		String errosLogs = importContext.getErrorLogs();
		
		boolean hasFailed = errosLogs != null && !errosLogs.isBlank();

		StringBuilder first50ErrorsLog = new StringBuilder(); // log stored in title, first
		// 50 errors
		StringBuilder after50ErrorsLog = new StringBuilder(); // log store in
		
		if (!importContext.getLog().isEmpty()) {
			int limit = 0;
			for (String error : importContext.getLog()) {
				if (limit <= ERROR_LOGS_LIMIT) {
					first50ErrorsLog.append(LOG_SEPARATOR);
					first50ErrorsLog.append(error);
				} else {
					after50ErrorsLog.append(LOG_ERROR);
					after50ErrorsLog.append(error);
				}
				limit++;
			}

			hasFailed = true;
		}
		
		String endlog = LOG_ENDING_DATE + Calendar.getInstance().getTime().toString();
		
		String log = startlog + LOG_SEPARATOR + (errosLogs != null ? errosLogs + LOG_SEPARATOR : "")
				+ (first50ErrorsLog.toString().isEmpty() ? "" : first50ErrorsLog.toString() + LOG_SEPARATOR)
				+ (after50ErrorsLog.toString().isEmpty() ? "" : LOG_ERROR_MAX_REACHED + LOG_SEPARATOR) + endlog;

		String allLog = startlog + LOG_SEPARATOR + (errosLogs != null ? errosLogs + LOG_SEPARATOR : "")
				+ (first50ErrorsLog.toString().isEmpty() ? "" : first50ErrorsLog.toString() + LOG_SEPARATOR)
				+ (after50ErrorsLog.toString().isEmpty() ? "" : after50ErrorsLog.toString() + LOG_SEPARATOR) + endlog;

		// set log, stackTrace and move file
		if ((doNotMoveNode == null) || Boolean.FALSE.equals(doNotMoveNode)) {
			moveImportedFile(importContext.getNodeRef(), hasFailed, log, allLog);
		} else {
			writeLogInFileTitle(importContext.getNodeRef(), log, hasFailed);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void moveImportedFile(final NodeRef nodeRef, final boolean hasFailed, final String titleLog, final String fileLog) {

		RetryingTransactionCallback<Object> actionCallback = () -> {
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
				if ((fileLog != null) && !fileLog.isEmpty()) {
					NodeRef logFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(),
							FULL_PATH_IMPORT_LOG_FOLDER);
					if (logFolder != null) {
						String logFileName = csvFileName.replace(".", "_") + RepoConsts.EXTENSION_LOG;
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
		};
		transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
	}

	/** {@inheritDoc} */
	@Override
	public void writeLogInFileTitle(final NodeRef nodeRef, final String log, final boolean hasFailed) {
	
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			if (nodeService.exists(nodeRef)) {
				ruleService.disableRules();
				try {
					if (hasFailed) {
						nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, log);
					} else {
						nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, "");
					}
				} finally {
					ruleService.enableRules();
				}
			}
			return null;
		}, false, true);
	}

	private ImportContext createImportContext(final NodeRef nodeRef) throws IOException {
		ImportContext importContext1 = new ImportContext();
	
		importContext1.setNodeRef(nodeRef);
		
		ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
		try (InputStream is = reader.getContentInputStream()) {
	
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
	
			importContext1.setImportFileName(fileName);
	
			ImportFileReader imporFileReader;
			if (MimetypeMap.MIMETYPE_EXCEL.equals(mimeType) || MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET.equals(mimeType)
					|| MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_MACRO.equals(mimeType)
					|| MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_BINARY_MACRO.equals(mimeType)) {
				imporFileReader = new ImportExcelFileReader(is, importContext1.getPropertyFormats());
			} else {
				imporFileReader = new ImportCSVFileReader(is, charset, SEPARATOR);
			}
	
			importContext1.setImportFileReader(imporFileReader);
	
		}
		return importContext1;
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
	 * @throws ImporterException 
	 * @throws MappingException 
	 * @throws IOException 
	 */
	private ImportContext importInBatch(ImportContext importContext, final int lastIndex) throws ImporterException, MappingException, IOException, ParseException  {

		Element mappingElt = null;
		String[] arrStr;

		Map<String, Object> annotationMapping = new HashMap<>();

		while ((importContext.getImportIndex() < lastIndex) && ((arrStr = importContext.nextLine()) != null)) {
			if (arrStr.length > 0) {
				String prefix = PropertiesHelper.cleanValue(arrStr[COLUMN_PREFIX]);

				if (prefix.equals(ImportHelper.PFX_PATH)) {

					importContext.setParentNodeRef(null);

					String pathValue = arrStr[COLUMN_PATH];
					importContext.setPath(cleanPath(pathValue));

					if (pathValue.isEmpty()) {
						throw new ImporterException(
								I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, ImportHelper.PFX_PATH, importContext.getImportIndex()));
					}
					
					importContext.setSiteDocLib(pathValue.startsWith(PATH_SITES) || pathValue.startsWith(RepoConsts.PATH_SEPARATOR + PATH_SITES));
				
					List<String> paths = new ArrayList<>();
					String[] arrPath = pathValue.split(RepoConsts.PATH_SEPARATOR);
					Collections.addAll(paths, arrPath);

					NodeRef parentNodeRef = repoService.getOrCreateFolderByPaths(repositoryHelper.getCompanyHome(), paths);
					importContext.setParentNodeRef(parentNodeRef);
				} else if (prefix.equals(ImportHelper.PFX_DOCS_BASE_PATH)) {

					String importDocsBasePath = arrStr[COLUMN_MAPPING];
					if (!importDocsBasePath.isEmpty()) {
						importContext.setDocsBasePath(importDocsBasePath);
					}

				} else if (prefix.equals(ImportHelper.PFX_IMPORT_TYPE)) {

					importContext.setImportType(null);

					String importTypeValue = arrStr[COLUMN_IMPORT_TYPE];
					if (importTypeValue.isEmpty()) {
						throw new ImporterException(
								I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, ImportHelper.PFX_IMPORT_TYPE, importContext.getImportIndex()));
					}

					ImportType importType = ImportType.valueOf(importTypeValue);
					importContext.setImportType(importType);
				} else if (prefix.equals(ImportHelper.PFX_DISABLED_POLICIES)) {

					importContext.getDisabledPolicies().clear();

					String disabledPoliciesValue = arrStr[COLUMN_DISABLED_POLICIES];
					if (!disabledPoliciesValue.isEmpty()) {
						for (String disabledPolicy : disabledPoliciesValue.split(RepoConsts.MULTI_VALUES_SEPARATOR)) {
							importContext.getDisabledPolicies().add(QName.createQName(disabledPolicy, nameSpaceService));
						}

					}
				} else if (prefix.equals(ImportHelper.PFX_LIST_TYPE)) {

					importContext.setListType(null);

					String typeValue = arrStr[COLUMN_LIST_TYPE];
					if (typeValue.isEmpty()) {
						throw new ImporterException(
								I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, ImportHelper.PFX_LIST_TYPE, importContext.getImportIndex()));
					}

					QName type = QName.createQName(typeValue, nameSpaceService);
					importContext.setListType(type);

				} else if (prefix.equals(ImportHelper.PFX_TYPE)) {

					importContext.setType(null);

					String typeValue = arrStr[COLUMN_TYPE];
					if (typeValue.isEmpty()) {
						throw new ImporterException(
								I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, ImportHelper.PFX_TYPE, importContext.getImportIndex()));
					}

					QName type = QName.createQName(typeValue, nameSpaceService);
					importContext.setType(type);

				} else if (prefix.equals(ImportHelper.PFX_ENTITY_TYPE)) {

					importContext.setEntityType(null);

					String typeValue = arrStr[COLUMN_ENTITY_TYPE];
					if (typeValue.isEmpty()) {
						throw new ImporterException(
								I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, ImportHelper.PFX_ENTITY_TYPE, importContext.getImportIndex()));
					}

					QName entityType = QName.createQName(typeValue, nameSpaceService);
					importContext.setEntityType(entityType);
				} else if (prefix.equals(ImportHelper.PFX_STOP_ON_FIRST_ERROR)) {

					String stopOnFirstErrorValue = arrStr[COLUMN_TYPE];
					if (!stopOnFirstErrorValue.isEmpty()) {
						importContext.setStopOnFirstError(Boolean.valueOf(stopOnFirstErrorValue));
					}
				} else if (prefix.equals(ImportHelper.PFX_DELETE_DATALIST)) {

					String deleteDataList = arrStr[COLUMN_TYPE];
					if (!deleteDataList.isEmpty()) {
						importContext.setDeleteDataList(Boolean.valueOf(deleteDataList));
					}

				} else if (prefix.equals(ImportHelper.PFX_COLUMS)) {

					if ((annotationMapping != null) && !annotationMapping.isEmpty()) {
						annotationMapping.put(ImportHelper.PFX_COLUMS, Arrays.asList(arrStr));
						importContext = importNodeVisitor.loadClassMapping(annotationMapping, importContext,
								mappingLoaderFactory.getMappingLoader(MappingType.ANNOTATION));
					}

					boolean undefinedColumns = true;
					List<String> columns = new ArrayList<>(arrStr.length);

					for (int z_idx = 1; z_idx < arrStr.length; z_idx++) {
						if (!arrStr[z_idx].isEmpty()) {
							columns.add(arrStr[z_idx]);
							undefinedColumns = false;
						}
					}
					if (undefinedColumns) {
						throw new ImporterException(
								I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, ImportHelper.PFX_COLUMS, importContext.getImportIndex()));
					}

					importContext = importNodeVisitor.loadMappingColumns(mappingElt, columns, importContext);
					
				} else if (prefix.equals(ImportHelper.PFX_COLUMNS_PARAMS)) {
					@SuppressWarnings("unchecked")
					List<List<String>> columnsParams = (List<List<String>>) annotationMapping.get(ImportHelper.PFX_COLUMNS_PARAMS);
					if (columnsParams == null) {
						columnsParams = new ArrayList<>();
					}
					columnsParams.add(Arrays.asList(arrStr));
					annotationMapping.put(ImportHelper.PFX_COLUMNS_PARAMS, columnsParams);

					if (annotationMapping.isEmpty()) {
						throw new ImporterException(
								I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, ImportHelper.PFX_COLUMNS_PARAMS, importContext.getImportIndex()));
					}

				} else if (prefix.equals(ImportHelper.PFX_MAPPING)) {

					String mappingValue = arrStr[COLUMN_MAPPING];
					if (mappingValue.isEmpty()) {
						throw new ImporterException(
								I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, ImportHelper.PFX_MAPPING, importContext.getImportIndex()));
					}

					mappingElt = loadMapping(mappingValue);
					importContext = importNodeVisitor.loadClassMapping(mappingElt, importContext,
							mappingLoaderFactory.getMappingLoader(MappingType.XML));

				} else if (prefix.equals(ImportHelper.PFX_VALUES)) {

					try {

						boolean undefinedValues = true;
						List<String> values = new ArrayList<>(arrStr.length);
						for (int z_idx = 1; z_idx < arrStr.length; z_idx++) {

							String value = PropertiesHelper.cleanValue(arrStr[z_idx]);
							values.add(value);

							if (!value.isEmpty()) {
								// one value defined, OK
								undefinedValues = false;
							}
						}

						if (undefinedValues) {
							throw new ImporterException(
									I18NUtil.getMessage(MSG_ERROR_UNDEFINED_LINE, ImportHelper.PFX_VALUES, importContext.getImportIndex()));
						}

						// disable policy
						for (QName disabledPolicy : importContext.getDisabledPolicies()) {
							logger.debug("disableBehaviour: " + disabledPolicy);
							policyBehaviourFilter.disableBehaviour(disabledPolicy);
						}

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
						String successMessage = importContext.markCurrLineSuccess();
						if (logger.isDebugEnabled()) {
							logger.debug(successMessage);
						}

					} catch (ImporterException | ParseException e) {

						if (importContext.isStopOnFirstError()) {
							throw e;
						} else {
							// Do not remove that
							String errorMessage = importContext.markCurrLineError(e);
							logger.error(errorMessage);
						}
					} catch (Exception e) {

						if (RetryingTransactionHelper.extractRetryCause(e) != null) {
						    throw e;
		                }
						
						if (importContext.isStopOnFirstError()) {
							throw e;
						}

						String errorMessage = importContext.markCurrLineError(e);
						logger.error(errorMessage, e);

					} finally {
						// enable policy
						for (QName disabledPolicy : importContext.getDisabledPolicies()) {
							policyBehaviourFilter.enableBehaviour(disabledPolicy);
						}
					}
				} else if (!(prefix.isEmpty() || prefix.equals(ImportHelper.PFX_COMMENT))) {
					throw new ImporterException(I18NUtil.getMessage(MSG_ERROR_UNSUPPORTED_PREFIX, importContext.getImportIndex(), prefix));
				}
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
	 * @throws IOException
	 * @throws ContentIOException
	 */

	private Element loadMapping(String name) throws ImporterException, IOException {

		Element mappingElt = null;

		NodeRef mappingNodeRef = BeCPGQueryBuilder.createQuery()
				.parent(BeCPGQueryBuilder.createQuery().selectNodeByPath(repositoryHelper.getCompanyHome(), FULL_PATH_IMPORT_MAPPING))
				.andPropEquals(ContentModel.PROP_NAME, name + ".xml").inDB().singleValue();

		if (mappingNodeRef == null) {
			String msg = I18NUtil.getMessage(MSG_ERROR_MAPPING_NOT_FOUND, name);
			logger.error(msg);
			throw new ImporterException(msg);
		}

		ContentReader reader = contentService.getReader(mappingNodeRef, ContentModel.PROP_CONTENT);
		try (InputStream is = reader.getContentInputStream()) {
			SAXReader saxReader = new SAXReader();
			Document doc = saxReader.read(is);
			mappingElt = doc.getRootElement();
		} catch (DocumentException e) {
			String msg = I18NUtil.getMessage(MSG_ERROR_READING_MAPPING, name, e.getMessage());
			logger.error(msg, e);
			throw new ImporterException(msg);
		}

		return mappingElt;
	}

	private void createLogFile(NodeRef parentNodeRef, String fileName, String content) throws IOException {

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, fileName);

		NodeRef nodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, fileName);
		if (nodeRef == null) {
			nodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					ContentModel.TYPE_CONTENT, properties).getChildRef();
		}

		ContentWriter contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		contentWriter.setEncoding("UTF-8");
		contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
		try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
			contentWriter.putContent(is);
		}
	}
}
