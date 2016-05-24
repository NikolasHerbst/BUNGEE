package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class createTemplate {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    if(args.length < 3) {
      System.out.println("I need at least: displaytext, name, ostypeid");
      System.exit(1);
    }
    String displaytext = args[0];
    String name = args[1];
    String ostypeid = args[2];
    HashMap<String,String> options = CLI.args_to_options(args,3);
    Document create_response = client.createTemplate(displaytext,name,ostypeid,options);
 
    String elements[] = {"id", "jobid"};

    CLI.printDocument(create_response,"/createtemplateresponse", elements);
  }
}
