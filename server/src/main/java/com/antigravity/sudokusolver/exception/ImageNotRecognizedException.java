package com.antigravity.sudokusolver.exception;

public class ImageNotRecognizedException extends RuntimeException {
    public ImageNotRecognizedException() {
        super("IMAGE_NOT_RECOGNIZED");
    }

    public ImageNotRecognizedException(String message) {
        super(message);
    }
}
