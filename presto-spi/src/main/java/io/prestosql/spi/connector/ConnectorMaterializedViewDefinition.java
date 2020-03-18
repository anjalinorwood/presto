/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.spi.connector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

public class ConnectorMaterializedViewDefinition
        extends ConnectorViewDefinition
{
    private final Map<String, Object> properties;
    private final Optional<String> comment;

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    @Override
    public Optional<String> getComment()
    {
        return comment;
    }

    @JsonCreator
    public ConnectorMaterializedViewDefinition(
            @JsonProperty("originalSql") String originalSql,
            @JsonProperty("viewExpandedText") Optional<String> viewExpandedText,
            @JsonProperty("catalog") Optional<String> catalog,
            @JsonProperty("schema") Optional<String> schema,
            @JsonProperty("columns") List<ViewColumn> columns,
            @JsonProperty("owner") Optional<String> owner,
            @JsonProperty("comment") Optional<String> comment,
            @JsonProperty("properties") Map<String, Object> properties)
    {
        super(originalSql, viewExpandedText, catalog, columns, owner, comment, false);
        this.comment = comment;
        this.properties = properties;
    }

    @JsonProperty
    public boolean isPrestoView()
    {
        throw new UnsupportedOperationException("'isPrestoView' operation is unsupported on materialized view");
    }

    @Override @JsonProperty
    public boolean isRunAsInvoker()
    {
        throw new UnsupportedOperationException("'isRunAsInvoker' operation is unsupported on materialized view");
    }

    @Override
    public String toString()
    {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        getOwner().ifPresent(value -> joiner.add("owner=" + value));
        joiner.add("runAsInvoker=" + isRunAsInvoker());
        joiner.add("columns=" + getColumns());
        getCatalog().ifPresent(value -> joiner.add("catalog=" + value));
        getSchema().ifPresent(value -> joiner.add("schema=" + value));
        joiner.add("originalSql=[" + getOriginalSql() + "]");
        joiner.add("comment" + comment.toString());
        joiner.add("properties=" + properties);
        return getClass().getSimpleName() + joiner.toString();
    }
}
