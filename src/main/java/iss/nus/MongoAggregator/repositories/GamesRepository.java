package iss.nus.MongoAggregator.repositories;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Repository;

import iss.nus.MongoAggregator.utils.GenericAggregationOperation;

import static iss.nus.MongoAggregator.utils.Constants.*;

import java.util.List;
import java.util.Optional;

@Repository
public class GamesRepository {
    
    @Autowired
    MongoTemplate mongoTemplate;

    public Optional<String> getSimpleAggregation(Integer id) {
        /*
        db.games.aggregate([
            {
                $match: {'gid': 5}
            }
        ])
         */

        final String MONGO_MATCH_GID = 
        """
            {
                $match: {'gid': %s}
            }
        """.formatted(id);
        
        AggregationOperation matchGid = new GenericAggregationOperation(MONGO_MATCH_GID);
        
        Aggregation pipeline = Aggregation.newAggregation(matchGid);
        AggregationResults<Document> res = mongoTemplate.aggregate(pipeline, COLLECTION_GAMES, Document.class);
        return (res.getMappedResults().isEmpty()) ? Optional.empty() : Optional.of(res.getMappedResults().get(0).toJson());
    }

    
    public List<Document> getComplexAggregation(String word) {
        /*
        db.games.aggregate([
            {
                $match: {'name': {$regex: 'die'}}
            }
            ,
            {
                $project: {
                    _id: 0,
                    'timestamp': {$toString:'$$NOW'},
                    'name': {$concat: ['$name', ' (', {$toString: '$year'}, ')']},
                    'rank': {$concat: [{$toString: '$ranking'} , ' (', {$toString: '$users_rated'}, ' users rated)']},
                }
            }
            ,
            {
                $group: {
                    _id: null,
                    games: {$push: '$$ROOT'}
                }
            }
            ,
            {
                $project: {
                    _id: 0,
                    status: '200',
                    timestamp: {$toString: '$$NOW'},
                    games: '$games'
                }
            }
        ]);
         */

         final String MONGO_MATCH_GID = 
        """
            {
                $match: {'name': {$regex: %s}}
            }
        """.formatted("'" + word + "'"); // use single quotes for String

        final String MONGO_PROJECT_GAMES = 
        """
            {
                $project: {
                    _id: 0,
                    'timestamp': {$toString:'$$NOW'},
                    'name': {$concat: ['$name', ' (', {$toString: '$year'}, ')']},
                    'rank': {$concat: [{$toString: '$ranking'} , ' (', {$toString: '$users_rated'}, ' users rated)']}
                }
                }
        """;

        final String MONGO_GROUP_NULL = 
        """
            {
                $group: {
                    _id: null,
                    games: {$push: '$$ROOT'}
                }
            }
        """;

        final String MONGO_PROJECT_OUTPUT = 
        """
            {
                $project: {
                    _id: 0,
                    status: '200',
                    timestamp: {$toString: '$$NOW'},
                    games: '$games'
                }
            }
        """;

        AggregationOperation matchGid = new GenericAggregationOperation(MONGO_MATCH_GID);
        AggregationOperation projectGames = new GenericAggregationOperation(MONGO_PROJECT_GAMES);
        AggregationOperation groupNull = new GenericAggregationOperation(MONGO_GROUP_NULL);        
        AggregationOperation projectOutput = new GenericAggregationOperation(MONGO_PROJECT_OUTPUT);

        Aggregation pipeline = Aggregation.newAggregation(matchGid, projectGames, groupNull, projectOutput);
        
        AggregationResults<Document> res = mongoTemplate.aggregate(pipeline, COLLECTION_GAMES, Document.class);

        return res.getMappedResults();
    }

}
