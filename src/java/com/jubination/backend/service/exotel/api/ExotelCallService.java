/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jubination.backend.service.exotel.api;

import com.jubination.backend.pojo.exotel.ExotelMessage;
import com.jubination.backend.service.core.leadcall.parallel.worker.CallWorker;
import com.jubination.backend.service.core.leadcall.parallel.worker.CallService;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author MumbaiZone
 */
@Component
@Scope("prototype")
public class ExotelCallService{
    
    public ExotelMessage makeCall(String callerId){
        System.out.println("#######com.jubination.backend.service.exotel.api.ExotelCallService.makeCall()");
              ExotelMessage eMessage=null;
              try{
                    String responseText="NA";
                    Document doc=null;
                    if(callerId!=null){
                            CloseableHttpResponse response=null;
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder builder;
                            InputSource is;
                            try { 
                                    //requesting exotel to initiate call
                                    /**1. Reset connection Exception while calling
                                     */
                                    /*****com.jubination.backend.service.core.leadcall.parallel.master.CallScheduler.freshCustomerCall()
                                     * Info:   I/O exception (java.net.SocketException) caught when processing request to {s}->https://twilix.exotel.in:443: Connection reset
                                       Info:   Retrying request to {s}->https://twilix.exotel.in:443 **/
                                    
                                    /** Second exception Related admin Task timer**
                                     *  Severe:   Exception in thread "C3P0PooledConnectionPoolManager[identityToken->z8kfsx9m1kno7z8j7srca|4a7871de]-AdminTaskTimer"
                                        Severe:   java.lang.IllegalStateException: This web container has not yet been started
                                        at org.glassfish.web.loader.WebappClassLoader.loadClass(WebappClassLoader.java:1674)
                                        at org.glassfish.web.loader.WebappClassLoader.loadClass(WebappClassLoader.java:1633)
                                        at com.mchange.v2.resourcepool.BasicResourcePool.destroyResource(BasicResourcePool.java:1081)
                                        at com.mchange.v2.resourcepool.BasicResourcePool.removeResource(BasicResourcePool.java:1552)
                                        at com.mchange.v2.resourcepool.BasicResourcePool.removeResource(BasicResourcePool.java:1522)
                                        at com.mchange.v2.resourcepool.BasicResourcePool.cullExpired(BasicResourcePool.java:1610)
                                        at com.mchange.v2.resourcepool.BasicResourcePool.access$1900(BasicResourcePool.java:44)
                                        at com.mchange.v2.resourcepool.BasicResourcePool$CullTask.run(BasicResourcePool.java:2134)
                                        at java.util.TimerThread.mainLoop(Timer.java:555)
                                        at java.util.TimerThread.run(Timer.java:505)
                                     * 
                                     */
                                    CloseableHttpClient httpclient = HttpClients.createDefault();
                                    HttpPost httpPost = new HttpPost("https://jubination:ce5e307d58d8ec07c8d8456e42ed171ff8322fd0@twilix.exotel.in/v1/Accounts/jubination/Calls/connect");
                                    List<NameValuePair> formparams = new ArrayList<>();
                                    formparams.add(new BasicNameValuePair("From",callerId));
                                   // formparams.add(new BasicNameValuePair("To",callerId));
                                    formparams.add(new BasicNameValuePair("CallerId","02239698495"));
                                    formparams.add(new BasicNameValuePair("CallerType","trans"));
                                    formparams.add(new BasicNameValuePair("Url","http://my.exotel.in/exoml/start/102261"));
                //                    formparams.add(new BasicNameValuePair("TimeLimit","1800"));
                   //                 formparams.add(new BasicNameValuePair("TimeOut","30"));
                                    formparams.add(new BasicNameValuePair("SatusCallback",""));
                                    formparams.add(new BasicNameValuePair("CustomField","testcall"));
                                    UrlEncodedFormEntity uEntity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
                                    httpPost.setEntity(uEntity);
                                    response = httpclient.execute(httpPost);
                                    System.out.println(Thread.currentThread().getName()+" "+"Stage 1:Calls sent to exotel");
                                    HttpEntity entity = response.getEntity();

                                    responseText = EntityUtils.toString(entity, "UTF-8");
                                    builder = factory.newDocumentBuilder();
                                    is = new InputSource(new StringReader(responseText));
                                    doc = builder.parse(is);
                                    doc.getDocumentElement().normalize();
                            } 
                            catch(IOException | ParseException | ParserConfigurationException | SAXException | DOMException e){
                                    e.printStackTrace();
                            }
                            finally {
                                    if(response!=null){
                                            response.close();
                                    }
                           }
                    }
                    //parsing xml response from exotel
                    JAXBContext jaxbContext = JAXBContext.newInstance(ExotelMessage.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    eMessage = (ExotelMessage) jaxbUnmarshaller.unmarshal(doc);
                    System.out.println(Thread.currentThread().getName()+" "+"Stage 1:Got xml message");
              }
              catch(IOException | JAXBException e){
                  e.printStackTrace();
              }
          
           return eMessage;
          }
    
     public ExotelMessage checkStatus(String sid){
            System.out.println("#######com.jubination.backend.service.exotel.api.ExotelCallService.checkStatus()");
            ExotelMessage eMessage=null;
                 String responseText="NA";
                            Document doc=null;
                            CloseableHttpResponse response=null;
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder builder;
                            InputSource is;
               try{
                          
                 //checking sid details with exotel
                                        CloseableHttpClient httpclient = HttpClients.createDefault();
                                        HttpGet httpGet = new HttpGet("https://jubination:ce5e307d58d8ec07c8d8456e42ed171ff8322fd0@twilix.exotel.in/v1/Accounts/jubination/Calls/"+sid);
                                        response = httpclient.execute(httpGet);
                                        System.out.println(Thread.currentThread().getName()+" "+"Stage 2:Sid sent to exotel");
                                        HttpEntity entity = response.getEntity();
                                        responseText = EntityUtils.toString(entity, "UTF-8");
                                        builder = factory.newDocumentBuilder();
                                        is = new InputSource(new StringReader(responseText));
                                        doc = builder.parse(is);
                                        doc.getDocumentElement().normalize();
                     
                            
                             //parsing xml response from exotel
                                    JAXBContext jaxbContext = JAXBContext.newInstance(ExotelMessage.class);
                                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                                    eMessage = (ExotelMessage) jaxbUnmarshaller.unmarshal(doc);
                                    
                                    System.out.println(Thread.currentThread().getName()+" "+"Stage 2:Got xml message");
               }
                catch(IOException | ParseException | ParserConfigurationException | SAXException | JAXBException e){
                    e.printStackTrace();
                        System.out.println("#"+Thread.currentThread().getName()+"Exception 2. Stage2 out");
                        
                    }
                    finally {
                                       if(response!=null){
                                           try {
                                               response.close();
                                           } catch (IOException ex) {
                                               Logger.getLogger(CallWorker.class.getName()).log(Level.SEVERE, null, ex);
                                           }
                                       }
                             }
                                    return eMessage;
           }
        
          
}
