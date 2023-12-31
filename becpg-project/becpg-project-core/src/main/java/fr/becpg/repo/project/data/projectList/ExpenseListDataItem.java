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

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Planning list (done or to do)
 *
 * @author quere
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "pjt:expenseList")
public class ExpenseListDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1014093883753070859L;
	private BudgetListDataItem budget ; // elBudgetRef;
	private Double expenseAmount; // elExpenseAmount;
	private TaskListDataItem task; //elTaskRef;
	
	/**
	 * <p>Getter for the field <code>budget</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.BudgetListDataItem} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "pjt:elBudgetRef")
	public BudgetListDataItem getBudget() {
		return budget;
	}


	/**
	 * <p>Setter for the field <code>budget</code>.</p>
	 *
	 * @param budget a {@link fr.becpg.repo.project.data.projectList.BudgetListDataItem} object.
	 */
	public void setBudget(BudgetListDataItem budget) {
		this.budget = budget;
	}

	/**
	 * <p>Getter for the field <code>expenseAmount</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:expense")
	public Double getExpenseAmount() {
		return expenseAmount;
	}


	/**
	 * <p>Setter for the field <code>expenseAmount</code>.</p>
	 *
	 * @param expenseAmount a {@link java.lang.Double} object.
	 */
	public void setExpenseAmount(Double expenseAmount) {
		this.expenseAmount = expenseAmount;
	}

	
	/**
	 * <p>Getter for the field <code>task</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "pjt:elTaskRef")
	public TaskListDataItem getTask() {
		return task;
	}


	/**
	 * <p>Setter for the field <code>task</code>.</p>
	 *
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 */
	public void setTask(TaskListDataItem task) {
		this.task = task;
	}
	

	/**
	 * <p>Constructor for ExpenseListDataItem.</p>
	 */
	public ExpenseListDataItem() {
		super();
	}

	
	/**
	 * <p>Constructor for ExpenseListDataItem.</p>
	 *
	 * @param budget a {@link fr.becpg.repo.project.data.projectList.BudgetListDataItem} object.
	 * @param expenseAmount a {@link java.lang.Double} object.
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 */
	public ExpenseListDataItem(BudgetListDataItem budget, Double expenseAmount, TaskListDataItem task) {
		super();
		this.task= task;
		this.expenseAmount = expenseAmount;
		this.budget = budget;
	}


	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((budget == null) ? 0 : budget.hashCode());
		result = prime * result
				+ ((expenseAmount == null) ? 0 : expenseAmount.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
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
		ExpenseListDataItem other = (ExpenseListDataItem) obj;
		if (budget == null) {
			if (other.budget != null)
				return false;
		} else if (!budget.equals(other.budget))
			return false;
		if (expenseAmount == null) {
			if (other.expenseAmount != null)
				return false;
		} else if (!expenseAmount.equals(other.expenseAmount))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		return true;
	}


	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ExpenseListDataItem [budget=" + budget + ", expenseAmount="
				+ expenseAmount + ", task=" + task + "]";
	}


	}
