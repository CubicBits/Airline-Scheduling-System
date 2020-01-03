package solution;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import baseclasses.DataLoadingException;
import baseclasses.IRouteDAO;
import baseclasses.Route;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.*;
import java.io.*;



/**
 * The RouteDAO parses XML files of route information, each route specifying
 * where the airline flies from, to, and on which day of the week
 */
public class RouteDAO implements IRouteDAO {

	ArrayList<Route> routes = new ArrayList<>();

	/**
	 * Loads the route data from the specified file, adding them to the currently loaded routes
	 * Multiple calls to this function, perhaps on different files, would thus be cumulative
	 * @param p A Path pointing to the file from which data could be loaded
	 * @throws DataLoadingException if anything goes wrong. The exception's "cause" indicates the underlying exception
	 */
	@Override
	public void loadRouteData(Path p) throws DataLoadingException {
//		When supplied with a Path object pointing to an XML file formatted as specified earlier, this method
//		should cause the DAO to load the routes contained within the file into an appropriate data structure
//				(e.g. an ArrayList), setting the properties of the added route objects to appropriately reflect the
//		values of the routes in the file. As with the previous DAOs, multiple calls to the loading method
//		should be additive: it should be possible to load more than one file’s worth of routes into a single
//		DAO. If there is a problem loading the data, perhaps because a file is malformed, then a
//		DataLoadingException should be thrown.
		
		try {
			File inputFile = new File(String.valueOf(p));
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			NodeList routeList = doc.getElementsByTagName("Route");

			for (int i = 0; i < routeList.getLength(); i++) {
				Node route = routeList.item(i);

				if (route.getNodeType() == Node.ELEMENT_NODE) {
					
					Element routeElement = (Element) route;
					int flightNumber = Integer.parseInt(routeElement
							.getElementsByTagName("FlightNumber")
							.item(0).getTextContent());
					String dayOfWeek = routeElement.getElementsByTagName("DayOfWeek").item(0).getTextContent();
					
					if (!dayOfWeek.equals("Mon") && !dayOfWeek.equals("Tue") && !dayOfWeek.equals("Wed") && !dayOfWeek.equals("Thu") && !dayOfWeek.equals("Fri") && !dayOfWeek.equals("Sat") && !dayOfWeek.equals("Sun")) {
						throw new DataLoadingException(new Throwable("Invalid day of week"));
					}
					
					LocalTime departureTime = LocalTime.parse(routeElement
							.getElementsByTagName("DepartureTime")
							.item(0).getTextContent());
					String departureAirport = routeElement
							.getElementsByTagName("DepartureAirport")
							.item(0).getTextContent();
					String departureAirportCode = routeElement
							.getElementsByTagName("DepartureAirportCode")
							.item(0).getTextContent();
					LocalTime arrivalTime = LocalTime.parse(routeElement
							.getElementsByTagName("ArrivalTime")
							.item(0).getTextContent());
					String arrivalAirport = routeElement
							.getElementsByTagName("ArrivalAirport")
							.item(0).getTextContent();
					String arrivalAirportCode = routeElement
							.getElementsByTagName("ArrivalAirportCode")
							.item(0).getTextContent();
					Duration duration = Duration.parse(routeElement
							.getElementsByTagName("Duration")
							.item(0).getTextContent());

					// create and populate Route object
					Route routeObj = new Route();
					this.routes.add(routeObj);
					routeObj.setFlightNumber(flightNumber);
					routeObj.setDayOfWeek(dayOfWeek);
					routeObj.setDepartureTime(departureTime);
					routeObj.setDepartureAirport(departureAirport);
					routeObj.setDepartureAirportCode(departureAirportCode);
					routeObj.setArrivalTime(arrivalTime);
					routeObj.setArrivalAirport(arrivalAirport);
					routeObj.setArrivalAirportCode(arrivalAirportCode);
					routeObj.setDuration(duration);
				}
			}
		} 
		catch (FileNotFoundException fne) {
			//There was a problem reading the file
			throw new DataLoadingException(fne);
		}
		catch (IOException ioe) {
			//There was a problem reading the file
			throw new DataLoadingException(ioe);
		}
		catch (IllegalArgumentException iae) {
			// there was a problem reading the file
			throw new DataLoadingException(iae);
		} 
		catch (ParserConfigurationException e) {
		 	throw new DataLoadingException(e);
		}
		catch (SAXParseException e) {
			throw new DataLoadingException(e);
		}
		catch (SAXException e) {
			throw new DataLoadingException(e);
		} 
		catch (DateTimeParseException e) {
			throw new DataLoadingException(e);
		}
		catch (Exception e) {
			throw new DataLoadingException(e);
		}



	}

