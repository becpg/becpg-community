package fr.becpg.repo.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.product.data.ProductData;

public class PropertyServiceImpl implements PropertyService {

	
	
	private NodeService nodeService;	
		
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}	

	@Override	
	public String getStringValue(PropertyDefinition propertyDef, Serializable v, PropertyFormats propertyFormats) {
		
		String value = null;
		
		if(v == null || propertyDef == null){
			return value;
		}		
		
		String dataType =propertyDef.getDataType().toString(); 		
		
		if(dataType.equals(DataTypeDefinition.ASSOC_REF.toString())){
			value = (String)nodeService.getProperty((NodeRef)v, ContentModel.PROP_NAME);
		}
		else if(dataType.equals(DataTypeDefinition.CATEGORY.toString())){
			
			@SuppressWarnings("unchecked")
			List<NodeRef> categories = (ArrayList<NodeRef>)v;
			
			for(NodeRef categoryNodeRef : categories){			
				if(value == null){
					value = (String)nodeService.getProperty(categoryNodeRef, ContentModel.PROP_NAME);
				}
				else{
					value += RepoConsts.LABEL_SEPARATOR + (String)nodeService.getProperty(categoryNodeRef, ContentModel.PROP_NAME);
				}
			}
		}
		else if(dataType.equals(DataTypeDefinition.BOOLEAN.toString())){
			
			Boolean b = (Boolean)v;			
			
//			if(propertyFormats.isUseDefaultLocale()){
//				value = b ? I18NUtil.getMessage(MESSAGE_TRUE, Locale.getDefault()) : I18NUtil.getMessage(MESSAGE_FALSE, Locale.getDefault());
//			}
//			else{
//				value = b ? I18NUtil.getMessage(MESSAGE_TRUE) : I18NUtil.getMessage(MESSAGE_FALSE);
//			}
			
			value = TranslateHelper.getTranslatedBoolean(b, propertyFormats.isUseDefaultLocale());
			
		}
		else if(dataType.equals(DataTypeDefinition.TEXT.toString())){
			
			if(propertyDef.getName().equals(BeCPGModel.PROP_PRODUCT_STATE)){
				
				value = TranslateHelper.getTranslatedProductState(ProductData.getSystemState((String)v));
			}			
			else if(propertyDef.isMultiValued()){
				
				@SuppressWarnings("unchecked")
				List<String> values = (List<String>)v;
				
				for(String tempValue : values){
					
					if(value == null){
						value = tempValue;
					}
					else{
						value += RepoConsts.LABEL_SEPARATOR + tempValue;
					}					
				}
			}
			else{
				value = v.toString();
			}
		}		
		else if(dataType.equals(DataTypeDefinition.DATE.toString())){					
			
			value = propertyFormats.getDateFormat().format(v);
		}
		else if(dataType.equals(DataTypeDefinition.DATETIME.toString())){					
			
			value = propertyFormats.getDatetimeFormat().format(v);
		}
		else if(dataType.equals(DataTypeDefinition.MLTEXT.toString())){
			
			value = v.toString();
		}
		else if(dataType.equals(DataTypeDefinition.DOUBLE.toString())  || dataType.equals(DataTypeDefinition.FLOAT.toString())){
		
			if(propertyFormats.getDecimalFormat() != null){
				value = propertyFormats.getDecimalFormat().format(v);
			}
			else{
				value = v.toString();
			}
		}
		else{
			TypeConverter converter = new TypeConverter();			
			value = converter.convert(propertyDef.getDataType(), v).toString();			
		}
		
		return value;
	}

}
