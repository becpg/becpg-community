package fr.becpg.repo.product.extractor;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;

/**
 * Unit tests of the requirement control matching of {@link SimpleCharactListExtractor}.
 *
 * The services are stubbed with JDK proxies because the Mockito 1.9.5 inherited by this branch cannot
 * create mocks on JDK 17.
 */
public class SimpleCharactListExtractorTest {

	private static final String GET_PROPERTY = "getProperty";

	private static final String GET_TARGET_ASSOC = "getTargetAssoc";

	private static final String COUNTRY_CODE = "FR";

	private static final String OTHER_COUNTRY_CODE = "US";

	private final NodeRef ing = newNodeRef("ing");

	private final NodeRef otherIng = newNodeRef("otherIng");

	private final NodeRef country = newNodeRef("country");

	private final Map<List<Object>, Serializable> properties = new HashMap<>();

	private final Map<List<Object>, NodeRef> targetAssocs = new HashMap<>();

	private SimpleCharactListExtractor extractor;

	@Before
	public void setUp() {
		extractor = new SimpleCharactListExtractor();
		extractor.setNodeService(stub(NodeService.class, GET_PROPERTY, properties));
		extractor.setAssociationService(stub(AssociationService.class, GET_TARGET_ASSOC, targetAssocs));
		setProperty(country, PLMModel.PROP_REGULATORY_CODE, COUNTRY_CODE);
	}

	@Test
	public void matchingReqCtrlIsReturnedOnlyOnce() {
		NodeRef reqCtrl = newReqCtrlOn(ing);

		List<NodeRef> matching = extractor.findMatchingReqCtrls(newIngListItem(), Collections.singletonList(reqCtrl));

		assertEquals(Collections.singletonList(reqCtrl), matching);
	}

	@Test
	public void reqCtrlOnAnotherCharactIsIgnored() {
		NodeRef reqCtrl = newReqCtrlOn(otherIng);

		List<NodeRef> matching = extractor.findMatchingReqCtrls(newIngListItem(), Collections.singletonList(reqCtrl));

		assertEquals(Collections.emptyList(), matching);
	}

	@Test
	public void reqCtrlSourcedFromCharactIsReturned() {
		NodeRef reqCtrl = newReqCtrlOn(otherIng);
		setProperty(reqCtrl, PLMModel.PROP_RCL_SOURCES_V2, new ArrayList<>(Arrays.asList(ing)));

		List<NodeRef> matching = extractor.findMatchingReqCtrls(newIngListItem(), Collections.singletonList(reqCtrl));

		assertEquals(Collections.singletonList(reqCtrl), matching);
	}

	@Test
	public void regulatoryReqCtrlIsReturnedOnlyOnceForItsCountry() {
		NodeRef reqCtrl = newRegulatoryReqCtrl(COUNTRY_CODE);

		List<NodeRef> matching = extractor.findMatchingReqCtrls(newIngRegulatoryItem(), Collections.singletonList(reqCtrl));

		assertEquals(Collections.singletonList(reqCtrl), matching);
	}

	@Test
	public void regulatoryReqCtrlOfAnotherCountryIsIgnored() {
		NodeRef reqCtrl = newRegulatoryReqCtrl(OTHER_COUNTRY_CODE);

		List<NodeRef> matching = extractor.findMatchingReqCtrls(newIngRegulatoryItem(), Collections.singletonList(reqCtrl));

		assertEquals(Collections.emptyList(), matching);
	}

	private NodeRef newReqCtrlOn(NodeRef charact) {
		NodeRef reqCtrl = newNodeRef("reqCtrl-" + charact.getId());
		targetAssocs.put(Arrays.asList(reqCtrl, PLMModel.ASSOC_RCL_CHARACT), charact);
		return reqCtrl;
	}

	private NodeRef newRegulatoryReqCtrl(String regulatoryCode) {
		NodeRef reqCtrl = newReqCtrlOn(ing);
		setProperty(reqCtrl, PLMModel.PROP_REGULATORY_CODE, regulatoryCode);
		return reqCtrl;
	}

	private IngListDataItem newIngListItem() {
		IngListDataItem item = new IngListDataItem();
		item.setIng(ing);
		return item;
	}

	private IngRegulatoryListDataItem newIngRegulatoryItem() {
		IngRegulatoryListDataItem item = new IngRegulatoryListDataItem();
		item.setIng(ing);
		item.setRegulatoryCountries(Collections.singletonList(country));
		return item;
	}

	private void setProperty(NodeRef nodeRef, QName property, Serializable value) {
		properties.put(Arrays.asList(nodeRef, property), value);
	}

	/**
	 * Builds a service whose given method answers from a map keyed by its arguments; every other method returns null.
	 */
	private static <T> T stub(Class<T> serviceClass, String methodName, Map<List<Object>, ?> answers) {
		return serviceClass.cast(Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class<?>[] { serviceClass },
				(proxy, method, args) -> methodName.equals(method.getName()) ? answers.get(Arrays.asList(args)) : null));
	}

	private static NodeRef newNodeRef(String id) {
		return new NodeRef("workspace://SpacesStore/" + id);
	}
}
