/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.widgets;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.rap.rwt.internal.events.IEventAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.DisposedWidgets;
import org.eclipse.rap.rwt.lifecycle.IWidgetAdapter;
import org.eclipse.rap.rwt.lifecycle.PhaseId;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.internal.events.EventList;
import org.eclipse.swt.internal.events.EventTable;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;


public class Widget_Test extends TestCase {

  private Display display;
  private Shell shell;

  protected void setUp() throws Exception {
    Fixture.setUp();
    Fixture.fakePhase( PhaseId.PROCESS_ACTION );
    display = new Display();
    shell = new Shell( display );
  }

  protected void tearDown() throws Exception {
    Fixture.tearDown();
  }
  
  public void testGetAdapterForDisposedWidget() {
    shell.dispose();
    Object adapterOfDisposedWidget = shell.getAdapter( IWidgetAdapter.class );
    assertNotNull( adapterOfDisposedWidget );
  }
  
  public void testGetEventAdapter() {
    IEventAdapter eventAdapter = shell.getAdapter( IEventAdapter.class );
    
    assertNotNull( eventAdapter );
  }

  public void testGetEventAdapterReturnsSame() {
    IEventAdapter eventAdapter1 = shell.getAdapter( IEventAdapter.class );
    IEventAdapter eventAdapter2 = shell.getAdapter( IEventAdapter.class );
    
    assertNotNull( eventAdapter1 );
    assertSame( eventAdapter1, eventAdapter2 );
  }
  
  public void testGetEventTableAdapter() {
    EventTable eventTable = shell.getAdapter( EventTable.class );
    
    assertNotNull( eventTable );
  }

  public void testGetEventTableAdapterWithDefaultConstructor() {
    Widget widget = new Widget() { };
    
    EventTable eventTable = widget.getAdapter( EventTable.class );
    
    assertNotNull( eventTable );
  }

  public void testGetEventTableAdapterReturnsSame() {
    EventTable eventTable1 = shell.getAdapter( EventTable.class );
    EventTable eventTable2 = shell.getAdapter( EventTable.class );
    
    assertSame( eventTable1, eventTable2 );
  }
  
  public void testCheckWidget() throws Throwable {
    final Widget widget = new Text( shell, SWT.NONE );
    Runnable target = new Runnable() {
      public void run() {
        widget.checkWidget();
      }
    };
    try {
      Fixture.runInThread( target );
      fail( "Illegal thread access expected." );
    } catch( SWTException swte ) {
      assertEquals( SWT.ERROR_THREAD_INVALID_ACCESS, swte.code );
    }
  }

  public void testData() {
    Widget widget = new Text( shell, SWT.NONE );

    // Test initial state
    assertEquals( null, widget.getData() );

    Object singleData = new Object();
    // Set/get some single data
    widget.setData( singleData );
    assertSame( singleData, widget.getData() );

    // Set/get some keyed data, ensure that single data remains unchanged
    Object keyedData = new Object();
    widget.setData( "key", keyedData );
    widget.setData( "null-key", null );
    assertSame( singleData, widget.getData() );
    assertSame( keyedData, widget.getData( "key" ) );
    assertSame( null, widget.getData( "null-key" ) );

    // Test 'deleting' a key
    widget.setData( "key", null );
    assertNull( widget.getData( "key" ) );

    // Test keyed data with non-existing key
    assertNull( widget.getData( "non-existing-key" ) );

    // Test keyed data with illegal arguments
    try {
      widget.setData( null, new Object() );
      fail( "Must not allow to set data with null key" );
    } catch( IllegalArgumentException e ) {
      // expected
    }
    try {
      widget.getData( null );
      fail( "Must not allow to get data for null key" );
    } catch( IllegalArgumentException expected ) {
    }
  }

