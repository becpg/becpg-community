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
	private NotificationRuleTimeType timeType;
	private List<NodeRef> authorities = new ArrayList<>();
	private Date frequencyStartDate;
	private VersionFilterType versionFilterType;
	private boolean enforced;
	private RecurringTimeType recurringTime;
	private DayOfWeek recurringDay;
	
	
	
	public NotificationRuleListDataItem() {
		super();	
	}

	@AlfProp
	@AlfQname(qname="bcpg:nrNodeType")
	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nrDateField")
	public String getDateField() {
		return dateField;
	}

	public void setDateField(String dateField) {
		this.dateField = dateField;
	}

	@AlfSingleAssoc
	@AlfQname(qname="bcpg:nrTarget")
	public NodeRef getTarget() {
		return target;
	}

	public void setTarget(NodeRef target) {
		this.target = target;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nrConditions")
	public String getCondtions() {
		return condtions;
	}

	public void setCondtions(String condtions) {
		this.condtions = condtions;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nrFrequency")
	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}


	@AlfProp
	@AlfQname(qname="bcpg:nrTimeNumber")
	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nrTimeType")
	public NotificationRuleTimeType getTimeType() {
		return timeType;
	}

	public void setTimeType(NotificationRuleTimeType timeType) {
		this.timeType = timeType;
	}

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:nrNotificationAuthorities")
	public List<NodeRef> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<NodeRef> authorities) {
		this.authorities = authorities;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nrSubject")
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@AlfSingleAssoc
	@AlfQname(qname="bcpg:nrEmail")
	public NodeRef getEmail() {
		return email;
	}

	public void setEmail(NodeRef email) {
		this.email = email;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nrFrequencyStartDate")
	public Date getFrequencyStartDate() {
		return frequencyStartDate;
	}

	public void setFrequencyStartDate(Date frequencyStartDate) {
		this.frequencyStartDate = frequencyStartDate;
	}


	@AlfProp
	@AlfQname(qname="bcpg:nrVersionFilter")
	public VersionFilterType getVersionFilterType() {
		return versionFilterType;
	}

	public void setVersionFilterType(VersionFilterType versionFilterType) {
		this.versionFilterType = versionFilterType;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nrForceNotification")
	public boolean isEnforced() {
		return enforced;
	}

	public void setEnforced(boolean enforced) {
		this.enforced = enforced;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nrRecurringTimeType")
	public RecurringTimeType getRecurringTime() {
		return recurringTime;
	}

	public void setRecurringTime(RecurringTimeType recurringTime) {
		this.recurringTime = recurringTime;
	}

	@AlfProp
	@AlfQname(qname="bcpg:nrRecurringDay")
	public DayOfWeek getRecurringDay() {
		return recurringDay;
	}

	public void setRecurringDay(DayOfWeek recurringDay) {
		this.recurringDay = recurringDay;
	}

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

	@Override
	public String toString() {
		return "NotificationRuleListDataItem [nodeType=" + nodeType + ", dateField=" + dateField + ", condtions=" + condtions + ", target=" + target
				+ ", days=" + days + ", subject=" + subject + ", email=" + email + ", frequency=" + frequency + ", timeType=" + timeType
				+ ", authorities=" + authorities + ", frequencyStartDate=" + frequencyStartDate + ", versionType=" + versionFilterType + ", enforced="
				+ enforced + ", recurringTime=" + recurringTime + ", recurringDay=" + recurringDay + "]";
	}

	
	

}
