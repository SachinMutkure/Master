/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jubination.model.pojo.crm;

import com.jubination.backend.service.sendgrid.EmailService;
import com.jubination.model.pojo.admin.Admin;
import com.jubination.model.pojo.exotel.Call;
import com.jubination.model.pojo.crm.Client;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonManagedReference;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

/**
 *
 * @author MumbaiZone
 */
@Entity
@Table(name="lead"
    ,catalog="jubination"
)

public class Lead implements Serializable {
    @Id
    String leadId;
    
    @Column(name="count")
    private int count;
    
    @Column(name="pending")
    private boolean pending;
    
    @Column(name="booked")
    private boolean booked;
    
    @Column(name="notification")
    private boolean notification;
    
    @Column(name="follow_up_date")
    private String followUpDate;  
    
    @Column(name="comments")
    private String comments;
    
    @Column(name="lead_status")
    private String leadStatus;
    
    @Column(name="missed_appointment")
    private Boolean missedAppointment;  
    
    @Column(name="missed_appointment_status")
    private String missedAppointmentStatus;  
    
    @Column(name="date_creation")
    String dateCreation;
    
    @Column(name="date_updated")
    String dateUpdated;
    
    @Column(name="hardcopy")
    private String hardcopy;
    
    @Column(name="appointment_date")
    private String appointmentDate;
    
    @Column(name="appointment_time")
    private String appointmentTime;
    
    
    @Column(name="order_id")
    private String orderId;
    @Column(name="order_by")
    private String orderBy;
    @Column(name="ben_count")
    private Integer benCount;
    
    @Column(name="product")
    private String product;
    @Column(name="service_type")
    private String serviceType;
    @Column(name="report_code")
    private String reportCode;
    @Column(name="rate")
    private String rate;
    @Column(name="margin")
    private String margin;
    @Column(name="passon")
    private String passon;
    @Column(name="pay_type")
    private String payType;
    @Column(name="handling_charges")
    private String handlingCharges;
    @Column(name="last_calling_thread")
    private String lastCallingThread;
    
    @Transient
    boolean persistent;
     
   //  @JsonBackReference  
    @ManyToOne
    private Client client;
    @JsonManagedReference 
    @OneToMany(mappedBy="lead")
    @Cascade({CascadeType.DELETE,CascadeType.SAVE_UPDATE})
    @NotFound(action=NotFoundAction.IGNORE)
    private List<Call> call= new ArrayList<>();
      
    @JsonManagedReference 
    @OneToMany(mappedBy="lead")
    @Cascade({CascadeType.DELETE,CascadeType.SAVE_UPDATE})
    @NotFound(action=NotFoundAction.IGNORE)
    private List<Beneficiaries> beneficiaries= new ArrayList<>();
    @OneToOne
      private Admin admin;

    public Lead() {
    }

    public Lead(String leadId) {
        this.leadId = leadId;
    }

    public String getLeadId() {
        return leadId;
    }

    public void setLeadId(String leadId) {
        this.leadId = leadId;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<Call> getCall() {
        return call;
    }

    public void setCall(List<Call> call) {
        this.call = call;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        if(count<0){
            try{
                    String val=null;
                    val.toCharArray();
            }
            catch(Exception e){
                System.out.println("CULPRIT::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
                e.printStackTrace();
            }
        }
        this.count = count;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(String followUpDate) {
        this.followUpDate = followUpDate;
    }

    public String getLeadStatus() {
        return leadStatus;
    }

    public void setLeadStatus(String leadStatus) {
        this.leadStatus = leadStatus;
    }

    public String getHardcopy() {
        return hardcopy;
    }

    public void setHardcopy(String hardcopy) {
        this.hardcopy = hardcopy;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Integer getBenCount() {
        return benCount;
    }

    public void setBenCount(Integer benCount) {
        this.benCount = benCount;
    }

    public String getReportCode() {
        return reportCode;
    }

    public void setReportCode(String reportCode) {
        this.reportCode = reportCode;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getMargin() {
        return margin;
    }

    public void setMargin(String margin) {
        this.margin = margin;
    }

  

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getHandlingCharges() {
        return handlingCharges;
    }

    public void setHandlingCharges(String handlingCharges) {
        this.handlingCharges = handlingCharges;
    }

    public List<Beneficiaries> getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(List<Beneficiaries> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }

    public String getPasson() {
        return passon;
    }

    public void setPasson(String passon) {
        this.passon = passon;
    }

public boolean isPersistent() {
        return persistent;
    }

    public String getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(String dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Boolean isMissedAppointment() {
        return missedAppointment;
    }

    public void setMissedAppointment(Boolean missedAppointment) {
        this.missedAppointment = missedAppointment;
    }

    public String getMissedAppointmentStatus() {
        return missedAppointmentStatus;
    }

    public void setMissedAppointmentStatus(String missedAppointmentStatus) {
        this.missedAppointmentStatus = missedAppointmentStatus;
    }

    public String getLastCallingThread() {
        return lastCallingThread;
    }

    public void setLastCallingThread(String lastCallingThread) {
        this.lastCallingThread = lastCallingThread;
    }

   


    @PostLoad
    @PostPersist
    public void setPersistent() {
        this.persistent = true;
    }
        
    
}
