/*
 * FengGUI - Java GUIs in OpenGL (http://www.fenggui.org)
 * 
 * Copyright (c) 2005, 2006 FengGUI Project
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details:
 * http://www.gnu.org/copyleft/lesser.html#TOC3
 * 
 * $Id: TextEditor.java 358 2007-09-21 16:03:59Z marcmenghin $
 */
package org.fenggui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Pattern;

import org.fenggui.event.FocusEvent;
import org.fenggui.event.IDragAndDropListener;
import org.fenggui.event.IFocusListener;
import org.fenggui.event.IKeyPressedListener;
import org.fenggui.event.IKeyReleasedListener;
import org.fenggui.event.IKeyTypedListener;
import org.fenggui.event.ITextChangedListener;
import org.fenggui.event.Key;
import org.fenggui.event.KeyPressedEvent;
import org.fenggui.event.KeyReleasedEvent;
import org.fenggui.event.KeyTypedEvent;
import org.fenggui.event.TextChangedEvent;
import org.fenggui.event.TextCursorMovedEvent;
import org.fenggui.event.mouse.IMouseEnteredListener;
import org.fenggui.event.mouse.IMouseExitedListener;
import org.fenggui.event.mouse.IMousePressedListener;
import org.fenggui.event.mouse.MouseEnteredEvent;
import org.fenggui.event.mouse.MouseExitedEvent;
import org.fenggui.event.mouse.MousePressedEvent;
import org.fenggui.render.Binding;
import org.fenggui.render.Font;
import org.fenggui.theme.xml.IXMLStreamableException;
import org.fenggui.theme.xml.InputOutputStream;
import org.fenggui.util.Dimension;

/**
 * Implementation of a text editor. Text editors come in multiple lines (text area) or single line
 * variants (text field). If it is set to multiline it is able to auto word warp the text.
 * 
 * @author Johannes Schaback, last edited by $Author: marcmenghin $, $Date: 2007-08-11 13:20:15 +0200
 *         (Sa, 11 Aug 2007) $
 * @version $Revision: 358 $
 * @dedication No Use For a Name - Invincible
 */
public class TextEditor extends ObservableWidget implements ITextWidget {
  private ArrayList<ITextChangedListener> textChangedHook = new ArrayList<ITextChangedListener>();

  public static final String              LABEL_DEFAULT   = "default";
  public static final String              LABEL_DISABLED  = "disabled";
  public static final String              LABEL_FOCUSED   = "focused";

  private int                             cursorIndex     = 0;
  private StringBuilder                   text            = new StringBuilder();
  private StringBuilder                   text_prepared   = new StringBuilder();
  private TextEditorAppearance            appearance      = null;
  private Selection                       selection       = new Selection();
  // TODO: use this to correct wrong selection values
  private java.util.List<Integer>         indexChangeList = new ArrayList<Integer>();

  private int                             size            = 20;

  private boolean                         autoWarpText    = false;
  private boolean                         multiline       = true;
  private boolean                         selectOnFocus   = false;

  private boolean                         inWritingState  = false;
  private TextEditorDnDListener           dndListener     = null;
  private boolean                         shiftKeyDown    = false;

  /**
   * Whether the characters should be drawn as *'s
   */
  private boolean                         passwordField   = false;

  /**
   * Define the max number of character that can be added to the TextEditor
   */
  private int                             maxCharacters   = -1;

  /**
   * Define a regularExpression representing allowed characters.
   */
  private Pattern                         restrict        = null;

  /**
   * Define if the regularExpression accepts unicode characters.
   */
  private boolean                         unicodeRestrict = true;

  /**
   * Creates a new text editor.
   * 
   * @param multiline
   *          flag if this text editor shall only has one single line (text field).
   */
  public TextEditor(boolean multiline) {
    this.multiline = multiline;
    appearance = new TextEditorAppearance(this);
    dndListener = new TextEditorDnDListener(this);
    resetSelection();

    buildMouseBehavior();
    buildKeyboardBehavior();

    setupTheme(TextEditor.class);
    updateMinSize();
    setTraversable(true);
  }

  public TextEditor() {
    this(true);
  }

  private int getIndex(int index) {
    int result = index;
    for (int change : this.indexChangeList) {
      if (change < index) {
        result--;
      }
    }

    return result;
  }

  private int getWarpedIndex(int index) {
    int result = index;
    for (int change : this.indexChangeList) {
      if (change < index) {
        result++;
      }
    }

    return result;
  }

