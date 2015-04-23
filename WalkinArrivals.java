package HealthCareSystem;

import java.lang.*;
import genDevs.modeling.*;
import genDevs.simulation.*;
import GenCol.*;
import util.*;
import simView.*;
import statistics.*;


public class WalkinArrivals extends ViewableAtomic{

  protected double int_arr_time;
  protected int count;
  protected rand r = null;

  public WalkinArrivals() {
    this("Walkin Patients Arriving",10);
  }

  public WalkinArrivals(String nm, double int_arr_time){
    super(nm);

    addInport("stop");
    addInport("start");
    addOutport("out");

    this.int_arr_time = int_arr_time;

    addTestInput("start",new entity(""),0);
    addTestInput("stop",new entity(""),0);

    r = new rand(2);
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
     content con = makeContent("out", new entity ("NormalPatient" + count));
     m.add(con);

     return m;
   }

   public boolean somethingOnPort(message x,String port){
     for (int i = 0; i < x.getLength(); i++)
       if (messageOnPort(x, port, i))
         return true;
     return false;
    }

    public entity getEntityOnPort(message x, String port) {
      for (int i = 0; i < x.getLength(); i++)
        if (messageOnPort(x, port, i)) {
          return x.getValOnPort(port, i);
        }
      return null;
    }
}// end of class arrGenerator


