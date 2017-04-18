/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.device.management.command.internal;

import org.eclipse.kapua.service.device.management.KapuaAppProperties;

/**
 * Device command properties definition.
 * 
 * @since 1.0
 *
 */
public enum CommandAppProperties implements KapuaAppProperties
{
    /**
     * Application name
     */
    APP_NAME("COMMAND"),
    /**
     * Application version
     */
    APP_VERSION("1.0.0"),

    // Request
    /**
     * Command
     */
    APP_PROPERTY_CMD("kapua.cmd.command"),
    /**
     * Arguments
     */
    APP_PROPERTY_ARG("kapua.cmd.arguments"),
    /**
     * Environment
     */
    APP_PROPERTY_ENVP("kapua.cmd.environment.pair"),
    /**
     * Working directory
     */
    APP_PROPERTY_DIR("kapua.cmd.working.directory"),
    /**
     * Standard input
     */
    APP_PROPERTY_STDIN("kapua.cmd.stdin"),
    /**
     * Command timeout
     */
    APP_PROPERTY_TOUT("kapua.cmd.timeout"),
    /**
     * Asynchronous running
     */
    APP_PROPERTY_ASYNC("kapua.cmd.run.async"),
    /**
     * Password
     */
    APP_PROPERTY_PASSWORD("kapua.cmd.password"),

    // Response
    /**
     * Standard error
     */
    APP_PROPERTY_STDERR("kapua.cmd.stderr"),
    /**
     * Standard output
     */
    APP_PROPERTY_STDOUT("kapua.cmd.stdout"),
    /**
     * Command exit code
     */
    APP_PROPERTY_EXIT_CODE("kapua.cmd.exit.code"),
    /**
     * Command timed out flag
     */
    APP_PROPERTY_TIMED_OUT("kapua.cmd.timed.out"),
    ;

    private String value;

    CommandAppProperties(String value)
    {
        this.value = value;
    }

    @Override
    public String getValue()
    {
        return value;
    }
}
