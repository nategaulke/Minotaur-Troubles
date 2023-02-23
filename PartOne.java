// Nathanael Gaulke
// COP 4930, Spring 2023
// Assignment 2, Part I

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class Guest implements Runnable {
    public int id;
    public PartOne minotaur;
    public boolean sawEmpty;
    public int numTimes;
    public boolean first;

    public Guest(int id, PartOne minotaur) {
        this.id = id;
        this.minotaur = minotaur;
        numTimes = 0;
        sawEmpty = false;
        first = false;
    }

    @Override
    public void run() {

    }

    public void inLabyrinth(PrintWriter output) {
        minotaur.lock.lock();
        try {
            // The first guest always eats a cupcake and keeps track of how many times they
            // ate a cupcake. This will help track if all guests have definitely entered the
            // labyrinth
            if (this.first && minotaur.cupcake) {
                output.println("Our first guest(" + id + ") enters the labyrinth.");
                output.println("    They see a cupcake on the plate.");
                output.println("    They eat the cupcake and leave.");
                minotaur.cupcake = false;
                this.numTimes++;
                output.println("    The first guest has had a cupcake: " + numTimes + " time(s).");
            } else if (this.first && !minotaur.cupcake) {
                output.println("Our first guest(" + id + ") enters the labyrinth.");
                output.println("    They don't see a cupcake on the plate.");
                output.println("    They leave the plate and leave.");
            }
            // The first time a guest sees an empty plate they must put a cupcake on the
            // plate for the first guest to eat
            else if (!minotaur.cupcake && !sawEmpty) {
                output.println("Our guest(" + id + ") enters the labyrinth.");
                output.println("    This is their first time seeing an empty plate in the labyrinth.");
                output.println("    They request a new cupcake from the minotaur's servants.");
                output.println("    They leave the cupcake and leave.");
                minotaur.cupcake = true;
                this.sawEmpty = true;

            }
            // Otherwise the state of the cupcake/plate does not change
            else if (!minotaur.cupcake && sawEmpty) {
                output.println("Our guest(" + id + ") enters the labyrinth.");
                output.println("    They have seen the plate empty before.");
                output.println("    They leave the plate and leave.");
            } else if (minotaur.cupcake && !sawEmpty) {
                output.println("Our guest(" + id + ") enters the labyrinth.");
                output.println("    They see a cupcake on the plate.");
                output.println("    They have not seen the plate empty.");
                output.println("    They leave the cupcake and leave.");
            } else if (minotaur.cupcake && sawEmpty) {
                output.println("Our guest(" + id + ") enters the labyrinth.");
                output.println("    They see a cupcake on the plate.");
                output.println("    They have seen the plate empty before.");
                output.println("    They leave the plate and leave.");
            }
        } finally {
            minotaur.lock.unlock();
        }
    }
}

public class PartOne {
    public int numGuests;
    public List<Guest> guests;
    public boolean cupcake;
    public Guest firstGuest;
    public ReentrantLock lock;
    public final static int DEFAULTGUESTNO = 100;

    public int chooseRandomGuest() {
        return (int) (Math.random() * (numGuests)) + 1;
    }

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        PartOne minotaur = new PartOne();
        minotaur.cupcake = true;
        minotaur.lock = new ReentrantLock();
        minotaur.guests = new ArrayList<>();
        PrintWriter output = new PrintWriter(new FileWriter("output1.txt"));

        if (args.length < 1) {
            minotaur.numGuests = DEFAULTGUESTNO;
        } else {
            minotaur.numGuests = Integer.parseInt(args[0]);
        }
        for (int i = 0; i < minotaur.numGuests; i++) {
            Guest guest = new Guest(i + 1, minotaur);
            minotaur.guests.add(guest);
            Thread th = new Thread(guest);
            th.start();
        }

        // Our first guest is picked
        // There job will be to keep track of how many times they have eaten the cupcake
        int id = minotaur.chooseRandomGuest();
        minotaur.firstGuest = minotaur.guests.get(id - 1);
        minotaur.firstGuest.first = true;
        minotaur.firstGuest.inLabyrinth(output);

        // After the first guest, minotaur keeps randomly picking guests to go into the
        // labyrinth
        while (minotaur.firstGuest.numTimes < minotaur.numGuests) {
            id = minotaur.chooseRandomGuest();
            output.println("The minotaur chooses a guest (" + id + ") to enter the labyrinth!");
            minotaur.guests.get(id - 1).inLabyrinth(output);
        }

        // Until the first guest has eaten a cupcake in the maze the n times where n is
        // the number of guests
        boolean allGuestsEntered = true;
        output.println("The first guest shouts: 'Everyone has entered the labyrinth!'");
        // A check of all the guests to see if they entered the maze (they must have
        // seen an empty plate or be the first guest)
        for (int i = 0; i < minotaur.numGuests; i++) {
            if (!minotaur.guests.get(i).sawEmpty && !minotaur.guests.get(i).first) {
                output.println("The minotaur sees that Guest " + (i + 1) + " never entered the labyrinth");
                allGuestsEntered = false;
            }
        }
        if (allGuestsEntered) {
            output.println("The minotaur confirms that all guests have entered the maze and nods his assent");
        } else {
            output.println("The minotaur is upset and kindly asks his guests to leave");
        }
        long end = System.currentTimeMillis();
        double sec = (end - start) / 1000.0;
        output.println("Time to complete: " + sec + "s.");
        output.close();
    }
}