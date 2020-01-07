package solution;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import baseclasses.*;


public class Scheduler implements IScheduler {
    Schedule schedule;
    SchedulerRunner schedulerRunner;
    boolean stop = false;
    long bestScheduleSoFarScore = 10000000000L;
    HashMap<CabinCrew, List<LocalDateTime>> cabinCrewArrivalsInUK;
    HashMap<Pilot, List<LocalDateTime>> pilotsArrivalsInUK;

    @Override
    public Schedule generateSchedule(IAircraftDAO aircraftDAO, ICrewDAO crewDAO, IRouteDAO routeDAO, IPassengerNumbersDAO passengerNumbersDAO,
                                     LocalDate startDate, LocalDate endDate) {
    	new Thread(new Runnable() {
    		public void run() {
	    		try { 
	    			Thread.sleep(120000); 
	    			}
	    		catch (InterruptedException e) { 
	    			e.printStackTrace(); 
	    			}
	    		stop();
    		}
    	}).start();
    	
    	System.out.println("Running...");
    	this.schedule = new Schedule(routeDAO, startDate, endDate);
        cabinCrewArrivalsInUK = new HashMap<CabinCrew, List<LocalDateTime>>(); // temporary crew data hold for UK arrivals
        pilotsArrivalsInUK = new HashMap<Pilot, List<LocalDateTime>>(); // temporary crew data hold for UK arrivals
        
    	/*while (!schedule.isCompleted()) {
    	    FlightInfo flight = schedule.getRemainingAllocations().get(0);
    	    allocateAircraft(aircraftDAO, flight);
    	    allocateCaptain(crewDAO, flight);
    	    allocateFirstOfficer(crewDAO, flight, 2); // int val being the seed to shuffle
            allocateCabinCrew(crewDAO, flight);
            try {
                schedule.completeAllocationFor(flight);
            }
            catch (InvalidAllocationException e)
            {
                e.printStackTrace();
		        System.err.print("invalid allocation");
            }
    	}*/
    	
        List<FlightInfo> flights = schedule.getRemainingAllocations();
        ArrayList<ArrayList<FlightInfo>> flightPairing = new ArrayList<>(); // two-dimensional arraylist legA:legB

        /* Create Flight Pairings, outbound and return Flights */
        FlightInfo outboundFlight;
        String outboundArrivalAirport;
        String outboundDepartureAirport;
        while (!flights.isEmpty()) {
            outboundFlight = flights.get(0);
            outboundArrivalAirport = outboundFlight.getFlight().getArrivalAirportCode();
            outboundDepartureAirport = outboundFlight.getFlight().getDepartureAirportCode();
            for (FlightInfo returnFlight : flights) {
                if (returnFlight.getFlight().getDepartureAirportCode().equals(outboundArrivalAirport)
                        && outboundFlight.getLandingDateTime().compareTo(returnFlight.getDepartureDateTime()) < 0
                        && returnFlight.getFlight().getArrivalAirportCode().equals(outboundDepartureAirport)
                    ) {
                    ArrayList<FlightInfo> pair = new ArrayList<>();
                    pair.add(outboundFlight);
                    pair.add(returnFlight);
                    flightPairing.add(pair);
                    flights.remove(outboundFlight);
                    flights.remove(returnFlight);
                    break;
                }
            }
            if (flights.contains(outboundFlight)) {
                ArrayList<FlightInfo> pair = new ArrayList<>();
                pair.add(outboundFlight); // no return flight found YET
                pair.add(null);
                flightPairing.add(pair);
                flights.remove(outboundFlight);
            }
        }

        // allocate matching aircraft to pairings
        for (int i=0; i<flightPairing.size(); i++) {
            if (flightPairing.get(i).get(1) == null) {
                allocateAircraft(aircraftDAO, flightPairing.get(i).get(0));
            }
            else {
                allocateAircraftToMatchingFlights(aircraftDAO, flightPairing.get(i).get(0), flightPairing.get(i).get(1));
            }
        }

        // allocate matching Captain to pairings
        for (int i=0; i<flightPairing.size(); i++) {
            if (flightPairing.get(i).get(1) == null) {
                allocateCaptain(crewDAO, flightPairing.get(i).get(0));
            }
            else {
                allocateCaptain(crewDAO, flightPairing.get(i).get(0), flightPairing.get(i).get(1));
            }
        }

        // allocate matching First Officer to pairings
        for (int i=0; i<flightPairing.size(); i++) {
            if (flightPairing.get(i).get(1) == null) {
                allocateFirstOfficer(crewDAO, flightPairing.get(i).get(0), 1);
            }
            else {
                allocateFirstOfficer(crewDAO, flightPairing.get(i).get(0), flightPairing.get(i).get(1));
            }
        }

        // allocate matching Cabin Crew to pairings
        for (int i=0; i<flightPairing.size(); i++) {
            if (flightPairing.get(i).get(1) == null) {
                allocateCabinCrew(crewDAO, flightPairing.get(i).get(0));
            }
            else {
                allocateCabinCrew(crewDAO, flightPairing.get(i).get(0), flightPairing.get(i).get(1));
            }
        }

        // complete allocations for pairings/all flights
        for (FlightInfo flight : schedule.getRemainingAllocations()) {
            try {
                schedule.completeAllocationFor(flight);
            }
            catch (InvalidAllocationException e)
            {
                e.printStackTrace();
                System.err.print("invalid allocation");
            }
        }



        handleScheduleScore(aircraftDAO, crewDAO, passengerNumbersDAO, schedule);
        return schedule;
    }


