package edu.eci.arsw.blacklistvalidator;

import java.util.List;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        HostBlackListsValidator hblv = new HostBlackListsValidator();

        long startTime = System.currentTimeMillis();

        List<Integer> blackListOcurrences =
                hblv.checkHost("200.24.24.55", 50);

        long endTime = System.currentTimeMillis();

        System.out.println("Total Time: " + (endTime - startTime) + " ms");
        System.out.println("The host was found in the following blacklists: "
                + blackListOcurrences);
    }
}
