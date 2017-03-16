/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jubination.service;

import com.jubination.backend.service.sendgrid.EmailService;
import com.jubination.backend.service.core.leadcall.parallel.master.CallScheduler;
import com.jubination.model.dao.AdminDAOImpl;
import com.jubination.model.dao.CallAPIMessageDAOImpl;
import com.jubination.model.dao.ClientDAOImpl;
import com.jubination.model.dao.ProductsDAOImpl;
import com.jubination.model.dao.ReportDAOImpl;
import com.jubination.model.pojo.admin.AdminSettings;
import com.jubination.model.pojo.crm.Beneficiaries;
import com.jubination.model.pojo.exotel.Call;
import com.jubination.model.pojo.products.Campaigns;
import com.jubination.model.pojo.crm.Client;
import com.jubination.model.pojo.crm.Lead;
import com.jubination.model.pojo.crm.TempClient;
import com.jubination.model.pojo.status.ReportStatus;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Administrator
 */

@Service
@Transactional
public class CallMaintainService {
    private static final Logger logger = LoggerFactory.getLogger(CallMaintainService.class);
    
    @Autowired
    CallAPIMessageDAOImpl callDao;
    @Autowired
    ClientDAOImpl clientDao;
    @Autowired
    ProductsDAOImpl pDao;
    @Autowired
    ReportDAOImpl rDao;
    @Autowired 
    AdminDAOImpl adao;
    
    @Autowired
    CallScheduler operator;
    @Autowired
    AdminMaintainService adminService;
     
     String errorFlag="NA";
    
    private final String excelOutputDirectory="C:\\Users\\Administrator\\Documents\\NetBeansProjects\\JubinationV3\\web\\admin\\";
    
    private final String excelOutputBuildDirectory="C:\\Users\\Administrator\\Documents\\NetBeansProjects\\JubinationV3\\build\\web\\admin\\";
  
    private final String settings="settings";

    
 
    public boolean addClientAndUnmarkBackupClient(Client client, Lead lead, Call call){
        System.out.println("com.jubination.service.CallMaintainService.addClientAndUnmarkBackupClient()");
     if(addClientCall(client,lead,call)){
         return unmarkBackupClient(client);
     }
     return false;
 }
 
    /** Saving the client call update **/
    /** Here we got Hibernate exception that is id */
    public boolean addClientCall(Client client,Lead lead,Call message){
        System.out.println("com.jubination.service.CallMaintainService.addClientCall()");
        List<Client> storedClientList=getClientsByEmailId(client.getEmailId());
     
        Client storedClient=null;
        if(storedClientList!=null&&!storedClientList.isEmpty()){
             storedClient=(Client) getClientsByEmailId(client.getEmailId()).get(0);
        }
        lead.setClient(client);       
        message.setLead(lead);
  
        if(storedClient==null){
                System.out.println("Client not present");
        }
        else{
                System.out.println("Client present");
                client.setClientId(storedClient.getClientId());

                if(client.getAddress()==null&&storedClient.getAddress()!=null){
                    client.setAddress(storedClient.getAddress());
                }
        }
            
        //if new lead
        if(lead.getLeadId()==null||lead.getLeadId().isEmpty()){
            lead.setLeadId(client.getTempLeadDetails());
        }
        
        Lead storedLead=(Lead)clientDao.readInnerPropertyList(lead);// Here the exception Generates
        System.out.println("Value of Lead object: "+storedLead);
        if(storedLead==null){
            System.out.println("Lead not present");
            if(lead.getBeneficiaries()!=null&&lead.getBeneficiaries().size()>0){
                                  
                    //if ben<10 add bens
                    System.out.println("get Beneficiaries size :"+lead.getBeneficiaries().size());
                    int count=9-lead.getBeneficiaries().size();
                    while(count>=0){
                        lead.getBeneficiaries().add(new Beneficiaries());
                        count--;
                    }
                    List<Beneficiaries> benList=lead.getBeneficiaries();

                    //  lead.setClient(null);
                    //  client.setLead(new ArrayList<Lead>());

                    lead.setBeneficiaries(new ArrayList<Beneficiaries>());
                                            
                            if(updateLeadOfClient(client, lead)){
                                 for(Beneficiaries ben:benList){
                                             ben.setLead(null);
                                             if(addBeneficiaries(ben)!=null){
                                                  if(updateSavedBenOfLead(lead, ben)!=null){
                                                      lead=(Lead)clientDao.readInnerPropertyList(lead);
                                                       System.out.println(lead.getBeneficiaries().size()+"::::::::::::::::::::::::::::::::::::::::::::::::::::::IS MY BEN SIZE YO");

                                                  }
                                              }
                                     }

                                     if(addCallAPIMessage(message)!=null){
                                           if(updateSavedCallOfLead(lead, message)!=null){
                                               lead=(Lead)clientDao.readInnerPropertyList(lead);
                                                System.out.println(lead.getBeneficiaries().size()+"::::::::::::::::::::::::::::::::::::::::::::::::::::::MSGIS MY BEN SIZE YO");

                                               client.getLead().add(lead);
                                               return lead!=null;
                                           }
                                       }

                             }

                         
                           }
                           else{
                                    System.out.println("Lead not present legacy");
                                    if(updateLeadOfClient(client, lead)){
                                        if(addCallAPIMessage(message)!=null){
                                             if(updateSavedCallOfLead(lead, message)!=null){
                                                 lead=(Lead)clientDao.readInnerPropertyList(lead);
                                                 return lead!=null;
                                             }
                                         }
                                    }
                        }
                         
                 }
                 else{
                           System.out.println("Lead present");
                            if(updateLeadOfClient(client, lead)){
                           if(addCallAPIMessage(message)!=null){
                                if(updateSavedCallOfLead(lead, message)!=null){
                                    lead=(Lead)clientDao.readInnerPropertyList(lead);
                                    return lead!=null;
                                }
                            }
                       }
                 }    
            
       return false;
 }
 
    public boolean updateTemporaryClient(Client client,Lead lead) {
        System.out.println("com.jubination.service.CallMaintainService.updateTemporaryClient()");
        List<Client> storedClientList=getClientsByEmailId(client.getEmailId());
        Client storedClient=null;
        if(storedClientList!=null&&storedClientList.size()>0){
             storedClient=(Client) getClientsByEmailId(client.getEmailId()).get(0);
        }
        if(storedClient==null){
                   System.out.println("Client not present");
        }
        else{
                   System.out.println("Client present");
                 client.setClientId(storedClient.getClientId());

                 if(client.getAddress()==null&&storedClient.getAddress()!=null){
                     client.setAddress(storedClient.getAddress());
                 }

        }
            
        //if new lead
        Lead storedLead=(Lead)clientDao.readInnerPropertyList(lead);//Here got hibernate exception
        if(storedLead==null){
                   System.out.println("Lead not present");
                  if(lead.getBeneficiaries()!=null&&lead.getBeneficiaries().size()>0){

                                   //if ben<10 add bens
                                   System.out.println("Beneficiaries size :" +lead.getBeneficiaries().size());
                                   int count=9-lead.getBeneficiaries().size();
                                   
                                   System.out.println("Count Value :"+count);
                                   while(count>=0){
                                       lead.getBeneficiaries().add(new Beneficiaries());
                                       count--; // this count not set anywhere...
                                   }
                                   List<Beneficiaries> benList=lead.getBeneficiaries();


//                                            lead.setClient(null);
//                                            client.setLead(new ArrayList<Lead>());

                                   lead.setBeneficiaries(new ArrayList<Beneficiaries>());

                                  if(updateLeadOfClient(client, lead)){
                                       for(Beneficiaries ben:benList){
                                                   ben.setLead(null);
                                                   if(addBeneficiaries(ben)!=null){
                                                        if(updateSavedBenOfLead(lead, ben)!=null){
                                                            lead=(Lead)clientDao.readInnerPropertyList(lead);
                                                             System.out.println(lead.getBeneficiaries().size()+"::::::::::::::::::::::::::::::::::::::::::::::::::::::IS MY BEN SIZE YO");

                                                        }
                                                    }
                                           }
                                   }


                  }
                   else{
                           System.out.println("Lead not present legacy");
                           if(updateLeadOfClient(client, lead)){
                                   return true;
                           }
               }

        }
        else{
                  System.out.println("Lead present");
                   if(updateLeadOfClient(client, lead)){
                       return true;
                   }
        }
        return false;
    }
   
    public List<Client> getClientDump(String date){
    return (List<Client>) clientDao.getByProperty(date,"DateUpdatedFull");
 }
    
