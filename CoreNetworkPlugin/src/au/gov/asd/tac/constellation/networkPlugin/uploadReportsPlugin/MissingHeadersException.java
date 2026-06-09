/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.uploadReportsPlugin;

import java.util.List;

/**
 * Thrown when the uploaded file is missing mandatory headers.
 */
public class MissingHeadersException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Missing headers for %s: %s\n";

    public MissingHeadersException(String fileName, List<String> missingHeaders) {
        super(String.format(MESSAGE_TEMPLATE, fileName, String.join(", ", missingHeaders)));
    }
}
