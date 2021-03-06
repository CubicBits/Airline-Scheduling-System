package solution;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;

import baseclasses.DataLoadingException;
import baseclasses.IPassengerNumbersDAO;

import java.util.HashMap;
import java.util.List;

import org.sqlite.*;

/**
 * Jack Owen
 * Manchester Metropolitan University id: 17032929
 */

/**
 * The PassengerNumbersDAO is responsible for loading an SQLite database
 * containing forecasts of passenger numbers for flights on dates
 */
public class PassengerNumbersDAO implements IPassengerNumbersDAO {

	HashMap<String, Integer> passengerNumbers = new HashMap<>();

	/**
	 * Returns the number of passenger number entries in the cache
	 * @return the number of passenger number entries in the cache
	 */
	@Override
	public int getNumberOfEntries() {
		// TODO still fully test
		return passengerNumbers.size();
	}

	/**
	 * Returns the predicted number of passengers for a given flight on a given date, or -1 if no data available
	 * @param flightNumber The flight number of the flight to check for
	 * @param date the date of the flight to check for
	 * @return the predicted number of passengers, or -1 if no data available
	 */
	@Override
	public int getPassengerNumbersFor(int flightNumber, LocalDate date) {
		try {
			return passengerNumbers.get(date.toString()+":"+flightNumber);
		}
		catch (NullPointerException npe) {
			return -1;
		}
	}

	/**
	 * Loads the passenger numbers data from the specified SQLite database into a cache for future calls to getPassengerNumbersFor()
	 * Multiple calls to this method are additive, but flight numbers/dates previously cached will be overwritten
	 * The cache can be reset by calling reset() 
	 * @param p The path of the SQLite database to load data from
	 * @throws DataLoadingException If there is a problem loading from the database
	 */
	@Override
	public void loadPassengerNumbersData(Path p) throws DataLoadingException {

		Connection connection = null;

		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"+String.valueOf(p));
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery("select * from PassengerNumbers");

			while(rs.next())
			{
				// read and add result set to Hash Map
				String date = rs.getString("date");
				Integer flightNumber = rs.getInt("FlightNumber");
				Integer passengerQty = rs.getInt("Passengers");
				String key = date + ":" +flightNumber.toString(); //? is this the most efficient solution to storing a two value Key of different types
				passengerNumbers.put(key, passengerQty);
			}
		}
		catch(SQLException e)
		{
			throw new DataLoadingException(e);
		}
		catch(NullPointerException e)
		{
			throw new DataLoadingException(e);
		}
		finally
		{
			try
			{
				if(connection != null)
					connection.close();
			}
			catch(SQLException e)
			{
				// connection close failed.
				System.err.println(e.getMessage());
				throw new DataLoadingException(e);
			}
		}

	}

	/**
	 * Removes all data from the DAO, ready to start again if needed
	 */
	@Override
	public void reset() {
		passengerNumbers.clear();
	}

}
