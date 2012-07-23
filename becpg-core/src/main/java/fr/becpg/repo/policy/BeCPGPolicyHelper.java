package fr.becpg.repo.policy;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;

public class BeCPGPolicyHelper {

	private static final String KEY_COPY_ENABLE = "BeCPGPolicyHelper.copyEnable";

	public static void enableCopyBehaviourForTransaction(){
		AlfrescoTransactionSupport.bindResource(KEY_COPY_ENABLE, Boolean.TRUE);
	}
	
	public static void disableCopyBehaviourForTransaction(){
		AlfrescoTransactionSupport.unbindResource(KEY_COPY_ENABLE);
	}
	
	public static boolean isCopyBehaviourEnableForTransaction(){
		return  AlfrescoTransactionSupport.getResource(KEY_COPY_ENABLE)!=null;
	}
	
}