  /**
   * Actual selection in the warped text.
   * 
   * @return
   */
  public Selection getSelectionWarped() {
    return selection;
  }

  /**
   * Actual position of cursor in the warped text.
   * 
   * @return
   */
  public int getCursorWarped() {
    return cursorIndex;
  }

  /**
   * Position of cursor in the not auto warped text.
   * 
   * @return
   */
  public int getCursor() {
    return getIndex(cursorIndex);
  }

  /**
   * Selection in the not auto warped text.
   * 
   * @return
   */
  public Selection getSelection() {
    Selection result = new Selection();

    result.startIndex = getIndex(selection.startIndex);
    result.startX = selection.startX;
    result.startY = selection.startY;
    result.endIndex = getIndex(selection.endIndex);
    result.endX = selection.endX;
    result.endY = selection.endY;
    return result;
  }

  public TextEditorAppearance getAppearance() {
    return appearance;
  }

  void buildMouseBehavior() {
    addMousePressedListener(new IMousePressedListener() {

      public void mousePressed(MousePressedEvent mp) {
      // System.out.println("mousePressed "+inWritingState +" "+hasFocus());
      /*
       * if(inWritingState) { setCursorIndex(seekNearestIndex(mp.getDisplayX() - getDisplayX(),
       * mp.getDisplayY() - getDisplayY())); getCursorPainter().resetTimer(); inWritingState =
       * false; resetSelection(); } else { inWritingState = true;
       * setCursorIndex(seekNearestIndex(mp.getDisplayX() - getDisplayX() - getPadding().getLeft(),
       * mp.getDisplayY() - getDisplayY() - getPadding().getBottom()));
       * getCursorPainter().resetTimer(); }
       */
      }
    });

    addMouseEnteredListener(new IMouseEnteredListener() {

      public void mouseEntered(MouseEnteredEvent mouseEnteredEvent) {
        Binding.getInstance().getCursorFactory().getTextCursor().show();
        getDisplay().addDndListener(dndListener);
      }
    });

    addMouseExitedListener(new IMouseExitedListener() {

      public void mouseExited(MouseExitedEvent mouseExited) {
        Binding.getInstance().getCursorFactory().getDefaultCursor().show();
        if (getDisplay() != null)
          getDisplay().removeDndListener(dndListener);
      }
    });

  }

  void buildKeyboardBehavior() {

    addKeyPressedListener(new IKeyPressedListener() {

      public void keyPressed(KeyPressedEvent keyPressedEvent) {
        if (inWritingState)
          handleKeyPressed(keyPressedEvent);
      }

    });

    addKeyTypedListener(new IKeyTypedListener() {

      public void keyTyped(KeyTypedEvent keyTypedEvent) {
        if (inWritingState)
          handleKeyTyped(keyTypedEvent);
      }

    });

    addKeyReleasedListener(new IKeyReleasedListener() {

      public void keyReleased(KeyReleasedEvent e) {
        if (e.getKeyClass() == Key.SHIFT)
          shiftKeyDown = false;
      }
    });

    addFocusListener(new IFocusListener() {
      public void focusChanged(FocusEvent f) {
        if (f.isFocusLost()) {
          inWritingState = false;
          selection.startIndex = -1;
        } else {
          inWritingState = true;
          if (getTextWarped() != null) {
            // Select the text and place the cursor at the end
            int textLength = getTextWarped().length();
            setCursorIndexWarped(textLength);
            if (selectOnFocus) {
              selection.startIndex = 0;
              selection.endIndex = textLength;
            }
          }
          getAppearance().getCursorPainter().resetTimer();
        }
      }
    });
  }

  private void handleKeyTyped(KeyTypedEvent e) {
    if (getAppearance().getFont().isCharacterMapped(e.getKey())) {
      if (restrict != null && !restrict.matcher(Character.toString(e.getKey())).matches()) {
        return;
      }
      if (selection.startIndex < selection.endIndex) {
        removeSelectedText();
        updateText();
        setCursorIndexWarped(selection.startIndex);
        resetSelection();
        processTextChange("");
      }
      if (maxCharacters < 0 || (maxCharacters >= 0 && text_prepared.length() < maxCharacters)) {
        insertCharAt(getCursorWarped(), e.getKey());
        updateText();
        setCursorIndexWarped(getCursorWarped() + 1);
        processTextChange("" + e.getKey());
      }

    }
  }

