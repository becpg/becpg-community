package fr.becpg.repo.product.formulation.spel;

import fr.becpg.repo.product.data.ProductData;

public interface CustomSpelFunctions {

	boolean match(String beanName);

	Object create(ProductData productData);

}
