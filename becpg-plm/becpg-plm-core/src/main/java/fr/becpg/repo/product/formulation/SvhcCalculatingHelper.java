/*
 *
 */
package fr.becpg.repo.product.formulation;

import fr.becpg.repo.product.data.productList.SvhcListDataItem;

/**
 * <p>SvhcCalculatingHelper class.</p>
 *
 * @author matthieu
 */
public class SvhcCalculatingHelper {

	/**
	 * <p>extractPackagingValue.</p>
	 *
	 * @param svhcListDataItem a {@link fr.becpg.repo.product.data.productList.SvhcListDataItem} object
	 * @return a {@link java.lang.Double} object
	 */
	public static Double extractPackagingValue(SvhcListDataItem svhcListDataItem) {
		Double migrationPerc = svhcListDataItem.getMigrationPerc();
		if (migrationPerc == null || migrationPerc == 0d) {
			return null;
		}
		if (svhcListDataItem.getQtyPerc() != null) {
			return migrationPerc * svhcListDataItem.getQtyPerc() / 100d;
		}
		return null;
	}

}