	/**
	 * Finds all flights that depart on the specified day of the week
	 * @param dayOfWeek A three letter day of the week, e.g. "Tue"
	 * @return A list of all routes that depart on this day
	 */
	@Override
	public List<Route> findRoutesByDayOfWeek(String dayOfWeek) {
		// TODO still fully test
		ArrayList<Route> res = new ArrayList<>();
		for (Route route : routes){
			if (route.getDayOfWeek().equals(dayOfWeek)) {
				res.add(route);
			}
		}
		return res;
	}

	/**
	 * Finds all of the flights that depart from a specific airport on a specific day of the week
	 * @param airportCode the three letter code of the airport to search for, e.g. "MAN"
	 * @param dayOfWeek the three letter day of the week code to search for, e.g. "Tue"
	 * @return A list of all routes from that airport on that day
	 */
	@Override
	public List<Route> findRoutesByDepartureAirportAndDay(String airportCode, String dayOfWeek) {
		// TODO still to fully test
		// search for dayOfWeek first, then by airportCode
		ArrayList<Route> res = new ArrayList<>();
		for (Route route : routes){
			if (route.getDayOfWeek().equals(dayOfWeek)) {
				if (route.getDepartureAirportCode().equals(airportCode)) {
					res.add(route);
				}
			}
		}	
		return res;
	}

	/**
	 * Finds all of the flights that depart from a specific airport
	 * @param airportCode the three letter code of the airport to search for, e.g. "MAN"
	 * @return A list of all of the routes departing the specified airport
	 */
	@Override
	public List<Route> findRoutesDepartingAirport(String airportCode) {
		// TODO still to fully test
		ArrayList<Route> res = new ArrayList<>();
		for (Route route : routes){
			if (route.getDepartureAirportCode().equals(airportCode)) {
				res.add(route);
			}
		}	
		return res;
	}

	/**
	 * Finds all of the flights that depart on the specified date
	 * @param date the date to search for
	 * @return A list of all routes that depart on this date
	 */
	@Override
	public List<Route> findRoutesbyDate(LocalDate date) { // Java LocalDate format: MONDAY
		// TODO still to be fully tested
		// TODO Check that this function should find result data, by getDayOfWeek() of a date 2019-08-12 date format.
		// convert DayOfWeek enum type date to a string in with format Mon/Tue/Wed and compare with List routes for match
		ArrayList<Route> res = new ArrayList<>();
		String strDate = date.getDayOfWeek().toString();
		switch (strDate) {
			case "MONDAY":
				strDate = "Mon";
				break;
			case "TUESDAY":
				strDate = "Tue";
				break;
			case "WEDNESDAY":
				strDate = "Wed";
				break;
			case "THURSDAY":
				strDate = "Thu";
				break;
			case "FRIDAY":
				strDate = "Fri";
				break;
			case "SATURDAY":
				strDate = "Sat";
				break;
			case "SUNDAY":
				strDate = "Sun";
				break;
		}
		for (Route route : routes){
			if (route.getDayOfWeek().equals(strDate)) {
				res.add(route);
			}
		}
		return res;
	}

	/**
	 * Returns The full list of all currently loaded routes
	 * @return The full list of all currently loaded routes
	 */
	@Override
	public List<Route> getAllRoutes() {
		// TODO still to fully test
		return new ArrayList<>(routes);
	}

	/**
	 * Returns The number of routes currently loaded
	 * @return The number of routes currently loaded
	 */
	@Override
	public int getNumberOfRoutes() {
		// TODO still to fully test
		return routes.size();
	}

	/**
	 * Unloads all of the crew currently loaded, ready to start again if needed
	 */
	@Override
	public void reset() {
		// TODO still to fully test
		routes.clear();
	}

}
