/*
 * 
 */
package fr.becpg.repo.product.data;

import java.util.List;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;


/**
 * <p>SemiFinishedProductData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:semiFinishedProduct")
public class SemiFinishedProductData extends ProductData {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5572375270730148786L;

	
	public static SemiFinishedProductData build() {
		return new SemiFinishedProductData();
	}

	public SemiFinishedProductData withName(String name) {
		setName(name);
		return this;
	}

	public SemiFinishedProductData withLegalName(String legalName) {
		setLegalName(legalName);
		return this;
	}
	
	public SemiFinishedProductData withLegalName(MLText legalName) {
		setLegalName(legalName);
		return this;
	}

	public SemiFinishedProductData withUnit(ProductUnit unit) {
		setUnit(unit);
		return this;
	}

	public SemiFinishedProductData withQty(Double qty) {
		setQty(qty);
		return this;
	}

	public SemiFinishedProductData withDensity(Double density) {
		setDensity(density);
		return this;
	}

	public SemiFinishedProductData withCompoList(List<CompoListDataItem> compoList) {
		getCompoListView().setCompoList(compoList);
		return this;
	}

	public SemiFinishedProductData withPackagingList(List<PackagingListDataItem> packagingList) {
		getPackagingListView().setPackagingList(packagingList);
		return this;
	}


}
