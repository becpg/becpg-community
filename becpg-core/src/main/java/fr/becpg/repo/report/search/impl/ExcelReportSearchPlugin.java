package fr.becpg.repo.report.search.impl;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.poi.ss.usermodel.Sheet;

import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>ExcelReportSearchPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ExcelReportSearchPlugin {

	/** Constant <code>TOKEN_INCLUDE_EMPTY="IncludeEmpty"</code> */
	String TOKEN_INCLUDE_EMPTY = "IncludeEmpty";

	/**
	 * <p>Indicates whether a row should be created for entities having an empty list.</p>
	 *
	 * @param parameters an array of {@link java.lang.String} objects.
	 * @return true if one of the parameters carries the <code>IncludeEmpty</code> token.
	 */
	static boolean isIncludeEmpty(String[] parameters) {
		if (parameters != null) {
			for (String parameter : parameters) {
				if ((parameter != null) && parameter.contains(TOKEN_INCLUDE_EMPTY)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * <p>fillSheet.</p>
	 *
	 * @param sheet a {@link org.apache.poi.ss.usermodel.Sheet} object.
	 * @param searchResults a {@link java.util.List} object.
	 * @param mainType a {@link org.alfresco.service.namespace.QName} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param rownum a int.
	 * @param parameter an array of {@link java.lang.String} objects.
	 * @param keyColumn a {@link fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param cache a {@link java.util.Map} object.
	 * @return a int.
	 */
	int fillSheet(Sheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum, String[] parameter,
			AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache);

	/**
	 * <p>isDefault.</p>
	 *
	 * @return a boolean.
	 */
	boolean isDefault();

	/**
	 * <p>isApplicable.</p>
	 *
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param parameters an array of {@link java.lang.String} objects.
	 * @return a boolean.
	 */
	boolean isApplicable(QName itemType, String[] parameters);

}
