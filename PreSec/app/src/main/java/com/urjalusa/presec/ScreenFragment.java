package com.urjalusa.presec;

class ScreenFragment {

    private final String textToDisplay;
    private final int imageToShow;

    ScreenFragment(String textToDisplay, int imageToShow) {
        this.textToDisplay = textToDisplay;
        this.imageToShow = imageToShow;
    }

    String getTextToDisplay() {
        return textToDisplay;
    }

    int getImageToShow() {
        return imageToShow;
    }
}