package fr.becpg.repo.formulation.impl;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.ReportableEntity;
import fr.becpg.repo.formulation.ReportableEntityService;
import fr.becpg.repo.formulation.ReportableError;
import fr.becpg.repo.formulation.ReportableError.ReportableErrorType;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>ReportableEntityServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("reportableEntityService")
public class ReportableEntityServiceImpl implements ReportableEntityService {

	private static final Log logger = LogFactory.getLog(ReportableEntityServiceImpl.class);

	@Autowired
	protected AlfrescoRepository<BeCPGDataObject> alfrescoRepository;
	
	@Autowired
	private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;
	
	@Autowired
	private NodeService nodeService;
	
	/** {@inheritDoc} */
	@Override
	public void postEntityErrors(NodeRef entityNodeRef, String formulationChainId, Set<ReportableError> errors) {

		QName type = nodeService.getType(entityNodeRef);
		Class<RepositoryEntity> entityClass = repositoryEntityDefReader.getEntityClass(type);
		
		if (entityClass != null) {
			BeCPGDataObject entity = alfrescoRepository.findOne(entityNodeRef);
			if (entity instanceof ReportableEntity) {
				postErrors((ReportableEntity) entity, formulationChainId, errors);
			}
		} else {
			logErrors(errors);
		}
	}

	private void postErrors(ReportableEntity entity, String formulationChainId, Set<ReportableError> errors) {
		for (ReportableError error : errors) {
			entity.addError(error.getDisplayMessage(), formulationChainId, error.getSources());
		}
		entity.setFormulationChainId(formulationChainId);
		if (entity.merge()) {
			alfrescoRepository.save((BeCPGDataObject) entity);
		}
	}
	
	private void logErrors(Set<ReportableError> errors) {
		for (ReportableError error : errors) {
			if (error.getType() == ReportableErrorType.WARNING) {
				logger.warn(error.getMessage());
			} else if (error.getType() == ReportableErrorType.ERROR) {
				logger.error(error.getMessage());
			}
		}
	}

}
