package ro.pub.cs.chatclient;

/**
 * Created by stancioi on 5/1/2016.
 */
public class SendMessageBody {
    private String senderId;
    private String receiverId;
    private String message;

    public SendMessageBody(String senderId, String message, String receiverId) {
        this.senderId = senderId;
        this.message = message;
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
