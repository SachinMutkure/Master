package com.jubination.backend.service.exotel.numbercall.serial;


import com.jubination.backend.pojo.exotel.ExotelMessage;
import com.jubination.model.pojo.exotel.Call;
import com.jubination.service.AdminMaintainService;
import com.jubination.service.CallMaintainService;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Welcome
 */
@Component
public class CallBox {
    private static final Logger logger = LoggerFactory.getLogger(CallBox.class);
    private boolean flag=false;
    private boolean checkFlag=false;
    private int count = 0;
    private Stack<String> numbers = new Stack<>();
    private ConcurrentLinkedQueue<String> sids = new ConcurrentLinkedQueue<>();
    private Stack<Call> stageThreeUpdates = new Stack<>();

    private int executives=0;
    private String appId;
    private String callerId;                   
                    

    @Autowired
    AdminMaintainService adminMaintain;
    @Autowired
    CallMaintainService callMaintain;
    @Async
    @Scheduled(fixedRate=2500)
    void callDicyCustomer() throws IOException,InterruptedException, JAXBException{
        logger.info("Inside callDicyCustomer() Method:");        
        Long startTime=System.currentTimeMillis();        
        //logger.info("Start Time :"+startTime);
        //logger.info("Number : "+numbers+" sid size" +sids.size()+" Executive"+getExecutives());
                if(!numbers.isEmpty()&&sids.size()<getExecutives()){                    
                        
                        String callerId=numbers.peek();
                        logger.info("Caller Id :"+callerId);
                        if(numbers.isEmpty()){
                                        flag=false;
                                    }




                                                    String responseText="NA";
                                                    Document doc=null;
                                                    //System.out.println("Stage 1:"+numbers.size()+" Sids in queue to be sent to exotel to process "+Thread.currentThread());
                                                    logger.info("Stage 1:"+numbers.size()+" Sids in queue to be sent to exotel to process "+Thread.currentThread());
                                                    if(callerId!=null){
                                                        
                                                                            CloseableHttpResponse response=null;
                                                                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                                                            DocumentBuilder builder;
                                                                            InputSource is;
                                                                            try { 
                                                                                System.out.println("Inside the Try:");
                                                                                                        //requesting exotel to initiate call
                                                                                                        CloseableHttpClient httpclient = HttpClients.createDefault();
                                                                                                        HttpPost httpPost = new HttpPost("https://jubination:ce5e307d58d8ec07c8d8456e42ed171ff8322fd0@twilix.exotel.in/v1/Accounts/jubination/Calls/connect");
                                                                                                        logger.info("HTTP Post :"+httpPost);
                                                                                                        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                                                                                                        formparams.add(new BasicNameValuePair("From",callerId));
                                                                                                        //formparams.add(new BasicNameValuePair("To",callerId));
                                                                                                        formparams.add(new BasicNameValuePair("CallerId",getCallerId()));
                                                                                                        formparams.add(new BasicNameValuePair("CallerType","trans"));
                                                                                                        formparams.add(new BasicNameValuePair("Url","http://my.exotel.in/exoml/start/"+"102261"));
                                                                                                //      formparams.add(new BasicNameValuePair("TimeLimit","1800"));
                                                                                                  //    formparams.add(new BasicNameValuePair("TimeOut","30"));
                                                                                                        formparams.add(new BasicNameValuePair("SatusCallback",""));
                                                                                                        formparams.add(new BasicNameValuePair("CustomField","customCall"));
                                                                                                        UrlEncodedFormEntity uEntity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
                                                                                                        httpPost.setEntity(uEntity);
                                                                                                        response = httpclient.execute(httpPost);
                                                                                                        logger.info("Value of response :"+response);
                                                                                                        //System.out.println("Stage 1:Number sent to exotel");
                                                                                                        HttpEntity entity = response.getEntity();

                                                                                                        responseText = EntityUtils.toString(entity, "UTF-8");
                                                                                                        builder = factory.newDocumentBuilder();
                                                                                                        is = new InputSource(new StringReader(responseText));
                                                                                                        doc = builder.parse(is);
                                                                                                        logger.info("Value of doc :"+doc);
                                                                                                        doc.getDocumentElement().normalize();
                                                                                                        
                                                                            } 
                                                                            catch(IOException | ParseException | ParserConfigurationException | SAXException | DOMException e){
                                                                                e.printStackTrace();
                                                                                logger.error("Error In callDicyCustomer() Method:" +e.getMessage());
                                                                            }
                                                                            finally {
                                                                                logger.info("Fynally Block");
                                                                                        if(response!=null){
                                                                                                response.close();
                                                                                        }
                                                                            }
                                                    }
                                                    //parsing xml response from exotel
                                                    JAXBContext jaxbContext = JAXBContext.newInstance(ExotelMessage.class);
                                                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                                                    ExotelMessage eMessage = (ExotelMessage) jaxbUnmarshaller.unmarshal(doc);
                                                    logger.info("Exotel Message: "+eMessage);
                                                    Call message=null;
                                                    //System.out.println("Stage 1:Got xml message");
                                                    logger.info("Stage 1:Got xml message");
                                                    if(eMessage!=null){
                                                                            
                                                                            //System.out.println("Stage 1:xml message not null");
                                                                            if(eMessage.getSuccessMessage()!=null){
                                                                                logger.info("Get Success Message:");
                                                                                                //System.out.println("Stage 1:xml message success");
                                                                                                message=eMessage.getSuccessMessage();
                                                                                                message.setTrackStatus("Call request sent");
                                                                                                message.setMessage("Stage 1 Calling");
                                                                                                message.setCallTo(callerId);
                                                                                                //adding all the sid values of calls placed
                                                                                                sids.offer(message.getSid());
                                                                             }
                                                                            else if (eMessage.getFailureMessage()!=null){
                                                                                logger.info("Get Failure Message:");
                                                                                               //System.out.println("Stage 1:xml message failed");
                                                                                                message=eMessage.getFailureMessage();
                                                                                                message.setCallTo(callerId);
                                                                                                message.setDateCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                                                                                                message.setTrackStatus(message.getMessage());
                                                                            }
                                                                            else{
                                                                                                //System.out.println("Stage 1:xml message unknown error");
                                                                                                logger.info("Get unknown Error Message:");
                                                                                                message.setCallTo(callerId);
                                                                                                message.setTrackStatus("Unknown Error");
                                                                                                message.setDateCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                                                                            }
                                                    }
                                                    //System.out.println("Stage 1:adding message to database");
                                                    //saving call details
                                                    callMaintain.addCallAPIMessage(message);
                                                    
                                                                            //removing number
                                                                            numbers.pop();
                                                                            //System.out.println("Stage 1:Number out of queue");
                                                    flag=!numbers.isEmpty();
                                                    logger.info("Value of flag:" +flag);
                                                    if(!flag){
                                                        //initiate call status update after call are over
                                                        checkFlag=true;
                                                    }
                                                    Long endTime=System.currentTimeMillis();
                                                    logger.info("End Time :"+endTime);
                            Long timeTaken=endTime-startTime;                            
                            float tt=timeTaken/1000;
                            logger.info("Time Taken :"+tt);
                            //System.out.println("Time Taken "+tt+" seconds");
                            }

    }

   
                    @Async
                    @Scheduled(fixedDelay=3000)
                    void checkCalledCustomers() throws IOException, JAXBException{
                                        logger.info("Inside checkCalledCustomers() Method:");                                        
                                        Long startTime=System.currentTimeMillis();
                                        //logger.info("Start Time :"+startTime);
                                        if(isCheckFlag()||sids.size()>=getExecutives()){
                                            
                                                            String sid=null;
                                                            String responseText="NA";
                                                            Document doc=null;
                                                           // logger.info("Stage 2:"+sids.size()+" Sids in queue to be sent to exotel to process "+Thread.currentThread());
                                                            //System.out.println("Stage 2:"+sids.size()+" Sids in queue to be sent to exotel to process "+Thread.currentThread());
                                                                                //fetching all the sid values
                                                                                sid=sids.peek();
                                                                                if(sids.isEmpty()){                                                                                    
                                                                                    checkFlag=false;
                                                                                }
                                                                                
                                                             CloseableHttpResponse response=null;
                                                             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                                             DocumentBuilder builder;
                                                             InputSource is;
                                                             try { 
                                                                         
                                                                         //checking sid details with exotel
                                                                         CloseableHttpClient httpclient = HttpClients.createDefault();
                                                                         HttpGet httpGet = new HttpGet("https://jubination:ce5e307d58d8ec07c8d8456e42ed171ff8322fd0@twilix.exotel.in/v1/Accounts/jubination/Calls/"+sid);
                                                                         response = httpclient.execute(httpGet);
                                                                         //logger.info("Http Response :"+response);
                                                                         //System.out.println("Stage 2:Sid sent to exotel");
                                                                         HttpEntity entity = response.getEntity();
                                                                         responseText = EntityUtils.toString(entity, "UTF-8");
                                                                         //logger.info("Response Text: "+responseText);
                                                                         builder = factory.newDocumentBuilder();                                                                         
                                                                         is = new InputSource(new StringReader(responseText));
                                                                         //logger.info("is value :"+is);
                                                                         doc = builder.parse(is);
                                                                         doc.getDocumentElement().normalize();                                                                         
                                                              } 
                                                             catch(IOException | ParseException | ParserConfigurationException | SAXException | DOMException e){
                                                                        e.printStackTrace();
                                                                        //logger.error("Erron in checkCalledCustomers() Method", e.getMessage());
                                                              }
                                                             finally {
                                                                        //logger.info("Fynally block:");
                                                                        if(response!=null){
                                                                                            response.close();
                                                                        }
                                                              }
                                                             //parsing xml response from exotel
                                                             
                                                             JAXBContext jaxbContext = JAXBContext.newInstance(ExotelMessage.class);
                                                             Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                                                             ExotelMessage eMessage = (ExotelMessage) jaxbUnmarshaller.unmarshal(doc);
                                                             //logger.info("Exotel Message: "+eMessage.toString());
                                                             Call message=null;
                                                             //System.out.println("Stage 2:Got xml message");
                                                             if(eMessage!=null){
                                                                 
                                                                            //System.out.println("Stage 2:xml message not null");                                                                            
                                                                            if(eMessage.getSuccessMessage()!=null){
                                                                                
                                                                                            message=eMessage.getSuccessMessage();
                                                                                            //System.out.println("Stage 2:xml message success "+message.getStatus()+message);
                                                                                            logger.info("Message :"+message);
                                                                                            if(message.getStatus().contains("queued")||message.getStatus().contains("ringing")||message.getStatus().contains("in-progress")){
                                                                                                            count++;
                                                                                                            logger.info("Count :"+count);
                                                                                                            //System.out.println("Stage 2:sid out of queue. Trying for"+count+"th time");
                                                                                            }
                                                                                            else {
                                                                                                
                                                                                                                        Call storedMessage=callMaintain.getCallRecordBySid(message.getSid());
                                                                                                                        logger.info("Value of Stored Message :"+storedMessage);
                                                                                                                        
                                                                                                                        if(storedMessage!=null){
                                                                                                                            
                                                                                                                                                //System.out.println("Stage 2:sid of the number was present already");
                                                                                                                                                 message.setOrderId(storedMessage.getOrderId());
                                                                                                                                                 message.setMessage("Stage 2 Tracking");
                                                                                                                                                //updating call details
                                                                                                                                                if(!storedMessage.getMessage().equals("Stage 3 Tracking")){
                                                                                                                                                                            logger.info("Stage 2:stage 3 not updated ye");
                                                                                                                                                                            //System.out.println("Stage 2:stage 3 not updated yet");
                                                                                                                                                                            message.setCallTo(storedMessage.getCallTo());
                                                                                                                                                                            callMaintain.updateCallAPIMessage(message);
                                                                                                                                                  }
                                                                                                                                                  else{
                                                                                                                                                                            //System.out.println("Stage 2:stage 3 updated already");
                                                                                                                                                                            logger.info("Stage 2:stage 3 updated already");
                                                                                                                                                                            System.out.println("Inside else of stored message");
                                                                                                                                                                            storedMessage.setStatus(message.getStatus());
                                                                                                                                                                            storedMessage.setCallType(message.getCallType());
                                                                                                                                                                            storedMessage.setRecordingUrl(message.getRecordingUrl());
                                                                                                                                                                            //callMaintain.updateCallAPIMessage(storedMessage);
                                                                                                                                                                            logger.info("UpdateCallApiMessage: "+callMaintain.updateCallAPIMessage(storedMessage));
                                                                                                                                                   }
                                                                                                                        }
                                                                                                                                                 //removing all the sid values
                                                                                                                                                sids.poll();
                                                                                                                                                count=0;
                                                                                                                                                //System.out.println("Stage 2:Sid out of queue");
                                                                                            }
                                                                                            if(count>=300){                                                                                                
                                                                                                    //System.out.println("Stage 2:Time out. sid out of queue. Trying for next sid");
                                                                                                    logger.info("Stage 2:Time out. sid out of queue. Trying for next sid");
                                                                                                    sids.poll();
                                                                                                    count=0;
                                                                                            }
                                                                                            logger.info("Value of count here:"+count);

                                                                            }
                                                            }
                                                            //stop operation if all done
                                                            checkFlag=!sids.isEmpty();
                                                            //logger.info("Check flag: "+checkFlag);
                                                            Long endTime=System.currentTimeMillis();
                                            Long timeTaken=endTime-startTime;
                                            float tt= timeTaken/1000;
                                            //System.out.println("Time taken:"+tt);
                                            //logger.info("Time Taken :"+tt);
                                            //logger.info("Value of counter:"+count);
                                            //System.out.println("Time Taken "+tt+" seconds");
                                        }
                                                                 
                  
                    }

