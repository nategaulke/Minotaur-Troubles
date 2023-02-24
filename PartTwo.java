// Nathanael Gaulke
// COP 4930, Spring 2023
// Assignment 2, Part II

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

class Guest implements Runnable {
    enum GuestState {
        LOOKING_AT_VASE, IN_LINE, OUT_OF_LINE
    };

    public int id;
    private GuestState state;
    public PartTwo minotaur;
    int numTimes;
    private boolean stillWantToSeeVase;
    public boolean sawVase;

    public void setState(GuestState state) {
        this.state = state;
    }

    public boolean curiousToSeeVase() {
        int coinFlip = (int) (Math.random() * 2);
        if (coinFlip == 1)
            return true;
        return false;
    }

    public Guest(int id, PartTwo minotaur) {
        this.id = id;
        this.minotaur = minotaur;
        numTimes = 0;
        stillWantToSeeVase = true;
        state = GuestState.OUT_OF_LINE;
        System.out.println("Guest (" + this.id + ") joined the party!");
    }

    // Options:
    // 1) Look at the vase
    // 2) Be in line for the vase
    // 3) Be out of the line for the vase
    @Override
    public void run() {
        while (stillWantToSeeVase) {
            switch (state) {
                case LOOKING_AT_VASE: {
                    System.out.println("Guest (" + this.id + ") is looking at the vase!");
                    numTimes++;
                    this.setState(GuestState.OUT_OF_LINE);
                    minotaur.lineForVase.poll();
                    if (minotaur.lineForVase.peek() != null)
                        minotaur.lineForVase.peek().setState(GuestState.LOOKING_AT_VASE);
                    sawVase = true;
                    break;
                }
                case IN_LINE: {
                    System.out.println("Guest (" + this.id + ") is lined up to look at the vase.");
                    break;
                }
                case OUT_OF_LINE: {
                    System.out.println("Guest (" + this.id + ") is milling around the party.");
                    if (this.curiousToSeeVase()) {
                        if (minotaur.lineForVase.isEmpty()) {
                            minotaur.lineForVase.add(this);
                            this.setState(GuestState.LOOKING_AT_VASE);
                            break;
                        }
                        minotaur.lineForVase.add(this);
                        this.setState(GuestState.IN_LINE);
                    } else if (sawVase) {
                        System.out.println("Guest (" + this.id + ") is done looking at the vase.");
                        this.stillWantToSeeVase = false;
                    }
                    break;
                }
            }
        }
        // Thread stops running once we leave the line
    }
}

public class PartTwo {
    public int numGuests;
    ConcurrentLinkedQueue<Guest> lineForVase;
    public final static int DEFAULTGUESTNO = 100;

    public static void main(String[] args) throws Exception {
        PartTwo minotaur = new PartTwo();
        minotaur.lineForVase = new ConcurrentLinkedQueue<>();

        if (args.length < 1) {
            minotaur.numGuests = DEFAULTGUESTNO;
        } else {
            minotaur.numGuests = Integer.parseInt(args[0]);
        }

        for (int i = 0; i < minotaur.numGuests; i++) {
            Guest guest = new Guest(i + 1, minotaur);
            Thread th = new Thread(guest);
            th.start();
        }
    }
}
