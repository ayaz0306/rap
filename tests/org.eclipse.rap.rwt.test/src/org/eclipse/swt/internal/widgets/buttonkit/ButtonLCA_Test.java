/*******************************************************************************
 * Copyright (c) 2002, 2013 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.buttonkit;

import static org.eclipse.rap.rwt.internal.protocol.ProtocolTestUtil.jsonEquals;
import static org.eclipse.rap.rwt.lifecycle.WidgetUtil.getId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.rwt.internal.json.JsonArray;
import org.eclipse.rap.rwt.internal.json.JsonObject;
import org.eclipse.rap.rwt.internal.protocol.ClientMessageConst;
import org.eclipse.rap.rwt.lifecycle.WidgetAdapter;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rap.rwt.testfixture.Message;
import org.eclipse.rap.rwt.testfixture.Message.CreateOperation;
import org.eclipse.rap.rwt.testfixture.internal.TestUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.graphics.ImageFactory;
import org.eclipse.swt.internal.widgets.IShellAdapter;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.internal.widgets.controlkit.ControlLCATestUtil;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


// TODO [rst] Split into different test classes for button types
public class ButtonLCA_Test {

  private static final String PROP_SELECTION_LISTENER = "listener_Selection";

  private Display display;
  private Shell shell;
  private Button button;
  private ButtonLCA lca;

  @Before
  public void setUp() {
    Fixture.setUp();
    display = new Display();
    shell = new Shell( display );
    button = new Button( shell, SWT.PUSH );
    lca = new ButtonLCA();
    Fixture.fakeNewRequest();
  }

  @After
  public void tearDown() {
    Fixture.tearDown();
  }

  @Test
  public void testControlListeners() throws IOException {
    button = new Button( shell, SWT.NONE );
    ControlLCATestUtil.testActivateListener( button );
    ControlLCATestUtil.testFocusListener( button );
    ControlLCATestUtil.testMouseListener( button );
    ControlLCATestUtil.testKeyListener( button );
    ControlLCATestUtil.testTraverseListener( button );
    ControlLCATestUtil.testMenuDetectListener( button );
    ControlLCATestUtil.testHelpListener( button );
  }

  @Test
  public void testRadioPreserveValues() throws IOException {
    button = new Button( shell, SWT.RADIO );
    Fixture.markInitialized( display );
    testPreserveValues( display, button );
    button.setSelection( true );
    Fixture.preserveWidgets();
    WidgetAdapter adapter = WidgetUtil.getAdapter( button );
    assertEquals( Boolean.TRUE, adapter.getPreserved( "selection" ) );
  }

  @Test
  public void testCheckPreserveValues() throws IOException {
    button = new Button( shell, SWT.CHECK );
    Fixture.markInitialized( display );
    testPreserveValues( display, button );
    button.setSelection( true );
    button.setGrayed( true );
    Fixture.preserveWidgets();
    WidgetAdapter adapter = WidgetUtil.getAdapter( button );
    assertEquals( Boolean.TRUE, adapter.getPreserved( "selection" ) );
    assertEquals( Boolean.TRUE, adapter.getPreserved( "grayed" ) );
  }

  @Test
  public void testTogglePreserveValues() throws IOException {
    button = new Button( shell, SWT.TOGGLE );
    Fixture.markInitialized( display );
    testPreserveValues( display, button );
    button.setSelection( true );
    Fixture.preserveWidgets();
    WidgetAdapter adapter = WidgetUtil.getAdapter( button );
    assertEquals( Boolean.TRUE, adapter.getPreserved( "selection" ) );
  }

  private void testPreserveValues( Display display, Button button ) throws IOException {
    Boolean hasListeners;
    // Text,Image
    WidgetAdapter adapter = WidgetUtil.getAdapter( button );
    if( ( button.getStyle() & SWT.ARROW ) == 0 ) {
      button.setText( "abc" );
      Fixture.preserveWidgets();
      adapter = WidgetUtil.getAdapter( button );
      Object object = adapter.getPreserved( Props.TEXT );
      assertEquals( "abc", object );
      Fixture.clearPreserved();
      Image image = TestUtil.createImage( display, Fixture.IMAGE1 );
      button.setImage( image );
      Fixture.preserveWidgets();
      adapter = WidgetUtil.getAdapter( button );
      assertSame( image, adapter.getPreserved( Props.IMAGE ) );
      Fixture.clearPreserved();
    }
    //Selection_Listener
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    hasListeners = ( Boolean )adapter.getPreserved( PROP_SELECTION_LISTENER );
    assertEquals( Boolean.FALSE, hasListeners );
    Fixture.clearPreserved();
    SelectionListener selectionListener = new SelectionAdapter() {
    };
    button.addSelectionListener( selectionListener );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    hasListeners = ( Boolean )adapter.getPreserved( PROP_SELECTION_LISTENER );
    assertEquals( Boolean.TRUE, hasListeners );
    Fixture.clearPreserved();
    //bound
    Rectangle rectangle = new Rectangle( 10, 10, 10, 10 );
    button.setBounds( rectangle );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    assertEquals( rectangle, adapter.getPreserved( Props.BOUNDS ) );
    Fixture.clearPreserved();
    //menu
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    assertEquals( null, adapter.getPreserved( Props.MENU ) );
    Fixture.clearPreserved();
    Menu menu = new Menu( button );
    MenuItem item = new MenuItem( menu, SWT.NONE );
    item.setText( "1 Item" );
    button.setMenu( menu );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    assertEquals( menu, adapter.getPreserved( Props.MENU ) );
    //visible
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    assertEquals( Boolean.TRUE, adapter.getPreserved( Props.VISIBLE ) );
    Fixture.clearPreserved();
    button.setVisible( false );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    assertEquals( Boolean.FALSE, adapter.getPreserved( Props.VISIBLE ) );
    Fixture.clearPreserved();
    //enabled
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    assertEquals( Boolean.TRUE, adapter.getPreserved( Props.ENABLED ) );
    Fixture.clearPreserved();
    button.setEnabled( false );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    assertEquals( Boolean.FALSE, adapter.getPreserved( Props.ENABLED ) );
    Fixture.clearPreserved();
    button.setEnabled( true );
    // foreground background font
    Color background = new Color( display, 122, 33, 203 );
    button.setBackground( background );
    Color foreground = new Color( display, 211, 178, 211 );
    button.setForeground( foreground );
    Font font = new Font( display, "font", 12, SWT.BOLD );
    button.setFont( font );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    assertEquals( background, adapter.getPreserved( Props.BACKGROUND ) );
    assertEquals( foreground, adapter.getPreserved( Props.FOREGROUND ) );
    assertEquals( font, adapter.getPreserved( Props.FONT ) );
    Fixture.clearPreserved();
    //tooltiptext
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    assertEquals( null, button.getToolTipText() );
    Fixture.clearPreserved();
    button.setToolTipText( "some text" );
    Fixture.preserveWidgets();
    adapter = WidgetUtil.getAdapter( button );
    assertEquals( "some text", button.getToolTipText() );
    Fixture.clearPreserved();
  }

  @Test
  public void testDisabledButtonSelection() {
    button = new Button( shell, SWT.NONE );
    Label label = new Label( shell, SWT.NONE );
    button.addListener( SWT.Activate, new Listener() {
      public void handleEvent( Event event ) {
        button.setEnabled( false );
      }
    } );
    SelectionListener listener = mock( SelectionListener.class );
    button.addSelectionListener( listener );
    shell.getAdapter( IShellAdapter.class ).setActiveControl( label );
    Fixture.fakeNewRequest();
    Fixture.fakeNotifyOperation( getId( button ), ClientMessageConst.EVENT_SELECTION, null );
    fakeActiveControl( button );

    Fixture.readDataAndProcessAction( display );

    assertFalse( button.getEnabled() );
    verify( listener, times( 0 ) ).widgetSelected( any( SelectionEvent.class ) );
  }

  @Test
  public void testSelectionEvent() {
    SelectionListener listener = mock( SelectionListener.class );
    button.addSelectionListener( listener );

    Fixture.fakeNotifyOperation( getId( button ), ClientMessageConst.EVENT_SELECTION, null );
    Fixture.readDataAndProcessAction( button );

    verify( listener, times( 1 ) ).widgetSelected( any( SelectionEvent.class ) );
  }

  @Test
  public void testRadioSelectionEvent() {
    button = new Button( shell, SWT.RADIO );
    SelectionListener listener = mock( SelectionListener.class );
    button.addSelectionListener( listener );

    Fixture.fakeSetParameter( getId( button ), "selection", Boolean.TRUE );
    Fixture.fakeNotifyOperation( getId( button ), ClientMessageConst.EVENT_SELECTION, null );
    Fixture.readDataAndProcessAction( button );

    assertTrue( button.getSelection() );
    verify( listener ).widgetSelected( any( SelectionEvent.class ) );
  }

  // https://bugs.eclipse.org/bugs/show_bug.cgi?id=224872
  @Test
  public void testRadioDeselectionEvent() {
    button = new Button( shell, SWT.RADIO );
    button.setSelection( true );
    SelectionListener listener = mock( SelectionListener.class );
    button.addSelectionListener( listener );

    Fixture.fakeSetParameter( getId( button ), "selection", Boolean.FALSE );
    Fixture.fakeNotifyOperation( getId( button ), ClientMessageConst.EVENT_SELECTION, null );
    Fixture.readDataAndProcessAction( button );

    assertFalse( button.getSelection() );
    verify( listener ).widgetSelected( any( SelectionEvent.class ) );
  }

  @Test
  public void testRadioTypedSelectionEventOrder_TypedListener() {
    final List<Widget> log = new ArrayList<Widget>();
    Button button1 = new Button( shell, SWT.RADIO );
    Button button2 = new Button( shell, SWT.RADIO );
    button2.setSelection( true );
    SelectionAdapter listener = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        log.add( event.widget );
      }
    };
    button1.addSelectionListener( listener );
    button2.addSelectionListener( listener );

    Fixture.fakeSetParameter( getId( button1 ), "selection", Boolean.TRUE );
    Fixture.fakeSetParameter( getId( button2 ), "selection", Boolean.FALSE );
    Fixture.fakeNotifyOperation( getId( button1 ), ClientMessageConst.EVENT_SELECTION, null );
    Fixture.fakeNotifyOperation( getId( button2 ), ClientMessageConst.EVENT_SELECTION, null );
    Fixture.readDataAndProcessAction( display );

    assertTrue( Arrays.equals( new Widget[]{ button2, button1 }, log.toArray() ) );
  }

  @Test
  public void testRadioTypedSelectionEventOrder_UntypedListener() {
    final List<Widget> log = new ArrayList<Widget>();
    Button button1 = new Button( shell, SWT.RADIO );
    Button button2 = new Button( shell, SWT.RADIO );
    button2.setSelection( true );
    Listener listener = new Listener() {
      public void handleEvent( Event event ) {
        log.add( event.widget );
      }
    };
    button1.addListener( SWT.Selection, listener );
    button2.addListener( SWT.Selection, listener );

    Fixture.fakeSetParameter( getId( button1 ), "selection", Boolean.TRUE );
    Fixture.fakeSetParameter( getId( button2 ), "selection", Boolean.FALSE );
    Fixture.fakeNotifyOperation( getId( button1 ), ClientMessageConst.EVENT_SELECTION, null );
    Fixture.fakeNotifyOperation( getId( button2 ), ClientMessageConst.EVENT_SELECTION, null );
    Fixture.readDataAndProcessAction( display );

    assertTrue( Arrays.equals( new Widget[]{ button2, button1 }, log.toArray() ) );
  }

  @Test
  public void testRenderWrap() throws Exception {
    button = new Button( shell, SWT.PUSH | SWT.WRAP );
    Fixture.fakeResponseWriter();
    PushButtonDelegateLCA lca = new PushButtonDelegateLCA();

    lca.renderInitialization( button );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( button );
    Object[] styles = operation.getStyles();
    assertTrue( Arrays.asList( styles ).contains( "WRAP" ) );
  }

  @Test
  public void testRenderCreate() throws IOException {
    Button pushButton = new Button( shell, SWT.PUSH );

    lca.renderInitialization( pushButton );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( pushButton );
    assertEquals( "rwt.widgets.Button", operation.getType() );
    Object[] styles = operation.getStyles();
    assertTrue( Arrays.asList( styles ).contains( "PUSH" ) );
  }

  @Test
  public void testRenderCreateArrow() throws IOException {
    Button pushButton = new Button( shell, SWT.ARROW );

    lca.renderInitialization( pushButton );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( pushButton );
    assertEquals( "rwt.widgets.Button", operation.getType() );
    Object[] styles = operation.getStyles();
    assertTrue( Arrays.asList( styles ).contains( "ARROW" ) );
  }

  @Test
  public void testRenderParent() throws IOException {
    Button pushButton = new Button( shell, SWT.PUSH );

    lca.renderInitialization( pushButton );

    Message message = Fixture.getProtocolMessage();
    CreateOperation operation = message.findCreateOperation( pushButton );
    assertEquals( WidgetUtil.getId( pushButton.getParent() ), operation.getParent() );
  }

  @Test
  public void testRenderInitialText() throws IOException {
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "text" ) );
  }

  @Test
  public void testRenderText() throws IOException {
    button.setText( "test" );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "test", message.findSetProperty( button, "text" ) );
  }

  @Test
  public void testRenderTextWithQuotationMarks() throws IOException {
    button.setText( "te\"s't" );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "te\"s't", message.findSetProperty( button, "text" ) );
  }

  @Test
  public void testRenderTextWithMnemonic() throws IOException {
    button.setText( "te&st" );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "test", message.findSetProperty( button, "text" ) );
  }

  @Test
  public void testRenderTextWithNewlines() throws IOException {
    button.setText( "\ntes\r\nt\n" );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "\ntes\r\nt\n", message.findSetProperty( button, "text" ) );
  }

  @Test
  public void testRenderTextUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( button );

    button.setText( "foo" );
    Fixture.preserveWidgets();
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "text" ) );
  }

  @Test
  public void testRenderInitialAlignment() throws IOException {
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "alignment" ) );
  }

  @Test
  public void testRenderInitialAlignment_Arrow() throws IOException {
    button = new Button( shell, SWT.ARROW );

    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "up", message.findSetProperty( button, "alignment" ) );
  }

  @Test
  public void testRenderAlignment() throws IOException {
    button.setAlignment( SWT.RIGHT );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "right", message.findSetProperty( button, "alignment" ) );
  }

  @Test
  public void testRenderAlignment_Arrow() throws IOException {
    button = new Button( shell, SWT.ARROW | SWT.DOWN );

    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( "down", message.findSetProperty( button, "alignment" ) );
  }

  @Test
  public void testRenderAlignmentUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( button );

    button.setAlignment( SWT.RIGHT );
    Fixture.preserveWidgets();
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "alignment" ) );
  }

  @Test
  public void testRenderAddSelectionListener() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( button );
    Fixture.preserveWidgets();

    button.addSelectionListener( new SelectionAdapter() { } );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findListenProperty( button, "Selection" ) );
  }

  @Test
  public void testRenderRemoveSelectionListener() throws Exception {
    SelectionListener listener = new SelectionAdapter() { };
    button.addSelectionListener( listener );
    Fixture.markInitialized( display );
    Fixture.markInitialized( button );
    Fixture.preserveWidgets();

    button.removeSelectionListener( listener );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.FALSE, message.findListenProperty( button, "Selection" ) );
  }

  @Test
  public void testRenderSelectionListenerUnchanged() throws Exception {
    Fixture.markInitialized( display );
    Fixture.markInitialized( button );
    Fixture.preserveWidgets();

    button.addSelectionListener( new SelectionAdapter() { } );
    Fixture.preserveWidgets();
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findListenOperation( button, "selection" ) );
  }

  @Test
  public void testRenderInitialImage() throws IOException {
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "image" ) );
  }

  @Test
  public void testRenderImage() throws IOException {
    Image image = TestUtil.createImage( display, Fixture.IMAGE_100x50 );

    button.setImage( image );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    String imageLocation = ImageFactory.getImagePath( image );
    String expected = "[\"" + imageLocation + "\", 100, 50 ]";
    JsonArray actual = ( JsonArray )message.findSetProperty( button, "image" );
    assertTrue( jsonEquals( expected, actual ) );
  }

  @Test
  public void testRenderImageUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( button );
    Image image = TestUtil.createImage( display, Fixture.IMAGE_100x50 );

    button.setImage( image );
    Fixture.preserveWidgets();
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "image" ) );
  }

  @Test
  public void testRenderImageReset() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( button );
    Image image = TestUtil.createImage( display, Fixture.IMAGE_100x50 );
    button.setImage( image );

    Fixture.preserveWidgets();
    button.setImage( null );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( JsonObject.NULL, message.findSetProperty( button, "image" ) );
  }

  @Test
  public void testRenderInitialSelection() throws IOException {
    button = new Button( shell, SWT.CHECK );

    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "selection" ) );
  }

  @Test
  public void testRenderSelection() throws IOException {
    button = new Button( shell, SWT.CHECK );

    button.setSelection( true );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findSetProperty( button, "selection" ) );
  }

  @Test
  public void testRenderSelectionUnchanged() throws IOException {
    button = new Button( shell, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( button );

    button.setSelection( true );
    Fixture.preserveWidgets();
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "selection" ) );
  }

  @Test
  public void testRenderInitialGrayed() throws IOException {
    button = new Button( shell, SWT.CHECK );

    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "grayed" ) );
  }

  @Test
  public void testRenderGrayed() throws IOException {
    button = new Button( shell, SWT.CHECK );

    button.setGrayed( true );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Boolean.TRUE, message.findSetProperty( button, "grayed" ) );
  }

  @Test
  public void testRenderGrayedUnchanged() throws IOException {
    button = new Button( shell, SWT.CHECK );
    Fixture.markInitialized( display );
    Fixture.markInitialized( button );

    button.setGrayed( true );
    Fixture.preserveWidgets();
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "grayed" ) );
  }

  @Test
  public void testRenderInitialMnemonicIndex() throws IOException {
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "mnemonicIndex" ) );
  }

  @Test
  public void testRenderMnemonicIndex() throws IOException {
    button.setText( "te&st" );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 2 ), message.findSetProperty( button, "mnemonicIndex" ) );
  }

  @Test
  public void testRenderMnemonic_OnTextChange() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( button );

    button.setText( "te&st" );
    Fixture.preserveWidgets();
    button.setText( "aa&bb" );
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertEquals( Integer.valueOf( 2 ), message.findSetProperty( button, "mnemonicIndex" ) );
  }

  @Test
  public void testRenderMnemonicIndexUnchanged() throws IOException {
    Fixture.markInitialized( display );
    Fixture.markInitialized( button );

    button.setText( "te&st" );
    Fixture.preserveWidgets();
    lca.renderChanges( button );

    Message message = Fixture.getProtocolMessage();
    assertNull( message.findSetOperation( button, "mnemonicIndex" ) );
  }

  private void fakeActiveControl( Control control ) {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put( "activeControl", getId( control ) );
    Fixture.fakeSetOperation( getId( control.getShell() ), properties );
  }

}
