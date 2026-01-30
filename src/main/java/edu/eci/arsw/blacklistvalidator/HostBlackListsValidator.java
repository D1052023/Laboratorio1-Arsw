/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;

    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int N)
            throws InterruptedException {

        HostBlacklistsDataSourceFacade facade =
                HostBlacklistsDataSourceFacade.getInstance();

        int totalServers = facade.getRegisteredServersCount();

        int blockSize = totalServers / N;
        int start = 0;

        List<BlackListSearchThread> threads = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            int end = (i == N - 1)
                    ? totalServers
                    : start + blockSize;

            BlackListSearchThread thread =
                    new BlackListSearchThread(start, end, ipaddress);

            threads.add(thread);
            start = end;
        }

        for (BlackListSearchThread thread : threads) {
            thread.start();
        }

        List<Integer> blackListOccurrences = new ArrayList<>();
        int totalChecked = 0;
        int totalOccurrences = 0;

        for (BlackListSearchThread thread : threads) {
            thread.join();

            totalChecked += thread.getCheckedCount();
            totalOccurrences += thread.getOccurrences().size();
            blackListOccurrences.addAll(thread.getOccurrences());
        }

  
        LOG.info("Checked Black Lists: "
                + totalChecked + " of " + totalServers);

   
        if (totalOccurrences >= BLACK_LIST_ALARM_COUNT) {
            facade.reportAsNotTrustworthy(ipaddress);
            LOG.info("HOST " + ipaddress + " Reported as NOT trustworthy");
        } else {
            facade.reportAsTrustworthy(ipaddress);
            LOG.info("HOST " + ipaddress + " Reported as trustworthy");
        }

        return blackListOccurrences;
    }
    
}
