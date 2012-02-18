/*******************************************************************************
 * Copyright (c) 2010, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying material
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.ui.internal.application;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.rwt.lifecycle.IEntryPoint;


public final class EntryPointApplicationWrapper implements IEntryPoint {

  private static final IApplicationContext context = new RAPApplicationContext();

  private final Class<? extends IApplication> applicationClass;

  public EntryPointApplicationWrapper( Class<? extends IApplication> applicationClass ) {
    this.applicationClass = applicationClass;
  }

  // TODO [bm] implement restart, see IApplication constants
  public int createUI() {
    Object exitCode;
    int result = 0;
    IApplication application = createApplication();
    try {
      exitCode = application.start( context );
      if( exitCode instanceof Integer ) {
        result = ( ( Integer )exitCode ).intValue();
      }
    } catch( Exception exception  ) {
      throw new RuntimeException( "Failed to run application", exception );
    } finally {
      application.stop();
    }
    return result;
  }

  private IApplication createApplication() {
    IApplication application;
    try {
      application = applicationClass.newInstance();
    } catch( Exception exception ) {
      String message = "Failed to create application " + applicationClass.getName();
      throw new IllegalArgumentException( message, exception );
    }
    return application;
  }
}
