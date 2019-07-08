package fr.becpg.repo.report.engine;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;


/**
 * 
Override ou ContentReader
ExporterComponent
 * @author matthieu
 *
 */
public class ReportContentServiceWrapper implements ContentService {

	private ContentService contentService;
	
	@Override
	public long getStoreTotalSpace() {
		return 	contentService.getStoreTotalSpace();
	}

	@Override
	public long getStoreFreeSpace() {
		return contentService.getStoreFreeSpace();
	}

	@Override
	public ContentReader getRawReader(String contentUrl) {
		return contentService.getRawReader(contentUrl);
	}

	@Override
	public ContentReader getReader(NodeRef nodeRef, QName propertyQName) throws InvalidNodeRefException, InvalidTypeException {
		return contentService.getReader(nodeRef, propertyQName);
	}

	@Override
	public ContentWriter getWriter(NodeRef nodeRef, QName propertyQName, boolean update) throws InvalidNodeRefException, InvalidTypeException {
		return contentService.getWriter(nodeRef, propertyQName, update);
	}

	@Override
	public ContentWriter getTempWriter() {
		return contentService.getTempWriter();
	}

	@Override
	public void transform(ContentReader reader, ContentWriter writer) throws NoTransformerException, ContentIOException {
		contentService.transform(reader, writer);
		
	}

	@Override
	public void transform(ContentReader reader, ContentWriter writer, Map<String, Object> options) throws NoTransformerException, ContentIOException {
		contentService.transform(reader, writer,options);
		
	}

	@Override
	public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options)
			throws NoTransformerException, ContentIOException {
		contentService.transform(reader, writer, options);
		
	}

	@Override
	public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype) {

		return contentService.getTransformer(sourceMimetype, targetMimetype);
	}

	@Override
	public List<ContentTransformer> getTransformers(String sourceUrl, String sourceMimetype, long sourceSize, String targetMimetype,
			TransformationOptions options) {
		return contentService.getTransformers(sourceUrl, sourceMimetype, sourceSize, targetMimetype, options);
	}

	@Override
	public ContentTransformer getTransformer(String sourceUrl, String sourceMimetype, long sourceSize, String targetMimetype,
			TransformationOptions options) {
		return contentService.getTransformer(sourceUrl, sourceMimetype, sourceSize, targetMimetype, options);
	}

	@Override
	public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype, TransformationOptions options) {
		return contentService.getTransformer(sourceMimetype, targetMimetype,options);
	}

	@Override
	public long getMaxSourceSizeBytes(String sourceMimetype, String targetMimetype, TransformationOptions options) {
		return contentService.getMaxSourceSizeBytes(sourceMimetype, targetMimetype, options);
	}

	@Override
	public List<ContentTransformer> getActiveTransformers(String sourceMimetype, long sourceSize, String targetMimetype,
			TransformationOptions options) {
		return contentService.getActiveTransformers(sourceMimetype, sourceSize, targetMimetype, options);
	}

	@Override
	public List<ContentTransformer> getActiveTransformers(String sourceMimetype, String targetMimetype, TransformationOptions options) {
		return contentService.getActiveTransformers(sourceMimetype, targetMimetype, options);
	}

	@Override
	public ContentTransformer getImageTransformer() {
		return contentService.getImageTransformer();
	}

	@Override
	public boolean isTransformable(ContentReader reader, ContentWriter writer) {
		return contentService.isTransformable(reader, writer);
	}

	@Override
	public boolean isTransformable(ContentReader reader, ContentWriter writer, TransformationOptions options) {
		return contentService.isTransformable(reader, writer, options);
	}

}
