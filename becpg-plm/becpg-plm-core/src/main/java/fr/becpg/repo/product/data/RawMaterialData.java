/*
 *
 */
package fr.becpg.repo.product.data;

import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.MultiLevelLeaf;

/**
 * <p>RawMaterialData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:rawMaterial")
@MultiLevelLeaf
@AlfCacheable
public class RawMaterialData extends ProductData {

	private static final long serialVersionUID = -2176815295417841030L;

	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.RawMaterialData} object
	 */
	public static RawMaterialData build() {
		return new RawMaterialData();
	}

	/**
	 * <p>withName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.RawMaterialData} object
	 */
	public RawMaterialData withName(String name) {
		setName(name);
		return this;
	}

	/**
	 * <p>withNetWeight.</p>
	 *
	 * @param netWeight a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.RawMaterialData} object
	 */
	public RawMaterialData withNetWeight(Double netWeight) {
		setNetWeight(netWeight);
		return this;
	}

	/**
	 * <p>withLegalName.</p>
	 *
	 * @param legalName a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.RawMaterialData} object
	 */
	public RawMaterialData withLegalName(String legalName) {
		setLegalName(legalName);
		return this;
	}

	/**
	 * <p>withLegalName.</p>
	 *
	 * @param legalName a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @return a {@link fr.becpg.repo.product.data.RawMaterialData} object
	 */
	public RawMaterialData withLegalName(MLText legalName) {
		setLegalName(legalName);
		return this;
	}

	/**
	 * <p>withUnit.</p>
	 *
	 * @param unit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object
	 * @return a {@link fr.becpg.repo.product.data.RawMaterialData} object
	 */
	public RawMaterialData withUnit(ProductUnit unit) {
		setUnit(unit);
		return this;
	}

	/**
	 * <p>withQty.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.RawMaterialData} object
	 */
	public RawMaterialData withQty(Double qty) {
		setQty(qty);
		return this;
	}

	/**
	 * <p>withDensity.</p>
	 *
	 * @param density a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.RawMaterialData} object
	 */
	public RawMaterialData withDensity(Double density) {
		setDensity(density);
		return this;
	}

	public RawMaterialData withPlants(List<NodeRef> plants) {
		setPlants(plants);
		return this;
	}

	/**
	 * <p>withCompoList.</p>
	 *
	 * @param compoList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public RawMaterialData withCompoList(List<CompoListDataItem> compoList) {
		getCompoListView().setCompoList(compoList);
		return this;
	}
	

}
