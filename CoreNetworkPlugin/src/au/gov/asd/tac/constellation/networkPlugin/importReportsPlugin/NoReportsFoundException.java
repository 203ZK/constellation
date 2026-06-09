/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin;

/**
 *
 * @author Intern-1003972
 */
public class NoReportsFoundException extends RuntimeException {
    public NoReportsFoundException(String userId) {
        super(String.format("No reports found for user ID: %s", userId));
    }
}
