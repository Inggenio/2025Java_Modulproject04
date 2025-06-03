package Programm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AlphaVantageJsonParser {


	public List<Aktie> parseJsonToAktien(String jsonString, String symbol) {
		List<Aktie> aktienList = new ArrayList<>();

		try {
			// Error Handling - prüfen ob API-Fehler
			if (hasApiError(jsonString)) {
				return aktienList; // Leere Liste zurückgeben
			}

			// Time Series Daten extrahieren
			String timeSeriesData = extractTimeSeriesData(jsonString);
			if (timeSeriesData == null) {
				System.err.println("Keine Time Series Daten gefunden in JSON Response");
				return aktienList;
			}

			// Einzelne Datumseinträge parsen
			aktienList = parseDateEntries(timeSeriesData, symbol);

			System.out.println("Erfolgreich " + aktienList.size() + " Aktien-Datensätze geparst");

		} catch (Exception e) {
			System.err.println("JSON Parsing Fehler: " + e.getMessage());
			e.printStackTrace();
		}

		return aktienList;
	}


	private boolean hasApiError(String jsonString) {
		if (jsonString.contains("\"Error Message\"")) {
			System.err.println("API Error gefunden in Response");
			return true;
		}

		if (jsonString.contains("\"Note\"")) {
			System.err.println("API Note - möglicherweise Rate Limit erreicht");
			return true;
		}

		return false;
	}


	private String extractTimeSeriesData(String jsonString) {
		// Pattern für verschiedene Time Series Typen
		String[] timeSeriesPatterns = {
				"\"Time Series \\(Daily\\)\":\\s*\\{([^}]+(?:\\{[^}]*\\}[^}]*)*)\\}",
				"\"Time Series \\(Weekly\\)\":\\s*\\{([^}]+(?:\\{[^}]*\\}[^}]*)*)\\}",
				"\"Time Series \\(Monthly\\)\":\\s*\\{([^}]+(?:\\{[^}]*\\}[^}]*)*)\\}",
				"\"Time Series \\(5min\\)\":\\s*\\{([^}]+(?:\\{[^}]*\\}[^}]*)*)\\}"
		};

		for (String patternString : timeSeriesPatterns) {
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(jsonString);

			if (matcher.find()) {
				return matcher.group(1);
			}
		}

		return null; // Keine Time Series gefunden
	}


	private List<Aktie> parseDateEntries(String timeSeriesData, String symbol) {
		List<Aktie> aktienList = new ArrayList<>();

		// Pattern für Datumseinträge: "2024-01-15": { ... }
		String datePattern = "\"(\\d{4}-\\d{2}-\\d{2})\":\\s*\\{([^}]+)\\}";
		Pattern datePatternCompiled = Pattern.compile(datePattern);
		Matcher dateMatcher = datePatternCompiled.matcher(timeSeriesData);

		while (dateMatcher.find()) {
			try {
				String dateString = dateMatcher.group(1);
				String dataString = dateMatcher.group(2);

				Aktie aktie = parseAktieFromData(dateString, dataString, symbol);
				if (aktie != null) {
					aktienList.add(aktie);
				}

			} catch (Exception e) {
				System.err.println("Fehler beim Parsen eines Datensatzes: " + e.getMessage());
			}
		}

		return aktienList;
	}


	private Aktie parseAktieFromData(String dateString, String dataString, String symbol) {
		try {
			LocalDate date = LocalDate.parse(dateString);

			// OHLCV Werte extrahieren
			double open = extractValue(dataString, "1\\. open");
			double high = extractValue(dataString, "2\\. high");
			double low = extractValue(dataString, "3\\. low");
			double close = extractValue(dataString, "4\\. close");
			long volume = (long) extractValue(dataString, "5\\. volume");

			return new Aktie(date, symbol, high, open, low, close, volume);

		} catch (Exception e) {
			System.err.println("Fehler beim Parsen von Datum " + dateString + ": " + e.getMessage());
			return null;
		}
	}


	private double extractValue(String dataString, String key) {
		String valuePattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
		Pattern pattern = Pattern.compile(valuePattern);
		Matcher matcher = pattern.matcher(dataString);

		if (matcher.find()) {
			return Double.parseDouble(matcher.group(1));
		}

		throw new RuntimeException("Wert für " + key + " nicht gefunden");
	}


	public void printJsonResponse(String jsonString) {
		System.out.println("=== JSON Response ===");
		System.out.println(jsonString);
		System.out.println("===================");
	}
}