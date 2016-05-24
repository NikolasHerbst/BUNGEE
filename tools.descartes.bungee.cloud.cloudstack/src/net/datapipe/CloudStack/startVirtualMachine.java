package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class startVirtualMachine {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    if(args.length == 0) {
      System.out.println("I need a vm id");
      System.exit(1);
    }
    String id = args[0];
    Document start_response = client.startVirtualMachine(id);
 
    String elements[] = {"jobid"};

    CLI.printDocument(start_response, "/startvirtualmachineresponse", elements);   
  }
}
