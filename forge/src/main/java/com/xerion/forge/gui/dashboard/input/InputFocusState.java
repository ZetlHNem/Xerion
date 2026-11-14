package com.xerion.forge.gui.dashboard;

final class InputFocusState {
    String textInputId;
    int textInputX;
    int textInputY;
    int textInputW;
    int textInputH;
    int cursor;
    int selectionAnchor;

    boolean hasTextInputFocus() {
        return textInputId != null;
    }

    void focusTextInput(String id, int x, int y, int w, int h, int textLength) {
        boolean changed = !id.equals(textInputId);
        textInputId = id;
        textInputX = x;
        textInputY = y;
        textInputW = w;
        textInputH = h;
        if (changed) {
            cursor = Math.max(0, textLength);
            selectionAnchor = cursor;
        }
    }

    void moveCursor(int nextCursor, boolean selecting, int textLength) {
        cursor = Math.max(0, Math.min(nextCursor, textLength));
        if (!selecting) {
            selectionAnchor = cursor;
        }
    }

    void selectAll(int textLength) {
        selectionAnchor = 0;
        cursor = Math.max(0, textLength);
    }

    boolean hasSelection() {
        return cursor != selectionAnchor;
    }

    int selectionStart() {
        return Math.min(cursor, selectionAnchor);
    }

    int selectionEnd() {
        return Math.max(cursor, selectionAnchor);
    }

    void clearTextInputFocus() {
        textInputId = null;
        textInputX = 0;
        textInputY = 0;
        textInputW = 0;
        textInputH = 0;
        cursor = 0;
        selectionAnchor = 0;
    }
}
