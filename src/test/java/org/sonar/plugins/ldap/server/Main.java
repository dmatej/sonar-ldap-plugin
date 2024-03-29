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

/**
 * Settings for Sonar:
 * <pre>
 * sonar.security.realm: LDAP
 * ldap.url: ldap://localhost:1024
 * ldap.baseDn: dc=example,dc=org
 * ldap.group.baseDn: dc=example,dc=org
 * </pre>
 */
public class Main {

  public static void main(String[] args) throws Exception {
    ApacheDS server = ApacheDS.start();
    server.importLdif(Main.class.getResourceAsStream("/static-groups.ldif"));
    System.out.println(server.getUrl());
  }

}
