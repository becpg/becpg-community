package fr.becpg.repo.signature;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.becpg.artworks.signature.model.SignatureModel;
import fr.becpg.artworks.signature.model.SignatureStatus;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;

/**
 * <p>SignatureProjectHelper class.</p>
 *
 * @author matthieu
 */
@Component
public class SignatureProjectHelper {
	

	private static final Log logger = LogFactory.getLog(SignatureProjectHelper.class);

	private static final String DELIVERABLE_SUFFIX_PREPARE = " - prepare";
	private static final String DELIVERABLE_SUFFIX_URL = " - url";
	private static final String DELIVERABLE_SUFFIX_SIGN = " - sign";
	private static final String DELIVERABLE_SUFFIX_VALIDATE = " - validate";
	private static final String DELIVERABLE_SUFFIX_REJECT = " - reject";
	private static final String DELIVERABLE_SUFFIX_DOC = " - doc";
	private static final String DELIVERABLE_SUFFIX_VALIDATE_DOC = DELIVERABLE_SUFFIX_VALIDATE + DELIVERABLE_SUFFIX_DOC;
	private static final String DELIVERABLE_SUFFIX_REJECT_DOC = DELIVERABLE_SUFFIX_REJECT + DELIVERABLE_SUFFIX_DOC;

	private static final int MAX_RECURSION_DEPTH = 4;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private EntityService entityService;

	@Autowired
	private ContentService contentService;
	
	@Autowired
	private RepoService repoService;


