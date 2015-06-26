package net.datapipe.CloudStack;

import org.w3c.dom.Document;

public class listIsos {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    Document iso_list = client.listIsos(null);

    String elements[] = {"id", "displaytext", "name", "domain", "isready"};

    CLI.printDocument(iso_list, "//iso", elements);
  }
}
