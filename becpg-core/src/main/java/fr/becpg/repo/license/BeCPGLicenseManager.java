package fr.becpg.repo.license;

import org.springframework.stereotype.Service;

@Service("becpgLicenseManager")
public class BeCPGLicenseManager {

	private long allowedConcurrentRead = -1L;
	private long allowedConcurrentWrite = -1L;
	private long allowedConcurrentSupplier = -1L;
	private long allowedNamedWrite = -1L;
	private long allowedNamedRead = -1L;
	
	public long getAllowedConcurrentRead() {
		return allowedConcurrentRead;
	}
	public long getAllowedConcurrentWrite() {
		return allowedConcurrentWrite;
	}
	public long getAllowedConcurrentSupplier() {
		return allowedConcurrentSupplier;
	}
	public long getAllowedNamedWrite() {
		return allowedNamedWrite;
	}
	public long getAllowedNamedRead() {
		return allowedNamedRead;
	}
	public String getLicenseName() {
		return "beCPG OO License";
	}
	
	//TODO look at license json file under /System/license/license.json 
	// if no license return -1L for unlimited else cache it 
	
	
}