  private void handleKeyPressed(KeyPressedEvent e) {

    // @todo add handling for the Page Up and Page Down key! #
    getAppearance().getCursorPainter().resetTimer();

    switch (e.getKeyClass()) {
    case BACKSPACE:
      if (selection.startIndex < selection.endIndex) {
        setCursorIndexWarped(selection.startIndex);
        removeSelectedText();
        resetSelection();
      } else if (getCursorWarped() > 0) {
        int newCursorIndex = getCursorWarped() - 1;
        setCursorIndex(newCursorIndex);
        deleteCharAt(newCursorIndex);
      }
      break;
    case ENTER:
      if (multiline) {
        if (selection.startIndex < selection.endIndex) {
          removeSelectedText();
          setCursorIndexWarped(selection.startIndex);
          resetSelection();
        }
        insertCharAt(getCursorWarped(), '\n');
        setCursorIndexWarped(getCursorWarped() + 1);
      }
      resetSelection();
      break;

    case SHIFT:
      if (selection.state == Selection.NO_SELECTION) {
        selection.startIndex = selection.endIndex = getCursorWarped();
        selection.state = Selection.AT_END_OF_SELECTION;
      }
      shiftKeyDown = true;
      break;

    case DELETE:
      if (selection.startIndex < selection.endIndex) {
        removeSelectedText();
        setCursorIndexWarped(selection.startIndex);
      } else if (getCursorWarped() < text_prepared.length()) {
        deleteCharAt(getCursorWarped());
      }
      resetSelection();
      break;

    case UP:
      setCursorIndexWarped(seekNearestIndex(getAppearance().getCursorPainter().getX(),
          getAppearance().getCursorPainter().getY()
              + (int) ((float) getAppearance().getFont().getHeight() * 1.5f)));
      if (shiftKeyDown)
        selection.upKey();
      else
        resetSelection();
      getDisplay().fireGlobalEventListener(
          new TextCursorMovedEvent(this, TextCursorMovedEvent.UP, getCursor(), shiftKeyDown));
      break;

    case RIGHT:
      if (getCursorWarped() < getText().length())
        setCursorIndexWarped(getCursorWarped() + 1);
      if (shiftKeyDown)
        selection.rightKey();
      else
        resetSelection();
      if (getCursorWarped() < text_prepared.length())
        getDisplay().fireGlobalEventListener(
            new TextCursorMovedEvent(this, TextCursorMovedEvent.RIGHT, getCursor(), shiftKeyDown));
      break;

    case LEFT:
      boolean alreadyLeft = getCursorWarped() == 0 ? true : false;
      if (getCursorWarped() > 0)
        setCursorIndexWarped(getCursorWarped() - 1);
      if (shiftKeyDown)
        selection.leftKey();
      else
        resetSelection();
      if (!alreadyLeft)
        getDisplay().fireGlobalEventListener(
            new TextCursorMovedEvent(this, TextCursorMovedEvent.LEFT, getCursor(), shiftKeyDown));
      break;

    case DOWN:
      setCursorIndexWarped(seekNearestIndex(getAppearance().getCursorPainter().getX(),
          getAppearance().getCursorPainter().getY()
              - (int) ((float) getAppearance().getFont().getHeight() * 0.5f)));
      if (shiftKeyDown)
        selection.downKey();
      else
        resetSelection();
      getDisplay().fireGlobalEventListener(
          new TextCursorMovedEvent(this, TextCursorMovedEvent.DOWN, getCursor(), shiftKeyDown));
      break;

    case END:
      int rightNewLineIndex = text_prepared.indexOf(new StringBuffer().append('\n').toString(),
          getCursorWarped());
      if (rightNewLineIndex != -1) {
        setCursorIndexWarped(rightNewLineIndex);
      } else {
        setCursorIndexWarped(text_prepared.length());
      }
      if (shiftKeyDown)
        selection.rightKey();
      else
        resetSelection();
      getDisplay().fireGlobalEventListener(
          new TextCursorMovedEvent(this, TextCursorMovedEvent.DOWN, getCursor(), shiftKeyDown));
      break;

    case HOME:
      int leftNewLineIndex = text_prepared.substring(0, getCursorWarped()).lastIndexOf(
          new StringBuffer().append('\n').toString(), getCursorWarped());
      if (leftNewLineIndex != -1) {
        setCursorIndexWarped(leftNewLineIndex + 1);
      } else {
        setCursorIndexWarped(0);
      }
      if (shiftKeyDown)
        selection.leftKey();
      else
        resetSelection();
      getDisplay().fireGlobalEventListener(
          new TextCursorMovedEvent(this, TextCursorMovedEvent.UP, getCursor(), shiftKeyDown));
      break;
    }
  }

