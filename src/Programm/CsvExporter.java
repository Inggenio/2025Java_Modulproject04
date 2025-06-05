package Programm;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExporter {

    public void exportToCsv(List<Aktie> aktien, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("date,symbol,open,high,low,close,volume\n");

            for (Aktie aktie : aktien) {
                writer.write(String.format("%s,%s,%.2f,%.2f,%.2f,%.2f,%d\n",
                        aktie.getDate(),
                        aktie.getSymbol(),
                        aktie.getOpen(),
                        aktie.getHigh(),
                        aktie.getLow(),
                        aktie.getClose(),
                        aktie.getVolume()
                ));
            }

            System.out.println(" CSV-Datei exportiert: " + filePath);
        } catch (IOException e) {
            System.err.println(" Fehler beim Schreiben der CSV-Datei: " + e.getMessage());
        }
    }
}