package fr.becpg.web.app.servlet;

import java.util.List;

import org.alfresco.repo.admin.SysAdminParams;

public class SysAdminWrapper implements SysAdminParams {

	private SysAdminParams sysAdminParams;
	
	private String oauthShareHost;
	
	
	public SysAdminParams getSysAdminParams() {
		return sysAdminParams;
	}

	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}

	public String getOauthShareHost() {
		return oauthShareHost;
	}

	public void setOauthShareHost(String oauthShareHost) {
		this.oauthShareHost = oauthShareHost;
	}

	@Override
	public String getAlfrescoContext() {
		return sysAdminParams.getAlfrescoContext();
	}

	@Override
	public String getAlfrescoHost() {
		return sysAdminParams.getAlfrescoHost();
	}

	@Override
	public int getAlfrescoPort() {
		return sysAdminParams.getAlfrescoPort();
	}

	@Override
	public String getAlfrescoProtocol() {
		return sysAdminParams.getAlfrescoProtocol();
	}

	@Override
	public boolean getAllowWrite() {
		return sysAdminParams.getAllowWrite();
	}

	@Override
	public List<String> getAllowedUserList() {
		return sysAdminParams.getAllowedUserList();
	}

	@Override
	public int getMaxUsers() {
		return sysAdminParams.getMaxUsers();
	}

	@Override
	public String getShareContext() {
		return sysAdminParams.getShareContext();
	}

	@Override
	public String getShareHost() {
		if(oauthShareHost!=null && oauthShareHost.length()>0){
			return oauthShareHost;
		} 
		return sysAdminParams.getShareHost();
	}

	@Override
	public int getSharePort() {
		return sysAdminParams.getSharePort();
	}

	@Override
	public String getShareProtocol() {
		return sysAdminParams.getShareProtocol();
	}

	@Override
	public String getSitePublicGroup() {
		return sysAdminParams.getSitePublicGroup();
	}

	@Override
	public String subsituteHost(String arg0) {
		return sysAdminParams.subsituteHost(arg0);
	}

}
