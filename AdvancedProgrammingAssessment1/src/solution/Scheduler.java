package solution;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import baseclasses.*;


public class Scheduler implements IScheduler {
    Schedule schedule;
    SchedulerRunner schedulerRunner;
    boolean stop = false;
    long bestScheduleSoFarScore = 10000000000L;

    @Override
    public Schedule generateSchedule(IAircraftDAO aircraftDAO, ICrewDAO crewDAO, IRouteDAO routeDAO, IPassengerNumbersDAO passengerNumbersDAO,
                                     LocalDate startDate, LocalDate endDate) {
//    	new Thread(new Runnable() {
//    		public void run() {
//	    		try { 
//	    			Thread.sleep(120000); 
//	    			}
//	    		catch (InterruptedException e) { 
//	    			e.printStackTrace(); 
//	    			}
//	    		stop();
//    		}
//    	}).start();
   	
    	this.schedule = new Schedule(routeDAO, startDate, endDate);
        while (!schedule.isCompleted()) {
            FlightInfo flight = schedule.getRemainingAllocations().get(0);
            allocateAircraft(aircraftDAO, flight);
            allocateCaptain(crewDAO, flight);
            allocateFirstOfficer(crewDAO, flight, 2); // int val being the seed to shuffle
            allocateCabinCrew(crewDAO, flight);
            try {
                schedule.completeAllocationFor(flight);
            } catch (InvalidAllocationException e) {
                e.printStackTrace();
                System.err.print("invalid allocation");
            }
        }
        handleScheduleScore(aircraftDAO, crewDAO, passengerNumbersDAO, schedule);

        while (stop == false) {
        	for (FlightInfo flight : schedule.getCompletedAllocations()) {
        		schedule.unAllocate(flight);
        		allocateAircraft(aircraftDAO, flight);
        		allocateCaptain(crewDAO, flight);
                allocateFirstOfficer(crewDAO, flight, 2); // integer being the seed to shuffle
                allocateCabinCrew(crewDAO, flight);
                try {
                    schedule.completeAllocationFor(flight);
                } catch (InvalidAllocationException e) {
                    e.printStackTrace();
                    System.err.print("invalid allocation");
                }
                if (stop == true) break;
        	}
        	if (schedule.isCompleted()) {
        		handleScheduleScore(aircraftDAO, crewDAO, passengerNumbersDAO, schedule);	
        	}
    
        }
        return schedule; 
    }

    // TODO check rest times for crew: maybe use class airportsInUk verification, or schedule.getCompletedAllocationsFor(x).getFlight()


    /*a function to report best schedules and output improved scores to the console
     * todo maybe rename to compareScheduleScore*/
     private void handleScheduleScore(IAircraftDAO aircraftDAO, ICrewDAO crewDAO, IPassengerNumbersDAO passengerNumbersDAO, Schedule schedule) {
    	 long scheduleScore = 1000000000L;
         scheduleScore = new QualityScoreCalculator(aircraftDAO, crewDAO, passengerNumbersDAO, schedule).calculateQualityScore();

    	 if (scheduleScore < bestScheduleSoFarScore) {
             bestScheduleSoFarScore = scheduleScore;
             schedulerRunner.reportBestScheduleSoFar(schedule);
//             System.out.println(bestScheduleSoFarScore + " (score best)"); // ongoing best score output
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

        // todo allocate aircraft by also analaysing passengerNumbers

        if (schedule.getAircraftFor(flight) == null) { // Tester to detect flights with unallocated Aircrafts
            System.err.println("Aircraft not allocated");
            System.out.println(flight+" flight");
        }
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
        }
                
        // debug tester
        if (captain == null) {
        	System.err.println("Captain allocated as Null");
        }
        
        try {
            schedule.allocateCaptainTo(captain, flight);
        } catch (DoubleBookedException e) {
            System.err.println("Captain allocation error");
            e.printStackTrace();
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
        // debug testers
        if (schedule.getFirstOfficerOf(flight) == null) {
            System.out.println("First Officer not allocated to flight " + flight);
        }
    }

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
            boolean allocated;
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
    
    
    private Boolean findAvailableCabinCrew(List<CabinCrew> crew, FlightInfo flight) {
//      Collections.shuffle(crew, new Random(1));
      for (CabinCrew c : crew) {
          if (!schedule.hasConflict(c, flight) && !schedule.getCabinCrewOf(flight).contains(c)) {
              try {
                  schedule.allocateCabinCrewTo(c, flight);
                  return true;
              } catch (DoubleBookedException e) {
                  e.printStackTrace();
              }
          }
      }
      return false;
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
    	System.out.println("stop condition triggered and should set stop to true [Scheduler.class]");
        this.stop = true;
    }


}
