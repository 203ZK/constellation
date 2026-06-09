/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package au.gov.asd.tac.constellation.networkPlugin.importReportsPlugin;

/**
* A particular dropdown option containing both a report's name and its ID.
*/
public class ReportOption {
   private final String id, name;

   public ReportOption(String id, String name) { 
       this.id = id;
       this.name = name;
   }

   public String getDisplayName() {
       return this.name + " (ID: " + this.id + ")";
   }
}
