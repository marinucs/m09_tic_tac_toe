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
import android.widget.TextView;
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
    private TextView player1;
    private TextView player2;
    int[] rowIds;
    private ObjectOutputStream output;
    private Context context;
    private boolean serverOnline = false;
    private boolean gameOn = false;
    private boolean isMyTurn;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        table = new ImageView[3][3];

        setupTable();

        clearTable(table);

        TextInputEditText ipInput = findViewById(R.id.ip_edit_text_input);
        ipInput.setText("10.0.2.2");
        TextInputEditText portInput = findViewById(R.id.port_edit_text_input);
        portInput.setText("4000");
        connect = findViewById(R.id.connect_btn);

        connect.setOnClickListener(view -> {
            String ip = Objects.requireNonNull(ipInput.getText()).toString();
            String port = Objects.requireNonNull(portInput.getText()).toString();
            String connectionAddress = ip + "-" + port;
            boolean ipOK = ip.length() > 0 && ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
            boolean portOK = port.length() > 0 && port.length() < 5;
            if (ipOK && portOK) {
                AsyncTaskConnect asyncTaskConnect = new AsyncTaskConnect();
                asyncTaskConnect.execute(connectionAddress);
                if (serverOnline) {
                    disableConnectButton(true);
                    Toast.makeText(context, "Conexión establecida", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "Error en la IP o en el puerto",
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
            } // else Toast.makeText(context, "No estás conectado", Toast.LENGTH_LONG).show();

            player1 = findViewById(R.id.player1);
            player2 = findViewById(R.id.player2);
        });

    }

    private void setupTable() {
        int[] rowIds = new int[] {
                R.id.img_row0_col0, R.id.img_row0_col1, R.id.img_row0_col2,
                R.id.img_row1_col0, R.id.img_row1_col1, R.id.img_row1_col2,
                R.id.img_row2_col0, R.id.img_row2_col1, R.id.img_row2_col2
        };

        int index = 0;
        for (int row = 0; row < table.length; row++) {
            for (int col = 0; col < table[row].length; col++) {
                ImageView imageView = findViewById(rowIds[index++]);
                imageView.setOnClickListener(new CellClickListener(row, col));
                table[row][col] = imageView;
            }
        }
    }

    private class CellClickListener implements View.OnClickListener {
        private final int row;
        private final int col;

        public CellClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void onClick(View v) {
            String coords = String.format(Locale.getDefault(), "%d%d", row, col);
            if (isMyTurn) {
                if (isBoxEmpty(coords)) {
                    AsyncTaskSendMessage asyncTaskSendMessage = new AsyncTaskSendMessage();
                    asyncTaskSendMessage.execute(coords);
                } else {
                    Toast.makeText(context, "No es tu turno!",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "La casilla está ocupada!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    class AsyncTaskConnect extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... values) {
            try {
                String[] socketAtts = values[0].split("-");
                Socket client = new Socket(socketAtts[0], Integer.parseInt(socketAtts[1]));
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
                            player1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_gray)));
                            table[row][col].setImageResource(R.drawable.ic_shape_circle_yellow_32dp);
                            table[row][col].setVisibility(View.VISIBLE);
                        });
                    }
                    output.writeObject(clientMove);
                    output.flush();
                    runOnUiThread(() -> {
                        player2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darker25_color_primary_dark)));
                    });
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
                        isMyTurn = true;
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
                            player1.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.darker25_color_primary_dark)));
                            player2.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_gray)));
                        });
                        isMyTurn = true;
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