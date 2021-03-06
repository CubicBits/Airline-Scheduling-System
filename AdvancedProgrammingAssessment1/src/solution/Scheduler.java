package solution;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import baseclasses.*;

/**
 * Jack Owen
 * Manchester Metropolitan University id: 17032929
 */

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
    	this.schedule = new Schedule(routeDAO, startDate, endDate);
    	/* Maps to record Crew UK arrival times, instead of having to loop through all Flights every time */
        cabinCrewArrivalsInUK = new HashMap<>();
        pilotsArrivalsInUK = new HashMap<>();

        List<FlightInfo> flights = schedule.getRemainingAllocations();
        ArrayList<ArrayList<FlightInfo>> flightPairing = new ArrayList<>(); // two-dimensional array list legA:legB
        
        /* Generate Flight Pairings */
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
                        && returnFlight.getFlight().getArrivalAirportCode().equals(outboundDepartureAirport)) {
                    ArrayList<FlightInfo> pair = new ArrayList<>();
                    pair.add(outboundFlight);
                    pair.add(returnFlight);
                    flightPairing.add(pair);
                    flights.remove(outboundFlight);
                    flights.remove(returnFlight);
                    break;
                }
            }
            if (flights.contains(outboundFlight)) { // if outboundFlight still has no flight pairing, i.e because no returnFlight was found
                ArrayList<FlightInfo> pair = new ArrayList<>();
                pair.add(outboundFlight); // no return flight found YET
                pair.add(null);
                flightPairing.add(pair);
                flights.remove(outboundFlight);
            }
        }

        // Allocate Flight values
        for (int i=0; i<flightPairing.size(); i++) {
            if (flightPairing.get(i).get(1) == null) { // when a Flight doesn't have a return Flight
                FlightInfo flight = flightPairing.get(i).get(0);
                allocateAircraft(aircraftDAO, passengerNumbersDAO, flight);
                allocateCaptain(crewDAO, flight);
                allocateFirstOfficer(crewDAO, flight);
                allocateCabinCrew(crewDAO, flight);
            }
            else {
                outboundFlight = flightPairing.get(i).get(0);
                FlightInfo returnFlight = flightPairing.get(i).get(1);
                allocateAircraft(aircraftDAO, passengerNumbersDAO, outboundFlight, returnFlight);
                allocateCaptain(crewDAO, outboundFlight, returnFlight);
                allocateFirstOfficer(crewDAO, outboundFlight, returnFlight);
                allocateCabinCrew(crewDAO, outboundFlight, returnFlight);
            }
        }

        /* complete all Flight allocations */
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

    /* Reports improved schedules and outputs improved score values to the console */
     private void handleScheduleScore(IAircraftDAO aircraftDAO, ICrewDAO crewDAO, IPassengerNumbersDAO passengerNumbersDAO, Schedule schedule) {
    	 long scheduleScore;
    	 boolean description = false;
         scheduleScore = new QualityScoreCalculator(aircraftDAO, crewDAO, passengerNumbersDAO, schedule).calculateQualityScore();
    	 if (scheduleScore < bestScheduleSoFarScore) {
             bestScheduleSoFarScore = scheduleScore;
             schedulerRunner.reportBestScheduleSoFar(schedule);
             System.out.println(bestScheduleSoFarScore + " (score best)"); // ongoing best score output
             
             if (description) {
            	 String[] a = new QualityScoreCalculator(aircraftDAO, crewDAO, passengerNumbersDAO, schedule).describeQualityScore();
                 for (String b : a) {
                    System.out.println(b);
                 }
             }
         } 
     }

    /*allocate a single Aircraft to a TWO flights
     * advanced: check for previously landed flights at the arrival airport, analyse PassengerNumber data
     * */
    private void allocateAircraft(IAircraftDAO aircraftDAO, IPassengerNumbersDAO passengerNumbersDAO, FlightInfo outboundFlight, FlightInfo returnFlight) {
        ArrayList<List<Aircraft>> history = new ArrayList<>();
        history.add(new ArrayList<>()); // hasConflict
        List<Aircraft> aircrafts = aircraftDAO.getAllAircraft();
        for (Aircraft a : aircrafts) {
            if (!schedule.hasConflict(a, outboundFlight) && !schedule.hasConflict(a, returnFlight)) {
                history.get(0).add(a);
            }
        }
        
        /* pick best option of all possibilities*/
        Aircraft a = null;
        for (int i=history.size()-1; i>=0; i--) {
            if (!history.get(i).isEmpty()) {
                a = history.get(i).get(history.get(i).size() / 2);
                break; // pick first result
            }
        }
        try {
        	schedule.allocateAircraftTo(a, outboundFlight);
        } catch (DoubleBookedException e) {
            System.err.println("error allocating aircraft to legA");
            e.printStackTrace();
        }
        try {
        	schedule.allocateAircraftTo(a, returnFlight);
        } catch (DoubleBookedException e) {
            System.err.println("error allocating aircraft to legB");
            e.printStackTrace();
        }
    }

    /* allocate a single Aircraft to ONE flight */
    private void allocateAircraft(IAircraftDAO aircraftDAO, IPassengerNumbersDAO passengerNumbersDAO, FlightInfo flight) {
         /*a simple hack to trick the outbound and return flight Aircraft allocation method.
         not a perfect solution, but saves many lines of code.*/
        allocateAircraft(aircraftDAO, passengerNumbersDAO, flight, flight);
    }

    /*allocate one Captain to TWO Flights
    * firstly, attempt to allocate a Captain with matching Route Home Base and Aircraft Type Rating to
    * take advantage of using a helper function to check for an appropriate Captain */
    private void allocateCaptain(ICrewDAO crewDAO, FlightInfo outboundFlight, FlightInfo returnFlight) {
        Pilot captain = findAvailablePilot(crewDAO.getAllPilots(), outboundFlight, returnFlight, Pilot.Rank.CAPTAIN);
        
        // allocate Captain to both Flights
        try {
            schedule.allocateCaptainTo(captain, outboundFlight);
        } catch (DoubleBookedException e) {
        	System.out.println(captain + outboundFlight.toString());
            System.err.println("Captain allocation error legA " +outboundFlight);
            e.printStackTrace();
        }
        try {
            schedule.allocateCaptainTo(captain, returnFlight);
        } catch (DoubleBookedException e) {
            System.err.println("Captain allocation error legB " + returnFlight);
            e.printStackTrace();
        }
        // add Captain Flight arrival times to Map
        if (Utilities.airportIsInUK(outboundFlight.getFlight().getArrivalAirportCode())) {
            if (pilotsArrivalsInUK.containsKey(captain)) { //captain key exists in Map already
                List<LocalDateTime> l = pilotsArrivalsInUK.get(captain);
                l.add(outboundFlight.getLandingDateTime());
            }
            else {
                List<LocalDateTime> l = new ArrayList<>();
                l.add(outboundFlight.getLandingDateTime());
                pilotsArrivalsInUK.put(captain, l);
            }
        }
        else if (Utilities.airportIsInUK(returnFlight.getFlight().getArrivalAirportCode())) {
            if (pilotsArrivalsInUK.containsKey(captain)) { //captain key exists already
                List<LocalDateTime> l = pilotsArrivalsInUK.get(captain);
                l.add(returnFlight.getLandingDateTime());
            }
            else { //! this breaks program.
                List<LocalDateTime> l = new ArrayList<>();
                l.add(returnFlight.getLandingDateTime());
                pilotsArrivalsInUK.put(captain, l);
            }
        }
    }

    /* allocate one Captain to a single Flight
     * firstly, attempt to allocate a Captain with matching Route Home Base and Aircraft Type Rating to
     * take advantage of using a helper function to check for an appropriate Captain */
    private void allocateCaptain(ICrewDAO crewDAO, FlightInfo flight) {
    	Pilot captain = findAvailablePilot(crewDAO.getAllPilots(), flight, Pilot.Rank.CAPTAIN);
        try {
            schedule.allocateCaptainTo(captain, flight);
        } catch (DoubleBookedException e) {
            System.err.println("Captain allocation error");
            e.printStackTrace();
        }
        // add cabinCrew member to ArrivalsInUK map for UK arrivals future reference.
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
    }



    /*allocate a First Officer to a multiple Flights*/
    private void allocateFirstOfficer(ICrewDAO crewDAO, FlightInfo outboundFlight, FlightInfo returnFlight) {
        Pilot firstOfficer = findAvailablePilot(crewDAO.getAllPilots(), outboundFlight, returnFlight, Pilot.Rank.FIRST_OFFICER);
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
    }

    /*allocate a First Officer to a single Flight*/
    private void allocateFirstOfficer(ICrewDAO crewDAO, FlightInfo flight) {
        Pilot firstOfficer = findAvailablePilot(crewDAO.getAllPilots(), flight, Pilot.Rank.FIRST_OFFICER);
        try {
            schedule.allocateFirstOfficerTo(firstOfficer, flight);
        } catch (DoubleBookedException e) {
            System.err.println("First Officer allocation error");
        }
        // add cabinCrew member to ArrivalsInUK map for UK arrivals future reference.
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
    }

    /* returns a Pilot for a matching Flight Pairing
    called twice by allocateFirstOfficer and allocateCaptain */
    private Pilot findAvailablePilot(List<Pilot> pilots, FlightInfo outboundFlight, FlightInfo returnFlight, Pilot.Rank rank ) {
        ArrayList<List<Pilot>> history = new ArrayList<>();
        int index = 0;
        history.add(new ArrayList<>()); // hasConflict & Rank
        for (Pilot p : pilots) {
            if (p.getRank() == rank) {
                if (!schedule.hasConflict(p, outboundFlight) && !schedule.hasConflict(p, returnFlight)) {
                    history.get(index).add(p);
                }
            }
        }
        index++;
        history.add(new ArrayList<>()); // isRestedInUK
        for (Pilot p : history.get(index-1)) {
            if (isRestedInUK(p, outboundFlight) && isRestedInUK(p, returnFlight)) {
                history.get(index).add(p);
            }
        }
        index++;
        history.add(new ArrayList<>()); // qualified
        for (Pilot p : history.get(index-1)) {
            if (p.isQualifiedFor(schedule.getAircraftFor(outboundFlight))) {
                history.get(index).add(p);
            }
        }
        index++;
        history.add(new ArrayList<>()); // homebase
        for (Pilot p : history.get(index-1)) {
            if (p.getHomeBase().equals(outboundFlight.getFlight().getDepartureAirportCode())) {
                history.get(index).add(p);
            }
        }
        /* pick best option of all possibilities*/
        for (int i=history.size()-1; i>=0; i--) {
            if (!history.get(i).isEmpty()) {
            	return history.get(i).get(history.get(i).size() / 2);
            }
        }
        return null;
    }

    /*  returns a Pilot for a matching Flight Pairing
    called twice by allocateFirstOfficer and allocateCaptain */
    private Pilot findAvailablePilot(List<Pilot> pilots, FlightInfo flight, Pilot.Rank rank) {
        ArrayList<List<Pilot>> history = new ArrayList<>();

        int index = 0;
        history.add(new ArrayList<>());
        for (Pilot p : pilots) {
            if (p.getRank() == rank) {
                if (!schedule.hasConflict(p, flight)) {
                    
                    history.get(index).add(p);
                }
            }
        }
        index++;
        history.add(new ArrayList<>());
        for (Pilot p : history.get(index-1)) {
            if (isRestedInUK(p, flight)) {
            	
                history.get(index).add(p);
            }
        }
        index++;
        history.add(new ArrayList<>());
        for (Pilot p : history.get(index-1)) {
            if (p.isQualifiedFor(schedule.getAircraftFor(flight))) {
            	
                history.get(index).add(p);
            }
        }
        index++;
        history.add(new ArrayList<>());
        for (Pilot p : history.get(index-1)) {
            if (p.getHomeBase().equals(flight.getFlight().getDepartureAirportCode())) {
                history.get(index).add(p);
            }
        }
        
        /* pick best option of all possibilities*/
        for (int i=history.size()-1; i>=0; i--) {
            if (!history.get(i).isEmpty()) {
                return history.get(i).get(history.get(i).size()-1); // last option in list
            }
        }
        return null;
    }

    /* allocate many Cabin Crew to 2 flights */
    private void allocateCabinCrew(ICrewDAO crewDAO, FlightInfo outboundFlight, FlightInfo returnFlight) {
        for (int i=0; i<schedule.getAircraftFor(outboundFlight).getCabinCrewRequired(); i++) {
            ArrayList<List<CabinCrew>> history = new ArrayList<>(); // two-dimensional history of crew lists
            history.add(new ArrayList<>());
            history.add(new ArrayList<>());
            history.add(new ArrayList<>());
            history.add(new ArrayList<>());
            int index = 0;
            for (CabinCrew member : crewDAO.getAllCabinCrew()) {
                if (!schedule.hasConflict(member, outboundFlight) && !schedule.getCabinCrewOf(outboundFlight).contains(member)) {
                    if (!schedule.hasConflict(member, returnFlight) && !schedule.getCabinCrewOf(returnFlight).contains(member)) {
                        history.get(index).add(member);
                    }
                }
            }
            index++;
            for (CabinCrew member : history.get(index-1)) {
                if (isRestedInUK(member, outboundFlight) && isRestedInUK(member, returnFlight)) {
                    history.get(index).add(member);
                }
            }
            index++;
            for (CabinCrew member : history.get(index-1)) {
                if (member.isQualifiedFor(schedule.getAircraftFor(outboundFlight))) {
                    history.get(index).add(member);
                }
            }
            index++;
            for (CabinCrew member : history.get(index-1)) {
                if (member.getHomeBase().equals(outboundFlight.getFlight().getDepartureAirportCode())) {
                    history.get(index).add(member);
                }
            }
            /* pick best option of all possibilities*/
            CabinCrew cabinCrew = null;
            for (int j=history.size()-1; j>=0; j--) {
                if (!history.get(j).isEmpty()) {
                    cabinCrew = history.get(j).get(new Random().nextInt(history.get(j).size())); // random object in list
                    break;
                }
            }

            /*allocate to flight*/
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
            /*add cabinCrew member to ArrivalsInUK map for UK arrivals future reference*/
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

    /*allocate many Cabin Crew to a flight aircraft
     * firstly, attempt to allocate matching Cabin Crew by matching Home Base and Type Rating
     * if not, attempt to allocate by matching type Rating only
     * if not, attempt to allocate by Home Base only,
     * if not, finally, allocate any Cabin Crew that do not have conflict with the Flight*/
    private void allocateCabinCrew(ICrewDAO crewDAO, FlightInfo flight) {
        for (int i=0; i<schedule.getAircraftFor(flight).getCabinCrewRequired(); i++) {
            ArrayList<List<CabinCrew>> history = new ArrayList<>(); // two-dimensional history of crew lists
            history.add(new ArrayList<>()); //index 0 // hasConflict
            history.add(new ArrayList<>()); //index 1 // isRestedInUK
            history.add(new ArrayList<>()); //index 2 // qualified to only one Aircraft Type
            history.add(new ArrayList<>()); //index 3
            int index = 0;
            CabinCrew c = null;
            for (CabinCrew member : crewDAO.getAllCabinCrew()) {
                if (!schedule.hasConflict(member, flight) && !schedule.getCabinCrewOf(flight).contains(member)) {
                    history.get(index).add(member);
                }
            }
            index++;
            for (CabinCrew member : history.get(index-1)) {
                if (member.isQualifiedFor(schedule.getAircraftFor(flight))) {
                    history.get(index).add(member);
                }
            }
            index++;
            for (CabinCrew member : history.get(index-1)) {
                if (isRestedInUK(member, flight)) {
                    history.get(index).add(member);
                }
            }
            index++;
            for (CabinCrew member : history.get(index-1)) {
                if (member.getHomeBase().equals(flight.getFlight().getDepartureAirportCode())) {
                    history.get(index).add(member);
                }
            }

            /* pick best option of all possibilities*/
            for (int j=history.size()-1; j>=0; j--) {
                if (!history.get(j).isEmpty()) {
                    c = history.get(j).get(history.get(j).size() / 2); // this takes the first item from the list
                    break;
                }
            }

            /* allocate to flight */
            try {
                schedule.allocateCabinCrewTo(c, flight);
            } catch (DoubleBookedException e) {
                e.printStackTrace();
            }
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
        }
    }

    /* Searches all existing Flight allocations for any that the cabin crew member in question has flown on
    crew member is allocated to a flight that departs the UK within 12 hours of their last landing in the UK */
    private boolean isRestedInUK(CabinCrew c, FlightInfo flight) {
        // . is flight arrival time less than 12 hours
        // differentiate between outbound and return flights todo ! this may have changed now.
        // verify with ONLY outbound flight for now todo
        if (!cabinCrewArrivalsInUK.containsKey(c)) return true; // no record of cabin crew member flying

        List<LocalDateTime> list = cabinCrewArrivalsInUK.get(c);
        for (LocalDateTime previousDateTime : list) {
            // is flight arrival to departure time less than 12 hours
            if (previousDateTime.until(flight.getDepartureDateTime(), ChronoUnit.HOURS) < 12L) {
                return false;
            }
        }
        return true;
    }

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

    @Override
    public void setSchedulerRunner(SchedulerRunner arg0) {
        this.schedulerRunner = arg0;
    }

    @Override
    public void stop() { this.stop = true; }
}