                    //Depricated
           /*         @Async
                    @Scheduled(fixedDelay=3000)
                    void checkStageThreeCustomers() throws IOException, JAXBException{
                       Long startTime=System.currentTimeMillis();
                        if(!flag&&!checkFlag&&!stageThreeUpdates.isEmpty()){
                                             //fetching all the call values
                                                        Call callUpdated= stageThreeUpdates.peek();
                                                                           
                                                                           
                                                        if(callUpdated!=null){
                                                                    List<Call> callList=callMaintain.getCallBySid(callUpdated.getSid());
                                                                    if(callList!=null&&!callList.isEmpty()){
                                                                                        Call  call=callList.get(0);
                                                                                          //System.out.println("Stage 3:"+stageThreeUpdates.size()+"left. Update service started");
                                                                                         if(call!=null){
                                                                                                             //System.out.println("Stage 3:Call message present in database already and not null");
                                                                                                             call.setMessage("Stage 3 Tracking");
                                                                                                             call.setStatus("completed");
                                                                                                             call.setDailCallDuration(callUpdated.getDailCallDuration());
                                                                                                             call.setCallType(callUpdated.getCallType());
                                                                                                             call.setTrackStatus(callUpdated.getTrackStatus());
                                                                                                             call.setDigits(callUpdated.getDigits());
                                                                                                             call.setDailWhomNumber(callUpdated.getDailWhomNumber());
                                                                                                             call.setRecordingUrl(callUpdated.getRecordingUrl());
                                                                                                             call.setStartTime(callUpdated.getStartTime());
                                                                                                             call.setDirection(callUpdated.getDirection());
                                                                                                             //System.out.println("Stage 3:Updating database properties");
                                                                                                             callMaintain.updateCallAPIMessage(call);
                                                                                                             synchronized(this){
                                                                                                                               //removing all the call values
                                                                                                                                stageThreeUpdates.pop();
                                                                                                                                //System.out.println("Stage 3:Call Message out of queue");
                                                                                                            }
                                                                                         }
                                                                    }
                                                                    else{
                                                                                            //System.out.println("Stage 3:Call message not present in database");
                                                                                            callUpdated.setMessage("Incoming Tracking");
                                                                                            callMaintain.buildCallAPIMessage(callUpdated);
                                                                                            //removing all the call values
                                                                                             stageThreeUpdates.pop();
                                                                                             //System.out.println("Stage 3:Call Message out of queue");
                                                                                            //System.out.println("Stage 3:Built new CallMessage");
                                                                    }
                                                        }
                                                        if(stageThreeUpdates.isEmpty()){
                                                            callMaintain.sendEmailUpdate();
                                                        }
                                                              Long endTime=System.currentTimeMillis();
                                            Long timeTaken=endTime-startTime;
                                            float tt= timeTaken/1000;
                                            //System.out.println("Time Taken "+tt+" seconds");
                                        }
                                                            
                  
                    }
*/

                    public boolean isFlag() {
                        synchronized(this){         
                        return flag;
                        }
                    }

                    public void setFlag(boolean flag) {
                        synchronized(this){ 
                        this.flag = flag;
                        }
                    }

    public boolean isCheckFlag() {
        synchronized(this){
        return checkFlag;
        }
    }

    public void setCheckFlag(boolean checkFlag) {
        synchronized(this){
        this.checkFlag = checkFlag;
        }
    }
                    

     public Stack<String> getNumbers() {
       synchronized(numbers){
        return numbers;
       }
    }

    public ConcurrentLinkedQueue<String> getSids() {
        return sids;
    }

    public Stack<Call> getStageThreeUpdates() {
        synchronized(stageThreeUpdates){
        return stageThreeUpdates;
        }
    }

    public int getExecutives() {
        return executives;
    }

    public void setExecutives(int executives) {
        this.executives = executives;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }                  
        
}