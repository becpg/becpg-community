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
