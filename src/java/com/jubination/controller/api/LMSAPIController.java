/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jubination.controller.api;

import com.jubination.backend.service.core.leadcall.parallel.master.CallManager;
import com.jubination.model.pojo.crm.Beneficiaries;
import com.jubination.model.pojo.crm.Client;
import com.jubination.model.pojo.crm.Lead;
import com.jubination.model.pojo.exotel.Call;
import com.jubination.model.pojo.status.ReportStatus;
import com.jubination.service.CallMaintainService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author MumbaiZone
 */
@Controller
public class LMSAPIController {
    
    private static final Logger logger = LoggerFactory.getLogger(LMSAPIController.class);
            
    @Autowired
    CallMaintainService callMaintain;
    
    @Autowired
    CallManager eCallHandler;
    
    private final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";// "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final long BASE = 36;

    
    @RequestMapping(value="/API/freshCall/Asdf7984sdfkjsdhfKFHDJFhshksdjflSFDAKHDfsjdhfrww",method=RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE,headers="Accept=*/*")
    public ResponseEntity freshCalls(@RequestBody Client client,HttpServletRequest request) throws IOException{
            logger.info("LMSAPIController : FreshCalls()Method:");            
            if(client!=null){
                if(client.getTempLeadDetails()!= null && client.getPhoneNumber()!= null && client.getEmailId()!= null){
                    if(client.getSource().trim().equalsIgnoreCase("iceberg")){
                        client.setEmailId(client.getEmailId()+getId());
                        client.setAge("24");
                        client.setName("Iceberg");
                        client.setCampaignName("SMS Campaign");
                        client.setCity("Mumbai");
                        client.setGender("Male");
                        client.setAddress("Address");
                        if(client.getLead() == null){
                            client.setLead(new ArrayList<Lead>());
                        }
                        client.setPincode("NA");
                    }
                    try{
                        if(eCallHandler.getStatus()){        
                            if(callMaintain.buildBackupClient(client)!= null){
                                return new ResponseEntity(HttpStatus.OK);
                            }
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
    
       
    @RequestMapping(value="/API/getDump/Asdf7984sdfkjsdhfKFHDJFhshksdjflSFDAKHDfsjdhfrww",method=RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE,headers="Accept=*/*")
    public @ResponseBody List<Client> getDump(HttpServletRequest request) throws IOException{
            logger.info("inside getDump:");
            System.out.println(request.getParameter("Date"));
            List<Client> list=callMaintain.getClientDump(request.getParameter("Date"));
            for(Client c:list){
               for(Lead l:c.getLead()){
                   l.setClient(null);
                   l.setAdmin(null);
                   for(Call call:l.getCall()){
                       call.setLead(null);
                   }
                   for(Beneficiaries ben:l.getBeneficiaries()){
                       ben.setLead(null);
                   }
               }
            }
            return list;
    }
    
    @RequestMapping(value="/API/reportStatus/Asdf7984sdfkjsdhfKFHDJFhshksdjflSFDAKHDfsjdhfrww",method=RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE,headers="Accept=*/*")
    public ResponseEntity reportStatusEntry(@RequestBody ReportStatus reportStatus,HttpServletRequest request) throws IOException{
            logger.info("inside reportStatusEntry:");
            if(callMaintain.addMissedAppointment(reportStatus)){
                return new ResponseEntity(HttpStatus.OK);
            }
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
    
 
    private String encode(long num) {
            logger.info("inside encode:");
            StringBuilder sb = new StringBuilder();

            while (num > 0) {
                    sb.append(ALPHABET.charAt((int) (num % BASE)));
                    num /= BASE;
            }

            return sb.reverse().toString();
    }
        
    public String getId() {
            logger.info("inside getId:");
            Date date = new Date();
            String id = encode(date.getTime());
            return id;
    }	
    
    /*****************************************************Test API***********************************/
     /** Dummy API of Fresh Call for testing **/
    @RequestMapping(value="/API/testFreshCall/test",method=RequestMethod.GET)
    public ResponseEntity testFreshCalls(HttpServletRequest request) {
        logger.info("Test controller for fresh call:");
        
        //Set Client Details
        Client client = new Client();
        client.setName("sam");
        client.setAge("30");
        client.setGender("Male");
        client.setEmailId("mutkure5@gmail.com");
        client.setTempLeadDetails("ssss");
        client.setPhoneNumber("8657290706");
        client.setSource("not iceberg");
        client.setAddress("Address");
        client.setDateCreation("15-03-2017");
        
        //Set Lead Details
        /**
        Lead lead = new Lead();
        List<Lead> list = new ArrayList();
        lead.setCount(8);  
        lead.setPending(true);
        lead.setBooked(true);
        lead.setNotification(true);
        list.add(lead);
        client.setLead(list);
        **/
        
        //Set Client Details
        client.setCampaignName("Diabetes"); 
        
        //
        
        if(client!=null){
            logger.info("Client not null");
                if(client.getTempLeadDetails()!= null && client.getPhoneNumber()!= null && client.getEmailId()!= null){
                    logger.info("Hello First:");
                    if(client.getSource().trim().equalsIgnoreCase("iceberg")){
                        logger.info("source not null:");
                        client.setEmailId(client.getEmailId()+getId());
                        client.setAge("24");
                        client.setName("Iceberg");
                        client.setCampaignName("SMS Campaign");
                        client.setCity("Mumbai");
                        client.setGender("Male");
                        client.setAddress("Address");
                        
                        if(client.getLead() == null){
                            logger.info("lead not null:");
                            client.setLead(new ArrayList<Lead>());
                        }
                        client.setPincode("NA");
                    }
                    try{
                        if(eCallHandler.getStatus()){
                            logger.info("get status call handler");
                            if(callMaintain.buildBackupClient(client)!= null){
                                logger.info("call maintain:");
                                return new ResponseEntity(HttpStatus.OK);
                            }
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
}
