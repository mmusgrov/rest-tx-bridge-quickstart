package org.jboss.jbossts.resttxbridge.quickstart.jpa.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.jboss.jbossts.resttxbridge.inbound.BridgeDurableParticipant;
import org.jboss.jbossts.resttxbridge.inbound.provider.InboundBridgePostProcessInterceptor;
import org.jboss.jbossts.resttxbridge.inbound.provider.InboundBridgePreProcessInterceptor;

/**
 * 
 * @author Gytis Trikleris
 * 
 */
public class TaskApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();

        classes.add(BridgeDurableParticipant.class);
        classes.add(TaskResource.class);

        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletos = new HashSet<Object>();

        singletos.add(new InboundBridgePreProcessInterceptor());
        singletos.add(new InboundBridgePostProcessInterceptor());

        return singletos;
    }

}
