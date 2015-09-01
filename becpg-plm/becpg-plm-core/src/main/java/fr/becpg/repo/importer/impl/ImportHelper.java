/*
 * 
 */
package fr.becpg.repo.importer.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;

import org.alfresco.encoding.CharactersetFinder;
import org.alfresco.encoding.GuessEncodingCharsetFinder;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.importer.ImportContext;

/**
 * Helper used by the import classes.
 *
 * @author querephi
 */
public class ImportHelper{

	/** The Constant MLTEXT_SEPARATOR. */
	public static final String MLTEXT_SEPARATOR 		= "_";

	public static final String NULL_VALUE = "NULL";
	
	
	
	
	/**
	 * Load the property according to the property type.
	 *
	 * @param importContext the import context
	 * @param values the values
	 * @param pos the pos
	 * @return the serializable
	 * @throws ParseException the parse exception
	 */
	 public static Serializable loadPropertyValue(ImportContext importContext, List<String> values, int pos) throws ParseException {
	
		Serializable value = null;		
		ClassAttributeDefinition attribute = importContext.getColumns().get(pos).getAttribute();
		
		if(attribute != null){			
			
			if(attribute instanceof PropertyDefinition){
				
				
				PropertyDefinition propertyDef = (PropertyDefinition)attribute;
				QName qName = propertyDef.getName();
				QName dataType =propertyDef.getDataType().getName();				
				
				if(NULL_VALUE.equalsIgnoreCase(values.get(pos))){
					return NULL_VALUE;
				}
				
				// MLText
				if(dataType.isMatch(DataTypeDefinition.MLTEXT)){
					
					MLText mlText = new MLText();
					
					// load translations
					boolean first = true;
					for(int z_idx=pos; z_idx<importContext.getColumns().size() ; z_idx++){
											
						// bcpg:legalName_en
						String transColumn = importContext.getColumns().get(z_idx).getId();
						if(!Objects.equals(transColumn, "")){
							
							String transLocalName = transColumn.contains(RepoConsts.MODEL_PREFIX_SEPARATOR) ? transColumn.split(RepoConsts.MODEL_PREFIX_SEPARATOR)[1] : null;			
							// default locale
							if(first){								
								mlText.addValue(I18NUtil.getContentLocaleLang(), values.get(z_idx));
								first = false;
							}
							// other locales
							else if(transLocalName != null && transLocalName.startsWith(qName.getLocalName() + MLTEXT_SEPARATOR)){
																				
								String strLocale = transLocalName.replace(qName.getLocalName() + MLTEXT_SEPARATOR, "");
								Locale locale = new Locale(strLocale);
								mlText.addValue(locale, values.get(z_idx));
							}
							else{
								// the translation is finished
								break;
							}
						}						
					}
					
					value = mlText;
				}
				// Text
				else if(dataType.isMatch(DataTypeDefinition.TEXT)){
					
					
					
					if (propertyDef.isMultiValued())
                	{
                        // Multi-valued property
                		value = new ArrayList<Serializable>(Arrays.asList((values.get(pos)).split(RepoConsts.MULTI_VALUES_SEPARATOR)));
                	}
                	else
                	{
                	    // Single value property
                		value = values.get(pos);
                	}
					
					// clean name
					if(qName.getLocalName().equals(ContentModel.PROP_NAME.getLocalName())){
						value = PropertiesHelper.cleanName((String)value);
					}
				}
				// Date
				else if(dataType.isMatch(DataTypeDefinition.DATE) || dataType.isMatch(DataTypeDefinition.DATETIME)){
					
					if(values.get(pos).isEmpty()){
						value = null;
					}
					else{												
						value = importContext.getPropertyFormats().parseDate(values.get(pos));																		
					}										
				}
				// int, long
				else if(dataType.isMatch(DataTypeDefinition.INT) || dataType.isMatch(DataTypeDefinition.LONG)){
										
					if(values.get(pos).isEmpty()){
						value = null;
					}
					else{
						value = values.get(pos);
					}	
				}
				// double
				else if(dataType.isMatch(DataTypeDefinition.DOUBLE)){
										
					if(values.get(pos).trim().isEmpty()){
						value = null;
					}
					else{
						String val = values.get(pos);
						if( importContext.getPropertyFormats().getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator() == ','){
							val = val.replaceAll("\\.",",");
						} else {
							val = val.replaceAll(",",".");
						}

						Number n = importContext.getPropertyFormats().parseDecimal(val);						
						value = n.doubleValue();
					}	
				}
				// float
				else if(dataType.isMatch(DataTypeDefinition.FLOAT)){
					
					if(values.get(pos).trim().isEmpty()){
						value = null;
					}
					else{
						String val = values.get(pos);
						if( importContext.getPropertyFormats().getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator() == ','){
							val = val.replaceAll("\\.",",");
						} else {
							val = val.replaceAll(",",".");
						}
						Number n = importContext.getPropertyFormats().parseDecimal(val);
						value = n.floatValue();
					}	
				}			
				else{
					value = values.get(pos);
				}
			}			
		}		
		 
		return value;		
	 }
	
	



	public static Charset guestCharset(InputStream is, String readerCharset){
		Charset defaultCharset = Charset.forName(RepoConsts.ISO_CHARSET);
		if(RepoConsts.ISO_CHARSET.equals(readerCharset)){
			return defaultCharset;
		}
		CharactersetFinder finder = new GuessEncodingCharsetFinder();
		Charset charset = finder.detectCharset(is);
		if(charset==null){
			return defaultCharset;
		}
		return charset;
	}

	public static Map<QName, Serializable> cleanProperties(Map<QName, Serializable> properties) {
		for (Iterator<Map.Entry<QName, Serializable>> iterator = properties.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<QName, Serializable> entry = iterator.next();
			if(entry.getValue()!=null && ImportHelper.NULL_VALUE.equals(entry.getValue())){
				 iterator.remove();
			 }
		}
		
		return properties;
	}

}
