package fr.becpg.repo.ecm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.ChangeOrderType;
import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.entity.version.EntityVersionPlugin;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>
 * ECOVersionPlugin class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ECOVersionPlugin implements EntityVersionPlugin {

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private RepoService repoService;

	@Autowired
	private ECOService ecoService;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;

	private Boolean deleteOnApply() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.eco.automatic.deleteOnApply"));
	}

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private static final Log logger = LogFactory.getLog(ECOVersionPlugin.class);

	/** {@inheritDoc} */
	@Override
	public void doAfterCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// DO Nothing
	}

	/** {@inheritDoc} */
	@Override
	public void doBeforeCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// DO Nothing
	}

	/** {@inheritDoc} */
	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// DO Nothing
	}

	/** {@inheritDoc} */
	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description, Date effectiveDate) {

		String userName = AuthenticationUtil.getFullyAuthenticatedUser();
		
		Thread asyncEcoGenerator = new Thread() {
			
			@Override
			public void run() {
				

				AuthenticationUtil.runAs(() -> {
					String name = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
					String versionLabel = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_VERSION_LABEL);

					if (versionLabel == null) {
						versionLabel = RepoConsts.INITIAL_VERSION;
					}
					
					String ecoName = generateEcoName(name + "_v" + versionLabel);

					NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {


						if (logger.isDebugEnabled()) {
							logger.debug("Creating new impactWUsed change order");
						}
						ChangeOrderData changeOrderData = (ChangeOrderData) alfrescoRepository.create(getChangeOrderFolder(),
								new ChangeOrderData(ecoName, ECOState.Automatic, ChangeOrderType.ImpactWUsed, null));

						changeOrderData.setDescription(description);
						changeOrderData.setEcoState(ECOState.InProgress);
						changeOrderData.setEffectiveDate(effectiveDate);

						List<ReplacementListDataItem> replacementList = changeOrderData.getReplacementList();

						if (replacementList == null) {
							replacementList = new ArrayList<>();
						}
						RevisionType revisionType = VersionType.MAJOR.equals(versionType) ? RevisionType.Major : RevisionType.Minor;

						replacementList.add(new ReplacementListDataItem(revisionType, Collections.singletonList(entityNodeRef), entityNodeRef, 100));

						if (logger.isDebugEnabled()) {
							logger.debug("Adding nodeRef " + entityNodeRef + " to automatic change order :" + changeOrderData.getName());
							logger.debug("Revision type : " + revisionType);
						}

						changeOrderData.setReplacementList(replacementList);
						alfrescoRepository.save(changeOrderData);

						return changeOrderData.getNodeRef();

					}, false, true);

					transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
						return ecoService.apply(ecoNodeRef, deleteOnApply(), true, false);
					}, false, true);
					return null;
				}, userName);
			};
		};
		
		asyncEcoGenerator.start();
		
	}
	
	private NodeRef getChangeOrderFolder() {
		return repoService.getFolderByPath("/" + RepoConsts.PATH_SYSTEM + "/" + PlmRepoConsts.PATH_ECO);
	}
	

	private String generateEcoName(String name) {
		return name + "-" + I18NUtil.getMessage("plm.ecm.current.name", new Date());
	}

}
