package fr.becpg.web.experimental;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class BeCPGWSHandlerEndpointConfigurer extends Configurator {

    private static BeCPGWSHandler beCPGWSHandler = new BeCPGWSHandler();

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return (T)beCPGWSHandler;
    }
}