  public void testDisposeParentWhileInDispose() {
    // This test leads to a stack overflow or, if line "item[ 0 ].dispose();"
    // is activated to a NPE
    final Composite composite = new Composite( shell, SWT.NONE );
    ToolBar toolbar = new ToolBar( composite, SWT.NONE );
    final ToolItem[] item = { null };
    toolbar.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        item[ 0 ].dispose();
      }
    } );
    toolbar.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        composite.dispose();
      }
    } );
    item[ 0 ] = new ToolItem( toolbar, SWT.PUSH );
    shell.dispose();
    // no assert: this test ensures that no StackOverflowError occurs
  }

  public void testDisposeSelfWhileInDispose() {
    shell.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        shell.dispose();
      }
    } );
    shell.dispose();
    // no assert: this test ensures that no exception occurs
  }

  public void testDisposeSelfWhileInDispose_RenderOnce() {
    Fixture.markInitialized( shell );
    shell.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        shell.dispose();
      }
    } );
    shell.dispose();
    int counter = 0;
    Widget[] disposedWidgets = DisposedWidgets.getAll();
    for( int i = 0; i < disposedWidgets.length; i++ ) {
      if( disposedWidgets[ i ] == shell ) {
        counter++;
      }
    }
    assertEquals( 1, counter );
  }

  public void testCheckBits() {
    int style = SWT.VERTICAL | SWT.HORIZONTAL;
    int result = Widget.checkBits( style, SWT.VERTICAL, SWT.HORIZONTAL, 0, 0, 0, 0 );
    assertTrue( ( result & SWT.VERTICAL ) != 0 );
    assertFalse( ( result & SWT.HORIZONTAL ) != 0 );
  }

  public void testDispose() {
    Widget widget = new Button( shell, SWT.NONE );

    // Ensure initial state
    assertEquals( false, widget.isDisposed() );

    // Test dispose the first time
    widget.dispose();
    assertEquals( true, widget.isDisposed() );

    // Disposing of an already disposed of widget does nothing
    widget.dispose();
    assertEquals( true, widget.isDisposed() );
  }

  public void testDisposeFromIllegalThread() throws Throwable {
    final Widget widget = new Button( shell, SWT.NONE );
    Runnable runnable = new Runnable() {
      public void run() {
        widget.dispose();
      }
    };
    try {
      Fixture.runInThread( runnable );
      fail( "Must not allow to dispose of a widget from a non-UI-thread" );
    } catch( SWTException expected ) {
    }
  }

  public void testDisposeWithException() {
    shell.addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        throw new RuntimeException();
      }
    } );
    try {
      shell.dispose();
      fail( "Wrong test setup: dispose listener must throw exception" );
    } catch( Exception e ) {
      // expected
    }
    assertFalse( shell.isDisposed() );
    assertEquals( 0, DisposedWidgets.getAll().length );
  }
  
  public void testAddDisposeListener() {
    shell.addDisposeListener( mock( DisposeListener.class ) );
    
    assertTrue( shell.isListening( SWT.Dispose ) );
  }

  public void testAddDisposeListenerWithNullArgument() {
    try {
      shell.addDisposeListener( null );
      fail();
    } catch( IllegalArgumentException expected ) {
    }
  }
  
  public void testRemoveDisposeListenerWithRegisteredListener() {
    DisposeListener listener = mock( DisposeListener.class );
    shell.addDisposeListener( listener );
    
    shell.removeDisposeListener( listener );

    assertFalse( shell.isListening( SWT.Dispose ) );
  }
  
  public void testRemoveDisposeListenerWithUnregisteredListener() {
    DisposeListener listener = mock( DisposeListener.class );
    
    shell.removeDisposeListener( listener );
    
    assertFalse( shell.isListening( SWT.Dispose ) );
  }
  
  public void testRemoveListener() {
    // Ensure that removing a listener that was never added is ignored
    // silently see https://bugs.eclipse.org/251816
    shell.removeListener( SWT.Activate, mock( Listener.class ) );
  }

  // bug 328043
  public void testUntypedDisposeListener() {
    DisposeListener listener = mock( DisposeListener.class );
    shell.addDisposeListener( listener );

    shell.notifyListeners( SWT.Dispose, new Event() );

    verify( listener ).widgetDisposed( any( DisposeEvent.class ) );
  }

  public void testNotifyListeners() {
    final StringBuilder log = new StringBuilder();
    shell.addListener( SWT.Resize, new Listener() {
      public void handleEvent( Event event ) {
        log.append( "untyped" );
      }
    } );
    shell.notifyListeners( SWT.Resize, new Event() );
    assertEquals( "untyped", log.toString() );
  }

  public void testNotifyListenersTyped() {
    ControlListener listener = mock( ControlListener.class );
    shell.addControlListener( listener );
    
    shell.notifyListeners( SWT.Resize, new Event() );
    
    verify( listener ).controlResized( any( ControlEvent.class ) );
    verify( listener, never() ).controlMoved( any( ControlEvent.class ) );
  }

  public void testNotifyListenersWithFilter() {
    Listener filter = mock( Listener.class );
    display.addFilter( SWT.Resize, filter );
    Listener listener = mock( Listener.class );
    shell.addListener( SWT.Resize, listener );
    
    shell.notifyListeners( SWT.Resize, new Event() );

    InOrder inOrder = inOrder( filter, listener );
    inOrder.verify( filter ).handleEvent( any( Event.class ) );
    inOrder.verify( listener ).handleEvent( any( Event.class ) );
  }
  
  public void testNotifyListenersWithDenyingFilter() {
    Listener filter = spy( new Listener() {
      public void handleEvent( Event event ) {
        event.type = SWT.None;
      }
    } );
    display.addFilter( SWT.Resize, filter );
    Listener listener = mock( Listener.class );
    shell.addListener( SWT.Resize, listener );
    
    shell.notifyListeners( SWT.Resize, new Event() );

    verify( filter ).handleEvent( any( Event.class ) );
    verify( listener, never() ).handleEvent( any( Event.class ) );
  }

  // SWT always overrides e.type, e.display and e.widget
  public void testNotifyListenersEventFields() {
    final StringBuilder log = new StringBuilder();
    display.addFilter( SWT.Resize, new Listener() {
      public void handleEvent( Event event ) {
        assertEquals( 2, event.button );
        assertEquals( 'a', event.character );
        assertEquals( 4, event.count );
        assertNotNull( event.data );
        assertEquals( 6, event.detail );
        assertSame( display, event.display );
        assertEquals( false, event.doit );
        assertEquals( 8, event.end );
        assertEquals( 10, event.height );
        assertEquals( 12, event.index );
        assertEquals( shell, event.item );
        assertEquals( 14, event.keyCode );
        assertEquals( 16, event.start );
        assertEquals( 18, event.stateMask );
        assertEquals( "foo", event.text );
        assertEquals( 20, event.width );
        assertEquals( 22, event.x );
        assertEquals( 24, event.y );
        assertEquals( SWT.Resize, event.type );
        assertEquals( shell, event.widget );
        assertTrue( event.time > 0 );
        log.append( "filter" );
      }
    });

    Event event = new Event();
    event.button = 2;
    event.character = 'a';
    event.count = 4;
    event.data = new Object();
    event.detail = 6;
    event.display = null;
    event.doit = false;
    event.end = 8;
    event.height = 10;
    event.index = 12;
    event.item = shell;
    event.keyCode = 14;
    event.start = 16;
    event.stateMask = 18;
    event.text = "foo";
    event.type = SWT.MouseDoubleClick;
    event.widget = shell;
    event.width = 20;
    event.x = 22;
    event.y = 24;

    shell.notifyListeners( SWT.Resize, event );
    assertEquals( "filter", log.toString() );
  }
  
  public void testNotifyListenersWithEmptyEvent() {
    Event event = new Event();
    Listener listener = mock( Listener.class );
    shell.addListener( SWT.Resize, listener );
    
    shell.notifyListeners( SWT.Resize, event );
    
    ArgumentCaptor<Event> captor = ArgumentCaptor.forClass( Event.class );
    verify( listener ).handleEvent( captor.capture() );
    assertSame( event, captor.getValue() );
    assertEquals( shell.getDisplay(), event.display );
    assertEquals( shell, event.widget );
    assertEquals( SWT.Resize, event.type );
    assertTrue( event.time > 0 );
  }

  public void testNotifyListenersNullEvent() {
    final StringBuilder log = new StringBuilder();
    shell.addControlListener( new ControlAdapter() {
      public void controlResized( ControlEvent event ) {
        assertSame( shell, event.widget );
        assertSame( display, event.display );
        log.append( "typed" );
      }
    } );
    shell.notifyListeners( SWT.Resize, null );
    assertEquals( "typed", log.toString() );
  }

  public void testNotifyListenersInvalidEventType() {
    Listener listener = mock( Listener.class );
    shell.addListener( SWT.Resize, listener );

    shell.notifyListeners( 4711, new Event() );

    verify( listener, never() ).handleEvent( any( Event.class ) );
  }
  
  public void testNotifyListenersInReadDataPhase() {
    Fixture.fakePhase( PhaseId.READ_DATA );
    Listener listener = mock( Listener.class );
    shell.addListener( SWT.Resize, listener );
    
    Event event = new Event();
    shell.notifyListeners( SWT.Resize, event );
    
    verify( listener, never() ).handleEvent( any( Event.class ) );
    assertEquals( 1, EventList.getInstance().getAll().length );
    assertEquals( event, EventList.getInstance().getAll()[ 0 ] );
  }

  public void testNotifyListenersWithNullPhase() {
    Fixture.fakePhase( null );
    Listener listener = mock( Listener.class );
    shell.addListener( SWT.Resize, listener );
    
    shell.notifyListeners( SWT.Resize, new Event() );
    
    verify( listener, never() ).handleEvent( any( Event.class ) );
  }
  
  public void testNotifyListenersWithPreInitializedTime() {
    int predefinedTime = 12345;
    Listener listener = mock( Listener.class );
    shell.addListener( SWT.Resize, listener );
    
    Event event = new Event();
    event.time = predefinedTime;
    shell.notifyListeners( SWT.Resize, event );
    
    ArgumentCaptor<Event> captor = ArgumentCaptor.forClass( Event.class );
    verify( listener ).handleEvent( captor.capture() );
    assertEquals( predefinedTime, captor.getValue().time );
  }
  
  public void testGetListeners() {
    Listener[] listeners = shell.getListeners( 0 );
    assertNotNull( listeners );
    assertEquals( 0, listeners.length );
    Listener dummyListener = new Listener() {
      public void handleEvent( Event event ) {
      }
    };
    Listener dummyListener2 = new Listener() {
      public void handleEvent( Event event ) {
      }
    };
    shell.addListener( SWT.Resize, dummyListener );
    assertEquals( 0, shell.getListeners( SWT.Move ).length );
    assertEquals( 1, shell.getListeners( SWT.Resize ).length );
    assertSame( dummyListener, shell.getListeners( SWT.Resize )[0] );
    shell.addListener( SWT.Resize, dummyListener2 );
    assertEquals( 2, shell.getListeners( SWT.Resize ).length );
  }

  public void testIsListeningWithoutRegisteredListeners() {
    boolean listening = shell.isListening( SWT.Dispose );
    
    assertFalse( listening );
  }
  
  public void testIsListeningAfterAddListener() {
    Listener listener = mock( Listener.class ); 
    
    shell.addListener( SWT.Resize, listener );
    
    assertTrue( shell.isListening( SWT.Resize ) );
  }

  public void testIsListeningAfterRemoveListener() {
    Listener listener = mock( Listener.class ); 
    shell.addListener( SWT.Resize, listener );
    
    shell.removeListener( SWT.Resize, listener );
    
    assertFalse( shell.isListening( SWT.Resize ) );
  }
  
  public void testIsListeningForTypedEvent() {
    shell.addHelpListener( new HelpListener() {
      public void helpRequested( HelpEvent event ) {
      }
    } );
    assertTrue( shell.isListening( SWT.Help ) );
  }

  public void testGetDisplay() {
    assertSame( display, shell.getDisplay() );
  }

  public void testGetDisplayFromNonUIThread() throws Exception {
    final Display[] widgetDisplay = { null };
    Thread thread = new Thread( new Runnable() {
      public void run() {
        widgetDisplay[ 0 ] = shell.getDisplay();
      }
    } );
    thread.start();
    thread.join();
    assertSame( display, widgetDisplay[ 0 ] );
  }

  public void testReskin() {
    final java.util.List<Widget> log = new ArrayList<Widget>();
    Listener listener = new Listener() {
      public void handleEvent( Event event ) {
        if( event.type == SWT.Skin ) {
          log.add( event.widget );
        }
      }
    };
    display.addListener( SWT.Skin, listener );
    Composite child1 = new Composite( shell, SWT.NONE );
    Label subchild1 = new Label( child1, SWT.NONE );
    Composite child2 = new Composite( shell, SWT.NONE );
    Label subchild2 = new Label( child2, SWT.NONE );
    Composite child3 = new Composite( shell, SWT.NONE );
    Label subchild3 = new Label( child3, SWT.NONE );
    shell.reskin( SWT.ALL );
    display.readAndDispatch();
    assertEquals( 7, log.size() );
    assertSame( shell, log.get( 0 ) );
    assertSame( child1, log.get( 1 ) );
    assertSame( subchild1, log.get( 2 ) );
    assertSame( child2, log.get( 3 ) );
    assertSame( subchild2, log.get( 4 ) );
    assertSame( child3, log.get( 5 ) );
    assertSame( subchild3, log.get( 6 ) );
    log.clear();
    shell.setData( SWT.SKIN_CLASS, "skin" );
    display.readAndDispatch();
    assertEquals( 7, log.size() );
    assertSame( shell, log.get( 0 ) );
    assertSame( child1, log.get( 1 ) );
    assertSame( subchild1, log.get( 2 ) );
    assertSame( child2, log.get( 3 ) );
    assertSame( subchild2, log.get( 4 ) );
    assertSame( child3, log.get( 5 ) );
    assertSame( subchild3, log.get( 6 ) );
    log.clear();
    shell.setData( SWT.SKIN_ID, "skin" );
    display.readAndDispatch();
    assertEquals( 7, log.size() );
    assertSame( shell, log.get( 0 ) );
    assertSame( child1, log.get( 1 ) );
    assertSame( subchild1, log.get( 2 ) );
    assertSame( child2, log.get( 3 ) );
    assertSame( subchild2, log.get( 4 ) );
    assertSame( child3, log.get( 5 ) );
    assertSame( subchild3, log.get( 6 ) );
    log.clear();
    child3.reskin( SWT.ALL );
    display.readAndDispatch();
    assertEquals( 2, log.size() );
    assertSame( child3, log.get( 0 ) );
    assertSame( subchild3, log.get( 1 ) );
    log.clear();
    child2.reskin( SWT.NONE );
    display.readAndDispatch();
    assertEquals( 1, log.size() );
    assertSame( child2, log.get( 0 ) );
    log.clear();
    display.removeListener( SWT.Skin, listener );
    shell.reskin( SWT.ALL );
    display.readAndDispatch();
    assertEquals( 0, log.size() );
  }
}