    public List<Client> getClientDumpForDisplay(String date){
    return (List<Client>) clientDao.getByProperty(date,"DateCreated");
 }
    
    /** METHOD FOR SAVE TEMP CLIENT...
     * 
     */
    public TempClient buildBackupClient(Client client){
            logger.info("Inside the buildBackupClient:");
            String beneficiaries="";
            Lead lead=new Lead();
            logger.info("Lead :" +client.getLead().toString());
            if(client.getLead()!= null && !client.getLead().isEmpty()){
                logger.info("lead not empty");
                                lead=client.getLead().get(0);
                                lead.setLeadId(client.getTempLeadDetails());
                                lead.setOrderId("JUBI0000"+lead.getLeadId());
                                lead.setOrderBy(client.getName());
                                lead.setDateCreation(client.getDateCreation());
                                lead.setDateCreation(client.getDateUpdated());
                                lead.setProduct(client.getCampaignName());

                                Campaigns camp = (Campaigns) pDao.readCampaignProperty(client.getCampaignName());
                                logger.info("Campaigns value :"+camp.toString());
                                if(camp!=null){
                                    lead.setMargin(camp.getMargin());
                                    lead.setHandlingCharges(camp.getHc());
                                    lead.setPasson(camp.getPasson());
                                    lead.setProduct(camp.getProducts());
                                    lead.setRate(camp.getRate());
                                    lead.setReportCode(camp.getReportCode());
                                    lead.setServiceType(camp.getServiceType());
                                }
                                logger.info("Beneficiaries value :"+ lead.getBeneficiaries().toString());
                                if(lead.getBeneficiaries()!=null){
                                        int count=9-lead.getBeneficiaries().size();
                                        logger.info("Count :"+count);
                                        while(count>=0){
                                            lead.getBeneficiaries().add(new Beneficiaries());
                                            count--;
                                            logger.info("Count :"+count);
                                        }

                                        if(!lead.getBeneficiaries().isEmpty()){
                                                 for(Beneficiaries bens:lead.getBeneficiaries()){
                                                     logger.info("Get beneficiary from lead:");
                                                              bens.setLead(lead);
                                                              if(bens.getName()!=null){
                                                                bens.setName(bens.getName().replace("@", ""));
                                                                bens.setName(bens.getName().replace(":", ""));
                                                              }
                                                              if(bens.getAge()!=null){
                                                                bens.setAge(bens.getAge().replace("@", ""));
                                                                bens.setAge(bens.getAge().replace(":", ""));
                                                              }
                                                              if(bens.getGender()!=null){
                                                                bens.setGender(bens.getGender().replace("@", ""));
                                                                bens.setGender(bens.getGender().replace(":", ""));
                                                              }
                                                              
                                                              beneficiaries+=bens.getName()+"@"+bens.getGender()+"@"+bens.getAge()+":";
                                                 }
                                        }
                                }

            }else{
                logger.info("lead Empty:");
                
                                lead.setLeadId(client.getTempLeadDetails());
                                lead.setOrderId("JUBI0000"+lead.getLeadId());
                                lead.setOrderBy(client.getName());
                                lead.setDateCreation(client.getDateCreation());
                                lead.setDateCreation(client.getDateUpdated());
                                lead.setProduct(client.getCampaignName());
                                Campaigns camp= (Campaigns) pDao.readCampaignProperty(client.getCampaignName());
                                logger.info("Else");
                                if(camp!=null){
                                    lead.setMargin(camp.getMargin());
                                    lead.setHandlingCharges(camp.getHc());
                                    lead.setPasson(camp.getPasson());
                                    lead.setProduct(camp.getProducts());
                                    lead.setRate(camp.getRate());
                                    lead.setReportCode(camp.getReportCode());
                                    lead.setServiceType(camp.getServiceType());
                                }
                                
                                int count=0;
                                        while(count<10){
                                            lead.getBeneficiaries().add(new Beneficiaries());
                                            logger.info("Count ++ value: "+count);
//                                            if(count==0){
//                                                lead.getBeneficiaries().get(0).setName(client.getName());
//                                                lead.getBeneficiaries().get(0).setAge(client.getAge());
//                                                lead.getBeneficiaries().get(0).setGender(client.getGender());
//                                            }
                                            count++;
                                        }
                                
            }
            
            TempClient tempClient=new TempClient(client.getEmailId(), client.getName(), client.getCampaignName(), client.getAge(), client.getGender(), client.getPhoneNumber(), client.getAddress(), client.getCity(), client.getPincode(), client.getDateCreation(), client.getDateUpdated(), client.getIpAddress(), client.getInitialComments(), client.getSource(), client.getPubId(), null, false, client.getTempLeadDetails(), lead.getHardcopy(), lead.getOrderId() , lead.getProduct(), lead.getServiceType(), lead.getOrderBy(), lead.getAppointmentDate(), lead.getAppointmentTime(), lead.getBenCount(),lead.getReportCode(),lead.getRate() , lead.getMargin(), lead.getPasson(),lead.getPayType(), lead.getHandlingCharges(), beneficiaries);
            logger.info("tempClient value : name:"+tempClient.getName()+" Email: "+client.getEmailId()+" Age:" +client.getAge()+" Campaign Name: "+client.getCampaignName()+" Gender: "+client.getGender()+" Get Ph.NO: "+client.getPhoneNumber());
            

       //     if(!checkIfClientPresent(client.getPhoneNumber(),new SimpleDateFormat("yyyy-MM-dd").format(new Date()))){
            
            if(!checkIfClientPresent(client.getPhoneNumber(),client.getDateCreation().split(" ")[0],client.getTempLeadDetails())){
                logger.info("inside if");
                            operator.getClients().offer(client); // here above checking duplicacy of client
                            operator.setFreshFlag(true);  
                            tempClient.setCallStatus("pending"); // ***** Face understanding issue.
            
            }else if(tempClient.getProduct().contains("DietChart")||tempClient.getCampaignName().contains("DietChart")){
                logger.info("inside else if");
                             tempClient.setCallStatus("diet");
            }
            else{
                logger.info("inside else duplicate ");
                             tempClient.setCallStatus("duplicate");
            }
            logger.info("out of iff"); 
        return (TempClient) clientDao.buildBackupEntity(tempClient); // Here save client into temp db.
       }
   
    public TempClient readBackupClient(String leadId){
       List<TempClient> list=clientDao.readBackupEntity(leadId);
       if(list!=null){
           if(list.size()>0){
               return list.get(0);
           }
       }
       return new TempClient();
   }
   
    public boolean updateBackupClient(TempClient client){
       return clientDao.updateBackupEntity(client);
     
   }
    
     
      
       public boolean updateOvernightClient(String leadId){
          List<TempClient> list= (List<TempClient>) clientDao.readBackupEntity(leadId);
          if(list!=null&&list.size()>0){
              TempClient client=list.get(0);
              client.setOvernight(true);
                return clientDao.updateBackupEntity(client);
          }
                  
           return false;       
     
     
   }
   
    public Boolean checkIfClientPresent(String number,String date, String leadId){
        logger.info("inside checkIfClientPresent:");
        List<TempClient> list=clientDao.readBackupEntityByNumberAndDate(number,date);
        logger.info("List :"+list);
        List<TempClient> list2=clientDao.readBackupEntityByLeadId(leadId);
        logger.info("List2 :" +list2);
        if(list!=null){
            if(list2!=null){
                 list.addAll(list2);
            }
            //System.out.println("in check up"+number+"list size"+list.size());
            logger.info("in check up"+number+"list size"+list.size());
            if(!list.isEmpty()){
                //System.out.println("present");
                logger.info("present");
                return true;
            }
        }
        logger.info("Not Present");
        
       return false;
   }
   
    public boolean unmarkBackupClient(Client client){
       TempClient tempClient=readBackupClient(client.getTempLeadDetails());
       tempClient.setCallStatus(null);
       return updateBackupClient(tempClient);
   }
   
