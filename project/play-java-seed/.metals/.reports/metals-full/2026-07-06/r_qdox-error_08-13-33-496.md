error id: file:///mnt/c/Users/Vivobook%20Pro/Documents/HEIG-VD/TB/travail_de_bachelor/project/play-java-seed/app/model/entities/PoolEntry.java
file:///mnt/c/Users/Vivobook%20Pro/Documents/HEIG-VD/TB/travail_de_bachelor/project/play-java-seed/app/model/entities/PoolEntry.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[26,34]

error in qdox parser
file content:
```java
offset: 614
uri: file:///mnt/c/Users/Vivobook%20Pro/Documents/HEIG-VD/TB/travail_de_bachelor/project/play-java-seed/app/model/entities/PoolEntry.java
text:
```scala
package model.entities;

import jakarta.persistence.*;
import model.entities.unit.Unit;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "pool_entry")
@IdClass(PoolEntry.PoolEntryId.class)
public class PoolEntry {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(nullable = false)
    private Integer number;

    @Column(name = "created_at", ,@@ nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class PoolEntryId implements Serializable {
        private Long pool;
        private Long unit;

        public PoolEntryId() {
        }

        public PoolEntryId(Long pool, Long unit) {
            this.pool = pool;
            this.unit = unit;
        }

        public Long getPool() {
            return pool;
        }

        public void setPool(Long pool) {
            this.pool = pool;
        }

        public Long getUnit() {
            return unit;
        }

        public void setUnit(Long unit) {
            this.unit = unit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PoolEntryId that)) return false;
            return java.util.Objects.equals(pool, that.pool) && java.util.Objects.equals(unit, that.unit);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(pool, unit);
        }
    }
}

```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:560)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:691)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:688)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:688)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:940)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	java.base/java.lang.Thread.run(Thread.java:1583)
```
#### Short summary: 

QDox parse error in file:///mnt/c/Users/Vivobook%20Pro/Documents/HEIG-VD/TB/travail_de_bachelor/project/play-java-seed/app/model/entities/PoolEntry.java