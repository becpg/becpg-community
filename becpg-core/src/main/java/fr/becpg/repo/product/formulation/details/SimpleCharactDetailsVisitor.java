package fr.becpg.repo.product.formulation.details;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.product.CharactDetailsVisitor;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.BaseObject;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.SimpleCharactDataItem;
import fr.becpg.repo.product.formulation.FormulateException;
import fr.becpg.repo.product.formulation.FormulationHelper;

public class SimpleCharactDetailsVisitor  implements CharactDetailsVisitor{

	private static Log logger = LogFactory.getLog(SimpleCharactDetailsVisitor.class);
	
	protected ProductDAO productDAO;
	
	protected QName dataListType;
	

	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}

	@Override
	public void setDataListType(QName dataListType) {
		this.dataListType = dataListType;
	}

	@Override
	public CharactDetails visit(ProductData productData, List<NodeRef> dataListItems) throws FormulateException {
		   
		  CharactDetails ret = new CharactDetails(extractCharacts(dataListItems));
		
		  // no compo => no formulation
		   if(productData.getCompoList() == null){			
				logger.debug("no compo => no formulation");
				return ret;
		   }
		   
			Double netWeight = FormulationHelper.getNetWeight(productData);
				
		   for(CompoListDataItem compoItem : productData.getCompoList()){	
			   //TODO un peu lourd de tous charger juste pour la densité ???
				ProductData productItem = productDAO.find(compoItem.getProduct(), new ArrayList<QName>());
				Double qty = FormulationHelper.getQty(compoItem);
				Double density = FormulationHelper.getDensity(productItem); 
				visitPart(compoItem.getProduct(), ret ,netWeight,  density * qty );
			}
		
		return ret;
	}

	protected List<NodeRef> extractCharacts(List<NodeRef> dataListItems) {
		
		List<NodeRef> ret = new ArrayList<NodeRef>();
		if(dataListItems!=null){
			for(NodeRef dataListItem : dataListItems ){
				
				BaseObject o  = productDAO.loadItemByType(dataListItem, dataListType);
				if(o!=null && o instanceof SimpleCharactDataItem){
					ret.add(((SimpleCharactDataItem)o).getCharactNodeRef());
				}
				
			}
		}
		
		return ret;
	}

	protected void visitPart(NodeRef product, CharactDetails charactDetails, Double qty ,Double netWeight) throws FormulateException {
		
		List<? extends SimpleCharactDataItem> simpleCharactDataList =  productDAO.loadList(product, dataListType);

		if(simpleCharactDataList == null){
			return;
		}
		
		for(SimpleCharactDataItem simpleCharact : simpleCharactDataList){	
			if(simpleCharact!=null && charactDetails.hasElement(simpleCharact.getCharactNodeRef())){
				
				
				Double value = (simpleCharact.getValue()!=null ? simpleCharact.getValue() :0d );
				value = value * qty;
				if(netWeight != 0.0d){
					value = value / netWeight;
				}
				charactDetails.addKeyValue(simpleCharact.getCharactNodeRef(), product,value );
				
			}
		}
		
	}



}
