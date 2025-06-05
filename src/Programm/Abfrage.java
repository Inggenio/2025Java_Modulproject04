package Programm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Abfrage {

    public static void main(String[] args) {
        String symbol = "MSFT";
        PFUNCTION function = PFUNCTION.DAILY;
        POUTPUT output = POUTPUT.COMPACT;

        AlphaVantageConnection conn = new AlphaVantageConnection(symbol, function, output);
        AlphaVantageJsonParser parser = new AlphaVantageJsonParser();
        CsvExporter exporter = new CsvExporter();
        DatabaseManager db = new DatabaseManager();

        try {
            System.out.println("Starte API-Abfrage für Symbol: " + symbol);

            // API-Verbindung → speichert JSON in Datei
            conn.connect(symbol, function, output);

            // JSON aus Datei lesen
            String jsonPath = conn.getFilename();
            Path path = Paths.get(jsonPath);

            if (!Files.exists(path)) {
                System.err.println("JSON-Datei nicht gefunden: " + jsonPath);
                return;
            }

            String jsonContent = Files.readString(path);
            System.out.println("JSON-Datei gelesen: " + jsonPath + " (" + jsonContent.length() + " Zeichen)");

            // JSON → Java-Objekte
            List<Aktie> aktien = parser.parseJsonToAktien(jsonContent, symbol);

            if (aktien.isEmpty()) {
                System.err.println("Keine Aktien-Daten erhalten. Prozess beendet.");
                return;
            }

            // Export als CSV
            Path csvDir = Paths.get("Exported_CSV");
            Files.createDirectories(csvDir); // Ordner erstellen falls nicht vorhanden

            String csvPath = csvDir.resolve(symbol + "_export.csv").toString();
            exporter.exportToCsv(aktien, csvPath);

            // In DB speichern
            System.out.println("Speichere Daten in Datenbank...");
            db.saveAktie(aktien);

            System.out.println("Prozess erfolgreich abgeschlossen!");

        } catch (IOException e) {
            System.err.println("Datei-/Netzwerkfehler: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Fehler bei Datenbank oder Parser: " + e.getMessage());
            e.printStackTrace();
        }
    }
}