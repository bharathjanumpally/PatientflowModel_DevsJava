package HealthCareSystem;

import genDevs.modeling.*;
import genDevs.simulation.*;
import GenCol.*;
import simView.*;

public class WalkinPatientsQueue extends ViewableAtomic{

  public static final int MAX_DEPRT = 10;
  public static final double SHIFT_TIME = 0; /* time to move a plane in the queue */
  protected int count;
  protected int max_count_waitingToLand;
  protected double sum;
  protected int n;
  protected entity queue[];

  public WalkinPatientsQueue() {
    this("Walkin Patients Queue");
  }

  public WalkinPatientsQueue(String nm) {
    super(nm);

    count = 0;
    max_count_waitingToLand = 0;
    sum = 0;
    n = 0;

    queue = new entity [MAX_DEPRT];/*maximum possible size of the queue*/
    for(int i = 0;i < MAX_DEPRT;i++)
      queue[i] = null;

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
    addInport("yes_ready");
    addOutport("ready");

    addTestInput("in", new entity("RUNWAY_IS_CLEAR"), 0);
    addTestInput("in", new entity("RUNWAY_IS_CLEAR"), 5);
    addTestInput("in", new entity("RUNWAY_IS_CLEAR"), 10);
    addTestInput("in", new entity("Patient1"), 0);
    addTestInput("in", new entity("Patient1"), 5);
    addTestInput("in", new entity("Patient1"), 10);
    addTestInput("in", new entity("Patient2"), 0);
    addTestInput("in", new entity("Patient3"), 0);
    addTestInput("yes_ready", new entity(""), 0);
    addTestInput("yes_ready", new entity(""), 10);
  }

  public void initialize() {
    super.initialize();
    count = 0;
    sum = 0;
    max_count_waitingToLand = 0;
    n = 0;
    for(int i = 0;i < MAX_DEPRT;i++)
      queue[i] = null;

    holdIn("count = " + count, INFINITY);
  }

  public void deltext(double e, message x) {
    Continue(e);

    if(sigma == INFINITY) {
      if (somethingOnPort(x,"in")) {
        entity ent = getEntityOnPort(x,"in");

        if(ent.toString().startsWith("Patient")) { /* new aircraft from the Gen. */
          if(queue[count] == null) //empty
            queue[count] = new entity (ent.toString());
          if(count < MAX_DEPRT) { // MAX Allowed
            count += 1; //increase count by 1
            if(count > max_count_waitingToLand)
              max_count_waitingToLand = count;

            sum += count;
            n += 1;
          }
          if(count == 1) /* Go to handshaking */
            holdIn("warming", 0);
          else
            holdIn("count = " + count, INFINITY);//update phase
        }

         if (ent.eq("RUNWAY_IS_CLEAR")){
           if (count > 0) {
             if (count == 1){
               queue[0] = null;
               count = 0;
               holdIn("count = " + count, INFINITY);
             }
             else {
               /* shift the queue */
               for (int i = 0; i < count - 1; i++)
                 queue[i] = queue[i + 1];
               queue[count - 1] = null;
               count -= 1; // decrease count by 1
               holdIn("count = " + count, SHIFT_TIME);
             }
           }
         }
      }

      if (somethingOnPort(x,"yes_ready")) {
        /*
         here we don't need to update count; just move to the state
         "start" to indicate the startup (warming) and to send an aircraft
         to the aircraft obj since it's ready. <Handshaking>
        */
        holdIn("start", 0);
      }
    }
  }

  public void deltint() {
    if(phaseIs("warming") || phaseIs("start") || sigma == SHIFT_TIME)
      holdIn("count = " + count, INFINITY);
  }

  public void deltcon(double e, message x) {
    deltint();
    deltext(0, x);
  }

  public message out() {
    message m = new message();

    if (phaseIs("warming"))
      m.add(makeContent("ready", new entity("READY")));
    else
      m.add(makeContent("out", new entity(queue[0].toString())));

    return m;
  }

  public String getTooltipText(){
    return super.getTooltipText()+"\nTotal # of Arriving AC = " + n +
        "\nMAX # AC waiting to land = "+ max_count_waitingToLand +
        "\nMean # AC waiting to land = " + ((n>0)? (sum / n): 0);
 }

 public void showState(){
  super.showState();
  System.out.println("\nTotal # of Arriving Patients = " + n);
  System.out.println("\nMAX # AC waiting to land = " + max_count_waitingToLand);
  System.out.println("\nMean # AC waiting to land = "+((n>0)? (sum / n): 0));

 }



}// end of class arrivalQueue




