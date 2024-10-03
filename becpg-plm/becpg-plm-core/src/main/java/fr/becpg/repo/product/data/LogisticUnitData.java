package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

/**
 * <p>LogisticUnitData class.</p>
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:logisticUnit")
public class LogisticUnitData extends ProductData {
	
	private Double secondaryWidth;
	private Double tertiaryWidth;
	
	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.LogisticUnitData} object
	 */
	public static LogisticUnitData build() {
		return new LogisticUnitData();
	}

	/**
	 * <p>withName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.LogisticUnitData} object
	 */
	public LogisticUnitData withName(String name) {
		setName(name);
		return this;
	}
	
	/**
	 * <p>withSecondaryWidth.</p>
	 *
	 * @param secondaryWidth a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.LogisticUnitData} object
	 */
	public LogisticUnitData withSecondaryWidth(Double secondaryWidth) {
		setSecondaryWidth(secondaryWidth);
		return this;
	}
	
	/**
	 * <p>withTertiaryWidth.</p>
	 *
	 * @param tertiaryWidth a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.LogisticUnitData} object
	 */
	public LogisticUnitData withTertiaryWidth(Double tertiaryWidth) {
		setTertiaryWidth(tertiaryWidth);
		return this;
	}
	
	/**
	 * <p>withCompoList.</p>
	 *
	 * @param compoList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.LogisticUnitData} object
	 */
	public LogisticUnitData withCompoList(List<CompoListDataItem> compoList) {
		getCompoListView().setCompoList(compoList);
		return this;
	}
	
	/**
	 * <p>withPackagingList.</p>
	 *
	 * @param packagingList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.LogisticUnitData} object
	 */
	public LogisticUnitData withPackagingList(List<PackagingListDataItem> packagingList) {
		getPackagingListView().setPackagingList(packagingList);
		return this;
	}
	
	/**
	 * <p>Getter for the field <code>tertiaryWidth</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "gs1:tertiaryWidth")
	public Double getTertiaryWidth() {
		return tertiaryWidth;
	}
	
	/**
	 * <p>Setter for the field <code>tertiaryWidth</code>.</p>
	 *
	 * @param tertiaryWidth a {@link java.lang.Double} object.
	 */
	public void setTertiaryWidth(Double tertiaryWidth) {
		this.tertiaryWidth = tertiaryWidth;
	}
	
	/**
	 * <p>Getter for the field <code>secondaryWidth</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "gs1:secondaryWidth")
	public Double getSecondaryWidth() {
		return secondaryWidth;
	}
	
	/**
	 * <p>Setter for the field <code>secondaryWidth</code>.</p>
	 *
	 * @param secondaryWidth a {@link java.lang.Double} object.
	 */
	public void setSecondaryWidth(Double secondaryWidth) {
		this.secondaryWidth = secondaryWidth;
	}
}
