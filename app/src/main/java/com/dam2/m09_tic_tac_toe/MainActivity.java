package com.dam2.m09_tic_tac_toe;

import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ImageView[][] table;
    private Button connect;
    private Button start;
    int[] rowIds;
    private ObjectOutputStream output;
    private Context context;
    private boolean serverOnline = false;
    private boolean gameOn = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        table = new ImageView[3][3];
        rowIds = new int[]{R.id.img_row0_col0, R.id.img_row0_col1, R.id.img_row0_col2,
                R.id.img_row1_col0, R.id.img_row1_col1, R.id.img_row1_col2, R.id.img_row2_col0,
                R.id.img_row2_col1, R.id.img_row2_col2};
        int index = 0;

        for (int row = 0; row < table.length; row++) {
            for (int col = 0; col < table[row].length; col++) {
                table[row][col] = findViewById(rowIds[index++]);
            }
        }

        clearTable(table);

        TextInputEditText ipInput = findViewById(R.id.ip_edit_text_input);
        ipInput.setText("10.0.2.2");
        TextInputEditText portInput = findViewById(R.id.port_edit_text_input);
        portInput.setText("4000");
        connect = findViewById(R.id.connect_btn);

        connect.setOnClickListener(view -> {
            String ip = Objects.requireNonNull(ipInput.getText()).toString();
            String port = Objects.requireNonNull(portInput.getText()).toString();
            String[] connectionAddress = {ip, port};
            boolean ipOK = ip.length() > 0 && ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
            boolean portOK = port.length() > 0 && port.length() < 5;
            if (ipOK && portOK) {
                AsyncTaskConnect asyncTaskConnect = new AsyncTaskConnect();
                asyncTaskConnect.execute(connectionAddress);
                if (serverOnline) disableConnectButton(true);
                Toast.makeText(context, "Conexión establecida", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "La dirección introducida es incorrecta.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        start = findViewById(R.id.start_btn);

        start.setOnClickListener(view -> {
            // Enviamos mensaje de jugar
            AsyncTaskSendMessage asyncTaskSendMessage = new AsyncTaskSendMessage();
            asyncTaskSendMessage.execute("jugar");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // En caso de conexión establecida y partida aceptada, deshabilitamos el botón 'start'
            if (serverOnline && gameOn) {
                disableStartGameButton(true);
                Toast.makeText(context, "Comienza la partida", Toast.LENGTH_LONG).show();
            } else Toast.makeText(context, "No estás conectado", Toast.LENGTH_LONG).show();

        });

        do {
            for (int row = 0; row < table.length; row++) {
                for (int col = 0; col < table[row].length; col++) {
                    ImageView imageView = table[row][col];
                    String coords = String.format(Locale.getDefault(), "%d%d", row, col);
                    imageView.setTag(coords);
                    imageView.setOnClickListener(v -> {
                        String clickedCoords = (String) v.getTag();
                        if (isBoxEmpty(clickedCoords)) {
                            AsyncTaskSendMessage asyncTaskSendMessage = new AsyncTaskSendMessage();
                            asyncTaskSendMessage.execute(clickedCoords);
                        } else {
                            Toast.makeText(context, "La casilla está ocupada!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        } while (gameOn);

    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    class AsyncTaskConnect extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... values) {
            try {
                Socket client = new Socket(values[0], Integer.parseInt(values[1]));
                output = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                serverOnline = true;
                new ThreadClient(input, table).start();
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(context, "Servidor no disponible",
                        Toast.LENGTH_LONG).show());
            }
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    class AsyncTaskSendMessage extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... values) {
            try {
                String clientMove = values[0];
                if (serverOnline) {
                    if (!clientMove.equals("jugar")) {
                        int row = Integer.parseInt(clientMove.substring(0, 1));
                        int col = Integer.parseInt(clientMove.substring(1, 2));
                        runOnUiThread(() -> {
                            table[row][col].setImageResource(R.drawable.ic_shape_circle_yellow_32dp);
                            table[row][col].setVisibility(View.VISIBLE);
                        });
                    }
                    output.writeObject(clientMove);
                    output.flush();
                } else {
                    runOnUiThread(() -> Toast.makeText(context, "Servidor no disponible",
                            Toast.LENGTH_LONG).show());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
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
                boolean end = false;
                do {
                    String serverMove = (String) input.readObject();
                    if (serverMove.equals("ack")) {
                        gameOn = true;
                        continue;
                    }
                    if (serverMove.equals("ganador") || serverMove.equals("empate")) {
                        gameOn = false;
                        end = true;
                    }
                    if (serverMove.contains("-")) {
                        int row = Integer.parseInt(serverMove.substring(serverMove.length() - 2,
                                serverMove.length() - 1));
                        int col = Integer.parseInt(serverMove.substring(serverMove.length() - 1));
                        runOnUiThread(() -> {
                            table[row][col].setImageResource(R.drawable.ic_shape_x_32dp);
                            table[row][col].setVisibility(View.VISIBLE);
                        });
                        gameOn = false;
                        end = true;
                    } else {
                        int row = Integer.parseInt(serverMove.substring(0, 1));
                        int col = Integer.parseInt(serverMove.substring(1, 2));
                        runOnUiThread(() -> {
                            table[row][col].setImageResource(R.drawable.ic_shape_x_32dp);
                            table[row][col].setVisibility(View.VISIBLE);
                        });
                    }
                } while (!end);
            } catch (Exception e) {
                System.out.println("[X] Recibido error: " + e.getMessage());
            }
        }
    }

    public void disableConnectButton(boolean connectionOK) {
        if (connectionOK) {
            connect.setClickable(false);
            connect.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        } else {
            connect.setClickable(true);
            connect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darker25_color_primary_dark)));
        }
    }

    public void disableStartGameButton(boolean gameOn) {
        if (gameOn) start.setVisibility(View.GONE);
        else start.setVisibility(View.VISIBLE);
    }

    public static void clearTable(ImageView[][] table) {
        for (ImageView[] imageViews : table) {
            for (int col = 0; col < table.length; col++) {
                imageViews[col].setVisibility(View.GONE);
            }
        }
    }

    public boolean isBoxEmpty(String coords) {
        boolean empty = false;
        int row = Integer.parseInt(coords.substring(0, 1));
        int col = Integer.parseInt(coords.substring(1, 2));
        if (table[row][col].getVisibility() == View.GONE) {
            empty = true;
        }
        return empty;
    }

}