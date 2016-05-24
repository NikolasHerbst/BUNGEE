package net.datapipe.CloudStack;

import org.w3c.dom.Document;

import java.util.HashMap;

public class listInstanceGroups {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document group_list_doc = client.listInstanceGroups(options);

    String elements[] = {"id", "name", "created"};

    CLI.printDocument(group_list_doc, "//instancegroup", elements);
  }
}
