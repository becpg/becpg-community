package fr.becpg.repo.formulation.spel;

import java.util.List;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>ImageSpelFunctions class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
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
			return getEntityImagePublicUrl(entity);
		}

		public byte[] getEntityImageBytes() {
			return getEntityImageBytes(entity);
		}

		public String getEntityImagePublicUrl(RepositoryEntity entity) {
			return getEntityImagePublicUrl(entity.getNodeRef());
		}

		public byte[] getEntityImageBytes(RepositoryEntity entity) {
			return getEntityImageBytes(entity.getNodeRef());
		}

		public String getEntityImagePublicUrl(NodeRef entityNodeRef) {
			try {
				return shareImage(entityService.getEntityDefaultImage(entityNodeRef));
			} catch (BeCPGException e) {
				return null;
			}
		}

		public byte[] getEntityImageBytes(NodeRef entityNodeRef) {
			try {
				return entityService.getImage((entityService.getEntityDefaultImage(entityNodeRef)));
			} catch (BeCPGException e) {
				return null;
			}
		}

		public String getImagePublicUrlByPath(String path) {
			return getImagePublicUrlByPath(entity, path);

		}

		public String getImagePublicUrlByPath(RepositoryEntity entity, String path) {
			return getImagePublicUrlByPath(entity.getNodeRef(), path);

		}

		public String getImagePublicUrlByPath(NodeRef entityNodeRef, String path) {
			List<NodeRef> imageNodeRefs = BeCPGQueryBuilder.createQuery().selectNodesByPath(entityNodeRef, path);
			if ((imageNodeRefs != null) && !imageNodeRefs.isEmpty()) {
				return shareImage(imageNodeRefs.get(0));
			}
			return null;
		}

		public byte[] getImageBytesByPath(String path) {
			return getImageBytesByPath(entity, path);

		}

		public byte[] getImageBytesByPath(RepositoryEntity entity, String path) {
			return getImageBytesByPath(entity.getNodeRef(), path);

		}

		public byte[] getImageBytesByPath(NodeRef entityNodeRef, String path) {
			List<NodeRef> imageNodeRefs = BeCPGQueryBuilder.createQuery().selectNodesByPath(entityNodeRef, path);
			if ((imageNodeRefs != null) && !imageNodeRefs.isEmpty()) {

				return getEntityImageBytes(imageNodeRefs.get(0));
			}
			return null;
		}

		private String shareImage(NodeRef imageNodeRef) {

			if (imageNodeRef != null) {

				QuickShareDTO quickShareDTO = quickShareService.shareContent(imageNodeRef);

				if (quickShareDTO != null) {
					return sysAdminParams.getAlfrescoProtocol() + "://" + sysAdminParams.getAlfrescoHost() + ":" + sysAdminParams.getAlfrescoPort()
							+ "/alfresco/service/api/internal/shared/node/" + quickShareDTO.getId() + "/content";
				}
			}
			return null;
		}

	}

}
