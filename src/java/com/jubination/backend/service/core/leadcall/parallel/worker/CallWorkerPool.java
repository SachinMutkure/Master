/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jubination.backend.service.core.leadcall.parallel.worker;

import com.jubination.backend.service.core.leadcall.parallel.worker.CallWorker;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 *
 * @author MumbaiZone
 */
@Component
public class CallWorkerPool extends ThreadPoolTaskExecutor{
    private static final Logger logger = LoggerFactory.getLogger(CallWorkerPool.class);
    private List<CallWorker> workerList = new ArrayList<>();
    private final int maxSize=10;
    
                    public CallWorkerPool(){                        
                        super();
                        logger.info("Inside the CallWorkerPool()");
                        setCorePoolSize(maxSize);
                        setMaxPoolSize(maxSize);
                        setWaitForTasksToCompleteOnShutdown(true);
                    }
                    
                     public CallWorker getCallWorker(int i){
                        logger.info("Inside the getCallWorker()");
                        return workerList.get(i);
                     }   
                     
                     public boolean isRunning(int i){
                        logger.info("Inside the isRunning()");
                        return false;
                     }
                     
                     
                     public void startTask(){
                         logger.info("Inside the startTask()");
                         for(int i=0;i<maxSize;i++){
                             
                                if(!workerList.get(i).isStatus()){
                                    
                                    workerList.get(i).setStatus(true);
                                    super.execute(workerList.get(i));
                                    return;
                                }
                            }
                    }

    @Override
    public int getActiveCount() {
        logger.info("Inside the getActiveCount()");
        int count=0;
        for(CallWorker callWorker:workerList){
            
            if(callWorker.isStatus()){            
                count++;
            }
        
        }
//        return count>super.getActiveCount()?count:super.getActiveCount(); 
        logger.info("count value:"+count);
        return count;
    }
                     
                     
                     @Autowired
                     public void setCallWorker0(CallWorker callWorker){
                         //System.out.println("REQUESTED FOR NEW CALLWORKER0");
                         logger.info("REQUESTED FOR NEW CALLWORKER0");
                         workerList.add(callWorker);
                     }
                        
                     @Autowired
                     public void setCallWorker1(CallWorker callWorker){
                         System.out.println("REQUESTED FOR NEW CALLWORKER1");
                         workerList.add(callWorker);
                     }
                        
                     @Autowired
                     public void setCallWorker2(CallWorker callWorker){
                         System.out.println("REQUESTED FOR NEW CALLWORKER2");
                         workerList.add(callWorker);
                     }
                     
                     @Autowired
                     public void setCallWorker3(CallWorker callWorker){
                         System.out.println("REQUESTED FOR NEW CALLWORKER3");
                         workerList.add(callWorker);
                     }
                     
                     @Autowired
                     public void setCallWorker4(CallWorker callWorker){
                         System.out.println("REQUESTED FOR NEW CALLWORKER4");
                         workerList.add(callWorker);
                     }
                     
                     @Autowired
                     public void setCallWorker5(CallWorker callWorker){
                         System.out.println("REQUESTED FOR NEW CALLWORKER5");
                         workerList.add(callWorker);
                     }
                     
                     @Autowired
                     public void setCallWorker6(CallWorker callWorker){
                         System.out.println("REQUESTED FOR NEW CALLWORKER6");
                         workerList.add(callWorker);
                     }
                     
                     @Autowired
                     public void setCallWorker7(CallWorker callWorker){
                         System.out.println("REQUESTED FOR NEW CALLWORKER7");
                         workerList.add(callWorker);
                     }
                     
                     @Autowired
                     public void setCallWorker8(CallWorker callWorker){
                         System.out.println("REQUESTED FOR NEW CALLWORKER8");
                         workerList.add(callWorker);
                     }
                     
                     @Autowired
                     public void setCallWorker9(CallWorker callWorker){
                         System.out.println("REQUESTED FOR NEW CALLWORKER9");
                         workerList.add(callWorker);
                     }

    @Override
    public void setThreadNamePrefix(String threadNamePrefix) {
        System.out.println("*******com.jubination.backend.service.core.leadcall.parallel.worker.CallWorkerPool.setThreadNamePrefix()");
        super.setThreadNamePrefix("callworker - "+threadNamePrefix); //To change body of generated methods, choose Tools | Templates.
    }
                     
}
