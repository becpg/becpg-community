/*
 *
 */
package fr.becpg.repo.product.formulation;

import fr.becpg.repo.product.data.productList.SvhcListDataItem;

public class SvhcCalculatingHelper {

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