	/**
	 * <p>extractReportName.</p>
	 *
	 * @param reportNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	public String extractReportName(NodeRef reportNodeRef) {
		String reportName = (String) nodeService.getProperty(reportNodeRef, ContentModel.PROP_NAME);

		int lastDotIndex = reportName.lastIndexOf(".");
		
		if (lastDotIndex != -1) {
			String nameWithoutExtension = reportName.substring(0, lastDotIndex);
			String extension = reportName.substring(lastDotIndex);
			String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).substring(0, 10);
			reportName = nameWithoutExtension + " - " + date + extension;
		}
		return reportName;
	}
	

	/**
	 * <p>copyReport.</p>
	 *
	 * @param parentFolder a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param reportNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef copyReport(NodeRef parentFolder, NodeRef reportNodeRef) {
		String reportName = extractReportName(reportNodeRef);
		
		reportName = repoService.getAvailableName(parentFolder, reportName, false, true);
		
		Map<QName, Serializable> props = new HashMap<>();

		props.put(ContentModel.PROP_NAME, reportName);

		NodeRef reportCopy = nodeService
				.createNode(parentFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT, props)
				.getChildRef();
		
		nodeService.setProperty(reportCopy, ContentModel.PROP_OWNER, AuthenticationUtil.SYSTEM_USER_NAME);

		ContentReader reader = contentService.getReader(reportNodeRef, ContentModel.PROP_CONTENT);
		ContentWriter writer = contentService.getWriter(reportCopy, ContentModel.PROP_CONTENT, true);
		writer.setEncoding(reader.getEncoding());
		writer.setMimetype(reader.getMimetype());

		writer.putContent(reader);
		
		return reportCopy;
	}
	
	
	/**
	 * <p>copyReports.</p>
	 *
	 * @param originalDocuments a {@link java.util.List} object
	 * @return a {@link java.util.List} object
	 */
	public List<NodeRef> copyReports(List<NodeRef> originalDocuments) {
		List<NodeRef> documents = new ArrayList<>();
		for (NodeRef originalDocument : originalDocuments) {
			if (ReportModel.TYPE_REPORT.equals(nodeService.getType(originalDocument))) {
				NodeRef entity = entityService.getEntityNodeRef(originalDocument, ReportModel.TYPE_REPORT);
				String signedReportsName = TranslateHelper.getTranslatedPath("SignedReports");
				NodeRef signedReportsFolder = nodeService.getChildByName(entity, ContentModel.ASSOC_CONTAINS, signedReportsName);
				if (signedReportsFolder == null) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, signedReportsName);
					
					signedReportsFolder = nodeService.createNode(entity, ContentModel.ASSOC_CONTAINS,
							ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_FOLDER, properties).getChildRef();
				}
				documents.add(copyReport(signedReportsFolder, originalDocument));
			} else {
				documents.add(originalDocument);
			}
		}
		return documents;
	}
	
	/**
	 * <p>findDocumentsToSign.</p>
	 *
	 * @param folder a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param signed a boolean
	 * @return a {@link java.util.List} object
	 */
	public List<NodeRef> findDocumentsToSign(NodeRef folder, boolean signed) {
		return findDocumentsToSign(folder, signed, 0);
	}

	private List<NodeRef> findDocumentsToSign(NodeRef folder, boolean signed, int depth) {
		List<NodeRef> docs = new ArrayList<>();

		if (depth >= MAX_RECURSION_DEPTH) {
			if (logger.isDebugEnabled()) {
				logger.debug("Maximum recursion depth (" + MAX_RECURSION_DEPTH + ") reached for folder: " + folder);
			}
			return docs;
		}

		List<NodeRef> children = associationService.getChildAssocs(folder, ContentModel.ASSOC_CONTAINS);
		for (NodeRef child : children) {
			QName type = nodeService.getType(child);
			if (ContentModel.TYPE_CONTENT.equals(type)) {
				if (nodeService.hasAspect(child, SignatureModel.ASPECT_SIGNATURE)) {
					String status = (String) nodeService.getProperty(child, SignatureModel.PROP_STATUS);
					if (signed && SignatureStatus.Signed.toString().equals(status) || !signed && !SignatureStatus.Signed.toString().equals(status)) {
						docs.add(child);
					}
				}
			} else if (ContentModel.TYPE_FOLDER.equals(type)) {
				docs.addAll(findDocumentsToSign(child, signed, depth + 1));
			}
		}
		return docs;
	}
	
	/**
	 * <p>getDeliverableByDocuments.</p>
	 *
	 * @param project a {@link fr.becpg.repo.project.data.ProjectData} object
	 * @return a {@link java.util.Map} object containing deliverables grouped by document NodeRef
	 */
	public Map<NodeRef, List<DeliverableListDataItem>> getDeliverableByDocuments(ProjectData project) {
		Map<NodeRef, List<DeliverableListDataItem>> deliverableByDocuments = new HashMap<>();

		if (logger.isDebugEnabled()) {
			logger.debug("Grouping deliverables by documents for project: " + project.getName());
		}

		if (project.getDeliverableList() != null) {
			// First pass: collect all deliverables by their base document name
			Map<String, List<DeliverableListDataItem>> deliverablesByBaseName = new HashMap<>();

			for (DeliverableListDataItem deliverable : project.getDeliverableList()) {
				String deliverableName = deliverable.getName();
				if (deliverableName != null) {
					// Extract base name (document name + first name + last name)
					int lastDashIndex = deliverableName.lastIndexOf(" - ");
					if (lastDashIndex != -1) {
						String baseName = deliverableName.substring(0, lastDashIndex);
						deliverablesByBaseName.computeIfAbsent(baseName, k -> new ArrayList<>()).add(deliverable);
					}
				}
			}

			// Second pass: for each group of deliverables with the same base name,
			// find the document reference from the URL deliverable
			for (Map.Entry<String, List<DeliverableListDataItem>> entry : deliverablesByBaseName.entrySet()) {
				String baseName = entry.getKey();
				List<DeliverableListDataItem> deliverables = entry.getValue();

				// Find the URL deliverable in this group
				DeliverableListDataItem urlDeliverable = deliverables.stream()
					.filter(d -> d.getName() != null && d.getName().endsWith(DELIVERABLE_SUFFIX_URL))
					.findFirst()
					.orElse(null);

				if (urlDeliverable != null && urlDeliverable.getContent() != null) {
					NodeRef documentRef = urlDeliverable.getContent();

					// Add all deliverables from this group to the document
					deliverableByDocuments.computeIfAbsent(documentRef, k -> new ArrayList<>()).addAll(deliverables);

					if (logger.isDebugEnabled()) {
						logger.debug("Added " + deliverables.size() + " deliverables for document " + documentRef + " (base name: " + baseName + ")");
						for (DeliverableListDataItem d : deliverables) {
							logger.debug("  - " + d.getName());
						}
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("No URL deliverable found or no content for base name: " + baseName);
					}
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Project has no deliverable list");
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Found " + deliverableByDocuments.size() + " documents with deliverables");
			for (Map.Entry<NodeRef, List<DeliverableListDataItem>> entry : deliverableByDocuments.entrySet()) {
				logger.debug("Document " + entry.getKey() + " has " + entry.getValue().size() + " deliverables");
			}
		}

		return deliverableByDocuments;
	}

	/**
	 * <p>generateDeliverableName.</p>
	 *
	 * @param docName a {@link java.lang.String} object
	 * @param resourceFirstName a {@link java.lang.String} object
	 * @param resourceLastName a {@link java.lang.String} object
	 * @param suffix a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String generateDeliverableName(String docName, String resourceFirstName, String resourceLastName, String suffix) {
		return String.format("%s%s%s%s", docName, resourceFirstName, resourceLastName, suffix);
	}

	/**
	 * <p>generatePrepareDeliverableName.</p>
	 *
	 * @param docName a {@link java.lang.String} object
	 * @param resourceFirstName a {@link java.lang.String} object
	 * @param resourceLastName a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String generatePrepareDeliverableName(String docName, String resourceFirstName, String resourceLastName) {
		return generateDeliverableName(docName, resourceFirstName, resourceLastName, DELIVERABLE_SUFFIX_PREPARE);
	}

	/**
	 * <p>generateUrlDeliverableName.</p>
	 *
	 * @param docName a {@link java.lang.String} object
	 * @param resourceFirstName a {@link java.lang.String} object
	 * @param resourceLastName a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String generateUrlDeliverableName(String docName, String resourceFirstName, String resourceLastName) {
		return generateDeliverableName(docName, resourceFirstName, resourceLastName, DELIVERABLE_SUFFIX_URL);
	}

	/**
	 * <p>generateSignDeliverableName.</p>
	 *
	 * @param docName a {@link java.lang.String} object
	 * @param resourceFirstName a {@link java.lang.String} object
	 * @param resourceLastName a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String generateSignDeliverableName(String docName, String resourceFirstName, String resourceLastName) {
		return generateDeliverableName(docName, resourceFirstName, resourceLastName, DELIVERABLE_SUFFIX_SIGN);
	}

	/**
	 * <p>generateValidateDeliverableName.</p>
	 *
	 * @param docName a {@link java.lang.String} object
	 * @param resourceFirstName a {@link java.lang.String} object
	 * @param resourceLastName a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String generateValidateDeliverableName(String docName, String resourceFirstName, String resourceLastName) {
		return generateDeliverableName(docName, resourceFirstName, resourceLastName, DELIVERABLE_SUFFIX_VALIDATE);
	}

	/**
	 * <p>generateRejectDeliverableName.</p>
	 *
	 * @param docName a {@link java.lang.String} object
	 * @param resourceFirstName a {@link java.lang.String} object
	 * @param resourceLastName a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String generateRejectDeliverableName(String docName, String resourceFirstName, String resourceLastName) {
		return generateDeliverableName(docName, resourceFirstName, resourceLastName, DELIVERABLE_SUFFIX_REJECT);
	}

	/**
	 * <p>generateValidateDocDeliverableName.</p>
	 *
	 * @param docName a {@link java.lang.String} object
	 * @param resourceFirstName a {@link java.lang.String} object
	 * @param resourceLastName a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String generateValidateDocDeliverableName(String docName, String resourceFirstName, String resourceLastName) {
		return generateDeliverableName(docName, resourceFirstName, resourceLastName, DELIVERABLE_SUFFIX_VALIDATE_DOC);
	}

	/**
	 * <p>generateRejectDocDeliverableName.</p>
	 *
	 * @param docName a {@link java.lang.String} object
	 * @param resourceFirstName a {@link java.lang.String} object
	 * @param resourceLastName a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String generateRejectDocDeliverableName(String docName, String resourceFirstName, String resourceLastName) {
		return generateDeliverableName(docName, resourceFirstName, resourceLastName, DELIVERABLE_SUFFIX_REJECT_DOC);
	}
	
	/**
	 * <p>findUrlDeliverable.</p>
	 *
	 * @param project a {@link fr.becpg.repo.project.data.ProjectData} object
	 * @param signDeliverable a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object
	 * @return a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object
	 */
	public static DeliverableListDataItem findUrlDeliverable(ProjectData project, DeliverableListDataItem signDeliverable) {
		String urlDeliverableName = signDeliverable.getName().replace(DELIVERABLE_SUFFIX_SIGN, "").replace(DELIVERABLE_SUFFIX_PREPARE, "")
				+ DELIVERABLE_SUFFIX_URL;
		for (DeliverableListDataItem deliverable : project.getDeliverableList()) {
			if (deliverable.getContent() != null && urlDeliverableName.equals(deliverable.getName())) {
				return deliverable;
			}
		}
		return null;
	}
	
	/**
	 * <p>findDocDeliverable.</p>
	 *
	 * @param project a {@link fr.becpg.repo.project.data.ProjectData} object
	 * @param taskDeliverable a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object
	 * @return a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object
	 */
	public static DeliverableListDataItem findDocDeliverable(ProjectData project, DeliverableListDataItem taskDeliverable) {
		for (DeliverableListDataItem deliverable : project.getDeliverableList()) {
			if (deliverable.getContent() != null && deliverable.getName().startsWith(taskDeliverable.getName()) && deliverable.getName().endsWith(DELIVERABLE_SUFFIX_DOC)) {
				return deliverable;
			}
		}
		return null;
	}
}
