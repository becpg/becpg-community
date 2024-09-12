package fr.becpg.repo.product.formulation;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.variant.model.VariantData;

/**
 * <p>SimpleListQtyProvider interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface SimpleListQtyProvider {

	/**
	 * <p>getQty.</p>
	 *
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 * @param parentLossRatio a {@link java.lang.Double} object
	 * @param componentProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.lang.Double} object
	 */
	Double getQty(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct);

	/**
	 * <p>getVolume.</p>
	 *
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 * @param parentLossRatio a {@link java.lang.Double} object
	 * @param componentProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.lang.Double} object
	 */
	Double getVolume(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct);
	

	/**
	 * <p>getQty.</p>
	 *
	 * @param packagingListDataItem a {@link fr.becpg.repo.product.data.productList.PackagingListDataItem} object
	 * @param componentProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.lang.Double} object
	 */
	Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct);

	/**
	 * <p>getQty.</p>
	 *
	 * @param processListDataItem a {@link fr.becpg.repo.product.data.productList.ProcessListDataItem} object
	 * @param variant a {@link fr.becpg.repo.variant.model.VariantData} object
	 * @return a {@link java.lang.Double} object
	 */
	Double getQty(ProcessListDataItem processListDataItem, VariantData variant);


	/**
	 * <p>getNetWeight.</p>
	 *
	 * @param variant a {@link fr.becpg.repo.variant.model.VariantData} object
	 * @return a {@link java.lang.Double} object
	 */
	Double getNetWeight(VariantData variant);

	/**
	 * <p>getNetQty.</p>
	 *
	 * @param variant a {@link fr.becpg.repo.variant.model.VariantData} object
	 * @return a {@link java.lang.Double} object
	 */
	Double getNetQty(VariantData variant);

	/**
	 * <p>omitElement.</p>
	 *
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object
	 * @return a {@link java.lang.Boolean} object
	 */
	Boolean omitElement(CompoListDataItem compoListDataItem);
}
