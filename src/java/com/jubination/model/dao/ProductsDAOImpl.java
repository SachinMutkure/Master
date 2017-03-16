/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jubination.model.dao;

import com.jubination.model.pojo.products.Campaigns;
import com.jubination.model.pojo.products.Products;
import java.io.Serializable;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author MumbaiZone
 */
@Repository
public class ProductsDAOImpl<T> implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ProductsDAOImpl.class);
            
    @Autowired
    private SessionFactory sessionFactory;
    private Session session=null;
    
    
    
    @Transactional(propagation = Propagation.REQUIRED)
    public Object buildEntity(Object entity) {
            Products p=(Products) entity;
            session = getSessionFactory().getCurrentSession();
            session.save(p);
            p = (Products) session.get(Products.class, p.getId());
            return (T) p;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<T> fetchProductEntities() {
            session = getSessionFactory().getCurrentSession();
            return session.createCriteria(Products.class).list();
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean deleteAllProductEntities() {
            session = getSessionFactory().getCurrentSession();
            List<Products> list=session.createCriteria(Products.class).list();
            for(Products p:list){
                session.delete(p);
            }
            return true;
    }
    
    
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public Object fetchCampaignEntities() {
            session = getSessionFactory().getCurrentSession();
            return session.createCriteria(Campaigns.class).list();
           
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public Object buildCampaignEntity(Object entity) {
            Campaigns camp=(Campaigns) entity;
            session = getSessionFactory().getCurrentSession();
            session.saveOrUpdate(camp);
            camp = (Campaigns) session.get(Campaigns.class, camp.getName());
        
        return (T) camp;
           
    }
 @Transactional(propagation = Propagation.REQUIRED)
    public Boolean updateCampaignEntity(Object entity) {
        Campaigns camp = (Campaigns) entity;
            session = getSessionFactory().getCurrentSession();
            camp=(Campaigns) session.merge(camp);
            session.update(camp);
            return true;
           
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public Object readCampaignProperty(String param) {
        logger.info("Inside readCampaignProperty:");
        logger.info("Value of param:"+param);
        session = getSessionFactory().getCurrentSession();
        Campaigns camp = (Campaigns) session.get(Campaigns.class, param);
        logger.info("Campaigns: "+camp.toString());
        return (T) camp;
           
    }
   @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<String> fetchProductNames(String name) {
            session = getSessionFactory().getCurrentSession();
            return session.createCriteria(Products.class)
                    .setProjection(Projections.projectionList().add(Projections.property("name"))).add(Restrictions.ilike("name", name, MatchMode.START))
                    .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list();
            
    }
     @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<String> fetchCampaignNames(String name) {
            session = getSessionFactory().getCurrentSession();
            return session.createCriteria(Campaigns.class)
                    .setProjection(Projections.projectionList().add(Projections.property("name"))).add(Restrictions.ilike("name", name, MatchMode.START))
                    .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).list();
    }
  
     public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
}
