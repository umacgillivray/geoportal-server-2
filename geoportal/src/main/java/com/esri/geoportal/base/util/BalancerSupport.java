/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.geoportal.base.util;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.client.HttpClient;

/**
 * Supports forward proxy load balancing to a cluster of nodes.
 */
public class BalancerSupport {
  
  /** Instance vriables. */
  protected final AtomicLong balancerCount = new AtomicLong();
  private List<BalancerNode> balancerNodes = new ArrayList<>();
  
  /** Constructor. */
  public BalancerSupport() {}
  
  /** The cluster of nodes. */ 
  public List<BalancerNode> getBalancerNodes() {
    return balancerNodes;
  }
  /** The cluster of nodes. */ 
  public void getBalancerNodes(List<BalancerNode> balancerNodes) {
    this.balancerNodes = balancerNodes;
  }
  
  /**
   * Make a new HTTP client
   * @return the client
   */
  public HttpClient newHttpClient() {
    HttpClient client = new HttpClient();
    // TODO HttpProxy??
    //HttpProxy proxy = new HttpProxy("localhost",8888);
    //ProxyConfiguration proxyConfig = client.getProxyConfiguration();
    //proxyConfig.getProxies().add(proxy);
    return client;
  }
  
  /**
   * Rewrite the target url for a request.
   * @param request the request
   * @return the url
   */
  public String rewriteTarget(HttpServletRequest request) {
    if (balancerNodes.size() == 0) return null;
    int index = (int)(balancerCount.getAndIncrement() % balancerNodes.size());
    BalancerNode node =  balancerNodes.get(index);
    StringBuilder target = new StringBuilder(node.proxyTo);
    String pathInfo = request.getPathInfo();
    if (pathInfo != null) target.append(pathInfo);
    String query = request.getQueryString();
    if (query != null) target.append("?").append(query); // TODO URLEncode?
    String uri = URI.create(target.toString()).normalize().toString();
    return uri;
  }
  
  /** A node that is part of the cluster to which requests are proxied. */
  public static class BalancerNode {
    
    /** The url for the node. */
    public String proxyTo;
    
    /**
     * Constructor.
     * @param proxyTo the url for the node.
     */
    public BalancerNode(String proxyTo) {
      this.proxyTo = proxyTo;
    }
  }

}