  private void updateText() {
    text.trimToSize();
    String currentText = text.toString();
    indexChangeList.clear();

    if (autoWarpText && multiline) {
      int maxWidth = this.appearance.getContentWidth();
      if (maxWidth <= 0) {
        maxWidth = 200;
      }
      Font font = appearance.getFont();
      String[] lines = currentText.split("\n", -1);
      java.util.List<String> resultLines = new Vector<String>(lines.length);

      for (String s : lines) {
        int start = 0;
        int end = s.length();
        if (font.getWidth(s) >= maxWidth) {
          while (start < end && font.getWidth(s.substring(start, end)) >= maxWidth) {
            int next = findNextLineEnd(s.substring(start, end), font, maxWidth);
            next = next + start;
            resultLines.add(s.substring(start, next));
            indexChangeList.add(next);
            start = next;
            if (s.length() > next && s.charAt(next) == ' ')
              start += 1;
          }
          // add rest
          if (start < end)
            resultLines.add(s.substring(start, end));

        } else {
          // line is small enough
          resultLines.add(s);
        }
      }

      // put new string together
      text_prepared = new StringBuilder(text.capacity() + resultLines.size());
      for (int i = 0; i < resultLines.size() - 1; i++) {
        String str = resultLines.get(i);
        text_prepared.append(str + "\n");
      }
      text_prepared.append(resultLines.get(resultLines.size() - 1));

    } else {
      text_prepared = text;
    }
  }

  private int findNextLineEnd(String text, Font font, int maxWidth) {
    int result = text.length();
    int charSize = font.getWidth("M") - 1;
    int end = result;

    if (font.getWidth(text) >= maxWidth) {
      // guess start
      end = maxWidth / charSize;

      // get word start position
      int pos = text.indexOf(" ", end); // after end
      if (pos < 0) {
        // no end word found after 'end' so use end of string
        end = text.length();
      } else {
        end = pos;
      }

      while (true) {
        int currentLength = font.getWidth(text.substring(0, end));
        if (currentLength <= maxWidth) {
          // get next word
          int wordEnd = text.indexOf(" ", end + 1);
          if (wordEnd < 0)
            wordEnd = text.length();
          if (wordEnd == end) {
            return end;
          }
          String nextWord = text.substring(end, wordEnd);
          int wordLength = font.getWidth(nextWord);
          if (currentLength + wordLength <= maxWidth) {
            end = wordEnd;
          } else {
            return end;
          }
        } else {
          int wordEnd = text.lastIndexOf(" ", end);
          if (wordEnd < 0)
            wordEnd = text.length();
          if (wordEnd == end) {
            return wordEnd;
          }
          String nextWord = text.substring(wordEnd, end);
          int wordLength = font.getWidth(nextWord);
          if (currentLength - wordLength <= maxWidth) {
            end = wordEnd;
          } else {
            return wordEnd;
          }
        }
      }
    } else {
      return result;
    }
  }

  private void removeSelectedText() {
    if (selection.startIndex < 0 || selection.endIndex > text_prepared.length()) {
      return;
    }
    deleteText(getIndex(selection.startIndex), getIndex(selection.endIndex));
  }

  public void setWordWarp(boolean value) {
    this.autoWarpText = value;
    processTextChange(null);
  }

  public boolean isWordWarp() {
    return this.autoWarpText;
  }

  /**
   * @return multipleLinesVisible
   */
  public boolean isMultiline() {
    return multiline;
  }

  /**
   * @param multiline
   *          to define
   */
  public void setMultiline(boolean multiline) {
    this.multiline = multiline;
    processTextChange(null);
  }

  /**
   * @return <code>true</code> if the text is selected on Focus
   */
  public boolean isSelectOnFocus() {
    return selectOnFocus;
  }

  /**
   * Sets if the text will be selected on focus.
   * 
   * @param selectOnFocus
   */
  public void setSelectOnFocus(boolean selectOnFocus) {
    this.selectOnFocus = selectOnFocus;
  }

