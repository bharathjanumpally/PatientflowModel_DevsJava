package HealthCareSystem;

import java.lang.*;
import genDevs.modeling.*;
import genDevs.simulation.*;
import GenCol.*;
import util.*;
import simView.*;
import statistics.*;

public class EmergencyArrivals extends ViewableAtomic{

  protected double int_arr_time;
  protected int count;
  protected rand r = null;

  public EmergencyArrivals() {
    this("Emergency Arrivals ",10);
  }

  public EmergencyArrivals(String nm, double int_arr_time){
    super(nm);

    addInport("stop");
    addInport("start");
    addOutport("out");

    this.int_arr_time = int_arr_time;

    addTestInput("start",new entity(""),0);
    addTestInput("stop",new entity(""),0);

    r = new rand(1);
    initialize();
  }

  public void initialize(){
    if (r != null)
      holdIn("active",r.uniform(int_arr_time));

    count = 0;
    super.initialize();
   }

   public void  deltext(double e,message x){

     Continue(e);

     if (somethingOnPort(x,"start"))
       holdIn("active",r.expon(int_arr_time));

     if (somethingOnPort(x,"stop"))
       passivate();
   }

   public void  deltint() {
     if(phaseIs("active")){
       count = count +1;
       holdIn("active",r.uniform(int_arr_time));
     }
   }

   public message  out() {
     message  m = new message();
     content con = makeContent("out", new entity ("Patient" + count));
     m.add(con);

     return m;
   }

}// end of class depGenerator


