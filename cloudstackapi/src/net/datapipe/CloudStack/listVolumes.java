package net.datapipe.CloudStack;

import org.w3c.dom.Document;

import java.util.HashMap;

public class listVolumes {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document volume_list_doc = client.listVolumes(options);

    String elements[] = {"id", "name", "type", "size", "state", "vmname", "vmstate"};

    CLI.printDocument(volume_list_doc, "//volume", elements);
  }
}
