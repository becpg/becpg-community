/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.publishing;

import java.util.Set;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Fix alfresco bug when mimetype is NULL
 * @author matthieu
 */
public class ChannelHelper extends org.alfresco.repo.publishing.ChannelHelper{

    private DictionaryService dictionaryService;
    private FileFolderService fileFolderService;
  
    
	

    public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public boolean canPublish(NodeRef nodeToPublish, ChannelType type)
    {
        if (type.canPublish() == false)
        {
            return false;
        }
        FileInfo file = fileFolderService.getFileInfo(nodeToPublish);
        ContentData contentData = file.getContentData();
        String mimetype = contentData == null ? null : contentData.getMimetype();
        boolean isContentTypeSupported = isContentTypeSupported(file.getType(), type);
        boolean isMimetypeSupported = isMimetypeSupported(mimetype, type);
        return isContentTypeSupported && isMimetypeSupported;
    }

    private boolean isMimetypeSupported(String mimetype, ChannelType type)
    {
        Set<String> supportedMimetypes = type.getSupportedMimeTypes();
        if (mimetype ==null || supportedMimetypes == null || supportedMimetypes.isEmpty())
        {
            return true;
        }
        return supportedMimetypes.contains(mimetype);
    }

    private boolean isContentTypeSupported(QName contentType, ChannelType type)
    {
        Set<QName> supportedContentTypes = type.getSupportedContentTypes();
        if (supportedContentTypes == null || supportedContentTypes.isEmpty())
        {
            return true;
        }
        for (QName supportedType : supportedContentTypes)
        {
            if (contentType.equals(supportedType) 
                    || dictionaryService.isSubClass(contentType, supportedType))
            {
                return true;
            }
        }
        return false;
    }
	
}
