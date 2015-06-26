package net.datapipe.CloudStack;

import org.w3c.dom.Document;

import java.util.HashMap;

public class deployVirtualMachine {
  public static void main(String[] args) throws Exception {
    if(args.length < 3) {
      System.out.println("I need: serviceofferingid, templateid, zoneid");
      System.exit(1);
    }
    String serviceofferingid = args[0];
    String templateid = args[1];
    String zoneid = args[2];
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args,3);
    Document deploy_reply = client.deployVirtualMachine(serviceofferingid,templateid,zoneid,options);

    String elements[] = {"id", "jobid"};

    CLI.printDocument(deploy_reply, "/deployvirtualmachineresponse", elements);   
  }
}
