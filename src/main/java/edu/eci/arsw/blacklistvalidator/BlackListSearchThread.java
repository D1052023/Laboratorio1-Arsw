package edu.eci.arsw.blacklistvalidator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BlackListSearchThread  extends Thread{
    private final int start;
    private final int end;
    private final String ipaddress;
    private final AtomicInteger globalOccurrences;
    
    private final HostBlacklistsDataSourceFacade facade = 
        HostBlacklistsDataSourceFacade.getInstance();
    private int checkedCount = 0;
    private final List<Integer> occurrences = new ArrayList<>();
    
    @Override
    public void run(){
        for(int i=start; i<end; i++){
            if(globalOccurrences.get() >= HostBlackListsValidator.BLACK_LIST_ALARM_COUNT){
                return;
            }

            checkedCount++;

            if(facade.isInBlackListServer(i, ipaddress)){
                occurrences.add(i);
                globalOccurrences.incrementAndGet();
            }
        }
    }
}
