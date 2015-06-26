package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class stopVirtualMachine {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    if(args.length == 0) {
      System.out.println("I need a vm id");
      System.exit(1);
    }
    String id = args[0];
    HashMap<String,String> options = CLI.args_to_options(args,1);
    Document stop_response = client.stopVirtualMachine(id,options);
 
    String elements[] = {"jobid"};

    CLI.printDocument(stop_response, "/stopvirtualmachineresponse", elements);   
  }
}
