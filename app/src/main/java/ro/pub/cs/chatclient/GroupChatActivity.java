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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class GroupChatActivity extends AppCompatActivity {

    private Button leaveGroup = null;
    private Button sendGroupMessage = null;
    private TextView groupChat = null;
    private EditText groupMessageText = null;
    private EditText groupConversation = null;
    private SimpleAdapter simpleAdpt = null;
    private List<Map<String, String>> inviteUserList = null;
    private ListView userListView = null;
    private Timer t = null;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void makeInviteFriendRequest(String myId, String groupId, String friendId) throws Exception {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestParams params = new RequestParams();
        params.put("groupId", groupId);
        params.put("friendId", friendId);
        params.put("inviterId", myId);
        AsyncRestClientService.post("inviteUser", params, new AsyncHttpResponseHandler() {
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void makeReceiveGroupMessagesRequest() throws Exception {

        RequestParams params = new RequestParams();
        params.add("groupId", GlobalModel.groupNameToGroupId.get(groupChat.getText()));
        SyncRestClientService.post("receiveGroupMessages", params, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers,
                                  java.lang.String responseString, java.lang.Throwable throwable) {
                Log.d("fail", "aaaa");
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

                        groupConversation.setText(conversation_);

                    }
                });
            }
        });
    }

    private void initFriendsList() {

        inviteUserList.add(createUser("user", "Ion"));
        inviteUserList.add(createUser("user", "Vasile"));
        inviteUserList.add(createUser("user", "Alina"));
        inviteUserList.add(createUser("user", "Ioana"));
        inviteUserList.add(createUser("user", "Bogdan"));
        inviteUserList.add(createUser("user", "Gigi"));
        inviteUserList.add(createUser("user", "Daria"));
    }

    private void createFriendsList() {

        for (String friend : GlobalModel.friendsUserNameToUserId.keySet()) {
            inviteUserList.add(createUser("user", friend));
        }
    }

    private HashMap<String, String> createUser(String key, String name) {
        HashMap<String, String> user = new HashMap<>();
        user.put(key, name);

        return user;
    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.leave_group:
                    setResult(RESULT_OK);
                    finish();
                    break;
                case R.id.send_group_message:
                    String message = GlobalModel.myName + ": " + groupMessageText.getText();
                    String groupId = GlobalModel.groupNameToGroupId.get(groupChat.getText());
                    String myId = GlobalModel.myUserId;

                    SendMessageBody smb = new SendMessageBody(myId, message, groupId);
                    try {
                        makeGroupSendMessageRequest(smb);
                    } catch (Exception e) {
                        Log.d("exceptie", e.toString());
                    }
                    groupMessageText.setText("");
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        leaveGroup = (Button) findViewById(R.id.leave_group);
        leaveGroup.setOnClickListener(new ClickListener());

        sendGroupMessage = (Button) findViewById(R.id.send_group_message);
        sendGroupMessage.setOnClickListener(new ClickListener());

        groupMessageText = (EditText) findViewById(R.id.group_message_text);

        groupConversation = (EditText) findViewById(R.id.group_conversation);

        groupChat = (TextView) findViewById(R.id.group_chat);
        Intent intent = getIntent();
        if (intent.getExtras().containsKey("name")) {
            groupChat.setText(intent.getStringExtra("name"));
        }

        userListView = (ListView) findViewById(R.id.invite_user_list);

        inviteUserList = new ArrayList<>();
        //initFriendsList();
        createFriendsList();
        simpleAdpt = new SimpleAdapter(this, inviteUserList, android.R.layout.simple_list_item_1, new String[] {"user"}, new int[] {android.R.id.text1});
        userListView.setAdapter(simpleAdpt);


        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    makeReceiveGroupMessagesRequest();
                } catch (Exception e) {
                    Log.d("exceptie", e.toString());
                }
            }
        }, 1000, 1000);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Map<String, String> entry = (Map<String, String>) userListView.getItemAtPosition(position);
                String name = entry.get("user");
                String friendId = GlobalModel.friendsUserNameToUserId.get(name);
                String groupId = GlobalModel.groupNameToGroupId.get(groupChat.getText());
                String myId = GlobalModel.myUserId;

                try {
                    makeInviteFriendRequest(myId, groupId, friendId);
                } catch (Exception e) {

                }
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void makeGroupSendMessageRequest(SendMessageBody smb) throws Exception {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestParams params = new RequestParams();
        params.put("message", smb.getMessage());
        params.put("senderId", smb.getSenderId());
        params.put("groupId", smb.getReceiverId());
        AsyncRestClientService.post("sendGroupMessage", params, new AsyncHttpResponseHandler() {
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

}
