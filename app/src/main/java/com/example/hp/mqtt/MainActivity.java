package com.example.hp.mqtt;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.util.Strings;

public class MainActivity extends AppCompatActivity implements MqttCallback, View.OnClickListener {

    MqttAndroidClient client;
    ListView list;
    Button publish, subscribe;
    EditText message, topic;

    //Settings Page
    Button Connect;
    EditText ipAddress, port;
    LinearLayout layout, dashboard;
   ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (LinearLayout)findViewById(R.id.llSettings);

        //Linear layout not visible to user
        dashboard = (LinearLayout)findViewById(R.id.llDashboard);
        dashboard.setVisibility(View.GONE);

        list = (ListView)findViewById(R.id.lvMessages);
        list.setVisibility(View.GONE);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);

        publish = (Button)findViewById(R.id.bPublish);
        subscribe = (Button)findViewById(R.id.bSubscribe);
        message = (EditText)findViewById(R.id.etMessage);
        topic = (EditText)findViewById(R.id.etTopic);


        Connect = (Button)findViewById(R.id.sbConnect);
        ipAddress = (EditText) findViewById(R.id.setBroker);
        port = (EditText) findViewById(R.id.setPort);

        Connect.setOnClickListener(this);

        publish.setOnClickListener(this);

        subscribe.setOnClickListener(this);

        adapter.add("No Messages");
        list.setAdapter(adapter);
    }


    @Override
    public void connectionLost(Throwable cause) {
        Toast.makeText(this,"Connection to broker is lost. Try reconnecting!",Toast.LENGTH_SHORT).show();
        layout.setVisibility(View.VISIBLE);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Toast.makeText(this,"Message Topic:"+topic,Toast.LENGTH_SHORT).show();
        String data = "Topic: "+topic+" Message: "+message.toString();

        adapter.add(data);
        list.setAdapter(adapter);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    @Override
    public void onClick(View v) {
        String TOPIC;

        switch(v.getId())
        {
            case R.id.bPublish:
                                    String DATA = message.getText().toString().trim();
                                    TOPIC = topic.getText().toString().trim();
                                    sendMessage(TOPIC, DATA);

                                break;

            case R.id.bSubscribe:
                                    TOPIC = topic.getText().toString().trim();
                                    subscribeMqttTopic(TOPIC);
                                    adapter.clear();
                                break;

            case R.id.sbConnect:
                                String IP = ipAddress.getText().toString().trim();
                                String PORT = port.getText().toString().trim();

                                String BROKER_URI = "tcp://"+IP+":"+PORT;
                                String CLIENT_ID="mqtt_friend";

                                //Creating the connection to MQTT broker and returning a MQtt client
                                client = new MqttAndroidClient(this,BROKER_URI,CLIENT_ID);


                                //Sets all the callbacks for MQTT related states: MessageRe
                                client.setCallback(this);

                                final ProgressDialog pd = new ProgressDialog(MainActivity.this);
                                pd.setMessage("Connecting...");
                                pd.show();



                                //Connecting to Broker
                                try {

                                    client.connect(null, new IMqttActionListener() {
                                        @Override
                                        public void onSuccess(IMqttToken asyncActionToken) {
                                           pd.cancel();
                                            Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();

                                            //Settings page disappeared
                                            layout.setVisibility(View.GONE);

                                            //Dashboard appears
                                            dashboard.setVisibility(View.VISIBLE);
                                            list.setVisibility(View.VISIBLE);
                                        }

                                        @Override
                                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                            Toast.makeText(MainActivity.this, "Could not be Connected! Try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }

                                break;
        }
    }

    private void subscribeMqttTopic(final String topic) {

        try{
            client.subscribe(topic,0);
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void sendMessage(final String topic, String data) {
        Toast.makeText(MainActivity.this,"Published to-"+topic,Toast.LENGTH_SHORT).show();
        MqttMessage msg = new MqttMessage(data.getBytes());
        msg.setQos(0);

        try{
            client.publish(topic,msg);
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
      if(client.isConnected())
        try {
                client.disconnect();
                client.unregisterResources();
            } catch (MqttException e) {
                e.printStackTrace();
           }
        super.onPause();
    }


}
