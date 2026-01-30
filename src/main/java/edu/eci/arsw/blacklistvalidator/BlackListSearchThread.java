package edu.eci.arsw.blacklistvalidator;

import java.util.ArrayList;
import java.util.List;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import lombok.Getter;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BlackListSearchThread  extends Thread{
    private final int start;
    private final int end;
    private final String ipaddress;
    
    private final HostBlacklistsDataSourceFacade facade = 
        HostBlacklistsDataSourceFacade.getInstance();
    private int checkedCount = 0;
    private final List<Integer> occurrences = new ArrayList<>();
    
    @Override
    public void run(){
        IntStream.range(start, end).forEach(i -> {
            checkedCount++;
            if(facade.isInBlackListServer(i, ipaddress)){
                occurrences.add(i);
            }
        });
    }    
}
