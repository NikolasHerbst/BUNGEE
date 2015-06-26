package net.datapipe.CloudStack;

import org.w3c.dom.Document;

import java.util.HashMap;

public class createVolume {
  public static void main(String[] args) throws Exception {
    if(args.length < 1) {
      System.out.println("I need: name");
      System.exit(1);
    }
    String name = args[0];
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args,1);
    Document create_reply = client.createVolume(name,options);

    String elements[] = {"id", "jobid"};

    CLI.printDocument(create_reply, "/createvolumeresponse", elements);   
  }
}
