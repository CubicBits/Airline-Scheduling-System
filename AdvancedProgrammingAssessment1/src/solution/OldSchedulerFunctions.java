package solution;

import baseclasses.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class OldSchedulerFunctions {}
    /*
    * Deprecated functions that I want to keep for reference, or possible implementation.
    * */

    /*private void scheduleOptimiserByNewSchedule(IAircraftDAO aircrafts, ICrewDAO crew, IRouteDAO routes, IPassengerNumbersDAO passengerNumbers,
                                                LocalDate startDate, LocalDate endDate, Schedule objSchedule) {
        *//* attempt to optimise each aspect of the Schedule (aircraft, pilots, cabin crew) and each time run ScoreCalc
     * 1. unAllocate the aircrafts, re-allocate DIFFERENTLY, run Score Calculator (look for improvement)
     * 2. unAllocate the Pilots, re-allocate, run Score Calculator (look for improvement)
     * 3. unAllocate Cabin Crew, re-allocate, run Score Calculator (look for improvement)
     *
     * look at calculating score every third change made to schedule, and experiment for optimal n here
     * *//*
            objSchedule = new Schedule(routes, startDate, endDate);
            List<FlightInfo> remainingAllocations = objSchedule.getRemainingAllocations();

            for (int j = 0; j < remainingAllocations.size(); j++) {
                FlightInfo flightInfo = remainingAllocations.get(j);
                Route route = flightInfo.getFlight();
                Aircraft a = allocateAircraft(aircrafts, route, objSchedule, flightInfo); // allocate Aircraft
                allocatePilots(crew, objSchedule, flightInfo); // allocate Captain and First Officer
                allocateCabinCrew(crew, objSchedule, flightInfo); // allocate the right number of Cabin Crew
                try {
                    objSchedule.completeAllocationFor(flightInfo);
                } catch (InvalidAllocationException e) {
                    e.printStackTrace();
                }
            }

            long scheduleScore = new QualityScoreCalculator(aircrafts, crew, passengerNumbers, objSchedule).calculateQualityScore();
//            System.out.println(scheduleScore + " (curr score)");

            if (scheduleScore < bestScheduleSoFarScore) {
                bestScheduleSoFarScore = scheduleScore;
                schedulerRunner.reportBestScheduleSoFar(objSchedule); // submit best schedule so far
            }
            System.out.println(bestScheduleSoFarScore + " (score best)");

    }*/



    /*Now Deprecated
    * Original method used to allocate Aircrafts to Flight Routes,
    * using list shuffling with random seeding
    private Aircraft allocateAircraft(IAircraftDAO aircrafts, Route route, Schedule objSchedule, FlightInfo flightInfo) {
        // NOW Deprecated
        List<Aircraft> aircraftsAll = aircrafts.getAllAircraft();
        Collections.shuffle(aircraftsAll, new Random(shuffledSeed));

        // attempt 1: allocate an aircraft with starting pos that matches the route departure airport
        for (Aircraft a : aircraftsAll) {
            if (a.getStartingPosition().equals(route.getDepartureAirportCode()) && !objSchedule.hasConflict(a, flightInfo)) {
                try {
                    objSchedule.allocateAircraftTo(a, flightInfo);
                    return a;
                } catch (DoubleBookedException e) {
                    System.err.println("Aircraft allocation error e4");
                }
            }
        }
        // attempt 2: allocate any aircraft that has no conflict (will be located at different airport to route departure)
        for (Aircraft a : aircraftsAll) {
            if (!objSchedule.hasConflict(a, flightInfo)) {
                try {
                    objSchedule.allocateAircraftTo(a, flightInfo);
                    return a;
                } catch (DoubleBookedException e) {
                    System.err.println("exception e8" + e);
                }
            }
        }
        return null;
    }*/


