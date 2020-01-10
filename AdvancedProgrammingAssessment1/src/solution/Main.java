package solution;

import java.nio.file.Paths;
import java.time.LocalDate;

import baseclasses.*;

/**
 * This class allows you to run the code in your classes yourself, for testing and development
 */
public class Main {

	public static void main(String[] args) {
		AircraftDAO aircraft = new AircraftDAO();
		CrewDAO crew = new CrewDAO();
		RouteDAO route = new RouteDAO();
		PassengerNumbersDAO passengers = new PassengerNumbersDAO();

		// load Aircraft data file
		try {
			aircraft.loadAircraftData(Paths.get("./data/aircraft.csv"));
//			aircraft.loadAircraftData(null); // load null
		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading aircraft data");
			dle.printStackTrace();
		}

		// load Crew data file
		try {
			crew.loadCrewData(Paths.get("./data/crew.json"));
//			crew.loadCrewData(null); // load null
		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading crew data");
			dle.printStackTrace();
		}

		// load Routes XML file
		try {
			route.loadRouteData(Paths.get("./data/routes.xml"));
//			route.loadRouteData(Paths.get("./data/malformed_routes5.xml")); // contains wrong tag error
//			route.loadRouteData(Paths.get("./data/malformed_routes3.xml")); // invalid Day of week (Tee)
//			route.loadRouteData(Paths.get("./data/malformed_routes4.xml")); // duration error
//			route.loadRouteData(null); // load null
		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading route data");
			dle.printStackTrace();
		}

		// load Passenger Numbers SQLite db
		try {
			passengers.loadPassengerNumbersData(Paths.get("./data/schedule_passengers.db"));
//			passengers.loadPassengerNumbersData(null); // load null
//			System.out.println(passengers.getPassengerNumbersFor(1162222229,LocalDate.parse("2020-07-07")));
		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading route data");
			dle.printStackTrace();
		}

		Scheduler scheduler = new Scheduler(); // initialise Scheduler object & hold variable reference for setting SchedulerRunner later
		SchedulerRunner schedulerRunner = new SchedulerRunner(aircraft, crew, route, passengers, LocalDate.parse("2020-07-01"), LocalDate.parse("2020-08-31"), scheduler);
		scheduler.setSchedulerRunner(schedulerRunner);
		
		schedulerRunner.run();
		

	}

}
