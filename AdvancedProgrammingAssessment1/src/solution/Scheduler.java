package solution;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import baseclasses.*;


public class Scheduler implements IScheduler {
    Schedule schedule;
    SchedulerRunner schedulerRunner;
    boolean stop = false;

    @Override
    public Schedule generateSchedule(IAircraftDAO aircraftDAO, ICrewDAO crewDAO, IRouteDAO routeDAO, IPassengerNumbersDAO passengerNumbersDAO,
                                     LocalDate startDate, LocalDate endDate) {
        this.schedule = new Schedule(routeDAO, startDate, endDate);

        while (!schedule.isCompleted()) {
            FlightInfo flight = schedule.getRemainingAllocations().get(0);
            allocateAircraft(aircraftDAO, flight);
            allocateCaptain(crewDAO, flight);
            allocateFirstOfficer(crewDAO, flight);
            allocateCabinCrew(crewDAO, flight);
            try {
                schedule.completeAllocationFor(flight);
            } catch (InvalidAllocationException e) {
                e.printStackTrace();
                System.err.print("invalid allocation");
            }
        }

        return schedule;
    }

    /*allocate a single Aircraft to a single flight
     * advanced: check for previously landed flights at the arrival airport, analyse PassengerNumber data
     * */
    private void allocateAircraft(IAircraftDAO aircraftDAO, FlightInfo flight) {
        // simple aircraft allocations, any that have no conflict
        for (Aircraft a : aircraftDAO.getAllAircraft()) {
            if (!schedule.hasConflict(a, flight)) {
                try {
                    schedule.allocateAircraftTo(a, flight);
                    break;
                } catch (DoubleBookedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (schedule.getAircraftFor(flight) == null) { // Tester to detect flights with unallocated Aircrafts
            System.err.println("Aircraft not allocated");
        }
    }

    /*allocate a Captain to a single Flight
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
            case 2: // todo review this condition, it seemed to add ~ 120,000 to score.
                List<Pilot> pilotsByHomeBase = crewDAO.findPilotsByHomeBase(route.getDepartureAirportCode());
//                captain = findAvailablePilot(pilotsByHomeBase, flight, rank);
                if (captain != null) break;
            case 3:
                List<Pilot> pilotsAll = crewDAO.getAllPilots();
                captain = findAvailablePilot(pilotsAll, flight, rank);
                if (captain != null) break;
        }


        try {
            schedule.allocateCaptainTo(captain, flight);
        } catch (DoubleBookedException e) {
            System.err.println("Captain allocation error");
        }
        // debug testers
        if (schedule.getCaptainOf(flight) == null) {
            System.out.println("Captain not allocated to flight " + flight);
        }
    }

    /*allocate a First Officer to a single Flight*/
    private void allocateFirstOfficer(ICrewDAO crewDAO, FlightInfo flight) {
        Pilot firstOfficer = null;
        Aircraft aircraft = schedule.getAircraftFor(flight);
        Route route = flight.getFlight();
        Pilot.Rank rank = Pilot.Rank.FIRST_OFFICER;

        switch (0) {
            case 0:
                List<Pilot> pilotsByHomeBaseAndTypeRating = crewDAO.findPilotsByHomeBaseAndTypeRating(aircraft.getTypeCode(), route.getDepartureAirportCode());
                firstOfficer = findAvailablePilot(pilotsByHomeBaseAndTypeRating, flight, rank);
                if (firstOfficer != null) break;
            case 1:
                List<Pilot> pilotsByTypeRating = crewDAO.findPilotsByTypeRating(aircraft.getTypeCode());
                firstOfficer = findAvailablePilot(pilotsByTypeRating, flight, rank);
                if (firstOfficer != null) break;
            case 2: // todo review this condition, it seemed to add ~ 132,380 to score.
                List<Pilot> pilotsByHomeBase = crewDAO.findPilotsByHomeBase(route.getDepartureAirportCode());
//                firstOfficer = findAvailablePilot(pilotsByHomeBase, flight, rank);
                if (firstOfficer != null) break;
            case 3:
                List<Pilot> pilotsAll = crewDAO.getAllPilots();
                firstOfficer = findAvailablePilot(pilotsAll, flight, rank);
                if (firstOfficer != null) break;
        }

        try {
            schedule.allocateFirstOfficerTo(firstOfficer, flight);
        } catch (DoubleBookedException e) {
            System.err.println("First Officer allocation error");
        }
        // debug testers
        if (schedule.getFirstOfficerOf(flight) == null) {
            System.out.println("First Officer not allocated to flight " + flight);
        }
    }

    /* the goal of this function is to eliminate the repeat of code in the pilot allocation functions
     * aka helper function*/
    private Pilot findAvailablePilot(List<Pilot> pilots, FlightInfo flight, Pilot.Rank rank ) {
        for (Pilot p : pilots) {
            if (p.getRank() == rank && !schedule.hasConflict(p, flight)) {
                return p;
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
            for (CabinCrew crew : crewDAO.findCabinCrewByHomeBaseAndTypeRating(aircraft.getTypeCode(), route.getDepartureAirportCode())) {
                if (!schedule.hasConflict(crew, flight) && !schedule.getCabinCrewOf(flight).contains(crew)) {
                    try {
                        schedule.allocateCabinCrewTo(crew, flight);
                        allocated = true;
                        break;
                    } catch (DoubleBookedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!allocated) {
                for (CabinCrew crew : crewDAO.findCabinCrewByTypeRating(aircraft.getTypeCode())) {
                    if (!schedule.hasConflict(crew, flight) && !schedule.getCabinCrewOf(flight).contains(crew)) {
                        try {
                            schedule.allocateCabinCrewTo(crew, flight);
                            allocated = true;
                            break;
                        } catch (DoubleBookedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (!allocated) {
                for (CabinCrew crew : crewDAO.findCabinCrewByHomeBase(route.getDepartureAirportCode())) {
                    if (!schedule.hasConflict(crew, flight) && !schedule.getCabinCrewOf(flight).contains(crew)) {
                        try {
                            schedule.allocateCabinCrewTo(crew, flight);
                            allocated = true;
                            break;
                        } catch (DoubleBookedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (allocated == false) {
                for (CabinCrew crew : crewDAO.getAllCabinCrew()) {
                    if (!schedule.hasConflict(crew, flight) && !schedule.getCabinCrewOf(flight).contains(crew)) {
                        try {
                            schedule.allocateCabinCrewTo(crew, flight);
                            allocated = true;
                            break;
                        } catch (DoubleBookedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }


    }

    /*
     * The ScheduleRunner will pass a reference to itself via this method, so that if you're running for the full 2 mins
     * you can call the reportBestScheduleSoFar() method to keep the runner informed of your progress
     */
    @Override
    public void setSchedulerRunner(SchedulerRunner arg0) {
        // TODO Auto-generated method stub
//        arg0.reportBestScheduleSoFar(); // figure out what type of Schedule object it wants
        this.schedulerRunner = arg0;
    }

    /*
     * Used by the SchedulerRunner to ask you to stop your generateSchedule method when 2 minutes have elapsed
     * I would recommend having this set a boolean variable that is checked frequently by long-running generateSchedule() methods
     * If this method has been called, time is up and it doesn't matter what generateSchedule() returns, it can just return null.
     */
    @Override
    public void stop() {
        // TODO
        this.stop = true;

    }


}
