package net.datapipe.CloudStack;

import org.w3c.dom.Document;

import java.util.HashMap;

public class listSecurityGroups {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document secgroups_list_doc = client.listSecurityGroups(options);

    String elements[] = {"id", "name", "description"};

    CLI.printDocument(secgroups_list_doc, "//securitygroup", elements);
  }
}
