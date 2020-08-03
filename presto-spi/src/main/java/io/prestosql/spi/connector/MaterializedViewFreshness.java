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

import java.util.Objects;
import java.util.Optional;

public final class MaterializedViewFreshness
{
    private final boolean isFresh;
    private final Optional<String> incrementalRefreshPredicate;

    public MaterializedViewFreshness(boolean isFresh, Optional<String> incrementalRefreshPredicate)
    {
        this.isFresh = isFresh;
        this.incrementalRefreshPredicate = incrementalRefreshPredicate;
    }

    public boolean getIsFresh()
    {
        return isFresh;
    }

    public Optional<String> getIncrementalRefreshPredicate()
    {
        return incrementalRefreshPredicate;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        MaterializedViewFreshness that = (MaterializedViewFreshness) obj;
        return Objects.equals(isFresh, that.isFresh) &&
                Objects.equals(incrementalRefreshPredicate, that.incrementalRefreshPredicate);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(isFresh, incrementalRefreshPredicate);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("MaterializedViewFreshness{");
        sb.append("isFresh=").append(isFresh);
        sb.append(", incremenalRefreshPredicate=").append(incrementalRefreshPredicate);
        sb.append('}');
        return sb.toString();
    }
}
