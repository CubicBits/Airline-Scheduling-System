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
            allocatePilots(crewDAO, flight);
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

    /*allocate a Pilots (Captain, First Officer) to a single Flight*/
    private void allocatePilots(ICrewDAO crewDAO, FlightInfo flight) {
        // allocate Captain to flight
        Pilot captain = null;
        Pilot firstOfficer = null;

        // allocate Captain
        for (Pilot p : crewDAO.getAllPilots()) {
            if (p.getRank() == Pilot.Rank.CAPTAIN && !schedule.hasConflict(p, flight)) {
                captain = p;
                break;
            }
        }

        // allocate First Officer
        for (Pilot p : crewDAO.getAllPilots()) {
            if (p.getRank() == Pilot.Rank.FIRST_OFFICER && !schedule.hasConflict(p, flight)) {
                firstOfficer = p;
                break;
            }
        }

        try {
            schedule.allocateCaptainTo(captain, flight);
        } catch (DoubleBookedException e) {
            System.err.println("Captain allocation error");
        }
        try {
            schedule.allocateFirstOfficerTo(firstOfficer, flight);
        } catch (DoubleBookedException e) {
            System.err.println("Captain allocation error");
        }

        // testers
        if (schedule.getCaptainOf(flight) == null) {
            System.out.println("Captain not allocated to flight " + flight);
        }
        if (schedule.getCaptainOf(flight) == null) {
            System.out.println("First Officer not allocated to flight " + flight);
        }
    }

    /*allocate many Cabin Crew to a flight aircraft*/
    private void allocateCabinCrew(ICrewDAO crewDAO, FlightInfo flight) {
        Aircraft aircraft = schedule.getAircraftFor(flight);

        for (int i=0; i<schedule.getAircraftFor(flight).getCabinCrewRequired(); i++) {
            for (CabinCrew crew : crewDAO.getAllCabinCrew()) {
                if (schedule.hasConflict(crew, flight) == false && !schedule.getCabinCrewOf(flight).contains(crew)) {
                    try {
                        schedule.allocateCabinCrewTo(crew, flight);
                        break;
                    } catch (DoubleBookedException e) {
                        e.printStackTrace();
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
