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

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Planning list (done or to do)
 * 
 * @author quere
 * 
 */
@AlfType
@AlfQname(qname = "pjt:invoiceList")
public class InvoiceListDataItem extends BeCPGDataObject  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1066729680514598898L;
	private BudgetListDataItem budget ; //ilBudgetRef = Item
	private Double invoiceAmount;  //ilInvoiceAmount
	private TaskListDataItem task; //ilTaskRef
	
	@AlfSingleAssoc
	@AlfQname(qname = "pjt:ilBudgetRef")
	public BudgetListDataItem getBudget() {
		return budget;
	}
	public void setBudget(BudgetListDataItem budget) {
		this.budget = budget;
	}

	@AlfProp
	@AlfQname(qname = "pjt:invoice")
	public Double getInvoiceAmount() {
		return invoiceAmount;
	}

	public void setInvoiceAmount(Double invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}
	@AlfSingleAssoc
	@AlfQname(qname = "pjt:ilTaskRef")
	public TaskListDataItem getTask() {
		return task;
	}

	public void setTask(TaskListDataItem task) {
		this.task = task;
	}
	
	
	public InvoiceListDataItem() {
		super();
	}
	
	public InvoiceListDataItem(BudgetListDataItem budget, Double invoiceAmount, TaskListDataItem task ) {
		super();
		this.budget = budget;	
		this.invoiceAmount = invoiceAmount;
		this.task = task;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((budget == null) ? 0 : budget.hashCode());
		result = prime * result
				+ ((invoiceAmount == null) ? 0 : invoiceAmount.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
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
		InvoiceListDataItem other = (InvoiceListDataItem) obj;
		if (budget == null) {
			if (other.budget != null)
				return false;
		} else if (!budget.equals(other.budget))
			return false;
		if (invoiceAmount == null) {
			if (other.invoiceAmount != null)
				return false;
		} else if (!invoiceAmount.equals(other.invoiceAmount))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InvoiceListDataItem [budget=" + budget + ", invoiceAmount="
				+ invoiceAmount + ", task=" + task + "]";
	}
	
	
	
	


}
