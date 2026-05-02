package com.example.labthreadsasynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // 1) Références vers l’interface
    private TextView txtStatus;
    private ProgressBar progressBar;
    private ImageView img;

    // 2) Handler lié au UI thread (Main thread) pour les mises à jour
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 3) ExecutorService pour remplacer AsyncTask (obsolète depuis l'API 30)
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activation du mode EdgeToEdge (optionnel mais moderne)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Gestion des Insets (si l'ID 'main' existe dans votre layout)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // A) Lier les vues XML au code Java
        txtStatus = findViewById(R.id.txtStatus);
        progressBar = findViewById(R.id.progressBar);
        img = findViewById(R.id.img);

        Button btnLoadThread = findViewById(R.id.btnLoadThread);
        Button btnCalcAsync = findViewById(R.id.btnCalcAsync);
        Button btnToast = findViewById(R.id.btnToast);

        // B) Bouton Toast : vérifie que l'UI reste réactive
        btnToast.setOnClickListener(v ->
                Toast.makeText(getApplicationContext(), "UI réactive", Toast.LENGTH_SHORT).show()
        );

        // C) Lancer un Thread classique
        btnLoadThread.setOnClickListener(v -> loadImageWithThread());

        // D) Lancer un calcul lourd avec ExecutorService (remplace AsyncTask)
        btnCalcAsync.setOnClickListener(v -> startHeavyCalculation());
    }

    // -----------------------------------------
    // PARTIE 1 : THREAD (Chargement image)
    // -----------------------------------------
    private void loadImageWithThread() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        txtStatus.setText("Statut : chargement image (Thread)...");

        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulation délai
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            // Retour au UI thread pour modifier les vues
            mainHandler.post(() -> {
                img.setImageBitmap(bitmap);
                progressBar.setVisibility(View.INVISIBLE);
                txtStatus.setText("Statut : image chargée (Thread)");
            });
        }).start();
    }

    // -----------------------------------------
    // PARTIE 2 : EXECUTOR SERVICE (Calcul lourd)
    // -----------------------------------------
    private void startHeavyCalculation() {
        // Pré-exécution (UI Thread)
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        txtStatus.setText("Statut : calcul lourd en cours...");

        executorService.execute(() -> {
            // Arrière-plan (Worker Thread)
            long result = 0;
            for (int i = 1; i <= 100; i++) {
                // Simulation calcul
                for (int k = 0; k < 200000; k++) {
                    result += (i * k) % 7;
                }

                // Mise à jour de la progression via Handler
                final int progress = i;
                mainHandler.post(() -> progressBar.setProgress(progress));
            }

            // Post-exécution (UI Thread)
            final long finalResult = result;
            mainHandler.post(() -> {
                progressBar.setVisibility(View.INVISIBLE);
                txtStatus.setText("Statut : calcul terminé résultat = " + finalResult);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fermeture propre de l'executor
        executorService.shutdown();
    }
}
