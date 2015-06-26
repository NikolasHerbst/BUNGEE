package net.datapipe.CloudStack;

import java.util.HashMap;

import org.w3c.dom.Document;

public class listAsyncJobs {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    HashMap<String,String> options = CLI.args_to_options(args);
    Document jobs_list = client.listAsyncJobs(options);

    String elements[] = {"jobid", "cmd", "jobinstancetype"};

    CLI.printDocument(jobs_list, "//asyncjobs", elements);
  }
}