    public List<Client> getMarkedClients(){
        System.out.println("com.jubination.service.CallMaintainService.getMarkedClients()");
        List<TempClient> tempClientList = clientDao.readClientWithStatus("pending");
        List<Client> clientList = new ArrayList<>();
        for(TempClient tempClient:tempClientList){
            Client client = new Client(tempClient.getName(), tempClient.getCampaignName(), tempClient.getAge(), tempClient.getGender(), tempClient.getEmailId(), tempClient.getPhoneNumber(), tempClient.getAddress(), tempClient.getCity(), tempClient.getPincode(), tempClient.getDateCreation(), tempClient.getDateUpdated(), false, tempClient.getTempLeadDetails(), tempClient.getIpAddress(), tempClient.getInitialComments(),tempClient.getSource());
            Lead lead = new Lead();
            lead.setLeadId(tempClient.getTempLeadDetails());
            lead.setAppointmentDate(tempClient.getAppointmentDate());
            lead.setAppointmentTime(tempClient.getAppointmentTime());
            lead.setBenCount(tempClient.getBenCount());
            lead.setMargin(tempClient.getMargin());
            lead.setPasson(tempClient.getPasson());
            lead.setHandlingCharges(tempClient.getHandlingCharges());
            lead.setHardcopy(tempClient.getHardcopy());
            lead.setOrderBy(tempClient.getOrderId());
            lead.setPayType(tempClient.getPayType());
            lead.setProduct(tempClient.getCampaignName());
            lead.setRate(lead.getReportCode());
            lead.setServiceType(lead.getServiceType());
            lead.setDateCreation(tempClient.getDateCreation());
            lead.setDateUpdated(lead.getDateUpdated());
            client.getLead().add(lead);
            if(tempClient.getBeneficiaries()!=null&&tempClient.getBeneficiaries().contains(":")&&!tempClient.getBeneficiaries().isEmpty()){
                        String[] benDetailsList=tempClient.getBeneficiaries().split(":");
                        if(benDetailsList.length>0){
                                for(String benDetails:benDetailsList){
                                    System.out.println(benDetails);
                                            Beneficiaries ben= new Beneficiaries();
                                            String[] benBifurcation= benDetails.split("@");
                                            if(benBifurcation.length==3){
                                                ben.setName(benBifurcation[0]);
                                                ben.setGender(benBifurcation[1]);
                                                ben.setAge(benBifurcation[2]);
                                                client.getLead().get(0).getBeneficiaries().add(ben);
                                            }
                                }
                        }
            }
            else{
//                            Beneficiaries ben= new Beneficiaries();
//                            ben.setName(tempClient.getName());
//                            ben.setGender(tempClient.getGender());
//                            ben.setAge(tempClient.getAge());
//                            client.getLead().get(0).getBeneficiaries().add(ben);
            }
             clientList.add(client);
            System.out.println("Client not null");
                if(client.getLead()!=null&&client.getLead().size()>0){
                                System.out.println("Lead not null, leads : "+client.getLead().size());
                    if(client.getLead().get(0)!=null){
                                System.out.println("Lead not null, "+client.getLead().get(0).getLeadId());
                        if(client.getLead().get(0).getBeneficiaries()!=null&&client.getLead().get(0).getBeneficiaries().size()>0){
                                System.out.println("Beneficiaries not null, bens : "+client.getLead().get(0).getBeneficiaries().size());
                            if(client.getLead().get(0).getBeneficiaries().get(0)!=null){
                                
                                System.out.println("Beneficiaries not null, "+client.getLead().get(0).getBeneficiaries().get(0).getName());
                            System.out.println(client.getLead().get(0).getBeneficiaries().get(0).getName()+"::::::::::::::::::::BENTO");
                                
                            }
                        }
                    }
                }
            
        }
        return clientList;
   }
   
     public List<Client> getOvernightClients(){
        System.out.println("*******com.jubination.service.CallMaintainService.getOvernightClients()");
        List<TempClient> tempClientList = clientDao.readClientOvernight();
        List<Client> clientList = new ArrayList<>();
        for(TempClient tempClient:tempClientList){
            Client client = new Client(tempClient.getName(), tempClient.getCampaignName(), tempClient.getAge(), tempClient.getGender(), tempClient.getEmailId(), tempClient.getPhoneNumber(), tempClient.getAddress(), tempClient.getCity(), tempClient.getPincode(), tempClient.getDateCreation(), tempClient.getDateUpdated(), false, tempClient.getTempLeadDetails(), tempClient.getIpAddress(), tempClient.getInitialComments(),tempClient.getSource());
            Lead lead = new Lead();
            lead.setLeadId(tempClient.getTempLeadDetails());
            lead.setAppointmentDate(tempClient.getAppointmentDate());
            lead.setAppointmentTime(tempClient.getAppointmentTime());
            lead.setBenCount(tempClient.getBenCount());
            lead.setMargin(tempClient.getMargin());
            lead.setPasson(tempClient.getPasson());
            lead.setHandlingCharges(tempClient.getHandlingCharges());
            lead.setHardcopy(tempClient.getHardcopy());
            lead.setOrderBy(tempClient.getOrderId());
            lead.setPayType(tempClient.getPayType());
            lead.setProduct(tempClient.getCampaignName());
            lead.setRate(lead.getReportCode());
            lead.setServiceType(lead.getServiceType());
            lead.setDateCreation(tempClient.getDateCreation());
            lead.setDateUpdated(lead.getDateUpdated());
            client.getLead().add(lead);
            if(tempClient.getBeneficiaries()!=null&&tempClient.getBeneficiaries().contains(":")&&!tempClient.getBeneficiaries().isEmpty()){
                        String[] benDetailsList=tempClient.getBeneficiaries().split(":");
                        if(benDetailsList.length>0){
                                for(String benDetails:benDetailsList){
                                    System.out.println(benDetails);
                                            Beneficiaries ben= new Beneficiaries();
                                            String[] benBifurcation= benDetails.split("@");
                                            if(benBifurcation.length==3){
                                                ben.setName(benBifurcation[0]);
                                                ben.setGender(benBifurcation[1]);
                                                ben.setAge(benBifurcation[2]);
                                                client.getLead().get(0).getBeneficiaries().add(ben);
                                            }
                                }
                        }
            }
            else{
//                            Beneficiaries ben= new Beneficiaries();
//                            ben.setName(tempClient.getName());
//                            ben.setGender(tempClient.getGender());
//                            ben.setAge(tempClient.getAge());
//                            client.getLead().get(0).getBeneficiaries().add(ben);
            }
             clientList.add(client);
            System.out.println("Client not null");
                if(client.getLead()!=null&&client.getLead().size()>0){
                                System.out.println("Lead not null, leads : "+client.getLead().size());
                    if(client.getLead().get(0)!=null){
                                System.out.println("Lead not null, "+client.getLead().get(0).getLeadId());
                        if(client.getLead().get(0).getBeneficiaries()!=null&&client.getLead().get(0).getBeneficiaries().size()>0){
                                System.out.println("Beneficiaries not null, bens : "+client.getLead().get(0).getBeneficiaries().size());
                            if(client.getLead().get(0).getBeneficiaries().get(0)!=null){
                                
                                System.out.println("Beneficiaries not null, "+client.getLead().get(0).getBeneficiaries().get(0).getName());
                            System.out.println(client.getLead().get(0).getBeneficiaries().get(0).getName()+"::::::::::::::::::::BENTO");
                                
                            }
                        }
                    }
                }
            
        }
        return clientList;
   }
    
    public Client getClientDetailsWithList(Client client){
        System.out.println("*******com.jubination.service.CallMaintainService.getClientDetailsWithList()");
        return (Client) clientDao.readEntityLists(client);
    }
    
    public Client addClient(Client client){
        System.out.println("*******com.jubination.service.CallMaintainService.addClient()");
        return (Client) clientDao.buildEntity(client);
    }
    



    //------------------------------------------------Make Lead Count Zero----------------------------------//
    // Here Set Lead count to zero and Update the lead into the db.
    public boolean addMissedAppointment(ReportStatus rStatus){
        System.out.println("*******com.jubination.service.CallMaintainService.addMissedAppointment()");
        Lead lead=new Lead();
        lead.setLeadId(rStatus.getLeadId());
        lead=readLead(lead);
        if(lead!=null&&lead.getLeadId().compareTo("46841")>=0&&rStatus.getStatus()!=null&&(rStatus.getStatus().contains("DEFERRED")||rStatus.getStatus().contains("CANCELLED"))){
                if(lead.getCount()<1){
                        lead.setCount(operator.getCount());
                        lead.setLeadStatus("Lead sent to Thyrocare");
                }
                lead.setMissedAppointment(true);
                lead.setMissedAppointmentStatus(rStatus.getStatus());
                updateLeadOnly(lead);
        }
        else if(lead!=null&&rStatus.getStatus()!=null&&rStatus.getStatus()!=null&&(rStatus.getStatus().contains("DONE")||rStatus.getStatus().contains("SERVICED")||rStatus.getStatus().contains("APPOINTMENT")||rStatus.getStatus().contains("REVISED")||rStatus.getStatus().contains("YET TO ASSIGN"))){
                lead.setMissedAppointment(false);
                lead.setCount(0);
                lead.setLeadStatus("Lead sent to Thyrocare");
                lead.setMissedAppointmentStatus(rStatus.getStatus());
                updateLeadOnly(lead);
        }
                return rDao.buildReportStatus(rStatus)!=null;
    }
    
