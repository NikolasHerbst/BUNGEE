package net.datapipe.CloudStack;

import org.w3c.dom.Document;

import java.util.HashMap;

public class listTemplates {
  public static void main(String[] args) throws Exception {
    String filter = "community";
    if(args.length > 0) {
      filter = args[0];
    }
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args,1);
    Document template_list_doc = client.listTemplates(filter,options);

    String elements[] = {"id", "displaytext", "name", "hypervisor", "isready", "ispublic"};

    CLI.printDocument(template_list_doc, "//template", elements);
  }
}
