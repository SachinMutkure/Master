package com.jubination.controller.web;

import com.jubination.backend.service.exotel.numbercall.serial.CallBox;
import com.jubination.backend.service.core.leadcall.parallel.master.CallScheduler;
import com.jubination.backend.service.core.leadcall.parallel.master.CallManager;
import com.jubination.model.pojo.admin.Admin;
import com.jubination.model.pojo.crm.Client;
import com.jubination.model.pojo.crm.Lead;
import com.jubination.model.pojo.exotel.Call;
import com.jubination.service.AdminMaintainService;
import com.jubination.service.CallMaintainService;
import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class CallController {
    @Autowired
    CallMaintainService callMaintain;
    
    @Autowired
    AdminMaintainService adminMaintain;
    
    @Autowired
    CallScheduler operator;
    
    @Autowired
    CallManager eCallHandler;
    
    @Autowired
    CallBox callHandler;

    /** 1. Some leads don't get a call on assigned follow up date */
    @RequestMapping(value="/admin/callcustom/call/lead",method = RequestMethod.POST)
    public ModelAndView callCustomLeads(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callCustomLeads()");
            ModelAndView model= new ModelAndView("admincallcustom");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            if(request.getParameter("numbers")!=null){
                operator.doLeadCall(request.getParameter("numbers"));
                model.addObject("message", "Keep Calm and attend calls..:P");
                return model;
            }
            model.addObject("message", "Error during call");
            return model;
    }
    
    /** issue 2.Some leads follow up count become zero in spite of 8 follow ups assigned to all leads
     */
    @RequestMapping(value="/admin/callcustom/add/lead/count",method = RequestMethod.POST)
    public ModelAndView changeLeadCount(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.changeLeadCount()");
            ModelAndView model= new ModelAndView("admincallcustom");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            String numbers=request.getParameter("numbers");
            String count=request.getParameter("count");
            if(request.getParameter("numbers")!=null){
                 for(String number:numbers.trim().split(System.lineSeparator())){
                     Lead lead=callMaintain.readLead(number);
                     if(lead!=null){
                         lead.setCount(Integer.parseInt(count));
                         callMaintain.updateLeadOnly(lead);
                     }
                 }
                model.addObject("message", "Keep Calm and attend calls..:P");
                return model;
            }
            model.addObject("message", "Error during call");
            return model;
    }
    
    /**11. Incoming call get disconnected after 2 mins */
    @RequestMapping(value="/admin/callcustom/call/cust",method = RequestMethod.POST)
    public ModelAndView callCustomCustomers(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callCustomCustomers()");
            ModelAndView model= new ModelAndView("admincallcustom");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            if(request.getParameter("numbers")!=null){
                operator.doCustCall(request.getParameter("numbers"));
                model.addObject("message", "Keep Calm and attend calls..:P");
                return model;
            }
            model.addObject("message", "Error during call");
            return model;
    }
    
    
    @RequestMapping(value="/admin/callcustom")
    public ModelAndView customCallInterface(HttpServletRequest request, Principal principal) throws IOException {
       
            System.out.println("*******com.jubination.controller.web.CallController.customCallInterface()");
            ModelAndView model= new ModelAndView("admincallcustom");
            model.addObject("ex",callHandler.getExecutives());
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            return model;
    }
    
    
    // Call in real time **** if client got real time issue prefer this method
    // not understanding how the thread will call this method...
    @RequestMapping(value="/admin/callinterface")
    public ModelAndView callInterface(HttpServletRequest request, Principal principal) throws IOException {
         // here got exception after few call 
        //Severe:   Exception in thread "C3P0PooledConnectionPoolManager[identityToken->z8kfsx9m1et4v9l1vu3904|3fedc673]-AdminTaskTimer"
        //Severe:   java.lang.IllegalStateException: This web container has not yet been started
            System.out.println("*******com.jubination.controller.web.CallController.callInterface()");
            ModelAndView model= new ModelAndView("admincallinterface");
            model.addObject("clientStage",eCallHandler.getClientStage2());
            model.addObject("clientStage1",eCallHandler.getClientStage1());
            model.addObject("clientStage2",eCallHandler.getClientStage2());
            model.addObject("callStage3",eCallHandler.getStageThreeUpdates()); 
            return model;
    }
    @RequestMapping(value="/admin/callsettings")
    public ModelAndView callSettings(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettings()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            getCallSettingsParam(model);
            model.addObject("cs1", callHandler.isFlag());
            model.addObject("cs2", callHandler.isCheckFlag());
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/setExecs/custom")
    public ModelAndView setCustomExecsCustom(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.setCustomExecsCustom()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            callHandler.setExecutives(Integer.parseInt(request.getParameter("exc")));
            model.addObject("message","Processed");
            getCallSettingsParam(model);
            model.addObject("cs1", callHandler.isFlag());
            model.addObject("cs2", callHandler.isCheckFlag());
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/followup/check")
    public ModelAndView setFollowup(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.setFollowup()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            callMaintain.checkFollowUp();
            getCallSettingsParam(model);
            model.addObject("cs1", callHandler.isFlag());
            model.addObject("cs2", callHandler.isCheckFlag());
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/setExecs/auto")
    public ModelAndView setCustomExecsAuto(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.setCustomExecsAuto()");
            ModelAndView model= new ModelAndView("admincallsettings");
            Admin admin=adminMaintain.checkPresence(new Admin(principal.getName()));
            model.addObject("admin",admin);
            eCallHandler.setExecutives(Integer.parseInt(request.getParameter("ex")),admin.getName());
           
            model.addObject("message","Processed");
            getCallSettingsParam(model);
            model.addObject("cs1", callHandler.isFlag());
            model.addObject("cs2", callHandler.isCheckFlag());
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/set/count")
    public ModelAndView callSettingsSetCount(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsSetCount()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            operator.setCount(Integer.parseInt(request.getParameter("count")));
            getCallSettingsParam(model);
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/stage1/on")
    public ModelAndView callSettingsSwitchOnStage1(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsSwitchOnStage1()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            eCallHandler.setStatus(true);
            getCallSettingsParam(model);
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/stage2/on")
    public ModelAndView callSettingsSwitchOnStage2(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsSwitchOnStage2()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            getCallSettingsParam(model);
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/stage3/on")
    public ModelAndView callSettingsSwitchOnStage3(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsSwitchOnStage3()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            getCallSettingsParam(model);
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/stage1/off")
    public ModelAndView callSettingsSwitchOffStage1(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsSwitchOffStage1()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            eCallHandler.setStatus(false);
            getCallSettingsParam(model);
            return model;
    }

    @RequestMapping(value="/admin/callsettings/stage2/off")
    public ModelAndView callSettingsSwitchOffStage2(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsSwitchOffStage2()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            getCallSettingsParam(model);
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/stage3/off")
    public ModelAndView callSettingsSwitchOffStage3(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsSwitchOffStage3()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            getCallSettingsParam(model);
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/stage1/flush")
    public ModelAndView callSettingsFlushStage1Numbers(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsFlushStage1Numbers()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            eCallHandler.getClientStage1().clear();
            getCallSettingsParam(model);
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/stage2/flush")
    public ModelAndView callSettingsFlushStage2(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsFlushStage2()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            eCallHandler.getClientStage2().clear();
            getCallSettingsParam(model);
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/stage3/flush")
    public ModelAndView callSettingsFlushStage3(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsFlushStage3()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            eCallHandler.getStageThreeUpdates().clear();
            getCallSettingsParam(model);
            return model;
    }
     
    @RequestMapping(value="/admin/callsettings/cc/stage1/flush")
    public ModelAndView callSettingsFlushCCStage1Numbers(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsFlushCCStage1Numbers()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            callHandler.getNumbers().clear();
            callHandler.setFlag(false);
            getCallSettingsParam(model);
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/cc/stage2/flush")
    public ModelAndView callSettingsFlushCCStage2(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsFlushCCStage2()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            callHandler.getSids().clear();
            callHandler.setCheckFlag(false);
            getCallSettingsParam(model);
            return model;
    }
    
    @RequestMapping(value="/admin/callsettings/cc/stage3/flush")
    public ModelAndView callSettingsFlushCCStage3(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callSettingsFlushCCStage3()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            callHandler.getStageThreeUpdates().clear();
            getCallSettingsParam(model);
            return model;
    }
   
    @RequestMapping(value="/admin/callsettings/followup/on")
    public ModelAndView FollowupOn(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.FollowupOn()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            operator.setFollowupFlag(true);
            getCallSettingsParam(model);
            return model;
    }    
    
    @RequestMapping(value="/admin/callsettings/followup/off")
    public ModelAndView noonFollowupOff(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.noonFollowupOff()");
            ModelAndView model= new ModelAndView("admincallsettings");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            operator.setFollowupFlag(false);
            getCallSettingsParam(model);
            return model;
    }    
    
   

    @RequestMapping(value="/admin/callrecords")
    public ModelAndView callRecords(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callRecords()");
            ModelAndView model= new ModelAndView("admincallrecords");
            List<Call> list=callMaintain.getAllCallRecordsByDate(request.getParameter("date"));
            if(list!=null&&!list.isEmpty()){   
                model.addObject("callrecords",list);
            }
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            model.addObject("message", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            return model;
    }
     
    @RequestMapping(value="/admin/callrecords/get")
    public ModelAndView getCallRecords(HttpServletRequest request, Principal principal) throws IOException {
        System.out.println("*******com.jubination.controller.web.CallController.getCallRecords()");
        ModelAndView model= new ModelAndView("admincallrecords");
        model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
        List<Call> list=callMaintain.getAllCallRecordsByDate(request.getParameter("date"));
        if(list!=null&&!list.isEmpty()){   
            model.addObject("callrecords",list);
            model.addObject("message", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            model.addObject("excel",callMaintain.createCallExcel(list)&&callMaintain.createClientExcel(request.getParameter("date")));
        }
        else{
            model.addObject("message","No such records found");
        }
            return model;
    }
    
    // follow Up Calls action page     
    @RequestMapping(value="/admin/callnotification")
    public ModelAndView callNotifications(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.CallController.callNotifications()");
            ModelAndView model= new ModelAndView("admincallnotification");
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            model.addObject("lead", callMaintain.readNotifiedLead());
            return model;
    }
    
   
    private ModelAndView getCallSettingsParam(ModelAndView model){
            System.out.println("*******com.jubination.controller.web.CallController.getCallSettingsParam()");
            model.addObject("ex",eCallHandler.getExecutives());
            model.addObject("sFlag1", eCallHandler.getStatus());
            model.addObject("sCount1", eCallHandler.getClientStage1().size());
            model.addObject("sCount2", eCallHandler.getClientStage2().size());
            model.addObject("sCount3", eCallHandler.getStageThreeUpdates().size());
            model.addObject("sCountCC1", callHandler.getNumbers().size());
            model.addObject("sCountCC2", callHandler.getSids().size());
            model.addObject("sCountCC3", callHandler.getStageThreeUpdates().size());
            model.addObject("followUpCount", operator.getCount());
            model.addObject("followupFlag", operator.isFollowupFlag());
            model.addObject("ex", eCallHandler.getExecutives());
            model.addObject("exc",callHandler.getExecutives());
            return model;
    }
    
}
