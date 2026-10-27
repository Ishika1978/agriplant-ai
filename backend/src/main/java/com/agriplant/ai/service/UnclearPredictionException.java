package com.agriplant.ai.service;

/**
 * Thrown when the CNN's top prediction is below the confidence threshold.
 *
 * The classifier is a fixed 38-class softmax: handed a houseplant or an
 * unlisted crop it still returns its closest match, sometimes with a
 * confident-looking score. Treating that as a diagnosis is how a farmer ends
 * up spraying for a disease their plant doesn't have, so it surfaces as a
 * 422 asking for a better photo instead.
 */
public class UnclearPredictionException extends RuntimeException {
    public UnclearPredictionException(String message) {
        super(message);
    }
}