    public boolean addTodaysMissedAppointment(){
        System.out.println("*******com.jubination.service.CallMaintainService.addTodaysMissedAppointment()");    
        List<Lead> leads= (List<Lead>) clientDao.fetchInnerEntities("MissedAppointmentStatusToday", "YET TO ASSIGN");
        for(Lead lead:leads){
            lead.setMissedAppointment(true);
            lead.setMissedAppointmentStatus("Missed Appointment on "+new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            if(lead.getCount()<1){
                    lead.setCount(operator.getCount());
            }
            updateLeadOnly(lead);
        }
        return true;
    }
    /*------------------------------------------------------------------------------------------*/
    
    
    public boolean updateClientOnly(Client client) {
        System.out.println("*******com.jubination.service.CallMaintainService.updateClientOnly()");
        return clientDao.updateProperty(client)!=null;
    }
    
    public boolean updateLeadOfClient(Client client,Lead lead) {
        System.out.println("*******com.jubination.service.CallMaintainService.updateLeadOfClient()");
        return clientDao.updatePropertyList(client,lead,"AddLead");
    }
    
    public List<Lead> readLeadsBySource(String source){
        System.out.println("*******com.jubination.service.CallMaintainService.readLeadsBySource()");                
        return (List<Lead>) clientDao.fetchInnerEntities("ActiveSourceLeads",source);
    }
     
    public Lead readLead(Lead lead) {
        logger.info("Read Lead by object:");
        return (Lead) clientDao.readInnerPropertyList(lead);
    }
    
    public Lead readLead(String leadId) {
        logger.info("Read Lead By Lead Id:");
        return (Lead) clientDao.readInnerPropertyList(new Lead(leadId));
    }
    
    public boolean updateLeadOnly(Lead lead) {
        System.out.println("*******com.jubination.service.CallMaintainService.updateLeadOnly()");
                
        boolean saved=false;
         int  i=0;
        while(!saved&&i<10){
            try{
                System.out.println("Trying to save lead for "+i+"th time ");
                saved=clientDao.updatePropertyOfList(lead,"Lead");

            }
            catch(Exception e){
                saved=false;
                e.printStackTrace();
            }

                   i++;
        }
        return saved;
    }
    
    public Lead updateSavedCallOfLead(Lead lead,Call call) {
        System.out.println("*******com.jubination.service.CallMaintainService.updateSavedCallOfLead()");
        return (Lead) clientDao.updateInnerPropertyList(lead,call,"Call");
    }
    
    public Lead updateSavedBenOfLead(Lead lead,Beneficiaries ben) {
        System.out.println("*******com.jubination.service.CallMaintainService.updateSavedBenOfLead()");
        return (Lead) clientDao.updateInnerPropertyList(lead,ben,"Beneficiaries");
    }
    
    public List<Client> getClientsByEmailId(String email){
        System.out.println("*******com.jubination.service.CallMaintainService.getClientsByEmailId()");
        return (List<Client>) clientDao.getByProperty(email, "Email");
    }
    
    public List<Client> getClientsByPhoneNumber(String number){
        System.out.println("*******com.jubination.service.CallMaintainService.getClientsByPhoneNumber()");
        return (List<Client>) clientDao.getByProperty(number, "Number");
    }
    
    public List<Client> getClientsByName(String name){
        System.out.println("*******com.jubination.service.CallMaintainService.getClientsByName()");                
        return (List<Client>) clientDao.getByProperty(name, "Name");
    }
    
    public List<Client> getClientsByDateCreated(String date){
        System.out.println("*******com.jubination.service.CallMaintainService.getClientsByDateCreated()");
        return (List<Client>) clientDao.getByProperty(date, "DateCreated");
    }   
    
    public List<Client> getClientsByDateUpdated(String date){
        System.out.println("*******com.jubination.service.CallMaintainService.getClientsByDateUpdated()");
        return (List<Client>) clientDao.getByProperty(date, "DateCreated");
    }  
    
    public List<Client> getClientsByCity(String city){
        System.out.println("*******com.jubination.service.CallMaintainService.getClientsByCity()");
        return (List<Client>) clientDao.getByProperty(city, "City");
    }  
    
    public List<Client> getClientsById(long id){
        System.out.println("*******com.jubination.service.CallMaintainService.getClientsById()");
        return (List<Client>) clientDao.getByProperty(id, "Id");
    }  
    
     public List<Client> getClientsByLeadId(String id){
         System.out.println("*******com.jubination.service.CallMaintainService.getClientsByLeadId()");
        return (List<Client>) clientDao.getByProperty(id, "LeadId");
    } 
    
    public List<Client> getPendingCallsWithNotificationAndRecentLead(String param) {
        System.out.println("*******com.jubination.service.CallMaintainService.getPendingCallsWithNotificationAndRecentLead()");
        return (List<Client>)clientDao.fetchEntities(param);
    }

    public List<Client> getAllTemporaryClients() {
        System.out.println("*******com.jubination.service.CallMaintainService.getAllTemporaryClients()");
        return (List<Client>)clientDao.fetchEntities("Overnight");
    }
             
    public Call addCallAPIMessage(Call call){
        System.out.println("*******com.jubination.service.CallMaintainService.addCallAPIMessage()");
        return (Call) callDao.buildEntity(call);
    }

    public Beneficiaries addBeneficiaries(Beneficiaries ben){
        System.out.println("*******com.jubination.service.CallMaintainService.addBeneficiaries()");
        return (Beneficiaries) clientDao.buildBeneficiaryEntity(ben);
    }
     
    public List<Call> getCallBySid(String sid){
        System.out.println("*******com.jubination.service.CallMaintainService.getCallBySid()");
       return (List<Call>) callDao.getByProperty(sid, "Sid");
    }

    public List<Call> getAllCallRecordsByDate(String date) {
        System.out.println("*******com.jubination.service.CallMaintainService.getAllCallRecordsByDate()");
        return (List<Call>) callDao.getByProperty(date, "DateCreated");
    }
      
    public Call getCallRecordBySid(String sid) {
        System.out.println("*******com.jubination.service.CallMaintainService.getCallRecordBySid()");
        return ((List<Call>) callDao.getByProperty(sid, "Sid")).get(0);
    }

    public Call updateCallAPIMessage(Call call) {
        System.out.println("*******com.jubination.service.CallMaintainService.updateCallAPIMessage()");
        return (Call) callDao.updateProperty(call);
    }
    
    public void buildCallAPIMessage(Call call){
        System.out.println("*******com.jubination.service.CallMaintainService.buildCallAPIMessage()");
        callDao.buildEntity(call);
    }
    
    public Object getPendingCallOnDate(String date) {
        System.out.println("*******com.jubination.service.CallMaintainService.getPendingCallOnDate()");
        return (List<Call>) callDao.getByProperty(date, "PendingOnDate");
    }
    
    public List<Lead> readNotifiedLead() {
        System.out.println("*******com.jubination.service.CallMaintainService.readNotifiedLead()");
        List<Lead> list = (List<Lead>) clientDao.fetchInnerEntities("Lead","Pending");
        return list;
    }

    public List<Lead> getDuplicateLeads(String number) {
        System.out.println("*******com.jubination.service.CallMaintainService.getDuplicateLeads()");
        List<Lead> list = (List<Lead>) clientDao.fetchInnerEntities("Number",number);
        return list;
    }
    
    public Lead getClientDetails(String leadId) {
        System.out.println("*******com.jubination.service.CallMaintainService.getClientDetails()");
        Lead lead=new Lead();
        lead.setLeadId(leadId);
        lead=readLead(lead);        
        return lead;
       // return readBackupClient(leadId);
    }
    
    public List<Lead> getLeadDumpForDisplay(String date){
        System.out.println("*******com.jubination.service.CallMaintainService.getLeadDumpForDisplay()");
        return (List<Lead>) clientDao.getByProperty(date,"DateCreatedLeadProperty");
    }
    
    // Here we can make some improvement in excel code
    public boolean createCallExcel(List<Call> list){
        System.out.println("*******com.jubination.service.CallMaintainService.createCallExcel()");
        FileOutputStream out=null;
        HSSFWorkbook workbook =null;
        String excelOutputFilePath=excelOutputDirectory+"data.xls";
         String excelOutputBuildFilePath=excelOutputBuildDirectory+"data.xls";
            boolean flag=false;
            
		try {
            workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Sample sheet");
		
                                      Call[] messageArray = new Call[list.size()];
                                        list.toArray(messageArray);
		Map<String, Object[]> data = new LinkedHashMap<>();
		
                                        Integer index=1;
                                        data.put(index.toString(), new Object[] {
                                                                                "CallFrom","CallTo","Status","TrackStatus",
                                                                                "CallType","DailWhomNumber","DailCallDuration","Message",
                                                                                "DateCreated","AnsweredBy","StartTime","EndTime",
                                                                                "DateUpdated","Duration","Price","Direction",
                                                                                "Digits","Sid","Uri","RecordingUrl","PhoneNumberSid",
                                                                                "AccountSid","ForwardedFrom","CallerName","ParentCallSid"
                                                                                });
                                        index++;
                                        for(Call message:messageArray){
                                                data.put(index.toString(), new Object[] {message.getCallFrom(),message.getCallTo(),message.getStatus(),message.getTrackStatus(),
                                                message.getCallType(),message.getDialWhomNumber(),message.getDialCallDuration(),message.getMessage(),
                                                message.getDateCreated(),message.getAnsweredBy(),message.getStartTime(),message.getEndTime(),
                                                message.getDateUpdated(),message.getDuration(),message.getPrice(),message.getDirection(),
                                                message.getDigits(),message.getSid(),message.getUri(),message.getRecordingUrl(),message.getPhoneNumberSid(),
                                                message.getAccountSid(),message.getForwardedFrom(),message.getCallerName(),message.getParentCallSid()});
                                                index++;
                                        
                                        }
                                        
                                        
		Set<String> keyset = data.keySet();
		int rownum = 0;
		for (String key : keyset) {
			Row row = sheet.createRow(rownum++);
			Object [] objArr = data.get(key);
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if(obj instanceof Date) 
					cell.setCellValue((Date)obj);
				else if(obj instanceof Boolean)
					cell.setCellValue((Boolean)obj);
				else if(obj instanceof String)
					cell.setCellValue((String)obj);
				else if(obj instanceof Double)
					cell.setCellValue((Double)obj);
			}
		}
		
                out = new FileOutputStream(new File(excelOutputFilePath));
                workbook.write(out);
                                                            
                out = new FileOutputStream(new File(excelOutputBuildFilePath));
                workbook.write(out);
                                 
                flag=true;
                System.out.println("Excel written successfully..");
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
                finally{
                    try{
                        if(workbook!=null){
                            workbook.close();
		
                        }
                    }
                    catch(Exception e){
                } try{
                        if(out!=null){
		                   out.close();         

                        }
                    }
                    catch(Exception e){
                }
                }
                return flag;	
    }

    public boolean createClientExcel(String date){
        System.out.println("*******com.jubination.service.CallMaintainService.createClientExcel()");
        FileOutputStream out=null;
        HSSFWorkbook workbook =null;
        
        String excelOutputFilePath=excelOutputDirectory+"client.xls";
         String excelOutputBuildFilePath=excelOutputBuildDirectory+"client.xls";
            boolean flag=false;
            
		try {
                                        workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Client Sheet");
		
                
                                        Map<String,Object[]> data=doReportingOperation(getClientDumpForDisplay(date));
                
		Set<String> keyset = data.keySet();
		int rownum = 0;
		for (String key : keyset) {
			Row row = sheet.createRow(rownum++);
			Object [] objArr = data.get(key);
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if(obj instanceof Date) 
					cell.setCellValue((Date)obj);
				else if(obj instanceof Boolean)
					cell.setCellValue((Boolean)obj);
				else if(obj instanceof String)
					cell.setCellValue((String)obj);
				else if(obj instanceof Double)
					cell.setCellValue((Double)obj);
			}
		}
		
			out = new FileOutputStream(new File(excelOutputFilePath));
			workbook.write(out);
                                                            
                                                            out = new FileOutputStream(new File(excelOutputBuildFilePath));
			workbook.write(out);
                                                           flag=true;
			System.out.println("Excel written successfully..");
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
                finally{
                    try{
                        if(workbook!=null){
                                                       workbook.close();
		
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                } try{
                        if(out!=null){
		                   out.close();         

                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                }
                }
                return flag;	
    }

    public Map<String, Object[]> doReportingOperation(List<Client> list){
        System.out.println("*******com.jubination.service.CallMaintainService.doReportingOperation()");
                                        
                                      Client[] messageArray = new Client[list.size()];
                                       list.toArray(messageArray);
		Map<String, Object[]> data = new LinkedHashMap<>();
                                        Integer index=1;
                                        data.put(index.toString(), new Object[] {
                                                 "Lead id","Name","Number","Email","Campaign Name","Pub Id","Source","Date","City","Affiliate Status","Picked up by","Follow ups left","Client comment","Lead comment","Follow up date",
                                                "Status","Date","Status-1","Date-1"," Status-2","Date-2"," Status-3","Date-3"," Status-4","Date-4"," Status-5","Date-5"," Status-6","Date-6"," Status-7","Date-7",
                                                " Status-8","Date-8"," Status-9","Date-9"," Status-10","Date-10"," Status-11","Date-11"," Status-12","Date-12"," Status-13","Date-13"," Status-14","Date-14"," Status-15","Date-15","","Final Status Beta"});
                                        index++;
                                        for(Client client:messageArray){
                                            String[] leadDetailsArray= new String[]{"","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""};
                                            String[] dateDetailsArray= new String[]{"","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""};
                                            String affiliateDetails= "WIP";
                                            
                                            for(int i=0;i<20;i++){
                                                dateDetailsArray[i]="";
                                                leadDetailsArray[i]="";
                                            }
                                            int count=0;
                                            String caller="";
                                            
                                            if(client!=null&&client.getLead()!=null&&client.getLead().size()>=1){
                                                Lead lead = client.getLead().get(client.getLead().size()-1);
                                                if(lead.getCall().size()>0){     
                                                                for(int i=lead.getCall().size()-1;i>=0;i--){
                                                                    if(count<15){
                                                                                Call call=lead.getCall().get(i);
                                                                                if(call==null){
                                                                                    break;
                                                                                }
                                                                                System.out.println(count+" "+i+" "+call.getDateCreated());
                                                                                ////////change to allow all lead sent to thyrocare leads///////////
                                                                                                             
                                                                                ////////////////////////
                                                                                                            if(call.getStatus()!=null&&call.getStatus().contains("busy")){
                                                                                                                leadDetailsArray[count]="Busy";
                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                    affiliateDetails="Disconnecting the call";
                                                                                                                }
                                                                                                            }
                                                                                                            else if(call.getStatus()!=null&&call.getStatus().contains("failed")){
                                                                                                                leadDetailsArray[count]="Failed";
                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                    affiliateDetails="Not Reachable";
                                                                                                                }
                                                                                                            }
                                                                                                            else if(call.getStatus()!=null&&call.getStatus().contains("no-answer")){
                                                                                                                leadDetailsArray[count]="No Answer";
                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                    affiliateDetails="Ringing";
                                                                                                                }
                                                                                                            }
                                                                                                            else if(call.getStatus()!=null&&call.getStatus().contains("completed")&&call.getCallType().contains("trans")){
                                                                                                               leadDetailsArray[count]="Hanged up while greetings";
                                                                                                               dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                               if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                    affiliateDetails="Disconnecting the call";
                                                                                                                }
                                                                                                            }
                                                                                                            else if(call.getTrackStatus()!=null&&call.getTrackStatus().contains("did not speak")&&call.getCallType().contains("client-hangup")){
                                                                                                                leadDetailsArray[count]="Hanged up while connecting";
                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                      affiliateDetails="Disconnecting the call";
                                                                                                                  }
                                                                                                            }
                                                                                                             
                                                                                                            else if(call.getTrackStatus()!=null&&call.getTrackStatus().contains("did not speak")&&call.getCallType().contains("incomplete")){
                                                                                                                leadDetailsArray[count]="We missed client's call";
                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                                              affiliateDetails="WIP";
                                                                                                                                          }
                                                                                                                caller=call.getDialWhomNumber();
                                                                                                            }
                                                                                                            else if(call.getTrackStatus()!=null&&call.getTrackStatus().contains("spoke")){
                                                                                                                if(lead.getLeadStatus()!=null&&(lead.getLeadStatus().contains("Follow up/Call back")||
                                                                                                                     lead.getLeadStatus().contains("Lead sent to Thyrocare")||
                                                                                                                        lead.getLeadStatus().contains("Not interested")||
                                                                                                                        lead.getLeadStatus().contains("Not registered")||
                                                                                                                        lead.getLeadStatus().contains("Language not recognizable")||
                                                                                                                        lead.getLeadStatus().contains("No Service")||
                                                                                                                        lead.getLeadStatus().contains("Customer complained")||
                                                                                                                        lead.getLeadStatus().contains("Disapproved")||
                                                                                                                        lead.getLeadStatus().contains("Rescheduled")
                                                                                                                        )){
                                                                                                                                             leadDetailsArray[count]=lead.getLeadStatus();
                                                                                                                                             if(lead.getLeadStatus().contains("Lead sent to Thyrocare")||lead.getLeadStatus().contains("Rescheduled")){
                                                                                                                                                     affiliateDetails="Interested";
                                                                                                                                             }
                                                                                                                                             else{
                                                                                                                                                     affiliateDetails=lead.getLeadStatus();
                                                                                                                                             } 
                                                                                                                                             dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                 }
                                                                                                                else{

                                                                                                                           if(i==lead.getCall().size()-1){
                                                                                                                                     leadDetailsArray[count]="Spoke but not updated";
                                                                                                                                      dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                                      affiliateDetails="Spoke but not updated";
                                                                                                                           }
                                                                                                                           else{
                                                                                                                                     leadDetailsArray[count]=lead.getLeadStatus()+":";
                                                                                                                                      dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                           }
                                                                                                                 }
                                                                                                                  caller=call.getDialWhomNumber();

                                                                                                            }
                                                                                                        else{
                                                                                                            
                                                                                                            if(i==lead.getCall().size()-1){
                                                                                                                    if(lead.getLeadStatus()!=null){
                                                                                                                                caller=call.getDialWhomNumber();
                                                                                                                                if(lead.getLeadStatus()!=null&&(lead.getLeadStatus().contains("Follow up/Call back")||
                                                                                                                                           lead.getLeadStatus().contains("Not interested")||
                                                                                                                                           lead.getLeadStatus().contains("Not registered")||
                                                                                                                                           lead.getLeadStatus().contains("Language not recognizable")||
                                                                                                                                           lead.getLeadStatus().contains("No Service")||
                                                                                                                                           lead.getLeadStatus().contains("Customer complained")||
                                                                                                                                           lead.getLeadStatus().contains("Disapproved")
                                                                                                                                           )){

                                                                                                                                        affiliateDetails=lead.getLeadStatus();

                                                                                                                                }
                                                                                                                                else if( lead.getLeadStatus().contains("Lead sent to Thyrocare")||lead.getLeadStatus().contains("Rescheduled")){

                                                                                                                                  affiliateDetails="Interested";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("Busy")){
                                                                                                                                    affiliateDetails="Disconnecting the call";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("Failed")){
                                                                                                                                    affiliateDetails="Not Reachable";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("No Answer")){
                                                                                                                                     affiliateDetails="Ringing";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("Hanged up while greetings")){
                                                                                                                                    affiliateDetails="Disconnecting the call";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("Hanged up while connecting")){
                                                                                                                                    affiliateDetails="Disconnecting the call";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("Spoke but not updated")){
                                                                                                                                    affiliateDetails="Spoke but not updated";
                                                                                                                                }
                                                                                                                                else{
                                                                                                                                        affiliateDetails=lead.getLeadStatus();
                                                                                                                                }

                                                                                                                                leadDetailsArray[count]=lead.getLeadStatus();
                                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                    }
                                                                                                                    else{
                                                                                                                                leadDetailsArray[count]=call.getStatus()+"%";
                                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                                caller=call.getDialWhomNumber();
                                                                                                                                 affiliateDetails=lead.getLeadStatus();
                                                                                                                    }

                                                                                                            }
                                                                                                            else{
                                                                                                                            leadDetailsArray[count]=lead.getLeadStatus()+"$";
                                                                                                                            dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                            }
                                                                                                                                            
                                                                                                                                            
                                                                                                        }
                                                                                                if(lead.getLeadStatus()!=null&&(lead.getLeadStatus().contains("Lead sent to Thyrocare")||lead.getLeadStatus().contains("Rescheduled"))){
                                                                                                                                        affiliateDetails="Interested";
                                                                                                                                }
                                                                                                 if(lead.getFollowUpDate()!=null&&!affiliateDetails.contains("")&&!lead.getLeadStatus().contains("Follow up/Call back")&&
                                                                                                                                           !lead.getLeadStatus().contains("Not interested")&&
                                                                                                                                           !lead.getLeadStatus().contains("Not registered")&&
                                                                                                                                           !lead.getLeadStatus().contains("Language not recognizable")&&
                                                                                                                                           !lead.getLeadStatus().contains("No Service")&&
                                                                                                                                           !lead.getLeadStatus().contains("Customer complained")&&
                                                                                                                                           !lead.getLeadStatus().contains("Disapproved")){
                                                                                                                                        affiliateDetails="Follow up/Call back";
                                                                                                                                }
                                                                                 count++;
                                                                }

                                                                }
                                                                
                                            }
                                                data.put(index.toString(), new Object[] {lead.getLeadId(),client.getName(),client.getPhoneNumber(),client.getEmailId(),client.getCampaignName(),client.getPubId(),client.getSource(),client.getDateCreation(),client.getCity(),affiliateDetails,caller,Integer.toString(lead.getCount()),client.getInitialComments(),lead.getComments(),lead.getFollowUpDate(),
                                                leadDetailsArray[0],dateDetailsArray[0],leadDetailsArray[1],dateDetailsArray[1],leadDetailsArray[2],dateDetailsArray[2],leadDetailsArray[3],dateDetailsArray[3],leadDetailsArray[4],dateDetailsArray[4],leadDetailsArray[5],dateDetailsArray[5],leadDetailsArray[6],dateDetailsArray[6],leadDetailsArray[7],dateDetailsArray[7],
                                                leadDetailsArray[8],dateDetailsArray[8],leadDetailsArray[9],dateDetailsArray[9],leadDetailsArray[10],dateDetailsArray[10],leadDetailsArray[11],dateDetailsArray[11],leadDetailsArray[12],dateDetailsArray[12],leadDetailsArray[13],dateDetailsArray[13],leadDetailsArray[14],dateDetailsArray[14],leadDetailsArray[15],dateDetailsArray[15],"",lead.getLeadStatus()});
                                                index++;
                                                client=null;
                                        }
                                            
                                        }
                                        return data;
    }
    
    public boolean createClientExcelAllLead(String date){
        System.out.println("*******com.jubination.service.CallMaintainService.createClientExcelAllLead()");
        FileOutputStream out=null;
        HSSFWorkbook workbook =null;
        
        String excelOutputFilePath=excelOutputDirectory+"client.xls";
        String excelOutputBuildFilePath=excelOutputBuildDirectory+"client.xls";
        boolean flag=false;
            
        try {
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Client Sheet");
		
                
                                        List<Lead> list=getLeadDumpForDisplay(date);
                
                                          
                
                                      Lead[] messageArray = new Lead[list.size()];
                                        list.toArray(messageArray);
		Map<String, Object[]> data = new LinkedHashMap<>();
		
                                        Integer index=1;
                                        data.put(index.toString(), new Object[] {
                                                 "Lead id","Name","Number","Email","Campaign Name","Pub Id","Source","Date","City","Affiliate Status","Picked up by","Follow ups left","Client comment","Lead comment","Follow up date",
                                                "Status","Date","Status-1","Date-1"," Status-2","Date-2"," Status-3","Date-3"," Status-4","Date-4"," Status-5","Date-5"," Status-6","Date-6"," Status-7","Date-7",
                                                " Status-8","Date-8"," Status-9","Date-9"," Status-10","Date-10"," Status-11","Date-11"," Status-12","Date-12"," Status-13","Date-13"," Status-14","Date-14"," Status-15","Date-15","","Final Status Beta"});
                                        index++;
                                        for(Lead lead:messageArray){
                                            String[] leadDetailsArray= new String[]{"","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""};
                                            String[] dateDetailsArray= new String[]{"","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""};
                                            String affiliateDetails= "WIP";
                                            
                                            for(int i=0;i<20;i++){
                                                dateDetailsArray[i]="";
                                                leadDetailsArray[i]="";
                                            }
                                            int count=0;
                                            String caller="";
                                            if(lead!=null){
                                                if(lead.getCall().size()>0){     
                                                                for(int i=lead.getCall().size()-1;i>=0;i--){
                                                                    if(count<15){
                                                                                Call call=lead.getCall().get(i);
                                                                                if(call==null){
                                                                                    break;
                                                                                }
                                                                                System.out.println(count+" "+i+" "+call.getDateCreated());
                                                                                ////////change to allow all lead sent to thyrocare leads///////////
                                                                                                             
                                                                                ////////////////////////
                                                                                                            if(call.getStatus()!=null&&call.getStatus().contains("busy")){
                                                                                                                leadDetailsArray[count]="Busy";
                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                    affiliateDetails="Disconnecting the call";
                                                                                                                }
                                                                                                            }
                                                                                                            else if(call.getStatus()!=null&&call.getStatus().contains("failed")){
                                                                                                                leadDetailsArray[count]="Failed";
                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                    affiliateDetails="Not Reachable";
                                                                                                                }
                                                                                                            }
                                                                                                            else if(call.getStatus()!=null&&call.getStatus().contains("no-answer")){
                                                                                                                leadDetailsArray[count]="No Answer";
                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                    affiliateDetails="Ringing";
                                                                                                                }
                                                                                                            }
                                                                                                            else if(call.getStatus()!=null&&call.getStatus().contains("completed")&&call.getCallType().contains("trans")){
                                                                                                               leadDetailsArray[count]="Hanged up while greetings";
                                                                                                               dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                               if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                    affiliateDetails="Disconnecting the call";
                                                                                                                }
                                                                                                            }
                                                                                                            else if(call.getTrackStatus()!=null&&call.getTrackStatus().contains("did not speak")&&call.getCallType().contains("client-hangup")){
                                                                                                                leadDetailsArray[count]="Hanged up while connecting";
                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                      affiliateDetails="Disconnecting the call";
                                                                                                                  }
                                                                                                            }
                                                                                                             
                                                                                                            else if(call.getTrackStatus()!=null&&call.getTrackStatus().contains("did not speak")&&call.getCallType().contains("incomplete")){
                                                                                                                leadDetailsArray[count]="We missed client's call";
                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                if(lead.getCall().size()>=operator.getCount()-1&&i==lead.getCall().size()-1){
                                                                                                                                              affiliateDetails="WIP";
                                                                                                                                          }
                                                                                                                caller=call.getDialWhomNumber();
                                                                                                            }
                                                                                                        else if(call.getTrackStatus()!=null&&call.getTrackStatus().contains("spoke")){
                                                                                                                if(lead.getLeadStatus()!=null&&(lead.getLeadStatus().contains("Follow up/Call back")||
                                                                                                                     lead.getLeadStatus().contains("Lead sent to Thyrocare")||
                                                                                                                        lead.getLeadStatus().contains("Not interested")||
                                                                                                                        lead.getLeadStatus().contains("Not registered")||
                                                                                                                        lead.getLeadStatus().contains("Language not recognizable")||
                                                                                                                        lead.getLeadStatus().contains("No Service")||
                                                                                                                        lead.getLeadStatus().contains("Customer complained")||
                                                                                                                        lead.getLeadStatus().contains("Disapproved")||
                                                                                                                        lead.getLeadStatus().contains("Rescheduled")
                                                                                                                        )){
                                                                                                                                             leadDetailsArray[count]=lead.getLeadStatus();
                                                                                                                                             if(lead.getLeadStatus().contains("Lead sent to Thyrocare")||
                                                                                                                                                                lead.getLeadStatus().contains("Rescheduled")){
                                                                                                                                                 affiliateDetails="Interested";
                                                                                                                                                
                                                                                                                                             }
                                                                                                                                             else{
                                                                                                                                                     affiliateDetails=lead.getLeadStatus();
                                                                                                                                             } 
                                                                                                                                             dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                 }
                                                                                                                else{

                                                                                                                           if(i==lead.getCall().size()-1){
                                                                                                                                     leadDetailsArray[count]="Spoke but not updated";
                                                                                                                                      dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                                      affiliateDetails="Spoke but not updated";
                                                                                                                           }
                                                                                                                           else{
                                                                                                                                     leadDetailsArray[count]=lead.getLeadStatus()+":";
                                                                                                                                      dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                           }
                                                                                                                 }
                                                                                                                  caller=call.getDialWhomNumber();

                                                                                                            }
                                                                                                        else{
                                                                                                            
                                                                                                            if(i==lead.getCall().size()-1){
                                                                                                                    if(lead.getLeadStatus()!=null){
                                                                                                                                caller=call.getDialWhomNumber();
                                                                                                                                if(lead.getLeadStatus()!=null&&(lead.getLeadStatus().contains("Follow up/Call back")||
                                                                                                                                           lead.getLeadStatus().contains("Not interested")||
                                                                                                                                           lead.getLeadStatus().contains("Not registered")||
                                                                                                                                           lead.getLeadStatus().contains("Language not recognizable")||
                                                                                                                                           lead.getLeadStatus().contains("No Service")||
                                                                                                                                           lead.getLeadStatus().contains("Customer complained")||
                                                                                                                                           lead.getLeadStatus().contains("Disapproved")
                                                                                                                                           )){

                                                                                                                                        affiliateDetails=lead.getLeadStatus();

                                                                                                                                }
                                                                                                                                else if( lead.getLeadStatus().contains("Lead sent to Thyrocare")||
                                                                                                                                             lead.getLeadStatus().contains("Rescheduled")){

                                                                                                                                  affiliateDetails="Interested";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("Busy")){
                                                                                                                                    affiliateDetails="Disconnecting the call";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("Failed")){
                                                                                                                                    affiliateDetails="Not Reachable";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("No Answer")){
                                                                                                                                     affiliateDetails="Ringing";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("Hanged up while greetings")){
                                                                                                                                    affiliateDetails="Disconnecting the call";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("Hanged up while connecting")){
                                                                                                                                    affiliateDetails="Disconnecting the call";
                                                                                                                                }
                                                                                                                                else if(lead.getLeadStatus().contains("Spoke but not updated")){
                                                                                                                                    affiliateDetails="Spoke but not updated";
                                                                                                                                }
                                                                                                                                else{
                                                                                                                                    affiliateDetails=lead.getLeadStatus();
                                                                                                                                }

                                                                                                                                leadDetailsArray[count]=lead.getLeadStatus();
                                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                    }
                                                                                                                    else{
                                                                                                                                leadDetailsArray[count]=call.getStatus()+"%";
                                                                                                                                dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                                                caller=call.getDialWhomNumber();
                                                                                                                                 affiliateDetails=lead.getLeadStatus();
                                                                                                                    }

                                                                                                            }
                                                                                                            else{
                                                                                                                            leadDetailsArray[count]=lead.getLeadStatus()+"$";
                                                                                                                            dateDetailsArray[count]=call.getDateCreated()+" "+call.getDuration();
                                                                                                            }
                                                                                                                                            
                                                                                                                                            
                                                                                                        }
                                                                                                if(lead.getLeadStatus()!=null&&(lead.getLeadStatus().contains("Lead sent to Thyrocare")||lead.getLeadStatus().contains("Rescheduled"))){
                                                                                                                                        affiliateDetails="Interested";
                                                                                                                                }
                                                                               
                                                                                                      if(lead.getFollowUpDate()!=null&&!affiliateDetails.contains("")&&!lead.getLeadStatus().contains("Follow up/Call back")&&
                                                                                                                                           !lead.getLeadStatus().contains("Not interested")&&
                                                                                                                                           !lead.getLeadStatus().contains("Not registered")&&
                                                                                                                                           !lead.getLeadStatus().contains("Language not recognizable")&&
                                                                                                                                           !lead.getLeadStatus().contains("No Service")&&
                                                                                                                                           !lead.getLeadStatus().contains("Customer complained")&&
                                                                                                                                           !lead.getLeadStatus().contains("Disapproved")){
                                                                                                                                        affiliateDetails="Follow up/Call back";
                                                                                                                                }
                                                                                                      
                                                                                                                                if(lead.isMissedAppointment()!=null&&lead.isMissedAppointment()&&lead.getCount()<1){
                                                                                                                                        affiliateDetails="Missed Appointment";
                                                                                                                                }
                                                                               

                                                                                count++;
                                                                }

                                                                }
                                                                
                                            }
                                                data.put(index.toString(), new Object[] {lead.getLeadId(),lead.getClient().getName(),lead.getClient().getPhoneNumber(),lead.getClient().getEmailId(),lead.getClient().getCampaignName(),lead.getClient().getPubId(),lead.getClient().getSource(),lead.getClient().getDateCreation(),lead.getClient().getCity(),affiliateDetails,caller,Integer.toString(lead.getCount()),lead.getClient().getInitialComments(),lead.getComments(),lead.getFollowUpDate(),
                                                leadDetailsArray[0],dateDetailsArray[0],leadDetailsArray[1],dateDetailsArray[1],leadDetailsArray[2],dateDetailsArray[2],leadDetailsArray[3],dateDetailsArray[3],leadDetailsArray[4],dateDetailsArray[4],leadDetailsArray[5],dateDetailsArray[5],leadDetailsArray[6],dateDetailsArray[6],leadDetailsArray[7],dateDetailsArray[7],
                                                leadDetailsArray[8],dateDetailsArray[8],leadDetailsArray[9],dateDetailsArray[9],leadDetailsArray[10],dateDetailsArray[10],leadDetailsArray[11],dateDetailsArray[11],leadDetailsArray[12],dateDetailsArray[12],leadDetailsArray[13],dateDetailsArray[13],leadDetailsArray[14],dateDetailsArray[14],leadDetailsArray[15],dateDetailsArray[15],"",lead.getLeadStatus()});
                                                index++;
                                        lead=null;
                                        }
                                            
                                        }
                                        
                                        
                                     list=null;
		Set<String> keyset = data.keySet();
		int rownum = 0;
		for (String key : keyset) {
			Row row = sheet.createRow(rownum++);
			Object [] objArr = data.get(key);
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if(obj instanceof Date) 
					cell.setCellValue((Date)obj);
				else if(obj instanceof Boolean)
					cell.setCellValue((Boolean)obj);
				else if(obj instanceof String)
					cell.setCellValue((String)obj);
				else if(obj instanceof Double)
					cell.setCellValue((Double)obj);
			}
		}
		
			out = new FileOutputStream(new File(excelOutputFilePath));
			workbook.write(out);
                                                            
                                                            out = new FileOutputStream(new File(excelOutputBuildFilePath));
			workbook.write(out);
                                                           flag=true;
			System.out.println("Excel written successfully..");
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
                finally{
                    try{
                        if(workbook!=null){
                                                       workbook.close();
		
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                } try{
                        if(out!=null){
		                   out.close();         

                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                }
                }
                return flag;	
    }

    public void checkFollowUp() {
        System.out.println("*******com.jubination.service.CallMaintainService.checkFollowUp()");
        
                            System.out.println("follow up ");
                                long pending=0l;
                                long notification=0l;
                                long pendingMa=0l;
                                long notificationMa=0l;
                                long pendingFresh=0l;
                                long pendingMinusOne=0l;
                                long inProgress=0l;
                                
                               List<Client> list=getPendingCallsWithNotificationAndRecentLead("Pending");
                               if(list!=null){
                                    for(Client client:list){
                                        client.setPriority(4); 
                                                    Lead lead=client.getLead().get(client.getLead().size()-1);
                                                    if(lead.getCount()!=(operator.getCount()-lead.getCall().size())){
                                                                lead.setCount(operator.getCount()-lead.getCall().size());
                                                                updateLeadOnly(lead);
                                                    }
                                                    
                                                     if(lead.getCount()>0&&lead.getLeadStatus()!=null&&(!lead.getLeadStatus().contains("Follow up/Call back")&&
                                                        !lead.getLeadStatus().contains("Not interested")&&
                                                        !lead.getLeadStatus().contains("Not registered")&&
                                                        !lead.getLeadStatus().contains("Language not recognizable")&&
                                                        !lead.getLeadStatus().contains("No Service")&&
                                                        !lead.getLeadStatus().contains("Customer complained")&&
                                                        !lead.getLeadStatus().contains("Disapproved")
                                                        )){
                                                         
                                                            pending++;
                                                    }
                                                     else{
                                                                lead.setCount(0);
                                                          //      updateLeadOnly(lead);
                                                     }
                                        
                                        
                                    }
                               }
                               list=getPendingCallsWithNotificationAndRecentLead("Notified");
                               if(list!=null){
                                 for(Client client:list){
                                    client.setPriority(4);    
                                     Lead lead=client.getLead().get(client.getLead().size()-1);
                                    if(lead.getCall().size()>=operator.getCount()+4){
                                                lead.setCount(0);
                                               // updateLeadOnly(lead);
                                    }
                                    if(lead.getCount()>0){
                                        
                                             notification++;
                                    }
                                   
                                }
                               }
                               list=getPendingCallsWithNotificationAndRecentLead("PendingMA");
                               if(list!=null){
                                for(Client client:list){
                                    client.setPriority(4);         
                                    Lead lead=client.getLead().get(client.getLead().size()-1);
                                    if(lead.getCall().size()>=operator.getCount()+4){
                                                lead.setCount(0);
                                            //    updateLeadOnly(lead);
                                    }
                                    if(lead.getCount()>0){
                                            pendingMa++;
                                    }
                                    
                                }
                               }
                               list=getPendingCallsWithNotificationAndRecentLead("NotifiedMA");
                               if(list!=null){
                                 for(Client client:list){
                                    client.setPriority(4);         
                                    Lead lead=client.getLead().get(client.getLead().size()-1);
                                    if(lead.getCall().size()>=operator.getCount()+4){
                                                lead.setCount(0);
                                         //       updateLeadOnly(lead);
                                    }
                                    if(lead.getCount()>0){
                                        
                                             notificationMa++;
                                    }
                                   
                                }
                               }
                                 list=getPendingCallsWithNotificationAndRecentLead("PendingMinusOne");
                               if(list!=null){
                                 for(Client client:list){
                                    client.setPriority(4);         
                                    Lead lead=client.getLead().get(client.getLead().size()-1);
                                    if(lead.getCall().size()<=operator.getCount()&&lead.getCount()<0){
                                             lead.setCount(operator.getCount()-lead.getCall().size());
                                            updateLeadOnly(lead);
                                             pendingMinusOne++;
                                    }
                                   
                                }
                               }
                               
                               list=getPendingCallsWithNotificationAndRecentLead("PendingInProgress");
                               if(list!=null){
                                 for(Client client:list){
                                    client.setPriority(4);         
                                    Lead lead=client.getLead().get(client.getLead().size()-1);
                                    if(lead.getCall().size()<=operator.getCount()&&lead.getCount()<0){
                                             lead.setCount(operator.getCount()-lead.getCall().size());
                                           updateLeadOnly(lead);
                                             inProgress++;
                                    }
                                   
                                }
                               }
                               
                               //miss call added as well
                               System.out.println("miss call check started");
                                for(Client client:getMarkedClients()){
                                        pendingFresh++;
                                }
                           
                                sendEmailFollowupUpdate("souvik@jubination.com","Fresh Followup : "+pending+", Fresh Callback : "+notification+"Followup Missed Appointment : "+pendingMa+" CallBack Missed Appointment : "+notificationMa+" Fresh Call pending : "+pendingFresh+" Minus One pending : "+pendingMinusOne+" In progress leads : "+inProgress);
                             
                              
    }
    
     private void sendEmailFollowupUpdate(String email,String content){
         System.out.println("*******com.jubination.service.CallMaintainService.sendEmailFollowupUpdate()");
           AdminSettings adminSettings = adminService.readSettings(settings);
            new EmailService(email,"Follow up started",
                                          "Hi,<br/>" +
                                                "<br/>" +
                                                "I am call Bot!<br/>" +
                                                "<br/>" +
                                                "Follow u  has started" +
                                                "<br/>" +
                                                "<br/>" +
                                                content+
                                                "<br/>" +
                                                "<br/>" +
                                                "Regards,<br/>" + 
                                                "Call Bot ",adminSettings.getMyUsername(),adminSettings.getMyPassword(),adminSettings.getAuth(),adminSettings.getStarttls(),adminSettings.getHost(),adminSettings.getPort(),adminSettings.getSendgridApi()).start();
     }

    public String getErrorFlag() {
        System.out.println("*******com.jubination.service.CallMaintainService.getErrorFlag()");
        return errorFlag;
    }

    public void setErrorFlag(String errorFlag) {
        System.out.println("*******com.jubination.service.CallMaintainService.setErrorFlag()");
        this.errorFlag = errorFlag;
    }    
}
