package net.kuronicle.chat.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ConnectedUserManager {

	private Map<String, String> userMap = new HashMap<String, String>();

	public void putUser(String sessionId, String userName) {
		userMap.put(sessionId, userName);
	}

	public String removeUser(String sessionId) {
		return userMap.remove(sessionId);
	}

	public List<String> getUserList() {
		return new ArrayList<String>(userMap.values());
	}
}
