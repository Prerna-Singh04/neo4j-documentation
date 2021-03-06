/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.docgen.refcard

class DatabaseManagementTest extends AdministrationCommandTestBase {
  val title = "DATABASE MANAGEMENT"
  override val linkId = "administration/databases"

  def text: String =
    """
###assertion=update-one
//

CREATE OR REPLACE DATABASE myDatabase
###

(★) Create a database named `myDatabase`. If a database with that name exists, then the existing database is deleted and a new one created.

###assertion=update-one
//

STOP DATABASE myDatabase
###

(★) Stop the database `myDatabase`.

###assertion=update-one
//

START DATABASE myDatabase
###

(★) Start the database `myDatabase`.

###assertion=show-one
//

SHOW DATABASES
###

List all databases in the system and information about them.

###assertion=show-one
//

SHOW DATABASE myDatabase
###

List information about the database `myDatabase`.

###assertion=show-nothing
//

SHOW DEFAULT DATABASE
###

List information about the default database.

###assertion=update-one
//

DROP DATABASE myDatabase IF EXISTS
###

(★) Delete the database `myDatabase`, if it exists.

"""
}
