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
package fr.becpg.repo.project.data.projectList;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Planning list (done or to do)
 *
 * @author quere
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "pjt:budgetList")
public class BudgetListDataItem extends BeCPGDataObject implements CompositeDataItem<BudgetListDataItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4337355975343249214L;
	private String item;
	private Double budgetedExpense;
	private Double budgetedInvoice;
	private Double expense;
	private Double invoice;
	private Double profit;
	
	private BudgetListDataItem parent;
	private Integer depthLevel;
	

	/**
	 * <p>Constructor for BudgetListDataItem.</p>
	 */
	public BudgetListDataItem (){
		super();
	}
	
	/**
	 * <p>Constructor for BudgetListDataItem.</p>
	 *
	 * @param item a {@link java.lang.String} object.
	 * @param budgetedExpense a {@link java.lang.Double} object.
	 * @param budgetedInvoice a {@link java.lang.Double} object.
	 * @param expense a {@link java.lang.Double} object.
	 * @param invoice a {@link java.lang.Double} object.
	 * @param profit a {@link java.lang.Double} object.
	 */
	public BudgetListDataItem (String item, Double budgetedExpense, Double budgetedInvoice, Double expense, Double invoice, Double profit){
		this.item = item;
		this.budgetedExpense= budgetedExpense;
		this.budgetedInvoice= budgetedInvoice;
		this.expense = expense;
		this.invoice = invoice;
		this.profit = profit;
	}
	
	
	/**
	 * <p>Getter for the field <code>item</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:blItem")
	public String getItem() {
		return item;
	}

	/**
	 * <p>Setter for the field <code>item</code>.</p>
	 *
	 * @param item a {@link java.lang.String} object.
	 */
	public void setItem(String item) {
		this.item = item;
	}
	
	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	/**
	 * <p>Setter for the field <code>depthLevel</code>.</p>
	 *
	 * @param depthLevel a {@link java.lang.Integer} object.
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	public BudgetListDataItem getParent() {
		return this.parent;
	}

	/**
	 * <p>Setter for the field <code>parent</code>.</p>
	 *
	 * @param parent a {@link fr.becpg.repo.project.data.projectList.BudgetListDataItem} object.
	 */
	public void setParent(BudgetListDataItem parent) {
		this.parent = parent;
	}
	
	/**
	 * <p>Getter for the field <code>budgetedExpense</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:blBudgetedExpense")
	public Double getBudgetedExpense() {
		return budgetedExpense;
	}

	/**
	 * <p>Setter for the field <code>budgetedExpense</code>.</p>
	 *
	 * @param budgetedExpense a {@link java.lang.Double} object.
	 */
	public void setBudgetedExpense(Double budgetedExpense) {
		this.budgetedExpense = budgetedExpense;
	}
	
	/**
	 * <p>Getter for the field <code>budgetedInvoice</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:blBudgetedInvoice")
	public Double getBudgetedInvoice() {
		return budgetedInvoice;
	}
	
	/**
	 * <p>Setter for the field <code>budgetedInvoice</code>.</p>
	 *
	 * @param budgetedInvoice a {@link java.lang.Double} object.
	 */
	public void setBudgetedInvoice(Double budgetedInvoice) {
		this.budgetedInvoice = budgetedInvoice;
	}	

	/**
	 * <p>Getter for the field <code>expense</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:expense")
	public Double getExpense() {
		return expense;
	}

	/**
	 * <p>Setter for the field <code>expense</code>.</p>
	 *
	 * @param actualExpense a {@link java.lang.Double} object.
	 */
	public void setExpense(Double actualExpense) {
		this.expense = actualExpense;
	}
	/**
	 * <p>Getter for the field <code>invoice</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:invoice")
	public Double getInvoice() {
		return invoice;
	}

	/**
	 * <p>Setter for the field <code>invoice</code>.</p>
	 *
	 * @param actualInvoice a {@link java.lang.Double} object.
	 */
	public void setInvoice(Double actualInvoice) {
		this.invoice = actualInvoice;
	}
	/**
	 * <p>Getter for the field <code>profit</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:blProfit")
	public Double getProfit() {
		return profit;
	}

	/**
	 * <p>Setter for the field <code>profit</code>.</p>
	 *
	 * @param profit a {@link java.lang.Double} object.
	 */
	public void setProfit(Double profit) {
		this.profit = profit;
	}
	
	

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expense == null) ? 0 : expense.hashCode());
		result = prime * result + ((invoice == null) ? 0 : invoice.hashCode());
		result = prime * result + ((budgetedExpense == null) ? 0 : budgetedExpense.hashCode());
		result = prime * result + ((budgetedInvoice == null) ? 0 : budgetedInvoice.hashCode());
		result = prime * result + ((depthLevel == null) ? 0 : depthLevel.hashCode());
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((profit == null) ? 0 : profit.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BudgetListDataItem other = (BudgetListDataItem) obj;
		if (expense == null) {
			if (other.expense != null)
				return false;
		} else if (!expense.equals(other.expense))
			return false;
		if (invoice == null) {
			if (other.invoice != null)
				return false;
		} else if (!invoice.equals(other.invoice))
			return false;
		if (budgetedExpense == null) {
			if (other.budgetedExpense != null)
				return false;
		} else if (!budgetedExpense.equals(other.budgetedExpense))
			return false;
		if (budgetedInvoice == null) {
			if (other.budgetedInvoice != null)
				return false;
		} else if (!budgetedInvoice.equals(other.budgetedInvoice))
			return false;
		if (depthLevel == null) {
			if (other.depthLevel != null)
				return false;
		} else if (!depthLevel.equals(other.depthLevel))
			return false;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.equals(other.item))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (profit == null) {
			if (other.profit != null)
				return false;
		} else if (!profit.equals(other.profit))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "BudgetListDataItem [item=" + item + ", budgetedExpense=" + budgetedExpense + ", budgetedInvoice=" + budgetedInvoice + ", actualExpense=" + expense + ", actualInvoice="
				+ invoice + ", profit=" + profit + ", parent=" + parent + ", depthLevel=" + depthLevel + "]";
	}

	
	
	

}
