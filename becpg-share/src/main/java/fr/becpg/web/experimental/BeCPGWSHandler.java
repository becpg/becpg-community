package fr.becpg.web.experimental;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@ServerEndpoint(value = "/becpgws/{store_type}/{store_id}/{id}/{user}",configurator=BeCPGWSHandlerEndpointConfigurer.class)
public class BeCPGWSHandler {

	private Set<Session> userSessions = Collections.synchronizedSet(new HashSet<Session>());
	
	private static Log logger = LogFactory.getLog(BeCPGWSHandler.class);

	@OnOpen
	public void onOpen(Session session, @PathParam("id") final String room, @PathParam("user") final String user) {
		logger.debug("Connected ... " + session.getId() + " to room " + room);

		session.getUserProperties().put("room", room);
		session.getUserProperties().put("user", user);
		
		userSessions.add(session);
		
		try {
			for (Session s : userSessions) {
				if (s.isOpen() && room.equals(s.getUserProperties().get("room")) && !s.getId().equals(session.getId())) {
					session.getBasicRemote().sendText("{\"type\":\"JOINING\",\"user\":\"" + s.getUserProperties().get("user")  + "\"}");
					s.getBasicRemote().sendText("{\"type\":\"JOINING\",\"user\":\"" + user  + "\"}");
				}
			}
		} catch (IOException e) {
			logger.error("onMessage failed", e);
		}

	}

	@OnMessage
	public void onMessage(String message, Session session) {

		logger.debug("Receiving ... " + session.getId());
		String room = (String) session.getUserProperties().get("room");
		try {
			for (Session s : userSessions) {
				if (s.isOpen() && room.equals(s.getUserProperties().get("room")) && !s.getId().equals(session.getId())) {
					s.getBasicRemote().sendText(message);
				}
			}
		} catch (IOException e) {
			logger.error("onMessage failed", e);
		}

	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		logger.debug(String.format("Session %s closed because of %s", session.getId(), closeReason));
		try {
			String room = (String) session.getUserProperties().get("room");
			String user = (String)session.getUserProperties().get("user");
			for (Session s : userSessions) {
				if (s.isOpen() && room.equals(s.getUserProperties().get("room")) && !s.getId().equals(session.getId())) {
					s.getBasicRemote().sendText("{\"type\":\"LEAVING\",\"user\":\"" +  user + "\"}");
				}
			}
		} catch (IOException e) {
			logger.error("onMessage failed", e);
		} finally {
			userSessions.remove(session);
		}
	}
	
	@OnError
	public void onError(Session session, Throwable thr) {
		if(!session.isOpen()) {
			userSessions.remove(session);
		}
		if(logger.isDebugEnabled()){
			logger.debug(thr,thr);
		}
	}

}
