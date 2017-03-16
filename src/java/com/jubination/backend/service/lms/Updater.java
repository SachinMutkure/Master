/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jubination.backend.service.lms;

import com.jubination.backend.service.sendgrid.EmailService;
import com.jubination.controller.web.UpdateAndBookingController;
import com.jubination.model.pojo.admin.Admin;
import com.jubination.model.pojo.admin.AdminSettings;
import com.jubination.model.pojo.crm.Beneficiaries;
import com.jubination.model.pojo.crm.Client;
import com.jubination.model.pojo.crm.Lead;
import com.jubination.service.AdminMaintainService;
import com.jubination.service.CallMaintainService;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author MumbaiZone
 */
@Component
public class Updater {
    
    @Autowired
    CallMaintainService service;
    
    @Autowired
    private AdminMaintainService adminService;
    
    private String settings="settings";
    
    public String sendAutomatedUpdate(String id){
            String responseText="";
            try {   
                String url="http://mypage.jubination.com/api/booking";
                ObjectMapper mapper = new ObjectMapper();
                //Object to JSON in String
                Lead lead=service.getClientDetails(id);
                
                if(lead!=null){
                    if(lead.getClient()!=null){
                         lead.getClient().setLead(null);
                    }
                    else{
                       List<Client> clients= service.getClientsByLeadId(lead.getLeadId());
                       if(clients!=null&&clients.size()>0){
                           lead.setClient(clients.get(0));
                       }
                    }
                    
                        if(lead.getBeneficiaries()!=null){
                            for(Beneficiaries ben:lead.getBeneficiaries()){
                                ben.setLead(null);
                            }
                            if(lead.getBenCount()==null||(lead.getBenCount()!=null&&lead.getBenCount()<=1)){
                                lead.setBeneficiaries(null);
                            }
                            else{
                                for(int i=0;i<lead.getBenCount()-1;i++){
                                     if(lead.getBeneficiaries().get(i).getName()==null||lead.getBeneficiaries().get(i).getAge()==null||lead.getBeneficiaries().get(i).getGender()==null){
                                        lead.getBeneficiaries().remove(i);
                                        lead.setBenCount(lead.getBenCount()-1);
                                    }
                                     else if(lead.getBeneficiaries().get(i).getName().isEmpty()||lead.getBeneficiaries().get(i).getAge().isEmpty()||lead.getBeneficiaries().get(i).getGender().isEmpty()){
                                        lead.getBeneficiaries().remove(i);
                                        lead.setBenCount(lead.getBenCount()-1);
                                    }
                                }
                            }
                        }
                         if(lead.getAdmin()!=null){
                               lead.getAdmin().setReceivedMessageList(null);
                               lead.getAdmin().setSentMessageList(null);
                               lead.getAdmin().setPassword(null);
                        }
                         lead.setCall(null);
             
                }
                else{
                        lead=new Lead();
                        lead.setClient(new Client());
                        lead.setAdmin(new Admin());
                }
                
                
                
                String jsonString= mapper.writeValueAsString(lead);
                HttpClient httpClient = HttpClientBuilder.create().build();
                System.out.println(jsonString);
                StringEntity requestEntity = new StringEntity(
                jsonString,
                ContentType.APPLICATION_JSON);
                HttpPost postMethod = new HttpPost(url);
                postMethod.setEntity(requestEntity);
                HttpResponse response = httpClient.execute(postMethod);
                HttpEntity entity = response.getEntity();
                responseText = EntityUtils.toString(entity, "UTF-8");
                if(responseText.startsWith("Failed")){
                    
                                                 sendEmailToFailUpdate("disha@jubination.com", lead.getLeadId());
                                                 sendEmailToFailUpdate("trupti@jubination.com", lead.getLeadId());
                                                 sendEmailToFailUpdate("vinay@jubination.com",lead.getLeadId());
                                                 sendEmailToFailUpdate("tauseef@jubination.com", lead.getLeadId());
                                                 sendEmailToFailUpdate("souvik@jubination.com", lead.getLeadId());
                }
            } 
            catch (Exception ex) {
                           Logger.getLogger(UpdateAndBookingController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return responseText;
    }
    
     private void sendEmailToFailUpdate(String email,String leadId){
           AdminSettings adminSettings = adminService.readSettings(settings);
            new EmailService(email,"Failed to updatein LMS",
                                          "Hi,<br/>" +
                                                "<br/>" +
                                                "I am call Bot!<br/>" +
                                                "<br/>" +
                                                "CallBot has failed to update the following lead" +
                                                "<br/>" +
                                                "Lead "+leadId+
                                                "<br/>" +
                                                "<br/>" +
                                                "Wish you a happy & healthy day!<br/>" +
                                                "<br/>" +
                                                "<br/>" +
                                                "Regards,<br/>" + 
                                                "Call Bot ",adminSettings.getMyUsername(),adminSettings.getMyPassword(),adminSettings.getAuth(),adminSettings.getStarttls(),adminSettings.getHost(),adminSettings.getPort(),adminSettings.getSendgridApi()).start();
     }
}
