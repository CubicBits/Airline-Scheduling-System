package solution;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import baseclasses.Aircraft;
import baseclasses.CabinCrew;
import baseclasses.Crew;
import baseclasses.DataLoadingException;
import baseclasses.ICrewDAO;
import baseclasses.Pilot;

/**
 * The CrewDAO is responsible for loading data from JSON-based crew files It
 * contains various methods to help the scheduler find the right pilots and
 * cabin crew
 */
public class CrewDAO implements ICrewDAO {

	List<Pilot> pilots = new ArrayList<Pilot>();
	ArrayList<CabinCrew> cabinCrew = new ArrayList<CabinCrew>();

	/**
	 * Loads the crew data from the specified file, adding them to the currently
	 * loaded crew Multiple calls to this function, perhaps on different files,
	 * would thus be cumulative
	 * 
	 * @param p A Path pointing to the file from which data could be loaded
	 * @throws DataLoadingException if anything goes wrong. The exception's "cause"
	 *                              indicates the underlying exception
	 */
	@Override
	public void loadCrewData(Path p) throws DataLoadingException {

		try {
	        String content = new String(Files.readAllBytes(p));
	        JSONObject jsonObject = new JSONObject(content);
	        JSONArray pilots = new JSONArray(jsonObject.get("pilots").toString());
	        JSONArray allCabincrew = new JSONArray(jsonObject.get("cabincrew").toString());

	        // create and populate Pilot objects
	        for (int i=0; i < pilots.length(); i++) {
	        	JSONObject pilot = pilots.getJSONObject(i); // store a single JSON Pilot record, to be used by multiple get methods later

	        	String forename = pilot.get("forename").toString();
	        	String surname = pilot.get("surname").toString();
	        	String rank = pilot.get("rank").toString();
	        	String homeBase = pilot.get("homebase").toString();
	        	JSONArray typeCodes = new JSONArray(pilot.get("typeRatings").toString()); // get list of qualified aircrafts

	        	Pilot staff = new Pilot();
	        	this.pilots.add(staff); // add new Pilot Object to ArrayList
	        	staff.setForename(forename);
	        	staff.setSurname(surname);
	        	staff.setRank(Pilot.Rank.valueOf(rank.toUpperCase()));
	        	staff.setHomeBase(homeBase); // also set Home Base names to upper case
	        	for(Object item : typeCodes) {
	        		staff.setQualifiedFor(item.toString()); // also set Type Codes to upper case
	        	}
	        }

	        // create and populate CabinCrew objects
	        for (int i=0; i < allCabincrew.length(); i++) {
	        	JSONObject cabincrew = allCabincrew.getJSONObject(i);

	        	String forename = cabincrew.get("forename").toString();
	        	String surname = cabincrew.get("surname").toString();
	        	String homeBase = cabincrew.get("homebase").toString();
	        	JSONArray typeCodes = new JSONArray(cabincrew.get("typeRatings").toString());

	        	CabinCrew staff = new CabinCrew();
	        	this.cabinCrew.add(staff);
	        	staff.setForename(forename);
	        	staff.setSurname(surname);
	        	staff.setHomeBase(homeBase);
	        	for(Object item : typeCodes) {
	        		staff.setQualifiedFor(item.toString());
	        	}
	        }

		}
		catch (IOException ioe) {
			//There was a problem reading the file
			throw new DataLoadingException(ioe);
		}
		catch (OutOfMemoryError e) {
			throw new DataLoadingException(e);
		}
		catch (SecurityException e) {
			throw new DataLoadingException(e);
		}
		catch (JSONException e) {
			throw new DataLoadingException(e);
		}
		catch (NullPointerException e) {
			throw new DataLoadingException(e);
		}
		catch (Exception e) {
			throw new DataLoadingException(e);
		}
	}

	/**
	 * Returns a list of all the cabin crew based at the airport with the specified
	 * airport code
	 * 
	 * @param airportCode the three-letter airport code of the airport to check for
	 * @return a list of all the cabin crew based at the airport with the specified
	 *         airport code
	 */
	@Override
	public List<CabinCrew> findCabinCrewByHomeBase(String airportCode) {
		// still to be tested
		List<CabinCrew> res = new ArrayList<CabinCrew>();
		for (CabinCrew cabincrew : cabinCrew) { 
			if (cabincrew.getHomeBase().toUpperCase().equals(airportCode.toUpperCase())) {
				res.add(cabincrew);
			}
		}
		return res;
	}

