/*
 * 
 */
package fr.becpg.repo.product.data;

import java.util.List;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
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

	
	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.SemiFinishedProductData} object
	 */
	public static SemiFinishedProductData build() {
		return new SemiFinishedProductData();
	}

	/**
	 * <p>withName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.SemiFinishedProductData} object
	 */
	public SemiFinishedProductData withName(String name) {
		setName(name);
		return this;
	}

	/**
	 * <p>withLegalName.</p>
	 *
	 * @param legalName a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.SemiFinishedProductData} object
	 */
	public SemiFinishedProductData withLegalName(String legalName) {
		setLegalName(legalName);
		return this;
	}
	
	/**
	 * <p>withLegalName.</p>
	 *
	 * @param legalName a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @return a {@link fr.becpg.repo.product.data.SemiFinishedProductData} object
	 */
	public SemiFinishedProductData withLegalName(MLText legalName) {
		setLegalName(legalName);
		return this;
	}

	/**
	 * <p>withUnit.</p>
	 *
	 * @param unit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object
	 * @return a {@link fr.becpg.repo.product.data.SemiFinishedProductData} object
	 */
	public SemiFinishedProductData withUnit(ProductUnit unit) {
		setUnit(unit);
		return this;
	}

	/**
	 * <p>withQty.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.SemiFinishedProductData} object
	 */
	public SemiFinishedProductData withQty(Double qty) {
		setQty(qty);
		return this;
	}

	/**
	 * <p>withDensity.</p>
	 *
	 * @param density a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.SemiFinishedProductData} object
	 */
	public SemiFinishedProductData withDensity(Double density) {
		setDensity(density);
		return this;
	}

	/**
	 * <p>withCompoList.</p>
	 *
	 * @param compoList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.SemiFinishedProductData} object
	 */
	public SemiFinishedProductData withCompoList(List<CompoListDataItem> compoList) {
		getCompoListView().setCompoList(compoList);
		return this;
	}

	/**
	 * <p>withPackagingList.</p>
	 *
	 * @param packagingList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.SemiFinishedProductData} object
	 */
	public SemiFinishedProductData withPackagingList(List<PackagingListDataItem> packagingList) {
		getPackagingListView().setPackagingList(packagingList);
		return this;
	}

	/**
	 * <p>withLabelingRuleList.</p>
	 *
	 * @param labelingRuleList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.SemiFinishedProductData} object
	 */
	public SemiFinishedProductData withLabelingRuleList(List<LabelingRuleListDataItem> labelingRuleList) {
		getLabelingListView().setLabelingRuleList(labelingRuleList);
		return this;
	}

}
