/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jubination.backend.service.core.leadcall.parallel.worker;


import com.jubination.backend.service.sendgrid.EmailService;
import com.jubination.backend.pojo.exotel.ExotelMessage;
import com.jubination.backend.service.core.leadcall.parallel.master.CallManager;
import com.jubination.backend.service.exotel.api.ExotelCallService;
import com.jubination.backend.service.lms.Updater;
import com.jubination.model.pojo.admin.AdminSettings;
import com.jubination.model.pojo.exotel.Call;
import com.jubination.model.pojo.crm.Client;
import com.jubination.model.pojo.crm.Lead;
import com.jubination.service.AdminMaintainService;
import com.jubination.service.CallMaintainService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CallWorkerSlave2 {
         
        @Autowired
        private  CallMaintainService service;
        @Autowired
        private  CallManager manager;
        @Autowired
        private CallWorkerSlave3 worker3;
        @Autowired
        private  ExotelCallService callService;
        @Autowired
        private  AdminMaintainService adminService;
        @Autowired
        private Updater updater;
    
        private static final String settings="settings";
    
          public void work(Client client){
              System.out.println("*******com.jubination.backend.service.core.leadcall.parallel.worker.CallWorkerSlave2.work()");
              try{
                //STAGE 2-----------------------GET CALL STATUS----------------------------------------------------------------//
                      
                      
                         //fetching all the sid values
                   if(client!=null&&client.getLead()!=null&&client.getLead().size()>0){
                        int count=0;
                        System.out.println("Stage two count:" +count);
                        Lead lead=client.getLead().get(client.getLead().size()-1);
                        if(lead.getLastCallingThread().equals(Thread.currentThread().getName())){
                        String  sid=tryFetchingSid(lead);
                        int countCheck=0;
                        System.out.print("Lead Count check"+lead.getCount());
                        countCheck=lead.getCount();
                        
                        int nullCount=0;
                        while(count<2000&&!manager.getClientStage2().isEmpty()){
                              try {         
                                       
                                          //Check status
                                          int i=0;
                                          ExotelMessage eMessage=null;
                                          while(i<10&&eMessage==null){
                                                        eMessage=callService.checkStatus(sid);
                                                        i++;
                                          }
                                          if(eMessage==null){
                                                if(nullCount==100){
                                                    manager.getClientStage2().poll();

                                               }
                                               nullCount++;
                                               break;
                                          }
                                          
                                          nullCount=0;
                                          
                                            if(eMessage!=null&&eMessage.getSuccessMessage()!=null){

                                                        Call message=eMessage.getSuccessMessage();
                                                        System.out.println(Thread.currentThread().getName()+" "+"Stage 2:xml message success "+message.getStatus()+" "+message.getCallType()+" "+message.getDialCallDuration());
                                                        if(message.getStatus().contains("queued")||message.getStatus().contains("ringing")||message.getStatus().contains("in-progress")){
                                                                            count++;
                                                                            System.out.println("Stage 2:sid out of queue. Trying for"+count+"th time");
                                                                            if(count%10==0){
                                                                                lead.setCount(countCheck);
                                                                                if(lead.getLeadStatus().contains("Follow up/Call back")||
                                                                                            lead.getLeadStatus().contains("Lead sent to Thyrocare")||
                                                                                            lead.getLeadStatus().contains("Rescheduled")||
                                                                                            lead.getLeadStatus().contains("Not interested")||
                                                                                            lead.getLeadStatus().contains("Not registered")||
                                                                                            lead.getLeadStatus().contains("Language not recognizable")||
                                                                                            lead.getLeadStatus().contains("No Service")||
                                                                                            lead.getLeadStatus().contains("Customer complained")||
                                                                                            lead.getLeadStatus().contains("Disapproved")){
                                                                                           System.out.println("##########"+Thread.currentThread().getName()+" "+"Stage 2: SPOKE AND UPDATED");
                                                                                    }
                                                                                else{
                                                                                    lead.setLeadStatus(message.getStatus());
                                                                                    lead.setCount(countCheck);
                                                                                    service.updateLeadOnly(lead);
                                                                                }
                                                                               
                                                                            }
                                                          }
                                                        else if(message.getStatus().contains("completed")){
                                                                        try {
                                                                                        Thread.sleep(2000);
                                                                                        } catch (InterruptedException ex) {
                                                                                            Logger.getLogger(CallWorkerSlave2.class.getName()).log(Level.SEVERE, null, ex);
                                                                                        }
                                                                                            int countInner=0;
                                                                                            boolean flagInner=false;
                                                                                             if(client.getRealTimeData()!=null){
                                                                                                countInner=100;
                                                                                            }
                                                                                            while(!flagInner&&countInner!=0){
                                                                                                        flagInner=worker3.work(sid);
                                                                                                        countInner--;
                                                                                                        Thread.sleep(900);
                                                                                             }
                                                                                            
                                                                                        if(flagInner){
                                                                                            System.out.println(Thread.currentThread().getName()+" "+"Stage 2 : STAGE 3 ROUND 2- COMPLETED");
                                                                                        }
                                                                                        
//                                                                                      else  if(message.getCallType().contains("trans")){
//                                                                                                            Call storedMessage=service.getCallRecordBySid(message.getSid());
//                                                                                                            if(storedMessage!=null){
//
//                                                                                                                System.out.println(Thread.currentThread().getName()+" "+"Stage 2:sid of the number was present already");
//
//                                                                                                                message.setOrderId(storedMessage.getOrderId());
//                                                                                                                 message.setMessage("Stage 2 Tracking");
//
//                                                                                                                //updating call details
//                                                                                                                if(!storedMessage.getMessage().equals("Stage 3 Tracking")){
//                                                                                                                                processIfStage3NotUpdated(storedMessage, message, client);
//                                                                                                                  }
//                                                                                                                 else{
//                                                                                                                           processIfStage3Updated(storedMessage, message, client);
//                                                                                                                  }
//                                                                                                                }
//                                                                                        }
                                                                        else {
                                                                                             int countInner1=0;
                                                                                            boolean flagInner1=false;
                                                                                             if(client.getRealTimeData()!=null){
                                                                                                countInner1=10;
                                                                                            }
                                                                                            while(!flagInner1&&countInner1!=0){
                                                                                                        flagInner1=worker3.work(sid);
                                                                                                        countInner1--;
                                                                                                        Thread.sleep(500);
                                                                                             }
                                                                                             if(lead.getLeadStatus().contains("Follow up/Call back")||
                                                                                                    lead.getLeadStatus().contains("Lead sent to Thyrocare")||
                                                                                                    lead.getLeadStatus().contains("Rescheduled")||
                                                                                                    lead.getLeadStatus().contains("Not interested")||
                                                                                                    lead.getLeadStatus().contains("Not registered")||
                                                                                                    lead.getLeadStatus().contains("Language not recognizable")||
                                                                                                    lead.getLeadStatus().contains("No Service")||
                                                                                                    lead.getLeadStatus().contains("Customer complained")||
                                                                                                    lead.getLeadStatus().contains("Disapproved")){
                                                                                                        System.out.println("##########"+Thread.currentThread().getName()+" "+"Stage 2: SPOKE AND UPDATED");
                                                                                                }
                                                                                            else{
                                                                                                 lead.setLeadStatus("Hung up while greetings");
                                                                                                        service.updateLeadOnly(lead);
                                                                                            }
                                                                                           
                                                                                              System.out.println(Thread.currentThread().getName()+" "+"Stage 2 : STAGE 3 ROUND 2 - FAILED");
                                                                        }
                                                                         manager.getClientStage2().poll();
                                                                            System.out.println("#"+Thread.currentThread().getName()+"Stage 2 updated. Stage2 out");
                                                                            System.out.println(Thread.currentThread().getName()+" "+"Stage 2:Sid out of queue");
                                                                           //get out of the while loop
                                                                            count=9000;
                                                                            break;

                                                        }
                                                        else {
                                                                            Call storedMessage=service.getCallRecordBySid(message.getSid());
                                                                            if(storedMessage!=null){

                                                                                System.out.println(Thread.currentThread().getName()+" "+"Stage 2:sid of the number was present already");

                                                                                message.setOrderId(storedMessage.getOrderId());
                                                                                message.setMessage("Stage 2 Tracking");

                                                                                //updating call details
                                                                                if(!storedMessage.getMessage().equals("Stage 3 Tracking")){
                                                                                        processIfStage3NotUpdated(storedMessage, message, client);
                                                                                  }
//                                                                                 else{
                                                                                    
//                                                                                                        worker3.work(sid);
//                                                                                           processIfStage3Updated(storedMessage, message, client);
//                                                                                  }
                                                                                }
                                                                            //try stage 3
                                                                             int countInner=0;
                                                                             boolean flagInner=false;
                                                                             if(client.getRealTimeData()!=null){
                                                                                countInner=10;
                                                                            }
                                                                            while(!flagInner&&countInner!=0){
                                                                                        flagInner=worker3.work(sid);
                                                                                        countInner--;
                                                                                        Thread.sleep(500);
                                                                             }
                                                                            manager.getClientStage2().poll();
                                                                            System.out.println("#"+Thread.currentThread().getName()+"Stage 2 updated. Stage2 out");
                                                                            System.out.println(Thread.currentThread().getName()+" "+"Stage 2:Sid out of queue");
                                                                            //get out of the while loop
                                                                            count=9000;
                                                                            break;
                                                        }

                                                }

                                              Thread.sleep(200);
                                    } catch (Exception ex) {
                                        Logger.getLogger(CallWorkerSlave2.class.getName()).log(Level.SEVERE, null, ex);
                                                                            
                                manager.getClientStage2().poll();
                                    }
                      }
                   }
                    
                else{
                    System.out.println("Stage 2 : ERROR FECHING SIDS");
                }
                        
                   }
                       }
            catch(Exception e){
                System.out.println("Error @ outer work Slave 2");
                
                e.printStackTrace();
            } 
              finally{
              }
           }
           
          private void processIfStage3NotUpdated(Call storedMessage, Call message, Client client){
              System.out.println("*******com.jubination.backend.service.core.leadcall.parallel.worker.CallWorkerSlave2.processIfStage3NotUpdated()");
                    System.out.println(Thread.currentThread().getName()+" "+"Stage 2:stage 3 not updated yet");
                        message.setCallTo(storedMessage.getCallTo());
                        //saving in database
                        message.setLead(client.getLead().get(client.getLead().size()-1));
                        if(message.getStatus().contains("busy")){
                            message.getLead().setLeadStatus("Busy");
                            service.updateLeadOnly(message.getLead());
                                sendEmailToFailCall(client.getEmailId());
                            updater.sendAutomatedUpdate(message.getLead().getLeadId());
                        }
                        else if(message.getStatus().contains("failed")){
                            message.getLead().setLeadStatus("Failed");
                            service.updateLeadOnly(message.getLead());
                                sendEmailToFailCall(client.getEmailId());
                             updater.sendAutomatedUpdate(message.getLead().getLeadId());
                        }
                        else if(message.getStatus().contains("no-answer")){
                            message.getLead().setLeadStatus("No Answer");
                            service.updateLeadOnly(message.getLead());
                                sendEmailToFailCall(client.getEmailId());
                             updater.sendAutomatedUpdate(message.getLead().getLeadId());
                        }
                                                                 
//                        else if(message.getStatus().contains("completed")&&message.getCallType().contains("trans")){
//                            message.getLead().setLeadStatus("Hanged up while greetings");
//                            service.updateLeadOnly(message.getLead());
//                             if(client!=null){
//                                sendEmailToFailCall(client.getEmailId());
//                            }
//                             updater.sendAutomatedUpdate(message.getLead().getLeadId());
//                        }


                                 service.updateCallAPIMessage(message);
           }
           
//          private void processIfStage3Updated(Call storedMessage,Call message, Client client){
//                    System.out.println("Stage 2:stage 3 updated already");
//                    storedMessage.setStatus(message.getStatus());
//                    storedMessage.setCallType(message.getCallType());
//                    storedMessage.setRecordingUrl(message.getRecordingUrl());
//                    //saving in database
//                    message.setLead(client.getLead().get(client.getLead().size()-1));
//                    if(message.getStatus().contains("busy")){
//                        message.getLead().setLeadStatus("Busy");
//                        service.updateLeadOnly(message.getLead());
//                            sendEmailToFailCall(client.getEmailId());
//                             updater.sendAutomatedUpdate(message.getLead().getLeadId());
//                    }
//                    else if(message.getStatus().contains("failed")){
//                        message.getLead().setLeadStatus("Failed");
//                        service.updateLeadOnly(message.getLead());
//                            sendEmailToFailCall(client.getEmailId());
//                        
//                             updater.sendAutomatedUpdate(message.getLead().getLeadId());
//                    }
//                    else if(message.getStatus().contains("no-answer")){
//                        message.getLead().setLeadStatus("No Answer");
//                        service.updateLeadOnly(message.getLead());
//                        
//                            sendEmailToFailCall(client.getEmailId());
//                        
//                             updater.sendAutomatedUpdate(message.getLead().getLeadId());
//                    }
////                    else if(message.getStatus().contains("completed")&&message.getCallType().contains("trans")){
////                            message.getLead().setLeadStatus("Hanged up while greetings");
////                            service.updateLeadOnly(message.getLead());
////                             if(client!=null){
////                                sendEmailToFailCall(client.getEmailId());
////                            }
////                             updater.sendAutomatedUpdate(message.getLead().getLeadId());
////                        }
//
//                    service.updateCallAPIMessage(storedMessage);
//           }
           
//          private boolean tryStage3PreProcessing(String sid){
//                if(worker3.work(sid)){
//                                    System.out.println(Thread.currentThread().getName()+" "+"Stage 2 : STAGE 3 ROUND 1- COMPLETED");
//                                                                                                                                         
//                                manager.getClientStage2().poll();
//                                return true;
//                            }
//                return false;
//           }
           
          private String tryFetchingSid(Lead lead){
              System.out.println("*******com.jubination.backend.service.core.leadcall.parallel.worker.CallWorkerSlave2.tryFetchingSid()");
               String sid=null;
                 try{
                                
                                    sid= lead.getCall().get(lead.getCall().size()-1).getSid();
                            }
                            catch(Exception e){
                                
                                            e.printStackTrace();
                                            System.out.println("#"+Thread.currentThread().getName()+"Exception 1. Stage2 out");
                                            manager.getClientStage2().poll();
                                
                                    if(lead!=null){
                                            System.out.println(Thread.currentThread().getName()+" "+"Problem in "+lead.getLeadId());
                                    }
                            }
                 return sid;
           } 
           
         
          private void sendEmailToFailCall(String email){
           AdminSettings adminSettings = adminService.readSettings(settings);
            new EmailService(email,"Your pending health checkup",
                                          "Hi,<br/>" +
                                                "<br/>" +
                                                "Greetings from Jubination!<br/>" +
                                                "<br/>" +
                                                "It's great to have you as a part of Jubination family!<br/>" +
                                                "<br/>" +
                                                "We received your inquiry for Thyrocare health check-up package. We have been trying to get in touch with you to fix your appointment but was unable to get through.<br/>" +
                                                "<br/>" +
                                                "Request you to suggest a suitable slot for a call-back or call us on 02239652819 or WhatsApp your name & email id on 9930421623 or mail us on support@jubination.com <br/>" +
                                                "<br/>" +
                                                "<br/>" +
                                                "Look forward to hearing from you soon. <br/>" +
                                                "<br/>" +
                                                "<br/>" +
                                                "Wish you a happy & healthy day!<br/>" +
                                                "<br/>" +
                                                "<br/>" +
                                                "Regards,<br/>" +
                                                "Trupti<br/>" +
                                                "Customer Happiness Manager<br/>" +
                                                "02239652819 ",adminSettings.getMyUsername(),adminSettings.getMyPassword(),adminSettings.getAuth(),adminSettings.getStarttls(),adminSettings.getHost(),adminSettings.getPort(),adminSettings.getSendgridApi()).start();
     }
//           private void sendTestEmail(String text){
//           AdminSettings adminSettings = adminService.readSettings(settings);
//            new EmailService("souvik@jubination.com","stage 2",
//                                          text+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),adminSettings.getMyUsername(),
//                    adminSettings.getMyPassword(),
//                    adminSettings.getAuth(),
//                    adminSettings.getStarttls(),
//                    adminSettings.getHost(),adminSettings.getPort(),adminSettings.getSendgridApi()).start();
//     }
}
