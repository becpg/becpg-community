/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
 * 
 */
@AlfType
@AlfQname(qname = "pjt:budgetList")
public class BudgetListDataItem extends BeCPGDataObject implements CompositeDataItem<BudgetListDataItem> {

	private String item;
	private Double budgetedExpense;
	private Double budgetedInvoice;
	private Double expense;
	private Double invoice;
	private Double profit;
	
	private BudgetListDataItem parent;
	private Integer depthLevel;
	

	public BudgetListDataItem (){
		super();
	}
	
	public BudgetListDataItem (String item, Double budgetedExpense, Double budgetedInvoice, Double expense, Double invoice, Double profit){
		this.item = item;
		this.budgetedExpense= budgetedExpense;
		this.budgetedInvoice= budgetedInvoice;
		this.expense = expense;
		this.invoice = invoice;
		this.profit = profit;
	}
	
	
	@AlfProp
	@AlfQname(qname = "pjt:blItem")
	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}
	
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	public BudgetListDataItem getParent() {
		return this.parent;
	}

	public void setParent(BudgetListDataItem parent) {
		this.parent = parent;
	}
	
	@AlfProp
	@AlfQname(qname = "pjt:blBudgetedExpense")
	public Double getBudgetedExpense() {
		return budgetedExpense;
	}

	public void setBudgetedExpense(Double budgetedExpense) {
		this.budgetedExpense = budgetedExpense;
	}
	
	@AlfProp
	@AlfQname(qname = "pjt:blBudgetedInvoice")
	public Double getBudgetedInvoice() {
		return budgetedInvoice;
	}
	
	public void setBudgetedInvoice(Double budgetedInvoice) {
		this.budgetedInvoice = budgetedInvoice;
	}	

	@AlfProp
	@AlfQname(qname = "pjt:expense")
	public Double getExpense() {
		return expense;
	}

	public void setExpense(Double actualExpense) {
		this.expense = actualExpense;
	}
	@AlfProp
	@AlfQname(qname = "pjt:invoice")
	public Double getInvoice() {
		return invoice;
	}

	public void setInvoice(Double actualInvoice) {
		this.invoice = actualInvoice;
	}
	@AlfProp
	@AlfQname(qname = "pjt:blProfit")
	public Double getProfit() {
		return profit;
	}

	public void setProfit(Double profit) {
		this.profit = profit;
	}
	
	

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

	@Override
	public String toString() {
		return "BudgetListDataItem [item=" + item + ", budgetedExpense=" + budgetedExpense + ", budgetedInvoice=" + budgetedInvoice + ", actualExpense=" + expense + ", actualInvoice="
				+ invoice + ", profit=" + profit + ", parent=" + parent + ", depthLevel=" + depthLevel + "]";
	}

	
	
	

}
