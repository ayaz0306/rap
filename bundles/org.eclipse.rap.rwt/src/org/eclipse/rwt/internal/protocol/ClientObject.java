/*******************************************************************************
* Copyright (c) 2011 EclipseSource and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    EclipseSource - initial API and implementation
*******************************************************************************/
package org.eclipse.rwt.internal.protocol;

import java.util.Map;

import org.eclipse.rwt.internal.service.ContextProvider;

public final class ClientObject implements IClientObject {

  private final String targetId;

  public ClientObject( String targetId ) {
    this.targetId = targetId;
  }

  public void create( String type ) {
    getWriter().appendCreate( targetId, type );
  }

  public void destroy() {
    getWriter().appendDestroy( targetId );
  }

  public void setProperty( String name, int value ) {
    getWriter().appendSet( targetId, name, value );
  }

  public void setProperty( String name, double value ) {
    getWriter().appendSet( targetId, name, value );
  }

  public void setProperty( String name, boolean value ) {
    getWriter().appendSet( targetId, name, value );
  }

  public void setProperty( String name, String value ) {
    getWriter().appendSet( targetId, name, value );
  }

  public void setProperty( String name, int[] value ) {
    getWriter().appendSet( targetId, name, value );
  }

  public void setProperty( String name, Object value ) {
    getWriter().appendSet( targetId, name, value );
  }

  public void addListener( String eventName ) {
    getWriter().appendListen( targetId, eventName, true );
  }

  public void removeListener( String eventName ) {
    getWriter().appendListen( targetId, eventName, false );
  }

  public void call( String method, Map<String, Object> properties ) {
    getWriter().appendCall( targetId, method, properties );
  }

  public void executeScript( String type, String script ) {
    getWriter().appendExecuteScript( targetId, type, script );
  }

  private static ProtocolMessageWriter getWriter() {
    return ContextProvider.getProtocolWriter();
  }

}
