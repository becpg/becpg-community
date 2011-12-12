package fr.becpg.repo.designer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.becpg.common.dom.DOMUtils;
import fr.becpg.model.DesignerModel;
import fr.becpg.repo.designer.data.FormControl;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class FormModelVisitor {

	private static Log logger = LogFactory.getLog(FormModelVisitor.class);
	
	private NodeService nodeService;
	
	
	/**
	 * @param nodeService the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Build a list of controls
	 * @param is
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws FactoryConfigurationError
	 */
	public List<FormControl> visitControls(InputStream is) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError{
		List<FormControl> ret = new ArrayList<FormControl>();
		
		Document doc = DOMUtils.parse(is);
		
		NodeList list = doc.getElementsByTagName("control");
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);
			FormControl formControl = new FormControl();
			formControl.setId(elem.getAttribute("id"));
			formControl.setDescription(DOMUtils.getElementText(elem, "description"));
			ret.add(formControl);
		}
		
		
		return ret;
	}

	public void visitModelTemplate(NodeRef ret, QName nodeTypeQname, String controlId, InputStream is) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
		
		if(controlId!=null){
			Document doc = DOMUtils.parse(is);
			
			NodeList list = doc.getElementsByTagName("control");
			for (int i = 0; i < list.getLength(); i++) {
				Element elem = (Element) list.item(i);
				if(controlId.equals(elem.getAttribute("id"))){
					logger.debug("found control : "+controlId);
					nodeService.setProperty(ret, DesignerModel.PROP_DSG_TEMPLATEPATH, elem.getAttribute("template"));
					nodeService.setProperty(ret, DesignerModel.PROP_DSG_ID, controlId);
					NodeList params =  elem.getElementsByTagName("control-param");
					for (int j = 0; j < params.getLength(); j++) {
						Element param = (Element) params.item(j);
						ChildAssociationRef childAssociationRef = nodeService.createNode(ret, DesignerModel.ASSOC_DSG_PARAMETERS, DesignerModel.ASSOC_DSG_PARAMETERS, DesignerModel.TYPE_DSG_CONTROLPARAMETER);
						NodeRef paramRef = childAssociationRef.getChildRef();
						nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_OPTIONAL, param.getAttribute("optional"));
						nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_ID, param.getAttribute("name"));
						nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_PARAMETERTYPE, param.getAttribute("type"));
						nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_PARAMETERVALUE, param.getAttribute("default"));
						nodeService.setProperty(paramRef, DesignerModel.PROP_DSG_PARAMETERDESCRIPTION, DOMUtils.getElementText(param));
						
					}
					
				}
			}
		}
		
	}
	

}
