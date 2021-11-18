package fr.becpg.repo.batch;

import java.util.Objects;

import org.alfresco.repo.security.authentication.AuthenticationUtil;

public class BatchInfo {
	
	
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
	
	private Boolean runAsSystem;
	
	private Boolean notifyByMail;
	
	private String mailAction;
	
	private String mailActionUrl;


	public BatchInfo(String batchId, String batchDescId) {
		super();
		this.batchId = batchId;
		this.batchDescId = batchDescId;
		this.runAsSystem = Boolean.FALSE;
		this.notifyByMail = Boolean.FALSE;
		this.batchUser = AuthenticationUtil.getFullyAuthenticatedUser();
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


	public void enableNotifyByMail(String mailAction, String mailActionUrl ) {
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


	@Override
	public int hashCode() {
		return Objects.hash(batchDescId, batchId, batchUser, mailAction, mailActionUrl, notifyByMail, runAsSystem);
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
		return Objects.equals(batchDescId, other.batchDescId) && Objects.equals(batchId, other.batchId) && Objects.equals(batchUser, other.batchUser)
				&& Objects.equals(mailAction, other.mailAction) && Objects.equals(mailActionUrl, other.mailActionUrl)
				&& Objects.equals(notifyByMail, other.notifyByMail) && Objects.equals(runAsSystem, other.runAsSystem);
	}
	

}
