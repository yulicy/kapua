/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.consumer.activemq.lifecycle;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

//import java.util.Optional;
import javax.inject.Inject;

import org.eclipse.kapua.broker.client.amqp.ClientOptions;
import org.eclipse.kapua.broker.client.amqp.ClientOptions.AmqpClientOptions;
import org.eclipse.kapua.commons.core.vertx.AbstractMainVerticle;
//import org.eclipse.kapua.commons.jpa.JdbcConnectionUrlResolvers;
//import org.eclipse.kapua.commons.setting.system.SystemSetting;
//import org.eclipse.kapua.commons.setting.system.SystemSettingKey;
import org.eclipse.kapua.commons.core.vertx.HttpRestServer;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.connector.MessageContext;
import org.eclipse.kapua.connector.Properties;
import org.eclipse.kapua.connector.activemq.AmqpTransportActiveMQConnector;
import org.eclipse.kapua.consumer.activemq.lifecycle.settings.ActiveMQLifecycleSettings;
import org.eclipse.kapua.consumer.activemq.lifecycle.settings.ActiveMQLifecycleSettingsKey;
import org.eclipse.kapua.converter.kura.KuraPayloadProtoConverter;
import org.eclipse.kapua.message.transport.TransportMessage;
import org.eclipse.kapua.processor.Processor;
import org.eclipse.kapua.processor.error.amqp.activemq.ErrorProcessor;
import org.eclipse.kapua.processor.lifecycle.LifecycleProcessor;
//import org.eclipse.kapua.service.liquibase.KapuaLiquibaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.google.common.base.MoreObjects;

import io.vertx.core.Future;
import io.vertx.ext.healthchecks.Status;
import io.vertx.proton.ProtonQoS;

public class MainVerticle extends AbstractMainVerticle {

    protected final static Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    private final static String HEALTH_NAME_CONNECTOR = "ActiveMQ-connector";
    private final static String HEALTH_NAME_LIFECYCLE = "Lifecycle-processor";
    private final static String HEALTH_NAME_ERROR = "Error-processor";

    private final static String PROCESSOR_NAME_LIFECYCLE = "Lifecycle";

    protected Map<String, Processor<TransportMessage>> processorMap;
    protected Map<String, Processor> errorProcessorMap;

    private ClientOptions connectorOptions;
    private AmqpTransportActiveMQConnector connector;
    private KuraPayloadProtoConverter converter;
    private LifecycleProcessor processor;
    private ClientOptions errorOptions;
    protected ErrorProcessor errorProcessor;

    @Inject
    private HttpRestServer httpRestServer;

    public MainVerticle() {
//        SystemSetting configSys = SystemSetting.getInstance();
//        logger.info("Checking database... '{}'", configSys.getBoolean(SystemSettingKey.DB_SCHEMA_UPDATE, false));
//        if (configSys.getBoolean(SystemSettingKey.DB_SCHEMA_UPDATE, false)) {
//            logger.debug("Starting Liquibase embedded client.");
//            String dbUsername = configSys.getString(SystemSettingKey.DB_USERNAME);
//            String dbPassword = configSys.getString(SystemSettingKey.DB_PASSWORD);
//            String schema = MoreObjects.firstNonNull(configSys.getString(SystemSettingKey.DB_SCHEMA_ENV), configSys.getString(SystemSettingKey.DB_SCHEMA));
//            new KapuaLiquibaseClient(JdbcConnectionUrlResolvers.resolveJdbcUrl(), dbUsername, dbPassword, Optional.of(schema)).update();
//        }
        connectorOptions = new ClientOptions(
                ActiveMQLifecycleSettings.getInstance().getString(ActiveMQLifecycleSettingsKey.CONNECTION_HOST),
                ActiveMQLifecycleSettings.getInstance().getInt(ActiveMQLifecycleSettingsKey.CONNECTION_PORT),
                ActiveMQLifecycleSettings.getInstance().getString(ActiveMQLifecycleSettingsKey.CONNECTION_USERNAME),
                ActiveMQLifecycleSettings.getInstance().getString(ActiveMQLifecycleSettingsKey.CONNECTION_PASSWORD));
        connectorOptions.put(AmqpClientOptions.CLIENT_ID, ActiveMQLifecycleSettings.getInstance().getString(ActiveMQLifecycleSettingsKey.TELEMETRY_CLIENT_ID));
        connectorOptions.put(AmqpClientOptions.DESTINATION, ActiveMQLifecycleSettings.getInstance().getString(ActiveMQLifecycleSettingsKey.TELEMETRY_DESTINATION));
        connectorOptions.put(AmqpClientOptions.CONNECT_TIMEOUT, ActiveMQLifecycleSettings.getInstance().getInt(ActiveMQLifecycleSettingsKey.CONNECT_TIMEOUT));
        connectorOptions.put(AmqpClientOptions.MAXIMUM_RECONNECTION_ATTEMPTS, ActiveMQLifecycleSettings.getInstance().getInt(ActiveMQLifecycleSettingsKey.MAX_RECONNECTION_ATTEMPTS));
        connectorOptions.put(AmqpClientOptions.IDLE_TIMEOUT, ActiveMQLifecycleSettings.getInstance().getInt(ActiveMQLifecycleSettingsKey.IDLE_TIMEOUT));
        connectorOptions.put(AmqpClientOptions.AUTO_ACCEPT, false);
        connectorOptions.put(AmqpClientOptions.PREFETCH_MESSAGES, ActiveMQLifecycleSettings.getInstance().getInt(ActiveMQLifecycleSettingsKey.TELEMETRY_PREFETCH_MESSAGES));
        connectorOptions.put(AmqpClientOptions.QOS, ProtonQoS.AT_LEAST_ONCE);
        errorOptions = new ClientOptions();
        errorOptions = new ClientOptions(
                ActiveMQLifecycleSettings.getInstance().getString(ActiveMQLifecycleSettingsKey.CONNECTION_HOST),
                ActiveMQLifecycleSettings.getInstance().getInt(ActiveMQLifecycleSettingsKey.CONNECTION_PORT),
                ActiveMQLifecycleSettings.getInstance().getString(ActiveMQLifecycleSettingsKey.CONNECTION_USERNAME),
                ActiveMQLifecycleSettings.getInstance().getString(ActiveMQLifecycleSettingsKey.CONNECTION_PASSWORD));
        errorOptions.put(AmqpClientOptions.CLIENT_ID, ActiveMQLifecycleSettings.getInstance().getString(ActiveMQLifecycleSettingsKey.ERROR_CLIENT_ID));
        errorOptions.put(AmqpClientOptions.DESTINATION, ActiveMQLifecycleSettings.getInstance().getString(ActiveMQLifecycleSettingsKey.ERROR_DESTINATION));
        errorOptions.put(AmqpClientOptions.CONNECT_TIMEOUT, ActiveMQLifecycleSettings.getInstance().getInt(ActiveMQLifecycleSettingsKey.CONNECT_TIMEOUT));
        errorOptions.put(AmqpClientOptions.MAXIMUM_RECONNECTION_ATTEMPTS, ActiveMQLifecycleSettings.getInstance().getInt(ActiveMQLifecycleSettingsKey.MAX_RECONNECTION_ATTEMPTS));
        errorOptions.put(AmqpClientOptions.IDLE_TIMEOUT, ActiveMQLifecycleSettings.getInstance().getInt(ActiveMQLifecycleSettingsKey.IDLE_TIMEOUT));
        errorOptions.put(AmqpClientOptions.AUTO_ACCEPT, false);
        errorOptions.put(AmqpClientOptions.PREFETCH_MESSAGES, ActiveMQLifecycleSettings.getInstance().getInt(ActiveMQLifecycleSettingsKey.TELEMETRY_PREFETCH_MESSAGES));
        errorOptions.put(AmqpClientOptions.QOS, ProtonQoS.AT_LEAST_ONCE);
        processorMap = new HashMap<>();
        errorProcessorMap = new HashMap<>();
    }

