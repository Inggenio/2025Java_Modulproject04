package Programm;

import java.sql.*;
import java.util.List;

public class DatabaseManager {

	private static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/kurs";
	private static final String DB_USER = "koko63";
	private static final String DB_PASSWORD = "verysecure";

	public void saveAktie(List<Aktie> dataList) throws Exception {
		if (dataList == null || dataList.isEmpty()) {
			System.out.println("Keine Daten zum Speichern vorhanden");
			return;
		}

		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			conn.setAutoCommit(false);


			String sql = "INSERT INTO stock_prices (symbol, date, open, high, low, close, volume) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?)";

			stmt = conn.prepareStatement(sql);

			int successCount = 0;
			int skipCount = 0;

			for (Aktie stock : dataList) {
				try {
					stmt.setString(1, stock.getSymbol());
					stmt.setDate(2, Date.valueOf(stock.getDate()));
					stmt.setDouble(3, stock.getOpen());
					stmt.setDouble(4, stock.getHigh());
					stmt.setDouble(5, stock.getLow());
					stmt.setDouble(6, stock.getClose());
					stmt.setLong(7, stock.getVolume());

					stmt.executeUpdate();
					successCount++;

				} catch (SQLException e) {
					// Duplikat-Fehler ignorieren
					if (e.getMessage().contains("duplicate key") ||
							e.getMessage().contains("unique constraint") ||
							e.getSQLState().equals("23505")) {
						skipCount++;
						System.out.println("Datensatz bereits vorhanden: " +
								stock.getSymbol() + " " + stock.getDate());
					} else {

						throw e;
					}
				}
			}

			conn.commit();

			System.out.println("Datenbank-Operation abgeschlossen:");
			System.out.println("- " + successCount + " neue Datensätze eingefügt");
			System.out.println("- " + skipCount + " Duplikate übersprungen");

		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.rollback();
					System.err.println("Transaction wurde zurückgerollt");
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			throw new Exception("Datenbankfehler: " + e.getMessage(), e);
		} finally {
			if (stmt != null) stmt.close();
			if (conn != null) conn.close();
		}
	}
}