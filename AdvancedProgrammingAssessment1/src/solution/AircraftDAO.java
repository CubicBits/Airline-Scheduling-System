package solution;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import baseclasses.Aircraft;
import baseclasses.DataLoadingException;
import baseclasses.IAircraftDAO;

/**
 * Jack Owen
 * Manchester Metropolitan University id: 17032929
 */

/**
 * The AircraftDAO class is responsible for loading aircraft data from CSV files
 * and contains methods to help the system find aircraft when scheduling
 */
public class AircraftDAO implements IAircraftDAO {
	List<Aircraft> fleet = new ArrayList<Aircraft>();
		
	/**
	 * Loads the aircraft data from the specified file, adding them to the currently loaded aircraft
	 * Multiple calls to this function, perhaps on different files, would thus be cumulative
	 * @param p A Path pointing to the file from which data could be loaded
	 * @throws DataLoadingException if anything goes wrong. The exception's "cause" indicates the underlying exception
     *
	 * Initially, this contains some starter code to help you get started in reading the CSV file...
	 */
	@Override
	public void loadAircraftData(Path p) throws DataLoadingException {	
		try {
			//open the file
			BufferedReader reader = Files.newBufferedReader(p);

			//read the file line by line
			String line = "";

			//skip the first line of the file - headers
			reader.readLine();

			while( (line = reader.readLine()) != null) {
				//each line has fields separated by commas, split into an array of fields
				String[] fields = line.split(",");

				String tailcode = fields[0];
				String type = fields[1];
				Aircraft.Manufacturer man = Aircraft.Manufacturer.valueOf(fields[2].toUpperCase());
				String model = fields[3];
				int seats = Integer.parseInt(fields[4]);
				int cabinCrewRequired = Integer.parseInt(fields[5]);
				String startLocation = fields[6];

				Aircraft aircraft = new Aircraft();
				this.fleet.add(aircraft);
				aircraft.setTailCode(tailcode.toUpperCase());
				aircraft.setTypeCode(type.toUpperCase());
				aircraft.setManufacturer(man);
				aircraft.setModel(model.toUpperCase());
				aircraft.setSeats(seats);
				aircraft.setCabinCrewRequired(cabinCrewRequired);
				aircraft.setStartingPosition(startLocation.toUpperCase());

			}
		}
		catch (IOException ioe) {
			//There was a problem reading the file
			throw new DataLoadingException(ioe);
		}
		catch (IllegalArgumentException iae) {
			// there was a problem reading the file
			throw new DataLoadingException(iae);
		}
		catch (NullPointerException npe) {
			throw new DataLoadingException(npe);
		}
		catch (Exception e) {
			throw new DataLoadingException(e);
		}
	}
	
	/**
	 * Returns a list of all the loaded Aircraft with at least the specified number of seats
	 * @param seats the number of seats required
	 * @return a List of all the loaded aircraft with at least this many seats
	 */
	@Override
	public List<Aircraft> findAircraftBySeats(int seats) {
		List<Aircraft> result = new ArrayList<Aircraft>();
		for (Aircraft aircraft : fleet) {
			if (aircraft.getSeats() >= seats) {
				result.add(aircraft);
			}
		}
		return result;
	}

	/**
	 * Returns a list of all the loaded Aircraft that start at the specified airport code
	 * @param startingPosition the three letter airport code of the airport at which the desired aircraft start
	 * @return a List of all the loaded aircraft that start at the specified airport
	 */
	@Override
	public List<Aircraft> findAircraftByStartingPosition(String startingPosition) {
		List<Aircraft> result = new ArrayList<Aircraft>();
		for (Aircraft aircraft : fleet) {
			if (aircraft.getStartingPosition().equals(startingPosition.toUpperCase())) {
				result.add(aircraft);
			}
		}
		return result;
	}

	/**
	 * Returns the individual Aircraft with the specified tail code.
	 * @param tailCode the tail code for which to search
	 * @return the aircraft with that tail code, or null if not found
	 */
	@Override
	public Aircraft findAircraftByTailCode(String tailCode) {
		for (Aircraft aircraft : fleet) {
			if (aircraft.getTailCode().equals(tailCode.toUpperCase())) {
				return aircraft;
			}	
		}
		return null;
	}

	/**
	 * Returns a List of all the loaded Aircraft with the specified type code
	 * @param typeCode the type code of the aircraft you wish to find
	 * @return a List of all the loaded Aircraft with the specified type code
	 */
	@Override
	public List<Aircraft> findAircraftByType(String typeCode) {
		List<Aircraft> result = new ArrayList<Aircraft>();
		for (Aircraft aircraft : fleet) {
			if (aircraft.getTypeCode().equals(typeCode)) {
				result.add(aircraft);
			}
		}
		return result;
	}

	/**
	 * Returns a List of all the currently loaded aircraft
	 * @return a List of all the currently loaded aircraft
	 */
	@Override
	public List<Aircraft> getAllAircraft() {
		return new ArrayList<>(fleet);
	}

	/**
	 * Returns the number of aircraft currently loaded 
	 * @return the number of aircraft currently loaded
	 */
	@Override
	public int getNumberOfAircraft() {
		return fleet.size();
	}

	/**
	 * Unloads all of the aircraft currently loaded, ready to start again if needed
	 */
	@Override
	public void reset() {
		fleet.clear();
	}

}
