/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.broker.core.converter;

import org.apache.camel.Exchange;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.broker.core.listener.AbstractListener;
import org.eclipse.kapua.broker.core.message.MessageConstants;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.security.KapuaSession;

/**
 * Kapua Camel session filter used to bind/unbind Kapua session to the thread context
 *
 */
public class KapuaCamelFilter extends AbstractListener
{
    public KapuaCamelFilter()
    {
        super("filter");
    }

    /**
     * Bind the Kapua session retrieved from the message header (with key {@link MessageConstants#HEADER_KAPUA_SESSION}) to the current thread context.
     * 
     * @param exchange
     * @param value
     * @throws KapuaException
     */
    public void bindSession(Exchange exchange, Object value) throws KapuaException
    {
        ThreadContext.unbindSubject();
        KapuaSession kapuaSession = exchange.getIn().getHeader(MessageConstants.HEADER_KAPUA_SESSION, KapuaSession.class);
        KapuaSecurityUtils.setSession((KapuaSession) kapuaSession);
    }

    /**
     * Unbind the Kapua session from the current thread context.
     * 
     * @param exchange
     * @param value
     * @throws KapuaException
     */
    public void unbindSession(Exchange exchange, Object value) throws KapuaException
    {
        KapuaSecurityUtils.clearSession();
    }

}
