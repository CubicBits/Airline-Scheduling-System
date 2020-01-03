package solution;

import java.nio.file.Paths;
import java.time.LocalDate;

import baseclasses.*;

/**
 * This class allows you to run the code in your classes yourself, for testing and development
 */
public class Main {

	public static void main(String[] args) {
		// TODO google search a number of exceptions every function in the DAO's can throw, and catch these.
		// TODO check for additional file formats that may cause an error when being read.


		AircraftDAO aircraft = new AircraftDAO();
		CrewDAO crew = new CrewDAO();
		RouteDAO route = new RouteDAO();
		PassengerNumbersDAO passengers = new PassengerNumbersDAO();

		// load Aircraft data file
		try {
			aircraft.loadAircraftData(Paths.get("./data/aircraft.csv"));
		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading aircraft data");
			dle.printStackTrace();
		}

		// load Crew data file
		try {
			crew.loadCrewData(Paths.get("./data/crew.json"));
		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading crew data");
			dle.printStackTrace();
		}

		// load Routes XML file
		try {
			route.loadRouteData(Paths.get("./data/routes.xml"));
//			route.loadRouteData(Paths.get("./data/malformed_routes5.xml")); // contains wrong tag error
//			route.loadRouteData(Paths.get("./data/malformed_routes3.xml"));
//			route.loadRouteData(Paths.get("./data/malformed_routes4.xml")); // duration error
//			System.out.println(route.findRoutesbyDate(LocalDate.parse("2019-09-06")));
//			System.out.println(route.findRoutesByDepartureAirportAndDay("MAN", "Tue"));
		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading route data");
			dle.printStackTrace();
		}

		// load Passenger Numbers SQLite db
		try {
			passengers.loadPassengerNumbersData(Paths.get("./data/schedule_passengers.db"));
//			System.out.println(passengers.getPassengerNumbersFor(1162222229,LocalDate.parse("2020-07-07")));
		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading route data");
			dle.printStackTrace();
		}

		Scheduler scheduler = new Scheduler(); // initialise Scheduler object & hold variable reference for setting SchedulerRunner later
		SchedulerRunner schedulerRunner = new SchedulerRunner(aircraft, crew, route, passengers, LocalDate.parse("2020-07-01"), LocalDate.parse("2020-08-31"), scheduler);
		scheduler.setSchedulerRunner(schedulerRunner);
		
		Schedule schedule = schedulerRunner.run();

//		QualityScoreCalculator score = new QualityScoreCalculator(aircraft, crew, passengers, schedule); // submit final schedule for score analysis
//		
//		String[] a = new QualityScoreCalculator(aircraft, crew, passengers, schedule).describeQualityScore();
//		for (String b : a) {
//		  	System.out.println(b);
//		  }
//		System.out.println(score.calculateQualityScore()+" (score total)");

	}

}
