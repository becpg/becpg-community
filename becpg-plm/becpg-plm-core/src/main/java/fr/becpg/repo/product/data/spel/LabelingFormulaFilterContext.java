/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.data.spel;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.formulation.spel.DataListItemSpelContext;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ing.IngTypeItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>DeclarationFilterContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LabelingFormulaFilterContext extends DataListItemSpelContext<ProductData> {

	private CompoListDataItem compoListDataItem;
	private IngListDataItem ingListDataItem;
	private IngTypeItem ingTypeItem;

	/**
	 * <p>Constructor for DeclarationFilterContext.</p>
	 *
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object.
	 * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object.
	 * @param formulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object
	 */
	public LabelingFormulaFilterContext(SpelFormulaService formulaService, CompoListDataItem compoListDataItem, IngListDataItem ingListDataItem) {
		super(formulaService);
		this.setDataListItem(compoListDataItem);
		this.compoListDataItem = compoListDataItem;
		this.ingListDataItem = ingListDataItem;
	}

	/**
	 * <p>Constructor for DeclarationFilterContext.</p>
	 *
	 * @param formulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object
	 * @param ingTypeItem a {@link fr.becpg.repo.product.data.ing.IngTypeItem} object
	 */
	public LabelingFormulaFilterContext(SpelFormulaService formulaService, IngTypeItem ingTypeItem) {
		super(formulaService);
		this.ingTypeItem = ingTypeItem;
	}

	/**
	 * <p>Getter for the field <code>compoListDataItem</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object.
	 */
	public CompoListDataItem getCompoListDataItem() {
		return compoListDataItem;
	}

	/**
	 * <p>Getter for the field <code>ingListDataItem</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object.
	 */
	public IngListDataItem getIngListDataItem() {
		return ingListDataItem;
	}

	/**
	 * <p>Getter for the field <code>ingTypeItem</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ing.IngTypeItem} object
	 */
	public IngTypeItem getIngTypeItem() {
		return ingTypeItem;
	}
	
	/**
	 * Spel Helper do not remove
	 *
	 * @param claimNodeRefStr a {@link java.lang.String} object
	 * @return Check for claim on ingredient or product
	 */
	public boolean isClaimed(String claimNodeRefStr) {
		return isClaimed(new NodeRef(claimNodeRefStr));
	}

	/**
	 * <p>isClaimed.</p>
	 *
	 * @param claimNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	public boolean isClaimed(NodeRef claimNodeRef) {
		if (ingListDataItem != null) {
			return (ingListDataItem.getClaims() != null) && ingListDataItem.getClaims().contains(claimNodeRef);
		}

		RepositoryEntity entity = getDataListItemEntity();

		if (entity instanceof ProductData) {
			for (LabelClaimListDataItem claim : ((ProductData) entity).getLabelClaimList()) {
				if (claim.getLabelClaim().equals(claimNodeRef)) {
					return claim.getIsClaimed();
				}
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "DeclarationFilterContext [compoListDataItem=" + compoListDataItem + ", ingListDataItem=" + ingListDataItem + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(compoListDataItem, ingListDataItem, ingTypeItem);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		LabelingFormulaFilterContext other = (LabelingFormulaFilterContext) obj;
		return Objects.equals(compoListDataItem, other.compoListDataItem) && Objects.equals(ingListDataItem, other.ingListDataItem)
				&& Objects.equals(ingTypeItem, other.ingTypeItem);
	}

}
