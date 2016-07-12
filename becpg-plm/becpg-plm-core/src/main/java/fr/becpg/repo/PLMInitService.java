package fr.becpg.repo;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;

@Service
public class PLMInitService {

	@Autowired
	EntityListDAO entityListDAO;
	
	
	@PostConstruct
	public void init(){
		entityListDAO.registerHiddenList(PLMModel.TYPE_REQCTRLLIST);
	}
	
}
