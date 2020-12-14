package com.sdgesi;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ContractorImporterRouteBuilder extends EndpointRouteBuilder {

    static final String CAMEL_SPLIT_INDEX = "CamelSplitIndex";
    static final String CAMEL_SPLIT_COMPLETE = "CamelSplitComplete";
    static final String IS_COMPLETE = "IsComplete";

    @Inject
    Configuration config;


    @Override
    public void configure() {

        BindyCsvDataFormat bindy = new BindyCsvDataFormat(Contractor.class);

        from("direct:import").streamCaching()
                .toF("rest:get:%s&host=%s", config.getUri(), config.getHost())
                .split().tokenize("\n").streaming()
                .filter(simple("${header.CamelSplitIndex} > 0"))
                .unmarshal(bindy)
                .marshal().json()
                .convertBodyTo(String.class)
                .process(calculateChunkOffset())
                .aggregate(simple("${header.batch}"), chunkedOffsetAggregator())
                .completionPredicate(checkBatchCompleted())
                .log(String.format("Wrote %d records to DB", config.getChunkSize()))
                .toF("mongodb:camelMongoClient?database=%s&collection=%s&operation=insert&lazyStartProducer=false&exchangePattern=InOnly", config.getDbName(), config.getStagingCollection());

    }

    private Predicate checkBatchCompleted() {
        return exchange -> {
            var body = exchange.getIn().getBody(ArrayList.class);
            if (body == null)
                return true;
            boolean isComplete = exchange.getProperty(IS_COMPLETE, Boolean.class);
            return body.size() == config.getChunkSize() || isComplete;
        };
    }

    private Processor calculateChunkOffset() {
        return exchange -> {
            int index = getSplitIndex(exchange);
            exchange.setProperty("batch", (index / config.getChunkSize()) + 1);
        };
    }


    private AggregationStrategy chunkedOffsetAggregator() {
        return (oldExchange, newExchange) -> {
            boolean isComplete = checkFileSplitComplete(newExchange).orElse(false);
            String body = newExchange.getIn().getBody(String.class);

            if (oldExchange == null) {
                newExchange.setProperty(IS_COMPLETE, isComplete);
                List<String> strings = new ArrayList<>();
                strings.add(body);
                newExchange.getIn().setBody(strings);
                return newExchange;
            } else {
                oldExchange.setProperty(IS_COMPLETE, isComplete);
                var strings = oldExchange.getIn().getBody(ArrayList.class);
                //noinspection unchecked
                strings.add(body);
                return oldExchange;
            }


        };
    }

    private Optional<Boolean> checkFileSplitComplete(Exchange exchange) {
        return Optional.ofNullable(exchange.getProperty(CAMEL_SPLIT_COMPLETE, Boolean.class));
    }

    private int getSplitIndex(Exchange exchange) {
        int camelSplitIndex =
                Optional.ofNullable(exchange.getProperty(CAMEL_SPLIT_INDEX, Integer.class)).orElse(1);
        return camelSplitIndex - 1;
    }
}
