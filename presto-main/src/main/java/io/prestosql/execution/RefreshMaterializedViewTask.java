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
package io.prestosql.execution;

import com.google.common.util.concurrent.ListenableFuture;
import io.prestosql.Session;
import io.prestosql.connector.CatalogName;
import io.prestosql.metadata.Metadata;
import io.prestosql.metadata.QualifiedObjectName;
import io.prestosql.security.AccessControl;
import io.prestosql.sql.analyzer.FeaturesConfig;
import io.prestosql.sql.parser.SqlParser;
import io.prestosql.sql.tree.Expression;
import io.prestosql.sql.tree.RefreshMaterializedView;
import io.prestosql.transaction.TransactionManager;

import javax.inject.Inject;

import java.util.List;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static io.prestosql.metadata.MetadataUtil.createQualifiedObjectName;
import static java.util.Objects.requireNonNull;

public class RefreshMaterializedViewTask
        implements DataDefinitionTask<RefreshMaterializedView>
{
    private final SqlParser sqlParser;

    @Inject
    public RefreshMaterializedViewTask(SqlParser sqlParser, FeaturesConfig featuresConfig)
    {
        this.sqlParser = requireNonNull(sqlParser, "sqlParser is null");
        requireNonNull(featuresConfig, "featuresConfig is null");
    }

    @Override
    public String getName()
    {
        return "REFRESH MATERIALIZED VIEW";
    }

    @Override
    public String explain(RefreshMaterializedView statement, List<Expression> parameters)
    {
        return "REFRESH MATERIALIZED VIEW " + statement.getName();
    }

    @Override
    public ListenableFuture<?> execute(RefreshMaterializedView statement, TransactionManager transactionManager, Metadata metadata, AccessControl accessControl, QueryStateMachine stateMachine, List<Expression> parameters)
    {
        Session session = stateMachine.getSession();
        QualifiedObjectName name = createQualifiedObjectName(session, statement, statement.getName());

        accessControl.checkCanDeleteFromTable(session.toSecurityContext(), name);
        accessControl.checkCanInsertIntoTable(session.toSecurityContext(), name);

        // OPEN QUESTION
        // A couple ways to implement refresh:
        // 1) Connector returns commands to be run in order to refresh the materialized view.
        // In a simple full refresh case, the commands will be of the form:
        // delete from materialized_view; insert into materialized_view select ..<materialized view definition>
        // When individual delete and insert commands are executed, access checks can be done by those commands.
        // An 'incremental refresh' implementation will look different. The 'delete' and 'insert' will be per partition.
        // (All the partitions that have been added, deleted, changed in the base table).
        // In a simple case, lets say, base_tab is partitioned on column 'p1' and matview is also partitioned on 'p1', then refresh would look like:
        // delete from matview where p1=1;
        // insert into matview select .. from base_tab .. where .. and p1=1 group by ..
        // delete from matview where p1=2;
        // insert into matview select .. from base_tab .. where .. and p1=2 group by ..
        // .....
        // There maybe more involved cases where base_tab is partitioned on say (p1, p2) and matview only on (p1).
        // The where clauses in the above statements will look different.
        // So, how many of these delete-insert pairs get generated depends on number of partitions changed.
        // Moreover, for other connectors (non-iceberg), there may be additional commands that need to be run to store some metadata
        // (e.g. the table token) in table properties.
        // Given this, I think we need a flexible infrastructure like 1) for refresh.
        // 2) Another option is to rewrite 'refresh materialized view' at planner level.
        // If all that is needed from the connector is what predicates to apply in delete and insert
        // (or if presto added insert overwrite) then the API could look like:
        // Constraint metadata.getRefreshMaterializedViewConstraint(session, name)
        // and it should be invoked during planing like applyFilter is invoked and the plan should be generated automatically by presto.
        // We still need a 'metadata.refreshMaterializedView' method for other operations like updating metadata.
        List<String> refreshCommands = metadata.refreshMaterializedView(session, name);
        return immediateFuture(refreshCommands);
    }
}
