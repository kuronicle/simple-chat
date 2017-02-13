package net.kuronicle.chat.listener;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.extern.slf4j.Slf4j;
import net.kuronicle.chat.form.ChatForm;

@Component
@Slf4j
public class WebSocketEventListener {

	private SimpMessagingTemplate template;

	/**
	 * 接続ユーザ一覧
	 */
	private Map<String, String> sessionUserMap = new HashMap<String, String>();

	@Autowired
	public WebSocketEventListener(SimpMessagingTemplate template) {
		this.template = template;
	}

	@EventListener
	public void handleSessionConnectedEvent(SessionConnectedEvent event) {

		Message<byte[]> message = event.getMessage();
		if (message != null) {
			MessageHeaders headers = message.getHeaders();
			if (headers != null) {
				log.debug("MessageHeaders:{}", headers.toString());
			}

			byte[] payload = message.getPayload();
			if (payload != null) {
				log.debug("Message:{}", new String(payload));
			}
		}

		Principal user = event.getUser();
		if (user != null) {
			log.debug("User name:{}", user.getName());
		}

		// 接続ユーザを接続ユーザ一覧に追加する。
		String login = getLogin(event);
		String sessionId = getSimpSessionId(event);
		if (sessionId != null) {
			sessionUserMap.put(sessionId, login);
		}

		long timestamp = event.getTimestamp();
		log.debug("Event time:{}", new Date(timestamp).toString());

		ChatForm chatForm = new ChatForm();
		chatForm.setName("SYSTEM");
		chatForm.setDate(new Date(timestamp).toString());
		chatForm.setMessage(login + " in this room.");
		log.info(login + " in this room.");
		this.template.convertAndSend("/topic/messages", chatForm);
	}

	private String getSimpSessionId(SessionConnectedEvent event) {
		Message<byte[]> message = event.getMessage();

		if (message == null) {
			return null;
		}

		MessageHeaders messageHeaders = message.getHeaders();
		if (messageHeaders == null) {
			return null;
		}

		String sessionId = (String) messageHeaders.get("simpSessionId");

		return sessionId;
	}

	@SuppressWarnings("unchecked")
	private String getLogin(SessionConnectedEvent event) {
		Message<byte[]> message = event.getMessage();

		if (message == null) {
			return null;
		}

		MessageHeaders messageHeaders = message.getHeaders();
		if (messageHeaders == null) {
			return null;
		}

		GenericMessage<?> genericMessage = messageHeaders.get("simpConnectMessage", GenericMessage.class);
		if (genericMessage == null) {
			return null;
		}

		MessageHeaders genericMessageHeaders = genericMessage.getHeaders();
		if (genericMessageHeaders == null) {
			return null;
		}

		Map<String, Object> nativeHeaders = (Map<String, Object>) genericMessageHeaders.get("nativeHeaders");
		if (nativeHeaders == null) {
			return null;
		}

		List<String> loginList = (List<String>) nativeHeaders.get("login");
		String login = null;
		if (loginList != null && !loginList.isEmpty()) {
			login = loginList.get(0);
		}

		return login;
	}

	@EventListener
	public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
		CloseStatus closeStatus = event.getCloseStatus();
		if (closeStatus != null) {
			log.debug("CloseStatus: code={}, reason={}", closeStatus.getCode(), closeStatus.getReason());
		}

		Message<byte[]> message = event.getMessage();
		if (message != null) {
			MessageHeaders headers = message.getHeaders();
			if (headers != null) {
				log.debug("MessageHeaders:{}", headers.toString());
			}

			byte[] payload = message.getPayload();
			if (payload != null) {
				log.debug("Message:{}", new String(payload));
			}
		}

		Principal user = event.getUser();
		if (user != null) {
			log.debug("User name:{}", user.getName());
		}

		// セッションIDを取得する。
		String sessionId = event.getSessionId();
		log.debug("SessionId:{}", sessionId);
		String login = null;
		if (sessionId != null) {
			login = sessionUserMap.remove(sessionId);
		}

		long timestamp = event.getTimestamp();
		log.debug("Event time:{}", new Date(timestamp).toString());

		ChatForm chatForm = new ChatForm();
		chatForm.setName("SYSTEM");
		chatForm.setDate(new Date(timestamp).toString());
		chatForm.setMessage(login + " out this room.");
		log.info(login + " out this room.");
		this.template.convertAndSend("/topic/messages", chatForm);
	}
}