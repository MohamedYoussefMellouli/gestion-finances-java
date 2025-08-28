package service;

import java.net.*;
import java.io.*;

public class PredictionClient {

    public static double predireMontant(String categorie, int mois, int jour, String devise) throws Exception {
        URL url = new URL("http://127.0.0.1:5000/predict");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Créer le JSON à envoyer
        String jsonInput = String.format(
            "{\"categorie\":\"%s\",\"mois\":%d,\"jour\":%d,\"devise\":\"%s\"}",
            categorie, mois, jour, devise
        );

        
        // Envoyer la requête
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Vérifier le code HTTP
        int status = conn.getResponseCode();

        InputStream stream = (status == 200) ? conn.getInputStream() : conn.getErrorStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }

            String resp = response.toString();

            // Si l'API a renvoyé une erreur
            if (status != 200) {
                throw new Exception("Erreur API : " + resp);
            }

            // Extraire le montant depuis le JSON sans librairie externe
            // On suppose que la réponse est du type {"montant":45.5}
            double montant;
            if (resp.contains("montant")) {
                // Extraire le nombre après "montant"
                String valeur = resp.replaceAll(".*\"montant\"\\s*:\\s*([0-9.]+).*", "$1");
                montant = Double.parseDouble(valeur);
            } else {
                throw new Exception("Réponse invalide : " + resp);
            }

            return montant;
        }
    }

    public static void main(String[] args) {
        try {
            double montant = predireMontant("Alimentation", 1, 15, "EUR");
            System.out.println("Montant prédit : " + montant);
        } catch (Exception e) {
            System.err.println("Erreur lors de la prédiction : " + e.getMessage());
        }
    }
}
