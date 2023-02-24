# Foreword
For IBF-TFIP Candidates using this class for your assessment:

* To avoid suspicion of plagiarism, do:
    * (Preferred) Use this class in one of your existing public repository, and/or
    * Link this repo to your submission README.md

If you use this class, it is assumed that you have read and understood both [Disclaimer](#disclaimers) and [Limitations](#limitations). 

**Use at your own risk**.


# Contents
* [Credits](#credits)
* [Disclaimer](#disclaimers)
* [Overview](#overview)
    * [Setup](#setup)
    * [Single-Stage Aggregation](#single-stage-aggregation)
    * [Multi-Stage Aggregation](#multi-stage-aggregation)
* [Working Examples](#working-examples)



## Credits
This Java Class is adapted ~~(read: stolen)~~ from [this](https://stackoverflow.com/questions/59697496/how-to-do-a-mongo-aggregation-query-in-spring-data) StackOverflow thread. 

I'm not that good at programming. I'm just good at searching Google.
But sometimes they are the same thing. 


## Disclaimers
1. This class **DOES NOT** replace the need to know Mongo Aggregation Queries.
2. This class **DOES NOT** replace the functionalities provided by `org.springframework.data.mongodb.core.aggregation.*`. There may be some functionalities that Spring provides that this class cannot do. **USE AT YOUR OWN RISK**.
3. This class is __possbly__ **unsafe for production**. 

See [Limitations](#limitations) for details.


## Overview
Spring's `MongoTemplate` provides some methods to form and run Mongo Aggregation Queries. However, these methods are (1) very-far-removed from the native mongo query, (2) inconsistent amongst themselves, and (3) not a one-to-one map to native mongo query.

This repo showcases the `GenericAggregationOperation` class that implements `AggregationOperation` interface. It creates an `AggregationOperation` directly from native mongo query. The created instance can be used directly in the aggregation pipeline. Of course, this class bypasses all the checks that other methods may have. 

**Use at your own risk**. 

See [Limitations](#limitations).

### Setup
To use this method, copy the `GenericAggregationOperation.java` class to your project folder, ideally under the `utils` folder. Below are examples of how to use this class:

### Single-Stage Aggregation

Assume you want to do the following Mongo aggregation:
```
db.games.aggregate([
    {
        $match: {'gid': 5}
    }
])
```

First, you define the native Mongo Query as a String:
```
final String MONGO_MATCH_GID = 
"""
    {
        $match: {'gid': %s}
    }
""".formatted(id);
```

Then pass the String to the `GenericAggregationOperation` constructor to create an `AggregationOperation` instance:
```
AggregationOperation matchGid = new GenericAggregationOperation(MONGO_MATCH_GID);
```

And use the created `AggregationOperation` as you normally would:
```
Aggregation pipeline = Aggregation.newAggregation(matchGid);
AggregationResults<Document> res = mongoTemplate.aggregate(pipeline, COLLECTION_GAMES, Document.class);
```


### Multi-Stage Aggregation

For a more realistic example, assume you want to perform this multi-stage aggregation:
```
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
```

Similar to the previous section, you first define the each stage as a separate String:

```
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
```

Then pass each String to the `GenericAggregationOperation` constructor to create instances of `AggregationOperation`:

```
AggregationOperation matchGid = new GenericAggregationOperation(MONGO_MATCH_GID);
AggregationOperation projectGames = new GenericAggregationOperation(MONGO_PROJECT_GAMES);
AggregationOperation groupNull = new GenericAggregationOperation(MONGO_GROUP_NULL);        
AggregationOperation projectOutput = new GenericAggregationOperation(MONGO_PROJECT_OUTPUT);
```

And finally use the `AggregationOperation`s as you normally would:
```
Aggregation pipeline = Aggregation.newAggregation(matchGid, projectGames, groupNull, projectOutput);
AggregationResults<Document> res = mongoTemplate.aggregate(pipeline, COLLECTION_GAMES, Document.class);
```
## Working Examples

If you want to see the above code in action, you can clone this repo:
```
git clone https://github.com/GuyAtTheFront/Java-Mongo-GenericAggregationOperation.git
```

Import game.json into boardgames database as games collection:
```
mongoimport "mongodb://localhost:27017" -d boardgames -c games --jsonArray --file json/game.json --drop
```

Then start the spring-boot application:
```
mvn spring-boot:run
```

The REST endpoints are listed on `localhost:8080/` for you to test.

## Limitations

* This class uses raw-Strings for Mongo Aggregation Queries, and is vulnerable to injection. The extent of this vulnerablility depends on (1) the structure of the aggregation pipeline, (2) the `Aggregate.createAggregation` method implementation, and (3) the Java-Mongo driver implementation.

* A possible way to safeguard against this vulnerability is to use `AggregationExpression.from(MongoExpression.create(MONGO_QUERY_STRING))` instead.

* I have opted not to implemented the above solution because I have not tested it rigourously - in my mind, such implementation would add additional layers of complexity without certainty that it'll adequately address the vulnerability.

* I am not experienced enough to address this or any other problems that may arise from bypassing the methods that the Spring developers have provided. There *should* be a good reason why they did not expose this fairly obvious functionality in their API. 

* I leave this work for the more experienced / determined.



