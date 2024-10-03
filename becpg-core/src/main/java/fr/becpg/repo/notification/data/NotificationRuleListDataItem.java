package fr.becpg.repo.notification.data;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.search.data.DateFilterType;
import fr.becpg.repo.search.data.VersionFilterType;

/**
 * <p>NotificationRuleListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:notificationRuleList")
public class NotificationRuleListDataItem extends BeCPGDataObject{

	private static final long serialVersionUID = 996800984821144870L;

	private String nodeType;
	private String dateField;
	private String condtions;
	private NodeRef target;
	private int days;
	private String subject;
	private NodeRef email;
	private int frequency;
	private DateFilterType timeType;
	private List<NodeRef> authorities = new ArrayList<>();
	private Date frequencyStartDate;
	private VersionFilterType versionFilterType;
	private boolean enforced;
	private RecurringTimeType recurringTime;
	private DayOfWeek recurringDay;
	private NodeRef script;
	private ScriptMode scriptMode;
	private String errorLog;
	private List<NodeRef> reportTpls;
	
	
	
	/**
	 * <p>Constructor for NotificationRuleListDataItem.</p>
	 */
	public NotificationRuleListDataItem() {
		super();	
	}

	/**
	 * <p>Getter for the field <code>nodeType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrNodeType")
	public String getNodeType() {
		return nodeType;
	}

	/**
	 * <p>Setter for the field <code>nodeType</code>.</p>
	 *
	 * @param nodeType a {@link java.lang.String} object.
	 */
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * <p>Getter for the field <code>dateField</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrDateField")
	public String getDateField() {
		return dateField;
	}

	/**
	 * <p>Setter for the field <code>dateField</code>.</p>
	 *
	 * @param dateField a {@link java.lang.String} object.
	 */
	public void setDateField(String dateField) {
		this.dateField = dateField;
	}

	/**
	 * <p>Getter for the field <code>target</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:nrTarget")
	public NodeRef getTarget() {
		return target;
	}

	/**
	 * <p>Setter for the field <code>target</code>.</p>
	 *
	 * @param target a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setTarget(NodeRef target) {
		this.target = target;
	}

	/**
	 * <p>Getter for the field <code>condtions</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrConditions")
	public String getCondtions() {
		return condtions;
	}

	/**
	 * <p>Setter for the field <code>condtions</code>.</p>
	 *
	 * @param condtions a {@link java.lang.String} object.
	 */
	public void setCondtions(String condtions) {
		this.condtions = condtions;
	}

	/**
	 * <p>Getter for the field <code>frequency</code>.</p>
	 *
	 * @return a int.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrFrequency")
	public int getFrequency() {
		return frequency;
	}

	/**
	 * <p>Setter for the field <code>frequency</code>.</p>
	 *
	 * @param frequency a int.
	 */
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}


	/**
	 * <p>Getter for the field <code>days</code>.</p>
	 *
	 * @return a int.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrTimeNumber")
	public int getDays() {
		return days;
	}

	/**
	 * <p>Setter for the field <code>days</code>.</p>
	 *
	 * @param days a int.
	 */
	public void setDays(int days) {
		this.days = days;
	}

	/**
	 * <p>Getter for the field <code>timeType</code>.</p>
	 *
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrTimeType")
	public DateFilterType getTimeType() {
		return timeType;
	}

	/**
	 * <p>Setter for the field <code>timeType</code>.</p>
	 *
	 */
	public void setTimeType(DateFilterType timeType) {
		this.timeType = timeType;
	}

	/**
	 * <p>Getter for the field <code>authorities</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:nrNotificationAuthorities")
	public List<NodeRef> getAuthorities() {
		return authorities;
	}

	/**
	 * <p>Setter for the field <code>authorities</code>.</p>
	 *
	 * @param authorities a {@link java.util.List} object.
	 */
	public void setAuthorities(List<NodeRef> authorities) {
		this.authorities = authorities;
	}

	/**
	 * <p>Getter for the field <code>subject</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrSubject")
	public String getSubject() {
		return subject;
	}

	/**
	 * <p>Setter for the field <code>subject</code>.</p>
	 *
	 * @param subject a {@link java.lang.String} object.
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * <p>Getter for the field <code>email</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="bcpg:nrEmail")
	public NodeRef getEmail() {
		return email;
	}

	/**
	 * <p>Setter for the field <code>email</code>.</p>
	 *
	 * @param email a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setEmail(NodeRef email) {
		this.email = email;
	}

	/**
	 * <p>Getter for the field <code>frequencyStartDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrFrequencyStartDate")
	public Date getFrequencyStartDate() {
		return frequencyStartDate;
	}

	/**
	 * <p>Setter for the field <code>frequencyStartDate</code>.</p>
	 *
	 * @param frequencyStartDate a {@link java.util.Date} object.
	 */
	public void setFrequencyStartDate(Date frequencyStartDate) {
		this.frequencyStartDate = frequencyStartDate;
	}


	/**
	 * <p>Getter for the field <code>versionFilterType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.search.data.VersionFilterType} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrVersionFilter")
	public VersionFilterType getVersionFilterType() {
		return versionFilterType;
	}

	/**
	 * <p>Setter for the field <code>versionFilterType</code>.</p>
	 *
	 * @param versionFilterType a {@link fr.becpg.repo.search.data.VersionFilterType} object.
	 */
	public void setVersionFilterType(VersionFilterType versionFilterType) {
		this.versionFilterType = versionFilterType;
	}

	/**
	 * <p>isEnforced.</p>
	 *
	 * @return a boolean.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrForceNotification")
	public boolean isEnforced() {
		return enforced;
	}

	/**
	 * <p>Setter for the field <code>enforced</code>.</p>
	 *
	 * @param enforced a boolean.
	 */
	public void setEnforced(boolean enforced) {
		this.enforced = enforced;
	}

	/**
	 * <p>Getter for the field <code>recurringTime</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.notification.data.RecurringTimeType} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrRecurringTimeType")
	public RecurringTimeType getRecurringTime() {
		return recurringTime;
	}

	/**
	 * <p>Setter for the field <code>recurringTime</code>.</p>
	 *
	 * @param recurringTime a {@link fr.becpg.repo.notification.data.RecurringTimeType} object.
	 */
	public void setRecurringTime(RecurringTimeType recurringTime) {
		this.recurringTime = recurringTime;
	}

	/**
	 * <p>Getter for the field <code>recurringDay</code>.</p>
	 *
	 * @return a {@link java.time.DayOfWeek} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrRecurringDay")
	public DayOfWeek getRecurringDay() {
		return recurringDay;
	}
	
	/**
	 * <p>Getter for the field <code>script</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:nrScript")
	public NodeRef getScript() {
		return script;
	}
	
	/**
	 * <p>Setter for the field <code>script</code>.</p>
	 *
	 * @param script a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setScript(NodeRef script) {
		this.script = script;
	}
	
	/**
	 * <p>Getter for the field <code>scriptMode</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.notification.data.ScriptMode} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrScriptMode")
	public ScriptMode getScriptMode() {
		return scriptMode;
	}
	
	/**
	 * <p>Setter for the field <code>scriptMode</code>.</p>
	 *
	 * @param scriptMode a {@link fr.becpg.repo.notification.data.ScriptMode} object
	 */
	public void setScriptMode(ScriptMode scriptMode) {
		this.scriptMode = scriptMode;
	}
	
	/**
	 * <p>Setter for the field <code>recurringDay</code>.</p>
	 *
	 * @param recurringDay a {@link java.time.DayOfWeek} object.
	 */
	public void setRecurringDay(DayOfWeek recurringDay) {
		this.recurringDay = recurringDay;
	}
	
	/**
	 * <p>Getter for the field <code>errorLog</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname="bcpg:nrErrorLog")
	public String getErrorLog() {
		return errorLog;
	}
	
	/**
	 * <p>Setter for the field <code>errorLog</code>.</p>
	 *
	 * @param errorLog a {@link java.lang.String} object
	 */
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}
	
	/**
	 * <p>Getter for the field <code>reportTpls</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "rep:reportTpls")
	public List<NodeRef> getReportTpls() {
		return reportTpls;
	}

	/**
	 * <p>Setter for the field <code>reportTpls</code>.</p>
	 *
	 * @param reportTpls a {@link java.util.List} object
	 */
	public void setReportTpls(List<NodeRef> reportTpls) {
		this.reportTpls = reportTpls;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((authorities == null) ? 0 : authorities.hashCode());
		result = prime * result + ((condtions == null) ? 0 : condtions.hashCode());
		result = prime * result + ((dateField == null) ? 0 : dateField.hashCode());
		result = prime * result + days;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + (enforced ? 1231 : 1237);
		result = prime * result + frequency;
		result = prime * result + ((frequencyStartDate == null) ? 0 : frequencyStartDate.hashCode());
		result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
		result = prime * result + ((recurringDay == null) ? 0 : recurringDay.hashCode());
		result = prime * result + ((recurringTime == null) ? 0 : recurringTime.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		result = prime * result + ((timeType == null) ? 0 : timeType.hashCode());
		result = prime * result + ((versionFilterType == null) ? 0 : versionFilterType.hashCode());
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
		NotificationRuleListDataItem other = (NotificationRuleListDataItem) obj;
		if (authorities == null) {
			if (other.authorities != null)
				return false;
		} else if (!authorities.equals(other.authorities))
			return false;
		if (condtions == null) {
			if (other.condtions != null)
				return false;
		} else if (!condtions.equals(other.condtions))
			return false;
		if (dateField == null) {
			if (other.dateField != null)
				return false;
		} else if (!dateField.equals(other.dateField))
			return false;
		if (days != other.days)
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (enforced != other.enforced)
			return false;
		if (frequency != other.frequency)
			return false;
		if (frequencyStartDate == null) {
			if (other.frequencyStartDate != null)
				return false;
		} else if (!frequencyStartDate.equals(other.frequencyStartDate))
			return false;
		if (nodeType == null) {
			if (other.nodeType != null)
				return false;
		} else if (!nodeType.equals(other.nodeType))
			return false;
		if (recurringDay != other.recurringDay)
			return false;
		if (recurringTime != other.recurringTime)
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (timeType != other.timeType)
			return false;
		if (versionFilterType != other.versionFilterType)
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "NotificationRuleListDataItem [nodeType=" + nodeType + ", dateField=" + dateField + ", condtions=" + condtions + ", target=" + target
				+ ", days=" + days + ", subject=" + subject + ", email=" + email + ", frequency=" + frequency + ", timeType=" + timeType
				+ ", authorities=" + authorities + ", frequencyStartDate=" + frequencyStartDate + ", versionType=" + versionFilterType + ", enforced="
				+ enforced + ", recurringTime=" + recurringTime + ", recurringDay=" + recurringDay + "]";
	}

	
	

}
