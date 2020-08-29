package com.example.reconocimientofacialuteq.ui.login;

import android.app.Activity;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reconocimientofacialuteq.Clase.Servidor;
import com.example.reconocimientofacialuteq.MainActivity2;
import com.example.reconocimientofacialuteq.R;
import com.example.reconocimientofacialuteq.data.Result;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class LoginActivity extends AppCompatActivity {

    private static final String IP = "192.168.1.15"; // Puedes cambiar a localhost
    private static final int PUERTO = 1100;
    private static final int SERVER_PORT = 5556;
    private static final String SERVER_IP = "192.168.1.15";
    private  Socket socket;
    private String usuario="null";
    private String clave="null";
    private SharedPreferences sharedPreferences;
    private boolean guardarCredenciales;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final RadioButton radioButton = findViewById(R.id.radioButton);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        if(EstadoLogeado())
        {
            Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
            startActivity(intent);
        }
        sharedPreferences= getSharedPreferences("Login",MODE_PRIVATE);

        guardarCredenciales=radioButton.isChecked();
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(guardarCredenciales){
                    radioButton.setChecked(false);
                }
                guardarCredenciales= radioButton.isChecked();

            }
        });

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                }
                return false;
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                usuario=usernameEditText.getText().toString();
                clave=passwordEditText.getText().toString();
                new Thread(new ClientThreadLog()).start();
            }
        });
    }

    public boolean EstadoLogeado(){
        SharedPreferences sharedPreferences2= getSharedPreferences("Login",MODE_PRIVATE);
        return sharedPreferences2.getBoolean("Logeado",false);
    }

    class ClientThreadLog implements Runnable {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            try {
                String resp="";
                String idUser="";
                socket = new Socket(Servidor.IpServidor, Servidor.PuertoLogin);
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
                    objectOutputStream.writeObject(usuario);
                    objectOutputStream.writeObject(clave);
                    DataInputStream entrada = new DataInputStream(socket.getInputStream());
                    resp= (String) entrada.readUTF();
                    if("Ok".equals(resp)){
                        idUser= (String) entrada.readUTF();
                        Intent intent = new Intent(LoginActivity.this, MainActivity2.class);
                        intent.putExtra("usuario", usuario);
                        if(guardarCredenciales){
                            SharedPreferences.Editor editor= sharedPreferences.edit();
                            editor.putBoolean("Logeado",true);
                            editor.apply();
                        }
                        intent.putExtra("idUser", idUser);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        String welcome = getString(R.string.welcome) + usuario;
                        //Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
                        //Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                        //startActivity(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

