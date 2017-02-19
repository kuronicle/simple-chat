package net.kuronicle.chat.form;

import java.util.List;

import lombok.Data;

@Data
public class ChatForm {

	public static String NAME_SYSTEM = "SYSTEM";

	public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private String date;

	private String name;

	private String message;

	private List<String> userList;

}