//
//    SchedulerRunner schedulerRunner;
//    Boolean stop = false;
//    long bestScheduleSoFarScore = 100000000000L;
//    int shuffledSeed = 0;
//
//    @Override
//    public Schedule generateSchedule(IAircraftDAO aircrafts, ICrewDAO crew, IRouteDAO routes, IPassengerNumbersDAO passengerNumbers,
//                                     LocalDate startDate, LocalDate endDate) {
//        Schedule schedule = new Schedule(routes, startDate, endDate);
//
//
//        /*Initial Schedule allocations*/
//        while (schedule.isCompleted() != true) { // allocate Pilots and Cabin Crew until all Flights are completed
//            FlightInfo flightAllocation = schedule.getRemainingAllocations().get(0); // first element of remaining allocations
//            allocateAircraft(aircrafts, schedule, flightAllocation);
//            allocatePilots(crew, schedule, flightAllocation); // allocate Captain and First Officer
//            allocateCabinCrew(crew, schedule, flightAllocation); // allocate the right number of Cabin Crew
//            try {
//                schedule.completeAllocationFor(flightAllocation);
////                System.out.println("success");
//            } catch (InvalidAllocationException e) {
//                e.printStackTrace();
//                System.err.print("invalid allocation");
//            }
//        }
//
////        while (stop == false) {
//////            smartScheduleOptimiser(objSchedule, aircrafts, routes, crew, passengerNumbers);
//////            smartScheduleOptimiserv2(objSchedule, aircrafts, routes, crew, passengerNumbers, startDate, endDate);
//////            scheduleOptimiserByFlightInfo(aircrafts, crew, passengerNumbers, objSchedule); // a small yet powerful function.
//////            shuffledSeed++;
////        }
//        return null;
//    }
//
//
//
//    /*a function to report best schedules and output improved scores to the console*/
//    private void handleScheduleScore(IAircraftDAO aircraftDAO, ICrewDAO crewDAO, IPassengerNumbersDAO passengerNumbersDAO, Schedule schedule) {
//        long scheduleScore = new QualityScoreCalculator(aircraftDAO, crewDAO, passengerNumbersDAO, schedule).calculateQualityScore();
//        if (scheduleScore < bestScheduleSoFarScore) {
//            bestScheduleSoFarScore = scheduleScore;
//            try {
//                schedulerRunner.reportBestScheduleSoFar(schedule);
//            } catch (NullPointerException npe) {
//                System.err.println("npe abc");
//                System.out.println();
//                System.err.println("schedulerRunner: "+schedulerRunner);
//                npe.printStackTrace();
//            }
//            System.out.println("\n"+bestScheduleSoFarScore + " (score best)");
////            String[] a = new QualityScoreCalculator(aircraftDAO, crewDAO, passengerNumbersDAO, schedule).describeQualityScore();
////            for (String b : a) {
////                System.out.println(b);
////            }
//        } else {
//            System.out.println("...");
//        }
//    }
//
//    private void allocateAircraft(IAircraftDAO aircrafts, Schedule objSchedule, FlightInfo flightInfo) {
//        // NOW Deprecated
//        List<Aircraft> aircraftsAll = aircrafts.getAllAircraft();
//        Collections.shuffle(aircraftsAll, new Random(shuffledSeed));
//        boolean found = false;
//        Route route = flightInfo.getFlight();
//
//        // attempt 1: allocate an aircraft with starting pos that matches the route departure airport
//        for (Aircraft a : aircraftsAll) {
//            if (a.getStartingPosition().equals(route.getDepartureAirportCode()) && !objSchedule.hasConflict(a, flightInfo)) {
//                try {
//                    objSchedule.allocateAircraftTo(a, flightInfo);
//                    found = true;
//                    break;
//                } catch (DoubleBookedException e) {
//                    System.err.println("Aircraft allocation error e4");
//                }
//            }
//        }
//        // attempt 2: allocate any aircraft that has no conflict (will be located at different airport to route departure)
//        if (found == false) {
//            for (Aircraft a : aircraftsAll) {
//                if (!objSchedule.hasConflict(a, flightInfo)) {
//                    try {
//                        objSchedule.allocateAircraftTo(a, flightInfo);
//                        found = true;
//                        break;
//                    } catch (DoubleBookedException e) {
//                        System.err.println("exception e8" + e);
//                    }
//                }
//            }
//        }
//        if (found == false) {
//            System.out.println("NOT");
//        }
//    }
//
//    /*  */
//    // alternavitevly, schedule Flights using the RouteDAO methods for finding routes by departure airport and day
//    /*private void allocateAircraftsUsingRouteDAO(IAircraftDAO aircraftsDAO, IRouteDAO routeDAO, Schedule schedule) {
//        List<FlightInfo> flightsAllocationsRemaining = schedule.getRemainingAllocations();
//        List<Route> routes = routeDAO.getAllRoutes();
//        List<Route> route2 = routeDAO.findRoutesByDepartureAirportAndDay("BFS","Tue");
//        String r = flightsAllocationsRemaining.get(0).getFlight().getDayOfWeek();
//        System.out.println(r);
//        *//*while (flightsAllocationsRemaining != null) {
//            FlightInfo flightOutbound = flightsAllocationsRemaining.get(0);
//
//
//            flightsAllocationsRemaining.remove(flightOutbound);
//        }*//*
//    }*/
//
//    /*
//     * Assign an aircraft to every flight route, from a single method call.
//     * prioritising turn around times and destinations, to reduce amount of aircraft relocation's necessary.
//     * Airports outside of the UK seems to be the only issue.
//     *
//     * aka allocateManyAircrafts
//     *
//     * */
//
//    /*an optimiser that looks to Clone a schedule with already allocated Aircrafts, and each loop essentially clear the
//Pilots and Crew allocations, then re-allocate Pilots and Crew randomly, recording improved Schedules*/
//    /*private void smartScheduleOptimiserv2(Schedule schedule, IAircraftDAO aircraftDAO, IRouteDAO routeDAO, ICrewDAO crewDAO, IPassengerNumbersDAO passengerNumbersDAO, LocalDate startDate, LocalDate endDate) {
//
//        schedule = new Schedule(routeDAO, startDate, endDate);
//        schedule.sort(); // Sorts the schedule's internal data structures such that flights departing earliest are held first
//
//        allocateAircraftsByOutboundAndReturnFlights(aircraftDAO, routeDAO, schedule); // allocate all Aircrafts, smart method
//        while (schedule.isCompleted() != true) {
//            FlightInfo flightAllocation = schedule.getRemainingAllocations().get(0); // first element of remaining allocations
//
//            allocatePilots(crewDAO, schedule, flightAllocation); // allocate Captain and First Officer
//            allocateCabinCrew(crewDAO, schedule, flightAllocation); // allocate the right number of Cabin Crew
//            try {
//                schedule.completeAllocationFor(flightAllocation);
//            } catch (InvalidAllocationException e) {
//                e.printStackTrace();
//                System.err.print("invalid allocation");
//            }
//        }
//        handleScheduleScore(aircraftDAO, crewDAO, passengerNumbersDAO, schedule);
//    }*/
//
////    private void allocateAircraftsByOutboundAndReturnFlights(IAircraftDAO aircraftsDAO, IRouteDAO routeDAO, Schedule objSchedule) {
////        // todo test to see if there's a score advantage to moving this sort statement to Main()
//////        objSchedule.sort(); // Sorts the schedule's internal data structures such that flights departing earliest are held first
////        List<FlightInfo> flights = objSchedule.getRemainingAllocations();
////
////        for (FlightInfo flightOutbound : flights) { // 1 flight
////            // TODO arrival time verification...
////            // todo accommodate when an outbound flight's aircraft has already been allocated by the return flight process
////            // todo accommodate passengerNumbers
////            for (FlightInfo flightReturn : flights) {
////                // if arrivalAirport of outbound = departureAirport of return Flight
////                if (flightOutbound.getFlight().getArrivalAirportCode().equals(flightReturn.getFlight().getDepartureAirportCode())) {
////                    // allocate Aircraft to outbound and return flights
////                    /*// look at flight times
////                        System.out.println(flightOutbound.getLandingDateTime());
////                        System.out.println(flightReturn.getDepartureDateTime());
////                        System.out.println();*/
////
////                    // 1. allocate aircraft by starting position airport, to match outbound departure airport
////                    List<Aircraft> aircraftsByStartingPosition = aircraftsDAO.findAircraftByStartingPosition(flightOutbound.getFlight().getDepartureAirportCode());
////
//////                    if (objSchedule.getAircraftFor(flightOutbound) == null) { // it can also be assumed that flightReturn has not been allocated.
////                        // 2. if not, allocate the second best Aircraft to routes A and B
////                        // todo
////
////                        // 3. if not, allocate the third best Aircraft to routes A and B
////                        // todo
////
////                        // 4. else, finally, allocate ANY aircraft to both routes A and B
////                        for (Aircraft a : aircraftsDAO.getAllAircraft()) {
////                            if (!objSchedule.hasConflict(a, flightOutbound)) { // determines if aircraft is free to work the outbound flight
////                                try {
////                                    objSchedule.allocateAircraftTo(a, flightOutbound);
//////                                    System.out.println("Aircraft allocated to outbound flight " + flightOutbound);
////                                } catch (DoubleBookedException e) {
////                                    System.out.println(a);
////                                    e.printStackTrace();
////                                }
////                                // allocate the same Aircraft to return Flight
////                                if (!objSchedule.hasConflict(a, flightReturn)) { // determines if aircraft is free to work the return flight
////                                    try {
////                                        objSchedule.allocateAircraftTo(a, flightReturn);
//////                                        System.out.println("Aircraft allocated to return flight   " + flightReturn);
//////                                        successfulAircraftAllocationA++;
//////                                    System.out.println(successfulAircraftAllocation);
////                                        break; //! hmm,
////                                    } catch (DoubleBookedException e) {
////                                        System.out.println(a);
////                                        e.printStackTrace();
////                                    }
////                                } else {
////                                    // un-allocate outbound flight, because Aircraft had conflict with return flight
////                                    objSchedule.unAllocate(flightOutbound);
//////                                    System.out.println("Outbound Aircraft un-allocated now " + objSchedule.getAircraftFor(flightOutbound));
////                                }
////                            }
////                        }
////
//////                    }
////                    break; // exit attempting to allocate this return Aircraft, move onto next Outbound & Return pair.
////                }
////                else {
////                    // allocate different aircrafts to routes A and B, because an Aircraft was not found to arrive at Airport B for a return flight (however, this would be strange, kind of like a one off flight...)
////                    // or, maybe do nothing and let the previous statement handle allocation for A and B.
////                    //? what if there are flight routes that do not have a return OR outbound flight
////                }
////
////            }
////        }
////
////         /*final check to ensure all flights have an allocated Aircraft
////         (9 un-allocated flights when run with Marker.exe config)
////         allocate any Aircraft with no conflict*/
////         int successfulAircraftAllocationC = 1;
////        for (FlightInfo f : flights) {
////            if (objSchedule.getAircraftFor(f) == null) {
////                for (Aircraft a : aircraftsDAO.getAllAircraft()) {
////                    if (!objSchedule.hasConflict(a, f) ) { // determines if aircraft is free to work this flight
////                        try {
////                            objSchedule.allocateAircraftTo(a, f);
////                            successfulAircraftAllocationC++;
////                            break;
////                        } catch (DoubleBookedException e) {
////                            e.printStackTrace();
////                        }
////                    }
////                }
////            }
////        }
////        System.out.println(successfulAircraftAllocationC+" aircraft allocation C");
////        for (FlightInfo f : flights) {
////            objSchedule.getAircraftFor(f);
////        }
////
/////*        System.out.println(flights.size()+" start qty of allocations");
////        System.out.println(successfulAircraftAllocationA+" A successfulAircraftAllocation");
////        System.out.println(successfulAircraftAllocationB+" B successfulAircraftAllocation");
////        System.out.println(successfulAircraftAllocationC+"    C successfulAircraftAllocation");*/
////
////    }
////
////    /*function created to attempt to re-allocate an Aircraft, when the smart Schedule optimiser function
////    unAllocates flight data*/
////
////    private void allocateSingleAircraft(FlightInfo flight, Aircraft aircraft, Schedule schedule) {
////
////        try {
////            schedule.allocateAircraftTo(aircraft, flight);
////        } catch (DoubleBookedException e) {
////            e.printStackTrace();
////        }
////
////    }
////
////    private void allocatePilots(ICrewDAO crew, Schedule objSchedule, FlightInfo flight) {
////        // possible methods to implement for increased optimisation: random picking of assignments that seem similar.
////        // this would require holding crew data fetches in pre defined variables.
////
////        Aircraft aircraft = objSchedule.getAircraftFor(flight);
//////        System.out.println(aircraft);
////        Route route = flight.getFlight();
//////        aircraft.getTypeCode(); //! this returns null on 20ish loop.
////        crew.findPilotsByHomeBaseAndTypeRating(aircraft.getTypeCode(), route.getDepartureAirportCode());
////        List<Pilot> pilotsByHomeBaseAndTypeRating = crew.findPilotsByHomeBaseAndTypeRating(aircraft.getTypeCode(), route.getDepartureAirportCode());
////        Collections.shuffle(pilotsByHomeBaseAndTypeRating, new Random(shuffledSeed));
////        List<Pilot> pilotsByTypeRating = crew.findPilotsByTypeRating(aircraft.getTypeCode());
////        Collections.shuffle(pilotsByTypeRating, new Random(shuffledSeed));
////        List<Pilot> pilotsByHomeBase = crew.findPilotsByHomeBase(route.getDepartureAirportCode());
////        Collections.shuffle(pilotsByHomeBase, new Random(shuffledSeed));
////
////        try {
////            // try allocate Captain to flightInfo object by Home Base and Type Rating
////            for (Pilot p : pilotsByHomeBaseAndTypeRating) {
////                if (p.getRank() == Pilot.Rank.CAPTAIN && !objSchedule.hasConflict(p, flight)) {
////                    try {
////                        objSchedule.allocateCaptainTo(p, flight);
////                        break; // break when the first Captain has been successfully allocated to flightInfo schedule
////                    } catch (DoubleBookedException e) {
////                        System.err.println("Captain allocation error");
////                    }
////                }
////            }
////            // if not, try allocate Captain to flightInfo by only matching type rating
////            if (objSchedule.getCaptainOf(flight) == null) {
////                for (Pilot p : pilotsByTypeRating) {
////                    if (p.getRank() == Pilot.Rank.CAPTAIN && !objSchedule.hasConflict(p, flight)) {
////                        try {
////                            objSchedule.allocateCaptainTo(p, flight);
////                            break; // break when the first Captain has been successfully allocated to flightInfo schedule
////                        } catch (DoubleBookedException e) {
////                            System.err.println("Captain allocation error");
////                        }
////                    }
////                }
////            }
////            // if not, try allocate Captain to flightInfo by only matching home base
////            if (objSchedule.getCaptainOf(flight) == null) {
////                for (Pilot p : pilotsByHomeBase) {
////                    if (p.getRank() == Pilot.Rank.CAPTAIN && !objSchedule.hasConflict(p, flight)) {
////                        try {
////                            objSchedule.allocateCaptainTo(p, flight);
////                            break; // break when the first Captain has been successfully allocated to flightInfo schedule
////                        } catch (DoubleBookedException e) {
////                            System.err.println("Captain allocation error");
////                        }
////                    }
////                }
////            }
////            // if not, finally try allocate ANY Captain to flightInfo
////            if (objSchedule.getCaptainOf(flight) == null) {
////                for (Pilot p : crew.getAllPilots()) {
////                    if (p.getRank() == Pilot.Rank.CAPTAIN && !objSchedule.hasConflict(p, flight)) {
////                        try {
////                            objSchedule.allocateCaptainTo(p, flight);
////                            break; // break when the first Captain has been successfully allocated to flightInfo schedule
////                        } catch (DoubleBookedException e) {
////                            System.err.println("Captain allocation error");
////                        }
////                    }
////                }
////            }
////
////            // Assign FIRST OFFICER to aircraft (flightInfo)
////            for (Pilot p : pilotsByHomeBaseAndTypeRating) {
////                if (p.getRank() == Pilot.Rank.FIRST_OFFICER && !objSchedule.hasConflict(p, flight)) {
////                    try {
////                        objSchedule.allocateFirstOfficerTo(p, flight);
////                        break; // break when the first Captain has been successfully allocated to flightInfo schedule
////                    } catch (DoubleBookedException e) {
////                        System.err.println("First Officer allocation error");
////                    }
////                }
////            }
////            // try allocate first officer to flightInfo by only matching type rating
////            if (objSchedule.getFirstOfficerOf(flight) == null) {
////                for (Pilot p : crew.findPilotsByTypeRating(aircraft.getTypeCode())) {
////                    if (p.getRank() == Pilot.Rank.FIRST_OFFICER && !objSchedule.hasConflict(p, flight)) {
////                        try {
////                            objSchedule.allocateFirstOfficerTo(p, flight);
////                            break; // break when the first Captain has been successfully allocated to flightInfo schedule
////                        } catch (DoubleBookedException e) {
////                            System.err.println("First Officer allocation error");
////                        }
////                    }
////                }
////            }
////            // try allocate Captain to flightInfo by only matching home base
////            if (objSchedule.getFirstOfficerOf(flight) == null) {
////                for (Pilot p : crew.findPilotsByHomeBase(route.getDepartureAirportCode())) {
////                    if (p.getRank() == Pilot.Rank.FIRST_OFFICER && !objSchedule.hasConflict(p, flight)) {
////                        try {
////                            objSchedule.allocateFirstOfficerTo(p, flight);
////                            break; // break when the first Captain has been successfully allocated to flightInfo schedule
////                        } catch (DoubleBookedException e) {
////                            System.err.println("First Officer allocation error");
////                        }
////                    }
////                }
////            }
////            // finally try allocate ANY Captain to flightInfo
////            if (objSchedule.getFirstOfficerOf(flight) == null) {
////                for (Pilot p : crew.getAllPilots()) {
////                    if (p.getRank() == Pilot.Rank.FIRST_OFFICER && !objSchedule.hasConflict(p, flight)) {
////                        try {
////                            objSchedule.allocateFirstOfficerTo(p, flight);
////                            break; // break when the first Captain has been successfully allocated to flightInfo schedule
////                        } catch (DoubleBookedException e) {
////                            System.err.println("First Officer allocation error");
////                        }
////                    }
////                }
////            }
////
////        } catch (Exception e) {
////            System.err.println("e: " + e);
////        }
////    }
////
////    // TODO
////    static int cabinCrewAllocated = 0;
////    static int cabinCrewAllocatedClassA = 0;
////    static int cabinCrewAllocatedClassB = 0;
////    static int cabinCrewAllocatedClassC = 0;
////    static int cabinCrewAllocatedClassD = 0;
////
////    private void allocateCabinCrew(ICrewDAO crew, Schedule objSchedule, FlightInfo flightInfo) {
////        // potentially remove cabin crew members from an array list once they've already been allocated, instead of using && .contains
////
////        Aircraft a = objSchedule.getAircraftFor(flightInfo);
////        Route route = flightInfo.getFlight();
////
////        // TODO improve allocate algorithm: read points system
////        for (int i = 0; i < a.getCabinCrewRequired(); i++) { // while an aircraft still has outstanding cabin crew allocations
////
////            Boolean found = false;
////            // attempt to allocate a matching crew by home base and type rating
////            //! however, this seems to decrease score. look into why this might be, it may be appropriate to methodically allocate crew instead of random allocation
//////            for (CabinCrew c : crew.findCabinCrewByHomeBaseAndTypeRating(a.getTypeCode(), route.getDepartureAirportCode())) {
//////                if (objSchedule.hasConflict(c, flightInfo) == false
//////                        && !objSchedule.getCabinCrewOf(flightInfo).contains(c)) { // && check crew member isn't already allocated to this flight
//////                    try {
//////                        objSchedule.allocateCabinCrewTo(c, flightInfo);
//////                        cabinCrewAllocated++; // a counter for testing purposes
//////                        cabinCrewAllocatedClassA++; // a counter for testing purposes
//////
//////                    } catch (DoubleBookedException e) {
//////                        e.printStackTrace();
//////                    }
//////                    found = true;
//////                    break;
//////                }
//////            }
////            // next, if prev wasn't successful, attempt to allocate a matching crew by type rating only
////            List<CabinCrew> cabinCrewByTypeRating = crew.findCabinCrewByTypeRating(a.getTypeCode());
////            Collections.shuffle(cabinCrewByTypeRating, new Random(shuffledSeed));
////            for (CabinCrew c : cabinCrewByTypeRating) {
////                if (objSchedule.hasConflict(c, flightInfo) == false
////                        && !objSchedule.getCabinCrewOf(flightInfo).contains(c)) { // && check crew member isn't already allocated to this flight
////                    try {
////                        objSchedule.allocateCabinCrewTo(c, flightInfo);
//////                        System.out.println("allocation");
////                        cabinCrewAllocated++; // a counter for testing purposes
////                        cabinCrewAllocatedClassB++; // a counter for testing purposes
////
////                    } catch (DoubleBookedException e) {
////                        e.printStackTrace();
////                    }
////                    found = true;
////                    break;
////                }
////            }
////            // next, if prev wasn't successful, attempt to allocate a matching crew by home base only
//////            List<CabinCrew> cabinCrewByHomeBase = crew.findCabinCrewByHomeBase(route.getDepartureAirportCode());
//////            Collections.shuffle(cabinCrewByHomeBase, new Random(shuffledSeed++));
//////            for (CabinCrew c : cabinCrewByHomeBase) {
//////                if (objSchedule.hasConflict(c, flightInfo) == false
//////                        && !objSchedule.getCabinCrewOf(flightInfo).contains(c)) { // && check crew member isn't already allocated to this flight
//////                    try {
//////                        objSchedule.allocateCabinCrewTo(c, flightInfo);
//////                        cabinCrewAllocated++; // a counter for testing purposes
//////                        cabinCrewAllocatedClassC++; // a counter for testing purposes
//////
//////                    } catch (DoubleBookedException e) {
//////                        e.printStackTrace();
//////                    }
//////                    found = true;
//////                    break;
//////                }
//////            }
////            // finally, if prev wasn't successful, attempt to allocate any remaining crew member.
////            if (found == false) {
////                List<CabinCrew> cabinCrewAll = crew.getAllCabinCrew();
////                Collections.shuffle(cabinCrewAll, new Random(shuffledSeed));
////                for (CabinCrew c : cabinCrewAll) {
////                    if (objSchedule.hasConflict(c, flightInfo) == false
////                            && !objSchedule.getCabinCrewOf(flightInfo).contains(c)) { // && check crew member isn't already allocated to this flight
////                        try {
////                            objSchedule.allocateCabinCrewTo(c, flightInfo);
////                        } catch (DoubleBookedException e) {
////                            e.printStackTrace();
////                        }
////                        found = true;
////                        break;
////                    }
////
////                }
////            }
////        }
////
////    }
////
////}
