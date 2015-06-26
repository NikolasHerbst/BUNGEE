package net.datapipe.CloudStack;

import org.w3c.dom.Document;

import java.util.HashMap;

public class listSSHKeyPairs {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document keypair_list_doc = client.listSSHKeyPairs(options);

    String elements[] = {"name", "fingerprint", "privatekey"};

    CLI.printDocument(keypair_list_doc, "//sshkeypair", elements);
  }
}
