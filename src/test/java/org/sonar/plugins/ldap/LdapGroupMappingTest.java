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
package org.sonar.plugins.ldap;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.sonar.api.config.Settings;

public class LdapGroupMappingTest {

  @Test
  public void defaults() {
    LdapGroupMapping groupMapping = new LdapGroupMapping(new Settings());

    assertThat(groupMapping.getBaseDn(), equalTo(null));
    assertThat(groupMapping.getObjectClass(), equalTo("groupOfUniqueNames"));
    assertThat(groupMapping.getIdAttribute(), equalTo("cn"));
    assertThat(groupMapping.getMemberAttribute(), equalTo("uniqueMember"));
    assertThat(groupMapping.getRequest(), equalTo("(&(objectClass={0})({1}={2}))"));

    assertThat(groupMapping.toString(), equalTo("LdapGroupMapping{" +
      "baseDn=null," +
      " objectClass=groupOfUniqueNames," +
      " idAttribute=cn," +
      " memberAttribute=uniqueMember," +
      " request=(&(objectClass={0})({1}={2}))}"));
  }

  @Test
  public void test() {
    Settings settings = new Settings()
        .setProperty("ldap.group.baseDn", "ou=groups,o=mycompany")
        .setProperty("ldap.group.objectClass", "groupOfUniqueNames")
        .setProperty("ldap.group.idAttribute", "cn")
        .setProperty("ldap.group.memberAttribute", "uniqueMember")
        .setProperty("ldap.group.request", "(&(objectClass={0})({1}={3}))");


    LdapGroupMapping groupMapping = new LdapGroupMapping(settings);
    LdapSearch search = groupMapping.createSearch(null, "cn=Tester Testerovich,ou=users,dc=example,dc=org", "tester");
    assertThat(search.getBaseDn(), equalTo("ou=groups,o=mycompany"));
    assertThat(search.getRequest(), equalTo("(&(objectClass={0})({1}={3}))"));
    assertThat(search.getParameters(), equalTo(new String[] {"groupOfUniqueNames", "uniqueMember",
        "cn=Tester Testerovich,ou=users,dc=example,dc=org", "tester"}));
    assertThat(search.getReturningAttributes(), equalTo(new String[] {"cn"}));

    assertThat(groupMapping.toString(), equalTo("LdapGroupMapping{" +
      "baseDn=ou=groups,o=mycompany," +
      " objectClass=groupOfUniqueNames," +
      " idAttribute=cn," +
      " memberAttribute=uniqueMember," +
      " request=(&(objectClass={0})({1}={3}))}"));
  }
}
