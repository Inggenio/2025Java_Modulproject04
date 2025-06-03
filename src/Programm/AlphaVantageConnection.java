package Programm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;




public class AlphaVantageConnection {

	String BASE_url = "https://www.alphavantage.co/query?";
	String API_Key ="CSAA3X0CNSYIUCT8";

	public void connect(String symbol, PFUNCTION function, POUTPUT output) throws IOException {
		StringBuilder url = new StringBuilder();
		url.append(BASE_url)
				.append(urlFunction(function)).append("&")
				.append("symbol=").append(symbol).append("&")
				.append(urlOutput(output)).append("&")
				.append("datatype=csv").append("&")
				.append("apikey=").append(API_Key)
				;

		String myURL = url.toString();
		HttpURLConnection conn = (HttpURLConnection) new URL(myURL).openConnection();
		FileWriter stream = new FileWriter("stream"+symbol+".txt");

		try {
			System.out.println(myURL);
			conn.setRequestMethod("GET");

			BufferedReader jsonReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while((line = jsonReader.readLine()) != null){
				stream.write(line + "\n");
				System.out.println(line);
			}
			jsonReader.close();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}



	public String urlOutput(POUTPUT output){
		return switch (output){
			case POUTPUT.COMPACT -> "outputsize=compact";
			case POUTPUT.FULL -> "outputsize=full";

		};
	}



	public String urlFunction(PFUNCTION function){
		return switch (function){
			case PFUNCTION.INTRADAY -> "function=TIME_SERIES_INTRADAY";
			case PFUNCTION.DAILY    -> "function=TIME_SERIES_DAILY";
			case PFUNCTION.WEEKLY   -> "function=TIME_SERIES_WEEKLY";
			case PFUNCTION.MONTHLY  -> "function=TIME_SERIES_MONTHLY";
		};
	}


	public static void main(String[] args) {
		System.out.println("Connecting to AlphaVantage");
		AlphaVantageConnection connection = new AlphaVantageConnection();
		AlphaVantageJsonParser json = new AlphaVantageJsonParser();
		try {
			connection.connect("MMM", PFUNCTION.DAILY, POUTPUT.COMPACT);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		//json.parseJsonToAktien()
		System.out.println("End of Connection");
	}

}
