package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class listSnapshots {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document snapshot_list = client.listSnapshots(options);

    String elements[] = {"id", "name", "created", "state", "volumename", "volumetype"};

    CLI.printDocument(snapshot_list, "//snapshot", elements);
  }
}
