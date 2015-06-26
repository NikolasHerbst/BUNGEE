package net.datapipe.CloudStack;

import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

public class queryAsyncJobResult {
  public static void main(String[] args) throws Exception {
    CloudStack client = CLI.factory();
    if(args.length == 0) {
      System.out.println("I need a jobid");
      System.exit(255);
    }
    String jobid = args[0];
    Document job_result = client.queryAsyncJobResult(jobid);

    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    XPathExpression jobstatus_xp = xpath.compile("/queryasyncjobresultresponse/jobstatus/text()");
    String jobstatus = (String)jobstatus_xp.evaluate(job_result, XPathConstants.STRING);

    XPathExpression jobid_xp = xpath.compile("/queryasyncjobresultresponse/jobid/text()");
    String jobid_response = (String)jobid_xp.evaluate(job_result, XPathConstants.STRING);

    System.out.print("jobid = "+jobid_response);
    if(jobstatus.equals("1")) {
      System.out.print(" completed ok");
    } else if(jobstatus.equals("0")) {
      System.out.print(" still running");
    } else if(jobstatus.equals("2")) {
      System.out.println(" failed");
      XPathExpression errorcode_xp = xpath.compile("/queryasyncjobresultresponse/jobresult/errorcode/text()");
      XPathExpression errortext_xp = xpath.compile("/queryasyncjobresultresponse/jobresult/errortext/text()");
      String errorcode = (String)errorcode_xp.evaluate(job_result, XPathConstants.STRING);
      String errortext = (String)errortext_xp.evaluate(job_result, XPathConstants.STRING);
      if(errorcode.length() > 0 || errortext.length() > 0) {
        System.out.print("errorcode = "+errorcode+"  errortext = "+errortext);
      }
    } else {
      System.out.print(" unknown status = "+jobstatus);
    }
   
    System.out.println("");
    System.exit(Integer.parseInt(jobstatus));
  }
}
