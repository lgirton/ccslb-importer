package com.sdgesi;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.RenameCollectionOptions;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.SneakyThrows;
import lombok.extern.jbosslog.JBossLog;
import org.apache.camel.*;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;
import org.bson.Document;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@QuarkusMain
@JBossLog
public class ContractorImporterApplication extends EndpointRouteBuilder implements QuarkusApplication {

    static final String CAMEL_SPLIT_INDEX_PROPERTY = "CamelSplitIndex";
    static final String CAMEL_SPLIT_COMPLETE_PROPERTY = "CamelSplitComplete";
    static final String IS_COMPLETE_PROPERTY = "IsComplete";
    static final String CHUNK_PROPERTY = "Chunk";
    static final String CAMEL_GROUPED_EXCHANGE_PROPERTY = "CamelGroupedExchange";

    static final String ROUTE_EXTRACT = "direct:extract";
    static final String ROUTE_TRANSFORM = "direct:transform";
    static final String ROUTE_LOAD = "direct:load";

    @Inject
    CamelContext context;

    @Inject
    MongoClient mongoClient;

    @Inject
    Configuration config;


    @Override
    public int run(String... args) {

        initializeDb();

        extractTransformLoad();

        cleanup();

        return 0;
    }

    @Override
    public void configure() {

        BindyCsvDataFormat bindy = new BindyCsvDataFormat(Contractor.class);

        String host = config.getHost();
        String uri = config.getUri();

        from(ROUTE_EXTRACT).streamCaching()
                .id("extract")
                .log(LoggingLevel.INFO, String.format("Extracting contractors from REST GET API %s/%s", host, uri))
                .toF("rest:get:%s&host=%s", uri, host)
                .split().tokenize("\n").streaming()
                .filter(simple("${header.CamelSplitIndex} > 0"))
                .to(ROUTE_TRANSFORM);

        from(ROUTE_TRANSFORM)
                .id("transform")
                .unmarshal(bindy)
                .marshal().json()
                .convertBodyTo(String.class)
                .process(labelChunk())
                .aggregate(simple("${header.Chunk}"), new ChunkListAggregationStrategy())
                .completion(checkChunkLimitOrSplitComplete())
                .to(ROUTE_LOAD);

        from(ROUTE_LOAD)
                .id("load")
                .log("Wrote ${body} to DB")
                .toF("mongodb:camelMongoClient?database=%s&collection=%s&operation=insert&lazyStartProducer=false&exchangePattern=InOnly", config.getDbName(), config.getStagingCollection());

    }

    private Predicate checkChunkLimitOrSplitComplete() {
        return exchange -> {
            int size = Optional.ofNullable(exchange.getProperty(CAMEL_GROUPED_EXCHANGE_PROPERTY, List.class)).map(List::size).orElse(0);
            boolean complete = exchange.getProperty(IS_COMPLETE_PROPERTY, Boolean.class);
            return config.getChunkSize() == size || complete;
        };
    }


    private Processor labelChunk() {
        return exchange -> {
            int index = calculateAdjustedSplitIndex(exchange);
            exchange.setProperty(CHUNK_PROPERTY, (index / config.getChunkSize()) + 1);
        };
    }

    private int calculateAdjustedSplitIndex(Exchange exchange) {
        int camelSplitIndex =
                Optional.ofNullable(exchange.getProperty(CAMEL_SPLIT_INDEX_PROPERTY, Integer.class)).orElse(1);
        return camelSplitIndex - 1;
    }

    @SneakyThrows
    private void extractTransformLoad() {
        log.infof("Starting ETL processing");
        try (FluentProducerTemplate template = context.createFluentProducerTemplate()) {
            template.to(ROUTE_EXTRACT).request();
        }
    }

    private void cleanup() {
        MongoDatabase db = mongoClient.getDatabase(config.getDbName());

        String stagingCollectionName = config.getStagingCollection();
        String collectionName = config.getCollectionName();

        MongoCollection<Document> staging = db.getCollection(stagingCollectionName);

        log.infof("Renaming collection %s to %s", stagingCollectionName, collectionName);

        staging.renameCollection(new MongoNamespace(db.getName(), collectionName),
                new RenameCollectionOptions().dropTarget(true));
    }

    private void initializeDb() {

        MongoDatabase db = mongoClient.getDatabase(config.getDbName());
        String stagingName = config.getStagingCollection();
        MongoCollection<Document> staging = db.getCollection(stagingName);

        log.infof("Dropping staging table: %s", stagingName);
        staging.drop();

        log.infof("Creating staging table: %s", stagingName);
        db.createCollection(stagingName);

        log.infof("Creating index on licenseNo");
        staging.createIndex(Indexes.ascending("licenseNo"));

    }

    private static class ChunkListAggregationStrategy extends AbstractListAggregationStrategy<String> {
        @Override
        public String getValue(Exchange exchange) {
            return exchange.getIn().getBody(String.class);
        }

        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            Exchange result = super.aggregate(oldExchange, newExchange);
            result.setProperty(IS_COMPLETE_PROPERTY, newExchange.getProperty(CAMEL_SPLIT_COMPLETE_PROPERTY));
            return result;
        }
    }
}
