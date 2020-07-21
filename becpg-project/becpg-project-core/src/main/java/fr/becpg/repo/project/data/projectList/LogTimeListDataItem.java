package fr.becpg.repo.project.data.projectList;

import java.util.Date;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Log time list
 *
 * @author quere
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "pjt:logTimeList")
public class LogTimeListDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1949764030787913474L;
	private Date date;
	private Double time;
	private TaskListDataItem task;
	private Double invoice;
	
	/**
	 * <p>Getter for the field <code>date</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:ltlDate")
	public Date getDate() {
		return date;
	}

	/**
	 * <p>Setter for the field <code>date</code>.</p>
	 *
	 * @param date a {@link java.util.Date} object.
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * <p>Getter for the field <code>time</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:ltlTime")
	public Double getTime() {
		return time;
	}

	/**
	 * <p>Setter for the field <code>time</code>.</p>
	 *
	 * @param time a {@link java.lang.Double} object.
	 */
	public void setTime(Double time) {
		this.time = time;
	}

	/**
	 * <p>Getter for the field <code>task</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "pjt:ltlTask")
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
	 * @param invoice a {@link java.lang.Double} object.
	 */
	public void setInvoice(Double invoice) {
		this.invoice = invoice;
	}
	
	/**
	 * <p>Constructor for LogTimeListDataItem.</p>
	 */
	public LogTimeListDataItem(){
		super();
	}
	
	/**
	 * <p>Constructor for LogTimeListDataItem.</p>
	 *
	 * @param date a {@link java.util.Date} object.
	 * @param time a {@link java.lang.Double} object.
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 */
	public LogTimeListDataItem(Date date, Double time, TaskListDataItem task){
		this.date = date;
		this.time = time;
		this.task = task;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((invoice == null) ? 0 : invoice.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
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
		LogTimeListDataItem other = (LogTimeListDataItem) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (invoice == null) {
			if (other.invoice != null)
				return false;
		} else if (!invoice.equals(other.invoice))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "LogTimeListDataItem [date=" + date + ", time=" + time + ", task=" + task + ", invoice=" + invoice + "]";
	}

}