  /**
   * @return the maxCharacters
   */
  public int getMaxCharacters() {
    return maxCharacters;
  }

  /**
   * @param maxCharacters
   *          the max number of characters in the textEditor
   */
  public void setMaxCharacters(int maxCharacters) {
    this.maxCharacters = maxCharacters;
  }

  /**
   * @return the validCharacters
   */
  public String getRestrict() {
    return restrict.pattern();
  }

  /**
   * Sets the valid characters as a RegularExpression. if we want to enable only letters from a to z
   * and numbers, we would set : "[a-zA-Z0-9]+"
   * 
   * @param validCharacters
   *          a regular expression representing valid characters
   */
  public void setRestrict(String restrict) {
    if (restrict != null) {
      if (unicodeRestrict) {
        this.restrict = Pattern.compile(restrict, Pattern.UNICODE_CASE);
      } else {
        this.restrict = Pattern.compile(restrict);
      }
    } else {
      this.restrict = null;
    }
  }

  /**
   * @return the unicodeRestrict
   */
  public boolean isUnicodeRestrict() {
    return unicodeRestrict;
  }

  /**
   * @param unicodeRestrict
   *          a d�fnir
   */
  public void setUnicodeRestrict(boolean unicodeRestrict) {
    this.unicodeRestrict = unicodeRestrict;
  }

  public void setCursorIndexWarped(int cursorIndex) {
    if (cursorIndex < 0 || getTextWarped().length() == 0)
      cursorIndex = 0;
    else if (cursorIndex > getTextWarped().length())
      cursorIndex = getTextWarped().length() - 1;

    this.cursorIndex = cursorIndex;
    scrollToCursorPosition();
  }

  public void setCursorIndex(int index) {
    setCursorIndexWarped(getWarpedIndex(index));
  }

  public int getSelectionLength() {
    return selection.startIndex - selection.endIndex;
  }

  /**
   * @return the passwordField
   */
  public boolean isPasswordField() {
    return passwordField;
  }

  /**
   * @param passwordField
   *          the passwordField to set
   */
  public void setPasswordField(boolean passwordField) {
    this.passwordField = passwordField;
  }

  protected String getTextWarped() {
    if (text_prepared != null)
      return text_prepared.toString();

    return getText();
  }

  /**
   * @return the text editor's text
   */
  public String getText() {
    if (text != null)
      return text.toString();

    return null;
  }

  /**
   * Define the textEditor's text
   * 
   * @param text
   */
  public void setText(String text) {
    this.text.delete(0, this.text.length());

    if (restrict == null || (restrict != null && restrict.matcher(text).matches())) {

      if (text != null && text.length() != 0) {
        String fittingText;
        if (maxCharacters < 0 || text.length() <= maxCharacters) {
          fittingText = text;
        } else {
          fittingText = text.substring(0, maxCharacters);
        }
        this.text.append(fittingText);
      }
      processTextChange(text);
    }
    if (getAppearance().useBufferedTextRenderer)
      getAppearance().getTextRenderer().setText(text_prepared.toString());
  }

  /**
   * Append text to the end of the textEditor
   * 
   * @param text
   */
  public void appendText(String text) {
    this.text.append(text);
    processTextChange(text);
  }

  /**
   * Terminate the current line by writing the line separator string and Append text to the end of
   * the textEditor
   * 
   * @param text
   */
  public void addTextLine(String text) {
    if (this.text.length() == 0) {
      setText(text);
      // No need to emitTextChangedSignal because the setText does it !!
    } else {
      StringBuffer newText = new StringBuffer("\n").append(text);
      this.text.append(newText);
      processTextChange(newText.toString());
    }
  }

  /**
   * Delete a portion of text editor's text
   * 
   * @param start
   * @param end
   */
  void deleteText(int start, int end) {
    try {

      String removed = text.substring(start, end);
      text.delete(start, end);
      processTextChange(removed);
    } catch (StringIndexOutOfBoundsException e) {
      // Does nothing
    }
  }

  /**
   * Insert char a specified index
   * 
   * @param index
   * @param c
   */
  void insertCharAt(int index, char c) {
    try {
      text.insert(index, c);
      processTextChange(String.valueOf(c));
    } catch (IndexOutOfBoundsException e) {
      // Does nothing
    }
  }

