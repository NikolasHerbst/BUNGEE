package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class listCapabilities {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    Document cap_list = client.listCapabilities();

    String elements[] = {"cloudstackversion", "firewallRuleUiEnabled", "securitygroupsenabled", "supportELB", "userpublictemplateenabled"};

    CLI.printDocument(cap_list, "//capability", elements);
  }
}
