package fr.becpg.repo.product.formulation;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.expression.AccessException;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.security.aop.SecurityMethodBeforeAdvice;

public class FormulaService {
	private SecurityMethodBeforeAdvice securityMethodBeforeAdvice;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	public void setSecurityMethodBeforeAdvice(SecurityMethodBeforeAdvice securityMethodBeforeAdvice) {
		this.securityMethodBeforeAdvice = securityMethodBeforeAdvice;
	}


	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}


	public void registerCustomFunctions(StandardEvaluationContext context) {
		context.setBeanResolver(new BeanResolver() {

			SpelHelperFonctions spelHelperFonctions = new SpelHelperFonctions();

			@Override
			public Object resolve(EvaluationContext context, String beanName) throws AccessException {
				if (beanName.equals("beCPG")) {
					return spelHelperFonctions;
				}
				return null;
			}
		});
	}

	private ProductData createSecurityProxy(ProductData productData) {
		ProxyFactory factory = new ProxyFactory();
		factory.setTarget(productData);
		factory.addAdvice(securityMethodBeforeAdvice);
		return (ProductData) factory.getProxy();

	}

	public class SpelHelperFonctions {
		public ProductData findOne(NodeRef nodeRef) {
			return createSecurityProxy(alfrescoRepository.findOne(nodeRef));
		}

		public Serializable propValue(NodeRef nodeRef, String qname) {
			return nodeService.getProperty(nodeRef, QName.createQName(qname, namespaceService));
		}

		public QName getQName(String qName){
			return QName.createQName(qName, namespaceService);
		}
	}

	public StandardEvaluationContext createEvaluationContext(ProductData productData) {
		StandardEvaluationContext context = new StandardEvaluationContext(createSecurityProxy(productData));

		registerCustomFunctions(context);

		return context;
	}
}
