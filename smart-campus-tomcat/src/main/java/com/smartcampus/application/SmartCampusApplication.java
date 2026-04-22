package com.smartcampus.application;

import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.mapper.GenericExceptionMapper;
import com.smartcampus.mapper.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.mapper.RoomNotEmptyExceptionMapper;
import com.smartcampus.mapper.SensorUnavailableExceptionMapper;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.SensorReadingResource;
import com.smartcampus.resource.SensorResource;
import com.smartcampus.resource.SensorRoomResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application subclass for Tomcat deployment.
 *
 * Deployment is managed via WEB-INF/web.xml which maps this application
 * to the "/api/v1/*" path.
 *
 * All resource classes, exception mappers, and filters are explicitly registered
 * in getClasses() to ensure Tomcat deploys them correctly.
 */
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Resource classes
        classes.add(DiscoveryResource.class);
        classes.add(SensorRoomResource.class);
        classes.add(SensorResource.class);
        classes.add(SensorReadingResource.class);

        // Exception mappers
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GenericExceptionMapper.class);

        // Filters
        classes.add(LoggingFilter.class);

        return classes;
    }
}
