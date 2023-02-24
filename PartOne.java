// Nathanael Gaulke
// COP 4930, Spring 2023
// Assignment 2, Part I

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

class Guest implements Runnable {
    enum GuestState {
        WAITING, IN_LABYRINTH, AT_CUPCAKE, EVERYONE_VISITED
    };

    public int id;
    public PartOne minotaur;
    public boolean sawEmpty;
    private int numTimes;
    public boolean first;
    private PrintWriter output;
    private GuestState state;

    public GuestState getState() {
        return this.state;
    }

    public void setState(GuestState state) {
        this.state = state;
    }

    public Guest(int id, PartOne minotaur, PrintWriter output) {
        this.id = id;
        this.minotaur = minotaur;
        this.output = output;
        numTimes = 0;
        sawEmpty = false;
        first = false;
        state = GuestState.WAITING;
    }

    public int wanderLabyrinth() {
        return (int) (Math.random() * 100) + 1;
    }

    @Override
    public void run() {
        boolean notAllHaveBeenInMaze = true;
        while (notAllHaveBeenInMaze) {
            switch (state) {
                case WAITING:
                    break;
                case IN_LABYRINTH: {
                    if (!minotaur.lock.isLocked())
                        state = GuestState.AT_CUPCAKE;
                    try {
                        Thread.sleep(wanderLabyrinth());
                    } catch (Exception e) {
                        System.out.println("Died in the maze :(");
                    }
                    break;
                }
                case AT_CUPCAKE: {
                    if (!minotaur.lock.isLocked())
                        atCupcake();
                    break;
                }
                case EVERYONE_VISITED:
                    notAllHaveBeenInMaze = false;
                    break;
            }
        }
    }

    public void atCupcake() {
        // The first guest always eats a cupcake and keeps track of how many times they
        // ate a cupcake. This will help track if all guests have definitely entered the
        // labyrinth
        minotaur.lock.lock();
        try {
            if (this.first && minotaur.cupcake.get()) {
                System.out.println("Our first guest(" + id + ") enters the labyrinth.");
                System.out.println("    They see a cupcake on the plate.");
                System.out.println("    They eat the cupcake and leave.");
                minotaur.cupcake.set(false);
                this.numTimes++;
                System.out.println("    The first guest has had a cupcake: " + numTimes + " time(s).");
                if (numTimes == minotaur.numGuests) {
                    setState(GuestState.EVERYONE_VISITED);
                }
            } else if (this.first && !minotaur.cupcake.get()) {
                System.out.println("Our first guest(" + id + ") enters the labyrinth.");
                System.out.println("    They don't see a cupcake on the plate.");
                System.out.println("    They leave the plate and leave.");
                setState(GuestState.WAITING);
            }
            // The first time a guest sees an empty plate they must put a cupcake on the
            // plate for the first guest to eat
            else if (!minotaur.cupcake.get() && !sawEmpty) {
                System.out.println("Our guest(" + id + ") enters the labyrinth.");
                System.out.println("    This is their first time seeing an empty plate in the labyrinth.");
                System.out.println("    They request a new cupcake from the minotaur's servants.");
                System.out.println("    They leave the cupcake and leave.");
                minotaur.cupcake.set(true);
                this.sawEmpty = true;
                setState(GuestState.WAITING);
            }
            // Otherwise the state of the cupcake/plate does not change
            else if (!minotaur.cupcake.get() && sawEmpty) {
                System.out.println("Our guest(" + id + ") enters the labyrinth.");
                System.out.println("    They have seen the plate empty before.");
                System.out.println("    They leave the plate and leave.");
                setState(GuestState.WAITING);
            } else if (minotaur.cupcake.get() && !sawEmpty) {
                System.out.println("Our guest(" + id + ") enters the labyrinth.");
                System.out.println("    They see a cupcake on the plate.");
                System.out.println("    They have not seen the plate empty.");
                System.out.println("    They leave the cupcake and leave.");
                setState(GuestState.WAITING);
            } else if (minotaur.cupcake.get() && sawEmpty) {
                System.out.println("Our guest(" + id + ") enters the labyrinth.");
                System.out.println("    They see a cupcake on the plate.");
                System.out.println("    They have seen the plate empty before.");
                System.out.println("    They leave the plate and leave.");
                setState(GuestState.WAITING);
            }
        } finally {
            minotaur.lock.unlock();
        }
    }
}

public class PartOne {
    public int numGuests;
    public List<Guest> guests;
    public AtomicBoolean cupcake;
    public Guest firstGuest;
    public ReentrantLock lock;
    public final static int DEFAULTGUESTNO = 100;

    public int chooseRandomGuest() {
        return (int) (Math.random() * (numGuests)) + 1;
    }

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        PartOne minotaur = new PartOne();
        minotaur.cupcake = new AtomicBoolean(true);
        minotaur.guests = new ArrayList<>();
        minotaur.lock = new ReentrantLock();
        PrintWriter output = new PrintWriter(new FileWriter("output1.txt"));

        if (args.length < 1) {
            minotaur.numGuests = DEFAULTGUESTNO;
        } else {
            minotaur.numGuests = Integer.parseInt(args[0]);
        }
        for (int i = 0; i < minotaur.numGuests; i++) {
            Guest guest = new Guest(i + 1, minotaur, output);
            minotaur.guests.add(guest);
            Thread th = new Thread(guest);
            th.start();
        }

        // Our first guest is picked
        // There job will be to keep track of how many times they have eaten the cupcake
        int id = minotaur.chooseRandomGuest();
        minotaur.firstGuest = minotaur.guests.get(id - 1);
        System.out.println("The minotaur chooses a guest (" + id + ") to enter the labyrinth!");
        minotaur.firstGuest.first = true;
        minotaur.firstGuest.setState(Guest.GuestState.IN_LABYRINTH);

        // After the first guest, minotaur keeps randomly picking guests to go into the
        // labyrinth
        while (minotaur.firstGuest.getState() != Guest.GuestState.EVERYONE_VISITED) {
            id = minotaur.chooseRandomGuest();
            while (minotaur.guests.get(id - 1).getState() != Guest.GuestState.WAITING)
                id = minotaur.chooseRandomGuest();
            System.out.println("The minotaur chooses a guest (" + id + ") to enter the labyrinth!");
            minotaur.guests.get(id - 1).setState(Guest.GuestState.IN_LABYRINTH);
        }

        // Until the first guest has eaten a cupcake in the maze the n times where n is
        // the number of guests
        boolean allGuestsEntered = true;
        System.out.println("The first guest shouts: 'Everyone has entered the labyrinth!'");
        // A check of all the guests to see if they entered the maze (they must have
        // seen an empty plate or be the first guest)
        for (int i = 0; i < minotaur.numGuests; i++) {
            if (!minotaur.guests.get(i).sawEmpty && !minotaur.guests.get(i).first) {
                System.out.println("The minotaur sees that Guest " + (i + 1) + " never entered the labyrinth");
                allGuestsEntered = false;
            }
            minotaur.guests.get(i).setState(Guest.GuestState.EVERYONE_VISITED);
        }
        if (allGuestsEntered) {
            System.out.println("The minotaur confirms that all guests have entered the maze and nods his assent");
        } else {
            System.out.println("The minotaur is upset and kindly asks his guests to leave");
        }
        long end = System.currentTimeMillis();
        double sec = (end - start) / 1000.0;
        System.out.println("Time to complete: " + sec + "s.");
        output.close();
    }
}