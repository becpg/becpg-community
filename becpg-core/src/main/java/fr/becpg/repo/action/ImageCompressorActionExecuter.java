/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.action;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.magick.ImageTransformationOptions;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Action used to compress, transform an image
 *
 * @author querephi
 */
public class ImageCompressorActionExecuter extends ActionExecuterAbstractBase{
	
    private static final String CONTENT_READER_NOT_FOUND_MESSAGE = "Can not find Content Reader for document. Operation can't be performed";
    private static final String TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN = "Transformer for '%s' source mime type and '%s' target mime type was not found. Operation can't be performed";

	/** The Constant NAME. */
	public static final String NAME = "compress-image";
	public static final String PARAM_CONVERT_COMMAND = "convert-command";
	
	/** The logger. */
	private static final Log logger = LogFactory.getLog(ImageCompressorActionExecuter.class);
	
	private ContentService contentService;
	private ContentTransformer imageMagickContentTransformer;
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setImageMagickContentTransformer(
			ContentTransformer imageMagickContentTransformer) {
		this.imageMagickContentTransformer = imageMagickContentTransformer;
	}
	
	/**
	 * Add parameter definitions
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
		paramList.add(new ParameterDefinitionImpl(PARAM_CONVERT_COMMAND, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_CONVERT_COMMAND)));
	}	

	@Override
	protected void executeImpl(Action action, NodeRef nodeRef) {
				
        ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT); 	
        if (null == contentReader || !contentReader.exists())
        {
            throw new RuleServiceException(CONTENT_READER_NOT_FOUND_MESSAGE);
        }
        if (null == contentService.getTransformer(contentReader.getMimetype(), contentReader.getMimetype()))
        {
            throw new RuleServiceException(String.format(TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN, contentReader.getMimetype(), contentReader.getMimetype()));
        }        
        ContentWriter contentWriter  = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        
        // options
        String convertCommand = (String) action.getParameterValue(PARAM_CONVERT_COMMAND);
        ImageTransformationOptions imageOptions = new ImageTransformationOptions();
        imageOptions.setCommandOptions(convertCommand);
        
        // check if the transformer is going to work, i.e. is available
        if (!this.imageMagickContentTransformer.isTransformable(contentReader.getMimetype(),contentReader.getSize(), contentWriter.getMimetype(), imageOptions)){
            throw new NoTransformerException(contentReader.getMimetype(), contentWriter.getMimetype());
        }
        
    	imageMagickContentTransformer.transform(contentReader, contentWriter, imageOptions);
    	if(logger.isDebugEnabled()){
    		logger.debug("Image compressor. convertCommand: " + convertCommand + " - initialSize: " + contentReader.getSize() + " - afterSize: " + contentWriter.getSize());
    	}    			
	}

}
