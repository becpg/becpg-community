package fr.becpg.repo.system.impl;

import java.io.Serializable;
import java.util.Optional;

import org.alfresco.service.cmr.attributes.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.stereotype.Service;

import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>SystemConfigurationServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("systemConfigurationService")
public class SystemConfigurationServiceImpl implements SystemConfigurationService {
	
	private static final String CACHE_KEY = SystemConfigurationService.class.getName();
	
	@Autowired
	private PropertySourcesPlaceholderConfigurer[] resolvers;
	
	@Autowired
	private BeCPGCacheService beCPGCacheService;
	
	@Autowired
	private AttributeService attributeService;
	

	/** {@inheritDoc} */
	@Override
	public String confValue(String propKey) {
	    Optional<String> nullableString = beCPGCacheService.getFromCache(CACHE_KEY, propKey, () -> {
	        Serializable ret = attributeService.getAttribute(propKey);
	        if (ret != null) {
	            return Optional.of((String) ret);
	        }

	        for (PropertySourcesPlaceholderConfigurer source : resolvers) {
	            PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(source.getAppliedPropertySources());
	            
	            String val = resolver.getProperty(propKey);
	            if (val != null) {
	                return Optional.of(val);
	            }
	        }

	        return Optional.empty();
	    });
	    
	    return nullableString.orElse(null);
	}

	/** {@inheritDoc} */
	@Override
	public void updateConfValue(String propKey, String value) {
		attributeService.setAttribute(value, propKey);
		beCPGCacheService.clearCache(CACHE_KEY);
		MLTextHelper.flushCache();
	}

	/** {@inheritDoc} */
	@Override
	public void resetConfValue(String propKey) {
		attributeService.removeAttribute(propKey);
		beCPGCacheService.clearCache(CACHE_KEY);
		MLTextHelper.flushCache();
	}

}
