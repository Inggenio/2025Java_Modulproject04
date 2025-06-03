package Programm;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

public class DatabaseManager {

	private static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/kurs";
	private static final String DB_USER = "koko63";
	private static final String DB_PASSWORD = "verysecure";

	public void saveAktie(List<Aktie> dataList) throws Exception {
		Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

		String sql = "INSERT INTO stock_prices (symbol, date, open, high, low, close, volume) VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			for (Aktie stock : dataList) {
				stmt.setString(1, stock.getSymbol());
				stmt.setDate(2, Date.valueOf(stock.getDate()));
				stmt.setDouble(3, stock.getOpen());
				stmt.setDouble(4, stock.getHigh());
				stmt.setDouble(5, stock.getLow());
				stmt.setDouble(6, stock.getClose());
				stmt.setLong(7, stock.getVolume());


			}

			stmt.executeBatch();
		}

		conn.close();
	}
}
