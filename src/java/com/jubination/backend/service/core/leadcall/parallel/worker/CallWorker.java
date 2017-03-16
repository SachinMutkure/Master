/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jubination.backend.service.core.leadcall.parallel.worker;

import com.jubination.backend.service.core.leadcall.parallel.master.CallManager;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 *
 * @author MumbaiZone
 */
@Component
@Scope("prototype")
public class CallWorker implements Runnable{
    private static final Logger logger= LoggerFactory.getLogger(CallWorker.class);
    @Autowired
    CallWorkerSlave1 slave1;
    
    @Autowired
    private CallWorkerSlave3Leftover slave3Leftover;
    
    @Autowired
    private CallManager manager;
      
    private  boolean status;    
    
    @Override
    public void run() {
       logger.info("Inside run() Method:");   
        try{
               slave1.work();
               if(manager.getClientStage1().isEmpty()&&manager.getClientStage2().isEmpty()&&!manager.getStageThreeUpdates().isEmpty()){
                            slave3Leftover.work();
               }
               status=!status;
        }
        catch(Exception e){
                e.printStackTrace();
        }
    }
           
           
   
     
    
   
    public boolean isStatus() {        
        return status;
    }

    public void setStatus(boolean status) {        
        this.status = status;
    }

    
    
}
