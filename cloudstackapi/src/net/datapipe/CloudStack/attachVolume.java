package net.datapipe.CloudStack;

import org.w3c.dom.Document;

import java.util.HashMap;

public class attachVolume {
  public static void main(String[] args) throws Exception {
    if(args.length < 2) {
      System.out.println("I need: id, virtualmachineid");
      System.exit(1);
    }
    String id = args[0];
    String vm_id = args[1];
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args,2);
    Document attach_reply = client.attachVolume(id,vm_id,options);

    String elements[] = {"jobid"};

    CLI.printDocument(attach_reply, "/attachvolumeresponse", elements);   
  }
}
