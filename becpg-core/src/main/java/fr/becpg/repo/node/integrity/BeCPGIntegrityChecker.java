package fr.becpg.repo.node.integrity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.node.integrity.IntegrityRecord;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AttributeExtractorService;

/**
 * <p>BeCPGIntegrityChecker class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGIntegrityChecker extends IntegrityChecker {

	private static final String MSG_INTEGRITY_MULTIPLICITY = "integrity-checker.association-multiplicity-error";
	private static final String MSG_INTEGRITY_CONSTRAINT = "integrity-checker.constraint-error";

	/** Constant <code>targetNodeRefRegex</code> */
	public static final  Pattern targetNodeRefRegex = Pattern.compile("   Source Node: (.*)", Pattern.MULTILINE);

	private EntityDictionaryService entityDictionaryService;

	private AttributeExtractorService attributeExtractorService;

	private EntityListDAO entityListDAO;

	private NodeService nodeService;

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>attributeExtractorService</code>.</p>
	 *
	 * @param attributeExtractorService a {@link fr.becpg.repo.helper.AttributeExtractorService} object.
	 */
	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/** {@inheritDoc} */
	@Override
	public void setNodeService(NodeService nodeService) {
		// Important do not remove
		super.setNodeService(nodeService);
		this.nodeService = nodeService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Runs several types of checks, querying specifically for events that will
	 * necessitate each type of test.
	 * <p>
	 * The interface contracts also requires that all events for the transaction
	 * get cleaned up.
	 */
	@Override
	public void checkIntegrity() throws IntegrityException {
		try {
			super.checkIntegrity();
		} catch (IntegrityException e) {

			List<IntegrityRecord> records = e.getRecords();
			List<IntegrityRecord> newRecords = new ArrayList<>();

			StringBuilder sb = new StringBuilder();
			boolean isConstraint = false;
			boolean isMultiplicity = false;
			for (IntegrityRecord integrityRecord : records) {
				if (integrityRecord.getMessage().contains("Constraint:")) {
					isConstraint = true;
				} else if(integrityRecord.getMessage().contains("The association target multiplicity has been violated")) {
					isMultiplicity = true;
				}

				String message = decorate(integrityRecord.getMessage());
				newRecords.add(new IntegrityRecord(message));
				sb.append("\n").append(message);

			}

			if (isConstraint) {
				throw new IntegrityException(I18NUtil.getMessage(MSG_INTEGRITY_CONSTRAINT, sb.toString()), newRecords);
			} else if(isMultiplicity) {
				throw new IntegrityException(I18NUtil.getMessage(MSG_INTEGRITY_MULTIPLICITY, sb.toString()), newRecords);
			}

			throw new IntegrityException(sb.toString(), newRecords);
		}
	}

	/*
	 * Error templates
	 *
	 * Invalid property value: Node:
	 * workspace://SpacesStore/74d8e8af-df16-4a30-8666-59211ff4a2cc Name:
	 * 13d87abc-36a8-4fbd-9152-ecb85bb05e66 Type:
	 * {http://www.bcpg.fr/model/becpg/1.0}nutList Property:
	 * {http://www.bcpg.fr/model/becpg/1.0}nutListGroup Constraint: 08030010 The
	 * value is not an allowed value: Autres nutriments
	 *
	 * The association target multiplicity has been violated: Source Node:
	 * workspace://SpacesStore/278cf1e9-aaff-40c4-a2ae-cbb14f5f7a99 Association:
	 * Association[
	 * class=ClassDef[name={http://www.bcpg.fr/model/becpg/1.0}compoList],
	 * name={http://www.bcpg.fr/model/becpg/1.0}compoListProduct, target
	 * class={http://www.bcpg.fr/model/becpg/1.0}product, source role=null,
	 * target role=null] Required target Multiplicity: 1..1 Actual target
	 * Multiplicity: 0
	 *
	 *
	 */

	private String decorate(String message) {

		if (message.contains("The association target multiplicity has been violated")) {

			Matcher ma = targetNodeRefRegex.matcher(message);
			if (ma.find()) {
				NodeRef sourceNodeRef = new NodeRef(ma.group(1));
				if (nodeService.exists(sourceNodeRef)) {
					if (entityDictionaryService.isSubClass(nodeService.getType(sourceNodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
						sourceNodeRef = entityListDAO.getEntity(sourceNodeRef);
					}
					String ret =  attributeExtractorService.extractPropName(sourceNodeRef) ;
					String code = (String) nodeService.getProperty(sourceNodeRef, BeCPGModel.PROP_CODE);
					if(code!=null && !code.isEmpty()) {
						ret = code +" - "+ret;
					} else {
						ret = ret + " ("+sourceNodeRef+")";
					}
					return ret;
				}
			}
		}

		return message;
	}
}