    // TODO check rest times for crew: maybe use class airportsInUk verification, or schedule.getCompletedAllocationsFor(x).getFlight()


    /*a function to report best schedules and output improved scores to the console
     * maybe rename to compareScheduleScore*/
     private void handleScheduleScore(IAircraftDAO aircraftDAO, ICrewDAO crewDAO, IPassengerNumbersDAO passengerNumbersDAO, Schedule schedule) {
    	 long scheduleScore = 1000000000L;
         scheduleScore = new QualityScoreCalculator(aircraftDAO, crewDAO, passengerNumbersDAO, schedule).calculateQualityScore();

    	 if (scheduleScore < bestScheduleSoFarScore) {
             bestScheduleSoFarScore = scheduleScore;
             schedulerRunner.reportBestScheduleSoFar(schedule);
             System.out.println(bestScheduleSoFarScore + " (score best)"); // ongoing best score output
             String[] a = new QualityScoreCalculator(aircraftDAO, crewDAO, passengerNumbersDAO, schedule).describeQualityScore();
             for (String b : a) {
                System.out.println(b);
             }
         } 
     }



    /*allocate a single Aircraft to a single flight
     * advanced: check for previously landed flights at the arrival airport, analyse PassengerNumber data
     * */
    private void allocateAircraft(IAircraftDAO aircraftDAO, FlightInfo flight) {
        //* there is an allocation score advantage to allocating by AllAircraft then by AircraftByStartingPosition,
        // which doesn't make sense to me. Even when doing an already allocated condition check.
    	List<Aircraft> aircraftsAll = aircraftDAO.getAllAircraft();
//    	Collections.shuffle(aircraftsAll, new Random(1));
    	
    	findAvailableAircraft(aircraftDAO.findAircraftByStartingPosition(flight.getFlight().getDepartureAirportCode()), flight);
    	findAvailableAircraft(aircraftsAll, flight);

        // todo might it be possible to allocate aircraft by aircraft Type Code

        // todo allocate aircraft by also analysing passengerNumbers

        if (schedule.getAircraftFor(flight) == null) { // Tester to detect flights with unallocated Aircrafts
            System.err.println("Aircraft not allocated");
            System.out.println(flight+" flight");
        }
    }

    /*allocate a single Aircraft to a two flights
     * advanced: check for previously landed flights at the arrival airport, analyse PassengerNumber data
     * */
    private void allocateAircraftToMatchingFlights(IAircraftDAO aircraftDAO, FlightInfo legA, FlightInfo legB) {
        List<Aircraft> aircraftsAll = aircraftDAO.getAllAircraft();

        for (Aircraft a : aircraftsAll) {
            if (!schedule.hasConflict(a, legA) && !schedule.hasConflict(a, legB)) {
                try {
                    schedule.allocateAircraftTo(a, legA);
                } catch (DoubleBookedException e) {
                    System.err.println("error allocating aircraft to legA");
                    e.printStackTrace();
                }
                try {
                    schedule.allocateAircraftTo(a, legB);
                } catch (DoubleBookedException e) {
                    System.err.println("error allocating aircraft to legB");
                    e.printStackTrace();
                }
                break;
            }

        }

//        if (schedule.getAircraftFor(legA) == null) { // Tester to detect flights with unallocated Aircrafts
//            System.err.println("Aircraft not allocated");
//            System.out.println(legA+" flight");
//        }
//        if (schedule.getAircraftFor(legB) == null) { // Tester to detect flights with unallocated Aircrafts
//            System.err.println("Aircraft not allocated");
//            System.out.println(legB+" flight");
//        }
    }

