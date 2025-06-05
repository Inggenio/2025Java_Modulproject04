package Programm;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlphaVantageJsonParser {

	public List<Aktie> parseJsonToAktien(String jsonString, String symbol) {
		List<Aktie> aktienList = new ArrayList<>();

		if (jsonString == null || jsonString.trim().isEmpty()) {
			System.err.println("JSON String ist leer oder null");
			return aktienList;
		}

		try {
			// Error Handling - prüfen ob API-Fehler
			if (hasApiError(jsonString)) {
				return aktienList;
			}

			// Time Series Daten extrahieren
			String timeSeriesData = extractTimeSeriesData(jsonString);
			if (timeSeriesData == null) {
				System.err.println("Keine Time Series Daten gefunden in JSON Response");
				printJsonResponse(jsonString); // Debug-Output
				return aktienList;
			}

			// Einzelne Datumseinträge parsen
			aktienList = parseDateEntries(timeSeriesData, symbol);

			if (aktienList.isEmpty()) {
				System.err.println("Keine Aktien-Datensätze geparst - prüfe JSON Format");
				printJsonResponse(jsonString.substring(0, Math.min(500, jsonString.length()))); // Ersten 500 Zeichen ausgeben
			} else {
				System.out.println("Erfolgreich " + aktienList.size() + " Aktien-Datensätze geparst");
			}

		} catch (Exception e) {
			System.err.println("JSON Parsing Fehler: " + e.getMessage());
			e.printStackTrace();
		}

		return aktienList;
	}

	private boolean hasApiError(String jsonString) {
		if (jsonString.contains("\"Error Message\"")) {
			System.err.println("API Error gefunden in Response");
			// Extrahiere und zeige die konkrete Fehlermeldung
			Pattern errorPattern = Pattern.compile("\"Error Message\":\\s*\"([^\"]+)\"");
			Matcher matcher = errorPattern.matcher(jsonString);
			if (matcher.find()) {
				System.err.println("Fehlermeldung: " + matcher.group(1));
			}
			return true;
		}

		if (jsonString.contains("\"Note\"")) {
			System.err.println("API Note - möglicherweise Rate Limit erreicht");
			Pattern notePattern = Pattern.compile("\"Note\":\\s*\"([^\"]+)\"");
			Matcher matcher = notePattern.matcher(jsonString);
			if (matcher.find()) {
				System.err.println("Note: " + matcher.group(1));
			}
			return true;
		}

		return false;
	}

	private String extractTimeSeriesData(String jsonString) {
		// Verbesserte Regex-Patterns für nested JSON
		String[] timeSeriesPatterns = {
				"\"Time Series \\(Daily\\)\":\\s*\\{([\\s\\S]*?)\\}(?=\\s*\\}\\s*$)",
				"\"Time Series \\(Weekly\\)\":\\s*\\{([\\s\\S]*?)\\}(?=\\s*\\}\\s*$)",
				"\"Time Series \\(Monthly\\)\":\\s*\\{([\\s\\S]*?)\\}(?=\\s*\\}\\s*$)",
				"\"Time Series \\(5min\\)\":\\s*\\{([\\s\\S]*?)\\}(?=\\s*\\}\\s*$)"
		};

		for (String patternString : timeSeriesPatterns) {
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(jsonString);

			if (matcher.find()) {
				return matcher.group(1);
			}
		}

		return null;
	}

	private List<Aktie> parseDateEntries(String timeSeriesData, String symbol) {
		List<Aktie> aktienList = new ArrayList<>();

		// Verbesserte Regex für Datumseinträge
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

			// OHLCV Werte extrahieren mit besserer Fehlerbehandlung
			Double open = extractValueSafe(dataString, "1\\. open");
			Double high = extractValueSafe(dataString, "2\\. high");
			Double low = extractValueSafe(dataString, "3\\. low");
			Double close = extractValueSafe(dataString, "4\\. close");
			Long volume = extractVolumeSafe(dataString, "5\\. volume");

			if (open == null || high == null || low == null || close == null || volume == null) {
				System.err.println("Unvollständige Daten für " + dateString);
				return null;
			}

			return new Aktie(date, symbol, high, open, low, close, volume);

		} catch (Exception e) {
			System.err.println("Fehler beim Parsen von Datum " + dateString + ": " + e.getMessage());
			return null;
		}
	}

	private Double extractValueSafe(String dataString, String key) {
		try {
			return extractValue(dataString, key);
		} catch (Exception e) {
			System.err.println("Wert für " + key + " nicht gefunden oder ungültig");
			return null;
		}
	}

	private Long extractVolumeSafe(String dataString, String key) {
		try {
			return (long) extractValue(dataString, key);
		} catch (Exception e) {
			System.err.println("Volume für " + key + " nicht gefunden oder ungültig");
			return null;
		}
	}

	private double extractValue(String dataString, String key) {
		String valuePattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
		Pattern pattern = Pattern.compile(valuePattern);
		Matcher matcher = pattern.matcher(dataString);

		if (matcher.find()) {
			String value = matcher.group(1);
			return Double.parseDouble(value);
		}

		throw new RuntimeException("Wert für " + key + " nicht gefunden");
	}

	public void printJsonResponse(String jsonString) {
		System.out.println("=== JSON Response (Debug) ===");
		System.out.println(jsonString);
		System.out.println("=============================");
	}
}