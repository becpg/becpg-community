/*
 * 
 */
package fr.becpg.repo.importer.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.alfresco.encoding.CharactersetFinder;
import org.alfresco.encoding.GuessEncodingCharsetFinder;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.RepoConsts;
import fr.becpg.repo.importer.ImportContext;

// TODO: Auto-generated Javadoc
/**
 * Helper used by the import classes.
 *
 * @author querephi
 */
public class ImportHelper{

	/** The Constant MLTEXT_SEPARATOR. */
	public static final String MLTEXT_SEPARATOR 		= "_";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ImportHelper.class);
	
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
				
				// MLText
				if(dataType.isMatch(DataTypeDefinition.MLTEXT)){
					
					MLText mlText = new MLText();					
					
					// load translations
					boolean first = true;
					for(int z_idx=pos; z_idx<importContext.getColumns().size() ; z_idx++){
											
						// bcpg:ingMLName_en
						String transColumn = importContext.getColumns().get(z_idx).getId();
						if(transColumn != ""){
							
							String transLocalName = transColumn.contains(RepoConsts.MODEL_PREFIX_SEPARATOR) ? transColumn.split(RepoConsts.MODEL_PREFIX_SEPARATOR)[1] : null;			
							// default locale
							if(first){
								mlText.addValue(Locale.getDefault(), values.get(z_idx));
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
								pos = z_idx;
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
					if(qName.getLocalName().equals(ContentModel.PROP_NAME.getLocalName()))
						value = cleanName((String)value);
				}
				// Date
				else if(dataType.isMatch(DataTypeDefinition.DATE) || dataType.isMatch(DataTypeDefinition.DATETIME)){
					
					if(values.get(pos).isEmpty()){
						value = null;
					}
					else{												
						value = importContext.getPropertyFormats().getDateFormat().parse(values.get(pos));																		
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
										
					if(values.get(pos).isEmpty()){
						value = null;
					}
					else{
						Number n = importContext.getPropertyFormats().getDecimalFormat().parse(values.get(pos));
						value = n.doubleValue();
					}	
				}
				// float
				else if(dataType.isMatch(DataTypeDefinition.FLOAT)){
					
					if(values.get(pos).isEmpty()){
						value = null;
					}
					else{
						Number n = importContext.getPropertyFormats().getDecimalFormat().parse(values.get(pos));
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
	
	/**
	 * remove invalid characters.
	 *
	 * @param name the name
	 * @return the string
	 */
	public static String cleanName(String name) {
		/*(.*[\"\*\\\>\<\?\/\:\|]+.*)|(.*[\.]?.*[\.]+$)|(.*[ ]+$) */
		return name!=null? name.replaceAll("([\"*\\><?/:|])", "-").trim(): null;
	}	
	
	/**
	 * remove invalid characters (trim).
	 *
	 * @param value the value
	 * @return the string
	 */
	public static String cleanValue(String value) {		
		return value!=null? value.trim(): null;		
	}
	
	
	public static Charset guestCharset(InputStream is){
		CharactersetFinder finder = new GuessEncodingCharsetFinder();
		Charset charset = finder.detectCharset(is);
		if(charset==null){
			return Charset.forName("ISO-8859-15");
		}
		return charset;
	}
	
}
