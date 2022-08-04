package fr.becpg.repo.batch;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.alfresco.repo.security.authentication.AuthenticationUtil;

import fr.becpg.repo.audit.AuditModelVisitor;
import fr.becpg.repo.audit.model.AuditModel;

public class BatchInfo implements Serializable, AuditModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7569652805526586359L;
	
	public static final int BATCH_THREAD = 3;
	public static final int BATCH_SIZE = 15;

	/**
	 * Will serve as uniq batchId
	 */
	private String batchId;

	/**
	 * Id of the batch will be used assign to threadPool
	 */
	private String batchDescId;

	/**
	 * User that has launch the batch
	 */
	private String batchUser;

	private Boolean runAsSystem = Boolean.FALSE;

	private Boolean notifyByMail = Boolean.FALSE;

	private String mailAction;

	private String mailActionUrl;
	
	private String entityDescription;

	private int workerThreads = BATCH_THREAD;

	private int batchSize = BATCH_SIZE;
	
	private Date startTime;
	
	private Date endTime;
	
	private Boolean isCompleted = Boolean.FALSE;
	
	private int totalItems = 0;

	public BatchInfo(String batchId, String batchDescId) {
		super();
		this.batchId = batchId;
		this.batchDescId = batchDescId;
		this.batchUser = AuthenticationUtil.getRunAsUser();
	}
	
	public BatchInfo(String batchId, String batchDescId, String entityDescription) {
		super();
		this.batchId = batchId;
		this.batchDescId = batchDescId;
		this.entityDescription = entityDescription;
		this.batchUser = AuthenticationUtil.getRunAsUser();
	}
	
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public Date getEndTime() {
		return endTime;
	}
	
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public Date getStartTime() {
		return startTime;
	}

	public String getBatchId() {
		return batchId;
	}

	public String getBatchUser() {
		return batchUser;
	}

	public Boolean getRunAsSystem() {
		return runAsSystem;
	}

	public Boolean getNotifyByMail() {
		return notifyByMail;
	}
	
	public String getEntityDescription() {
		return entityDescription;
	}
	
	public int getTotalItems() {
		return totalItems;
	}
	
	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	public void enableNotifyByMail(String mailAction, String mailActionUrl) {
		this.notifyByMail = Boolean.TRUE;
		this.mailAction = mailAction;
		this.mailActionUrl = mailActionUrl;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public void setBatchUser(String batchUser) {
		this.batchUser = batchUser;
	}

	public void setRunAsSystem(Boolean runAsSystem) {
		this.runAsSystem = runAsSystem;
	}

	public String getMailActionUrl() {
		return mailActionUrl;
	}

	public String getBatchDescId() {
		return batchDescId;
	}

	public String getMailAction() {
		return mailAction;
	}

	public int getWorkerThreads() {
		return workerThreads;
	}

	public void setWorkerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public Boolean getIsCompleted() {
		return isCompleted;
	}

	public void setIsCompleted(Boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	@Override
	public int hashCode() {
		return Objects.hash(batchDescId, batchId, batchSize, batchUser, mailAction, mailActionUrl, notifyByMail, runAsSystem, workerThreads);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BatchInfo other = (BatchInfo) obj;
		return Objects.equals(batchDescId, other.batchDescId) && Objects.equals(batchId, other.batchId) && batchSize == other.batchSize
				&& Objects.equals(batchUser, other.batchUser) && Objects.equals(mailAction, other.mailAction)
				&& Objects.equals(mailActionUrl, other.mailActionUrl) && Objects.equals(notifyByMail, other.notifyByMail)
				&& Objects.equals(runAsSystem, other.runAsSystem) && workerThreads == other.workerThreads;
	}

	@Override
	public Map<String, Serializable> accept(AuditModelVisitor visitor) {
		return visitor.visitBatchInfo(this);
	}

}
