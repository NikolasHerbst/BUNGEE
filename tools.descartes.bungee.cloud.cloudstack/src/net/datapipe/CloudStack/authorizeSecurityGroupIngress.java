package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class authorizeSecurityGroupIngress {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document authorize_result = client.authorizeSecurityGroupIngress(options);

    String elements[] = {"jobid"};

    CLI.printDocument(authorize_result, "/authorizesecuritygroupingressresponse", elements);
  }
}
