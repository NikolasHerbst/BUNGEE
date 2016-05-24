package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class deleteVolume {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    if(args.length == 0) {
      System.out.println("I need a volume id");
      System.exit(1);
    }
    String id = args[0];
    Document destroy_response = client.deleteVolume(id);
 
    String elements[] = {"success","displaytext"};

    CLI.printDocument(destroy_response, "/deletevolumeresponse", elements);   
  }
}
