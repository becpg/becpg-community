package fr.becpg.repo.form.column.decorator;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>
 * DynamicColumnNameResolver class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DynamicColumnNameResolver implements ExcelFieldTitleProvider, DataGridFormFieldTitleProvider {

	private DictionaryService dictionaryService;

	Map<String, String> dynamicColumnNames = new HashMap<>();

	/**
	 * <p>
	 * Constructor for DynamicColumnNameResolver.
	 * </p>
	 *
	 * @param filter
	 *            a {@link fr.becpg.repo.entity.datalist.data.DataListFilter}
	 *            object.
	 * @param nodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 * @param dictionaryService
	 *            a
	 *            {@link org.alfresco.service.cmr.dictionary.DictionaryService}
	 *            object.
	 */
	public DynamicColumnNameResolver(DataListFilter filter, NodeService nodeService, DictionaryService dictionaryService) {

		this.dictionaryService = dictionaryService;
		if (filter.getParentNodeRef() != null) {
			for (NodeRef nodeRef : BeCPGQueryBuilder.createQuery().parent(filter.getParentNodeRef()).ofType(PLMModel.TYPE_DYNAMICCHARACTLIST)
					.isNotNull(PLMModel.PROP_DYNAMICCHARACT_COLUMN).inDB().list()) {

				String title = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
				if (title == null || title.isBlank()) {
					title = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_DYNAMICCHARACT_TITLE);
				}

				dynamicColumnNames.put(((String) nodeService.getProperty(nodeRef, PLMModel.PROP_DYNAMICCHARACT_COLUMN)).replace("bcpg_", ""), title);

			}

		}

	}

	/** {@inheritDoc} */
	@Override
	public String getTitle(AttributeExtractorStructure field) {
		String title = field.getFieldLabel();
		title = (title != null && !title.isBlank()) ? title : getTitle(field.getFieldDef().getName());
		return (title != null) ? title : field.getFieldDef().getTitle(dictionaryService);
	}

	/** {@inheritDoc} */
	@Override
	public String getTitle(QName field) {
		String fieldName = field.getLocalName().replace("bcpg:", "");
		if (dynamicColumnNames.containsKey(fieldName)) {
			return dynamicColumnNames.get(fieldName);
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAllowed(AttributeExtractorStructure field) {
		return isAllowed(field.getFieldDef().getName());
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAllowed(QName field) {
		String fieldName = field.getLocalName().replace("bcpg:", "");
		if (fieldName.contains("dynamicCharactColumn")) {
			if (!dynamicColumnNames.containsKey(fieldName)) {
				return false;
			}
		} else if (PLMModel.PROP_COMPARE_WITH_DYN_COLUMN.equals(field) || ForumModel.PROP_COMMENT_COUNT.equals(field)) {
			return false;
		}
		return true;
	}

}
