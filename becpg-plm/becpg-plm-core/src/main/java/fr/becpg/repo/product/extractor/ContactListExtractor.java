package fr.becpg.repo.product.extractor;

import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>ContactListExtractor class.</p>
 *
 * @author matthieu
 */
public class ContactListExtractor extends SimpleExtractor {

	private static final String UPDATE_SUPPLIER_ACCOUNT = "update-supplier-account";
	private static final String CONTACT_LIST = "contactList";

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> extractJSON(NodeRef nodeRef, List<AttributeExtractorStructure> metadataFields, Map<String, Object> props,
			Map<NodeRef, Map<String, Object>> cache) {
		Map<String, Object> ret = super.extractJSON(nodeRef, metadataFields, props, cache);
		Map<String, Object> permissions = (Map<String, Object>) ret.get(PROP_PERMISSIONS);
		Map<String, Boolean> userAccess = (Map<String, Boolean>) permissions.get(PROP_USERACCESS);
		String email = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_CONTACT_LIST_EMAIL);
		List<NodeRef> contactListAccounts = associationService.getTargetAssocs(nodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
		boolean canUpdate = userAccess.get("edit")
				&& ((email != null && !email.isBlank()
						&& contactListAccounts.stream()
								.noneMatch(a -> email.toLowerCase().equals(nodeService.getProperty(a, ContentModel.PROP_EMAIL))))
				|| ((email == null || email.isBlank()) && !contactListAccounts.isEmpty()));
		userAccess.put(UPDATE_SUPPLIER_ACCOUNT, canUpdate);
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return (dataListFilter.getDataListName() != null) && dataListFilter.getDataListName().equals(CONTACT_LIST);
	}

}