  /**
   * Delete char a specified index
   * 
   * @param index
   */
  void deleteCharAt(int index) {
    try {
      char c = text.charAt(index);
      text.deleteCharAt(index);
      processTextChange(String.valueOf(c));
    } catch (ArrayIndexOutOfBoundsException e) {
      // Does nothing
    }
  }

  public void resetSelection() {
    selection.reset();
  }

  @Override
  public void process(InputOutputStream stream) throws IOException, IXMLStreamableException {
    super.process(stream);

    // TODO process text
  }

  private void processTextChange(String txt) {
    setCursorIndexWarped(cursorIndex); // Place the cursor in a valid place
    updateText();
    updateMinSize();
    fireTextChangedEvent(txt);

  }

  private void scrollToCursorPosition() {
    if (getParent() != null && getParent() instanceof ScrollContainer) {
      // TODO: impelement for vertical

      if (multiline) {
        int cursorIndex = this.getCursorWarped();

        // FIXME: sometimes this happens but shouldn't, don't know why anyone else? :)
        if (cursorIndex > text_prepared.length()) {
          cursorIndex = text_prepared.length();
        }
        String[] lines = text_prepared.toString().substring(0, cursorIndex).split("\n", -1);
        int height = lines.length + 1;
        height *= this.appearance.getFont().getHeight();
        double scroll = 100.0d - ((100.0d / this.getMinHeight()) * height);
        if (scroll < 0.0d)
          scroll = 0.0d;

        ((ScrollContainer) getParent()).scrollVertical(scroll);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.fenggui.Widget#setSize(org.fenggui.util.Dimension)
   */
  @Override
  public void setSize(Dimension s) {
    super.setSize(s);

    if (autoWarpText)
      updateText();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.fenggui.Widget#layout()
   */
  @Override
  public void layout() {
    super.layout();
  }

  @Override
  public void updateMinSize() {

    setMinSize(getAppearance().getMinSizeHint());

    if (getParent() != null && getParent() instanceof ScrollContainer) {
      ((ScrollContainer) getParent()).layout();
      scrollToCursorPosition();
    } else if (getParent() != null)
      getParent().updateMinSize();
  }

  private int seekNearestIndex(int x, int y) {
    String text = getTextWarped();
    if (text == null || text.length() == 0)
      return 0;

    Font font = getAppearance().getFont();

    // line in which the hit falls
    int line = (getAppearance().getContentHeight() - y) / font.getHeight();

    // System.out.println("Line : "+line);

    int i = 0;
    char c = text.charAt(0);
    int lineCounter = 0;

    // counting '\n's until the correct lines starts
    while (lineCounter < line && i < text.length()) {
      c = text.charAt(i++);
      if (c == '\n')
        lineCounter++;
    }

    // sum up all chars and compare the length to
    // nearestX until nearestX is bigger than the
    // x value of the hit point. That means we just
    // stepped over the char we searched for
    int nearestX = font.getWidth(c);
    while (nearestX < x && i < text.length()) {
      c = text.charAt(i++);
      nearestX += font.getWidth(c);

      // the click falls after the last char in this line
      if (c == '\n') {
        --i; // ignore '\n'
        break;
      }
    }

    if (line == 0)
      ++i;

    // System.out.println("GRR: "+nearestX+" "+x+" "+line);

    if (i > text.length()) {
      // index must not be greater than text length
      --i;
    } else if ((nearestX - x) > font.getWidth(c) / 2) {
      // origin of a char is not in the middle of the char, that
      // means that i always points behind the clicked character.
      // However, the user may have intendent to klick _before_ the
      // char and clicked on the left side of the currently selected
      // char.
      --i;
    }

    // System.out.println("Line start index: "+i);

    return i;
  }

  private class TextEditorDnDListener implements IDragAndDropListener {
    TextEditor parent = null;

    public TextEditorDnDListener(TextEditor parent) {
      this.parent = parent;
    }

    public boolean isDndWidget(IWidget w, int displayX, int displayY) {
      return w.equals(parent);
    }

    public void select(int displayX, int displayY) {
      selection.state = Selection.AT_START_OF_SELECTION;
      selection.startIndex = selection.endIndex = seekNearestIndex(displayX - parent.getDisplayX(),
          displayY - parent.getDisplayY());
      selection.state = Selection.AT_END_OF_SELECTION;
    }

    public void drag(int displayX, int displayY) {
      int index = seekNearestIndex(displayX - parent.getDisplayX(), displayY - parent.getDisplayY());
      updateSelection(index);
    }

    public void drop(int displayX, int displayY, IWidget droppedOn) {
      if (!droppedOn.equals(parent))
        return;
      int index = seekNearestIndex(displayX - parent.getDisplayX(), displayY - parent.getDisplayY());
      updateSelection(index);
      selection.state = Selection.NO_SELECTION;
    }

    private void updateSelection(int index) {
      if (selection.state == Selection.AT_END_OF_SELECTION) {
        if (index > selection.startIndex) {
          selection.endIndex = index;
        } else {
          selection.endIndex = selection.startIndex;
          selection.startIndex = index;
          selection.state = Selection.AT_START_OF_SELECTION;
        }
      } else {
        if (index < selection.endIndex) {
          selection.startIndex = index;
        } else {
          selection.startIndex = selection.endIndex;
          selection.endIndex = index;
          selection.state = Selection.AT_END_OF_SELECTION;
        }
      }
      setCursorIndexWarped(index);
    }
  }

  public class Selection {
    public static final int NO_SELECTION          = 0;
    public static final int AT_END_OF_SELECTION   = 1;
    public static final int AT_START_OF_SELECTION = 2;

    public int              startIndex            = -1;
    public int              endIndex              = -1;
    public int              startX                = -1;
    public int              startY                = -1;
    public int              endX                  = -1;
    public int              endY                  = -1;

    public int              state                 = NO_SELECTION;

    public void reset() {
      startIndex = endIndex = startX = endX = startY = endY = -1;
      state = NO_SELECTION;
    }

    public void upKey() {
      if (state != NO_SELECTION && getSelectionLength() == 0) {
        state = AT_START_OF_SELECTION;
      }

      if (state == AT_END_OF_SELECTION) {
        if (getCursorWarped() < startIndex) {
          endIndex = startIndex;
          startIndex = getCursorWarped();
          state = AT_START_OF_SELECTION;
        } else {
          endIndex = getCursorWarped();
        }
      } else if (state == AT_START_OF_SELECTION) {
        startIndex = getCursorWarped();
      }

    }

    public void downKey() {
      if (state != NO_SELECTION && getSelectionLength() == 0) {
        state = AT_END_OF_SELECTION;
      }

      if (state == AT_END_OF_SELECTION) {
        endIndex = getCursorWarped();
      } else if (state == AT_START_OF_SELECTION) {
        if (getCursorWarped() > endIndex) {
          startIndex = endIndex;
          endIndex = getCursorWarped();
          state = AT_END_OF_SELECTION;
        } else {
          startIndex = getCursorWarped();
        }
      }

    }

    public void leftKey() {
      if (state != NO_SELECTION && getSelectionLength() == 0) {
        state = AT_START_OF_SELECTION;
      }

      if (state == AT_END_OF_SELECTION) {
        endIndex = getCursorWarped();
      } else if (state == AT_START_OF_SELECTION) {
        startIndex = getCursorWarped();
      }

    }

    public void rightKey() {
      if (state != NO_SELECTION && getSelectionLength() == 0) {
        state = AT_END_OF_SELECTION;
      }

      if (state == AT_END_OF_SELECTION) {
        endIndex = getCursorWarped();
      } else if (state == AT_START_OF_SELECTION) {
        startIndex = getCursorWarped();
      }

    }
  }

  public boolean isInWritingState() {
    return inWritingState;
  }

  /**
   * Add a {@link ITextChangedListener} to the widget. The listener can be added only once.
   * 
   * @param l
   *          Listener
   */
  public void addTextChangedListener(ITextChangedListener l) {
    if (!textChangedHook.contains(l)) {
      textChangedHook.add(l);
    }
  }

  /**
   * Add the {@link ITextChangedListener} from the widget
   * 
   * @param l
   *          Listener
   */
  public void removeTextChangedListener(ITextChangedListener l) {
    textChangedHook.remove(l);
  }

  /**
   * Fire a {@link TextChangedEvent}
   */
  private void fireTextChangedEvent(String text) {
    TextChangedEvent e = new TextChangedEvent(this, text);

    for (ITextChangedListener l : textChangedHook) {
      l.textChanged(e);
    }

    Display display = getDisplay();
    if (display != null) {
      display.fireGlobalEventListener(e);
    }
  }

  public int getFixedSize() {
    return size;
  }

  public void setFixedSize(int size) {
    this.size = size;
    updateMinSize();
  }

}
