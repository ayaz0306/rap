/*******************************************************************************
 * Copyright (c) 2007, 2011 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

import org.eclipse.rwt.internal.theme.*;
import org.eclipse.rwt.internal.theme.WidgetMatcher.Constraint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;


  protected void configureMatcher( WidgetMatcher matcher ) {
    super.configureMatcher( matcher );
    matcher.addStyle( "TITLE", SWT.TITLE );
    matcher.addState( "maximized", new Constraint() {

      public boolean matches( Widget widget ) {
        return ( ( Shell )widget ).getMaximized();
      }
    } );
  }

    Rectangle result;
    if( ( shell.getStyle() & SWT.TITLE ) != 0 ) {
      result = getCssBoxDimensions( "Shell-Titlebar", "margin", shell );
    } else {
      result = getCssDimension( "Shell-Titlebar", "height", shell );
    }