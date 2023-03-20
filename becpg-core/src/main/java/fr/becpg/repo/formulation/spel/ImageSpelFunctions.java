package fr.becpg.repo.formulation.spel;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.repository.RepositoryEntity;

@Service
public class ImageSpelFunctions implements CustomSpelFunctions {

	@Autowired
	private QuickShareService quickShareService;

	@Autowired
	private EntityService entityService;

	@Autowired
	private SysAdminParams sysAdminParams;

	/** {@inheritDoc} */
	@Override
	public boolean match(String beanName) {
		return beanName.equals("img");
	}

	/** {@inheritDoc} */
	@Override
	public Object create(RepositoryEntity repositoryEntity) {
		return new ImageSpelFunctionsWrapper(repositoryEntity);
	}

	public class ImageSpelFunctionsWrapper {

		RepositoryEntity entity;

		public ImageSpelFunctionsWrapper(RepositoryEntity entity) {
			super();
			this.entity = entity;
		}

		public String getEntityImagePublicUrl() {

			NodeRef imageNodeRef = entityService.getEntityDefaultImage(entity.getNodeRef());

			if (imageNodeRef != null) {

				QuickShareDTO quickShareDTO = quickShareService.shareContent(entity.getNodeRef());

				if (quickShareDTO != null) {
					return sysAdminParams.getAlfrescoProtocol() + "://" + sysAdminParams.getAlfrescoHost() + ":" + sysAdminParams.getAlfrescoPort()
							+ "/alfresco/service/api/internal/shared/node/" + quickShareDTO.getId() + "/content";
				}
			}

			return null;

		}
	}

}
