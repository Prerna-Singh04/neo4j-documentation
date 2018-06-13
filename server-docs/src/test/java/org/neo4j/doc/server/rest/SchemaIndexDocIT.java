/*
 * Copyright (c) 2002-2018 "Neo4j,"
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
package org.neo4j.doc.server.rest;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.neo4j.function.Factory;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.kernel.impl.annotations.Documented;
import org.neo4j.server.rest.domain.JsonParseException;
import org.neo4j.test.GraphDescription;
import org.neo4j.test.mockito.matcher.Neo4jMatchers;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.neo4j.graphdb.Label.label;
import static org.neo4j.helpers.collection.MapUtil.map;
import static org.neo4j.server.rest.domain.JsonHelper.createJsonFrom;
import static org.neo4j.server.rest.domain.JsonHelper.jsonToList;
import static org.neo4j.server.rest.domain.JsonHelper.jsonToMap;
import static org.neo4j.test.mockito.matcher.Neo4jMatchers.containsOnly;

public class SchemaIndexDocIT extends AbstractRestFunctionalTestBase
{

    private List<Map<String,Object>> retryOnStillPopulating( Callable<String> callable ) throws Exception
    {
        long endTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis( 1 );
        List<Map<String,Object>> serializedList;
        do
        {
            String result = callable.call();
            serializedList = jsonToList( result );
            if ( System.currentTimeMillis() > endTime )
            {
                fail( "Indexes didn't populate correctly, last result '" + result + "'" );
            }
        }
        while ( stillPopulating( serializedList ) );
        return serializedList;
    }

    private boolean stillPopulating( List<Map<String,Object>> serializedList )
    {
        // We've created an index. That HTTP call for creating the index will return
        // immediately and indexing continue in the background. Querying the index endpoint
        // while index is populating gives back additional information like population progress.
        // This test below will look at the response of a "get index" result and if still populating
        // then return true so that caller may retry the call later.
        for ( Map<String,Object> map : serializedList )
        {
            if ( map.containsKey( "population_progress" ) )
            {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings( "unchecked" )
    @Documented( "Get all indexes." )
    @Test
    @GraphDescription.Graph
    public void get_indexes() throws Exception
    {
        data.get();

        String labelName1 = labels.newInstance(), propertyKey1 = properties.newInstance();
        String labelName2 = labels.newInstance(), propertyKey2 = properties.newInstance();
        createIndex( labelName1, propertyKey1 );
        createIndex( labelName2, propertyKey2 );

        List<Map<String,Object>> serializedList = retryOnStillPopulating(
                () -> gen.get().noGraph().expectedStatus( 200 ).get( getSchemaIndexUri() ).entity() );

        Map<String,Object> index1 = new HashMap<>();
        index1.put( "label", labelName1 );
        index1.put( "property_keys", singletonList( propertyKey1 ) );

        Map<String,Object> index2 = new HashMap<>();
        index2.put( "label", labelName2 );
        index2.put( "property_keys", singletonList( propertyKey2 ) );

        assertThat( serializedList, hasItems( index1, index2 ) );
    }

    private IndexDefinition createIndex( String labelName, String propertyKey )
    {
        try ( Transaction tx = graphdb().beginTx() )
        {
            IndexDefinition indexDefinition = graphdb().schema().indexFor( label( labelName ) ).on( propertyKey )
                    .create();
            tx.success();
            return indexDefinition;
        }
    }

    private final Factory<String> labels =  UniqueStrings.withPrefix( "label" );
    private final Factory<String> properties =  UniqueStrings.withPrefix( "property" );
}
