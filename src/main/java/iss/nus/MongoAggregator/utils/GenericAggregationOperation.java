package iss.nus.MongoAggregator.utils;

import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

public class GenericAggregationOperation implements AggregationOperation {

    private String jsonOperation;

    public GenericAggregationOperation(String jsonOperation) {
        this.jsonOperation = jsonOperation;
      }

    @Override
    public org.bson.Document toDocument(AggregationOperationContext aggregationOperationContext) {
        return aggregationOperationContext.getMappedObject(org.bson.Document.parse(jsonOperation));
    }
    
}
