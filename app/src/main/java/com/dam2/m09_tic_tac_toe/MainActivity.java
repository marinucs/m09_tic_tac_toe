package com.dam2.m09_tic_tac_toe;

import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private AppCompatImageView[][] table;
    private Button connect, start;
    private TextView server, client;
    private ObjectOutputStream output;
    private Context context;
    private boolean serverOnline = false;
    private boolean disconnect = false;
    private boolean isMyTurn;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        table = new AppCompatImageView[3][3];

        TextInputEditText ipInput = findViewById(R.id.ip_edit_text_input);
        ipInput.setText("10.0.2.2");
        TextInputEditText portInput = findViewById(R.id.port_edit_text_input);
        portInput.setText("4000");
        connect = findViewById(R.id.connect_btn);

        connect.setOnClickListener(view -> {
            if (connect.getText().equals("CONNECT")) {
                String ip = Objects.requireNonNull(ipInput.getText()).toString();
                String port = Objects.requireNonNull(portInput.getText()).toString();
                String[] connectionAddress = {ip, port};
                boolean ipOK = ip.length() > 0 && ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
                boolean portOK = port.length() > 0 && port.length() < 5;
                if (ipOK && portOK) {
                    AsyncTaskConnect asyncTaskConnect = new AsyncTaskConnect();
                    asyncTaskConnect.execute(connectionAddress);
                } else {
                    Toast.makeText(context, "Error en la IP o en el puerto", Toast.LENGTH_SHORT).show();
                }
            } else {
                disconnect = true;
            }
        });

        start = findViewById(R.id.start_btn);

        if (serverOnline) {
            start.setClickable(true);
        }
        
        start.setOnClickListener(view -> {
            // Enviamos mensaje de jugar
            AsyncTaskSendMessage asyncTaskSendMessage = new AsyncTaskSendMessage();
            asyncTaskSendMessage.execute("jugar");
            
            Toast.makeText(context, "Comienza la partida", Toast.LENGTH_SHORT).show();
            
            server = findViewById(R.id.server);
            client = findViewById(R.id.client);

        });

    }

    private void setUpTable() {
        int[] rowIds = new int[] {       
                        R.id.img_row0_col0, R.id.img_row0_col1, R.id.img_row0_col2,
                        R.id.img_row1_col0, R.id.img_row1_col1, R.id.img_row1_col2, 
                        R.id.img_row2_col0, R.id.img_row2_col1, R.id.img_row2_col2 };

        int index = 0;
        for (int row = 0; row < table.length; row++) {
            for (int col = 0; col < table[row].length; col++) {
                AppCompatImageView imageView = findViewById(rowIds[index++]);
                table[row][col] = imageView;
                table[row][col].setClickable(true);
                table[row][col].setOnClickListener(new CellClickListener(row, col));
            }
        }

        System.out.println("Generada la tabla");
    }

    public boolean isBoxEmpty(String coords) {
        int row = Integer.parseInt(coords.substring(0, 1));
        int col = Integer.parseInt(coords.substring(1, 2));

        return table[row][col].getDrawable() == null;
    }

    private class CellClickListener implements View.OnClickListener {
        private final int row, col;

        public CellClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void onClick(View v) {
            String coords = String.format(Locale.getDefault(), "%d%d", row, col);
            if (isMyTurn) {
                System.out.println("It's my turn from CellClickListener");
                if (isBoxEmpty(coords)) {
                    System.out.println("BoxIsEmpty from CellClickListener");
                    AsyncTaskSendMessage asyncTaskSendMessage = new AsyncTaskSendMessage();
                    asyncTaskSendMessage.execute(coords);
                }
            } // else Toast.makeText(context, "Es el turno del servidor", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    class AsyncTaskConnect extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... values) {
            try {
                //noinspection resource
                Socket client = new Socket(values[0], Integer.parseInt(values[1]));
                output = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                serverOnline = true;
                System.out.println("Conexión establecida");
                runOnUiThread(() -> {
                    // TODO: cambiar a conectar o desconectar en lugar de deshabilitarlo
                    Toast.makeText(context, "Conexión establecida", Toast.LENGTH_SHORT).show();
                    if (connect.getText().equals("CONNECT")) connect.setText("DISCONNECT");
                    setUpTable();
                });
                new ThreadClient(input, table).start();
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(context, "Servidor no disponible", Toast.LENGTH_SHORT).show());
                if (connect.getText().equals("DISCONNECT")) connect.setText("CONNECT");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
        }

    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    class AsyncTaskSendMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... values) {
            try {
                String clientOutput = values[0];
                System.out.println("[o] Client message: " + clientOutput);
                // System.out.println("From client - isMyTurn: " + isMyTurn);
                if (serverOnline) {
                    if (isMyTurn) {
                        System.out.println("Es tu turno, cliente");
                        int row = Integer.parseInt(clientOutput.substring(0, 1));
                        int col = Integer.parseInt(clientOutput.substring(1, 2));
                        runOnUiThread(() -> {
                            table[row][col].setImageResource(R.drawable.ic_shape_circle_yellow_50dp);
                            table[row][col].setClickable(false);
                        });
                        isMyTurn = false;
                        runOnUiThread(() -> {
                            // Toast.makeText(context, "Ahora es el turno del servidor", Toast.LENGTH_SHORT).show();
                            server.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darker_dark_background)));
                            client.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_gray)));
                        });
                        output.writeObject(clientOutput);
                        output.flush();
                    }
                    if (clientOutput.equals("jugar")) {
                        output.writeObject(clientOutput);
                        output.flush();
                        // TODO: Borrar lo siguiente, pues está en el Thread Client al recibir 'ACK'
                        isMyTurn = true;
                        
                        runOnUiThread(() -> {
                            clearTable();
                            server.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_gray)));
                            client.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darker_dark_background)));
                        });
                        // System.out.println("From client, after 'jugar' - isMyTurn: " + isMyTurn);
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(context, "Servidor no disponible",
                            Toast.LENGTH_SHORT).show());
                    if (connect.getText().equals("DISCONNECT")) connect.setText("CONNECT");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
        }

    }

    class ThreadClient extends Thread {
        private final ObjectInputStream input;
        private final ImageView[][] table;

        public ThreadClient(ObjectInputStream input, ImageView[][] table) throws IOException {
            this.table = table;
            this.input = input;
        }

        @Override
        public void run() {
            try {
                do {
                    String serverInput = (String) input.readObject();
                    System.out.println("[1] Server message: " + serverInput);
                    // System.out.println("From server - isMyTurn: " + isMyTurn);
                    if (serverInput.equals("ack")) {
                        
                        isMyTurn = true;
                        // System.out.println("From server, after 'ack' - isMyTurn: " + isMyTurn);
                        System.out.println("[2] Server message: Comienza la partida");
                        System.out.println("[3] Server message: Primer turno para cliente");
                        runOnUiThread(() -> {
                            clearTable();
                            server.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_gray)));
                        });
                    } else if (serverInput.equals("ganador") || serverInput.equals("empate")) {
                        
                    } else if (serverInput.contains("-")) {
                        int row = Integer.parseInt(serverInput.substring(serverInput.length() - 2
                                , serverInput.length() - 1));
                        int col = Integer.parseInt(serverInput.substring(serverInput.length() - 1));
                        runOnUiThread(() -> {
                            table[row][col].setImageResource(R.drawable.ic_shape_x_50dp);
                            table[row][col].setClickable(false);
                        });
                        
                    } else {
                        int row = Integer.parseInt(serverInput.substring(0, 1));
                        int col = Integer.parseInt(serverInput.substring(1, 2));
                        runOnUiThread(() -> {
                            table[row][col].setImageResource(R.drawable.ic_shape_x_50dp);
                            table[row][col].setClickable(false);
                            server.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_gray)));
                            client.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darker_dark_background)));
                        });
                        isMyTurn = true;
                    }
                } while (serverOnline || !disconnect);
                
            } catch (Exception e) {
                System.out.println("[X] Recibido error: " + e.getMessage());
            }
        }
    }

    private void clearTable() {
        for (AppCompatImageView[] appCompatImageViews : table) {
            for (AppCompatImageView appCompatImageView : appCompatImageViews) {
                appCompatImageView.setImageDrawable(null);
                if (!appCompatImageView.isClickable()) appCompatImageView.setClickable(true);
            }
        }
    }

}