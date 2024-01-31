package fr.becpg.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import jakarta.annotation.PostConstruct;

/**
 * <p>PLMInitService class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class PLMInitService {

	@Autowired
	EntityListDAO entityListDAO;
	
	
	/**
	 * <p>init.</p>
	 */
	@PostConstruct
	public void init(){
		entityListDAO.registerHiddenList(PLMModel.TYPE_REQCTRLLIST);
	}
	
}
