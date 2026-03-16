package fr.becpg.repo.regulatory;

import java.util.List;

import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

public class RegulatoryHelper {

	private RegulatoryHelper() {
		// Private constructor to prevent instantiation
	}
	
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
}
