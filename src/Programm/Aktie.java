package Programm;
import java.time.LocalDate;

public class Aktie {
	private String symbol;
	private LocalDate date;
	private double open;
	private double high;
	private double low;
	private double close;
	private long volume;

	public Aktie(LocalDate date, String symbol, double high, double open, double low, double close, long volume) {
		this.date = date;
		this.symbol = symbol;
		this.high = high;
		this.open = open;
		this.low = low;
		this.close = close;
		this.volume = volume;
	}

	public String getSymbol() {
		return symbol;
	}

	public double getOpen() {
		return open;
	}

	public LocalDate getDate() {
		return date;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public long getVolume() {
		return volume;
	}

	public double getClose() {
		return close;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setHigh(double high) {
		this.high = high;
	}


}
