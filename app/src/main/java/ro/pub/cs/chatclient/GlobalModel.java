package ro.pub.cs.chatclient;

import java.util.HashMap;

/**
 * Created by stancioi on 4/30/2016.
 */
public class GlobalModel {

    public static HashMap<String, String> friendsUserIdToUserName = new HashMap<>();
    public static HashMap<String, String> friendsUserNameToUserId = new HashMap<>();
    public static HashMap<String, String> groupIdToGroupName = new HashMap<>();
    public static HashMap<String, String> groupNameToGroupId = new HashMap<>();
    public static String myUserId;
    public static String myName;
}
