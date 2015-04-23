package HealthCareSystem;

import GenCol.*;
import genDevs.modeling.*;
import simView.*;

public class AssessmentController
   extends ViewableAtomic {

 protected boolean doctor_available;
 protected boolean npatientarrivalReq;
 protected boolean emergpatientarrivalReq;

 public AssessmentController() {
   this("Assessment controller");
 }

 public AssessmentController(String nm) {
   super(nm);

   doctor_available = true;
   npatientarrivalReq = false;
   emergpatientarrivalReq = false;

   addInport("in");
   addOutport("out");

   addTestInput("in", new entity("Normal_Patient"), 0);
   addTestInput("in", new entity("Normal_Patient"), 5);
   addTestInput("in", new entity("Normal_Patient"), 10);
   addTestInput("in", new entity("Emergency_Patient"), 0);
   addTestInput("in", new entity("Emergency_Patient"), 5);
   addTestInput("in", new entity("Emergency_Patient"), 10);
   addTestInput("in", new entity("AvailableForAssessment"), 0);
   addTestInput("in", new entity("AvailableForAssessment"), 5);
   addTestInput("in", new entity("AvailableForAssessment"), 10);
   
   /* the ID number of the aircraft in the queue
    that is reporting making an approach ; it ranges
    from  1 to 10 according to the Max size of the arriving queue*/
   //addTestInput("progress_in", new doubleEnt(1), 0);
 }

 public void initialize() {
   super.initialize();
   doctor_available = true;
   npatientarrivalReq = false;
   emergpatientarrivalReq = false;

   holdIn("FREE", INFINITY);
 }

 public void deltext(double e, message x) {
   Continue(e);

   /* arriving aircraft have priority over departing aircraft because
      their fuel capacity doesn't permit them  to wait for long periods.
      This is solved as shown below using the member variable runway_is_clears
    */

   if (somethingOnPort(x, "in")) {
     entity ent = getEntityOnPort(x, "in");

     if (ent.eq("Emergency_Patient")) {
       if (doctor_available) {
    	   doctor_available = false;
         holdIn("Assessing_EmergPatient", 0);
       }
       else {
    	   npatientarrivalReq = true; 
       }
     }

     else if (ent.eq("Normal_Patient")) {
       if (doctor_available) {
    	   doctor_available = false;
         holdIn("Assessing_NormalPatient", 0);
       }
       else {
    	   emergpatientarrivalReq = true; 
       }
     }

     else if (ent.eq("AvailableForAssessment")) {
       //  order of servicing reflects priority order of landing over takeoff
       if (npatientarrivalReq) {
    	   npatientarrivalReq = false;
    	   doctor_available = false;
         holdIn("Assessing_NormalPatient", 0);
       }
       else
       if (emergpatientarrivalReq) {
    	   emergpatientarrivalReq = false;
    	   doctor_available = false;
         holdIn("Assessing_EmergPatient", 0);
       }

       else {
    	   doctor_available = true;
         holdIn("FREE", INFINITY); //back to initial state
       }
     }
   }
 }

 public void deltint() {
   if (phaseIs("Assessing_NormalPatient")) {
     holdIn("Assessing_NormalPatient", INFINITY);
   }
   else if (phaseIs("Assessing_EmergPatient")) {
     holdIn("Assessing_EmergPatient", INFINITY);

   }
   doctor_available = false;
 }

 public void deltcon(double e, message x) {
   deltint();
   deltext(0, x);
 }

 public message out() {
   message m = new message();

   if (phaseIs("Assessing_NormalPatient")) {
     m.add(makeContent("out", new entity("Normal_Patient_Assessed")));
   }
   else if (phaseIs("Assessing_EmergPatient")) {
     m.add(makeContent("out", new entity("Emergency_Patient_Assessed")));

   }
   return m;
 }



} // end of class controller
