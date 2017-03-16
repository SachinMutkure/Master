/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jubination.controller.web;

import com.jubination.model.pojo.admin.Admin;
import com.jubination.model.pojo.crm.DataAnalytics;
import com.jubination.service.AdminMaintainService;
import com.jubination.service.CallMaintainService;
import com.jubination.service.DataAnalyticsService;
import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author MumbaiZone
 */

@Controller
public class AnalyticsController {
    
    @Autowired
    DataAnalyticsService analyticsMaintain;
    
    @Autowired
    AdminMaintainService adminMaintain;
    
    @RequestMapping(value = "/admin")
    public  ModelAndView adminLoginCheck(HttpServletRequest request,Principal principal) {
            // After Login user here...
            System.out.println("*******com.jubination.controller.web.AnalyticsController.adminLoginCheck()");
            ModelAndView model= new ModelAndView("adminpage");
            model.addObject("fDate",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            model.addObject("tDate",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            return model;
    }
   
    @RequestMapping(value = "/admin/callanalytics/getquality/{date}")
    public  ModelAndView adminmailSpoke(@PathVariable("date") String date,HttpServletRequest request,Principal principal) {
            System.out.println("*******com.jubination.controller.web.AnalyticsController.adminmailSpoke()");
            ModelAndView model= new ModelAndView("adminpage");
            if(date.equals("none")){
                            analyticsMaintain.mailSpokeAnalytics(null);

            }
            else{
                            analyticsMaintain.mailSpokeAnalytics(date);
            }
            model.addObject("fDate",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            model.addObject("tDate",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            return model;
    }
    
    /** Read Analysis by Date **/
    @RequestMapping(value = "/admin/callanalytics/get")
    public  ModelAndView readAnalytics(HttpServletRequest request,Principal principal) {
            System.out.println("*******com.jubination.controller.web.AnalyticsController.readAnalytics()");
            ModelAndView model= new ModelAndView("adminpage");
            model.addObject("analytics",analyticsMaintain.readAnalytics(request.getParameter("date")));
            model.addObject("fDate",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            model.addObject("tDate",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            return model;
    }    
    
    /** Read Recent Analysis By fromDate to toDate **/
    @RequestMapping(value = "/admin/callanalytics/get/recent")
    public  ModelAndView readRecentAnalyics(HttpServletRequest request,Principal principal) {
            System.out.println("*******com.jubination.controller.web.AnalyticsController.readRecentAnalyics()");
            ModelAndView model= new ModelAndView("adminpage");
            model.addObject("analytics",analyticsMaintain.readRecentAnalytics());
            model.addObject("fDate",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            model.addObject("tDate",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            return model;
    }
    /** Do Custom Analysis by fromDate to toDate 
        And Generate todays report
        **/
    @RequestMapping(value="/admin/do/analytics")
    public ModelAndView doCallAnalytics(HttpServletRequest request, Principal principal) throws IOException {
            System.out.println("*******com.jubination.controller.web.AnalyticsController.doCallAnalytics()");
            ModelAndView model= new ModelAndView("adminpage");
            if(request.getParameter("fromDate")!=null&&request.getParameter("toDate")!=null){
                //Do custom Analysis...
                System.out.println("*****Inside the if condition...");
                analyticsMaintain.doCustomAnalytics(request.getParameter("fromDate")+" 00:00:00",request.getParameter("toDate")+" 23:59:59");
                model.addObject("fDate",request.getParameter("fromDate"));
                model.addObject("tDate",request.getParameter("toDate"));
                model.addObject("analytics",analyticsMaintain.readRecentAnalytics());
            }
            else{
                // Generate Todays report...
                System.out.println("*****Inside the else condition");
                analyticsMaintain.doAnalytics();
                model.addObject("fDate",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                model.addObject("tDate",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                model.addObject("analytics",analyticsMaintain.readAnalytics(new SimpleDateFormat("yyyy").format(new Date())));
            }
            
            model.addObject("admin",adminMaintain.checkPresence(new Admin(principal.getName())));
            
            return model;
    }

    
}
