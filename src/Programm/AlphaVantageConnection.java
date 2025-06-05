package Programm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlphaVantageConnection {
	private String symbol;
	private PFUNCTION function;
	private POUTPUT output;

	private static final String BASE_URL = "https://www.alphavantage.co/query?";
	private static final String API_KEY = "SNJHLAIDO8E6TDZ4"; // Sollte in Config-Datei ausgelagert werden

	public AlphaVantageConnection() {}

	public AlphaVantageConnection(String symbol, PFUNCTION function, POUTPUT output) {
		this.symbol = symbol;
		this.function = function;
		this.output = output;
	}

	public String getSymbol() { return symbol; }
	public PFUNCTION getFunction() { return function; }
	public POUTPUT getOutput() { return output; }

	public String filename(String symbol) {
		String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		return "JSON_Streams/Stream_" + timeStamp + "_sym_" + symbol + ".json";
	}

	public String getFilename() {
		return filename(symbol);
	}

	public void connect(String symbol, PFUNCTION function, POUTPUT output) throws IOException {
		new File("JSON_Streams").mkdirs();

		String urlString = buildUrl(symbol, function, output);
		System.out.println("API URL: " + urlString);

		HttpURLConnection conn = null;
		FileWriter stream = null;
		BufferedReader jsonReader = null;

		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000); // 10 Sekunden Timeout
			conn.setReadTimeout(30000);    // 30 Sekunden Read Timeout


			int responseCode = conn.getResponseCode();
			if (responseCode != 200) {
				throw new IOException("HTTP Error: " + responseCode + " - " + conn.getResponseMessage());
			}

			stream = new FileWriter(filename(symbol));
			jsonReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String line;
			while ((line = jsonReader.readLine()) != null) {
				stream.write(line + "\n");
			}

			System.out.println("JSON erfolgreich gespeichert: " + filename(symbol));

		} finally {
			// Resources sauber schließen
			if (jsonReader != null) jsonReader.close();
			if (stream != null) stream.close();
			if (conn != null) conn.disconnect();
		}
	}

	private String buildUrl(String symbol, PFUNCTION function, POUTPUT output) {
		StringBuilder url = new StringBuilder();
		url.append(BASE_URL)
				.append(urlFunction(function)).append("&")
				.append("symbol=").append(symbol).append("&")
				.append(urlOutput(output)).append("&")
				.append("datatype=json").append("&")
				.append("apikey=").append(API_KEY);

		// Für INTRADAY zusätzliche Parameter
		if (function == PFUNCTION.INTRADAY) {
			url.append("&interval=5min");
		}

		return url.toString();
	}

	public String urlOutput(POUTPUT output) {
		return switch (output) {
			case COMPACT -> "outputsize=compact";
			case FULL -> "outputsize=full";
		};
	}

	public String urlFunction(PFUNCTION function) {
		return switch (function) {
			case INTRADAY -> "function=TIME_SERIES_INTRADAY";
			case DAILY -> "function=TIME_SERIES_DAILY";
			case WEEKLY -> "function=TIME_SERIES_WEEKLY";
			case MONTHLY -> "function=TIME_SERIES_MONTHLY";
		};
	}
}

//
//	public static void main(String[] args) {
//		System.out.println("Connecting to AlphaVantage");
//		AlphaVantageConnection connection = new AlphaVantageConnection("MMM",PFUNCTION.DAILY, POUTPUT.COMPACT);
//		AlphaVantageJsonParser json = new AlphaVantageJsonParser();
//		try {
//			connection.connect("MMM", PFUNCTION.DAILY, POUTPUT.COMPACT);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//
//
//		System.out.println("End of Connection");
//	}

