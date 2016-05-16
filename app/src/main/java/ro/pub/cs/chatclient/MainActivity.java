package ro.pub.cs.chatclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CHAT_ACTIVITY = 1;

    private ListView friendsListView = null;
    private ListView groupListView = null;
    private EditText notificationsEditText = null;
    private SimpleAdapter simpleAdpt = null;
    private SimpleAdapter simpleAdpt2 = null;
    private Button chatButton = null;
    private Button createGroup = null;
    private EditText groupName = null;
    private Timer t = null;
    private Timer t2 = null;
    private Handler updateListView;

    private List<Map<String, String>> friendsList = null;
    private List<Map<String, String>> groupList = null;

    private Group[] groups = null;

    private void makeNotificationsRequest() {

        RequestParams params = new RequestParams();
        params.add("id", GlobalModel.myUserId);
        SyncRestClientService.post("receiveNotifications", params, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers,
                                  java.lang.String responseString, java.lang.Throwable throwable) {
                Log.d("eroare", "eroare");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject messagesJson) {
                // If the response is JSONObject instead of expected JSONArray
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray notificationsJson) {
                List<Invite> invites = new ArrayList<>();

                for (int i = 0; i < notificationsJson.length(); i++) {
                    try {
                        JSONObject notification = notificationsJson.getJSONObject(i);
                        String inviterId = notification.getString("inviterId");
                        String groupId = notification.getString("groupId");
                        Invite invite = new Invite(inviterId, groupId);
                        invites.add(invite);
                    } catch (JSONException e) {
                    }
                }
                String notifications = "";
                for (Invite invite : invites) {
                    String inviterName = GlobalModel.friendsUserIdToUserName.get(invite.getInviterId());
                    String groupName = GlobalModel.groupIdToGroupName.get(invite.getGroupId());
                    notifications += "User " + inviterName + " invited you to join group " + groupName + "." + '\n';
                }

                final String notifications_ = notifications;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        notificationsEditText.setText(notifications_);

                    }
                });
            }
        });
    }

    private void makeRequestCreateGroup() {

        RequestParams params = new RequestParams();
        final String createGroupName = groupName.getText().toString();
        params.add("groupName", createGroupName);

        AsyncRestClientService.post("createGroup", params, new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers,
                                  java.lang.String responseString, java.lang.Throwable throwable) {
                Log.d("fdsfd", responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject groupJson) {
            }

        });
    }

    private void makeReceiveGroupsRequest() {
        SyncRestClientService.get("receiveGroups", new RequestParams(), new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers,
                                  java.lang.String responseString, java.lang.Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject messagesJson) {
                // If the response is JSONObject instead of expected JSONArray
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray groupNames) {

                List<Group> groups = new ArrayList<>();
                for(int i = 0; i < groupNames.length(); i++)
                {
                    try {
                        JSONObject gr = groupNames.getJSONObject(i);
                        Group group = new Group(gr.get("id").toString(), gr.get("name").toString());
                        groups.add(group);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                for (Group group : groups) {
                    if (!GlobalModel.groupIdToGroupName.containsKey(group.getId())) {
                        GlobalModel.groupIdToGroupName.put(group.getId(), group.getName());
                        GlobalModel.groupNameToGroupId.put(group.getName(), group.getId());
                    }
                }

                groupList.clear();
                for (Group group : groups) {
                    groupList.add(createGroup("group", group.getName()));
                }
            }
        });
    }

    private void initFriendsList() {

        friendsList.add(createFriend("friend", "Ion"));
        friendsList.add(createFriend("friend", "Vasile"));
        friendsList.add(createFriend("friend", "Alina"));
        friendsList.add(createFriend("friend", "Ioana"));
        friendsList.add(createFriend("friend", "Bogdan"));
        friendsList.add(createFriend("friend", "Gigi"));
        friendsList.add(createFriend("friend", "Daria"));
    }

    private void initGroupsList() {
        groupList.add(createGroup("group", "Facultate"));
        groupList.add(createGroup("group", "Regie"));
        groupList.add(createGroup("group", "Golanii din cartier"));
        groupList.add(createGroup("group", "Grupa 332CC"));
        groupList.add(createGroup("group", "Grupa 341C5"));
        groupList.add(createGroup("group", "Grupa 322CC"));
    }

    private HashMap<String, String> createFriend(String key, String name) {
        HashMap<String, String> friend = new HashMap<>();
        friend.put(key, name);

        return friend;
    }

    private HashMap<String, String> createGroup(String key, String name) {
        HashMap<String, String> group = new HashMap<>();
        group.put(key, name);

        return group;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        friendsListView = (ListView)findViewById(R.id.friend_list_view);
        groupListView = (ListView)findViewById(R.id.chat_group_list_view);
        notificationsEditText = (EditText)findViewById(R.id.notifications_edit_text);

        friendsList = new ArrayList<>();

        //initFriendsList();

        for (String friend : GlobalModel.friendsUserIdToUserName.values()) {
            friendsList.add(createFriend("friend", friend));
        }
        simpleAdpt = new SimpleAdapter(this, friendsList, android.R.layout.simple_list_item_1, new String[] {"friend"}, new int[] {android.R.id.text1});
        friendsListView.setAdapter(simpleAdpt);

        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                Map<String, String> entry = (Map<String, String>) friendsListView.getItemAtPosition(position);
                intent.putExtra("name", entry.get("friend"));
                startActivity(intent);
            }
        });

        createGroup = (Button)findViewById(R.id.create_group);
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRequestCreateGroup();
            }
        });

        notificationsEditText = (EditText) findViewById(R.id.notifications_edit_text);

        groupName = (EditText)findViewById(R.id.group_name);

        groupList = new ArrayList<>();
        //initGroupsList();
        simpleAdpt2 = new SimpleAdapter(this, groupList, android.R.layout.simple_list_item_1, new String[] {"group"}, new int[] {android.R.id.text1});
        groupListView.setAdapter(simpleAdpt2);

        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(MainActivity.this, GroupChatActivity.class);
                Map<String, String> entry = (Map<String, String>) groupListView.getItemAtPosition(position);
                intent.putExtra("name", entry.get("group"));
                startActivity(intent);
            }
        });

        updateListView = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        simpleAdpt2.notifyDataSetChanged();
                }
                super.handleMessage(msg);
            }
        };

        t = new Timer();
        final TimerTask timerTask = new TimerTask() {
            public void run() {
                try {
                    makeReceiveGroupsRequest();

                    Message msg;
                    msg = Message.obtain();
                    msg.what = 1;
                    updateListView.sendMessage(msg);

                } catch (Exception e) {
                    Log.d("exceptie", e.toString());
                }
            }
        };
        t.scheduleAtFixedRate(timerTask, 1000, 1000);

        t2 = new Timer();
        t2.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    makeNotificationsRequest();
                } catch (Exception e) {
                    Log.d("exceptie", e.toString());
                }
            }
        }, 1000, 1000);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE_CHAT_ACTIVITY) {
            Toast.makeText(this, "The activity returned with result " + resultCode, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
