package com.coderzheaven.client;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener  {

    public static final int SERVER_PORT = 3003;

    // Get this IP from the Device WiFi Settings
    // Make sure you have the devices in same WiFi if testing locally
    // Or Make sure the port specified is open for connections.
    public String SERVER_IP = "192.168.137.11";
    public int AllyCounter;
    public int EnemyCounter;
    private ClientThread clientThread;
    private Thread thread;
    private LinearLayout msgList;
    private Handler handler;
    private int clientTextColor;
    private EditText edMessage;
    private FusedLocationProviderClient client;
    public boolean changedTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
       hidePopUPbtn();
       AllyCounter = 0;
       EnemyCounter = 0;
       changedTemplate = false;
        setTitle("ATAKMessanger");
        clientTextColor = ContextCompat.getColor(this, R.color.green);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
        edMessage.setHint("Tu wpisz IP serwer np."+ "\""  +SERVER_IP + "\"");

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                startSettings();
                return true;
            case R.id.changePosition:
                changeTemplateOnNewPosition();
                changedTemplate = true;
                return true;
            case R.id.backToPreviousLayout:
                backToPreviousLayout();
                changedTemplate = false;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void backToPreviousLayout() {
        edMessage.setHint("Tu wpisz wiadomosc do wyslania");
        Button setAlly = findViewById(R.id.buttonAlly);
        Button setEnemy= findViewById(R.id.send_data2);
        setAlly.setText("Ustaw znacznik sojusznika");
        setEnemy.setText("Ustaw znacznik przeciwnika");
    }

    private void changeTemplateOnNewPosition() {
        edMessage.setHint("Tu wpisz id punktu do zmiany");
        Button setAlly = findViewById(R.id.buttonAlly);
        Button setEnemy= findViewById(R.id.send_data2);
        setAlly.setText("Zmien po??ozenie sojusznika");
        setEnemy.setText("Zmien po??ozenie przeciwnika");
    }

    public void startSettings(){
        startActivity(new Intent(ClientActivity.this, InstrukcjaOblugi.class));
    }

    //ta funcjkacja dziala wewnatrz przez co nie mg modyfikowac czasu jaki wysylam a sluzy jedynie do wyswietlania
    public TextView textView(String message, int color) {
        if (null == message || message.trim().isEmpty()) {
            message = "<Empty Message>";
        }
        String m = message + " [" + getTime() +"]";
        TextView tv = new TextView(this);
        tv.setTextColor(color);
        tv.setText(m);
        tv.setTextSize(20);
        tv.setPadding(0, 5, 0, 0);
        return tv;
    }

    private void hideConnectServerBtn(){
        handler.post(() -> findViewById(R.id.btnConnectServer).setVisibility(View.GONE));
    }

    private void showPopUPbtn(){
        Button PopUp = findViewById(R.id.PopUp);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) PopUp.getLayoutParams();
        params.width = 500;
        //params.height=50;
        PopUp.setLayoutParams(params);
    }

    private void hidePopUPbtn(){
        Button PopUp = findViewById(R.id.PopUp);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) PopUp.getLayoutParams();
        params.width = 1;
        //params.height=1;
        PopUp.setLayoutParams(params);
       // PopUp.setLayoutParams(new LinearLayout.LayoutParams(1, 1));
    }

    public void showMessage(final String message, final int color) {
        handler.post(() -> msgList.addView(textView(message, color)));
    }

    private void removeAllViews(){
        handler.post(() -> msgList.removeAllViews());
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnConnectServer) {
            SERVER_IP = edMessage.getText().toString();
            removeAllViews();
            showMessage("Connecting to Server...", clientTextColor);
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
           // showMessage("Connected to Server...", clientTextColor);
            hideConnectServerBtn();
            showPopUPbtn();
            edMessage.setText("");
            edMessage.setHint("Tu wpisz wiadomosc do wys??ania");
            edMessage.setInputType(InputType.TYPE_CLASS_TEXT);
            return;
        }

        if (view.getId() == R.id.send_data) {
            String clientMessage = edMessage.getText().toString().trim();
            showMessage(clientMessage, Color.BLUE);
            if (null != clientThread) {
                clientThread.sendMessage(clientMessage);
            }
        }

        if(view.getId() == R.id.send_data2){
            TextView lat = findViewById(R.id.Lat);
            TextView lonng = findViewById(R.id.Long);
            String isW = "0";
            String isS = "0";
            String LaT = lat.getText().toString();
            String LoNg = lonng.getText().toString();
            if(LaT.isEmpty() || LoNg.isEmpty()){
                Toast.makeText(this, "Nie wpisano tekstu do przeslania, uzupe??nij pola tekstowe!",Toast.LENGTH_SHORT).show();
            }else if(Integer.parseInt(LaT)<=90 && Integer.parseInt(LaT)>=-90 && Integer.parseInt(LoNg)>=-180 && Integer.parseInt(LoNg)<=180 ){
                if (Integer.parseInt(LaT)<0 ){
                    isS="1";
                    LaT = String.valueOf(Integer.parseInt(LaT)*-1);
                }

                if (Integer.parseInt(LoNg)<0 ){
                    isW="1";
                    LoNg = String.valueOf(Integer.parseInt(LoNg)*-1);
                }
                String Message = "1-EFlag-"+LaT+"-"+LoNg+"-"+isS+"-"+isW+"-0";
                if(changedTemplate){
                    TextView edMessage = findViewById(R.id.edMessage);
                    String id = edMessage.getText().toString();
                    if(id.isEmpty()){
                        Toast.makeText(this, "Nie podales wartosci ID, wiec ustawiono na 0!",Toast.LENGTH_SHORT).show();
                        id="0";
                    }
                    edMessage.setText("");
                    if(Integer.parseInt(id)>EnemyCounter){
                        Toast.makeText(this, "Podales wartosc spoza przedzialu!",Toast.LENGTH_SHORT).show();
                        Message = "Uzytkownik podal bledne dane";
                    }else{
                    Message = "2-EFlag-"+LaT+"-"+LoNg+"-"+isS+"-"+isW+"-"+id;
                    }
                }
                edMessage.setText(Message);
                String clientMessage = edMessage.getText().toString().trim();
                showMessage(clientMessage, Color.BLUE);
                if (null != clientThread) {
                    clientThread.sendMessage(clientMessage);
                }
            }else{
                Toast.makeText(this, "Podales wartosc spoza przedzialu!",Toast.LENGTH_SHORT).show();
            }

            edMessage.setText("");
            if(changedTemplate){}else{
                EnemyCounter=EnemyCounter+1;
            }

        }

        if(view.getId() == R.id.buttonAlly){
            TextView lat = findViewById(R.id.Lat);
            TextView lonng = findViewById(R.id.Long);
            String isW = "0";
            String isS = "0";
            String LaT = lat.getText().toString();
            String LoNg = lonng.getText().toString();
            if(LaT.isEmpty() || LoNg.isEmpty()){
                Toast.makeText(this, "Nie wpisano tekstu do przeslania, uzupe??nij pola tekstowe!",Toast.LENGTH_SHORT).show();
            }else if(Integer.parseInt(LaT)<=90 && Integer.parseInt(LaT)>=-90 && Integer.parseInt(LoNg)>=-180 && Integer.parseInt(LoNg)<=180 ){
                if (Integer.parseInt(LaT)<0 ){
                    isS="1";
                    LaT = String.valueOf(Integer.parseInt(LaT)*-1);
                }

                if (Integer.parseInt(LoNg)<0 ){
                    isW="1";
                    LoNg = String.valueOf(Integer.parseInt(LoNg)*-1);
                }
                String Message = "1-Flag-"+LaT+"-"+LoNg+"-"+isS+"-"+isW+"-0";
                if(changedTemplate){
                    TextView edMessage = findViewById(R.id.edMessage);
                    String id = edMessage.getText().toString();
                    if(id.isEmpty()){
                        Toast.makeText(this, "Nie podales wartosci ID, wiec ustawiono na 0!",Toast.LENGTH_SHORT).show();
                        id="0";
                    }
                    edMessage.setText("");
                    if(Integer.parseInt(id)>AllyCounter){
                        Toast.makeText(this, "Podales wartosc spoza przedzialu!",Toast.LENGTH_SHORT).show();
                        Message = "Uzytkownik podal bledne dane";
                    }else{
                        Message = "2-Flag-"+LaT+"-"+LoNg+"-"+isS+"-"+isW+"-"+id;
                    }
                }
                edMessage.setText(Message);
                String clientMessage = edMessage.getText().toString().trim();
                showMessage(clientMessage, Color.BLUE);
                if (null != clientThread) {
                    clientThread.sendMessage(clientMessage);
                }
            }else{
                Toast.makeText(this, "Podales wartosc spoza przedzialu!",Toast.LENGTH_SHORT).show();
            }

            edMessage.setText("");
            if(changedTemplate){}else {
                AllyCounter = AllyCounter + 1;
            }
        }
    }

    public void  showPopup(View view){
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1:
                Toast.makeText(this, "Wybrano dodanie samolotu mysliwskiego",Toast.LENGTH_SHORT).show();
                edMessage.setText("1-DSM-0-0-0");
                String clientMessage = edMessage.getText().toString().trim();
                //showMessage(clientMessage, Color.BLUE);
                if (null != clientThread) {
                    clientThread.sendMessage(clientMessage);
                }
                edMessage.setText("");
                return true;
            case R.id.item2:
                Toast.makeText(this, "Wybrano dodanie samolotu szturmowego",Toast.LENGTH_SHORT).show();
                edMessage.setText("1-DSS-0-0-0");
                String clientMessage1 = edMessage.getText().toString().trim();
                //showMessage(clientMessage, Color.BLUE);
                if (null != clientThread) {
                    clientThread.sendMessage(clientMessage1);
                }
                edMessage.setText("");
                return true;
            case R.id.item3:
                Toast.makeText(this, "Ustaw punkt na lat 30 lon 20",Toast.LENGTH_SHORT).show();
                edMessage.setText("1-Flag-20-30-0-0");
                String clientMessage2 = edMessage.getText().toString().trim();
                //showMessage(clientMessage, Color.BLUE);
                if (null != clientThread) {
                    clientThread.sendMessage(clientMessage2);
                }
                edMessage.setText("");
                AllyCounter = AllyCounter +1;
                return true;
            case R.id.item4:
                Toast.makeText(this, "Wybrano przejdz do polaczenia API",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ClientActivity.this, apiConnect.class));
                return true;
            case R.id.item5:
                Toast.makeText(this, "Wybrano umiesc znacznik w mojej lokalizacji",Toast.LENGTH_SHORT).show();
                client = LocationServices.getFusedLocationProviderClient(this);
                requestPermission();
                if (ActivityCompat.checkSelfPermission(ClientActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                    Log.e("nie masz uparawnien GPS", "bark uprawnien");
                }
                client.getLastLocation().addOnSuccessListener(ClientActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.e("Element 1", String.valueOf(location.getLatitude()));
                            Log.e("Element 2", String.valueOf(location.getLongitude()));
                            String MyMessageGPS= "1-Flag-"+String.valueOf(location.getLatitude())+"-"+String.valueOf(location.getLongitude())+"-0";
                            edMessage.setText(MyMessageGPS);
                            String clientMessage2 = edMessage.getText().toString().trim();
                            //showMessage(clientMessage, Color.BLUE);
                            if (null != clientThread) {
                                clientThread.sendMessage(clientMessage2);
                            }
                            edMessage.setText("");
                           // Log.e("My location",location.toString());
                        }
                    }
                });
                return true;
            default:
                return false;
        }
    }
    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader input;

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVER_PORT);
                while (!Thread.currentThread().isInterrupted()) {
                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = input.readLine();
                    if (null == message || "Disconnect".contentEquals(message)) {
                        boolean interrupted = Thread.interrupted();
                        message = "Server Disconnected: " + interrupted;
                        showMessage(message, Color.RED);
                        break;
                    }
                    showMessage("Server: " + message, clientTextColor);
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        void sendMessage(final String message) {
            new Thread(() -> {
                try {
                    if (null != socket) {
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())),
                                true);
                        out.println(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }

    String getTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread) {
            clientThread.sendMessage("Disconnect");
            clientThread = null;
        }
    }
}