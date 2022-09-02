package fr.becpg.repo.product.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>
 * IngListExtractor class.
 * </p>
 *
 * @author valentin
 * @version $Id: $Id
 */
public class SimpleCharactListExtractor extends SimpleExtractor {

	private static final List<String> APPLIED_LISTS = Arrays.asList("allergenList", "nutList", "physicoChemList", "labelClaimList");
	
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private static final Log logger = LogFactory.getLog(SimpleCharactListExtractor.class);
	
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,  FormatMode mode,
			Map<QName, Serializable> properties, final Map<String, Object> props, final Map<NodeRef, Map<String, Object>> cache) {

		return attributeExtractorService.extractNodeData(nodeRef, itemType, properties, metadataFields, mode,
				new AttributeExtractorService.DataListCallBack() {

					@Override
					public List<Map<String, Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field, FormatMode mode) {
						List<Map<String, Object>> ret = new ArrayList<>();
						if (field.isDataListItems()) {

							if (PLMModel.TYPE_REQCTRLLIST.equals(field.getFieldQname())) {

								try {
									NodeRef charact = null;

									RepositoryEntity item = alfrescoRepository.findOne(nodeRef);

									if (item instanceof SimpleCharactDataItem) {
										charact = ((SimpleCharactDataItem) item).getCharactNodeRef();
									} else if (item instanceof LabelClaimListDataItem) {
										charact = ((LabelClaimListDataItem) item).getLabelClaim();
									}

									NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityListDAO.getEntity(nodeRef));
									NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, field.getFieldQname());

									if (listNodeRef != null) {
										List<NodeRef> results = entityListDAO.getListItems(listNodeRef, field.getFieldQname());

										for (NodeRef itemNodeRef : results) {

											if ((charact != null)
													&& charact.equals(associationService.getTargetAssoc(itemNodeRef, PLMModel.ASSOC_RCL_CHARACT))
													|| associationService.getTargetAssocs(itemNodeRef, PLMModel.ASSOC_RCL_SOURCES)
															.contains(charact)) {
												addExtracted(itemNodeRef, field, cache, mode, ret);
											}
										}
									}
								} catch (StackOverflowError e) {
									logger.debug("Infinity loop : " + nodeRef, e);
								}

							} else {
								NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
								NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, field.getFieldQname());
								if (listNodeRef != null) {
									List<NodeRef> results = entityListDAO.getListItems(listNodeRef, field.getFieldQname());

									for (NodeRef itemNodeRef : results) {
										addExtracted(itemNodeRef, field, cache, mode, ret);
									}
								}
							}

						} else if (field.isEntityField()) {
							NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
							addExtracted(entityNodeRef, field, cache, mode, ret);

						} else {

							if (field.getFieldDef() instanceof AssociationDefinition) {
								List<NodeRef> assocRefs;
								if (((AssociationDefinition) field.getFieldDef()).isChild()) {
									assocRefs = associationService.getChildAssocs(nodeRef, field.getFieldDef().getName());
								} else {
									assocRefs = associationService.getTargetAssocs(nodeRef, field.getFieldDef().getName());
								}
								for (NodeRef itemNodeRef : assocRefs) {
									addExtracted(itemNodeRef, field, cache, mode, ret);
								}

							}
						}

						return ret;
					}

					private void addExtracted(NodeRef itemNodeRef, AttributeExtractorStructure field, Map<NodeRef, Map<String, Object>> cache,
							FormatMode mode, List<Map<String, Object>> ret) {
						if (cache.containsKey(itemNodeRef)) {
							ret.add(cache.get(itemNodeRef));
						} else {
							if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
								if (FormatMode.CSV.equals(mode) || FormatMode.XLSX.equals(mode)) {
									ret.add(extractExport(mode, itemNodeRef, field.getChildrens(), props, cache));
								} else {
									ret.add(extractJSON(itemNodeRef, field.getChildrens(), props, cache));
								}
							}
						}
					}

				});
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return (dataListFilter.getDataListName() != null) && APPLIED_LISTS.contains(dataListFilter.getDataListName());
	}

}
