package com.sdgesi;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.RenameCollectionOptions;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.apache.camel.CamelContext;
import org.bson.Document;

import javax.inject.Inject;

@QuarkusMain
public class ContractorImporterMain implements QuarkusApplication {

    @Inject
    CamelContext context;

    @Inject
    MongoClient mongoClient;

    @Inject
    Configuration config;

    @Override
    public int run(String... args) throws Exception {

        MongoDatabase db = mongoClient.getDatabase(config.getDbName());

        MongoCollection<Document> staging = initializeStagingCollection(db);

        extractAndLoadStaging();

        renameAndDropStagingCollection(db, staging);

        return 0;
    }

    private void extractAndLoadStaging() {
        context.createFluentProducerTemplate().to("direct:import").request();
    }

    private void renameAndDropStagingCollection(MongoDatabase db, MongoCollection<Document> staging) {
        staging.renameCollection(new MongoNamespace(db.getName(), config.getCollectionName()),
                new RenameCollectionOptions().dropTarget(true));
    }

    private MongoCollection<Document> initializeStagingCollection(MongoDatabase db) {
        MongoCollection<Document> staging = db.getCollection(config.getStagingCollection());
        if (staging != null) {
            staging.drop();
            db.createCollection(config.getStagingCollection());
        }
        staging.createIndex(Indexes.ascending("licenseNo"));
        return staging;
    }
}
