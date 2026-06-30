package fr.becpg.repo.regulatory;

import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import org.alfresco.service.cmr.repository.MLText;
import org.springframework.extensions.surf.util.I18NUtil;

import java.util.List;

/**
 * <p>RegulatoryHelper class.</p>
 *
 * @author matthieu
 */
public class RegulatoryHelper {
	/** Constant <code>DECERNIS_PREFIX="DECERNIS_"</code> */
	public static final String DECERNIS_PREFIX = "DECERNIS_";

	/**
	 * <p>Constructor for RegulatoryHelper.</p>
	 */
	private RegulatoryHelper() {
		// Private constructor to prevent instantiation
	}
	
	/**
	 * <p>extractIngTypes.</p>
	 *
	 * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @return a {@link java.util.List} object
	 */
	public static List<IngTypeItem> extractIngTypes(IngListDataItem ingListDataItem, AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		if (ingListDataItem.getIngTypes() != null && !ingListDataItem.getIngTypes().isEmpty()) {
			return ingListDataItem.getIngTypes().stream()
					.map(ingTypeRef -> (IngTypeItem) alfrescoRepository.findOne(ingTypeRef))
					.toList();
		}
		IngItem ingItem = (IngItem) alfrescoRepository.findOne(ingListDataItem.getIng());
		if (ingItem != null && ingItem.getIngType() != null) {
			return List.of(ingItem.getIngType());
		}
		return List.of();
	}

	/**
	 * <p>extractIngName.</p>
	 *
	 * @param ingItem a {@link fr.becpg.repo.product.data.ing.IngItem} object
	 * @return a {@link java.lang.String} object
	 */
	public static String extractIngName(IngItem ingItem) {
		MLText mlTextLegalName = ingItem.getLegalName();
		String legalName = mlTextLegalName != null ? mlTextLegalName.getClosestValue(I18NUtil.getContentLocale()) : null;
		return legalName != null && !legalName.isBlank() ? legalName : ingItem.getCharactName();
	}

	/**
	 * Keeps only the leading beCPG code of a requirement {@code <country> - <usage>}
	 * code. A usage may carry both its beCPG code and the Decernis phrase
	 * ({@code COSMETIC_BODY_CREAM_LOTION,DECERNIS_Body Cream/Lotion}); only the beCPG code is kept
	 * for display and filtering. A code with no beCPG part (food,
	 * {@code DECERNIS_<name>}) is returned unchanged as the only available token.
	 *
	 * @param regulatoryCode the raw requirement code, possibly null
	 * @return the shortened code, or null when the input is null
	 */
	public static String shortenRegulatoryCode(String regulatoryCode) {
		if (regulatoryCode == null) {
			return null;
		}
		return regulatoryCode.split(",(?=" + DECERNIS_PREFIX + ")")[0].trim();
	}
}
