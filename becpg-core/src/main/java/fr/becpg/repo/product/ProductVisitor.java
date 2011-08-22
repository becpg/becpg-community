/*
 * 
 */
package fr.becpg.repo.product;

import fr.becpg.repo.product.data.ProductData;

/**
 * The Interface ProductVisitor.
 *
 * @author querephi
 */
public interface ProductVisitor {

//	public FinishedProductData visit(FinishedProductData finishedProductData);
//	public RawMaterialData visit(RawMaterialData rawMaterialData);
//	public PackagingMaterialData visit(PackagingMaterialData packagingMaterialData);
//	public SemiFinishedProductData visit(SemiFinishedProductData semiFinishedProductData);	
//	public LocalSemiFinishedProduct visit(LocalSemiFinishedProduct localSemiFinishedProductData);
	/**
 * Visit.
 *
 * @param productData the product data
 * @return the product data
 */
public ProductData visit(ProductData productData);
}