	/**
	 * Returns a list of all the cabin crew based at a specific airport AND
	 * qualified to fly a specific aircraft type
	 * 
	 * @param typeCode    the type of plane to find cabin crew for
	 * @param airportCode the three-letter airport code of the airport to check for
	 * @return a list of all the cabin crew based at a specific airport AND
	 *         qualified to fly a specific aircraft type
	 */
	@Override
	public List<CabinCrew> findCabinCrewByHomeBaseAndTypeRating(String typeCode, String airportCode) {
		// still to test fully 
		List<CabinCrew> res = new ArrayList<CabinCrew>();
		for (CabinCrew cabincrew : cabinCrew) { 
			if (cabincrew.getHomeBase().toUpperCase().equals(airportCode.toUpperCase())) {
				for (String type : cabincrew.getTypeRatings()) {
					if (type.toUpperCase().equals(typeCode.toUpperCase())) {
						res.add(cabincrew);
					}
				}
			}
		}
		return res;
	}

	/**
	 * Returns a list of all the cabin crew currently loaded who are qualified to
	 * fly the specified type of plane
	 * 
	 * @param typeCode the type of plane to find cabin crew for
	 * @return a list of all the cabin crew currently loaded who are qualified to
	 *         fly the specified type of plane
	 */
	@Override
	public List<CabinCrew> findCabinCrewByTypeRating(String typeCode) {
		// still to be tested fully
		List<CabinCrew> res = new ArrayList<CabinCrew>();
		for (CabinCrew cabincrew : cabinCrew) { 
			for (String type : cabincrew.getTypeRatings()) {
				if (type.toUpperCase().equals(typeCode.toUpperCase())) {
					res.add(cabincrew);
				}
			}
		}
		return res;
	}

	/**
	 * Returns a list of all the pilots based at the airport with the specified
	 * airport code
	 * 
	 * @param airportCode the three-letter airport code of the airport to check for
	 * @return a list of all the pilots based at the airport with the specified
	 *         airport code
	 */
	@Override
	public List<Pilot> findPilotsByHomeBase(String airportCode) {
		// still to be tested
		List<Pilot> res = new ArrayList<Pilot>();
		for (Pilot pilot : pilots) { 
			if (pilot.getHomeBase().toUpperCase().equals(airportCode.toUpperCase())) {
				res.add(pilot);
			}
		}
		return res;
	}

	/**
	 * Returns a list of all the pilots based at a specific airport AND qualified to
	 * fly a specific aircraft type
	 * 
	 * @param typeCode    the type of plane to find pilots for
	 * @param airportCode the three-letter airport code of the airport to check for
	 * @return a list of all the pilots based at a specific airport AND qualified to
	 *         fly a specific aircraft type
	 */
	@Override
	public List<Pilot> findPilotsByHomeBaseAndTypeRating(String typeCode, String airportCode) {
		// still to be fully tested
		ArrayList<Pilot> res = new ArrayList<Pilot>();
		for (Pilot pilot : pilots) {
			if (pilot.getHomeBase().toUpperCase().equals(airportCode.toUpperCase())) {
				for (String type : pilot.getTypeRatings()) {
					if (type.toUpperCase().equals(typeCode.toUpperCase())) {
						res.add(pilot);
					}
				}
			}
		}
		return res;
	}

	/**
	 * Returns a list of all the pilots currently loaded who are qualified to fly
	 * the specified type of plane
	 * 
	 * @param typeCode the type of plane to find pilots for
	 * @return a list of all the pilots currently loaded who are qualified to fly
	 *         the specified type of plane
	 */
	@Override
	public List<Pilot> findPilotsByTypeRating(String typeCode) {
		// still to be tested
		List<Pilot> res = new ArrayList<Pilot>();
		for (Pilot pilot : pilots) { 
			for (String type : pilot.getTypeRatings()) {
				if (type.toUpperCase().equals(typeCode.toUpperCase())) {
					res.add(pilot);
				}
			}
		}
		return res;
	}

	/**
	 * Returns a list of all the cabin crew currently loaded
	 * 
	 * @return a list of all the cabin crew currently loaded
	 */
	@Override
	public List<CabinCrew> getAllCabinCrew() {
		return new ArrayList<>(cabinCrew);
	}

	/**
	 * Returns a list of all the crew, regardless of type
	 * 
	 * @return a list of all the crew, regardless of type
	 */
	@Override
	public List<Crew> getAllCrew() {
		List<Crew> allCrew = new ArrayList<>();
		allCrew.addAll(pilots);
		allCrew.addAll(cabinCrew);

		return allCrew;
	}

	/**
	 * Returns a list of all the pilots currently loaded
	 * 
	 * @return a list of all the pilots currently loaded
	 */
	@Override
	public List<Pilot> getAllPilots() {
		return new ArrayList<>(pilots);
	}

	@Override
	public int getNumberOfCabinCrew() {
		// still to be fully tested
		return cabinCrew.size();
	}

	/**
	 * Returns the number of pilots currently loaded
	 * 
	 * @return the number of pilots currently loaded
	 */
	@Override
	public int getNumberOfPilots() {
		// still to be tested
		return pilots.size();
	}

	/**
	 * Unloads all of the crew currently loaded, ready to start again if needed
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		pilots.clear();
		cabinCrew.clear();
	}

}
