/*
 * Sonar LDAP Plugin
 * Copyright (C) 2009 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.ldap.server;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapServer extends ExternalResource {
  private static final Logger LOG = LoggerFactory.getLogger(LdapServer.class);

  private ApacheDS server;
  private String ldif;


  /**
   *
   * @param ldifResourceName
   */
  public LdapServer(String ldifResourceName) {
    this.ldif = ldifResourceName;
  }

  @Override
  protected void before() throws Throwable {
    server = ApacheDS.start();
    activateNis();
    if (ldif != null) {
      importLdiff(ldif);
    }
  }

  public void importLdiff(String ldifResourceName) throws Exception {
    this.ldif = ldifResourceName;
    server.importLdif(LdapServer.class.getResourceAsStream(ldif));
  }

  @Override
  protected void after() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String getUrl() {
    return server.getUrl();
  }

  public void disableAnonymousAccess() {
    server.disableAnonymousAccess();
  }

  public void enableAnonymousAccess() {
    server.enableAnonymousAccess();
  }

  private Hashtable connectionEnvApache() {
    Hashtable env = new Hashtable(11);
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, getUrl());
    return env;
  }

  private boolean isNisDisabled(DirContext ctx) throws NamingException {
    Attributes nisAttrs = ctx.getAttributes("cn=nis,ou=schema");
    if (nisAttrs.get("m-disabled") != null) {
      return ((String) nisAttrs.get("m-disabled").get()).equalsIgnoreCase("TRUE");
    }

    return true;
  }

  public void activateNis() throws Exception {
    LOG.trace("activateNis()");
    try {
      DirContext ctx = new InitialDirContext(connectionEnvApache());
      boolean isNisDisabled = isNisDisabled(ctx);
      LOG.debug("nis disabled: {}", isNisDisabled);

      // if nis is disabled then enable it
      if (isNisDisabled) {
        // FIXME: bug in ApacheDS?: replace does not work
        Attribute disabled = new BasicAttribute("m-disabled", "TRUE");
        Attribute disabled2 = new BasicAttribute("m-disabled", "FALSE");
        ModificationItem[] mods = new ModificationItem[] {
            new ModificationItem(DirContext.REMOVE_ATTRIBUTE, disabled),
            new ModificationItem(DirContext.ADD_ATTRIBUTE, disabled2)
            };
        ctx.modifyAttributes("cn=nis,ou=schema", mods);
      }
      LOG.debug("nis enabled: {}", !isNisDisabled);
    } catch (NamingException e) {
      throw new IllegalStateException("Cannot initialize NIS scheme.", e);
    }
  }

}
