package ro.pub.cs.chatclient;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class ChatActivity extends AppCompatActivity {

    private Button backButton = null;
    private TextView chatWith = null;
    private Timer t = null;
    private Button sendMessage = null;
    private String chatPartnerName;
    private EditText messageBox = null;
    private EditText conversationBox = null;
    private Button publishMessage = null;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void makeReceiveMessagesRequest() throws Exception {

        RequestParams params = new RequestParams();
        String chatPartnerId = GlobalModel.friendsUserNameToUserId.get(chatPartnerName);
        params.add("chatPartnerId", chatPartnerId);
        params.add("myId", GlobalModel.myUserId);
        SyncRestClientService.post("receiveMessages", params, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers,
                                  java.lang.String responseString, java.lang.Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject messagesJson) {
                // If the response is JSONObject instead of expected JSONArray
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray messagesJson) {
                ObjectMapper mapper = new ObjectMapper();
                String[] messages = null;
                try {
                    messages = mapper.readValue(messagesJson.toString(), String[].class);
                } catch (Exception e) {
                }
                String conversation = "";
                for (String message : messages) {
                    conversation += message + '\n';
                }

                final String conversation_ = conversation;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        conversationBox.setText(conversation_);

                    }
                });
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void makeSendMessageRequest(SendMessageBody smb) throws Exception {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestParams params = new RequestParams();
        params.put("message", smb.getMessage());
        params.put("senderId", smb.getSenderId());
        params.put("receiverId", smb.getReceiverId());
        AsyncRestClientService.post("sendMessage", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String sc = String.valueOf(statusCode);
                Log.d("statusCode", sc);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String sc = String.valueOf(statusCode);
                Log.d("statusCode", sc);
            }
        });
    }

    private void postMessageToWall() {
        LoginActivity.loginButton.clearPermissions();
        LoginActivity.loginButton.setPublishPermissions("publish_actions");
        Bundle params = new Bundle();
        params.putString("message", "This is a test message");
        /* make the API call */
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
            /* handle the result */
                    }
                }
        ).executeAsync();
    }


    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.back:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case R.id.sent_message_button:
                    String senderId = GlobalModel.myUserId;
                    String receiverId = GlobalModel.friendsUserNameToUserId.get(chatPartnerName);
                    String message = GlobalModel.myName + ": " + messageBox.getText();
                    try {
                        SendMessageBody smb = new SendMessageBody(senderId, message, receiverId);
                        makeSendMessageRequest(smb);
                    } catch (Exception e) {
                        Log.d("exceptie", e.toString());
                    }
                    messageBox.setText("");
                    break;
                case R.id.private_message_edit_text:
                    messageBox.setText("");
                    break;
                case R.id.publish_discussion_to_wall_button:
                    postMessageToWall();
                    break;

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ClickListener cl = new ClickListener();

        backButton = (Button)findViewById(R.id.back);
        backButton.setOnClickListener(cl);

        sendMessage = (Button) findViewById(R.id.sent_message_button);
        sendMessage.setOnClickListener(cl);

        chatWith = (TextView)findViewById(R.id.chat_with);

        messageBox = (EditText)findViewById(R.id.private_message_edit_text);
        messageBox.setOnClickListener(new ClickListener());

        conversationBox = (EditText)findViewById(R.id.messages_edit_text);
        conversationBox.setOnClickListener(new ClickListener());

        Intent intent = getIntent();

        if (intent.getExtras().containsKey("name")) {
            chatPartnerName = intent.getStringExtra("name");
            chatWith.setText("Chat with " + chatPartnerName);
        }

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    makeReceiveMessagesRequest();
                } catch (Exception e) {
                    Log.d("exceptie", e.toString());
                }
            }
        }, 1000, 1000);

        publishMessage = (Button) findViewById(R.id.publish_discussion_to_wall_button);
        publishMessage.setOnClickListener(new ClickListener());

    }

}
