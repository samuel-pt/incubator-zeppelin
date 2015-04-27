/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zeppelin.ticket;

import java.util.Calendar;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hayssams on 24/04/15.
 * Very simple ticket container
 * No cleanup is done, since the same user accross different devices share the same ticket
 * The Map size is at most the number of different user names having access to a Zeppelin instance
 */


public class TicketContainer {
  private static class Entry {
    public final String ticket;
    // lastAccessTime still unused
    public final long lastAccessTime;
    Entry(String ticket) {
      this.ticket = ticket;
      this.lastAccessTime = Calendar.getInstance().getTimeInMillis();
    }
  }
  private Map<String, Entry> sessions = new ConcurrentHashMap<>();

  public static final TicketContainer instance = new TicketContainer();

  public boolean isValid(String principal, String ticket) {
    // Always accept anonymous with ticket anonymous
    if ("anonymous".equals(principal) && "anonymous".equals(ticket))
      return true;
    Entry entry = sessions.get(principal);
    return entry != null && entry.ticket.equals(ticket);
  }

  public synchronized String getTicket(String principal) {
    Entry entry = sessions.get(principal);
    String ticket;
    if (entry == null) {
      if (principal.equals("anonymous"))
        ticket = "anonymous"; // enable testing on anonymous when ticket is required in the url
      else
        ticket = UUID.randomUUID().toString();
    }
    else {
      ticket = entry.ticket;
    }
    entry = new Entry(ticket);
    sessions.put(principal, entry);
    return ticket;
  }
}