    /* the goal of this function is to eliminate the repeat of code in the allocateAircraft() method
     * aka helper function*/
    private void findAvailableAircraft(List<Aircraft> aircrafts, FlightInfo flight) {
        for (Aircraft a : aircrafts) {
            if (!schedule.hasConflict(a, flight)) {
                try {
                    schedule.allocateAircraftTo(a, flight);
                    break;
                } catch (DoubleBookedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    int c = 0;
    /*allocate one Captain to a multiple Flights
    * firstly, attempt to allocate a Captain with matching Route Home Base and Aircraft Type Rating to
    * take advantage of using a helper function to check for an appropriate Captain */
    private void allocateCaptain(ICrewDAO crewDAO, FlightInfo legA, FlightInfo legB) {
        Pilot captain = null;
        Aircraft aircraftLegA = schedule.getAircraftFor(legA);
        Aircraft aircraftLegB = schedule.getAircraftFor(legB);
        Route routeLegA = legA.getFlight();
        Route routeLegB = legB.getFlight();
        Pilot.Rank rank = Pilot.Rank.CAPTAIN;
        Aircraft aircraft = schedule.getAircraftFor(legA);
        Route route = legA.getFlight();
        FlightInfo flight = legA;
        switch (0) {
            case 0:
                List<Pilot> pilotsByHomeBaseAndTypeRating = crewDAO.findPilotsByHomeBaseAndTypeRating(aircraft.getTypeCode(), route.getDepartureAirportCode());
                captain = findAvailablePilot(pilotsByHomeBaseAndTypeRating, legA, legB, rank);
                if (captain != null) break;
            case 1:
                List<Pilot> pilotsByTypeRating = crewDAO.findPilotsByTypeRating(aircraft.getTypeCode());
                captain = findAvailablePilot(pilotsByTypeRating, legA, legB, rank);
                if (captain != null) break;
            case 2:
                List<Pilot> pilotsByHomeBase = crewDAO.findPilotsByHomeBase(route.getDepartureAirportCode());
                captain = findAvailablePilot(pilotsByHomeBase, legA, legB, rank);
                if (captain != null) break;
            case 3:
                List<Pilot> pilotsAll = crewDAO.getAllPilots();
                captain = findAvailablePilot(pilotsAll, legA, legB, rank);
                if (captain != null) break;
            case 4: // equivalent to case 3, just removed RestedInUk conditions
                List<Pilot> pilotsAll2 = crewDAO.getAllPilots();
                
                for (Pilot p : pilotsAll2) {
//                    if (isRestedInUK(p, legA) && isRestedInUK(p, legB)) {
                        if (p.getRank() == rank && !schedule.hasConflict(p, legA)) {
                            if (!schedule.hasConflict(p, legB)) {
//                            	System.out.println(pilotsAll2.size() +" "+ captain);
                            	captain = p;
                            	break;
                            }
                        }
//                    }
                }
                if (captain != null) break;
        }
        if (captain == null) {
        	System.err.println("HUGEEEE ERRROR CAPTAIN IS NULL");
        }
        try {
            schedule.allocateCaptainTo(captain, legA);
        } catch (DoubleBookedException e) {
            System.err.println("Captain allocation error legA " +legA);
            e.printStackTrace();
        }
        try {
            schedule.allocateCaptainTo(captain, legB);
        } catch (DoubleBookedException e) {
            System.err.println("Captain allocation error legB " + legB);
            e.printStackTrace();
        }
        // add cabinCrew member to ArrivalsInUK map for UK arrivals future reference.
        if (Utilities.airportIsInUK(legA.getFlight().getArrivalAirportCode())) {
            if (pilotsArrivalsInUK.containsKey(captain)) { //captain key exists already
                List<LocalDateTime> l = pilotsArrivalsInUK.get(captain);
                l.add(legA.getLandingDateTime());
            }
            else {
                List<LocalDateTime> l = new ArrayList<>();
                l.add(legA.getLandingDateTime());
                pilotsArrivalsInUK.put(captain, l);
            }
        }
        else if (Utilities.airportIsInUK(legB.getFlight().getArrivalAirportCode())) {
        	
            if (pilotsArrivalsInUK.containsKey(captain)) { //captain key exists already
                List<LocalDateTime> l = pilotsArrivalsInUK.get(captain);
                l.add(legB.getLandingDateTime());
            }
            else { //! this breaks program.
                List<LocalDateTime> l = new ArrayList<>();
//                l.add(legB.getLandingDateTime());
                pilotsArrivalsInUK.put(captain, l);
            }
        }
                
        // debug tester
        if (captain == null) {
        	System.err.println("Captain allocated as Null");
        }
        
        // debug tester
        if (schedule.getCaptainOf(legA) == null) {
            System.out.println("Captain not allocated to flight " + legA);
        }
    }

    /*allocate one Captain to a single Flight
     * firstly, attempt to allocate a Captain with matching Route Home Base and Aircraft Type Rating to
     * take advantage of using a helper function to check for an appropriate Captain */
    private void allocateCaptain(ICrewDAO crewDAO, FlightInfo flight) {
        Pilot captain = null;
        Aircraft aircraft = schedule.getAircraftFor(flight);
        Route route = flight.getFlight();
        Pilot.Rank rank = Pilot.Rank.CAPTAIN;
        switch (0) {
            case 0:
                List<Pilot> pilotsByHomeBaseAndTypeRating = crewDAO.findPilotsByHomeBaseAndTypeRating(aircraft.getTypeCode(), route.getDepartureAirportCode());
                captain = findAvailablePilot(pilotsByHomeBaseAndTypeRating, flight, rank);
                if (captain != null) break;
            case 1:
                List<Pilot> pilotsByTypeRating = crewDAO.findPilotsByTypeRating(aircraft.getTypeCode());
                captain = findAvailablePilot(pilotsByTypeRating, flight, rank);
                if (captain != null) break;
            case 2:
                List<Pilot> pilotsByHomeBase = crewDAO.findPilotsByHomeBase(route.getDepartureAirportCode());
                captain = findAvailablePilot(pilotsByHomeBase, flight, rank);
                if (captain != null) break;
            case 3:
                List<Pilot> pilotsAll = crewDAO.getAllPilots();
                captain = findAvailablePilot(pilotsAll, flight, rank);
                if (captain != null) break;
            case 4: // duplicate of case 3, however no isNightlyRested in UK check
                List<Pilot> pilotsAll2 = crewDAO.getAllPilots();
                for (Pilot p : pilotsAll2) {
	                if (p.getRank() == rank && !schedule.hasConflict(p, flight)) {
	                    captain = p;
	                    break;
	                }
                }
                if (captain != null) break;
        }

        // debug tester
        if (captain == null) {
            System.err.println("Captain allocated as Null 2");
        }

        try {
            schedule.allocateCaptainTo(captain, flight);
        } catch (DoubleBookedException e) {
            System.err.println("Captain allocation error");
            e.printStackTrace();
        }
        // add cabinCrew member to ArrivalsInUK map for UK arrivals future reference.
        // this actually didn't improve score, it's use was too insignificant.
        if (Utilities.airportIsInUK(flight.getFlight().getArrivalAirportCode())) {
            if (pilotsArrivalsInUK.containsKey(captain)) { //cabinCrew key exists already
                List<LocalDateTime> l = pilotsArrivalsInUK.get(captain);
                l.add(flight.getLandingDateTime());
            }
            else {
                List<LocalDateTime> l = new ArrayList<>();
                l.add(flight.getLandingDateTime());
                pilotsArrivalsInUK.put(captain, l);
            }
        }

        // debug tester
        if (schedule.getCaptainOf(flight) == null) {
            System.out.println("Captain not allocated to flight " + flight);
        }
    }

    /*allocate a First Officer to a single Flight*/
    private void allocateFirstOfficer(ICrewDAO crewDAO, FlightInfo flight, int seed) {
        Pilot firstOfficer = null;
        Aircraft aircraft = schedule.getAircraftFor(flight);
        Route route = flight.getFlight();
        Pilot.Rank rank = Pilot.Rank.FIRST_OFFICER;
        switch (0) {
            case 0:
                List<Pilot> pilotsByHomeBaseAndTypeRating = crewDAO.findPilotsByHomeBaseAndTypeRating(aircraft.getTypeCode(), route.getDepartureAirportCode());
                Collections.shuffle(pilotsByHomeBaseAndTypeRating, new Random(seed));
                firstOfficer = findAvailablePilot(pilotsByHomeBaseAndTypeRating, flight, rank);
                if (firstOfficer != null) break;
            case 1:
                List<Pilot> pilotsByTypeRating = crewDAO.findPilotsByTypeRating(aircraft.getTypeCode());
                Collections.shuffle(pilotsByTypeRating, new Random(seed));
                firstOfficer = findAvailablePilot(pilotsByTypeRating, flight, rank);
                if (firstOfficer != null) break;
            case 2: // todo review this condition, it seemed to add ~ 132,380 to score.
                List<Pilot> pilotsByHomeBase = crewDAO.findPilotsByHomeBase(route.getDepartureAirportCode());
//                firstOfficer = findAvailablePilot(pilotsByHomeBase, flight, rank);
                if (firstOfficer != null) break;
            case 3:
                List<Pilot> pilotsAll = crewDAO.getAllPilots();
                Collections.shuffle(pilotsAll, new Random(seed));
                firstOfficer = findAvailablePilot(pilotsAll, flight, rank);
                if (firstOfficer != null) break;
        }
        try {
            schedule.allocateFirstOfficerTo(firstOfficer, flight);
        } catch (DoubleBookedException e) {
            System.err.println("First Officer allocation error");
        }
        // add cabinCrew member to ArrivalsInUK map for UK arrivals future reference.
        // this also seems insignificant.
        if (Utilities.airportIsInUK(flight.getFlight().getArrivalAirportCode())) {
            if (pilotsArrivalsInUK.containsKey(firstOfficer)) {
                List<LocalDateTime> l = pilotsArrivalsInUK.get(firstOfficer);
                l.add(flight.getLandingDateTime());
            }
            else {
                List<LocalDateTime> l = new ArrayList<>();
                l.add(flight.getLandingDateTime());
                pilotsArrivalsInUK.put(firstOfficer, l);
            }
        }

        // debug testers
        if (schedule.getFirstOfficerOf(flight) == null) {
            System.out.println("First Officer not allocated to flight " + flight);
        }
    }

    /*allocate a First Officer to a multiple Flights*/
    private void allocateFirstOfficer(ICrewDAO crewDAO, FlightInfo outboundFlight, FlightInfo returnFlight) {
        Pilot firstOfficer = null;
        Aircraft aircraft = schedule.getAircraftFor(outboundFlight);
        Route route = outboundFlight.getFlight();
        Pilot.Rank rank = Pilot.Rank.FIRST_OFFICER;
        switch (0) {
            case 0:
                List<Pilot> pilotsByHomeBaseAndTypeRating = crewDAO.findPilotsByHomeBaseAndTypeRating(aircraft.getTypeCode(), route.getDepartureAirportCode());
//                Collections.shuffle(pilotsByHomeBaseAndTypeRating);
                firstOfficer = findAvailablePilot(pilotsByHomeBaseAndTypeRating, outboundFlight, returnFlight, rank);
                if (firstOfficer != null) break;
            case 1:
                List<Pilot> pilotsByTypeRating = crewDAO.findPilotsByTypeRating(aircraft.getTypeCode());
//                Collections.shuffle(pilotsByTypeRating, new Random(seed));
                firstOfficer = findAvailablePilot(pilotsByTypeRating, outboundFlight, returnFlight, rank);
                if (firstOfficer != null) break;
            case 2: // todo review this condition, it seemed to add ~ 132,380 to score.
                List<Pilot> pilotsByHomeBase = crewDAO.findPilotsByHomeBase(route.getDepartureAirportCode());
//                firstOfficer = findAvailablePilot(pilotsByHomeBase, outboundFlight, returnFlight, rank);
                if (firstOfficer != null) break;
            case 3:
                List<Pilot> pilotsAll = crewDAO.getAllPilots();
//                Collections.shuffle(pilotsAll, new Random(seed));
                firstOfficer = findAvailablePilot(pilotsAll, outboundFlight, returnFlight, rank);

                if (firstOfficer != null) break; // slightly redundant, only necessary for cases that are not the final case
        }
        // allocate First Officer to outbound and return Flight.
        try {
            schedule.allocateFirstOfficerTo(firstOfficer, outboundFlight);
        } catch (DoubleBookedException e) {
            System.err.println("First Officer allocation error outboundFlight");
        }
        try {
            schedule.allocateFirstOfficerTo(firstOfficer, returnFlight);
        } catch (DoubleBookedException e) {
            System.err.println("First Officer allocation error returnFlight");
        }

        // add firstOfficer to ArrivalsInUK map for UK arrivals future reference.
        // adding these first officers to the pilotsArrivalsInUK map, reduced marker.exe score by 36,349,810
        if (Utilities.airportIsInUK(outboundFlight.getFlight().getArrivalAirportCode())) {
            if (pilotsArrivalsInUK.containsKey(firstOfficer)) { //cabinCrew key exists already
                List<LocalDateTime> l = pilotsArrivalsInUK.get(firstOfficer);
                l.add(outboundFlight.getLandingDateTime());
            }
            else {
                List<LocalDateTime> l = new ArrayList<>();
                l.add(outboundFlight.getLandingDateTime());
                pilotsArrivalsInUK.put(firstOfficer, l);
            }
        }
        if (Utilities.airportIsInUK(returnFlight.getFlight().getArrivalAirportCode())) {
            if (pilotsArrivalsInUK.containsKey(firstOfficer)) { //cabinCrew key exists already
                List<LocalDateTime> l = pilotsArrivalsInUK.get(firstOfficer);
                l.add(returnFlight.getLandingDateTime());
            }
            else {
                List<LocalDateTime> l = new ArrayList<>();
                l.add(returnFlight.getLandingDateTime());
                pilotsArrivalsInUK.put(firstOfficer, l);
            }
        }

        // debug testers
        if (schedule.getFirstOfficerOf(outboundFlight) == null) {
            System.out.println("First Officer not allocated to outbound flight " + outboundFlight);
        }
        if (schedule.getFirstOfficerOf(returnFlight) == null) {
            System.out.println("First Officer not allocated to return flight " + returnFlight);
        }
    }

    private Pilot findAvailablePilot(List<Pilot> pilots, FlightInfo flight, Pilot.Rank rank ) {
        for (Pilot p : pilots) {
            if (isRestedInUK(p, flight)) {
                if (p.getRank() == rank && !schedule.hasConflict(p, flight)) {
                    return p;
                }
            }
        }
        return null;
    }

    private Pilot findAvailablePilot(List<Pilot> pilots, FlightInfo outboundFlight, FlightInfo returnFlight, Pilot.Rank rank ) {
        // todo add feat to check pilot is Nightly Rested.
        for (Pilot p : pilots) {
            if (isRestedInUK(p, outboundFlight) && isRestedInUK(p, returnFlight)) {
                if (p.getRank() == rank && !schedule.hasConflict(p, outboundFlight)) {
                    if (!schedule.hasConflict(p, returnFlight)) {
                    	return p;
                    }
                }
            }
        }
        return null;
    }
    
    /*allocate many Cabin Crew to a flight aircraft
    * firstly, attempt to allocate matching Cabin Crew by matching Home Base and Type Rating
    * if not, attempt to allocate by matching type Rating only
    * if not, attempt to allocate by Home Base only,
    * if not, finally, allocate any Cabin Crew that do not have conflict with the Flight*/
    private void allocateCabinCrew(ICrewDAO crewDAO, FlightInfo flight) {
        Aircraft aircraft = schedule.getAircraftFor(flight);
        Route route = flight.getFlight();
        for (int i=0; i<schedule.getAircraftFor(flight).getCabinCrewRequired(); i++) {
            boolean allocated = false;
            switch (0) {
                case 0:
                    List<CabinCrew> cabinCrewByHomeBaseAndTypeRating = crewDAO.findCabinCrewByHomeBaseAndTypeRating(aircraft.getTypeCode(), route.getDepartureAirportCode());
                    allocated = findAvailableCabinCrew(cabinCrewByHomeBaseAndTypeRating, flight);
                    if (allocated) break;
                case 1:
                    List<CabinCrew> cabinCrewByTypeRating = crewDAO.findCabinCrewByTypeRating(aircraft.getTypeCode());
                    allocated = findAvailableCabinCrew(cabinCrewByTypeRating, flight);
                    if (allocated) break;
                case 2: // todo review this condition, it seemed to add ~ 132,380 to score.
                    List<CabinCrew> cabinCrewByHomeBase = crewDAO.findCabinCrewByHomeBase(route.getDepartureAirportCode());
                    allocated = findAvailableCabinCrew(cabinCrewByHomeBase, flight);
                    if (allocated) break;
                case 3:
                    List<CabinCrew> cabinCrewAll = crewDAO.getAllCabinCrew();
                    allocated = findAvailableCabinCrew(cabinCrewAll, flight);
                    if (allocated) break;
            }
        }
    }

    /* single Flight*/
    private Boolean findAvailableCabinCrew(List<CabinCrew> crew, FlightInfo flight) {
//      Collections.shuffle(crew, new Random(1));
        for (CabinCrew c : crew) {
            if (!schedule.hasConflict(c, flight) && !schedule.getCabinCrewOf(flight).contains(c)) {
                try {
                    schedule.allocateCabinCrewTo(c, flight);
                } catch (DoubleBookedException e) {
                    e.printStackTrace();
                }
                // add cabinCrew member to ArrivalsInUK map for UK arrivals future reference.
                // this improved marker.exe score by only 333,500
                if (Utilities.airportIsInUK(flight.getFlight().getArrivalAirportCode())) {
                    if (cabinCrewArrivalsInUK.containsKey(c)) { //cabinCrew key exists already
                        List<LocalDateTime> l = cabinCrewArrivalsInUK.get(c);
                        l.add(flight.getLandingDateTime());
                    }
                    else {
                        List<LocalDateTime> l = new ArrayList<>();
                        l.add(flight.getLandingDateTime());
                        cabinCrewArrivalsInUK.put(c, l);
                    }
                }
                return true;
            }
        }
        return false;
    }

    /*allocate many Cabin Crew to 2 flights
     * */
    private void allocateCabinCrew(ICrewDAO crewDAO, FlightInfo outboundFlight, FlightInfo returnFlight) {
        Aircraft aircraft = schedule.getAircraftFor(outboundFlight);
        Route route = outboundFlight.getFlight();
        for (int i=0; i<schedule.getAircraftFor(outboundFlight).getCabinCrewRequired(); i++) {
            CabinCrew cabinCrew = null;
            switch (0) {
                case 0:
                    List<CabinCrew> cabinCrewByHomeBaseAndTypeRating = crewDAO.findCabinCrewByHomeBaseAndTypeRating(aircraft.getTypeCode(), route.getDepartureAirportCode());
                    cabinCrew = findAvailableCabinCrew(cabinCrewByHomeBaseAndTypeRating, outboundFlight, returnFlight);
                    if (cabinCrew != null) break;
                case 1:
                    List<CabinCrew> cabinCrewByTypeRating = crewDAO.findCabinCrewByTypeRating(aircraft.getTypeCode());
                    cabinCrew = findAvailableCabinCrew(cabinCrewByTypeRating, outboundFlight, returnFlight);
                    if (cabinCrew != null) break;
                case 2:
                    List<CabinCrew> cabinCrewByHomeBase = crewDAO.findCabinCrewByHomeBase(route.getDepartureAirportCode());
                    cabinCrew = findAvailableCabinCrew(cabinCrewByHomeBase, outboundFlight, returnFlight);
                    if (cabinCrew != null) break;
                case 3:
                    List<CabinCrew> cabinCrewAll = crewDAO.getAllCabinCrew();
                    cabinCrew = findAvailableCabinCrew(cabinCrewAll, outboundFlight, returnFlight);
                    if (cabinCrew != null) break;
            }
            try {
                schedule.allocateCabinCrewTo(cabinCrew, outboundFlight);
            } catch (DoubleBookedException e) {
                System.err.println("Cabin Crew allocation error outboundFlight");
                e.printStackTrace();
            }
            try {
                schedule.allocateCabinCrewTo(cabinCrew, returnFlight);
            } catch (DoubleBookedException e) {
                System.err.println("Cabin Crew allocation error returnFlight");
                e.printStackTrace();
            }
            // add cabinCrew member to ArrivalsInUK map for UK arrivals future reference.
            if (Utilities.airportIsInUK(outboundFlight.getFlight().getArrivalAirportCode())) {
                if (cabinCrewArrivalsInUK.containsKey(cabinCrew)) { //cabinCrew key exists already
                    List<LocalDateTime> l = cabinCrewArrivalsInUK.get(cabinCrew);
                    l.add(outboundFlight.getLandingDateTime());
                }
                else {
                    List<LocalDateTime> l = new ArrayList<>();
                    l.add(outboundFlight.getLandingDateTime());
                    cabinCrewArrivalsInUK.put(cabinCrew, l);
                }
            }
            if (Utilities.airportIsInUK(returnFlight.getFlight().getArrivalAirportCode())) {
                if (cabinCrewArrivalsInUK.containsKey(cabinCrew)) { //cabinCrew key exists already
                    List<LocalDateTime> l = cabinCrewArrivalsInUK.get(cabinCrew);
                    l.add(returnFlight.getLandingDateTime());
                }
                else {
                    List<LocalDateTime> l = new ArrayList<>();
                    l.add(returnFlight.getLandingDateTime());
                    cabinCrewArrivalsInUK.put(cabinCrew, l);
                }
            }
        }
    }


    int export = 0;
    private CabinCrew findAvailableCabinCrew(List<CabinCrew> crew, FlightInfo outboundFlight, FlightInfo returnFlight) {
        Collections.shuffle(crew, new Random(2));

        // If ONLY the outbound flights Departure Airports are in the UK, check for Nightly rest condition
        if (Utilities.airportIsInUK(outboundFlight.getFlight().getDepartureAirportCode())) {
            for (CabinCrew c : crew) {
                if (isRestedInUK(c, outboundFlight)) {
                    if (!schedule.hasConflict(c, outboundFlight) && !schedule.getCabinCrewOf(outboundFlight).contains(c)) {
                        if (!schedule.hasConflict(c, returnFlight) && !schedule.getCabinCrewOf(returnFlight).contains(c)) {
                            return c;
                        }
                    }
                }
            }
        }
      //  checking the returnFlight departs from UK seemed to result in a WORSE score by 86,940
        /*if (Utilities.airportIsInUK(returnFlight.getFlight().getDepartureAirportCode())) {
            for (CabinCrew c : crew) {
                if (isRestedInUK(c, returnFlight)) {
                    if (!schedule.hasConflict(c, outboundFlight) && !schedule.getCabinCrewOf(outboundFlight).contains(c)) {
                        if (!schedule.hasConflict(c, returnFlight) && !schedule.getCabinCrewOf(returnFlight).contains(c)) {
                            return c;
                        }
                    }
                }
            }
        }*/
        for (CabinCrew c : crew) {
            if (!schedule.hasConflict(c, outboundFlight) && !schedule.getCabinCrewOf(outboundFlight).contains(c)) {
                if (!schedule.hasConflict(c, returnFlight) && !schedule.getCabinCrewOf(returnFlight).contains(c)) {
                    return c;
                }
            }
        }
        return null;
    }

    /* Searches all existing Flight allocations for any that the cabin crew member in question has flown on
    crew member is allocated to a flight that departs the UK within 12 hours of their
    last landing in the UK
    * */
    private boolean isRestedInUK(CabinCrew c, FlightInfo flight) {
        // find crew that have not arrived at UK airport less than 12 hours before NEXT desired departure
        // . search through ALL flights
        // . find flights that contain given Crew Member
        // . does flight Arrive in UK
        // . is flight arrival time less than 12 hours
        // differentiate between outbound and return flights todo ! this may have changed now.
        // verify with ONLY outbound flight for now todo
        if (!cabinCrewArrivalsInUK.containsKey(c)) return true; // no record of cabin crew member flying

        List<LocalDateTime> list = cabinCrewArrivalsInUK.get(c);
        for (LocalDateTime previousDateTime : list) {
            // is flight arrival to departure time less than 12 hours
            /*System.out.println();
            System.out.println(previousDateTime);
            System.out.println(flight.getDepartureDateTime());
            System.out.println(previousDateTime.until(flight.getDepartureDateTime(), ChronoUnit.HOURS) < 12L);*/
            if (previousDateTime.until(flight.getDepartureDateTime(), ChronoUnit.HOURS) < 12L) {
                return false;
            }
        }
        return true;
    }

    /* the pilot variant of isRestedInUK()*/
    private boolean isRestedInUK(Pilot c, FlightInfo flight) {
        if (!pilotsArrivalsInUK.containsKey(c)) return true; // no record of cabin crew member flying
        if (!Utilities.airportIsInUK(flight.getFlight().getDepartureAirportCode())) return true; // todo add this feature to the Cabin Crew variant

        List<LocalDateTime> list = pilotsArrivalsInUK.get(c);
        for (LocalDateTime previousDateTime : list) {
            if (previousDateTime.until(flight.getDepartureDateTime(), ChronoUnit.HOURS) < 12L) {
                return false;
            }
        }
        return true;
    }

    /*
     * The ScheduleRunner will pass a reference to itself via this method, so that if you're running for the full 2 mins
     * you can call the reportBestScheduleSoFar() method to keep the runner informed of your progress
     */
    @Override
    public void setSchedulerRunner(SchedulerRunner arg0) {
        this.schedulerRunner = arg0;
    }

    /*
     * Used by the SchedulerRunner to ask you to stop your generateSchedule method when 2 minutes have elapsed
     * I would recommend having this set a boolean variable that is checked frequently by long-running generateSchedule() methods
     * If this method has been called, time is up and it doesn't matter what generateSchedule() returns, it can just return null.
     */
    @Override
    public void stop() {
//    	System.out.println("stop condition triggered and should set stop to true [Scheduler.class]");
        this.stop = true;
    }


}