    protected void initializeProcessors() {
        //disable Vertx BlockedThreadChecker log
        java.util.logging.Logger.getLogger("io.vertx.core.impl.BlockedThreadChecker").setLevel(Level.OFF);
        XmlUtil.setContextProvider(new JAXBContextProvider());
        logger.info("Starting Lifecycle Consumer...");
        logger.info("Starting Lifecycle Consumer... Starting KuraPayloadProtoConverter");
        converter = new KuraPayloadProtoConverter();
        logger.info("Starting Lifecycle Consumer... Starting LifecycleProcessor");
        processor = LifecycleProcessor.getProcessorWithNoFilter();
        logger.info("Starting Lifecycle Consumer... Starting ErrorProcessor");
        errorProcessor = ErrorProcessor.getProcessorWithNoFilter(vertx, errorOptions);
        logger.info("Starting Lifecycle Consumer... Starting AmqpActiveMQConnector");
        processorMap.put(PROCESSOR_NAME_LIFECYCLE, processor);
        errorProcessorMap.put(PROCESSOR_NAME_LIFECYCLE, errorProcessor);
    }

    @Override
    protected void internalStart(Future<Void> future) throws Exception {
        initializeProcessors();
        connector = new AmqpTransportActiveMQConnector(vertx, connectorOptions, converter, processorMap, errorProcessorMap) {

            @Override
            protected boolean isProcessDestination(MessageContext<byte[]> message) {
                String topic = (String) message.getProperties().get(Properties.MESSAGE_DESTINATION);
                if (topic!=null && (topic.endsWith("/MQTT/BIRTH") ||
                        topic.endsWith("/MQTT/DC") ||
                        topic.endsWith("/MQTT/LWT") ||
                        topic.endsWith("/MQTT/MISSING"))
                        ) {
                    return true;
                }
                else {
                    return false;
                }
            }

        };
        httpRestServer.registerHealthCheckProvider(handler -> {
            handler.register(HEALTH_NAME_CONNECTOR, hcm -> {
                if (connector.getStatus().isOk()) {
                    hcm.complete(Status.OK());
                }
                else {
                    hcm.complete(Status.KO());
                }
            });
        });
        httpRestServer.registerHealthCheckProvider(handler -> {
            handler.register(HEALTH_NAME_LIFECYCLE, hcm -> {
                if (processor.getStatus().isOk()) {
                    hcm.complete(Status.OK());
                }
                else {
                    hcm.complete(Status.KO());
                }
            });
        });
        httpRestServer.registerHealthCheckProvider(handler -> {
            handler.register(HEALTH_NAME_ERROR, hcm -> {
                if (errorProcessor.getStatus().isOk()) {
                    hcm.complete(Status.OK());
                }
                else {
                    hcm.complete(Status.KO());
                }
            });
        });
        vertx.deployVerticle(connector, ar -> {
            if (ar.succeeded()) {
                future.complete();
            }
            else {
                future.fail(ar.cause());
            }
        });
        vertx.deployVerticle(httpRestServer);
        logger.info("Starting Lifecycle Consumer... DONE");
    }

    @Override
    protected void internalStop(Future<Void> future) throws Exception {
        //do nothing
        logger.info("Closing Lifecycle Consumer...");
        future.complete();
        logger.info("Closing Lifecycle Consumer... DONE");
        //this stop call is no more needed since the connector is a verticle then is already stopped during the vertx.stop call
        //connector.stop(future);
    }

}