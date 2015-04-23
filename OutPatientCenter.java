package HealthCareSystem;

import java.lang.*;
import genDevs.modeling.*;
import genDevs.simulation.*;
import GenCol.*;
import util.*;
import simView.*;
import statistics.*;

public class OutPatientCenter extends ViewableAtomic{

  public static final double ARRIVING_RANDOM_CONST = 6;
  protected double assessing_time;
  protected double clock;
  protected int n;
  protected rand r = null;


  public OutPatientCenter() {
    this("Out Patient Center");
  }

  public OutPatientCenter(String nm) {
    super(nm);

    this.assessing_time  = 0;//will be set later randomelys
    clock = 0;
    n = 0;

    addInport("in");
    addOutport("out");

    /*
    These 2 ports are added to solve the problem of initial case; when the
    aircraft obj is in passive state, and so the queue will not get the input
    "RUNWAY_IS_CLEAR" so it'll not forward a new aircraft to the aircraft obj.
    This is somehow like handshaking; the queue asks the aircraft obj.
    Are you ready? and it simply replys "Yes" if it's in the passive state,
    and can accept a new obj. if it's not in the passive state it'll not reply
    back, and the queue will not overwhelm the aircraft obj with a new aircraft
   */
    addInport("ready");
    addOutport("yes_ready");


    addTestInput("in", new entity("DOCTOR_AVAILABLE"), 0);
    addTestInput("in", new entity("DOCTOR_AVAILABLE"), 5);
    addTestInput("in", new entity("DOCTOR_AVAILABLE"), 10);
    addTestInput("in", new entity("Patient"), 0);
    addTestInput("in", new entity("Patient"), 5);
    addTestInput("in", new entity("Patient"), 10);
    addTestInput("ready", new entity(""), 0);
    addTestInput("ready", new entity(""), 10);

    r = new rand(1);
  }

  public void initialize() {
    super.initialize();
    passivate();
  }

  public void deltext(double e, message x) {
    Continue(e);

    if (somethingOnPort(x,"in"))
    {
      entity ent = getEntityOnPort(x, "in");

      if(phaseIs("passive")) {
        if(ent.toString().startsWith("Patient"))
          holdIn("active",0);
      }

      if(phaseIs("WAITING CLEARANCE")) {
        if (ent.eq("DOCTOR_AVAILABLE")) {
        	assessing_time = r.uniform(ARRIVING_RANDOM_CONST);
         double overhead_time = r.expon(ARRIVING_RANDOM_CONST);
         /*
             This overhead time is because of taxi onto runway for TO
             or roll out after landing
          */
         clock += (assessing_time+overhead_time);
         n += 1;

         holdIn("Assessing", assessing_time + overhead_time);
       }
      }
    }

    if (somethingOnPort(x,"ready")) {
      if (phaseIs("passive")) {
        holdIn("YES", 0); //for handshaking
      }
    }
  }

  public void deltint() {
    if(phaseIs("active"))
      holdIn("WAITING CLEARANCE",INFINITY);
    else if(phaseIs("Assessing"))
      passivate();
    else if(phaseIs("YES"))
      passivate();
  }

  public void deltcon(double e,message x) {
    deltint();
    deltext(0,x);
  }

  public message out() {
    message m = new message();

    if ( phaseIs("active"))
      m.add(makeContent("out",new entity("Walkin_REQ")));
    else if ( phaseIs("Assessing"))
      m.add(makeContent("out",new entity("DOCTOR_AVAILABLE")));
    else if(phaseIs("YES"))
      m.add(makeContent("yes_ready",new entity("YES")));

    return m;
  }

  public String getTooltipText(){
    return super.getTooltipText() + "\nMean time spent landing = " +
        ((n>0)? (clock / n): 0);
  }

  public void showState(){
    super.showState();
    System.out.println("\nMean time spent landing = "  + ((n>0)? (clock / n): 0));
 }


}// end of class arrivingAircraft

