package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class listOsTypes {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document type_list = client.listOsTypes(options);

    String elements[] = {"id", "description", "oscategoryid"};

    CLI.printDocument(type_list, "//ostype", elements);
  }
}
