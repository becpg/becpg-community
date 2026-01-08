package fr.becpg.util;

import java.util.LinkedHashSet;
import java.util.Set;

import org.alfresco.util.transaction.TransactionListener;
import org.alfresco.util.transaction.TransactionSupportUtil;

public class BeCPGTransactionUtil {

    private static final String RESOURCE_KEY_TXN_PRE_LISTENERS = "AlfrescoTransactionSupport.preListeners";
	
	public static void bindLateTransactionListener(TransactionListener transactionListener) {
		Set<TransactionListener> preListeners = TransactionSupportUtil.getResource(RESOURCE_KEY_TXN_PRE_LISTENERS);
		if (preListeners == null) {
			preListeners = new LinkedHashSet<>();
		}
		preListeners.add(transactionListener);
		TransactionSupportUtil.bindResource(RESOURCE_KEY_TXN_PRE_LISTENERS, preListeners);
	}

}
