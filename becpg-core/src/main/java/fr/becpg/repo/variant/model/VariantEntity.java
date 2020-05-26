package fr.becpg.repo.variant.model;

import java.util.List;

public interface VariantEntity {

	
	VariantData getDefaultVariantData();
	List<VariantData> getVariants();
	
}
