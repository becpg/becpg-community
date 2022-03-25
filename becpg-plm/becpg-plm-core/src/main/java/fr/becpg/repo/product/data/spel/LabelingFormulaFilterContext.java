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

import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>DeclarationFilterContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LabelingFormulaFilterContext {

	private CompoListDataItem compoListDataItem;
	private IngListDataItem ingListDataItem;
	private SpelFormulaService formulaService;

	/**
	 * <p>Constructor for DeclarationFilterContext.</p>
	 *
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object.
	 * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object.
	 */
	public LabelingFormulaFilterContext(SpelFormulaService formulaService, CompoListDataItem compoListDataItem, IngListDataItem ingListDataItem) {
		super();
		this.compoListDataItem = compoListDataItem;
		this.ingListDataItem = ingListDataItem;
		this.formulaService = formulaService;
	}

	/**
	 * <p>Constructor for DeclarationFilterContext.</p>
	 */
	public LabelingFormulaFilterContext() {
		super();
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
	 * <p>getDataListItemEntity.</p>
	 *
	 * @return a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 */
	public RepositoryEntity getDataListItemEntity() {
		return compoListDataItem.getComponent() != null ? formulaService.findOne(compoListDataItem.getComponent()) : null;

	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "DeclarationFilterContext [compoListDataItem=" + compoListDataItem + ", ingListDataItem=" + ingListDataItem + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(compoListDataItem, formulaService, ingListDataItem);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		LabelingFormulaFilterContext other = (LabelingFormulaFilterContext) obj;
		return Objects.equals(compoListDataItem, other.compoListDataItem) && Objects.equals(formulaService, other.formulaService)
				&& Objects.equals(ingListDataItem, other.ingListDataItem);
	}

}
