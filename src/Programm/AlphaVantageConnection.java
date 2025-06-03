package Programm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.text.SimpleDateFormat;



public class AlphaVantageConnection {
	public String symbol;
	public PFUNCTION function;
	public POUTPUT output;

	public AlphaVantageConnection() {
	}

	public AlphaVantageConnection(String symbol, PFUNCTION function, POUTPUT output) {
		this.symbol = symbol;
		this.function = function;
		this.output = output;
	}

	public String getSymbol() {
		return symbol;
	}

	public PFUNCTION getFunction() {
		return function;
	}

	public POUTPUT getOutput() {
		return output;
	}
	public String filename(String symbol){
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
		return "JSON_Streams/Stream_" + timeStamp +"_sym_" + symbol + ".txt";

	}
	public String getFilename(){
		return filename(symbol);
	}

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


		FileWriter stream = new FileWriter(filename(symbol));

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

}
