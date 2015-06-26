package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class listVirtualMachines {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document vm_list_doc = client.listVirtualMachines(options);
 
    String elements[] = {"id", "displayname", "state", "domain", "nic/ipaddress", "templatename"};

    CLI.printDocument(vm_list_doc, "//virtualmachine", elements);   
  }
}
